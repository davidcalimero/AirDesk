package pt.ulisboa.tecnico.cmov.airdesk;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import pt.ulisboa.tecnico.cmov.airdesk.adapter.WorkspaceListAdapter;

public class ForeignFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_foreign, container, false);

        ExpandableListView expandableListView = (ExpandableListView) view.findViewById(R.id.foreignListView);
        WorkspaceListAdapter adapter = new WorkspaceListAdapter(getActivity());
        expandableListView.setAdapter(adapter);

        adapter.createGroup("Teste1");
        adapter.createGroup("Teste2");
        adapter.addChild("Teste2", "HelloHorld!");

        return view;
    }
}