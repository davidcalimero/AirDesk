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

import pt.ulisboa.tecnico.cmov.airdesk.ListActivity;
import pt.ulisboa.tecnico.cmov.airdesk.MainMenu;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.listener.WorkspacesChangeListener;
import pt.ulisboa.tecnico.cmov.airdesk.other.FlowManager;

public class ForeignFragment extends ExpandableListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_foreign, container, false);
        makeAdapter((ExpandableListView) view.findViewById(R.id.foreignListView), R.layout.list_group_foreign, R.layout.list_item);
        setHasOptionsMenu(true);

        setListener(new WorkspacesChangeListener() {
            @Override
            public void onWorkspaceAdded(String owner, String name) {}

            @Override
            public void onWorkspaceRemoved(String owner, String name) {}

            @Override
            public void onFileAdded(String owner, String workspaceName, String fileName) {}

            @Override
            public void onFileRemoved(String owner, String workspaceName, String fileName) {}

            @Override
            public void onWorkspaceEdited(String workspaceName, boolean isPrivate, ArrayList<CharSequence> users, ArrayList<CharSequence> tags) {}

            @Override
            public void onSubscriptionsChange(ArrayList<CharSequence> subscriptions) {}

            @Override
            public void onFileContentChange(String owner, String workspaceName, String filename, String content) {}
        });

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
            case R.id.action_subscriptions:
                ArrayList<CharSequence> subscriptions = FlowManager.getSubscriptions(getActivity().getApplicationContext());
                Intent intent = new Intent(getActivity().getApplicationContext(), ListActivity.class);
                intent.putCharSequenceArrayListExtra(ListActivity.LIST, subscriptions);
                intent.putExtra(ListActivity.TITLE, getString(R.string.subscriptions));
                getActivity().startActivityForResult(intent, MainMenu.SUBSCRIPTIONS);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}