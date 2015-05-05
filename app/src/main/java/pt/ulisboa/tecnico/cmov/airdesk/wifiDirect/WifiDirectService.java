package pt.ulisboa.tecnico.cmov.airdesk.wifiDirect;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;

public class WifiDirectService extends Service implements
        SimWifiP2pManager.PeerListListener, SimWifiP2pManager.GroupInfoListener,
        WifiP2pManager.PeerListListener, WifiP2pManager.GroupInfoListener, WifiP2pManager.ConnectionInfoListener {

    private final IBinder wifiDirectBinder = new WifiDirectBinder();
    private WifiDirectBroadcastReceiver receiver;

    //Termite
    private Messenger termiteService;
    private SimWifiP2pManager termiteManager;
    private SimWifiP2pManager.Channel termiteChannel;
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
    private SimWifiP2pSocketServer mSrvSocket = null;
    private ReceiveCommTask mComm = null;
    private SimWifiP2pSocket mCliSocket = null;

    //Wifi Direct
    private WifiP2pManager wifiManager;
    private WifiP2pManager.Channel wifiChannel;
    private WifiP2pInfo info;

    @Override
    public void onCreate() {
        // initialize the WDSim API
        SimWifiP2pSocketManager.Init(getApplicationContext());

        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        //Simulated WifiDirect
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        //WifiDirect
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        wifiChannel = wifiManager.initialize(this, getMainLooper(), null);

        receiver = new WifiDirectBroadcastReceiver(wifiManager, wifiChannel);
        registerReceiver(receiver, filter);

        bindService(new Intent(getApplicationContext(), SimWifiP2pService.class), termiteConnection, Context.BIND_AUTO_CREATE);
        new IncommingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return wifiDirectBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        unregisterReceiver(receiver);
        unbindService(termiteConnection);
        return super.onUnbind(intent);
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices, SimWifiP2pInfo groupInfo) {
        for (String deviceName : groupInfo.getDevicesInNetwork()) {
            SimWifiP2pDevice device = devices.getByName(deviceName);
        }
    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {

    }

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {
        String ip = "";
        for (SimWifiP2pDevice device : peers.getDeviceList()) {
            ip =  device.getVirtIp();
        }

        new OutgoingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ip);
    }


    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        WifiP2pDevice ip = null;
        for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
            ip =  device;
        }

        //Connect
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = ip.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        wifiManager.connect(wifiChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
            }
        });
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        this.info = wifiP2pInfo;
        if (info.groupFormed && info.isGroupOwner) {
            new FileServerAsyncTask(this, null).execute();
        } else if (info.groupFormed) {
            Intent serviceIntent = new Intent(this, CommTask.class);
            serviceIntent.setAction(CommTask.ACTION_SEND_FILE);
            serviceIntent.putExtra(CommTask.EXTRAS_FILE_PATH, "");
            serviceIntent.putExtra(CommTask.EXTRAS_GROUP_OWNER_ADDRESS, info.groupOwnerAddress.getHostAddress());
            serviceIntent.putExtra(CommTask.EXTRAS_GROUP_OWNER_PORT, 8988);
            startService(serviceIntent);
        }
    }

    public class WifiDirectBinder extends Binder {
        public WifiDirectService getService(){
            return WifiDirectService.this;
        }
    }

    public void sendHelloWorld(){
        termiteManager.requestPeers(termiteChannel, WifiDirectService.this);
        wifiManager.discoverPeers(wifiChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() { }

            @Override
            public void onFailure(int reasonCode) {}
        });
    }


    public class IncommingCommTask extends AsyncTask<Void, SimWifiP2pSocket, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Log.d("wifi-direct-termite", "IncommingCommTask started (" + this.hashCode() + ").");

            try {
                mSrvSocket = new SimWifiP2pSocketServer(10001);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SimWifiP2pSocket sock = mSrvSocket.accept();
                    if (mCliSocket != null && mCliSocket.isClosed()) {
                        mCliSocket = null;
                    }
                    if (mCliSocket != null) {
                        Log.d("wifi-direct-termite", "Closing accepted socket because mCliSocket still active.");
                        sock.close();
                    } else {
                        publishProgress(sock);
                    }
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
            mCliSocket = values[0];
            mComm = new ReceiveCommTask();

            mComm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCliSocket);
        }
    }

    public class OutgoingCommTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            Log.e("wifi-direct-termite", "Connecting...");
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                mCliSocket = new SimWifiP2pSocket(params[0], 10001);
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
                Log.e("wifi-direct-termite", result);
            }
            else {
                mComm = new ReceiveCommTask();
                mComm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,mCliSocket);
            }

            //TODO this try should not be here
            try {
                mCliSocket.getOutputStream().write(("HelloWorld\n").getBytes());
                mCliSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    public class ReceiveCommTask extends AsyncTask<SimWifiP2pSocket, String, Void> {
        SimWifiP2pSocket s;

        @Override
        protected Void doInBackground(SimWifiP2pSocket... params) {
            BufferedReader sockIn;
            String st;

            s = params[0];

            try {
                sockIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
                while ((st = sockIn.readLine()) != null) {
                    publishProgress(st);
                }
            } catch (IOException e) {
                Log.d("Error reading socket:", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Log.d("wifi-termite recived", values[0]);
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!s.isClosed()) {
                try {
                    s.close();
                }
                catch (Exception e) {
                    Log.d("Error closing socket:", e.getMessage());
                }
            }
            s = null;
        }
    }

    public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private TextView statusText;

        /**
         * @param context
         * @param statusText
         */
        public FileServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(8988);
                Log.d("wifi-direct-real", "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d("wifi-direct-real", "Server: connection done");

                InputStream inputstream = client.getInputStream();

                Log.e("wifi-direct-real", "server: copying files " + inputstream.toString());
                serverSocket.close();
                return inputstream.toString();
            } catch (IOException e) {
                Log.e("wifi-direct-real", e.getMessage());
                return null;
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            statusText.setText("Opening a server socket");
        }

    }
}
