package pt.ulisboa.tecnico.cmov.airdesk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.airdesk.dto.TextFileDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.UserDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.WorkspaceDto;
import pt.ulisboa.tecnico.cmov.airdesk.listener.WorkspacesChangeListener;
import pt.ulisboa.tecnico.cmov.airdesk.listener.ConnectionHandler;
import pt.ulisboa.tecnico.cmov.airdesk.utility.FlowManager;
import pt.ulisboa.tecnico.cmov.airdesk.utility.FlowProxy;
import pt.ulisboa.tecnico.cmov.airdesk.utility.Utils;

public class CreateEditFileActivity extends AppCompatActivity {

    public enum MODE {CREATE, EDIT}

    public static final String ACTIVITY_TITLE = "activity_title";
    public static final String ACTIVITY_MODE = "mode";
    public static final String FILE_DTO = "file_dto";

    private String activityTitle;
    private MODE mode;
    private TextFileDto dto;

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
        dto = (TextFileDto) bundle.getSerializable(FILE_DTO);

        Log.e("CreateEditFileActivity", dto.toString());

        //Init variables
        titleView = (EditText) findViewById(R.id.createFileTitle);
        contentView = (EditText) findViewById(R.id.createFileContent);

        //Update Interface
        if (mode.equals(MODE.EDIT)) {
            titleView.setVisibility(View.GONE);
            ((Button) findViewById(R.id.createFileCreate)).setText(getString(R.string.confirm));
        }

        titleView.setText(dto.title);
        contentView.setText(dto.content);
        setTitle(activityTitle);

        listener =  new WorkspacesChangeListener() {
            @Override
            public void onUserLeft(UserDto userDto) {
                if(dto.owner.equals(userDto.id))
                    finish();
            }

            @Override
            public void onWorkspaceAdded(WorkspaceDto workspaceDto) {}

            @Override
            public void onWorkspaceRemoved(WorkspaceDto workspaceDto) {
                if(dto.owner.equals(workspaceDto.owner) && dto.workspace.equals(workspaceDto.name))
                    finish();
            }

            @Override
            public void onFileAdded(TextFileDto textFileDto) {}

            @Override
            public void onFileRemoved(TextFileDto textFileDto) {
                if(mode == MODE.EDIT && dto.owner.equals(textFileDto.owner) &&
                        dto.workspace.equals(textFileDto.workspace) && dto.title.equals(textFileDto.title))
                    finish();
            }

            @Override
            public void onFileContentChange(TextFileDto textFileDto) {}
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
        dto.title = titleView.getText().toString();
        dto.content = contentView.getText().toString();

        outState.putString(ACTIVITY_TITLE, activityTitle);
        outState.putSerializable(ACTIVITY_MODE, mode);
        outState.putSerializable(FILE_DTO, dto);
        Log.e("CreateEditFileActivity", "state saved: " + titleView.getText().toString());
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
                        .setMessage(getString(R.string.dialog_confirm_delete) + " \"" + dto.title + "\"")
                        .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //Delete File
                                FlowProxy.getInstance().send_removeFile(getApplicationContext(), dto.owner, dto, new ConnectionHandler<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.file_removed_successfully), Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                    @Override
                                    public void onFailure() {
                                        Toast.makeText(getApplicationContext(), getString(R.string.error_connection_lost_try_again_later), Toast.LENGTH_SHORT).show();
                                    }
                                });
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
        dto.title = Utils.trim(titleView.getText().toString());
        dto.content = contentView.getText().toString();
        if(dto.title.length() == 0){
            Toast.makeText(getApplicationContext(), getString(R.string.invalid_input), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mode.equals(MODE.CREATE)) {
            FlowProxy.getInstance().send_addFile(getApplication(), dto.owner, dto, new ConnectionHandler<Exception>() {
                @Override
                public void onSuccess(Exception result) {
                    if(result != null) {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_could_not_apply_changes), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(getApplicationContext(), getString(R.string.file_created_successfully), Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure() {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_could_not_apply_changes), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            FlowProxy.getInstance().send_editFile(getApplicationContext(), dto.owner, dto, new ConnectionHandler() {
                @Override
                public void onSuccess(Object result) {
                    if(result != null) {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_could_not_apply_changes), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(getApplicationContext(), getString(R.string.file_edited_successfully), Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure() {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_could_not_apply_changes), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void onCancelCreateFileButtonPressed(View view) {
        finish();
    }
}
