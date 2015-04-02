package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_quota, menu);
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

    public void onDoneButtonPressed(View view){
        Intent intent = new Intent();
        intent.putExtra(SettingsActivity.QUOTA, quota);
        setResult(RESULT_OK, intent);
        finish();
    }
}
