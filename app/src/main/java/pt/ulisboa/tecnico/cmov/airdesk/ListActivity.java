package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class ListActivity extends ActionBarActivity {

    public static final String LIST = "list";
    public static final String TITLE = "title";

    private String title;
    private ArrayList<CharSequence> list;
    private ArrayAdapter<String> adapter;
    private EditText tagWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        //Restore data
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras(): savedInstanceState;
        list = bundle.getCharSequenceArrayList(LIST);
        title = bundle.getString(TITLE);

        tagWriter = (EditText) findViewById(R.id.tagWriteView);

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

        populateView();
        setTitle(title);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putCharSequenceArrayList(LIST, list);
        outState.putString(TITLE, title);
        Log.e("ListActivity", "state saved: " + title);
        super.onSaveInstanceState(outState);
    }

    public void done(View view){
        sendData();
        finish();
    }

    public void sendData(){
        Intent intent = new Intent();
        intent.putCharSequenceArrayListExtra(LIST, list);
        setResult(RESULT_OK, intent);
    }

    public void addItem(View view) {
        String item = tagWriter.getText().toString().trim();

        //Invalid input verification
        if (item.split(" ").length != 1 || item.length() == 0) {
            Toast.makeText(getApplicationContext(), R.string.invalid_input, Toast.LENGTH_SHORT).show();
            return;
        }
        if (list.contains(item)){
            Toast.makeText(getApplicationContext(), "\"" + item + "\" " + getString(R.string.already_exists), Toast.LENGTH_SHORT).show();
            return;
        }

        tagWriter.getText().clear();
        adapter.add(item);
        list.add(item);
    }

    //ListView population
    private void populateView(){
        for(CharSequence item : list){
            adapter.add(item.toString());
        }
    }
}
