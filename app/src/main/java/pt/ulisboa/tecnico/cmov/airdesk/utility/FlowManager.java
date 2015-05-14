package pt.ulisboa.tecnico.cmov.airdesk.utility;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;

import pt.ulisboa.tecnico.cmov.airdesk.ApplicationContext;
import pt.ulisboa.tecnico.cmov.airdesk.dto.TextFileDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.UserDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.WorkspaceDto;
import pt.ulisboa.tecnico.cmov.airdesk.exception.AlreadyExistsException;
import pt.ulisboa.tecnico.cmov.airdesk.exception.OutOfMemoryException;
import pt.ulisboa.tecnico.cmov.airdesk.listener.WorkspacesChangeListener;

public class FlowManager {

    private static FlowManager instance = new FlowManager();

    private ArrayList<WorkspacesChangeListener> listeners = new ArrayList<>();

    private FlowManager(){}

    private static FlowManager getInstance() {
        return instance;
    }

    //----------------------------------------------------------------------------------------------
    // FLOW MANAGER UPDATERS  ----------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    public static void addWorkspacesChangeListener(WorkspacesChangeListener listener) {
        getInstance().listeners.add(listener);
        Log.e("FlowManager", "listener added, size: " + getInstance().listeners.size());
    }

    public static void removeWorkspacesChangeListener(WorkspacesChangeListener listener) {
        getInstance().listeners.remove(listener);
        Log.e("FlowManager", "listener removed, size: " + getInstance().listeners.size());
    }

    //----------------------------------------------------------------------------------------------
    // NET LAYER METHODS ---------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    //RECEIVE --------------------------------------------------------------------------------------

    public static UserDto receive_userRequest(Context context){
        UserDto dto = new UserDto();
        dto.id = getActiveUserID(context);
        return dto;
    }

    public static void receive_userJoined(Context context, UserDto userDto){
        for(WorkspaceDto workspaceDto : getWorkspaces(context))
            if(getWorkspaceUsers(context, workspaceDto.name).contains(userDto.id))
                FlowProxy.getInstance().send_mountWorkspace(context, userDto.id, workspaceDto, null);
    }

    public static void receive_userLeft(Context context, UserDto userDto){
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onUserLeft(userDto);

        for(Workspace workspace : ((ApplicationContext) context).getActiveUser().getWorkspaces().values())
            for(TextFile textFile : workspace.getFiles().values())
                if(textFile.getUserEditing().equals(userDto.id)) {
                    textFile.setAvailability("", true);
                    return;
                }
    }

    public static void receive_userStopEditing(Context context, TextFileDto textFileDto){
        ((ApplicationContext) context).getActiveUser().getWorkspaces().get(textFileDto.workspace).getFiles().get(textFileDto.title).setAvailability("", true);
    }


    public static void receive_uninviteUserFromWorkspace(Context context, String userId, WorkspaceDto workspaceDto){
        ((ApplicationContext) context).getActiveUser().getWorkspaces().get(workspaceDto.name).removeUser(userId);
    }

    public static void receive_mountWorkspace(Context context, WorkspaceDto workspaceDto){
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onWorkspaceAdded(workspaceDto);

        //Notify the rest of the users
        if(getActiveUserID(context).equals(workspaceDto.owner)) {
            for (String userId : ((ApplicationContext) context).getActiveUser().getWorkspaces().get(workspaceDto.name).getUsers())
                FlowProxy.getInstance().send_mountWorkspace(context, userId, workspaceDto, null);
        }
    }

    public static void receive_unmountWorkspace(Context context, WorkspaceDto workspaceDto){
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onWorkspaceRemoved(workspaceDto);

        //Notify the rest of the users
        if(getActiveUserID(context).equals(workspaceDto.owner)) {
            //Update local data
            User user = ((ApplicationContext) context).getActiveUser();
            Workspace workspace = user.getWorkspaces().get(workspaceDto.name);
            user.removeWorkspace(workspaceDto.name, context);
            ((ApplicationContext) context).commit();

            //Notify other users
            for(String userId : workspace.getUsers())
                FlowProxy.getInstance().send_unmountWorkspace(context, userId, workspaceDto, null);
        }
    }

    public static void receive_addFile(Context context, TextFileDto textFileDto) throws AlreadyExistsException, OutOfMemoryException{
        //Update interface
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onFileAdded(textFileDto);

        //Notify the rest of the users
        if(getActiveUserID(context).equals(textFileDto.owner)) {
            //Update local data
            String id = getActiveUserID(context) + "-" + textFileDto.workspace + "-" + textFileDto.title;
            ((ApplicationContext) context).getActiveUser().getWorkspaces().get(textFileDto.workspace).addFile(context, id, textFileDto.title, textFileDto.content);
            ((ApplicationContext) context).commit();

            //Notify other users
            for (String user : ((ApplicationContext) context).getActiveUser().getWorkspaces().get(textFileDto.workspace).getUsers())
                FlowProxy.getInstance().send_addFile(context, user, textFileDto, null);
        }
    }

    public static void receive_removeFile(Context context, TextFileDto textFileDto){
        //Update interface
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onFileRemoved(textFileDto);

        if(getActiveUserID(context).equals(textFileDto.owner)) {
            //Update local data
            ((ApplicationContext) context).getActiveUser().getWorkspaces().get(textFileDto.workspace).removeFile(context, textFileDto.title);
            ((ApplicationContext) context).commit();

            //Notify other users
            for (String user : ((ApplicationContext) context).getActiveUser().getWorkspaces().get(textFileDto.workspace).getUsers())
                FlowProxy.getInstance().send_removeFile(context, user, textFileDto, null);
        }
    }

    public static void receive_editFile(Context context, TextFileDto textFileDto) throws AlreadyExistsException, OutOfMemoryException{
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onFileContentChange(textFileDto);

        //Notify the rest of the users
        if(getActiveUserID(context).equals(textFileDto.owner)) {
            //Update local data
            ((ApplicationContext) context).getActiveUser().getWorkspaces().get(textFileDto.workspace).editFile(context, textFileDto.title, textFileDto.content);

            //Notify other users
            for (String user : ((ApplicationContext) context).getActiveUser().getWorkspaces().get(textFileDto.workspace).getUsers())
                FlowProxy.getInstance().send_editFile(context, user, textFileDto, null);
        }
    }

    public static boolean receive_askToEditFile(Context context, UserDto userDto, TextFileDto textFileDto){
        TextFile file = ((ApplicationContext) context).getActiveUser().getWorkspaces().get(textFileDto.workspace).getFiles().get(textFileDto.title);
        if(file.isAvailable()){
            file.setAvailability(userDto.id, false);
            return true;
        }
        return false;
    }

    public static TextFileDto receive_getFileContent(Context context, TextFileDto textFileDto){
        textFileDto.content = ((ApplicationContext) context).getActiveUser().getWorkspaces().get(textFileDto.workspace).getFiles().get(textFileDto.title).getContent(context);
        return textFileDto;
    }

    public static void receive_subscribe(Context context, UserDto userDto){
        for(WorkspaceDto workspaceDto : getWorkspaces(context)){
            if(getWorkspaceUsers(context, workspaceDto.name).contains(getActiveUserID(context)) ||
                    (!isWorkspacePrivate(context, workspaceDto.name) && Utils.haveElementsInCommon(getWorkspaceTags(context, workspaceDto.name), userDto.subscriptions))) {
                ((ApplicationContext) context).getActiveUser().getWorkspaces().get(workspaceDto.name).addUser(userDto.id);
                FlowProxy.getInstance().send_mountWorkspace(context, userDto.id, workspaceDto, null);
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    // METHODS TO NOTIFY USERS AND INTERFACE  ------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    public static void notifyAddWorkspace(Context context, WorkspaceDto workspaceDto, boolean isPrivate, HashSet<String> users, HashSet<String> tags, long quota) throws AlreadyExistsException {
        //Updates business layer
        Workspace workspace = new Workspace(workspaceDto.name, isPrivate ? Workspace.PRIVACY.PRIVATE : Workspace.PRIVACY.PUBLIC, quota);
        workspace.setTags(tags);
        workspace.setUsers(users);
        ((ApplicationContext) context).getActiveUser().addWorkspace(workspace);
        ((ApplicationContext) context).commit();

        //Notify owner
        FlowProxy.getInstance().send_mountWorkspace(context, workspaceDto.owner, workspaceDto, null);
    }

    public static void notifyEditWorkspace(Context context, WorkspaceDto workspaceDto, boolean isPrivate, HashSet<String> users, HashSet<String> tags, long quota) {
        //Updates business layer
        Workspace workspace = ((ApplicationContext) context).getActiveUser().getWorkspaces().get(workspaceDto.name);
        workspace.setTags(tags);
        workspace.setUsers(users);
        workspace.setPrivacy(isPrivate ? Workspace.PRIVACY.PRIVATE : Workspace.PRIVACY.PUBLIC);
        workspace.setMaximumQuota(quota);
        ((ApplicationContext) context).commit();

        //Update dto
        workspaceDto.files = getFiles(context, workspaceDto.name);

        //Notify other users
        for(String userId : workspace.getUsers())
            if(!users.contains(userId))
                FlowProxy.getInstance().send_unmountWorkspace(context, userId, workspaceDto, null);

        for(String userId : users)
            if(!workspace.getUsers().contains(userId))
                FlowProxy.getInstance().send_mountWorkspace(context, userId, workspaceDto, null);
    }

    //----------------------------------------------------------------------------------------------
    // LOCAL METHODS -------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    public static String getActiveUserID(Context context){
        return ((ApplicationContext) context).getActiveUser().getID();
    }

    public static HashSet<String> getSubscriptions(Context context) {
        return ((ApplicationContext) context).getActiveUser().getSubscriptions();
    }

    public static void setSubscriptions(Context context, HashSet<String> tags) {
        ((ApplicationContext) context).getActiveUser().setSubscriptions(tags);
        ((ApplicationContext) context).commit();
    }

    public static ArrayList<WorkspaceDto> getWorkspaces(Context context) {
        ArrayList<WorkspaceDto> workspaces = new ArrayList<>();
        WorkspaceDto workspaceDto;

        for (Workspace workspace : ((ApplicationContext) context).getActiveUser().getWorkspaces().values()) {
            workspaceDto = new WorkspaceDto();
            workspaceDto.owner = getActiveUserID(context);
            workspaceDto.name = workspace.getName();
            workspaceDto.files = getFiles(context, workspaceDto.name);
            workspaces.add(workspaceDto);
        }
        return workspaces;
    }

    public static HashSet<String> getWorkspaceUsers(Context context, String workspaceName) {
        return ((ApplicationContext) context).getActiveUser().getWorkspaces().get(workspaceName).getUsers();
    }

    public static HashSet<String> getWorkspaceTags(Context context, String workspaceName) {
        return ((ApplicationContext) context).getActiveUser().getWorkspaces().get(workspaceName).getTags();
    }

    public static boolean isWorkspacePrivate(Context context, String workspaceName) {
        Workspace.PRIVACY privacy = ((ApplicationContext) context).getActiveUser().getWorkspaces().get(workspaceName).getPrivacy();
        return privacy == Workspace.PRIVACY.PRIVATE;
    }

    public static long getWorkspaceMemorySize(Context context, String workspaceName) {
        return ((ApplicationContext) context).getActiveUser().getWorkspaces().get(workspaceName).getCurrentMemorySize();
    }

    public static long getWorkspaceMaxQuota(Context context, String workspaceName) {
        return ((ApplicationContext) context).getActiveUser().getWorkspaces().get(workspaceName).getMaximumQuota();
    }

    public static long getUserFreeMemorySpace() {
        return FileManager.getInternalFreeSpace();
    }

    private static ArrayList<TextFileDto> getFiles(Context context, String workspaceName) {
        ArrayList<TextFileDto> files = new ArrayList<>();
        TextFileDto textFileDto;
        for (TextFile f : ((ApplicationContext) context).getActiveUser().getWorkspaces().get(workspaceName).getFiles().values()) {
            textFileDto = new TextFileDto();
            textFileDto.owner = getActiveUserID(context);
            textFileDto.workspace = workspaceName;
            textFileDto.title = f.getTitle();
            files.add(textFileDto);
        }
        return files;
    }
}
