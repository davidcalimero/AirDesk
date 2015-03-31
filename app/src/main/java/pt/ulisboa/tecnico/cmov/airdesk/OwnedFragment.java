package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import pt.ulisboa.tecnico.cmov.airdesk.adapter.WorkspaceListAdapter;
import pt.ulisboa.tecnico.cmov.airdesk.other.User;
import pt.ulisboa.tecnico.cmov.airdesk.other.Workspace;

public class OwnedFragment extends Fragment {

    private  WorkspaceListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_owned, container, false);

        ExpandableListView expandableListView = (ExpandableListView) view.findViewById(R.id.ownedListView);
        adapter = new WorkspaceListAdapter(getActivity());
        expandableListView.setAdapter(adapter);
        setHasOptionsMenu(true);

        populateListView();
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
                startActivityForResult(intent, 1);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String name = data.getStringExtra(NewWorkspaceActivity.WORKSPACE);
            if (name != null && !name.equals("")) {
                adapter.createGroup(name);
                adapter.notifyDataSetChanged();
                Log.e("OwnedFragment", "workspace added: " + name);
            }
        }
    }

    //Adds workspace views to the list view
    private void populateListView(){
        User user = ((ApplicationContext) getActivity().getApplicationContext()).getActiveUser();
        for(Workspace w : user.getOwnedWorkspaceList()){
            adapter.createGroup(w.getName());
            Log.e("OwnedFragment", "workspace added: " + w.getName());
            //TODO check for files
        }
        adapter.notifyDataSetChanged();
    }
}