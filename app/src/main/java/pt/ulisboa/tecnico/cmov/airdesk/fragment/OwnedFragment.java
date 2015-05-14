package pt.ulisboa.tecnico.cmov.airdesk.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import pt.ulisboa.tecnico.cmov.airdesk.CreateEditWorkspaceActivity;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.dto.TextFileDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.UserDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.WorkspaceDto;
import pt.ulisboa.tecnico.cmov.airdesk.listener.WorkspacesChangeListener;
import pt.ulisboa.tecnico.cmov.airdesk.utility.FlowManager;

public class OwnedFragment extends ExpandableListFragment {

    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_owned, container, false);
        makeAdapter((ExpandableListView) view.findViewById(R.id.ownedListView), R.layout.list_group_owner, R.layout.list_item);
        setHasOptionsMenu(true);
        userId = FlowManager.getActiveUserID(getActivity().getApplicationContext());
        populateView();

        setListener(new WorkspacesChangeListener() {
            @Override
            public void onUserLeft(UserDto userDto) {}

            @Override
            public void onWorkspaceAdded(WorkspaceDto workspaceDto) {
                if(userId.equals(workspaceDto.owner))
                    addWorkspace(workspaceDto.owner, workspaceDto.name);
            }

            @Override
            public void onWorkspaceRemoved(WorkspaceDto workspaceDto) {
                if(userId.equals(workspaceDto.owner))
                    removeWorkspace(workspaceDto.owner, workspaceDto.name);
            }

            @Override
            public void onFileAdded(TextFileDto textFileDto) {
                if(userId.equals(textFileDto.owner))
                    addFile(textFileDto.owner, textFileDto.workspace, textFileDto.title);
            }

            @Override
            public void onFileRemoved(TextFileDto textFileDto) {
                if(userId.equals(textFileDto.owner))
                    removeFile(textFileDto.owner, textFileDto.workspace, textFileDto.title);
            }

            @Override
            public void onFileContentChange(TextFileDto textFileDto) {}
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_owned, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_new_workspace:
                WorkspaceDto workspaceDto = new WorkspaceDto();
                workspaceDto.owner = FlowManager.getActiveUserID(getActivity().getApplicationContext());

                Intent intent = new Intent(getActivity().getApplicationContext(), CreateEditWorkspaceActivity.class);
                intent.putExtra(CreateEditWorkspaceActivity.ACTIVITY_MODE, CreateEditWorkspaceActivity.MODE.CREATE);
                intent.putExtra(CreateEditWorkspaceActivity.ACTIVITY_TITLE, getString(R.string.create_new_workspace));
                intent.putExtra(CreateEditWorkspaceActivity.WORKSPACE_DTO, workspaceDto);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    //Adds workspace views to the list view
    private void populateView() {
        for (WorkspaceDto workspace : FlowManager.getWorkspaces(getActivity().getApplicationContext())) {
            addWorkspace(userId, workspace.name);
            for (TextFileDto textFileDto : workspace.files)
                addFile(userId, workspace.name, textFileDto.title);
        }
    }
}