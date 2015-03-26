package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class TagsActivity extends ActionBarActivity {

    public static final String TAGS = "tags";
    public static final String TITLE = "title";

    private String title;
    private ArrayList<CharSequence> tags;
    private ArrayAdapter<String> adapter;
    private EditText tagWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);

        if (savedInstanceState == null) {
            //Default sate
            Intent intent = getIntent();
            tags = intent.getCharSequenceArrayListExtra(TAGS);
            title = intent.getStringExtra(TITLE);
        } else {
            //Saved state
            tags = savedInstanceState.getCharSequenceArrayList(TAGS);
            title = savedInstanceState.getString(TITLE);
        }

        tagWriter = (EditText) findViewById(R.id.tagWriteView);

        //ListView inicialization
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        ListView listView = (ListView) findViewById(R.id.tagsView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                adapter.remove(tags.get(position).toString());
                tags.remove(position);
            }
        });

        //ListView population
        for(CharSequence tag : tags){
            adapter.add(tag.toString());
        }

        setTitle(title);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putCharSequenceArrayList(TAGS, tags);
        outState.putString(TITLE, title);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        sendData();
        super.onBackPressed();
    }

    public void done(View view){
        sendData();
        finish();
    }

    public void sendData(){
        Intent intent = new Intent();
        intent.putCharSequenceArrayListExtra(TAGS, tags);
        setResult(RESULT_OK, intent);
    }

    public void addTag(View view){
        String tag = tagWriter.getText().toString();

        //Invalid input verification
        if(tag.equals("")){
            Toast.makeText(getApplicationContext(), R.string.invalid_input, Toast.LENGTH_SHORT).show();
            return;
        }

        tagWriter.getText().clear();
        adapter.add(tag);
        tags.add(tag);


    }
}
