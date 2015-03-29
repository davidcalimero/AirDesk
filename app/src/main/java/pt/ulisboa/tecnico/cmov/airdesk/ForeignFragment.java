package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.adapter.WorkspaceListAdapter;

public class ForeignFragment extends Fragment {

    private ArrayList<CharSequence> tags = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_foreign, container, false);

        ExpandableListView expandableListView = (ExpandableListView) view.findViewById(R.id.foreignListView);
        WorkspaceListAdapter adapter = new WorkspaceListAdapter(getActivity());
        expandableListView.setAdapter(adapter);

        setHasOptionsMenu(true);

        adapter.createGroup("Teste1");
        adapter.createGroup("Teste2");
        adapter.addChild("Teste2", "HelloHorld!");

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
            case R.id.tag_subscriptions:
                Intent intent = new Intent(getActivity().getApplicationContext(), TagsActivity.class);
                intent.putCharSequenceArrayListExtra(TagsActivity.TAGS, tags);
                intent.putExtra(TagsActivity.TITLE, getString(R.string.subscriptions));
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
        if (data == null) {
            return;
        }
        tags = data.getCharSequenceArrayListExtra(TagsActivity.TAGS);
    }
}