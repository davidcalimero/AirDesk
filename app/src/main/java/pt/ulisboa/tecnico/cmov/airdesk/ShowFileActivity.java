package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class ShowFileActivity extends ActionBarActivity {

    public static final String TITLE = "title";
    public static final String TEXT = "text";

    private String title;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_file);

        if(savedInstanceState == null) {
            //Default sate
            Intent intent = getIntent();
            title = intent.getStringExtra(TITLE);
            text = intent.getStringExtra(TEXT);
        }
        else{
            //Saved state
            title = savedInstanceState.getString(TITLE);
            text = savedInstanceState.getString(TEXT);
        }

        TextView showText = (TextView) findViewById(R.id.textView);
        showText.setText(text);
        setTitle(title);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(TITLE, title);
        outState.putString(TEXT, text);
        super.onSaveInstanceState(outState);
    }

    public void editButtonPressed(View view){
        Intent intent = new Intent(getApplicationContext(), EditFileActivity.class);
        intent.putExtra(TITLE, title);
        intent.putExtra(TEXT, text);
        startActivity(intent);
    }
}
