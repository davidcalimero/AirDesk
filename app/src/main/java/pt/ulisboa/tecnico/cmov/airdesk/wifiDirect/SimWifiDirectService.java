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
import pt.ulisboa.tecnico.cmov.airdesk.dto.MessageDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.TextFileDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.UserDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.WorkspaceDto;

public class SimWifiDirectService extends WifiDirectService implements
        SimWifiP2pManager.PeerListListener, SimWifiP2pManager.GroupInfoListener {

    //Termite
    private Messenger termiteService;
    private SimWifiP2pManager termiteManager;
    private SimWifiP2pManager.Channel termiteChannel;

    private SimWifiP2pSocketServer termiteSrvSocket = null;
    //private HashMap<String, String> termiteNickConverter = null;
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
    public void testFunc() {
        termiteManager.requestPeers(termiteChannel, SimWifiDirectService.this);
    }

    @Override
    public void sendDto(Dto dto){
        //In the moment, it will send to every client
        for(String key : termiteCliSocket.keySet()){
            SimWifiP2pSocket sock = termiteCliSocket.get(key);
            try {
                sock.getOutputStream().write(dto.hashCode());
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

        // REMOVE SOCKET IF NOT A MEMBER ANYMORE
        for(String key : termiteCliSocket.keySet()){
            if(!ips.contains(key)) {
                SimWifiP2pSocket sock = termiteCliSocket.get(key);
                try {
                    sock.close();
                    Log.e("GroupInfoAvailable", "Removing ClientSocket");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("Termite-GroupInfo", "Error removing the socket");
                }
                termiteCliSocket.remove(key);
            }
        }

        // ADD SOCKET IF NEW MEMBER, USING OUTGOINGCOMMTASK (only the GO does this)
        if(simWifiP2pInfo.askIsGO()){
            Log.e("Test","Group Owner");
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
                    Log.e("Hello World", "Recebeu socket");
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
            Log.e("CenazIncomming","add receiveCommTask");

            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, values[0]);
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
                if(termiteCliSocket.containsKey(param))
                    cli = termiteCliSocket.get(param);
                else {
                    cli = new SimWifiP2pSocket(param, 10001);
                    termiteCliSocket.put(param, cli);
                }
                Log.e("CenazOutgoing","add client socket to list");
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
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, termiteCliSocket.get(param));
            }

            //TODO this try should not be here
            try {
                if(cli != null) {
                    cli.getOutputStream().write(("HelloWorld\n").getBytes());
                    Log.e("CenazOutgoing","remove ClientSocket from list, and close it.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class DTOTreatmentTask extends AsyncTask<SimWifiP2pSocket, String, Void> {
        SimWifiP2pSocket s;

        @Override
        protected Void doInBackground(SimWifiP2pSocket... params) {
            ObjectInputStream ois;

            s = params[0];

            try {
                while (!Thread.currentThread().isInterrupted()) {
                    // PROCESS DTO
                    ois = new ObjectInputStream(s.getInputStream());
                    Dto dto = (Dto) ois.readObject();

                    // CHECK DTO TYPE, AND EXECUTE DTO REQUEST
                    switch (dto.messageType) {
                        case "MESSAGE":
                            Log.e("Received message", ((MessageDto) dto).message);
                            break;
                        case "USER":
                            Log.e("Received user", ((UserDto) dto).userID);
                            break;
                        case "TEXTFILE":
                            Log.e("Received file", "from owner " + ((TextFileDto) dto).owner + ", with title " + ((TextFileDto) dto).title + ", and content " + ((TextFileDto) dto).content);
                            break;
                        case "WORKSPACE":
                            Log.e("Received workspace", "from owner " + ((WorkspaceDto) dto).owner + ", with name " + ((WorkspaceDto) dto).name);
                            break;
                        default:
                            Log.e("Error: Unrecognized Dto", dto.messageType);
                    }
                    // SEND BACK INFORMATION

                }

            } catch (IOException e) {
                Log.d("Error reading socket:", e.getMessage());
            } catch (ClassNotFoundException e) {
                Log.e("Error getting DTO:", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            termiteComm.remove(this);
        }
    }
}
