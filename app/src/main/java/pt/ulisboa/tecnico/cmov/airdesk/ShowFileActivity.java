package pt.ulisboa.tecnico.cmov.airdesk;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.airdesk.dto.TextFileDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.UserDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.WorkspaceDto;
import pt.ulisboa.tecnico.cmov.airdesk.listener.WorkspacesChangeListener;
import pt.ulisboa.tecnico.cmov.airdesk.listener.ConnectionHandler;
import pt.ulisboa.tecnico.cmov.airdesk.utility.FlowManager;
import pt.ulisboa.tecnico.cmov.airdesk.utility.FlowProxy;


public class ShowFileActivity extends AppCompatActivity {

    public static final String FILE_DTO = "file_dto";

    private TextFileDto dto;

    private WorkspacesChangeListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_file);

        //Restore data
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        dto = (TextFileDto) bundle.getSerializable(FILE_DTO);

        Log.e("ShowFileActivity", dto.toString());

        //Init
        final TextView showText = (TextView) findViewById(R.id.textView);
        setTitle(dto.title);

        //Get File content
        final ProgressDialog dialog;
        dialog = new ProgressDialog(this);
        dialog.setTitle(getString(R.string.dialog_please_wait));
        dialog.setCancelable(false);
        dialog.setIndeterminate(false);
        dialog.setMessage(getString(R.string.dialog_loading_file));
        dialog.show();
        FlowProxy.getInstance().send_getFileContent(getApplicationContext(), dto, new ConnectionHandler<TextFileDto>() {
            @Override
            public void onSuccess(TextFileDto result) {
                dto.content = result.content;
                showText.setText(result.content);
                dialog.dismiss();
            }

            @Override
            public void onFailure() {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), getString(R.string.error_connection_lost_try_again_later), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        listener = new WorkspacesChangeListener() {
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
                if(dto.owner.equals(textFileDto.owner) && dto.workspace.equals(textFileDto.workspace) && dto.title.equals(textFileDto.title))
                    finish();
            }

            @Override
            public void onFileContentChange(TextFileDto textFileDto) {
                if(dto.owner.equals(textFileDto.owner) && dto.workspace.equals(textFileDto.workspace) && dto.title.equals(textFileDto.title))
                    showText.setText(textFileDto.content);
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
        outState.putSerializable(FILE_DTO, dto);
        Log.e("ShowFileActivity", "state saved: " + dto.title);
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
                            public void onClick(DialogInterface dialog, int which) {}
                        }).create().show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void editButtonPressed(View view) {
        UserDto userDto = new UserDto();
        userDto.id = FlowManager.getActiveUserID(getApplicationContext());
        FlowProxy.getInstance().send_askToEditFile(getApplicationContext(), userDto, dto, new ConnectionHandler<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if(result){
                    Intent intent = new Intent(getApplicationContext(), CreateEditFileActivity.class);
                    intent.putExtra(CreateEditFileActivity.ACTIVITY_TITLE, dto.title);
                    intent.putExtra(CreateEditFileActivity.ACTIVITY_MODE, CreateEditFileActivity.MODE.EDIT);
                    intent.putExtra(CreateEditFileActivity.FILE_DTO, dto);
                    startActivity(intent);
                    finish();
                }
                else
                    Toast.makeText(getApplicationContext(), getString(R.string.file_cant_be_edited_at_the_moment), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure() {
                Toast.makeText(getApplicationContext(), getString(R.string.error_connection_lost_try_again_later), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
