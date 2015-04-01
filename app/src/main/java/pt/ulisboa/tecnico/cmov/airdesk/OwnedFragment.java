package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.io.Serializable;
import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.airdesk.adapter.WorkspaceListAdapter;
import pt.ulisboa.tecnico.cmov.airdesk.listener.WorkspacesChangeListener;
import pt.ulisboa.tecnico.cmov.airdesk.other.TextFile;
import pt.ulisboa.tecnico.cmov.airdesk.other.User;
import pt.ulisboa.tecnico.cmov.airdesk.other.Workspace;

public class OwnedFragment extends ExpandableListFragment implements Serializable {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_owned, container, false);
        makeAdapter((ExpandableListView) view.findViewById(R.id.ownedListView), R.layout.list_group_owner, R.layout.list_item);
        populateView();

        User user = ((ApplicationContext) getActivity().getApplicationContext()).getActiveUser();
        user.setEventListener(new WorkspacesChangeListener(){
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
        });

        final ExpandableListView workspaceList = (ExpandableListView) view.findViewById(R.id.ownedListView);
        workspaceList.setOnChildClickListener(
                new ExpandableListView.OnChildClickListener() {
                    @Override
                    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                        Intent intent = new Intent(getActivity().getApplicationContext(), ShowFileActivity.class);
                        String workspace = (String)((WorkspaceListAdapter) (workspaceList.getExpandableListAdapter())).getGroup(groupPosition);
                        User user = ((ApplicationContext) (getActivity().getApplicationContext())).getActiveUser();
                        TextFile f = user.getWorkspaceList().get(workspace).getFiles().get(childPosition);
                        intent.putExtra(ShowFileActivity.TITLE, f.getTitle());
                        intent.putExtra(ShowFileActivity.TEXT, f.getContent());
                        startActivity(intent);
                        return false;
                    }
                });

        setHasOptionsMenu(true);
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
                Intent intent = new Intent(getActivity().getApplicationContext(), NewWorkspaceActivity.class);
                getActivity().startActivityForResult(intent, MainMenu.OWNED);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

   //Adds workspace views to the list view
    private void populateView(){
        HashMap<String, Workspace> workspaces = ((ApplicationContext) getActivity().getApplicationContext()).getActiveUser().getWorkspaceList();
        for(Workspace w : workspaces.values()){
            getAdapter().addGroup(w.getName());
            for(TextFile f : w.getFiles()){
                getAdapter().addChild(w.getName(), f.getTitle());
            }
        }
    }
}