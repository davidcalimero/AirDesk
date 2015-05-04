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
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.HashSet;

import pt.ulisboa.tecnico.cmov.airdesk.exception.AlreadyExistsException;
import pt.ulisboa.tecnico.cmov.airdesk.utility.FlowManager;
import pt.ulisboa.tecnico.cmov.airdesk.utility.Utils;

public class CreateEditWorkspaceActivity extends AppCompatActivity {

    public enum MODE {CREATE, EDIT}
    private EditText titleView;

    //region INTENT MACROS
    public static final String WORKSPACE_NAME = "workspaceName";
    public static final String ACTIVITY_MODE = "mode";
    public static final String ACTIVITY_TITLE = "title";
    public static final String MAP = "map";
    //endregion

    //region INTENT REQUEST CODES
    public static final int USERS = 1;
    public static final int TAGS = 2;
    //endregion

    //region SAVE STATE MACROS
    private static final String TAGS_LIST = "tagsList";
    private static final String USERS_LIST = "usersList";
    private static final String MAX_QUOTA = "maxQuota";
    private static final String PRIVACY = "privacy";
    //endregion

    //region CLASS VARIABLES
    private String workspaceName;
    private String title;
    private MODE mode;
    private HashSet<CharSequence> tags = new HashSet<>();
    private HashSet<CharSequence> users = new HashSet<>();
    private HashMap<CharSequence, Boolean> usersMap = new HashMap<>();
    private boolean isPrivate = false;
    private long quota = 0;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_workspace);

        //Restore data
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        workspaceName = bundle.getString(WORKSPACE_NAME);
        mode = (MODE) bundle.getSerializable(ACTIVITY_MODE);
        title = bundle.getString(ACTIVITY_TITLE);

        Log.e("CreateEditWorkspace", workspaceName + " " + title);

        if (savedInstanceState != null) {
            users = (HashSet<CharSequence>) savedInstanceState.getSerializable(USERS_LIST);
            tags = (HashSet<CharSequence>) savedInstanceState.getSerializable(TAGS_LIST);
            quota = savedInstanceState.getLong(MAX_QUOTA);
            isPrivate = savedInstanceState.getBoolean(PRIVACY);
            usersMap = (HashMap<CharSequence, Boolean>) savedInstanceState.getSerializable(MAP);
        } else if (mode == MODE.EDIT) {
            users = FlowManager.getWorkspaceUsers(getApplicationContext(), workspaceName);
            tags = FlowManager.getWorkspaceTags(getApplicationContext(), workspaceName);
            quota = FlowManager.getWorkspaceMaxQuota(getApplicationContext(), workspaceName);
            isPrivate = FlowManager.isWorkspacePrivate(getApplicationContext(), workspaceName);
        }

        // Privacy
        initializePrivacy();

        // Quota
        initializeQuota();

        // Format Layout
        formatLayout();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(WORKSPACE_NAME, titleView.getText().toString());
        outState.putSerializable(USERS_LIST, users);
        outState.putSerializable(TAGS_LIST, tags);
        outState.putSerializable(ACTIVITY_MODE, mode);
        outState.putLong(MAX_QUOTA, quota);
        outState.putBoolean(PRIVACY, isPrivate);
        outState.putString(ACTIVITY_TITLE, title);
        outState.putSerializable(MAP, usersMap);
        Log.e("CreateEditWorkspace", "state saved: " + titleView.getText().toString());
        super.onSaveInstanceState(outState);
    }

    // User List
    public void addRemoveUsers(View v) {
        Intent intent = new Intent(getApplicationContext(), ListActivity.class);
        intent.putExtra(ListActivity.LIST, users);
        String name = mode == MODE.EDIT ? workspaceName : getString(R.string.new_workspace);
        intent.putExtra(ListActivity.TITLE, getString(R.string.users_of) + " " + name);
        intent.putExtra(ListActivity.MAP, usersMap);
        startActivityForResult(intent, USERS);
    }

    // Tag List
    public void addRemoveTags(View v) {
        Intent intent = new Intent(getApplicationContext(), ListActivity.class);
        intent.putExtra(ListActivity.LIST, tags);
        String name = mode == MODE.EDIT ? workspaceName : getString(R.string.new_workspace);
        intent.putExtra(ListActivity.TITLE, getString(R.string.tags_of) + " " + name);
        startActivityForResult(intent, TAGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == RESULT_OK)
            switch (requestCode) {
                case USERS:
                    usersMap = (HashMap<CharSequence, Boolean>) data.getSerializableExtra(ListActivity.MAP);
                    users = (HashSet<CharSequence>) data.getSerializableExtra(ListActivity.LIST);
                    Log.e("CreateEditWorkspace", "User List loaded");
                    break;

                case TAGS:
                    tags = (HashSet<CharSequence>) data.getSerializableExtra(ListActivity.LIST);
                    Log.e("CreateEditWorkspace", "Tag List loaded");
                    break;
            }
    }

    // Privacy
    public void onPrivateButtonPressed(View view) {
        isPrivate = true;
    }

    public void onPublicButtonPressed(View view) {
        isPrivate = false;
    }

    // Final Buttons
    public void confirm(View v) {
        if (mode == MODE.EDIT) {
            FlowManager.notifyEditWorkspace(getApplicationContext(), workspaceName, isPrivate, usersMap, tags, quota);
            Toast.makeText(getApplicationContext(), getString(R.string.workspace_edited_successfully), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            workspaceName = Utils.trim(((EditText) findViewById(R.id.settingsWorkspaceName)).getText().toString());
            if(workspaceName.length() == 0){
                Toast.makeText(getApplicationContext(), getString(R.string.invalid_input), Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                FlowManager.notifyAddWorkspace(getApplicationContext(), workspaceName, isPrivate, users, tags, quota);
                Toast.makeText(getApplicationContext(), getString(R.string.workspace_created_successfully), Toast.LENGTH_SHORT).show();
                finish();
            } catch (AlreadyExistsException e) {
                Toast.makeText(getApplicationContext(), "\"" + workspaceName + "\" " + getString(R.string.already_exists), Toast.LENGTH_SHORT).show();
            }
        }
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

    // Layout Customization
    private void initializeQuota(){
        SeekBar quotaSeekBar = (SeekBar) findViewById(R.id.seekBar);
        final TextView quotaText = (TextView) findViewById(R.id.quotaValue);

        // Define Limits
        final long minQuota = mode == MODE.EDIT ? FlowManager.getWorkspaceMemorySize(getApplicationContext(), workspaceName) : 0;
        final long maxQuota = FlowManager.getUserFreeMemorySpace() + minQuota;

        // Customize SeekBar
        quotaSeekBar.setProgress((int) Utils.minMaxNormalization(quota, minQuota, maxQuota, 0, 100));
        quotaText.setText(Utils.formatNumber("#.######", quota / (float) 1048576) + " MB");

        quotaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                quota = Utils.minMaxNormalization(progress, 0, 100, minQuota, maxQuota) + minQuota;
                quotaText.setText(Utils.formatNumber("#.######", quota / (float) 1048576) + " MB");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void initializePrivacy(){
        RadioButton publicButton = (RadioButton) findViewById(R.id.publicButton);
        RadioButton privateButton = (RadioButton) findViewById(R.id.privateButton);
        if (isPrivate) {
            publicButton.setChecked(false);
            privateButton.setChecked(true);
        } else {
            publicButton.setChecked(true);
            privateButton.setChecked(false);
        }
    }

    private void formatLayout(){
        titleView = (EditText) findViewById(R.id.settingsWorkspaceName);
        if (mode.equals(MODE.EDIT)) {
            titleView.setText(workspaceName);
            titleView.setVisibility(View.GONE);
        } else {
            ((Button) findViewById(R.id.confirmButton)).setText(getString(R.string.create));
        }

        setTitle(title);
    }
}
