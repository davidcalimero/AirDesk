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
import java.util.HashSet;

import pt.ulisboa.tecnico.cmov.airdesk.ListActivity;
import pt.ulisboa.tecnico.cmov.airdesk.MainMenu;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.listener.WorkspacesChangeListener;
import pt.ulisboa.tecnico.cmov.airdesk.other.FlowManager;

public class ForeignFragment extends ExpandableListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_foreign, container, false);
        setHasOptionsMenu(true);
        setListener(new WorkspacesChangeListener() {
            @Override
            public void onWorkspaceAddedForeign(String owner, String workspaceName, ArrayList<String> files) {
                //TODO remove this method in version N
                addWorkspace(owner, workspaceName);
                for(String fileName : files)
                    addFile(owner, workspaceName, fileName);
            }

            @Override
            public void onWorkspaceRemovedForeign(String owner, String workspaceName) {
                //TODO remove this method in version N
                removeWorkspace(owner, workspaceName);
            }

            @Override
            public void onWorkspaceAdded(String owner, String name) {}

            @Override
            public void onWorkspaceRemoved(String owner, String name) {}

            @Override
            public void onFileAdded(String owner, String workspaceName, String fileName) {
                addFile(owner, workspaceName, fileName);
            }

            @Override
            public void onFileRemoved(String owner, String workspaceName, String fileName) {
                removeFile(owner, workspaceName, fileName);
            }

            @Override
            public void onFileContentChange(String owner, String workspaceName, String filename, String content) {}
        });

        refreshView(view);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_foreign, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh:
                refreshView(getView());
                break;

            case R.id.action_subscriptions:
                HashSet<CharSequence> subscriptions = FlowManager.getSubscriptions(getActivity().getApplicationContext());
                Intent intent = new Intent(getActivity().getApplicationContext(), ListActivity.class);
                intent.putExtra(ListActivity.LIST, subscriptions);
                intent.putExtra(ListActivity.TITLE, getString(R.string.subscriptions));
                getActivity().startActivityForResult(intent, MainMenu.SUBSCRIPTIONS);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void refreshView(View view){
        makeAdapter((ExpandableListView) view.findViewById(R.id.foreignListView), R.layout.list_group_foreign, R.layout.list_item);
        FlowManager.updateForeignList(getActivity().getApplicationContext(), FlowManager.getSubscriptions(getActivity().getApplicationContext()));
    }
}