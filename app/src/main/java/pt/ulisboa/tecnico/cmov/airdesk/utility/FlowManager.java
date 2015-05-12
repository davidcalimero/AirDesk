package pt.ulisboa.tecnico.cmov.airdesk.utility;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;

import pt.ulisboa.tecnico.cmov.airdesk.ApplicationContext;
import pt.ulisboa.tecnico.cmov.airdesk.dto.TextFileDto;
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

    public static void receive_uninviteUserFromWorkspace(Context context, String userId, WorkspaceDto workspaceDto){
        ((ApplicationContext) context).getActiveUser().getWorkspaces().get(workspaceDto.name).removeUser(userId);
    }

    public static void receive_mountWorkspace(WorkspaceDto workspaceDto){
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onWorkspaceAdded(workspaceDto);
    }

    public static void receive_unmountWorkspace(WorkspaceDto workspaceDto){
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onWorkspaceRemoved(workspaceDto);
    }

    public static void receive_addFile(Context context, TextFileDto textFileDto){
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onFileAdded(textFileDto);

        //Notify the rest of the users
        if(getActiveUserID(context).equals(textFileDto.owner))
            for(String user : ((ApplicationContext) context).getActiveUser().getWorkspaces().get(textFileDto.workspace).getUsers())
                FlowProxy.send_addFile(user, textFileDto, context);
    }

    public static void receive_removeFile(Context context, TextFileDto textFileDto){
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onFileRemoved(textFileDto);

        //Notify the rest of the users
        if(getActiveUserID(context).equals(textFileDto.owner))
            for(String user : ((ApplicationContext) context).getActiveUser().getWorkspaces().get(textFileDto.workspace).getUsers())
                FlowProxy.send_removeFile(user, textFileDto, context);
    }

    public static void receive_editFile(Context context, TextFileDto textFileDto){
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onFileContentChange(textFileDto);

        //Notify the rest of the users
        if(getActiveUserID(context).equals(textFileDto.owner))
            for(String user : ((ApplicationContext) context).getActiveUser().getWorkspaces().get(textFileDto.workspace).getUsers())
                FlowProxy.send_editFile(user, textFileDto, context);
    }

    public static boolean receive_askToEditFile(Context context, TextFileDto textFileDto){
        TextFile file = ((ApplicationContext) context).getActiveUser().getWorkspaces().get(textFileDto.workspace).getFiles().get(textFileDto.title);
            if(file.isAvailable()){
            //TODO file.setAvailability(false);
            return true;
        }
        return false;
    }

    public static String receive_getFileContent(Context context, TextFileDto textFileDto){
        return ((ApplicationContext) context).getActiveUser().getWorkspaces().get(textFileDto.workspace).getFiles().get(textFileDto.title).getContent(context);
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
        FlowProxy.send_mountWorkspace(workspaceDto.owner, workspaceDto, context);

        //Notify other users
        for(String userId : users)
            FlowProxy.send_mountWorkspace(userId, workspaceDto, context);
    }

    public static void notifyRemoveWorkspace(Context context, WorkspaceDto workspaceDto) {
        //Updates business layer
        User user = ((ApplicationContext) context).getActiveUser();
        Workspace workspace = user.getWorkspaces().get(workspaceDto.name);
        user.removeWorkspace(workspaceDto.name, context);
        ((ApplicationContext) context).commit();

        //Notify owner
        FlowProxy.send_unmountWorkspace(workspaceDto.owner, workspaceDto, context);

        //Notify other users
        for(String userId : workspace.getUsers())
            FlowProxy.send_unmountWorkspace(userId, workspaceDto, context);
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
                FlowProxy.send_unmountWorkspace(userId, workspaceDto, context);

        for(String userId : users)
            if(!workspace.getUsers().contains(userId))
                FlowProxy.send_mountWorkspace(userId, workspaceDto, context);
    }

    public static void notifyAddFile(Context context, TextFileDto textFileDto) throws AlreadyExistsException, OutOfMemoryException {
        //Updates business layer
        User user = ((ApplicationContext) context).getActiveUser();
        if(user.getID().equals(textFileDto.owner)) {
            String id = user.getID() + "-" + textFileDto.workspace + "-" + textFileDto.title;
            user.getWorkspaces().get(textFileDto.workspace).addFile(context, id, textFileDto.title, textFileDto.content);
            ((ApplicationContext) context).commit();
        }

        //Notify owner
        FlowProxy.send_addFile(textFileDto.owner, textFileDto, context);
    }

    public static void notifyRemoveFile(Context context, TextFileDto textFileDto) {
        //Only updates the user files list if he is the owner
        User user = ((ApplicationContext) context).getActiveUser();
        if(user.getID().equals(textFileDto.owner)) {
            user.getWorkspaces().get(textFileDto.workspace).removeFile(context, textFileDto.title);
            ((ApplicationContext) context).commit();
        }

        //Notify owner
        FlowProxy.send_removeFile(textFileDto.owner, textFileDto, context);
    }

    public static void notifyEditFile(Context context, TextFileDto textFileDto) throws OutOfMemoryException {
        //Only updates the user file if he is the owner
        User user = ((ApplicationContext) context).getActiveUser();
        if(user.getID().equals(textFileDto.owner))
            user.getWorkspaces().get(textFileDto.workspace).editFile(context, textFileDto.title, textFileDto.content);

        //Notify owner
        FlowProxy.send_editFile(textFileDto.owner, textFileDto, context);
    }

    //----------------------------------------------------------------------------------------------
    // METHODS TO ASK OWNER  -----------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    public static void updateForeignList(Context context, HashSet<String> tags){
        // N-Version TODO
        // Get public workspaces from users
        // Compare tags from Subscription and Public Profile

        MessagePack dto = new MessagePack();
        dto.request = MessagePack.HELLO_WORLD;
        ((ApplicationContext) context).getWifiDirectService().sendMessage(dto);

        // S-Version
        for(WorkspaceDto workspaceDto : getWorkspaces(context)){
            if(getWorkspaceUsers(context, workspaceDto.name).contains(getActiveUserID(context)) ||
                    (!isWorkspacePrivate(context, workspaceDto.name) && Utils.haveElementsInCommon(getWorkspaceTags(context, workspaceDto.name), tags))) {
                FlowProxy.send_mountWorkspace(getActiveUserID(context), workspaceDto, context);
            }
        }
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
