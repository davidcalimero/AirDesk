package pt.ulisboa.tecnico.cmov.airdesk.wifiDirect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
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
    private HashMap<String, SimWifiP2pSocket> termiteIDConverter = null;
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
        if(termiteIDConverter.containsKey(message.receiver)){}

        /** /
        Log.e("DtoSend", "Sending DTO");
        //In the moment, it will send to every client
        for(String key : termiteCliSocket.keySet()){
            SimWifiP2pSocket sock = termiteCliSocket.get(key);
            try {
                sock.getOutputStream().write(Utils.objectToBytes(message));
                Log.e("DtoSend", "Is sending...");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
         /**/
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList simWifiP2pDeviceList, SimWifiP2pInfo simWifiP2pInfo) {

        // CHECKS IF THE DEVICE IS GO
        isGO = simWifiP2pInfo.askIsGO();

        // MAKE A LIST OF IPs, TO INCREASE TIME EFFICIENCY
        ArrayList<String> ips = new ArrayList<>();
        for (String deviceName : simWifiP2pInfo.getDevicesInNetwork()) {
            SimWifiP2pDevice device = simWifiP2pDeviceList.getByName(deviceName);
            ips.add(device.getVirtIp());
        }

        // TERMINATE MESSAGETREAMENTTASK
        for(MessageTreatmentTask t : termiteComm){
            t.cancel();
            termiteComm.remove(t);
        }

        // REMOVE SOCKETS
        for(String key : termiteIDConverter.keySet()){
            SimWifiP2pSocket s = termiteIDConverter.get(key);
            if(s.isClosed())
                termiteIDConverter.remove(key);
        }

        // ESTABLISH CONNECTION WITH GROUP OWNER
        if(!isGO){
            Log.e("GroupInfoAvailable", ips.get(0));
            new OutgoingCommTask().execute(ips.get(0));
        }

        if(isGO) {
            //Create listener socket
            new IncomingCommTask().execute(null);
        }
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
    private void processMessage(MessagePack message) {
        Log.e("Message Process", "SAY DOWN FOR WHAT?!");
        Log.e("Message Process", "Message is " + message.request);

        switch(message.request){
            case MessagePack.HELLO_WORLD:
                Log.e("Message Process", "Hello World! :D");
                break;
            case MessagePack.USER_REQUEST:

                break;
            case MessagePack.MOUNT_WORKSPACE:

                break;
            case MessagePack.UNMOUNT_WORKSPACE:

                break;
            case MessagePack.ADD_FILE:

                break;
            case MessagePack.EDIT_FILE:

                break;
            case MessagePack.REMOVE_FILE:

                break;
            default:

                break;
        }
    }

    // ASYNC TASKS
    public class IncomingCommTask extends MyAsyncTask<Void, SimWifiP2pSocket, Void> {

        @Override
        protected Void doInBackground(Void params) {

            Log.d("SimWifiDirectService", "IncomingCommTask started (" + this.hashCode() + ").");
            if(Looper.getMainLooper().getThread() == Thread.currentThread())
                Log.e("Bkg: main thread", "NOOOES");
            else
                Log.e("Bkg: background thread", "yupie");

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

            if(Looper.getMainLooper().getThread() == Thread.currentThread())
                Log.e("Pgrs: main thread", "yupie");
            else
                Log.e("Pgrs: background thread", "NOOOES");

            MessageTreatmentTask task = new MessageTreatmentTask();
            termiteComm.add(task);
            Log.e("Incoming","add a Message Treatment Task");

            task.execute(value);
        }
    }

    public class OutgoingCommTask extends MyAsyncTask<String, Void, String> {

        SimWifiP2pSocket cli = null;

        @Override
        protected String doInBackground(String param) {
            long initialTime = System.currentTimeMillis();
            while(System.currentTimeMillis() - initialTime < 1000) {
                try {
                    cli = new SimWifiP2pSocket(param, 10001);
                    return null;
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if(Looper.getMainLooper().getThread() == Thread.currentThread())
                Log.e("out: main thread", "yupie");
            else
                Log.e("out: background thread", "NOOOES");

            if(cli != null) {
                Log.e("Outgoing","Chegou cá");
                MessageTreatmentTask task = new MessageTreatmentTask();
                termiteComm.add(task);
                task.execute(cli);

                MessagePack pack = new MessagePack();
                pack.request = MessagePack.HELLO_WORLD;
                try {
                    cli.getOutputStream().write(Utils.objectToBytes(pack));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class MessageTreatmentTask extends MyAsyncTask<SimWifiP2pSocket, String, Void> {

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
                processMessage(message);

            } catch (IOException e) {
                Log.e("Error reading socket:", e.getMessage());
            } catch (ClassNotFoundException e) {
                Log.e("Error getting Message:", e.getMessage());
            } catch (NullPointerException e){
                Log.e("Didn't receive Message", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            termiteComm.remove(this);
        }
    }

}
