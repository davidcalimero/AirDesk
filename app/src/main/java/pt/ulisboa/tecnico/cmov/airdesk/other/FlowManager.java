package pt.ulisboa.tecnico.cmov.airdesk.other;

import android.content.Context;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.ApplicationContext;
import pt.ulisboa.tecnico.cmov.airdesk.listener.WorkspacesChangeListener;

public class FlowManager {

    private static FlowManager singleton = null;

    private WorkspacesChangeListener listener;

    private static FlowManager getInstance() {
        if(singleton == null) {
            singleton = new FlowManager();
        }
        return singleton;
    }

    public static void setWorkspacesChangeListener(WorkspacesChangeListener listener){
        getInstance().listener = listener;
    }

    public static void notifyAddWorkspace(Context context, String name, Workspace.MODE mode, ArrayList<CharSequence> tags, long quota){
        if(getInstance().listener != null)
            getInstance().listener.onWorkspaceCreated(name);
        User user = ((ApplicationContext) context).getActiveUser();
        Workspace workspace = new Workspace(name, user.getID(), mode, quota);
        workspace.setPublicProfile(tags);
        user.addWorkspace(workspace);
    }

    public static void notifyRemoveWorkspace(Context context, String name){
        if(getInstance().listener != null)
            getInstance().listener.onWorkspaceRemoved(name);
        ((ApplicationContext) context).getActiveUser().removeWorkspace(name);
    }

    public static void notifyAddFile(Context context, String workspaceName, String fileName, String content){
        if(getInstance().listener != null)
            getInstance().listener.onFileCreated(workspaceName, fileName);
        User user = ((ApplicationContext) context).getActiveUser();
        String file = user.getID() + "-" + workspaceName + "-" + "-" + fileName;
        user.getWorkspaceList().get(workspaceName).addFile(new TextFile(context, file, fileName, content));
    }

    public static void notifyRemoveFile(Context context, String workspaceName, String fileName){
        if(getInstance().listener != null)
            getInstance().listener.onFileRemoved(workspaceName, fileName);
        User user = ((ApplicationContext) context).getActiveUser();
        user.getWorkspaceList().get(workspaceName).removeFile(context, fileName);
    }

    public static void notifyEditFile(Context context, String workspaceName, String fileName, String content){
        User user = ((ApplicationContext) context).getActiveUser();
        user.getWorkspaceList().get(workspaceName).getFiles().get(fileName).setContent(context, content);
    }

    public static void notifySubscriptionsChange(Context context, ArrayList<CharSequence> tags){
        ((ApplicationContext) context).getActiveUser().setSubscriptions(tags);
    }

    public static String getFileContent(Context context, String workspaceName, String fileName){
        User user = ((ApplicationContext) context).getActiveUser();
        return user.getWorkspaceList().get(workspaceName).getFiles().get(fileName).getContent(context);
    }

    public static void notifyEditWorkspace(Context context, String workspaceName, ArrayList<CharSequence> users, Workspace.MODE privacy, long quota){
        Workspace w = ((ApplicationContext) context).getActiveUser().getWorkspaceList().get(workspaceName);
        w.editWorkspace(users, privacy, quota);
    }
}
