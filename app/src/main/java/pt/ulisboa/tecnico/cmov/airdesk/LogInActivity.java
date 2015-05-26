package pt.ulisboa.tecnico.cmov.airdesk;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import pt.ulisboa.tecnico.cmov.airdesk.listener.ConnectionHandler;
import pt.ulisboa.tecnico.cmov.airdesk.utility.MyAsyncTask;
import pt.ulisboa.tecnico.cmov.airdesk.utility.Utils;


public class LogInActivity extends AppCompatActivity {

    public static final String PREFERENCES = "loginPrefs";
    public static final String LOGOUT = "logout";

    private static final String PASSWORD = "password";
    private static final String EMAIL = "email";

    private SharedPreferences sharedPreferences;
    private ApplicationContext appState;

    private EditText passwordView;
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
        String password = sharedPreferences.getString(PASSWORD, "");
        String email = sharedPreferences.getString(EMAIL, "");

        //Init variable
        appState = (ApplicationContext) getApplicationContext();

        //Logout if applicable
        if (getIntent().getBooleanExtra(LOGOUT, false)) {
            sharedPreferences.edit().clear().apply();
            appState.reset();
        }

        //If was not logout and have data to login autologin
        if (!password.equals("") && !email.equals("") && !getIntent().getBooleanExtra(LOGOUT, false)) {
            loadUser(email, password);
            return;
        }

        //Restore data
        passwordView = (EditText) findViewById(R.id.loginPassword);
        passwordView.setText(getIntent().getStringExtra(PASSWORD));
        emailView = (EditText) findViewById(R.id.loginEmail);
        emailView.setText(getIntent().getStringExtra(EMAIL));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(PASSWORD, passwordView.getText().toString());
        outState.putString(EMAIL, emailView.getText().toString());
        super.onSaveInstanceState(outState);
    }


    public void onLoginButtonPressed(View view) {
        //Get data from views
        String password = passwordView.getText().toString().trim();
        String email = emailView.getText().toString().trim();

        if (!Utils.isSingleWord(password) || !Utils.isSingleWord(email)) {
            Toast.makeText(getApplicationContext(), R.string.invalid_input, Toast.LENGTH_SHORT).show();
            return;
        }

        loadUser(email, password);
    }

    public void onCreateAccountButtonPressed(View view) {
        startActivity(new Intent(getApplicationContext(), CreateAccountActivity.class));
        finish();
    }

    private void loadUser(final String email, final String password) {
        final ProgressDialog dialog;
        dialog = new ProgressDialog(this);
        dialog.setTitle(getString(R.string.dialog_please_wait));
        dialog.setCancelable(false);
        dialog.setIndeterminate(false);
        dialog.setMessage(getString(R.string.dialog_loading_user));
        dialog.show();

        new MyAsyncTask<Void, Void, String>(){

            @Override
            protected String doInBackground(Void param) {
                String nickname = null;
                try {
                    Connection databaseConnection = Utils.generateConnection();
                    if(databaseConnection != null) {
                        CallableStatement stmt = databaseConnection.prepareCall("{call cmov_login(?, ?)}");
                        stmt.setString(1, email);
                        stmt.setString(2, password);
                        ResultSet result = stmt.executeQuery();
                        result.next();
                        nickname = result.getString(1);
                        databaseConnection.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return nickname;
            }

            @Override
            protected void onPostExecute(final String nickname) {
                if(nickname == null){
                    Toast.makeText(getApplicationContext(), getString(R.string.invalid_input), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
                else {
                    appState.init(email, nickname, new ConnectionHandler<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            //Save views content
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(PASSWORD, password);
                            editor.putString(EMAIL, email);
                            editor.apply();
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
        }.execute(null);
    }
}
