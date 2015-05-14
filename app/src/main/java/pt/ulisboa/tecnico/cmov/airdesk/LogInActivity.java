package pt.ulisboa.tecnico.cmov.airdesk;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.airdesk.listener.ConnectionHandler;
import pt.ulisboa.tecnico.cmov.airdesk.utility.Utils;


public class LogInActivity extends AppCompatActivity {

    public static final String PREFERENCES = "loginPrefs";
    public static final String LOGOUT = "logout";

    private static final String NICKNAME = "nickname";
    private static final String EMAIL = "email";

    private SharedPreferences sharedPreferences;
    private ApplicationContext appState;

    private EditText nicknameView;
    private EditText emailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Hide ActionBar
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();

        //Get login data from sharedPreferences
        sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        String nickname = sharedPreferences.getString(MainMenu.NICKNAME, "");
        String email = sharedPreferences.getString(MainMenu.EMAIL, "");

        //Init variable
        appState = (ApplicationContext) getApplicationContext();

        //Logout if applicable
        if (getIntent().getBooleanExtra(LOGOUT, false)) {
            sharedPreferences.edit().clear().apply();
            appState.reset();
        }

        //If was not logout and have data to login autologin
        if (!nickname.equals("") && !email.equals("") && !getIntent().getBooleanExtra(LOGOUT, false)) {
            loadUser(nickname, email);
            return;
        }

        //Restore data
        nicknameView = (EditText) findViewById(R.id.loginNickname);
        nicknameView.setText(getIntent().getStringExtra(NICKNAME));
        emailView = (EditText) findViewById(R.id.loginEmail);
        emailView.setText(getIntent().getStringExtra(EMAIL));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(NICKNAME, nicknameView.getText().toString());
        outState.putString(EMAIL, emailView.getText().toString());
        super.onSaveInstanceState(outState);
    }


    public void onLoginButtonPressed(View view) {
        //Get data from views
        String nickname = nicknameView.getText().toString().trim();
        String email = emailView.getText().toString().trim();

        if (!Utils.isSingleWord(nickname) || !Utils.isSingleWord(email)) {
            Toast.makeText(getApplicationContext(), R.string.invalid_input, Toast.LENGTH_SHORT).show();
            return;
        }

        loadUser(nickname, email);

        //Save views content
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MainMenu.NICKNAME, nickname);
        editor.putString(MainMenu.EMAIL, email);
        editor.apply();
    }

    private void loadUser(final String nickname, final String email) {
        final ProgressDialog dialog;
        dialog = new ProgressDialog(this);
        dialog.setTitle(getString(R.string.dialog_please_wait));
        dialog.setCancelable(false);
        dialog.setIndeterminate(false);
        dialog.setMessage(getString(R.string.dialog_loading_user));
        dialog.show();

        appState.init(email, nickname, new ConnectionHandler<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //Change activity
                Intent intent = new Intent(getApplicationContext(), MainMenu.class);
                intent.putExtra(MainMenu.NICKNAME, nickname);
                intent.putExtra(MainMenu.EMAIL, email);
                startActivity(intent);
                finish();
                dialog.dismiss();
            }

            @Override
            public void onFailure() {
                dialog.dismiss();
            }
        });
    }
}
