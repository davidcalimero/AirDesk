package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class ShowFileActivity extends ActionBarActivity {

    private String title;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_file);
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        text = intent.getStringExtra("text");

        TextView showText = (TextView) findViewById(R.id.textView);
        showText.setText(text);
        setTitle(title);
    }


    public void editButtonPressed(View view){
        Intent intent = new Intent(getApplicationContext(), EditFileActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("text", text);
        startActivity(intent);
    }
}
