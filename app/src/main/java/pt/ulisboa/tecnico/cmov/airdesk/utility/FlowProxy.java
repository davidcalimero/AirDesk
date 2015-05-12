package pt.ulisboa.tecnico.cmov.airdesk.utility;

import android.content.Context;

import pt.ulisboa.tecnico.cmov.airdesk.ApplicationContext;
import pt.ulisboa.tecnico.cmov.airdesk.dto.TextFileDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.UserDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.WorkspaceDto;

public abstract class FlowProxy {

    //----------------------------------------------------------------------------------------------
    // NET LAYER METHODS ---------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    //SEND -----------------------------------------------------------------------------------------

    public static void send_userLeavedWorkspace(WorkspaceDto workspaceDto, Context context){
        //unmouting workspace on local device
        FlowManager.receive_unmountWorkspace(workspaceDto);

        //Create message pack
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.UNINVITE_FROM_WORKSPACE;
        messagePack.dto = workspaceDto;
        messagePack.receiver = workspaceDto.owner;
        messagePack.sender = FlowManager.getActiveUserID(context);

        //sending the DTO
        ((ApplicationContext) context).getWifiDirectService().sendMessage(messagePack);
    }

    public static void send_mountWorkspace(String userId, WorkspaceDto workspaceDto, Context context){
        if(FlowManager.getActiveUserID(context).equals(workspaceDto.owner)) {
            FlowManager.receive_mountWorkspace(workspaceDto);
            return;
        }

        //Create message pack
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.MOUNT_WORKSPACE;
        messagePack.dto = workspaceDto;
        messagePack.receiver = userId;
        messagePack.sender = FlowManager.getActiveUserID(context);

        //sending the DTO
        ((ApplicationContext) context).getWifiDirectService().sendMessage(messagePack);
    }

    public static void send_unmountWorkspace(String userId, WorkspaceDto workspaceDto, Context context){
        if(FlowManager.getActiveUserID(context).equals(workspaceDto.owner)) {
            FlowManager.receive_unmountWorkspace(workspaceDto);
            return;
        }

        //Create message pack
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.UNMOUNT_WORKSPACE;
        messagePack.dto = workspaceDto;
        messagePack.receiver = userId;
        messagePack.sender = FlowManager.getActiveUserID(context);

        //sending the DTO
        ((ApplicationContext) context).getWifiDirectService().sendMessage(messagePack);
    }

    public static void send_addFile(String userId, TextFileDto textFileDto, Context context){
        if(FlowManager.getActiveUserID(context).equals(textFileDto.owner)) {
            FlowManager.receive_addFile(context, textFileDto);
            return;
        }

        //Create message pack
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.ADD_FILE;
        messagePack.dto = textFileDto;
        messagePack.receiver = userId;
        messagePack.sender = FlowManager.getActiveUserID(context);

        //sending the DTO
        ((ApplicationContext) context).getWifiDirectService().sendMessage(messagePack);
    }

    public static void send_removeFile(String userId, TextFileDto textFileDto, Context context){
        if(FlowManager.getActiveUserID(context).equals(textFileDto.owner)) {
            FlowManager.receive_removeFile(context, textFileDto);
            return;
        }

        //Create message pack
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.REMOVE_FILE;
        messagePack.dto = textFileDto;
        messagePack.receiver = userId;
        messagePack.sender = FlowManager.getActiveUserID(context);

        //sending the DTO
        ((ApplicationContext) context).getWifiDirectService().sendMessage(messagePack);
    }

    public static void send_editFile(String userId, TextFileDto textFileDto, Context context){
        if(FlowManager.getActiveUserID(context).equals(textFileDto.owner)) {
            FlowManager.receive_editFile(context, textFileDto);
            return;
        }

        //Create message pack
        MessagePack messagePack = new MessagePack();
        messagePack.request = MessagePack.EDIT_FILE;
        messagePack.dto = textFileDto;
        messagePack.receiver = userId;
        messagePack.sender = FlowManager.getActiveUserID(context);

        //sending the DTO
        ((ApplicationContext) context).getWifiDirectService().sendMessage(messagePack);
    }

    public static boolean send_askToEditFile(Context context, TextFileDto textFileDto) {
        if(FlowManager.getActiveUserID(context).equals(textFileDto.owner))
            return FlowManager.receive_askToEditFile(context, textFileDto);

        return true; //TODO
    }

    public static String send_getFileContent(Context context, TextFileDto textFileDto) {
        if(FlowManager.getActiveUserID(context).equals(textFileDto.owner))
            return FlowManager.receive_getFileContent(context, textFileDto);

        return ""; //TODO
    }

    public static UserDto send_userID(Context context){
        //TODO
        UserDto userDto = new UserDto();
        userDto.id = FlowManager.getActiveUserID(context);
        userDto.subscriptions = FlowManager.getSubscriptions(context);
        return userDto;
    }
}