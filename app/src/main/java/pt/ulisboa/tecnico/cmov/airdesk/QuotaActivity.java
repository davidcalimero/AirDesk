package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import pt.ulisboa.tecnico.cmov.airdesk.other.FileManager;


public class QuotaActivity extends ActionBarActivity {

    private float quota;
    private int freeSpace = (int) FileManager.getInternalFreeSpace();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quota);

        SeekBar quotaSeekBar = (SeekBar) findViewById(R.id.quotaSeekBar);
        final TextView quotaText = (TextView) findViewById(R.id.quotaTextView);

        quotaText.setText(((Float) quota).toString()); //cast para String

        quotaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                quota = progress * freeSpace / 100;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                quotaText.setText(((Float) quota).toString());
            }
        });
    }


    public void onDoneButtonPressed(View view){
        Intent intent = new Intent();
        intent.putExtra(SettingsActivity.QUOTA, quota);
        setResult(RESULT_OK, intent);
        finish();
    }
}
