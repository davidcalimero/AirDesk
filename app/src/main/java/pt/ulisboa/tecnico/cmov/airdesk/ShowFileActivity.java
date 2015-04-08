package pt.ulisboa.tecnico.cmov.airdesk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.listener.WorkspacesChangeListener;
import pt.ulisboa.tecnico.cmov.airdesk.other.FlowManager;


public class ShowFileActivity extends ActionBarActivity {

    public static final String WORKSPACE = "workspace";
    public static final String TITLE = "title";
    public static final String OWNER_NAME = "ownerName";

    private String workspace;
    private String title;
    private String text;
    private String owner;

    private WorkspacesChangeListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_file);

        //Restore data
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        workspace = bundle.getString(WORKSPACE);
        title = bundle.getString(TITLE);
        owner = bundle.getString(OWNER_NAME);
        text = FlowManager.getFileContent(getApplicationContext(), owner, workspace, title);

        Log.e("ShowFileActivity", owner + " " + workspace + " " + title);

        //Init
        final TextView showText = (TextView) findViewById(R.id.textView);
        showText.setText(text);
        setTitle(title);

        listener = new WorkspacesChangeListener() {
            @Override
            public void onWorkspaceAdded(String ownerName, String name) {}

            @Override
            public void onWorkspaceAddedForeign(String owner, String name, ArrayList<String> files) {
                //TODO remove this method in version N
            }

            @Override
            public void onWorkspaceRemovedForeign(String owner, String workspaceName) {
                //TODO remove this method in version N
            }

            @Override
            public void onWorkspaceRemoved(String ownerName, String name) {
                if(owner.equals(ownerName) && workspace.equals(name))
                    finish();
            }

            @Override
            public void onFileAdded(String ownerName, String workspaceName, String fileName) {}

            @Override
            public void onFileRemoved(String ownerName, String workspaceName, String fileName) {
                if(owner.equals(ownerName) && workspace.equals(workspaceName) && text.equals(fileName))
                    finish();
            }

            @Override
            public void onFileContentChange(String ownerName, String workspaceName, String filename, String content) {
                if(owner.equals(ownerName) && workspace.equals(workspaceName) && text.equals(filename))
                    showText.setText(content);
            }
        };
        FlowManager.addWorkspacesChangeListener(listener);
    }

    @Override
    protected void onDestroy() {
        FlowManager.removeWorkspacesChangeListener(listener);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(WORKSPACE, workspace);
        outState.putString(TITLE, title);
        outState.putString(OWNER_NAME, owner);
        Log.e("ShowFileActivity", "state saved: " + title);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delete, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_delete:
                // Create the AlertDialog object
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.dialog_confirm_delete) + " \"" + title + "\"")
                        .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //Delete File
                                FlowManager.notifyRemoveFile(getApplicationContext(), owner, workspace, title);
                                Toast.makeText(getApplicationContext(), getString(R.string.file_removed_successfully), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create().show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void editButtonPressed(View view) {
        if (FlowManager.havePermissionToEditFile(getApplicationContext(), workspace, title)) {
            Intent intent = new Intent(getApplicationContext(), CreateEditFileActivity.class);
            intent.putExtra(CreateEditFileActivity.ACTIVITY_TITLE, title);
            intent.putExtra(CreateEditFileActivity.ACTIVITY_MODE, CreateEditFileActivity.MODE.EDIT);
            intent.putExtra(CreateEditFileActivity.FILE_WORKSPACE, workspace);
            intent.putExtra(CreateEditFileActivity.FILE_TITLE, title);
            intent.putExtra(CreateEditFileActivity.FILE_CONTENT, text);
            intent.putExtra(CreateEditFileActivity.OWNER_NAME, owner);
            startActivity(intent);
            finish();
        } else
            Toast.makeText(getApplicationContext(), getString(R.string.file_cant_be_edited_at_the_moment), Toast.LENGTH_SHORT).show();
    }
}
