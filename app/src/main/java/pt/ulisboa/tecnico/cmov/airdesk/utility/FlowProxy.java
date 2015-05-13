package pt.ulisboa.tecnico.cmov.airdesk.utility;

import android.content.Context;

import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.airdesk.ApplicationContext;
import pt.ulisboa.tecnico.cmov.airdesk.dto.TextFileDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.UserDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.WorkspaceDto;

public class FlowProxy {

    private HashMap<String, String> ip_id = new HashMap<>();
    private HashMap<String, String> id_ip = new HashMap<>();

    private static FlowProxy instance = new FlowProxy();

    private FlowProxy(){}

    public static FlowProxy getInstance() {
        return instance;
    }

    public void addDevice(final Context context, String ip){
        //Create message pack
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.USER_REQUEST;
        messagePack.receiver = ip;
        messagePack.type = MessagePack.TYPE.REQUEST;

        /*new MyAsyncTask<MessagePack, Void, Void>(){
            @Override
            protected Void doInBackground(MessagePack param) {
                ((ApplicationContext) context).getWifiDirectService().sendMessage(param);
                return null;
            }
        }.execute(messagePack);*/

        //TODO
        //request userDTO from ip
        //recevie reply
        //send_mountWorksace to ip
    }

    public void removeDevice(String ip){
        UserDto userDto = new UserDto();
        userDto.id = ip_id.get(ip);
        FlowManager.receive_userLeaved(userDto);
        ip_id.remove(ip);
        id_ip.remove(userDto.id);
    }

    //----------------------------------------------------------------------------------------------
    // NET LAYER METHODS ---------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    //SEND -----------------------------------------------------------------------------------------

    public void send_userLeavedWorkspace(WorkspaceDto workspaceDto, Context context){
        //unmouting workspace on local device
        FlowManager.receive_unmountWorkspace(workspaceDto);

        //Create message pack
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.UNINVITE_FROM_WORKSPACE;
        messagePack.dto = workspaceDto;
        messagePack.receiver = id_ip.get(workspaceDto.owner);
        messagePack.sender = FlowManager.getActiveUserID(context);

        //sending the DTO
        ((ApplicationContext) context).getWifiDirectService().sendMessage(messagePack);
    }

    public void send_mountWorkspace(String userId, WorkspaceDto workspaceDto, Context context){
        if(FlowManager.getActiveUserID(context).equals(userId)) {
            FlowManager.receive_mountWorkspace(workspaceDto);
            return;
        }

        //Create message pack
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.MOUNT_WORKSPACE;
        messagePack.dto = workspaceDto;
        messagePack.receiver = id_ip.get(userId);
        messagePack.sender = FlowManager.getActiveUserID(context);

        //sending the DTO
        ((ApplicationContext) context).getWifiDirectService().sendMessage(messagePack);
    }

    public void send_unmountWorkspace(String userId, WorkspaceDto workspaceDto, Context context){
        if(FlowManager.getActiveUserID(context).equals(userId)) {
            FlowManager.receive_unmountWorkspace(workspaceDto);
            return;
        }

        //Create message pack
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.UNMOUNT_WORKSPACE;
        messagePack.dto = workspaceDto;
        messagePack.receiver = id_ip.get(userId);
        messagePack.sender = FlowManager.getActiveUserID(context);

        //sending the DTO
        ((ApplicationContext) context).getWifiDirectService().sendMessage(messagePack);
    }

    public void send_addFile(String userId, TextFileDto textFileDto, Context context){
        if(FlowManager.getActiveUserID(context).equals(userId)) {
            FlowManager.receive_addFile(context, textFileDto);
            return;
        }

        //Create message pack
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.ADD_FILE;
        messagePack.dto = textFileDto;
        messagePack.receiver = id_ip.get(userId);
        messagePack.sender = id_ip.get(FlowManager.getActiveUserID(context));

        //sending the DTO
        ((ApplicationContext) context).getWifiDirectService().sendMessage(messagePack);
    }

    public void send_removeFile(String userId, TextFileDto textFileDto, Context context){
        if(FlowManager.getActiveUserID(context).equals(userId)) {
            FlowManager.receive_removeFile(context, textFileDto);
            return;
        }

        //Create message pack
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.REMOVE_FILE;
        messagePack.dto = textFileDto;
        messagePack.receiver = id_ip.get(userId);
        messagePack.sender = id_ip.get(FlowManager.getActiveUserID(context));

        //sending the DTO
        ((ApplicationContext) context).getWifiDirectService().sendMessage(messagePack);
    }

    public void send_editFile(String userId, TextFileDto textFileDto, Context context){
        if(FlowManager.getActiveUserID(context).equals(userId)) {
            FlowManager.receive_editFile(context, textFileDto);
            return;
        }

        //Create message pack
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.EDIT_FILE;
        messagePack.dto = textFileDto;
        messagePack.receiver = id_ip.get(userId);
        messagePack.sender = id_ip.get(FlowManager.getActiveUserID(context));

        //sending the DTO
        ((ApplicationContext) context).getWifiDirectService().sendMessage(messagePack);
    }

    public boolean send_askToEditFile(Context context, TextFileDto textFileDto) {
        if(FlowManager.getActiveUserID(context).equals(textFileDto.owner))
            return FlowManager.receive_askToEditFile(context, textFileDto);

        return true; //TODO
    }

    public void send_getFileContent(final Context context, final TextFileDto textFileDto, final ConnectionHandler handler) {
        new MyAsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void param) {
                if(FlowManager.getActiveUserID(context).equals(textFileDto.owner))
                    handler.onSuccess(FlowManager.receive_getFileContent(context, textFileDto));
                else {
                    //Create message pack
                    MessagePack messagePack = new MessagePack();
                    messagePack.request = MessagePack.FILE_CONTENT;
                    messagePack.dto = textFileDto;
                    messagePack.receiver = id_ip.get(textFileDto.owner);
                    messagePack.sender = id_ip.get(FlowManager.getActiveUserID(context));

                    try{
                        handler.onSuccess(((ApplicationContext) context).getWifiDirectService().sendMessageWithResponse(messagePack));
                    } catch(ConnectionLostException e){
                        handler.onFailure();
                    }
                }

                return null;
            }
        };
    }

    public UserDto send_userID(Context context){
        //TODO
        UserDto userDto = new UserDto();
        userDto.id = FlowManager.getActiveUserID(context);
        userDto.subscriptions = FlowManager.getSubscriptions(context);
        return userDto;
    }
}