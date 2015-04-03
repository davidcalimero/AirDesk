package pt.ulisboa.tecnico.cmov.airdesk;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.other.FileManager;
import pt.ulisboa.tecnico.cmov.airdesk.other.FlowManager;
import pt.ulisboa.tecnico.cmov.airdesk.other.User;
import pt.ulisboa.tecnico.cmov.airdesk.other.Workspace;

public class SettingsActivity extends ActionBarActivity {

    public static final String WORKSPACE_NAME = "workspaceName";

    private String workspaceName;

    // Privacy
    private Workspace.MODE privacy;

    // Quota
    int quota;

    // New Code
    private ArrayList<CharSequence> list;
    private ArrayAdapter<String> adapter;
    private EditText personEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Checks if workspace exists
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras(): savedInstanceState;
        workspaceName = (String) bundle.get(WORKSPACE_NAME);
        ApplicationContext applicationContext = (ApplicationContext) getApplicationContext();
        User activeUser = applicationContext.getActiveUser();
        Workspace workspace = activeUser.getWorkspaceList().get(workspaceName);
        if(workspace == null) {
            Log.e("SettingsActivity", "This is a problem: Workspace " + workspaceName + " doesn't exist.");
            finish();
        }

        //Restore data
        personEdit = (EditText) findViewById(R.id.personID);
        list = workspace.getUserList();

        //ListView inicialization
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        ListView listView = (ListView) findViewById(R.id.personList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                adapter.remove(list.get(position).toString());
                list.remove(position);
            }
        });

        populateView();

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
                quotaText.setText(((Integer) quota).toString());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        setTitle(workspaceName);
    }

    public void addItem(View view){
        String item = personEdit.getText().toString();

        //Invalid input verification
        if (item.split(" ").length != 1 || item.length() == 0) {
            Toast.makeText(getApplicationContext(), R.string.invalid_input, Toast.LENGTH_SHORT).show();
            return;
        }
        if (list.contains(item)){
            Toast.makeText(getApplicationContext(), "\"" + item + "\" " + getString(R.string.already_exists), Toast.LENGTH_SHORT).show();
            return;
        }

        personEdit.getText().clear();
        adapter.add(item);
        list.add(item);
    }

    //ListView population
    private void populateView(){
        for(CharSequence item : list){
            adapter.add(item.toString());
        }
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
        FlowManager.notifyEditWorkspace(getApplicationContext(), workspaceName, list, privacy, ((long) quota) * 1048576);
        finish();
    }

    public void cancel(View v){
        finish();
    }
}
