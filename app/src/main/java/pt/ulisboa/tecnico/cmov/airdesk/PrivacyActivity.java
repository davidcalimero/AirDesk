package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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
