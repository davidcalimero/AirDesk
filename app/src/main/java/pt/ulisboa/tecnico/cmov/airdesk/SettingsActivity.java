package pt.ulisboa.tecnico.cmov.airdesk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.other.FileManager;
import pt.ulisboa.tecnico.cmov.airdesk.other.FlowManager;
import pt.ulisboa.tecnico.cmov.airdesk.other.User;
import pt.ulisboa.tecnico.cmov.airdesk.other.Workspace;

public class SettingsActivity extends ActionBarActivity {

    public static final String WORKSPACE_NAME = "workspaceName";
    public static final String TAGS_LIST = "tags";
    public static final String USERS_LIST = "users";

    public static final int USERS = 0;
    public static final int TAGS = 1;

    private String workspaceName;
    private Workspace workspace;

    // Privacy
    private Workspace.MODE privacy;

    // Quota
    int quota;

    // User List
    private ArrayList<CharSequence> users;

    // Tag list
    private ArrayList<CharSequence> tags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Checks if workspace exists
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras(): savedInstanceState;
        workspaceName = (String) bundle.get(WORKSPACE_NAME);
        ApplicationContext applicationContext = (ApplicationContext) getApplicationContext();
        User activeUser = applicationContext.getActiveUser();
        workspace = activeUser.getWorkspaceList().get(workspaceName);
        if(workspace == null) {
            Log.e("SettingsActivity", "This is a problem: Workspace " + workspaceName + " doesn't exist.");
            finish();
        }

        /*
        users = new ArrayList<>(workspace.getUserList());
        tags = new ArrayList<>(workspace.getPublicProfile());
        */

        users = workspace.getUserList();
        tags = workspace.getPublicProfile();

        //////////////////////////////////////////////
        // Privacy
        RadioButton publicButton = (RadioButton) findViewById(R.id.publicButton);
        RadioButton privateButton = (RadioButton) findViewById(R.id.privateButton);

        if(workspace.getPrivacy() == Workspace.MODE.PUBLIC){
            publicButton.setChecked(true);
            privateButton.setChecked(false);
        } else {
            publicButton.setChecked(false);
            privateButton.setChecked(true);
        }

        //////////////////////////////////////////////
        // Quota
        SeekBar quotaSeekBar = (SeekBar) findViewById(R.id.seekBar);
        final TextView quotaText = (TextView) findViewById(R.id.quotaValue);

        // Define minimal and maximal Value
        // Get current minimal value (sum of all file sizes), given by getQuota
        final long minQuota = workspace.getQuota();

        // Get maximal value (maximal space available in internal storage) in megabytes
        final long maxQuota = FileManager.getInternalFreeSpace();

        // Customize SeekBar placement
        // Get current quota value, in bytes
        final long currentQuota = workspace.getMaximumQuota() / 1048576;

        //set SeekBar Progress and Max
        quotaSeekBar.setMax((int) (maxQuota - minQuota));
        quotaSeekBar.setProgress((int) (currentQuota - minQuota));

        quota = (int) currentQuota;
        quotaText.setText(((Integer) quota).toString());

        quotaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                quota = (int) minQuota + progress;
                quotaText.setText(((Integer) quota).toString() + " MB");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        setTitle(workspaceName);

        //Force overflow menu on actionBar
        forceMenuOverflow();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(WORKSPACE_NAME, workspaceName);
        outState.putCharSequenceArrayList(USERS_LIST, users);
        outState.putCharSequenceArrayList(TAGS_LIST, tags);
        super.onSaveInstanceState(outState);
    }

    // User List
    public void addRemoveUsers(View v){
        Intent intent = new Intent(getApplicationContext(), ListActivity.class);
        intent.putCharSequenceArrayListExtra(ListActivity.LIST, users);
        intent.putExtra(ListActivity.TITLE, workspaceName + ": User List");
        startActivityForResult(intent, USERS);
    }

    // Tag List
    public void addRemoveTags(View v){
        Intent intent = new Intent(getApplicationContext(), ListActivity.class);
        intent.putCharSequenceArrayListExtra(ListActivity.LIST, tags);
        intent.putExtra(ListActivity.TITLE, workspaceName + ": Tag List");
        startActivityForResult(intent, TAGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data != null && resultCode == RESULT_OK)
            switch(requestCode){
                case USERS:
                    //tags = data.getCharSequenceArrayListExtra(TAGS_LIST);
                    users = data.getCharSequenceArrayListExtra(ListActivity.LIST);
                    break;

                case TAGS:
                    //users = data.getCharSequenceArrayListExtra(USERS_LIST);
                    tags = data.getCharSequenceArrayListExtra(ListActivity.LIST);
                    break;
            }
        /*
        if(data != null) {
            users = new ArrayList<>(data.getCharSequenceArrayListExtra(USERS_LIST));
            tags = new ArrayList<>(data.getCharSequenceArrayListExtra(TAGS_LIST));
        }*/

    }

    // Privacy
    public void onPublicButtonPressed(View view){
        privacy = Workspace.MODE.PUBLIC;
    }

    public void onPrivateButtonPressed(View view){
        privacy = Workspace.MODE.PRIVATE;
    }

    // Final Buttons
    public void confirm(View v){
        FlowManager.notifyEditWorkspace(getApplicationContext(), workspaceName, users, tags, privacy, ((long) quota) * 1048576);
        finish();
    }

    public void cancel(View v){
        finish();
    }

    // Action Bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_file, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.dialog_confirm_delete) + " \"" + workspaceName + "\"")
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
                        });
                // Display the AlertDialog object
                builder.create();
                builder.show();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    //Force overflow menu on actionBar
    private void forceMenuOverflow(){
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        }
        catch (Exception e) {
            Log.e("MainMenu", "Force action bar menu error");
        }
    }
}
