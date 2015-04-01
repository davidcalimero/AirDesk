package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import pt.ulisboa.tecnico.cmov.airdesk.other.User;

public class ForeignFragment extends ExpandableListFragment {

    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_foreign, container, false);
        makeAdapter((ExpandableListView) view.findViewById(R.id.foreignListView), R.layout.list_group_foreign, R.layout.list_item);
        user = ((ApplicationContext) getActivity().getApplicationContext()).getActiveUser();

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
                Intent intent = new Intent(getActivity().getApplicationContext(), TagsActivity.class);
                intent.putCharSequenceArrayListExtra(TagsActivity.TAGS, user.getSubscriptions());
                intent.putExtra(TagsActivity.TITLE, getString(R.string.subscriptions));
                getActivity().startActivityForResult(intent, MainMenu.FOREIGN);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
}