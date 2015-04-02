package pt.ulisboa.tecnico.cmov.airdesk.listener;

public interface WorkspacesChangeListener {

    public void onWorkspaceCreated(String name);

    public void onWorkspaceRemoved(String name);

    public void onFileCreated(String workspaceName, String fileName);

    public void onFileRemoved(String workspaceName, String fileName);

}
