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

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.CreateEditWorkspaceActivity;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.listener.WorkspacesChangeListener;
import pt.ulisboa.tecnico.cmov.airdesk.other.FlowManager;

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
            public void onWorkspaceAdded(String owner, String name) {
                if(userId.equals(owner))
                    addWorkspace(owner, name);
            }

            @Override
            public void onWorkspaceAddedForeign(String owner, String name, ArrayList<String> files) {
                //TODO remove this method in version N
            }

            @Override
            public void onWorkspaceRemovedForeign(String owner, String workspaceName) {
                //TODO remove this method in version N
            }

            @Override
            public void onWorkspaceRemoved(String owner, String name) {
                if(userId.equals(owner))
                    removeWorkspace(owner, name);
            }

            @Override
            public void onFileAdded(String owner, String workspaceName, String fileName) {
                if(userId.equals(owner))
                    addFile(owner, workspaceName, fileName);
            }

            @Override
            public void onFileRemoved(String owner, String workspaceName, String fileName) {
                if(userId.equals(owner))
                    removeFile(owner, workspaceName, fileName);
            }

            @Override
            public void onFileContentChange(String owner, String workspaceName, String filename, String content) {}
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
                Intent intent = new Intent(getActivity().getApplicationContext(), CreateEditWorkspaceActivity.class);
                intent.putExtra(CreateEditWorkspaceActivity.ACTIVITY_MODE, CreateEditWorkspaceActivity.MODE.CREATE);
                intent.putExtra(CreateEditWorkspaceActivity.ACTIVITY_TITLE, getString(R.string.create_new_workspace));
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    //Adds workspace views to the list view
    private void populateView() {
        for (String w : FlowManager.getWorkspaces(getActivity().getApplicationContext())) {
            addWorkspace(userId, w);
            for (String t : FlowManager.getFiles(getActivity().getApplicationContext(), w))
                addFile(userId, w, t);
        }
    }
}