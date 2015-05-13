package pt.ulisboa.tecnico.cmov.airdesk.wifiDirect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmov.airdesk.dto.TextFileDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.UserDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.WorkspaceDto;
import pt.ulisboa.tecnico.cmov.airdesk.utility.ConnectionHandler;
import pt.ulisboa.tecnico.cmov.airdesk.utility.FlowManager;
import pt.ulisboa.tecnico.cmov.airdesk.utility.FlowProxy;
import pt.ulisboa.tecnico.cmov.airdesk.utility.MessagePack;
import pt.ulisboa.tecnico.cmov.airdesk.utility.MyAsyncTask;
import pt.ulisboa.tecnico.cmov.airdesk.utility.Utils;

public class SimWifiDirectService extends WifiDirectService implements
        SimWifiP2pManager.PeerListListener, SimWifiP2pManager.GroupInfoListener {

    //Termite
    private Messenger termiteService;
    private SimWifiP2pManager termiteManager;
    private SimWifiP2pManager.Channel termiteChannel;

    private SimWifiP2pSocketServer termiteSrvSocket = null;
    private HashMap<String, String> termiteIDConverter = null;

    private boolean isGO = false;

    private ServiceConnection termiteConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            termiteService = new Messenger(service);
            termiteManager = new SimWifiP2pManager(termiteService);
            termiteChannel = termiteManager.initialize(getApplication(), getMainLooper(), null);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            termiteService = null;
            termiteManager = null;
            termiteChannel = null;
        }
    };

    @Override
    protected void init() {
        // initialize the WDSim API
        SimWifiP2pSocketManager.Init(getApplicationContext());

        //Bind termite service
        bindService(new Intent(getApplicationContext(), SimWifiP2pService.class), termiteConnection, Context.BIND_AUTO_CREATE);

        //Initialize structures
        termiteIDConverter = new HashMap<>();

        //Create listener socket
        new IncomingCommTask().execute(null);
    }

    @Override
    protected void unBind() {
        unbindService(termiteConnection);
    }

    @Override
    protected void processGroup() {
        termiteManager.requestGroupInfo(termiteChannel, SimWifiDirectService.this);
    }

    @Override
    public void sendMessage(MessagePack message){
        sendMessageWithResponse(message, null);
    }

    //Make the connection without worrying about blocking
    public void sendMessageWithResponse(MessagePack message, ConnectionHandler<MessagePack> handler) {
        Log.e("MessageSend", "Sending Message");

        if(termiteIDConverter.containsValue(message.receiver))
            new OutgoingCommTask(handler).execute(message);
        else {
            Log.e("MessageSend", "Don't know receiver");
            if(handler != null) handler.onFailure();
        }
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList simWifiP2pDeviceList, SimWifiP2pInfo simWifiP2pInfo) {

        // CHECKS IF THE DEVICE IS GO
        isGO = simWifiP2pInfo.askIsGO();
        Log.e("GroupInfoAvailable", "isGO = " + isGO);

        ArrayList<String> ips = new ArrayList<>();
        // GETS DEVICES' IPS
        for(SimWifiP2pDevice device : simWifiP2pDeviceList.getDeviceList()){
            ips.add(device.getVirtIp());
        }

        // REMOVE UNREACHABLE IPS FROM THE HASH
        for(String key : termiteIDConverter.keySet()){
            String ip = termiteIDConverter.get(key);
            if(!ips.contains(ip))
                termiteIDConverter.remove(key);
        }

        // KNOW IDs. WILL SEND A USER_REQUEST MESSAGEPACK
        /**/
        for(SimWifiP2pDevice device : simWifiP2pDeviceList.getDeviceList()){
            if(!termiteIDConverter.containsValue(device.getVirtIp()) && !simWifiP2pInfo.getDeviceName().equals(device.deviceName)){
                MessagePack message = new MessagePack();
                message.receiver = device.getVirtIp();
                message.request = MessagePack.USER_REQUEST;
                message.type = MessagePack.TYPE.REQUEST;
                new OutgoingCommTask(null).execute(message);
            }
        }
        /**/
    }

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList simWifiP2pDeviceList) {
        /** /
        String ip = "";
        for (SimWifiP2pDevice device : simWifiP2pDeviceList.getDeviceList()) {
            ip =  device.getVirtIp();
        }

        new OutgoingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ip);
        /**/
    }

    // PROCESS MESSAGE
    private MessagePack processMessage(MessagePack message) {
        Log.e("Message Process", "Message is " + message.request);

        switch(message.request){
            case MessagePack.HELLO_WORLD:
                Log.e("Message Process", "Hello World! :D");
                return message;
                //return null;
            case MessagePack.MOUNT_WORKSPACE:
                FlowManager.receive_mountWorkspace((WorkspaceDto) message.dto);
                return null;
            case MessagePack.UNMOUNT_WORKSPACE:
                FlowManager.receive_mountWorkspace((WorkspaceDto) message.dto);
                return null;
            case MessagePack.ADD_FILE:
                FlowManager.receive_addFile(getApplicationContext(), (TextFileDto) message.dto);
                return null;
            case MessagePack.EDIT_FILE:
                FlowManager.receive_editFile(getApplicationContext(), (TextFileDto) message.dto);
                return null;
            case MessagePack.REMOVE_FILE:
                FlowManager.receive_removeFile(getApplicationContext(), (TextFileDto) message.dto);
                return null;
            default:
                return null;
        }
    }

    private MessagePack process(MessagePack message, String ip){
        if((message.request).equals(MessagePack.USER_REQUEST))
            return processUser(message, ip);
        return processMessage(message);
    }

    private MessagePack processUser(MessagePack message, String ip){
        if(message.type == MessagePack.TYPE.REQUEST){
            MessagePack pack = new MessagePack();
            pack.request = MessagePack.USER_REQUEST;
            pack.type = MessagePack.TYPE.REPLY;
            pack.dto = FlowProxy.getInstance().send_userID(getApplicationContext());
            return pack;
        }
        else {
            Log.e("User with IP", ip);
            Log.e("Has the ID", ((UserDto) message.dto).id);
            termiteIDConverter.put(((UserDto)message.dto).id, ip);
            return null;
        }
    }

    // ASYNC TASKS
    public class IncomingCommTask extends MyAsyncTask<Void, SimWifiP2pSocket, Void> {

        @Override
        protected Void doInBackground(Void param) {
            Log.d("SimWifiDirectService", "IncomingCommTask started (" + this.hashCode() + ").");

            try {
                termiteSrvSocket = new SimWifiP2pSocketServer(10001);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Log.e("Incoming","Esta a espera");
                    SimWifiP2pSocket sock = termiteSrvSocket.accept();
                    Log.e("Incoming","Recebeu socket antes do publish");
                    publishProgress(sock);
                    Log.e("Incoming", "Recebeu socket");
                } catch (IOException e) {
                    Log.d("Error accepting socket:", e.getMessage());
                    break;
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(SimWifiP2pSocket value) {
            MessageTreatmentTask task = new MessageTreatmentTask();
            Log.e("Incoming","added a Message Treatment Task");

            task.execute(value);
        }
    }

    public class OutgoingCommTask extends MyAsyncTask<MessagePack, Void, Void> {
        ConnectionHandler<MessagePack> handler;

        SimWifiP2pSocket cli = null;
        MessagePack message = null;

        public OutgoingCommTask(ConnectionHandler<MessagePack> handler){super(); this.handler = handler;}

        @Override
        protected Void doInBackground(MessagePack message) {
            this.message = message;

            long initialTime = System.currentTimeMillis();
            while(System.currentTimeMillis() - initialTime < 1000) {
                try {
                    cli = new SimWifiP2pSocket(message.receiver, 10001);
                    return null;
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.e("Outgoing", "Não conseguiu estabelecer ligação");
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            if(cli != null) {
                // SEND MESSAGE
                try {
                    Log.e("Outgoing", "A enviar a mensagem para o client");
                    cli.getOutputStream().write(Utils.objectToBytes(message));
                    Log.e("Outgoing", "Saiu daqui");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.e("Outgoing","Chegou aqui");
                MessageTreatmentTask task = new MessageTreatmentTask(handler);
                task.setDeviceIp(message.receiver);
                Log.e("Outgoing", "A executar o MessageTreatment");
                task.execute(cli);
            }
        }
    }

    public class MessageTreatmentTask extends MyAsyncTask<SimWifiP2pSocket, Void, Void> {

        String ip = null;
        public void setDeviceIp(String ip){
            this.ip = ip;
        }

        ConnectionHandler<MessagePack> handler = null;
        public MessageTreatmentTask(ConnectionHandler<MessagePack> handler) {
            super();
            this.handler = handler;
        }
        public MessageTreatmentTask(){
            super();
        }

        @Override
        protected Void doInBackground(SimWifiP2pSocket s) {
            ObjectInputStream ois;

            try {
                // RECEIVE MESSAGEPACK
                Log.e("MessagePackReceive", "Primeiro");
                ois = new ObjectInputStream(s.getInputStream());
                Log.e("MessagePackReceive", "UM E MEIO!");
                MessagePack message = (MessagePack) ois.readObject();
                Log.e("MessagePackReceive", "Segundo");
                // PROCESS DTO
                MessagePack sendPack = process(message, ip);
                if(sendPack != null) {
                    s.getOutputStream().write(Utils.objectToBytes(sendPack));
                    Log.e("MessagePackSend", "A enviar o socket");
                }
                s.close();
                if(handler != null) handler.onSuccess(message);

            } catch (IOException e) {
                e.printStackTrace();
                if(handler != null) handler.onFailure();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                if(handler != null) handler.onFailure();
            } catch (NullPointerException e){
                e.printStackTrace();
                if(handler != null) handler.onFailure();
            }
            return null;
        }
    }

}
