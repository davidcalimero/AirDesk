package pt.ulisboa.tecnico.cmov.airdesk.other;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.ApplicationContext;
import pt.ulisboa.tecnico.cmov.airdesk.exception.AlreadyExistsException;
import pt.ulisboa.tecnico.cmov.airdesk.exception.OutOfMemoryException;
import pt.ulisboa.tecnico.cmov.airdesk.listener.WorkspacesChangeListener;

public class FlowManager {

    private static FlowManager singleton = null;

    private ArrayList<WorkspacesChangeListener> listeners = new ArrayList<>();

    private static FlowManager getInstance() {
        if (singleton == null) {
            singleton = new FlowManager();
        }
        return singleton;
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
    // METHODS TO NOTIFY OWNER  --------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    public static void notifyRemoveWorkspace(Context context, String owner, String workspaceName) {
        //Only updates the user workspace list if he is the owner
        User user = ((ApplicationContext) context).getActiveUser();
        if(user.getID().equals(owner)) {
            user.removeWorkspace(workspaceName, context);
            ((ApplicationContext) context).commit();
        }

        //Updates interface
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onWorkspaceRemoved(owner, workspaceName);

        //send information to users
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onWorkspaceRemovedForeign(owner, workspaceName);
    }

    public static void notifyAddFile(Context context, String owner, String workspaceName, String fileName, String content) throws AlreadyExistsException, OutOfMemoryException {
        //Only updates the user files list if he is the owner
        User user = ((ApplicationContext) context).getActiveUser();
        if(user.getID().equals(owner)) {
            String id = user.getID() + "-" + workspaceName + "-" + fileName;
            user.getWorkspaceList().get(workspaceName).addFile(context, id, fileName, content);
            ((ApplicationContext) context).commit();
        }

        //Updates interface
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onFileAdded(owner, workspaceName, fileName);

        //send information to users
    }

    public static void notifyRemoveFile(Context context, String owner, String workspaceName, String fileName) {
        //Only updates the user files list if he is the owner
        User user = ((ApplicationContext) context).getActiveUser();
        if(user.getID().equals(owner)) {
            user.getWorkspaceList().get(workspaceName).removeFile(context, fileName);
            ((ApplicationContext) context).commit();
        }

        //Updates interface
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onFileRemoved(owner, workspaceName, fileName);

        //send information to users
    }

    public static void notifyEditFile(Context context, String owner, String workspaceName, String fileName, String content) throws OutOfMemoryException {
        //Only updates the user file if he is the owner
        User user = ((ApplicationContext) context).getActiveUser();
        if(user.getID().equals(owner))
            user.getWorkspaceList().get(workspaceName).editFile(context, fileName, content);

        //Updates interface
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onFileContentChange(owner, workspaceName, fileName, content);

        //send information to users
    }

    public static void notifyAddWorkspaceUser(Context context, String workspaceName, String user) throws AlreadyExistsException {
        ((ApplicationContext) context).getActiveUser().getWorkspaceList().get(workspaceName).addUser(user);
        ((ApplicationContext) context).commit();

        //send information to users
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onWorkspaceAddedForeign(((ApplicationContext) context).getActiveUser().getID(), workspaceName);
    }

    public static void notifyRemoveWorkspaceUser(Context context, String owner, String workspaceName, String user) {
        ((ApplicationContext) context).getActiveUser().getWorkspaceList().get(workspaceName).removeUser(user);
        ((ApplicationContext) context).commit();

        //send information to users
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onWorkspaceRemovedForeign(owner, workspaceName);
    }

    //----------------------------------------------------------------------------------------------
    // METHODS TO ASK OWNER  -----------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    public static boolean havePermissionToEditFile(Context context, String workspaceName, String fileName) {
        return true;
    }

    public static String getFileContent(Context context, String workspaceName, String fileName) {
        return ((ApplicationContext) context).getActiveUser().getWorkspaceList().get(workspaceName).getFiles().get(fileName).getContent(context);
    }

    public static void updateForeignList(Context context, ArrayList<CharSequence> tags){
        // N-Version TODO
        // Get public workspaces from users
        // Compare tags from Subscription and Public Profile

        // S-Version
        for(String w : getWorkspaces(context)){
            if(getWorkspaceUserList(context, w).contains(getActiveUserID(context)) ||
                    (!isWorkspacePrivate(context, w) && Utils.haveElementsInCommon(getWorkspaceTagList(context, w), tags))) {
                // Add user to the list (if not already there)
                try {
                    notifyAddWorkspaceUser(context, w, getActiveUserID(context));
                } catch (AlreadyExistsException e) {
                }
                for (WorkspacesChangeListener l : getInstance().listeners) {
                    // Warn Foreign Fragment
                    l.onWorkspaceAddedForeign(((ApplicationContext) context).getActiveUser().getID(), w);
                    for(String f : getFiles(context, w))
                        l.onFileAdded(((ApplicationContext) context).getActiveUser().getID(), w, f);
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    // LOCAL METHODS -------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    public static String getActiveUserID(Context context){
        return ((ApplicationContext) context).getActiveUser().getID();
    }

    public static ArrayList<CharSequence> getSubscriptions(Context context) {
        return ((ApplicationContext) context).getActiveUser().getSubscriptions();
    }

    public static void setSubscriptions(Context context, ArrayList<CharSequence> tags) {
        ((ApplicationContext) context).getActiveUser().setSubscriptions(tags);
        ((ApplicationContext) context).commit();
    }

    public static ArrayList<String> getWorkspaces(Context context) {
        ArrayList<String> workspaces = new ArrayList<>();
        for (Workspace w : ((ApplicationContext) context).getActiveUser().getWorkspaceList().values()) {
            workspaces.add(w.getName());
        }
        return workspaces;
    }

    public static void addWorkspace(Context context, String owner, String name, boolean isPrivate, ArrayList<CharSequence> users, ArrayList<CharSequence> tags, long quota) throws AlreadyExistsException {
        //Only updates the user workspace list if he is the owner
        User user = ((ApplicationContext) context).getActiveUser();
        if(user.getID().equals(owner)) {
            Workspace workspace = new Workspace(name, isPrivate ? Workspace.PRIVACY.PRIVATE : Workspace.PRIVACY.PUBLIC, quota);
            workspace.setTagList(tags);
            workspace.setUserList(users);
            user.addWorkspace(workspace);
            ((ApplicationContext) context).commit();
        }

        //Updates interface
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onWorkspaceAdded(owner, name);
    }

    public static void editWorkspace(Context context, String workspaceName, boolean isPrivate, ArrayList<CharSequence> users, ArrayList<CharSequence> tags, long quota) {
        //TODO add and remove methods
        Workspace workspace = ((ApplicationContext) context).getActiveUser().getWorkspaceList().get(workspaceName);
        workspace.setUserList(users);
        workspace.setTagList(tags);
        workspace.setPrivacy(isPrivate ? Workspace.PRIVACY.PRIVATE : Workspace.PRIVACY.PUBLIC);
        workspace.setMaximumQuota(quota);
        ((ApplicationContext) context).commit();
    }

    public static ArrayList<CharSequence> getWorkspaceUserList(Context context, String workspaceName) {
        return ((ApplicationContext) context).getActiveUser().getWorkspaceList().get(workspaceName).getUserList();
    }

    public static ArrayList<CharSequence> getWorkspaceTagList(Context context, String workspaceName) {
        return ((ApplicationContext) context).getActiveUser().getWorkspaceList().get(workspaceName).getTagList();
    }

    public static boolean isWorkspacePrivate(Context context, String workspaceName) {
        Workspace.PRIVACY privacy = ((ApplicationContext) context).getActiveUser().getWorkspaceList().get(workspaceName).getPrivacy();
        return privacy == Workspace.PRIVACY.PRIVATE;
    }

    public static long getWorkspaceMemorySize(Context context, String workspaceName) {
        return ((ApplicationContext) context).getActiveUser().getWorkspaceList().get(workspaceName).getCurrentMemorySize();
    }

    public static long getWorkspaceMaxQuota(Context context, String workspaceName) {
        return ((ApplicationContext) context).getActiveUser().getWorkspaceList().get(workspaceName).getMaximumQuota();
    }

    public static long getUserFreeMemorySpace() {
        return FileManager.getInternalFreeSpace();
    }

    public static ArrayList<String> getFiles(Context context, String workspaceName) {
        ArrayList<String> files = new ArrayList<>();
        for (TextFile f : ((ApplicationContext) context).getActiveUser().getWorkspaceList().get(workspaceName).getFiles().values())
            files.add(f.getTitle());
        return files;
    }
}
