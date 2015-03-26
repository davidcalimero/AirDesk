package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    public static final String PREFERENCES = "loginPrefs";
    public static final String LOGOUT = "logout";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Hide ActionBar
        getSupportActionBar().hide();

        String nickname;
        String email;

        //Get login data from shareddprefs
        sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        nickname = sharedPreferences.getString(MainMenu.NICKNAME, "");
        email = sharedPreferences.getString(MainMenu.EMAIL, "");

        //Logout if applicable
        if(getIntent().getBooleanExtra(LOGOUT, false)){
            sharedPreferences.edit().clear().commit();
        }

        //If was not logout and have data to login autologin
        if(!nickname.equals("") && !email.equals("") && !getIntent().getBooleanExtra(LOGOUT, false)){
            sendLoginData(nickname, email);
        }
    }


    public void login(View view){
        //Get data from views
        EditText nicknameView = (EditText) findViewById(R.id.loginNickname);
        EditText emailView = (EditText) findViewById(R.id.loginEmail);
        String nickname = nicknameView.getText().toString();
        String email = emailView.getText().toString();

        //Invalid input verification
        if(nickname.equals("") || email.equals("")){
            Toast.makeText(getApplicationContext(), R.string.invalid_input, Toast.LENGTH_SHORT).show();
            return;
        }

        //Save views content
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MainMenu.NICKNAME, nickname);
        editor.putString(MainMenu.EMAIL, email);
        editor.commit();

        sendLoginData(nickname, email);
    }

    private void sendLoginData(String nickname, String email){
        //Change activity
        Intent intent = new Intent(getApplicationContext(), MainMenu.class);
        intent.putExtra(MainMenu.NICKNAME, nickname);
        intent.putExtra(MainMenu.EMAIL, email);
        startActivity(intent);
        finish();
    }
}
