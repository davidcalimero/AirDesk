package pt.ulisboa.tecnico.cmov.airdesk;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;


public class CreateFileActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_file);
    }

    /*public void createFile(){
        String title = findViewById(R.id);
        String content = findViewById(R.id);
        Workspace workspace = ((ApplicationContext) getApplicationContext()).getActiveUser().getWorkspaceByID();
        workspace.addFile(new TextFile(getApplicationContext(), workspace))
    }*/
}
