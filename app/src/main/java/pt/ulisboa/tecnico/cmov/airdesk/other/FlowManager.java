package pt.ulisboa.tecnico.cmov.airdesk.other;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
    // METHODS TO NOTIFY USERS  --------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    public static void notifyAddWorkspace(Context context, String name, boolean isPrivate, HashSet<CharSequence> users, HashSet<CharSequence> tags, long quota) throws AlreadyExistsException {
        //Only updates the user workspace list if he is the owner
        Workspace workspace = new Workspace(name, isPrivate ? Workspace.PRIVACY.PRIVATE : Workspace.PRIVACY.PUBLIC, quota);
        workspace.setTags(tags);
        ((ApplicationContext) context).getActiveUser().addWorkspace(workspace);
        ((ApplicationContext) context).commit();

        //Updates interface
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onWorkspaceAdded(getActiveUserID(context), name);

        //send information to users
        for(CharSequence u : users)
            notifyNewWorkspaceUser(context, name, u.toString(), new ArrayList<String>());
    }

    public static void notifyRemoveWorkspace(Context context, String workspaceName) {
        //Only updates the user workspace list if he is the owner
        ((ApplicationContext) context).getActiveUser().removeWorkspace(workspaceName, context);
        ((ApplicationContext) context).commit();

        //Updates interface
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onWorkspaceRemoved(getActiveUserID(context), workspaceName);

        //send information to users
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onWorkspaceRemovedForeign(getActiveUserID(context), workspaceName);
    }

    public static void notifyEditWorkspace(Context context, String workspaceName, boolean isPrivate, HashMap<CharSequence, Boolean> usersChanges, HashSet<CharSequence> tags, long quota) {
        Workspace workspace = ((ApplicationContext) context).getActiveUser().getWorkspaces().get(workspaceName);
        for(CharSequence item : usersChanges.keySet()) {
            if (usersChanges.get(item))
                notifyNewWorkspaceUser(context, workspaceName, item.toString(), getFiles(context, workspaceName));
            else
                notifyRemoveWorkspaceUser(context, getActiveUserID(context), workspaceName, item.toString());
        }
        workspace.setTags(tags);
        workspace.setPrivacy(isPrivate ? Workspace.PRIVACY.PRIVATE : Workspace.PRIVACY.PUBLIC);
        workspace.setMaximumQuota(quota);
        ((ApplicationContext) context).commit();
    }

    public static void notifyAddFile(Context context, String owner, String workspaceName, String fileName, String content) throws AlreadyExistsException, OutOfMemoryException {
        //Only updates the user files list if he is the owner
        User user = ((ApplicationContext) context).getActiveUser();
        if(user.getID().equals(owner)) {
            String id = user.getID() + "-" + workspaceName + "-" + fileName;
            user.getWorkspaces().get(workspaceName).addFile(context, id, fileName, content);
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
            user.getWorkspaces().get(workspaceName).removeFile(context, fileName);
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
        if(user.getID().equals(owner)){
            user.getWorkspaces().get(workspaceName).editFile(context, fileName, content);
        }

        //Updates interface
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onFileContentChange(owner, workspaceName, fileName, content);

        //send information to users
    }

    public static void notifyNewWorkspaceUser(Context context, String workspaceName, String user, ArrayList<String> files) {
        ((ApplicationContext) context).getActiveUser().getWorkspaces().get(workspaceName).addUser(user);
        ((ApplicationContext) context).commit();

        //send information to users
        for (WorkspacesChangeListener l : getInstance().listeners) {
            l.onWorkspaceAddedForeign(getActiveUserID(context), workspaceName, files);
        }
    }

    public static void notifyRemoveWorkspaceUser(Context context, String owner, String workspaceName, String user) {
        ((ApplicationContext) context).getActiveUser().getWorkspaces().get(workspaceName).removeUser(user);
        ((ApplicationContext) context).commit();

        //send information to users
        if(user.equals(getActiveUserID(context))) {
            for (WorkspacesChangeListener l : getInstance().listeners)
                l.onWorkspaceRemovedForeign(owner, workspaceName);
        }
    }

    //----------------------------------------------------------------------------------------------
    // METHODS TO ASK OWNER  -----------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    public static boolean askToEdit(Context context, String workspaceName, String fileName) {
        TextFile file = ((ApplicationContext) context).getActiveUser().getWorkspaces().get(workspaceName).getFiles().get(fileName);
        if(file.isAvailable()){
            //TODO file.setAvailability(false);
            return true;
        }
        return false;
    }

    public static String getFileContent(Context context, String owner, String workspaceName, String fileName) {
        return ((ApplicationContext) context).getActiveUser().getWorkspaces().get(workspaceName).getFiles().get(fileName).getContent(context);
    }

    public static void updateForeignList(Context context, HashSet<CharSequence> tags){
        // N-Version TODO
        // Get public workspaces from users
        // Compare tags from Subscription and Public Profile

        // S-Version
        for(String w : getWorkspaces(context)){
            if(getWorkspaceUsers(context, w).contains(getActiveUserID(context)) ||
                    (!isWorkspacePrivate(context, w) && Utils.haveElementsInCommon(getWorkspaceTags(context, w), tags))) {
                // Add user to the list (if not already there)
                notifyNewWorkspaceUser(context, w, getActiveUserID(context), getFiles(context, w));
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    // LOCAL METHODS -------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    public static String getActiveUserID(Context context){
        return ((ApplicationContext) context).getActiveUser().getID();
    }

    public static HashSet<CharSequence> getSubscriptions(Context context) {
        return ((ApplicationContext) context).getActiveUser().getSubscriptions();
    }

    public static void setSubscriptions(Context context, HashSet<CharSequence> tags) {
        ((ApplicationContext) context).getActiveUser().setSubscriptions(tags);
        ((ApplicationContext) context).commit();
    }

    public static ArrayList<String> getWorkspaces(Context context) {
        ArrayList<String> workspaces = new ArrayList<>();
        for (Workspace w : ((ApplicationContext) context).getActiveUser().getWorkspaces().values()) {
            workspaces.add(w.getName());
        }
        return workspaces;
    }

    public static HashSet<CharSequence> getWorkspaceUsers(Context context, String workspaceName) {
        return ((ApplicationContext) context).getActiveUser().getWorkspaces().get(workspaceName).getUsers();
    }

    public static HashSet<CharSequence> getWorkspaceTags(Context context, String workspaceName) {
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

    public static ArrayList<String> getFiles(Context context, String workspaceName) {
        ArrayList<String> files = new ArrayList<>();
        for (TextFile f : ((ApplicationContext) context).getActiveUser().getWorkspaces().get(workspaceName).getFiles().values())
            files.add(f.getTitle());
        return files;
    }
}
