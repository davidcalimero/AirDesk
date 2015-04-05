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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_owned, container, false);
        makeAdapter((ExpandableListView) view.findViewById(R.id.ownedListView), R.layout.list_group_owner, R.layout.list_item);
        setHasOptionsMenu(true);
        populateView();

        FlowManager.addWorkspacesChangeListener(new WorkspacesChangeListener() {
            @Override
            public void onWorkspaceCreated(String name) {
                getAdapter().addGroup(name);
                getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onWorkspaceRemoved(String name) {
                getAdapter().removeGroup(name);
                getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onFileCreated(String workspaceName, String fileName) {
                getAdapter().addChild(workspaceName, fileName);
                getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onFileRemoved(String workspaceName, String fileName) {
                getAdapter().removeChild(workspaceName, fileName);
                getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onWorkspaceEdited(String workspaceName, boolean isPrivate, ArrayList<CharSequence> users, ArrayList<CharSequence> tags) {}

            @Override
            public void onSubscriptionsChange(ArrayList<CharSequence> subscriptions) {}

            @Override
            public void onFileContentChange(String workspaceName, String filename, String content) {}
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
            getAdapter().addGroup(w);
            for (String t : FlowManager.getFiles(getActivity().getApplicationContext(), w))
                getAdapter().addChild(w, t);
        }
    }
}