package pt.ulisboa.tecnico.cmov.airdesk;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
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

        //Restore data
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras(): savedInstanceState;
        title = bundle.getString(ShowFileActivity.TITLE);
        text = bundle.getString(ShowFileActivity.TEXT);

        //Init
        EditText editText = (EditText) findViewById(R.id.editText);
        editText.setText(text);
        setTitle(title);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ShowFileActivity.TITLE, title);
        outState.putString(ShowFileActivity.TEXT, text);
        Log.e("EditFileActivity", "state saved: " + title);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_file, menu);
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
        ** TODO Falta guardar texto alterado
        */
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String text = editText.getText().toString();

        finish();
    }


    public void cancelButtonPressed(View view){
        finish();
    }
}
