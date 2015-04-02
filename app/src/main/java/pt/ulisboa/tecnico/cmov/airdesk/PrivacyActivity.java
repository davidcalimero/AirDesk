package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;

public class PrivacyActivity extends ActionBarActivity {


    private boolean publicPrivacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        RadioButton publicButton = (RadioButton) findViewById(R.id.publicButton);
        RadioButton privateButton = (RadioButton) findViewById(R.id.privateButton);

        Bundle bundle = savedInstanceState == null ? getIntent().getExtras(): savedInstanceState;
        publicPrivacy = (boolean) bundle.get(SettingsActivity.PRIVACY);

        if(publicPrivacy){
            publicButton.setChecked(true);
            privateButton.setChecked(false);
        } else {
            publicButton.setChecked(false);
            privateButton.setChecked(true);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_workspace_privacy, menu);
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

    public void onPublicButtonPressed(View view){
        publicPrivacy = true;
    }

    public void onPrivateButtonPressed(View view){
        publicPrivacy = false;
    }


    public void onDoneButtonPressed(View view){
        Intent intent = new Intent();
        intent.putExtra(SettingsActivity.PRIVACY, publicPrivacy);
        setResult(RESULT_OK, intent);
        finish();
    }
}
