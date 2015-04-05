package pt.ulisboa.tecnico.cmov.airdesk.other;

import android.content.Context;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.ApplicationContext;
import pt.ulisboa.tecnico.cmov.airdesk.exception.AlreadyExistsException;
import pt.ulisboa.tecnico.cmov.airdesk.exception.InvalidInputException;
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

    public static void addWorkspacesChangeListener(WorkspacesChangeListener listener) {
        getInstance().listeners.add(listener);
    }

    //----------------------------------------------------------------------------------------------

    public static void notifyAddWorkspace(Context context, String name, boolean isPrivate, ArrayList<CharSequence> users, ArrayList<CharSequence> tags, long quota) throws AlreadyExistsException, InvalidInputException {
        String newName = Utils.trim(name);
        if(newName.length() == 0)
            throw new InvalidInputException();

        Workspace workspace = new Workspace(newName, isPrivate ? Workspace.PRIVACY.PRIVATE : Workspace.PRIVACY.PUBLIC, quota);
        workspace.setTagList(tags);
        workspace.setUserList(users);
        ((ApplicationContext) context).getActiveUser().addWorkspace(workspace);

        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onWorkspaceCreated(newName);

        ((ApplicationContext) context).commit();
    }

    public static void notifyRemoveWorkspace(Context context, String name) {
        ((ApplicationContext) context).getActiveUser().removeWorkspace(name, context);

        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onWorkspaceRemoved(name);

        ((ApplicationContext) context).commit();
    }

    public static void notifyAddFile(Context context, String workspaceName, String fileName, String content) throws AlreadyExistsException, OutOfMemoryException, InvalidInputException {
        String newFileName = Utils.trim(fileName);
        if(newFileName.length() == 0)
            throw new InvalidInputException();

        User user = ((ApplicationContext) context).getActiveUser();

        String file = user.getID() + "-" + workspaceName + "-" + fileName;
        user.getWorkspaceList().get(workspaceName).addFile(context, file, fileName, content);

        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onFileCreated(workspaceName, fileName);

        ((ApplicationContext) context).commit();
    }

    public static void notifyRemoveFile(Context context, String workspaceName, String fileName) {
        ((ApplicationContext) context).getActiveUser().getWorkspaceList().get(workspaceName).removeFile(context, fileName);

        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onFileRemoved(workspaceName, fileName);

        ((ApplicationContext) context).commit();
    }

    public static void notifySubscriptionsChange(Context context, ArrayList<CharSequence> tags) {
        //TODO check input add and remove methods
        ((ApplicationContext) context).getActiveUser().setSubscriptions(tags);

        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onSubscriptionsChange(tags);

        ((ApplicationContext) context).commit();
    }

    public static void notifyEditWorkspace(Context context, String workspaceName, boolean isPrivate, ArrayList<CharSequence> users, ArrayList<CharSequence> tags, long quota) {
        //TODO verify input and and remove methods
        Workspace workspace = ((ApplicationContext) context).getActiveUser().getWorkspaceList().get(workspaceName);
        workspace.setUserList(users);
        workspace.setTagList(tags);
        workspace.setPrivacy(isPrivate ? Workspace.PRIVACY.PRIVATE : Workspace.PRIVACY.PUBLIC);
        workspace.setMaximumQuota(quota);

        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onWorkspaceEdited(workspaceName, isPrivate, users, tags);

        ((ApplicationContext) context).commit();
    }

    public static void notifyEditFile(Context context, String workspaceName, String fileName, String content) throws OutOfMemoryException {
        ((ApplicationContext) context).getActiveUser().getWorkspaceList().get(workspaceName).editFile(context, fileName, content);
        for (WorkspacesChangeListener l : getInstance().listeners)
            l.onFileContentChange(workspaceName, fileName, content);
    }

    //----------------------------------------------------------------------------------------------

    public static boolean canPermissionToEditFile(Context context, String workspaceName, String fileName) {
        return true;
    }

    //----------------------------------------------------------------------------------------------

    public static ArrayList<CharSequence> getSubscriptions(Context context) {
        return ((ApplicationContext) context).getActiveUser().getSubscriptions();
    }

    public static ArrayList<String> getWorkspaces(Context context) {
        ArrayList<String> workspaces = new ArrayList<>();
        for (Workspace w : ((ApplicationContext) context).getActiveUser().getWorkspaceList().values()) {
            workspaces.add(w.getName());
        }
        return workspaces;
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
        for (TextFile f : ((ApplicationContext) context).getActiveUser().getWorkspaceList().get(workspaceName).getFiles().values()) {
            files.add(f.getTitle());
        }
        return files;
    }

    public static String getFileContent(Context context, String workspaceName, String fileName) {
        return ((ApplicationContext) context).getActiveUser().getWorkspaceList().get(workspaceName).getFiles().get(fileName).getContent(context);
    }
}
