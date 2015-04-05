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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.exception.AlreadyExistsException;
import pt.ulisboa.tecnico.cmov.airdesk.other.FlowManager;
import pt.ulisboa.tecnico.cmov.airdesk.other.Utils;

public class CreateEditWorkspaceActivity extends ActionBarActivity {

    public static final String WORKSPACE_NAME = "workspaceName";
    public static final String ACTIVITY_MODE = "mode";
    public static final String ACTIVITY_TITLE = "tilte";
    public static final int USERS = 1;
    public static final int TAGS = 2;
    private static final String TAGS_LIST = "tagsList";
    private static final String USERS_LIST = "usersList";
    private static final String MAX_QUOTA = "maxQuota";
    private static final String PRIVACY = "privacy";
    private String workspaceName;
    private String title;
    private MODE mode;
    private ArrayList<CharSequence> tags;
    private ArrayList<CharSequence> users;
    private boolean isPrivate;
    private long quota;
    private long freeMemory;
    private long minQuota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_workspace);

        //Restore data
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        workspaceName = bundle.getString(WORKSPACE_NAME);
        mode = (MODE) bundle.getSerializable(ACTIVITY_MODE);
        title = bundle.getString(ACTIVITY_TITLE);
        if (savedInstanceState != null) {
            users = savedInstanceState.getCharSequenceArrayList(USERS_LIST);
            tags = savedInstanceState.getCharSequenceArrayList(TAGS_LIST);
            quota = savedInstanceState.getLong(MAX_QUOTA);
            isPrivate = savedInstanceState.getBoolean(PRIVACY);
        } else if (mode == MODE.EDIT) {
            users = FlowManager.getWorkspaceUserList(getApplicationContext(), workspaceName);
            tags = FlowManager.getWorkspaceTagList(getApplicationContext(), workspaceName);
            quota = FlowManager.getWorkspaceMaxQuota(getApplicationContext(), workspaceName);
            isPrivate = FlowManager.isWorkspacePrivate(getApplicationContext(), workspaceName);
        } else {
            tags = new ArrayList<>();
            users = new ArrayList<>();
            quota = 0;
            isPrivate = false;
        }

        if (mode == MODE.EDIT)
            minQuota = FlowManager.getWorkspaceQuota(getApplicationContext(), workspaceName);
        else
            minQuota = 0;

        freeMemory = FlowManager.getUserFreeMemorySpace();

        //////////////////////////////////////////////
        // Privacy
        RadioButton publicButton = (RadioButton) findViewById(R.id.publicButton);
        RadioButton privateButton = (RadioButton) findViewById(R.id.privateButton);
        if (isPrivate) {
            publicButton.setChecked(false);
            privateButton.setChecked(true);
        } else {
            publicButton.setChecked(true);
            privateButton.setChecked(false);
        }

        //////////////////////////////////////////////
        // Quota
        SeekBar quotaSeekBar = (SeekBar) findViewById(R.id.seekBar);
        final TextView quotaText = (TextView) findViewById(R.id.quotaValue);

        quotaSeekBar.setProgress((int) ((quota - minQuota) * quotaSeekBar.getMax() / (freeMemory - minQuota)));
        quotaText.setText(new DecimalFormat("#.######").format(quota / (float) 1048576) + " MB");

        quotaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                quota = minQuota + (progress * (freeMemory - minQuota) / seekBar.getMax());
                quotaText.setText(new DecimalFormat("#.######").format(quota / (float) 1048576) + " MB");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        if (mode.equals(MODE.EDIT)) {
            EditText titleView = (EditText) findViewById(R.id.settingsWorkspaceName);
            ((ViewGroup) titleView.getParent()).removeView(titleView);
        } else {
            ((Button) findViewById(R.id.confirmButton)).setText(getString(R.string.create));
        }

        setTitle(title);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(WORKSPACE_NAME, workspaceName);
        outState.putCharSequenceArrayList(USERS_LIST, users);
        outState.putCharSequenceArrayList(TAGS_LIST, tags);
        outState.putSerializable(ACTIVITY_MODE, mode);
        outState.putLong(MAX_QUOTA, quota);
        outState.putBoolean(PRIVACY, isPrivate);
        outState.putString(ACTIVITY_TITLE, title);
        super.onSaveInstanceState(outState);
    }

    // User List
    public void addRemoveUsers(View v) {
        Intent intent = new Intent(getApplicationContext(), ListActivity.class);
        intent.putCharSequenceArrayListExtra(ListActivity.LIST, users);
        String name = mode == MODE.EDIT ? workspaceName : getString(R.string.new_workspace);
        intent.putExtra(ListActivity.TITLE, getString(R.string.users_of) + " " + name);
        startActivityForResult(intent, USERS);
    }

    // Tag List
    public void addRemoveTags(View v) {
        Intent intent = new Intent(getApplicationContext(), ListActivity.class);
        intent.putCharSequenceArrayListExtra(ListActivity.LIST, tags);
        String name = mode == MODE.EDIT ? workspaceName : getString(R.string.new_workspace);
        intent.putExtra(ListActivity.TITLE, getString(R.string.tags_of) + " " + name);
        startActivityForResult(intent, TAGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == RESULT_OK)
            switch (requestCode) {
                case USERS:
                    users = data.getCharSequenceArrayListExtra(ListActivity.LIST);
                    Log.e("CreateEditWorkspace", "User List loaded");
                    break;

                case TAGS:
                    tags = data.getCharSequenceArrayListExtra(ListActivity.LIST);
                    Log.e("CreateEditWorkspace", "Tag List loaded");
                    break;
            }
    }

    // Privacy
    public void onRadioButtonPressed(View view) {
        isPrivate = !isPrivate;
    }

    // Final Buttons
    public void confirm(View v) {
        if (mode == MODE.EDIT) {
            FlowManager.notifyEditWorkspace(getApplicationContext(), workspaceName, isPrivate, users, tags, quota);
            Toast.makeText(getApplicationContext(), getString(R.string.workspace_edited_successfully), Toast.LENGTH_SHORT).show();
        } else {
            workspaceName = Utils.trim(((EditText) findViewById(R.id.settingsWorkspaceName)).getText().toString());
            if (workspaceName.length() != 0) {
                try {
                    FlowManager.notifyAddWorkspace(getApplicationContext(), workspaceName, isPrivate, users, tags, quota);
                } catch (AlreadyExistsException e) {
                    Toast.makeText(getApplicationContext(), "\"" + workspaceName + "\" " + getString(R.string.already_exists), Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(getApplicationContext(), getString(R.string.workspace_created_successfully), Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(getApplicationContext(), getString(R.string.invalid_input), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    public void cancel(View v) {
        finish();
    }

    // Action Bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mode == MODE.EDIT)
            getMenuInflater().inflate(R.menu.menu_delete, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_delete:
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.dialog_confirm_delete) + " \"" + workspaceName + "\"")
                        .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //Delete File
                                FlowManager.notifyRemoveWorkspace(getApplicationContext(), workspaceName);
                                Toast.makeText(getApplicationContext(), getString(R.string.workspace_removed_successfully), Toast.LENGTH_SHORT).show();
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

    public enum MODE {CREATE, EDIT}
}
