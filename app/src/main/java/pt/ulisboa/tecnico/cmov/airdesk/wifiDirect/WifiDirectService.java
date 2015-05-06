package pt.ulisboa.tecnico.cmov.airdesk.wifiDirect;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;

public abstract class WifiDirectService extends Service {

    private final IBinder wifiDirectBinder = new WifiDirectBinder();
    private WifiDirectBroadcastReceiver receiver;

    public class WifiDirectBinder extends Binder {
        public WifiDirectService getService(){
            return WifiDirectService.this;
        }
    }

    @Override
    public void onCreate() {
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
        receiver = new WifiDirectBroadcastReceiver();
        registerReceiver(receiver, filter);

        init();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return wifiDirectBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        unregisterReceiver(receiver);
        unBind();
        return super.onUnbind(intent);
    }

    protected abstract void init();

    protected abstract void unBind();

    public abstract boolean isSupported();

    public abstract void testFunc();
}
