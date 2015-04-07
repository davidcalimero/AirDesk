package pt.ulisboa.tecnico.cmov.airdesk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.airdesk.exception.AlreadyExistsException;
import pt.ulisboa.tecnico.cmov.airdesk.exception.OutOfMemoryException;
import pt.ulisboa.tecnico.cmov.airdesk.listener.WorkspacesChangeListener;
import pt.ulisboa.tecnico.cmov.airdesk.other.FlowManager;
import pt.ulisboa.tecnico.cmov.airdesk.other.Utils;

public class CreateEditFileActivity extends ActionBarActivity {

    public static final String ACTIVITY_TITLE = "activity_title";
    public static final String ACTIVITY_MODE = "mode";
    public static final String FILE_WORKSPACE = "workspace";
    public static final String FILE_TITLE = "title";
    public static final String FILE_CONTENT = "content";
    public static final String OWNER_NAME = "ownerName";

    private String activityTitle;
    private MODE mode;
    private String workspaceName;
    private String title;
    private String content;
    private String owner;

    private EditText titleView;
    private EditText contentView;

    private WorkspacesChangeListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_file);

        //Restore data
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        activityTitle = bundle.getString(ACTIVITY_TITLE);
        mode = (MODE) bundle.getSerializable(ACTIVITY_MODE);
        workspaceName = bundle.getString(FILE_WORKSPACE);
        title = bundle.getString(FILE_TITLE);
        content = bundle.getString(FILE_CONTENT);
        owner = bundle.getString(OWNER_NAME);

        Log.e("CreateEditFileActivity", owner + " " + workspaceName + " " + title);

        //Init variables
        titleView = (EditText) findViewById(R.id.createFileTitle);
        contentView = (EditText) findViewById(R.id.createFileContent);

        //Update Interface
        if (mode.equals(MODE.EDIT)) {
            ((ViewGroup) titleView.getParent()).removeView(titleView);
            ((Button) findViewById(R.id.createFileCreate)).setText(getString(R.string.confirm));
        }

        titleView.setText(title);
        contentView.setText(content);
        setTitle(activityTitle);

        listener =  new WorkspacesChangeListener() {
            @Override
            public void onWorkspaceAdded(String owner, String name) {}

            @Override
            public void onWorkspaceAddedForeign(String owner, String name) {
                //TODO remove this method in version N
            }

            @Override
            public void onWorkspaceRemovedForeign(String owner, String workspaceName) {}

            @Override
            public void onWorkspaceRemoved(String ownerName, String name) {
                if(owner.equals(ownerName) && workspaceName.equals(name))
                    finish();
            }

            @Override
            public void onFileAdded(String owner, String workspaceName, String fileName) {}

            @Override
            public void onFileRemoved(String ownerName, String workspace, String fileName) {
                if(mode == MODE.EDIT && owner.equals(ownerName) && workspaceName.equals(workspace) && title.equals(fileName))
                    finish();
            }

            @Override
            public void onFileContentChange(String owner, String workspaceName, String filename, String content) {}

            @Override
            public void onWorkspaceUserRemoved(String ownerName, String name) {
                if(owner.equals(ownerName) && workspaceName.equals(name))
                    finish();
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
        outState.putString(ACTIVITY_TITLE, activityTitle);
        outState.putSerializable(ACTIVITY_MODE, mode);
        outState.putString(FILE_WORKSPACE, workspaceName);
        outState.putString(FILE_TITLE, titleView.getText().toString());
        outState.putString(FILE_CONTENT, contentView.getText().toString());
        outState.putString(OWNER_NAME, owner);
        Log.e("CreateEditFileActivity", "state saved: " + title);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mode.equals(MODE.EDIT))
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
                                FlowManager.notifyRemoveFile(getApplicationContext(), owner, workspaceName, title);
                                Toast.makeText(getApplicationContext(), getString(R.string.file_removed_successfully), Toast.LENGTH_SHORT).show();
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

    public void onCreateFileButtonPressed(View view) {
        String newTitle = Utils.trim(titleView.getText().toString());
        String newContent = contentView.getText().toString();
        if(newTitle.length() == 0){
            Toast.makeText(getApplicationContext(), getString(R.string.invalid_input), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (mode.equals(MODE.CREATE)) {
                FlowManager.notifyAddFile(getApplicationContext(), owner, workspaceName, newTitle, newContent);
                Toast.makeText(getApplicationContext(), getString(R.string.file_created_successfully), Toast.LENGTH_SHORT).show();
            } else {
                FlowManager.notifyEditFile(getApplicationContext(), owner, workspaceName, newTitle, newContent);
                Toast.makeText(getApplicationContext(), getString(R.string.file_edited_successfully), Toast.LENGTH_SHORT).show();
            }
            finish();
        } catch (OutOfMemoryException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.not_enough_memory_available), Toast.LENGTH_SHORT).show();
        } catch (AlreadyExistsException e) {
            Toast.makeText(getApplicationContext(), "\"" + newTitle + "\" " + getString(R.string.already_exists), Toast.LENGTH_SHORT).show();
        }
    }

    public void onCancelCreateFileButtonPressed(View view) {
        finish();
    }

    public enum MODE {CREATE, EDIT}
}
