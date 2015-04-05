package pt.ulisboa.tecnico.cmov.airdesk.listener;

import java.util.ArrayList;

public interface WorkspacesChangeListener {

    public void onWorkspaceCreated(String name);

    public void onWorkspaceRemoved(String name);

    public void onFileCreated(String workspaceName, String fileName);

    public void onFileRemoved(String workspaceName, String fileName);

    public void onWorkspaceEdited(String workspaceName, boolean isPrivate, ArrayList<CharSequence> users, ArrayList<CharSequence> tags);

    public void onSubscriptionsChange(ArrayList<CharSequence> subscriptions);

    public void onFileContentChange(String workspaceName, String filename, String content);
}
