package pt.ulisboa.tecnico.cmov.airdesk.listener;

public interface WorkspacesChangeListener {

    public void onWorkspaceAdded(String owner, String name);

    //TODO REMOVE METHOD ON VERSION N
    public void onWorkspaceAddedForeign(String owner, String workspaceName);
    public void onWorkspaceRemovedForeign(String owner, String workspaceName);

    public void onWorkspaceRemoved(String owner, String workspaceName);

    public void onFileAdded(String owner, String workspaceName, String fileName);

    public void onFileRemoved(String owner, String workspaceName, String fileName);

    public void onFileContentChange(String owner, String workspaceName, String filename, String content);

    public void onWorkspaceUserRemoved(String owner, String workspaceName);
}
