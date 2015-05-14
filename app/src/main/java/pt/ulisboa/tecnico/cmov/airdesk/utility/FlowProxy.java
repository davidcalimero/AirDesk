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
            }

            @Override
            public void onFailure() {}
        });
    }

    public void removeDevice(String ip){
        UserDto userDto = new UserDto();
        userDto.id = ip_id.get(ip);
        FlowManager.receive_userLeft(userDto);
        ip_id.remove(ip);
        id_ip.remove(userDto.id);
        Log.e("FlowProxy", "User removed: " + userDto.id + "-" + ip);
    }

    //----------------------------------------------------------------------------------------------
    // NET LAYER METHODS ---------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    //SEND -----------------------------------------------------------------------------------------

    public void send_userLeftWorkspace(final Context context, final WorkspaceDto workspaceDto, final ConnectionHandler handler){
        new MyAsyncTask<Void, MessagePack, Void>() {
            private boolean failure = false;

            @Override
            protected Void doInBackground(Void param) {
                //Create message pack
                MessagePack messagePack = new MessagePack();
                messagePack.request = MessagePack.UNINVITE_FROM_WORKSPACE;
                messagePack.data = workspaceDto;
                messagePack.receiver = id_ip.get(workspaceDto.owner);
                messagePack.sender = FlowManager.getActiveUserID(context);
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
                        handler.onSuccess(message.data);
                }
            }
        }.execute(null);
    }

    public void send_mountWorkspace(final Context context, final String userId, final WorkspaceDto workspaceDto, final ConnectionHandler handler){
        new MyAsyncTask<Void, MessagePack, Void>() {
            private boolean failure = false;

            @Override
            protected Void doInBackground(Void param) {
                if(FlowManager.getActiveUserID(context).equals(userId)) {
                    publishProgress(null);
                    return null;
                }

                //Create message pack
                MessagePack messagePack = new MessagePack();
                messagePack.request = MessagePack.MOUNT_WORKSPACE;
                messagePack.data = workspaceDto;
                messagePack.receiver = id_ip.get(userId);
                messagePack.sender = FlowManager.getActiveUserID(context);
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
                if(FlowManager.getActiveUserID(context).equals(userId)) {
                    FlowManager.receive_mountWorkspace(workspaceDto);
                }

                if(handler != null){
                    if(failure)
                        handler.onFailure();
                    else
                        handler.onSuccess(message.data);
                }
            }
        }.execute(null);
    }

    public void send_unmountWorkspace(final Context context, final String userId, final WorkspaceDto workspaceDto, final ConnectionHandler handler){
        new MyAsyncTask<Void, MessagePack, Void>() {
            private boolean failure = false;

            @Override
            protected Void doInBackground(Void param) {

                if(FlowManager.getActiveUserID(context).equals(userId)) {
                    publishProgress(null);
                    return null;
                }

                //Create message pack
                MessagePack messagePack = new MessagePack();
                messagePack.request = MessagePack.UNMOUNT_WORKSPACE;
                messagePack.data = workspaceDto;
                messagePack.receiver = id_ip.get(userId);
                messagePack.sender = FlowManager.getActiveUserID(context);
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
                if(FlowManager.getActiveUserID(context).equals(userId)) {
                    FlowManager.receive_unmountWorkspace(workspaceDto);
                }

                if(handler != null){
                    if(failure)
                        handler.onFailure();
                    else
                        handler.onSuccess(message.data);
                }
            }
        }.execute(null);
    }

    public void send_addFile(final Context context, final String userId, final TextFileDto textFileDto, final ConnectionHandler handler){
        new MyAsyncTask<Void, Void, Void>() {
            private boolean failure = false;

            @Override
            protected Void doInBackground(Void param) {
                if(FlowManager.getActiveUserID(context).equals(userId)) {
                    publishProgress(null);
                    return null;
                }

                //Create message pack
                MessagePack messagePack = new MessagePack();
                messagePack.request = MessagePack.ADD_FILE;
                messagePack.data = textFileDto;
                messagePack.receiver = id_ip.get(userId);
                messagePack.sender = id_ip.get(FlowManager.getActiveUserID(context));
                messagePack.type = MessagePack.Type.REQUEST;

                //Send message
                if(handler == null) {
                    ((ApplicationContext) context).getWifiDirectService().sendMessage(messagePack);
                    return null;
                }

                ((ApplicationContext) context).getWifiDirectService().sendMessageWithResponse(messagePack, new ConnectionHandler<MessagePack>() {
                    @Override
                    public void onSuccess(MessagePack result) {
                        publishProgress(null);
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
            protected void onProgressUpdate(Void message) {
                if(FlowManager.getActiveUserID(context).equals(userId)) {
                    try {
                        FlowManager.receive_addFile(context, textFileDto);
                    } catch (AlreadyExistsException e) {
                        failure = true;
                    } catch (OutOfMemoryException e) {
                        failure = true;
                    }
                }

                if(handler != null){
                    if(failure)
                        handler.onFailure();
                    else
                        handler.onSuccess(null);
                }
            }
        }.execute(null);
    }

    public void send_removeFile(final Context context, final String userId, final TextFileDto textFileDto, final ConnectionHandler handler){
        new MyAsyncTask<Void, Void, Void>() {
            private boolean failure = false;

            @Override
            protected Void doInBackground(Void param) {

                if(FlowManager.getActiveUserID(context).equals(userId)) {
                    publishProgress(null);
                    return null;
                }

                //Create message pack
                MessagePack messagePack = new MessagePack();
                messagePack.request = MessagePack.REMOVE_FILE;
                messagePack.data = textFileDto;
                messagePack.receiver = id_ip.get(userId);
                messagePack.sender = id_ip.get(FlowManager.getActiveUserID(context));
                messagePack.type = MessagePack.Type.REQUEST;

                //Send message
                if(handler == null) {
                    ((ApplicationContext) context).getWifiDirectService().sendMessage(messagePack);
                    return null;
                }

                ((ApplicationContext) context).getWifiDirectService().sendMessageWithResponse(messagePack, new ConnectionHandler<MessagePack>() {
                    @Override
                    public void onSuccess(MessagePack result) {
                        publishProgress(null);
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
            protected void onProgressUpdate(Void message) {
                if(FlowManager.getActiveUserID(context).equals(userId)) {
                    FlowManager.receive_removeFile(context, textFileDto);
                }

                if(handler != null){
                    if(failure)
                        handler.onFailure();
                    else
                        handler.onSuccess(null);
                }
            }
        }.execute(null);
    }

    public void send_editFile(final Context context, final String userId, final TextFileDto textFileDto, final ConnectionHandler handler){
         new MyAsyncTask<Void, Void, Void>() {
             private boolean failure = false;

            @Override
            protected Void doInBackground(Void param) {

                if(FlowManager.getActiveUserID(context).equals(userId)) {
                    publishProgress(null);
                    return null;
                }

                //Create message pack
                MessagePack messagePack = new MessagePack();
                messagePack.request = MessagePack.EDIT_FILE;
                messagePack.data = textFileDto;
                messagePack.receiver = id_ip.get(userId);
                messagePack.sender = id_ip.get(FlowManager.getActiveUserID(context));
                messagePack.type = MessagePack.Type.REQUEST;

                //Send message
                if(handler == null) {
                    ((ApplicationContext) context).getWifiDirectService().sendMessage(messagePack);
                    return null;
                }

                ((ApplicationContext) context).getWifiDirectService().sendMessageWithResponse(messagePack, new ConnectionHandler<MessagePack>() {
                    @Override
                    public void onSuccess(MessagePack result) {
                        publishProgress(null);
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
             protected void onProgressUpdate(Void message) {
                 if(FlowManager.getActiveUserID(context).equals(userId)) {
                     try {
                         FlowManager.receive_editFile(context, textFileDto);
                     } catch (AlreadyExistsException e) {
                         failure = true;
                     } catch (OutOfMemoryException e) {
                         failure = true;
                     }
                 }

                 if(handler != null){
                     if(failure)
                         handler.onFailure();
                     else
                         handler.onSuccess(null);
                 }
             }
        }.execute(null);
    }

    public void send_askToEditFile(final Context context, final TextFileDto textFileDto, final ConnectionHandler handler) {
        new MyAsyncTask<Void, Boolean, Void>() {
            private boolean failure = false;

            @Override
            protected Void doInBackground(Void param) {
                if (FlowManager.getActiveUserID(context).equals(textFileDto.owner)) {
                    publishProgress(FlowManager.receive_askToEditFile(context, textFileDto));
                    return null;
                }

                //Create message pack
                MessagePack messagePack = new MessagePack();
                messagePack.request = MessagePack.ASK_TO_EDIT;
                messagePack.data = textFileDto;
                messagePack.receiver = id_ip.get(textFileDto.owner);
                messagePack.sender = id_ip.get(FlowManager.getActiveUserID(context));
                messagePack.type = MessagePack.Type.REQUEST;

                //Send message
                ((ApplicationContext) context).getWifiDirectService().sendMessageWithResponse(messagePack, new ConnectionHandler<MessagePack>() {
                    @Override
                    public void onSuccess(MessagePack result) {
                        publishProgress((Boolean) result.data);
                    }

                    @Override
                    public void onFailure() {
                        failure = true;
                        publishProgress(false);
                    }
                });
                return null;
            }

            @Override
            protected void onProgressUpdate(Boolean param) {
                if(handler != null){
                    if(failure)
                        handler.onFailure();
                    else
                        handler.onSuccess(param);
                }
            }
        }.execute(null);
    }

    public void send_getFileContent(final Context context, final TextFileDto textFileDto, final ConnectionHandler handler) {
        new MyAsyncTask<Void, String, Void>() {
            private boolean failure = false;

            @Override
            protected Void doInBackground(Void param) {
                if(FlowManager.getActiveUserID(context).equals(textFileDto.owner)) {
                    publishProgress(FlowManager.receive_getFileContent(context, textFileDto));
                    return null;
                }

                //Create message pack
                MessagePack messagePack = new MessagePack();
                messagePack.request = MessagePack.FILE_CONTENT;
                messagePack.data = textFileDto;
                messagePack.receiver = id_ip.get(textFileDto.owner);
                messagePack.sender = id_ip.get(FlowManager.getActiveUserID(context));
                messagePack.type = MessagePack.Type.REQUEST;

                //Send message
                ((ApplicationContext) context).getWifiDirectService().sendMessageWithResponse(messagePack, new ConnectionHandler<MessagePack>() {
                    @Override
                    public void onSuccess(MessagePack result) {
                        publishProgress(((TextFileDto) result.data).content);
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
            protected void onProgressUpdate(String content) {
                if(handler != null){
                    if(failure)
                        handler.onFailure();
                    else
                        handler.onSuccess(content);
                }
            }
        }.execute(null);
    }
}