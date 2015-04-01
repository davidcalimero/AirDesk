package pt.ulisboa.tecnico.cmov.airdesk;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ExpandableListView;

import pt.ulisboa.tecnico.cmov.airdesk.adapter.WorkspaceListAdapter;
import pt.ulisboa.tecnico.cmov.airdesk.other.TextFile;
import pt.ulisboa.tecnico.cmov.airdesk.other.User;


public class ExpandableListFragment extends Fragment {

    private WorkspaceListAdapter adapter;

    protected void makeAdapter(ExpandableListView view, int groupLayout, int childLayout) {
        this.adapter = new WorkspaceListAdapter(getActivity(), groupLayout, childLayout);
        view.setAdapter(adapter);

        view.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Intent intent = new Intent(getActivity().getApplicationContext(), ShowFileActivity.class);
                String workspaceName = adapter.getGroup(groupPosition).toString();
                String filename = adapter.getChild(groupPosition, childPosition).toString();

                User user = ((ApplicationContext) (getActivity().getApplicationContext())).getActiveUser();
                TextFile file = user.getWorkspaceList().get(workspaceName).getFiles().get(filename);
                intent.putExtra(ShowFileActivity.TITLE, file.getTitle());
                intent.putExtra(ShowFileActivity.TEXT, file.getContent());
                startActivity(intent);
                return false;
            }
        });
    }

    public WorkspaceListAdapter getAdapter(){
        return adapter;
    }
}
