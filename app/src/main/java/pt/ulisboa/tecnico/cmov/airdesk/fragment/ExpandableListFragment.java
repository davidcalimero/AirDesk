package pt.ulisboa.tecnico.cmov.airdesk.fragment;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ExpandableListView;

import pt.ulisboa.tecnico.cmov.airdesk.ShowFileActivity;
import pt.ulisboa.tecnico.cmov.airdesk.adapter.WorkspaceListAdapter;
import pt.ulisboa.tecnico.cmov.airdesk.listener.WorkspacesChangeListener;
import pt.ulisboa.tecnico.cmov.airdesk.other.FlowManager;


public class ExpandableListFragment extends Fragment {

    private WorkspaceListAdapter adapter;
    private WorkspacesChangeListener listener = null;
    private boolean wasChanged = false;

    protected void makeAdapter(ExpandableListView view, int groupLayout, int childLayout) {
        this.adapter = new WorkspaceListAdapter(getActivity(), groupLayout, childLayout);
        view.setAdapter(adapter);

        view.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String workspaceName = adapter.getGroup(groupPosition).toString();
                String filename = adapter.getChild(groupPosition, childPosition).toString();
                String owner = adapter.getTag(groupPosition).toString();

                Intent intent = new Intent(getActivity().getApplicationContext(), ShowFileActivity.class);
                intent.putExtra(ShowFileActivity.WORKSPACE, workspaceName);
                intent.putExtra(ShowFileActivity.TITLE, filename);
                intent.putExtra(ShowFileActivity.OWNER_NAME, owner);
                startActivity(intent);
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(wasChanged) {
            adapter.notifyDataSetChanged();
            wasChanged = false;
        }
    }

    @Override
    public void onDestroy() {
        if(listener != null)
            FlowManager.removeWorkspacesChangeListener(listener);
        super.onDestroy();
    }

    protected void setListener(WorkspacesChangeListener listener){
        if(listener != null)
            FlowManager.removeWorkspacesChangeListener(listener);
        this.listener = listener;
        FlowManager.addWorkspacesChangeListener(listener);
    }

    //Should be only called if you are shore the adapter was changed
    private void updateAdapter(){
        if(isVisible())
            adapter.notifyDataSetChanged();
        else
            wasChanged = true;
    }

    protected void addWorkspace(String owner, String workspaceName){
        adapter.addGroup(owner, workspaceName);
        updateAdapter();
    }

    protected void removeWorkspace(String owner, String workspaceName){
        adapter.removeGroup(owner, workspaceName);
        updateAdapter();
    }

    protected void addFile(String owner, String workspaceName, String fileName){
        adapter.addChild(owner, workspaceName, fileName);
        updateAdapter();
    }

    protected void removeFile(String owner, String workspaceName, String fileName){
        adapter.removeChild(owner, workspaceName, fileName);
        updateAdapter();
    }
}
