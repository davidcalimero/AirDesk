package pt.ulisboa.tecnico.cmov.airdesk.wifiDirect;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import pt.ulisboa.tecnico.cmov.airdesk.utility.MessagePack;

public class RealWifiDirectService extends WifiDirectService implements
        WifiP2pManager.PeerListListener, WifiP2pManager.GroupInfoListener, WifiP2pManager.ConnectionInfoListener {

    //Wifi Direct
    private WifiP2pManager wifiManager;
    private WifiP2pManager.Channel wifiChannel;
    private WifiP2pInfo info;

    @Override
    protected void init() {
        wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        wifiChannel = wifiManager.initialize(this, getMainLooper(), null);
    }

    @Override
    protected void unBind() {}

    @Override
    protected void processGroup() {
        // Does nothing
    }

    /*@Override
    public void testFunc() {
        wifiManager.discoverPeers(wifiChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() { }

            @Override
            public void onFailure(int reasonCode) {}
        });
    }*/

    @Override
    public void sendMessage(MessagePack message) {

    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {}

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
