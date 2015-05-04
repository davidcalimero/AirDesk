package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.HashSet;

import pt.ulisboa.tecnico.cmov.airdesk.listener.SwipeDismissListViewTouchListener;
import pt.ulisboa.tecnico.cmov.airdesk.utility.Utils;


public class ListActivity extends ActionBarActivity {

    public static final String LIST = "list";
    public static final String TITLE = "title";
    public static final String MAP = "map";

    private static final String TEXT = "text";

    private String title;
    private String text;
    private HashSet<CharSequence> list;
    private HashMap<CharSequence, Boolean> map;
    private ArrayAdapter<String> adapter;

    private EditText writer;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        //Restore data
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        list = (HashSet<CharSequence>) bundle.getSerializable(LIST);
        title = bundle.getString(TITLE);
        text = bundle.getString(TEXT);
        map = (HashMap<CharSequence, Boolean>) bundle.getSerializable(MAP);
        if(map == null)
            map = new HashMap<>();

        //ListView inicialization
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView = (ListView) findViewById(R.id.tagsView);
        listView.setAdapter(adapter);

        //Swipe Listener Initialization
        SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(listView,
                new SwipeDismissListViewTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(int position) {
                        return true;
                    }

                    @Override
                    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                        for (int position : reverseSortedPositions) {
                            //Remove item
                            String item = adapter.getItem(position);
                            adapter.remove(item);
                            adapter.remove(item);
                            list.remove(item);
                            Boolean ret = map.remove(item);
                            if(ret == null)
                                map.put(item, false);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
        listView.setOnTouchListener(touchListener);
        listView.setOnScrollListener(touchListener.makeScrollListener());

        writer = (EditText) findViewById(R.id.tagWriteView);
        writer.setText(text);
        populateView();
        setTitle(title);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(MAP, map);
        outState.putSerializable(LIST, list);
        outState.putString(TITLE, title);
        outState.putString(TEXT, writer.getText().toString());
        Log.e("ListActivity", "state saved: " + title);
        super.onSaveInstanceState(outState);
    }

    public void onConfirmButtonPressed(View view) {
        Intent intent = new Intent();
        intent.putExtra(MAP, map);
        intent.putExtra(LIST, list);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onAddButtonPressed(View view) {
        String item = writer.getText().toString().trim();

        //Invalid input verification
        if (!Utils.isSingleWord(item)) {
            Toast.makeText(getApplicationContext(), R.string.invalid_input, Toast.LENGTH_SHORT).show();
            return;
        }
        if (list.contains(item)) {
            Toast.makeText(getApplicationContext(), "\"" + item + "\" " + getString(R.string.already_exists), Toast.LENGTH_SHORT).show();
            return;
        }

        //Update lists
        writer.getText().clear();
        adapter.add(item);
        list.add(item);
        Boolean ret = map.remove(item);
        if(ret == null)
            map.put(item, true);

        //Focus last last added
        listView.setSelection(listView.getAdapter().getCount() - 1);
    }

    //ListView population
    private void populateView() {
        for (CharSequence item : list) {
            adapter.add(item.toString());
        }
    }

    public void cancel(View view){
        finish();
    }
}
