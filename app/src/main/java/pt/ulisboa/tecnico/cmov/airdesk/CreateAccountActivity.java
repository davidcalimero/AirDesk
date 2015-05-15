package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;

import pt.ulisboa.tecnico.cmov.airdesk.utility.MyAsyncTask;
import pt.ulisboa.tecnico.cmov.airdesk.utility.Utils;


public class CreateAccountActivity extends AppCompatActivity {

    private static final String NICKNAME = "nickname";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";
    private static final String KEY = "key";

    private EditText nicknameView;
    private EditText emailView;
    private EditText passwordView;
    private EditText keyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        //Hide ActionBar
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();

        nicknameView = ((EditText) findViewById(R.id.createAccountNickname));
        emailView = ((EditText) findViewById(R.id.createAccountEmail));
        passwordView = ((EditText) findViewById(R.id.createAccountPassword));
        keyView = ((EditText) findViewById(R.id.createAccountKey));

        //Restore data
        if(savedInstanceState != null) {
            nicknameView.setText(savedInstanceState.getString(NICKNAME));
            emailView.setText(savedInstanceState.getString(EMAIL));
            passwordView.setText(savedInstanceState.getString(PASSWORD));
            keyView.setText(savedInstanceState.getString(KEY));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(NICKNAME, nicknameView.getText().toString());
        outState.putString(EMAIL, emailView.getText().toString());
        outState.putString(PASSWORD, passwordView.getText().toString());
        outState.putString(KEY, keyView.getText().toString());
        Log.e("CreateAccountActivity", "state saved: ");
        super.onSaveInstanceState(outState);
    }

    public void onSendKeyButtonPressed(View view){
        //Get data from views
        final String nickname = nicknameView.getText().toString().trim();
        final String email = emailView.getText().toString().trim();
        final String password = passwordView.getText().toString().trim();

        if (!Utils.isSingleWord(nickname) || !Utils.isSingleWord(email) || !Utils.isSingleWord(password)) {
            Toast.makeText(getApplicationContext(), R.string.invalid_input, Toast.LENGTH_SHORT).show();
            return;
        }

        nicknameView.setEnabled(false);
        emailView.setEnabled(false);
        passwordView.setEnabled(false);

        //generate security key
        new MyAsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void param) {
                HttpURLConnection connection = null;

                try {
                    String parameters = "nickname" + "=" + nickname;
                    parameters += "&" + "email"    + "=" + email;
                    parameters += "&" + "password" + "=" + password;

                    URL url = new URL("http://web.ist.utl.pt/~ist166392/cmov/email.php?" + parameters);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                    wr.flush();
                    wr.close();

                    connection.getInputStream();

                    Log.e("URL", String.valueOf(connection.getURL()));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    connection.disconnect();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                Toast.makeText(getApplicationContext(), getString(R.string.security_key_sent), Toast.LENGTH_SHORT).show();
            }
        }.execute(null);
    }

    public void onCancelButtonPressed(View view){
        startActivity(new Intent(getApplicationContext(), LogInActivity.class));
        finish();
    }

    public void onCreateButtonPressed(View view){
        //Get data from views
        final String nickname = nicknameView.getText().toString().trim();
        final String email = emailView.getText().toString().trim();
        final String password = passwordView.getText().toString().trim();
        final String key = keyView.getText().toString().trim();

        if (!Utils.isSingleWord(nickname) || !Utils.isSingleWord(email) || !Utils.isSingleWord(password) || !Utils.isSingleWord(key)) {
            Toast.makeText(getApplicationContext(), R.string.invalid_input, Toast.LENGTH_SHORT).show();
            return;
        }

        //Active account
        new MyAsyncTask<Void, Void, Void>(){
            private boolean value = false;

            @Override
            protected Void doInBackground(Void param) {
                try {
                   Connection databaseConnection = Utils.generateConnection();
                    CallableStatement stmt = databaseConnection.prepareCall("{call cmov_active_account(?, ?)}");
                    stmt.setString(1, email);
                    stmt.setString(2, key);
                    ResultSet result = stmt.executeQuery();
                    result.next();
                    value = result.getBoolean(1);
                    databaseConnection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                if(!value)
                    Toast.makeText(getApplicationContext(), getString(R.string.invalid_input), Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(getApplicationContext(), getString(R.string.account_created_successfully), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), LogInActivity.class));
                    finish();
                }
            }
        }.execute(null);
    }
}
