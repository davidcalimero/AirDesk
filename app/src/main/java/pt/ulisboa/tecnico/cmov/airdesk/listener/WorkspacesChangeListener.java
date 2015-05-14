package pt.ulisboa.tecnico.cmov.airdesk.listener;

import pt.ulisboa.tecnico.cmov.airdesk.dto.TextFileDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.UserDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.WorkspaceDto;

public interface WorkspacesChangeListener {

    void onUserLeft(UserDto userDto);

    void onWorkspaceAdded(WorkspaceDto workspaceDto);

    void onWorkspaceRemoved(WorkspaceDto workspaceDto);

    void onFileAdded(TextFileDto textFileDto);

    void onFileRemoved(TextFileDto textFileDto);

    void onFileContentChange(TextFileDto textFileDto);
}
