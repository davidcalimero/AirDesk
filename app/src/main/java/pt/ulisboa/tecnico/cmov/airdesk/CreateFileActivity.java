package pt.ulisboa.tecnico.cmov.airdesk;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import pt.ulisboa.tecnico.cmov.airdesk.other.TextFile;
import pt.ulisboa.tecnico.cmov.airdesk.other.User;


public class CreateFileActivity extends ActionBarActivity {

    public static final String WORKSPACE = "workspace";
    private String workspaceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_file);

        //Restore data
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras(): savedInstanceState;
        workspaceName = bundle.getString(WORKSPACE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(WORKSPACE, workspaceName);
        Log.e("CreateFileActivity", "state saved: " + workspaceName);
        super.onSaveInstanceState(outState);
    }

    public void createFile(View view){
        String title = ((EditText) findViewById(R.id.createFileTitle)).getText().toString();
        String content = ((EditText) findViewById(R.id.createFileContent)).getText().toString();

        User user = ((ApplicationContext) getApplicationContext()).getActiveUser();
        user.addFile(workspaceName, new TextFile(getApplicationContext(), user.getID(), title, content));
        finish();
    }

    public void cancelCreateFile(View view){
        finish();
    }
}
