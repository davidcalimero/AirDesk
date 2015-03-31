package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.other.User;
import pt.ulisboa.tecnico.cmov.airdesk.other.Workspace;


public class NewWorkspaceActivity extends ActionBarActivity {

    private User user;
    private ArrayList<CharSequence> tags = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_workspace);
        user = ((ApplicationContext) getApplicationContext()).getActiveUser();
    }

    public boolean isWorkspaceNameEmpty(){
        EditText workspaceName = (EditText) findViewById(R.id.workspaceName);
        return workspaceName.getText().length() == 0;
    }

    public void onCreateWorkspaceButtonPressed (View view){
        if(isWorkspaceNameEmpty())
            Toast.makeText(getApplicationContext(), "Write a name", Toast.LENGTH_SHORT).show();
        else {
            EditText workspaceName = (EditText) findViewById(R.id.workspaceName);
            String name = workspaceName.getText().toString();
            // TODO falta privacidade e cota
            Workspace workspace = new Workspace(name, user.getID(), Workspace.MODE.PUBLIC, 0);
            workspace.setPublicProfile(tags);
            user.addWorkspace(workspace);
            setResult(RESULT_OK, new Intent());
            finish();
        }
    }

    public void onAddTagButtonPressed (View view){
        if(isWorkspaceNameEmpty())
            Toast.makeText(getApplicationContext(), R.string.enter_a_valid_name, Toast.LENGTH_SHORT).show();
        else {
            EditText workspaceName = (EditText) findViewById(R.id.workspaceName);
            String name = workspaceName.getText().toString();

            Intent intent = new Intent(getApplicationContext(), TagsActivity.class);
            intent.putCharSequenceArrayListExtra(TagsActivity.TAGS, tags);
            intent.putExtra(TagsActivity.TITLE, name + " Tags");
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        tags = data.getCharSequenceArrayListExtra(TagsActivity.TAGS);
    }
}
