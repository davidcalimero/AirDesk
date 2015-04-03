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

import pt.ulisboa.tecnico.cmov.airdesk.ApplicationContext;
import pt.ulisboa.tecnico.cmov.airdesk.ListActivity;
import pt.ulisboa.tecnico.cmov.airdesk.MainMenu;
import pt.ulisboa.tecnico.cmov.airdesk.R;

public class ForeignFragment extends ExpandableListFragment {

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
        View view = inflater.inflate(R.layout.fragment_foreign, container, false);
        makeAdapter((ExpandableListView) view.findViewById(R.id.foreignListView), R.layout.list_group_foreign, R.layout.list_item);

        setHasOptionsMenu(true);
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
                ArrayList<CharSequence> tags = ((ApplicationContext) getActivity().getApplicationContext()).getActiveUser().getSubscriptions();
                Intent intent = new Intent(getActivity().getApplicationContext(), ListActivity.class);
                intent.putCharSequenceArrayListExtra(ListActivity.LIST, tags);
                intent.putExtra(ListActivity.TITLE, getString(R.string.subscriptions));
                getActivity().startActivityForResult(intent, MainMenu.FOREIGN);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
}