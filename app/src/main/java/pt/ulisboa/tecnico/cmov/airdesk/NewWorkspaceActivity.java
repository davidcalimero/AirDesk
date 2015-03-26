package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;


public class NewWorkspaceActivity extends ActionBarActivity {

    private ArrayList<CharSequence> tags = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_workspace);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_workspace, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isWorkspaceNameEmpty(){
        EditText workspaceName = (EditText) findViewById(R.id.workspaceName);

        if(workspaceName.getText().length() == 0)
            return true;
        else
            return false;

    }

    public void onCreateWorkspaceButtonPressed (View view){
        if(isWorkspaceNameEmpty())
            Toast.makeText(getApplicationContext(), "Write a name", Toast.LENGTH_SHORT).show();
        else {
            EditText workspaceName = (EditText) findViewById(R.id.workspaceName);
            String name = workspaceName.getText().toString();
            // criar workspace
        }

    }


    public void onAddTagButtonPressed (View view){
        if(isWorkspaceNameEmpty())
            Toast.makeText(getApplicationContext(), "Write a name", Toast.LENGTH_SHORT).show();
        else {
            EditText workspaceName = (EditText) findViewById(R.id.workspaceName);
            String name = workspaceName.getText().toString();


            Intent intent = new Intent(getApplicationContext(), TagsActivity.class);
            intent.putCharSequenceArrayListExtra(TagsActivity.TAGS, tags);
            intent.putExtra(TagsActivity.TITLE, name+" Tags");
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
