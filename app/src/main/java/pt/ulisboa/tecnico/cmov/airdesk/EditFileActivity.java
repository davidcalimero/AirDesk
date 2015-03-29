package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


public class EditFileActivity extends ActionBarActivity {

    private String title;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_file);

        if(savedInstanceState == null) {
            //Default sate
            Intent intent = getIntent();
            title = intent.getStringExtra(ShowFileActivity.TITLE);
            text = intent.getStringExtra(ShowFileActivity.TEXT);
        }
        else{
            //Saved state
            title = savedInstanceState.getString(ShowFileActivity.TITLE);
            text = savedInstanceState.getString(ShowFileActivity.TEXT);
        }

        EditText editText = (EditText) findViewById(R.id.editText);
        editText.setText(text);
        setTitle(title);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ShowFileActivity.TITLE, title);
        outState.putString(ShowFileActivity.TEXT, text);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_file, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_delete:
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
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
