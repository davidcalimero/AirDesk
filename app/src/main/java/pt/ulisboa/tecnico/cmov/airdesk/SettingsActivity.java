package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.other.User;
import pt.ulisboa.tecnico.cmov.airdesk.other.Workspace;


public class SettingsActivity extends ActionBarActivity {

    public static final String PRIVACY = "privacy";
    public static final String QUOTA = "quota";

    private String workspaceName;
    private Workspace workspace;
    private ArrayList<CharSequence> users;
    private boolean publicPrivacy;  //true = public     false = private
    private float quota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Bundle bundle = savedInstanceState == null ? getIntent().getExtras(): savedInstanceState;
        workspaceName = (String) bundle.get(MainMenu.WORKSPACE_NAME);
        ApplicationContext applicationContext = (ApplicationContext) getApplicationContext();
        User activeUser = applicationContext.getActiveUser();
        workspace = activeUser.getWorkspaceByName(workspaceName);
        Log.e("SettingsActivity", "user: "+activeUser+"  workspace: "+workspaceName);

        if(workspace == null){
            Log.e("SettingsActivity", workspaceName + " nao e o nome de um workspace.");
            finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onEditPersonPressed(View view){
        /*
         * A TagsActivity e utilizada como forma de poupar codigo duplicado,
         * porque faz o que e preciso para adicionar e remover pessoas
         */
        Intent intent = new Intent(getApplicationContext(), TagsActivity.class);
        users = new ArrayList<>(workspace.getUserList());
        intent.putCharSequenceArrayListExtra(TagsActivity.TAGS, users);
        startActivityForResult(intent, 1);
    }

    public void onChangePrivacyPressed(View view){
        Intent intent = new Intent(getApplicationContext(), PrivacyActivity.class);
        publicPrivacy = modeToBoolean(workspace.getPrivacy());
        intent.putExtra(SettingsActivity.PRIVACY, publicPrivacy);
        startActivityForResult(intent, 2);
    }

    public void onChangeQuotaPressed(View view){
        Intent intent = new Intent(getApplicationContext(), QuotaActivity.class);
        intent.putExtra(SettingsActivity.QUOTA, quota);
        startActivityForResult(intent, 3);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            Toast.makeText(getApplicationContext(), "No data Received", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (requestCode){

            case(1):{
                users = data.getCharSequenceArrayListExtra(TagsActivity.TAGS);
                workspace.setUserList(users);
                break;
            }

            case(2):{
                publicPrivacy = data.getBooleanExtra(SettingsActivity.PRIVACY, false);
                workspace.setPrivacy(booleanToMode(publicPrivacy));
                break;
            }

            case(3):{
                quota = data.getFloatExtra(SettingsActivity.QUOTA, -1);
                break;
            }

            default:
                Toast.makeText(getApplicationContext(), "Error on request code", Toast.LENGTH_SHORT).show();
        }

    }

    /*
     * Converte o MODE num boolean
     * true = PUBLIC e false = PRIVATE
     */
    private boolean modeToBoolean(Workspace.MODE mode){
        return mode == Workspace.MODE.PUBLIC;
    }

    private Workspace.MODE booleanToMode(boolean publicPrivacy){
        if(publicPrivacy)
            return Workspace.MODE.PUBLIC;
        else
            return Workspace.MODE.PRIVATE;
    }


}
