package pt.ulisboa.tecnico.cmov.airdesk.wifiDirect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmov.airdesk.dto.Dto;
import pt.ulisboa.tecnico.cmov.airdesk.utility.MessagePack;
import pt.ulisboa.tecnico.cmov.airdesk.dto.TextFileDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.UserDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.WorkspaceDto;
import pt.ulisboa.tecnico.cmov.airdesk.utility.FlowManager;
import pt.ulisboa.tecnico.cmov.airdesk.utility.Utils;

public class SimWifiDirectService extends WifiDirectService implements
        SimWifiP2pManager.PeerListListener, SimWifiP2pManager.GroupInfoListener {

    //Termite
    private Messenger termiteService;
    private SimWifiP2pManager termiteManager;
    private SimWifiP2pManager.Channel termiteChannel;

    private SimWifiP2pSocketServer termiteSrvSocket = null;
    private HashMap<String, String> termiteNickConverter = null;
    private HashMap<String, SimWifiP2pSocket> termiteCliSocket = null;
    private ArrayList<DTOTreatmentTask> termiteComm = null;

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

        //Create listener socket
        new IncomingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        //Initialize structures
        termiteCliSocket = new HashMap<>();
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
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList simWifiP2pDeviceList, SimWifiP2pInfo simWifiP2pInfo) {

        // MAKE A LIST OF IPs, TO INCREASE TIME EFFICIENCY
        HashSet<String> ips = new HashSet<>();
        for (String deviceName : simWifiP2pInfo.getDevicesInNetwork()) {
            SimWifiP2pDevice device = simWifiP2pDeviceList.getByName(deviceName);
            ips.add(device.getVirtIp());
        }

        // TERMINATE DTOTREAMENTTASK
        for(DTOTreatmentTask t : termiteComm){
            t.cancel(true);
            termiteComm.contains(t);
        }


        // REMOVE SOCKET IF NOT A MEMBER ANYMORE
        for(String key : termiteCliSocket.keySet()){
            if(!ips.contains(key)) {
                SimWifiP2pSocket sock = termiteCliSocket.get(key);
                try {
                    sock.close();
                    Log.e("GroupInfoAvailable", "Removing ClientSocket");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("GroupInfoAvailable", "Error removing the socket");
                }
                termiteCliSocket.remove(key);
            }
        }

        // ADD SOCKET IF NEW MEMBER, USING OUTGOINGCOMMTASK (only the GO does this)
        if(simWifiP2pInfo.askIsGO()){
            Log.e("GroupInfoAvailable","Is Group Owner");
            for (String ip : ips) {
                if(!termiteCliSocket.containsKey(ip)){
                    new OutgoingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ip);
                }
            }
        }
    }

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList simWifiP2pDeviceList) {
        String ip = "";
        for (SimWifiP2pDevice device : simWifiP2pDeviceList.getDeviceList()) {
            ip =  device.getVirtIp();
        }

        new OutgoingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ip);
    }

    // PROCESS DTOs
    private void processWorkspaceDto(WorkspaceDto dto, SimWifiP2pSocket sockToSend) {

    }

    private void processTextFileDto(TextFileDto dto, SimWifiP2pSocket sockToSend) {

    }

    private void processUserDto(UserDto dto, String ip) {
        // Checks if the this user has already that nick there. If not, it adds to the hashmap.
        termiteNickConverter.put(ip, dto.id);
    }

    private void processMessageDto(MessagePack dto, SimWifiP2pSocket sockToSend) {
        Log.e("DTO Process", "Begin");
        UserDto newDto;
        switch(dto.request){
            case MessagePack.HELLO_WORLD:
                Log.e("Hello World", ":)");
            case MessagePack.USER_REQUEST:
                String id = FlowManager.getActiveUserID(getApplicationContext());
                newDto = new UserDto();
                newDto.id = id;
                try {
                    sockToSend.getOutputStream().write(Utils.objectToBytes(newDto));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            default:
                Log.e("MessagePack", "Don't know what to do");
        }
    }


    // ASYNC TASKS
    public class IncomingCommTask extends AsyncTask<Void, SimWifiP2pSocket, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Log.d("SimWifiDirectService", "IncomingCommTask started (" + this.hashCode() + ").");

            try {
                termiteSrvSocket = new SimWifiP2pSocketServer(10001);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SimWifiP2pSocket sock = termiteSrvSocket.accept();
                    publishProgress(sock);
                    Log.e("Incoming", "Recebeu socket");
                } catch (IOException e) {
                    Log.d("Error accepting socket:", e.getMessage());
                    break;
                    //e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(SimWifiP2pSocket... values) {

            //TODO RECEIVE IP AND PUT IT ON THE HASHMAP
            //termiteCliSocket.put(values[0]);

            DTOTreatmentTask task = new DTOTreatmentTask();
            termiteComm.add(task);
            Log.e("Incoming","add a DTO Treatment Task");

            //TODO!! FUUUUUUUUUUUCK!
            //task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, values[0]);
        }
    }

    public class OutgoingCommTask extends AsyncTask<String, Void, String> {

        String param = null;
        SimWifiP2pSocket cli = null;

        @Override
        protected void onPreExecute() {
            Log.e("SimWifiDirectService", "Connecting...");
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                param = params[0];
                if(termiteCliSocket.containsKey(param)) {
                    cli = termiteCliSocket.get(param);
                    Log.e("CenazOutgoing", "using already created socket");
                }
                else {
                    cli = new SimWifiP2pSocket(param, 10001);
                    termiteCliSocket.put(param, cli);
                    Log.e("CenazOutgoing","add client socket to list");
                }
            } catch (UnknownHostException e) {
                return "Unknown Host:" + e.getMessage();
            } catch (IOException e) {
                return "IO error:" + e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Log.e("SimWifiDirectService", result);
            }
            else {
                DTOTreatmentTask task = new DTOTreatmentTask();
                termiteComm.add(task);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, param);
                try {
                    MessagePack messagePack = new MessagePack();
                    messagePack.request = MessagePack.USER_REQUEST;
                    cli.getOutputStream().write(Utils.objectToBytes(messagePack));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class DTOTreatmentTask extends AsyncTask<String, String, Void> {
        String sockIp;
        SimWifiP2pSocket s;

        @Override
        protected Void doInBackground(String... ip) {
            ObjectInputStream ois;

            sockIp = ip[0];
            s = termiteCliSocket.get(sockIp);

            try {
                while (!Thread.currentThread().isInterrupted() || !isCancelled()) {
                    // PROCESS DTO
                    ois = new ObjectInputStream(s.getInputStream());
                    Dto dto = (Dto) ois.readObject();

                    // CHECK DTO TYPE, AND EXECUTE DTO REQUEST
                    if(dto instanceof MessagePack){
                        Log.e("Received message", ((MessagePack) dto).request);
                        processMessageDto(((MessagePack) dto), s);
                    } else if(dto instanceof UserDto){
                        Log.e("Received user", ((UserDto) dto).id);
                        processUserDto((UserDto) dto, sockIp);
                    } else if(dto instanceof TextFileDto){
                        Log.e("Received file", "from owner " + ((TextFileDto) dto).owner + ", with title " + ((TextFileDto) dto).title + ", and content " + ((TextFileDto) dto).content);
                        processTextFileDto((TextFileDto) dto, s);
                    } else if(dto instanceof WorkspaceDto){
                        Log.e("Received workspace", "from owner " + ((WorkspaceDto) dto).owner + ", with name " + ((WorkspaceDto) dto).name);
                        processWorkspaceDto((WorkspaceDto) dto, s);
                    } else {
                        Log.e("Received DTO", "It's not a DTO!");
                    }
                }

            } catch (IOException e) {
                //Log.d("Error reading socket:", e.getMessage());
            } catch (ClassNotFoundException e) {
                //Log.e("Error getting DTO:", e.getMessage());
            } catch (NullPointerException e){
                Log.e("Received DTO", "Let me guess?! :D");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            termiteComm.remove(this);
        }
    }

}
