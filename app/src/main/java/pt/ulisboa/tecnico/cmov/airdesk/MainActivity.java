package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


public class MainActivity extends ActionBarActivity {

    public static final String PREFERENCES = "loginPrefs";
    public static final String NICKNAME = "nickname";
    public static final String EMAIL = "email";

    private SharedPreferences sharedPreferences;
    private EditText nicknameView;
    private EditText emailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize variables
        sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        nicknameView = (EditText) findViewById(R.id.loginNickname);
        emailView = (EditText) findViewById(R.id.loginEmail);

        //Initialize views with saved content
        if (sharedPreferences.contains(NICKNAME)) {
            nicknameView.setText(sharedPreferences.getString(NICKNAME, ""));

        }
        if (sharedPreferences.contains(EMAIL)) {
            emailView.setText(sharedPreferences.getString(EMAIL, ""));

        }

        if(!nicknameView.getText().toString().equals("") && !emailView.getText().toString().equals("")){
            Login(null);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void Login(View view){
        String nickname = nicknameView.getText().toString();
        String email = emailView.getText().toString();

        //Save views content
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putString(NICKNAME, nickname);
        editor.putString(EMAIL, email);
        editor.commit();

        //Change activity
        Intent intent = new Intent();
        intent.putExtra(NICKNAME, nickname);
        intent.putExtra(EMAIL, email);
        startActivity(intent);
        finish();
    }

}
