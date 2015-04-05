package pt.ulisboa.tecnico.cmov.airdesk.fragment;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ExpandableListView;

import pt.ulisboa.tecnico.cmov.airdesk.ShowFileActivity;
import pt.ulisboa.tecnico.cmov.airdesk.adapter.WorkspaceListAdapter;


public class ExpandableListFragment extends Fragment {

    private WorkspaceListAdapter adapter;

    protected void makeAdapter(ExpandableListView view, int groupLayout, int childLayout) {
        this.adapter = new WorkspaceListAdapter(getActivity(), groupLayout, childLayout);
        view.setAdapter(adapter);

        view.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String workspaceName = adapter.getGroup(groupPosition).toString();
                String filename = adapter.getChild(groupPosition, childPosition).toString();

                Intent intent = new Intent(getActivity().getApplicationContext(), ShowFileActivity.class);
                intent.putExtra(ShowFileActivity.WORKSPACE, workspaceName);
                intent.putExtra(ShowFileActivity.TITLE, filename);
                startActivity(intent);
                return false;
            }
        });
    }

    public WorkspaceListAdapter getAdapter() {
        return adapter;
    }
}
