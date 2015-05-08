package pt.ulisboa.tecnico.cmov.airdesk;

import android.app.AlertDialog;
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

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.dto.TextFileDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.WorkspaceDto;
import pt.ulisboa.tecnico.cmov.airdesk.listener.WorkspacesChangeListener;
import pt.ulisboa.tecnico.cmov.airdesk.utility.FlowManager;
import pt.ulisboa.tecnico.cmov.airdesk.utility.ThreadHandler;


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
        ThreadHandler.startWorkerThread(getString(R.string.dialog_loading_file), new ThreadHandler<String>(this) {
            @Override
            public String start() {
                return FlowManager.send_getFileContent(getApplicationContext(), dto);
            }

            @Override
            public void onFinish(String result) {
                dto.content = result;
                showText.setText(result);
            }
        });

        listener = new WorkspacesChangeListener() {
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
                                FlowManager.notifyRemoveFile(getApplicationContext(), dto);
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
        if (FlowManager.send_askToEditFile(getApplicationContext(), dto)) {
            Intent intent = new Intent(getApplicationContext(), CreateEditFileActivity.class);
            intent.putExtra(CreateEditFileActivity.ACTIVITY_TITLE, dto.title);
            intent.putExtra(CreateEditFileActivity.ACTIVITY_MODE, CreateEditFileActivity.MODE.EDIT);
            intent.putExtra(CreateEditFileActivity.FILE_DTO, dto);
            startActivity(intent);
            finish();
        } else
            Toast.makeText(getApplicationContext(), getString(R.string.file_cant_be_edited_at_the_moment), Toast.LENGTH_SHORT).show();
    }
}
