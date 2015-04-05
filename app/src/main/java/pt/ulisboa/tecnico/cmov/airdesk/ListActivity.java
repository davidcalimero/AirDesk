package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.other.Utils;


public class ListActivity extends ActionBarActivity {

    //TODO remake to add and remove methods and verify input in flowmanager

    public static final String LIST = "list";
    public static final String TITLE = "title";

    private static final String TEXT = "text";

    private String title;
    private String text;
    private ArrayList<CharSequence> list;
    private ArrayAdapter<String> adapter;

    private EditText writer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        //Restore data
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        list = bundle.getCharSequenceArrayList(LIST);
        title = bundle.getString(TITLE);
        text = bundle.getString(TEXT);

        //ListView inicialization
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        ListView listView = (ListView) findViewById(R.id.tagsView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                adapter.remove(list.get(position).toString());
                list.remove(position);
            }
        });

        writer = (EditText) findViewById(R.id.tagWriteView);
        writer.setText(text);

        populateView();
        setTitle(title);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putCharSequenceArrayList(LIST, list);
        outState.putString(TITLE, title);
        outState.putString(TEXT, writer.getText().toString());
        Log.e("ListActivity", "state saved: " + title);
        super.onSaveInstanceState(outState);
    }

    public void onConfirmButtonPressed(View view) {
        Intent intent = new Intent();
        intent.putCharSequenceArrayListExtra(LIST, list);
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

        writer.getText().clear();
        adapter.add(item);
        list.add(item);
    }

    //ListView population
    private void populateView() {
        for (CharSequence item : list) {
            adapter.add(item.toString());
        }
    }
}
