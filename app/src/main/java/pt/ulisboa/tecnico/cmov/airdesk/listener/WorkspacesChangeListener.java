package pt.ulisboa.tecnico.cmov.airdesk.listener;

import java.util.ArrayList;

public interface WorkspacesChangeListener {

    public void onWorkspaceAdded(String owner, String name);

    public void onWorkspaceRemoved(String owner, String name);

    public void onFileAdded(String owner, String workspaceName, String fileName);

    public void onFileRemoved(String owner, String workspaceName, String fileName);

    public void onWorkspaceEdited(String workspaceName, boolean isPrivate, ArrayList<CharSequence> users, ArrayList<CharSequence> tags);

    public void onSubscriptionsChange(ArrayList<CharSequence> subscriptions);

    public void onFileContentChange(String owner, String workspaceName, String filename, String content);
}
