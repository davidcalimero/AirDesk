package pt.ulisboa.tecnico.cmov.airdesk.wifiDirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiDirectService _service;

    public WifiDirectBroadcastReceiver(WifiDirectService service){
        _service = service;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        switch (action){
            //Simulated WifiDirect
            case SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION:
                Log.e("BroadcastReceiver","State Changed");
                // This action is triggered when the WDSim service changes state:
                // - creating the service generates the WIFI_P2P_STATE_ENABLED event
                // - destroying the service generates the WIFI_P2P_STATE_DISABLED event
                int sim_state = intent.getIntExtra(SimWifiP2pBroadcast.EXTRA_WIFI_STATE, -1);
                if (sim_state == SimWifiP2pBroadcast.WIFI_P2P_STATE_ENABLED) {} else {}

                break;
            case SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION:
                Log.e("BroadcastReceiver","Peers Changed");
                // Request available peers from the wifi p2p manager. This is an
                // asynchronous call and the calling activity is notified with a
                // callback on PeerListListener.onPeersAvailable()
                break;
            case SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION:
                Log.e("BroadcastReceiver", "Network Membership Changed");
                SimWifiP2pInfo member_info = (SimWifiP2pInfo) intent.getSerializableExtra(SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
                member_info.print();
                _service.processGroup();
                break;
            case SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION:
                Log.e("BroadcastReceiver","Group Ownership Changed");
                SimWifiP2pInfo group_info = (SimWifiP2pInfo) intent.getSerializableExtra(SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
                group_info.print();
                //_service.processGroup();
                break;

            //WifiDirect
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {} else {}
                break;
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                break;
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                break;
            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                break;
        }
    }
}
