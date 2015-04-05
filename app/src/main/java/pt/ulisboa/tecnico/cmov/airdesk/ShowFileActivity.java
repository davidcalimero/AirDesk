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

    private String workspace;
    private String title;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_file);

        //Restore data
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        workspace = bundle.getString(WORKSPACE);
        title = bundle.getString(TITLE);
        text = FlowManager.getFileContent(getApplicationContext(), workspace, title);

        //Init
        final TextView showText = (TextView) findViewById(R.id.textView);
        showText.setText(text);
        setTitle(title);

        FlowManager.addWorkspacesChangeListener( new WorkspacesChangeListener() {
            @Override
            public void onWorkspaceCreated(String name) {}

            @Override
            public void onWorkspaceRemoved(String name) {
                if(workspace.equals(name))
                    finish();
            }

            @Override
            public void onFileCreated(String workspaceName, String fileName) {}

            @Override
            public void onFileRemoved(String workspaceName, String fileName) {
                if(workspace.equals(workspaceName) && text.equals(fileName))
                    finish();
            }

            @Override
            public void onWorkspaceEdited(String workspaceName, boolean isPrivate, ArrayList<CharSequence> users, ArrayList<CharSequence> tags) {
                //TODO finish activity if needed
            }

            @Override
            public void onSubscriptionsChange(ArrayList<CharSequence> subscriptions) {}

            @Override
            public void onFileContentChange(String workspaceName, String filename, String content) {
                if(workspace.equals(workspaceName) && text.equals(filename))
                    showText.setText(content);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(WORKSPACE, workspace);
        outState.putString(TITLE, title);
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
                                FlowManager.notifyRemoveFile(getApplicationContext(), workspace, title);
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
        if (FlowManager.canPermissionToEditFile(getApplicationContext(), workspace, title)) {
            Intent intent = new Intent(getApplicationContext(), CreateEditFileActivity.class);
            intent.putExtra(CreateEditFileActivity.ACTIVITY_TITLE, title);
            intent.putExtra(CreateEditFileActivity.ACTIVITY_MODE, CreateEditFileActivity.MODE.EDIT);
            intent.putExtra(CreateEditFileActivity.FILE_WORKSPACE, workspace);
            intent.putExtra(CreateEditFileActivity.FILE_TITLE, title);
            intent.putExtra(CreateEditFileActivity.FILE_CONTENT, text);
            startActivity(intent);
            finish();
        } else
            Toast.makeText(getApplicationContext(), getString(R.string.file_cant_be_edited_at_the_moment), Toast.LENGTH_SHORT).show();
    }
}
