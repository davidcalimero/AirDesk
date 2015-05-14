package pt.ulisboa.tecnico.cmov.airdesk.wifiDirect;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.ulisboa.tecnico.cmov.airdesk.dto.TextFileDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.UserDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.WorkspaceDto;
import pt.ulisboa.tecnico.cmov.airdesk.exception.AlreadyExistsException;
import pt.ulisboa.tecnico.cmov.airdesk.exception.OutOfMemoryException;
import pt.ulisboa.tecnico.cmov.airdesk.listener.ConnectionHandler;
import pt.ulisboa.tecnico.cmov.airdesk.utility.FlowManager;
import pt.ulisboa.tecnico.cmov.airdesk.utility.FlowProxy;
import pt.ulisboa.tecnico.cmov.airdesk.utility.MessagePack;

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
        receiver = new WifiDirectBroadcastReceiver(this);
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
        stopSelf();
        return super.onUnbind(intent);
    }

    protected abstract void init();

    protected abstract void unBind();

    protected abstract void processGroup();

    public abstract void sendMessage(MessagePack message);

    public abstract void sendMessageWithResponse(MessagePack message, ConnectionHandler<MessagePack> handler);

    // PROCESS MESSAGE
    protected MessagePack processMessage(MessagePack message) {
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
                FlowManager.receive_mountWorkspace(getApplicationContext(), (WorkspaceDto) message.data);
                return null;

            case MessagePack.UNMOUNT_WORKSPACE:
                FlowManager.receive_unmountWorkspace(getApplicationContext(), (WorkspaceDto) message.data);
                return null;

            case MessagePack.UNINVITE_FROM_WORKSPACE:
                if (message.type == MessagePack.Type.REQUEST) {
                    FlowManager.receive_uninviteUserFromWorkspace(getApplicationContext(), message.sender, (WorkspaceDto) message.data);
                    message.type = MessagePack.Type.REPLY;
                    return message;
                }
                return null;

            case MessagePack.ADD_FILE:
                if(message.type == MessagePack.Type.REQUEST){
                    MessagePack pack1 = new MessagePack();
                    pack1.request = MessagePack.ADD_FILE;
                    pack1.type = MessagePack.Type.REPLY;
                    pack1.data = null;
                    try {
                        FlowManager.receive_addFile(getApplicationContext(), (TextFileDto) message.data);
                    } catch (AlreadyExistsException | OutOfMemoryException e) {
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
                    } catch (AlreadyExistsException | OutOfMemoryException e) {
                        pack2.data = e;
                    }
                    return pack2;
                }
                return null;

            case MessagePack.REMOVE_FILE:
                if(message.type == MessagePack.Type.REQUEST) {
                    FlowManager.receive_removeFile(getApplicationContext(), (TextFileDto) message.data);
                    message.type = MessagePack.Type.REPLY;
                    return message;
                }
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
                    UserDto userDto = new UserDto();
                    userDto.id = message.sender;
                    MessagePack pack4 = new MessagePack();
                    pack4.request = MessagePack.USER_REQUEST;
                    pack4.type = MessagePack.Type.REPLY;
                    pack4.data = FlowManager.receive_askToEditFile(getApplicationContext(), userDto, (TextFileDto) message.data);
                    return pack4;
                }
                return null;

            case MessagePack.SUBSCRIBE:
                if(message.type == MessagePack.Type.REQUEST){
                    FlowManager.receive_subscribe(getApplicationContext(), (UserDto) message.data);
                    message.type = MessagePack.Type.REPLY;
                    return message;
                }
                return null;

            case MessagePack.STOP_EDITING:
                FlowManager.receive_userStopEditing(getApplicationContext(), (TextFileDto) message.data);
                return null;

            default:
                return null;
        }
    }

    //----------------------------------------------------------------------------------------------
    // WIFI DIRECT UPDATERS  -----------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    protected void addDevice(String ip){
        FlowProxy.getInstance().addDevice(getApplicationContext(), ip);
    }

    protected void removeDevice(String ip){
        FlowProxy.getInstance().removeDevice(getApplicationContext(), ip);
    }
}
