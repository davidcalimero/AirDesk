package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;


public class EditFileActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_file);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");

        EditText editText = (EditText) findViewById(R.id.editText);
        editText.setText(text);
        setTitle(title);
    }


    public void doneButtonPressed(View view){
        /*
        ** Falta guardar texto alterado
        */
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String text = editText.getText().toString();

        finish();
    }


    public void cancelButtonPressed(View view){
        finish();
    }
}
