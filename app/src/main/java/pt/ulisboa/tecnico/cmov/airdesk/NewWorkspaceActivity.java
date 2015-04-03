package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.other.FlowManager;
import pt.ulisboa.tecnico.cmov.airdesk.other.Utils;
import pt.ulisboa.tecnico.cmov.airdesk.other.Workspace;


public class NewWorkspaceActivity extends ActionBarActivity {

    private ArrayList<CharSequence> tags = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_workspace);
    }

    public boolean isWorkspaceNameEmpty(){
        EditText workspaceName = (EditText) findViewById(R.id.workspaceName);
        return Utils.trim(workspaceName.getText().toString()).length() == 0;
    }

    public void onCreateWorkspaceButtonPressed (View view){
        if(isWorkspaceNameEmpty())
            Toast.makeText(getApplicationContext(), getString(R.string.invalid_input), Toast.LENGTH_SHORT).show();
        else {
            EditText workspaceName = (EditText) findViewById(R.id.workspaceName);
            String name = Utils.trim(workspaceName.getText().toString());
            // TODO falta privacidade e cota
            FlowManager.notifyAddWorkspace(getApplicationContext(), name, Workspace.MODE.PUBLIC, tags, 0);
            Toast.makeText(getApplicationContext(), getString(R.string.workspace_created_successfully), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void onAddTagButtonPressed (View view){
        if(isWorkspaceNameEmpty())
            Toast.makeText(getApplicationContext(), R.string.invalid_input, Toast.LENGTH_SHORT).show();
        else {
            EditText workspaceName = (EditText) findViewById(R.id.workspaceName);
            String name = workspaceName.getText().toString();

            Intent intent = new Intent(getApplicationContext(), ListActivity.class);
            intent.putCharSequenceArrayListExtra(ListActivity.LIST, tags);
            intent.putExtra(ListActivity.TITLE, getString(R.string.tags_of) + " \"" + name + "\"");
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        tags = data.getCharSequenceArrayListExtra(ListActivity.LIST);
    }
}
