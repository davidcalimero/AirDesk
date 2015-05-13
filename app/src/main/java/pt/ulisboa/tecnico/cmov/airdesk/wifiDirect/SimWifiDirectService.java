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
    private ArrayList<MessageTreatmentTask> termiteComm = null;

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
        //termiteNickConverter = new HashMap<>();
        termiteComm = new ArrayList<>();

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
        Log.e("MessageSend", "Sending Message");
        // Send Message to someone
        if(termiteIDConverter.containsKey(message.receiver)){
            new OutgoingCommTask().execute(message);
        }
        else
            Log.e("MessageSend", "Don't know receiver");
    }

    //Make the connection without worrying about blocking
    public MessagePack sendMessageWithResponse(MessagePack message){
        SimWifiP2pSocket conn;
        ObjectInputStream ois;

        try {
            // Establishes the connection
            conn = new SimWifiP2pSocket(message.receiver, 10001);

            // Sends the message
            conn.getOutputStream().write(Utils.objectToBytes(message));

            // Waits for a pack from the other device
            ois = new ObjectInputStream(conn.getInputStream());

            // Get the received pack.
            MessagePack receivedPack = (MessagePack) ois.readObject();

            // Process pack?
            process(receivedPack, message.receiver);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList simWifiP2pDeviceList, SimWifiP2pInfo simWifiP2pInfo) {

        // CHECKS IF THE DEVICE IS GO
        isGO = simWifiP2pInfo.askIsGO();
        Log.e("GroupInfoAvailable", "isGO = " + isGO);

        // TERMINATE MESSAGETREAMENTTASKS
        for(MessageTreatmentTask t : termiteComm){
            t.cancel();
            termiteComm.remove(t);
        }

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
                new OutgoingCommTask().execute(message);
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
                return null;
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
            termiteComm.add(task);
            Log.e("Incoming","added a Message Treatment Task");

            task.execute(value);
        }
    }

    public class OutgoingCommTask extends MyAsyncTask<MessagePack, Void, Void> {

        SimWifiP2pSocket cli = null;
        MessagePack message = null;

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
                Log.e("Outgoing","Chegou aqui");
                MessageTreatmentTask task = new MessageTreatmentTask();
                task.setDeviceIp(message.receiver);
                termiteComm.add(task);
                Log.e("Outgoing", "A executar o MessageTreatment");
                task.execute(cli);

                // SEND MESSAGE
                try {
                    Log.e("Outgoing", "A enviar a mensagem para o client");
                    cli.getOutputStream().write(Utils.objectToBytes(message));
                    Log.e("Outgoing", "Saiu daqui");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class MessageTreatmentTask extends MyAsyncTask<SimWifiP2pSocket, Void, Void> {

        String ip = null;

        public void setDeviceIp(String ip){
            this.ip = ip;
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
                    s.close();
                    Log.e("MessagePackSend", "A enviar o socket");
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NullPointerException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            termiteComm.remove(this);
        }
    }

}
