package pt.ulisboa.tecnico.cmov.airdesk;


import android.support.v4.app.Fragment;
import android.widget.ExpandableListView;

import pt.ulisboa.tecnico.cmov.airdesk.adapter.WorkspaceListAdapter;


public class ExpandableListFragment extends Fragment {

    private WorkspaceListAdapter adapter;

    protected void makeAdapter(ExpandableListView view, int groupLayout, int childLayout) {
        this.adapter = new WorkspaceListAdapter(getActivity(), groupLayout, childLayout);
        view.setAdapter(adapter);
    }

    public WorkspaceListAdapter getAdapter(){
        return adapter;
    }
}
