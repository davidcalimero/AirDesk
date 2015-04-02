package pt.ulisboa.tecnico.cmov.airdesk;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.airdesk.other.FlowManager;


public class CreateEditFileActivity extends ActionBarActivity {

    public enum MODE {CREATE, EDIT}

    public static final String ACTIVITY_TITLE = "activity_title";
    public static final String ACTIVITY_MODE = "mode";
    public static final String FILE_WORKSPACE = "workspace";
    public static final String FILE_TITLE = "title";
    public static final String FILE_CONTENT = "content";

    private String activityTitle;
    private MODE mode;
    private String workspaceName;
    private String title;
    private String content;

    private EditText titleView;
    private EditText contentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_file);

        //Restore data
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras(): savedInstanceState;
        activityTitle = bundle.getString(ACTIVITY_TITLE);
        mode = (MODE) bundle.getSerializable(ACTIVITY_MODE);
        workspaceName = bundle.getString(FILE_WORKSPACE);
        title = bundle.getString(FILE_TITLE);
        content = bundle.getString(FILE_CONTENT);

        titleView = (EditText) findViewById(R.id.createFileTitle);
        contentView = (EditText) findViewById(R.id.createFileContent);

        //Update Interface
        if(mode.equals(MODE.EDIT)) {
            titleView.setEnabled(false);
            ((Button) findViewById(R.id.createFileCreate)).setText(getString(R.string.done));
        }
        titleView.setText(title);
        contentView.setText(content);
        setTitle(activityTitle);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ACTIVITY_TITLE, activityTitle);
        outState.putSerializable(ACTIVITY_MODE, mode);
        outState.putString(FILE_WORKSPACE, workspaceName);
        outState.putString(FILE_TITLE, title);
        outState.putString(FILE_CONTENT, content);
        Log.e("CreateEditFileActivity", "state saved: " + title);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mode.equals(MODE.EDIT))
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

    public void createFile(View view){
        String newTitle = titleView.getText().toString();
        String newContent = ((EditText) findViewById(R.id.createFileContent)).getText().toString();

        if(newTitle.equals("")){
            Toast.makeText(getApplicationContext(), getString(R.string.invalid_input), Toast.LENGTH_SHORT).show();
            return;
        }

        if(mode.equals(MODE.CREATE))
            FlowManager.notifyAddFile(getApplicationContext(), workspaceName, newTitle, newContent);
        else
            FlowManager.notifyEditFile(getApplicationContext(), workspaceName, newTitle, newContent);
        finish();
    }

    public void cancelCreateFile(View view){
        finish();
    }
}
