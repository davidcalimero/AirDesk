package pt.ulisboa.tecnico.cmov.airdesk.wifiDirect;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Messenger;

import java.io.IOException;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;

public class WifiDirectService extends Service implements
        SimWifiP2pManager.PeerListListener, SimWifiP2pManager.GroupInfoListener, WifiP2pManager.ChannelListener {

    private final IBinder wifiDirectBinder = new WifiDirectBinder();
    private WifiBroadcastReceiver receiver;

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

        WifiP2pManager manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager.Channel channel = manager.initialize(this, getMainLooper(), null);

        receiver = new WifiBroadcastReceiver(manager, channel);
        registerReceiver(receiver, filter);

        bindService(new Intent(getApplicationContext(), SimWifiP2pService.class), termiteConnection, Context.BIND_AUTO_CREATE);
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
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {
        for (SimWifiP2pDevice device : peers.getDeviceList()) {
        }
    }

    @Override
    public void onChannelDisconnected() {

    }

    public class WifiDirectBinder extends Binder{
        public WifiDirectService getService(){
            return WifiDirectService.this;
        }
    }
}
