package pt.ulisboa.tecnico.cmov.airdesk.utility;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.airdesk.ApplicationContext;
import pt.ulisboa.tecnico.cmov.airdesk.dto.TextFileDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.UserDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.WorkspaceDto;
import pt.ulisboa.tecnico.cmov.airdesk.exception.AlreadyExistsException;
import pt.ulisboa.tecnico.cmov.airdesk.exception.OutOfMemoryException;
import pt.ulisboa.tecnico.cmov.airdesk.listener.ConnectionHandler;

public class FlowProxy {

    private HashMap<String, String> ip_id = new HashMap<>();
    private HashMap<String, String> id_ip = new HashMap<>();

    private static FlowProxy instance = new FlowProxy();

    private FlowProxy(){}

    public static FlowProxy getInstance() {
        return instance;
    }

    public void addDevice(final Context context, final String ip){
        //Create message pack
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.USER_REQUEST;
        messagePack.receiver = ip;
        messagePack.type = MessagePack.Type.REQUEST;

        ((ApplicationContext) context).getWifiDirectService().sendMessageWithResponse(messagePack, new ConnectionHandler<MessagePack>() {
            @Override
            public void onSuccess(MessagePack result) {
                String id = ((UserDto) result.data).id;
                ip_id.put(ip, id);
                id_ip.put(id, ip);
                Log.e("FlowProxy", "User add: " + id + "-" + ip);

                //SEND WORKSPACES IF ID OR SUBSCRIPTIONS MEETS
                FlowManager.receive_userJoined(context, (UserDto) result.data);
            }

            @Override
            public void onFailure() {
            }
        });
    }

    public void removeDevice(Context context, String ip){
        UserDto userDto = new UserDto();
        userDto.id = ip_id.get(ip);
        FlowManager.receive_userLeft(context, userDto);
        ip_id.remove(ip);
        id_ip.remove(userDto.id);
        Log.e("FlowProxy", "User removed: " + userDto.id + "-" + ip);
    }

    //----------------------------------------------------------------------------------------------
    // NET LAYER METHODS ---------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    private void send(final Context context, final MessagePack messagePack, final ConnectionHandler handler){
        new MyAsyncTask<Void, MessagePack, Void>() {
            private boolean failure = false;

            @Override
            protected Void doInBackground(Void param) {
                if(messagePack.receiver.equals(FlowManager.getActiveUserID(context))) {
                    publishProgress(null);
                    return null;
                }

                messagePack.sender = FlowManager.getActiveUserID(context);
                messagePack.receiver = id_ip.get(messagePack.receiver);
                messagePack.type = MessagePack.Type.REQUEST;

                //Send message
                if(handler == null) {
                    ((ApplicationContext) context).getWifiDirectService().sendMessage(messagePack);
                    return null;
                }

                ((ApplicationContext) context).getWifiDirectService().sendMessageWithResponse(messagePack, new ConnectionHandler<MessagePack>() {
                    @Override
                    public void onSuccess(MessagePack result) {
                        publishProgress(result);
                    }

                    @Override
                    public void onFailure() {
                        failure = true;
                        publishProgress(null);
                    }
                });
                return null;
            }

            @Override
            protected void onProgressUpdate(MessagePack message) {
                if(handler != null){
                    if(failure)
                        handler.onFailure();
                    else
                        handler.onSuccess(message != null ? message.data : null);
                }
            }
        }.execute(null);
    }


    //SEND -----------------------------------------------------------------------------------------

    public void send_userLeft(Context context){
        for(WorkspaceDto workspaceDto : FlowManager.getWorkspaces(context))
            for(String userId : FlowManager.getWorkspaceUsers(context, workspaceDto.name))
                send_unmountWorkspace(context, userId, workspaceDto, null);
    }

    public void send_userLeftWorkspace(final Context context, final WorkspaceDto workspaceDto, final ConnectionHandler handler){
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.UNINVITE_FROM_WORKSPACE;
        messagePack.data = workspaceDto;
        messagePack.receiver = workspaceDto.owner;
        send(context, messagePack, new ConnectionHandler<WorkspaceDto>() {
            @Override
            public void onSuccess(WorkspaceDto dto) {
                if(handler != null) handler.onSuccess(dto);
                FlowManager.receive_unmountWorkspace(context, workspaceDto);
            }

            @Override
            public void onFailure() {
                if(handler != null)  handler.onFailure();
            }
        });
    }

    public void send_mountWorkspace(final Context context, final String userId, final WorkspaceDto workspaceDto, final ConnectionHandler handler){
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.MOUNT_WORKSPACE;
        messagePack.data = workspaceDto;
        messagePack.receiver = userId;
        send(context, messagePack, new ConnectionHandler<MessagePack>() {
            @Override
            public void onSuccess(MessagePack message) {
                if(handler != null) handler.onSuccess(message != null ? message.data : null);
                if(FlowManager.getActiveUserID(context).equals(userId))
                    FlowManager.receive_mountWorkspace(context, workspaceDto);
            }

            @Override
            public void onFailure() {
                if(handler != null) handler.onFailure();
            }
        });
    }

    public void send_unmountWorkspace(final Context context, final String userId, final WorkspaceDto workspaceDto, final ConnectionHandler handler){
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.UNMOUNT_WORKSPACE;
        messagePack.data = workspaceDto;
        messagePack.receiver = userId;
        send(context, messagePack, new ConnectionHandler<MessagePack>() {
            @Override
            public void onSuccess(MessagePack message) {
                if(handler != null)  handler.onSuccess(message != null ? message.data : null);
                if(FlowManager.getActiveUserID(context).equals(userId))
                    FlowManager.receive_unmountWorkspace(context, workspaceDto);
            }

            @Override
            public void onFailure() {
                if(handler != null) handler.onFailure();
            }
        });
    }

    public void send_addFile(final Context context, final String userId, final TextFileDto textFileDto, final ConnectionHandler handler){
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.ADD_FILE;
        messagePack.data = textFileDto;
        messagePack.receiver = userId;
        send(context, messagePack, new ConnectionHandler<Exception>() {
            @Override
            public void onSuccess(Exception exception) {
                if (FlowManager.getActiveUserID(context).equals(userId)) {
                    try {
                        FlowManager.receive_addFile(context, textFileDto);
                        if(handler != null) handler.onSuccess(exception);
                    } catch (AlreadyExistsException | OutOfMemoryException e) {
                        if(handler != null) handler.onFailure();
                    }
                    return;
                }
                if(handler != null) handler.onSuccess(exception);
            }

            @Override
            public void onFailure() {
                if(handler != null) handler.onFailure();
            }
        });
    }

    public void send_removeFile(final Context context, final String userId, final TextFileDto textFileDto, final ConnectionHandler handler){
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.REMOVE_FILE;
        messagePack.data = textFileDto;
        messagePack.receiver = userId;
        send(context, messagePack, new ConnectionHandler<TextFileDto>() {
            @Override
            public void onSuccess(TextFileDto message) {
                if(handler != null) handler.onSuccess(message);
                if(FlowManager.getActiveUserID(context).equals(userId)) {
                    FlowManager.receive_removeFile(context, textFileDto);
                }
            }

            @Override
            public void onFailure() {
                if(handler != null) handler.onFailure();
            }
        });
    }

    public void send_editFile(final Context context, final String userId, final TextFileDto textFileDto, final ConnectionHandler handler){
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.EDIT_FILE;
        messagePack.data = textFileDto;
        messagePack.receiver = userId;
        send(context, messagePack, new ConnectionHandler<Exception>() {
            @Override
            public void onSuccess(Exception exception) {
                if(FlowManager.getActiveUserID(context).equals(userId)) {
                    try {
                        FlowManager.receive_editFile(context, textFileDto);
                        if(handler != null) handler.onSuccess(exception);
                    } catch (AlreadyExistsException | OutOfMemoryException e) {
                        if(handler != null) handler.onFailure();
                    }
                    return;
                }
                if(handler != null) handler.onSuccess(exception);
            }
            @Override
            public void onFailure() {
                if(handler != null) handler.onFailure();
            }
        });
    }

    public void send_askToEditFile(final Context context, UserDto userDto, final TextFileDto textFileDto, final ConnectionHandler handler) {
        if(textFileDto.owner.equals(FlowManager.getActiveUserID(context))) {
            if(handler != null) handler.onSuccess(FlowManager.receive_askToEditFile(context, userDto, textFileDto));
            return;
        }

        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.ASK_TO_EDIT;
        messagePack.data = textFileDto;
        messagePack.receiver = textFileDto.owner;
        send(context, messagePack, handler);
    }

    public void send_getFileContent(final Context context, final TextFileDto textFileDto, final ConnectionHandler handler) {
        if(textFileDto.owner.equals(FlowManager.getActiveUserID(context))) {
            if(handler != null) handler.onSuccess(FlowManager.receive_getFileContent(context, textFileDto));
            return;
        }

        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.FILE_CONTENT;
        messagePack.data = textFileDto;
        messagePack.receiver = textFileDto.owner;
        send(context, messagePack, handler);
    }

    public void send_subscribe(final Context context, final UserDto userDto, final ConnectionHandler handler){
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.SUBSCRIBE;
        messagePack.data = userDto;
        messagePack.sender = id_ip.get(userDto.id);

        for(String id : id_ip.keySet()) {
            messagePack.receiver = id;
            send(context, messagePack, handler);
        }
    }

    public void send_userStopEditing(final Context context, final TextFileDto textFileDto, final ConnectionHandler handler){
        if(textFileDto.owner.equals(FlowManager.getActiveUserID(context))) {
            FlowManager.receive_userStopEditing(context, textFileDto);
            if(handler != null) handler.onSuccess(null);
            return;
        }

        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.STOP_EDITING;
        messagePack.data = textFileDto;
        messagePack.receiver = textFileDto.owner;
        send(context, messagePack, handler);
    }
}