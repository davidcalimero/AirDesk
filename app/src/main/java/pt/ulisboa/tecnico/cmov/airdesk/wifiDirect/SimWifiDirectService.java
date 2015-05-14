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
import java.util.HashSet;

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
import pt.ulisboa.tecnico.cmov.airdesk.exception.AlreadyExistsException;
import pt.ulisboa.tecnico.cmov.airdesk.exception.OutOfMemoryException;
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
    private HashSet<String> devices = null;

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
        devices = new HashSet<>();

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

        if(devices.contains(message.receiver))
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
        for(String ip : devices){
            if(!ips.contains(ip))
                devices.remove(ip);
            FlowProxy.getInstance().removeDevice(ip);
        }

        // KNOW IDs. WILL SEND A USER_REQUEST MESSAGEPACK
        /**/
        for(SimWifiP2pDevice device : simWifiP2pDeviceList.getDeviceList()){
            if(!devices.contains(device.getVirtIp()) && !simWifiP2pInfo.getDeviceName().equals(device.deviceName)){
                devices.add(device.getVirtIp());
                FlowProxy.getInstance().addDevice(getApplicationContext(), device.getVirtIp());
            }
        }
        /**/
    }

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList simWifiP2pDeviceList) {}

    // PROCESS MESSAGE
    private MessagePack processMessage(MessagePack message) {
        Log.e("Message Process", "Message is " + message.request);

        switch(message.request){
            case MessagePack.USER_REQUEST:
                if(message.type == MessagePack.Type.REQUEST){
                    MessagePack pack = new MessagePack();
                    pack.request = MessagePack.USER_REQUEST;
                    pack.type = MessagePack.Type.REPLY;
                    pack.data = FlowManager.receive_userRequest(getApplicationContext());
                    return pack;
                }
                return null;

            case MessagePack.MOUNT_WORKSPACE:
                FlowManager.receive_mountWorkspace((WorkspaceDto) message.data);
                return null;

            case MessagePack.UNMOUNT_WORKSPACE:
                FlowManager.receive_mountWorkspace((WorkspaceDto) message.data);
                return null;

            case MessagePack.UNINVITE_FROM_WORKSPACE:
                FlowManager.receive_uninviteUserFromWorkspace(getApplicationContext(), message.sender, (WorkspaceDto) message.data);
                return null;

            case MessagePack.ADD_FILE:
                if(message.type == MessagePack.Type.REQUEST){
                    MessagePack pack1 = new MessagePack();
                    pack1.request = MessagePack.ADD_FILE;
                    pack1.type = MessagePack.Type.REPLY;
                    pack1.data = null;
                    try {
                        FlowManager.receive_addFile(getApplicationContext(), (TextFileDto) message.data);
                    } catch (AlreadyExistsException e) {
                        pack1.data = e;
                    } catch (OutOfMemoryException e) {
                        pack1.data = e;
                    }
                    return pack1;
                }
                return null;

            case MessagePack.EDIT_FILE:
                if(message.type == MessagePack.Type.REQUEST){
                    MessagePack pack2 = new MessagePack();
                    pack2.request = MessagePack.EDIT_FILE;
                    pack2.type = MessagePack.Type.REPLY;
                    pack2.data = null;
                    try {
                        FlowManager.receive_editFile(getApplicationContext(), (TextFileDto) message.data);
                    } catch (AlreadyExistsException e) {
                        pack2.data = e;
                    } catch (OutOfMemoryException e) {
                        pack2.data = e;
                    }
                    return pack2;
                }
                return null;

            case MessagePack.REMOVE_FILE:
                FlowManager.receive_removeFile(getApplicationContext(), (TextFileDto) message.data);
                return null;

            case MessagePack.FILE_CONTENT:
                if(message.type == MessagePack.Type.REQUEST){
                    MessagePack pack3 = new MessagePack();
                    pack3.request = MessagePack.FILE_CONTENT;
                    pack3.type = MessagePack.Type.REPLY;
                    pack3.data = FlowManager.receive_getFileContent(getApplicationContext(), (TextFileDto) message.data);
                    return pack3;
                }
                return null;

            case MessagePack.ASK_TO_EDIT:
                if(message.type == MessagePack.Type.REQUEST){
                    MessagePack pack4 = new MessagePack();
                    pack4.request = MessagePack.USER_REQUEST;
                    pack4.type = MessagePack.Type.REPLY;
                    pack4.data = FlowManager.receive_askToEditFile(getApplicationContext(), (TextFileDto) message.data);
                    return pack4;
                }
                return null;

            case MessagePack.SUBSCRIBE:
                FlowManager.receive_subscribe(getApplicationContext(), (UserDto) message.data);
                return null;

            default:
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

                if (handler != null){
                    Log.e("Outgoing", "Chegou aqui");
                    MessageTreatmentTask task = new MessageTreatmentTask(handler);
                    task.setDeviceIp(message.receiver);
                    Log.e("Outgoing", "A executar o MessageTreatment");
                    task.execute(cli);
                }
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
                MessagePack sendPack = processMessage(message);
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
