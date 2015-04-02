package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class ShowFileActivity extends ActionBarActivity {

    public static final String WORKSPACE = "workspace";
    public static final String TITLE = "title";
    public static final String TEXT = "text";

    private String workspace;
    private String title;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_file);

        //Restore data
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras(): savedInstanceState;
        workspace = bundle.getString(WORKSPACE);
        title = bundle.getString(TITLE);
        text = bundle.getString(TEXT);

        //Init
        TextView showText = (TextView) findViewById(R.id.textView);
        showText.setText(text);
        setTitle(title);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(TITLE, title);
        outState.putString(TEXT, text);
        Log.e("ShowFileActivity", "state saved: " + title);
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

    public void editButtonPressed(View view){
        Intent intent = new Intent(getApplicationContext(), CreateEditFileActivity.class);
        intent.putExtra(CreateEditFileActivity.ACTIVITY_TITLE, title);
        intent.putExtra(CreateEditFileActivity.ACTIVITY_MODE, CreateEditFileActivity.MODE.EDIT);
        intent.putExtra(CreateEditFileActivity.FILE_WORKSPACE, workspace);
        intent.putExtra(CreateEditFileActivity.FILE_TITLE, title);
        intent.putExtra(CreateEditFileActivity.FILE_CONTENT, text);
        startActivity(intent);
    }
}
