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

import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.airdesk.ApplicationContext;
import pt.ulisboa.tecnico.cmov.airdesk.MainMenu;
import pt.ulisboa.tecnico.cmov.airdesk.NewWorkspaceActivity;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.other.FlowManager;
import pt.ulisboa.tecnico.cmov.airdesk.other.TextFile;
import pt.ulisboa.tecnico.cmov.airdesk.other.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.listener.WorkspacesChangeListener;

public class OwnedFragment extends ExpandableListFragment /*implements Serializable*/ {

    /*private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String workspaceName = intent.getStringExtra(Utils.WORKSPACE_NAME);
            String fileName = intent.getStringExtra(Utils.FILE_NAME);

            switch (intent.getAction()){
                case Utils.ADD_WORKSPACE:
                    Log.e("OwnedFragment", "ADD_WORKSPACE: " + workspaceName);
                    getAdapter().addGroup(workspaceName);
                    break;
                case Utils.REMOVE_WORKSPACE:
                    Log.e("OwnedFragment", "REMOVE_WORKSPACE: " + workspaceName);
                    getAdapter().removeGroup(workspaceName);
                    break;
                case Utils.ADD_FILE:
                    Log.e("OwnedFragment", "ADD_FILE: " + "[" + workspaceName + "] " + fileName);
                    getAdapter().addChild(workspaceName, fileName);
                    break;
                case Utils.REMOVE_FILE:
                    Log.e("OwnedFragment", "REMOVE_FILE: " + "[" + workspaceName + "] " + fileName);
                    getAdapter().removeChild(workspaceName, fileName);
                    break;
                default:
                    return;
            }
            getAdapter().notifyDataSetChanged();
        }
    };*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_owned, container, false);
        makeAdapter((ExpandableListView) view.findViewById(R.id.ownedListView), R.layout.list_group_owner, R.layout.list_item);
        populateView();

        /*//Receiver register
        IntentFilter filter = new IntentFilter();
        filter.addAction(Utils.ADD_WORKSPACE);
        filter.addAction(Utils.REMOVE_WORKSPACE);
        filter.addAction(Utils.ADD_FILE);
        filter.addAction(Utils.REMOVE_FILE);
        getActivity().registerReceiver(receiver, filter);*/

        FlowManager.setWorkspacesChangeListener(new WorkspacesChangeListener() {
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
            for(TextFile t : w.getFiles().values())
                getAdapter().addChild(w.getName(), t.getTitle());
        }
    }
}