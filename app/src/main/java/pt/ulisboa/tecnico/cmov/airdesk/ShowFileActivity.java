package pt.ulisboa.tecnico.cmov.airdesk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.airdesk.other.FlowManager;


public class ShowFileActivity extends ActionBarActivity {

    public static final String WORKSPACE = "workspace";
    public static final String TITLE = "title";
    public static final String TEXT = "text";

    private String workspace;
    private String title;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_file);

        //Restore data
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras(): savedInstanceState;
        workspace = bundle.getString(WORKSPACE);
        title = bundle.getString(TITLE);
        text = bundle.getString(TEXT);

        //Init
        TextView showText = (TextView) findViewById(R.id.textView);
        showText.setText(text);
        setTitle(title);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(TITLE, title);
        outState.putString(TEXT, text);
        Log.e("ShowFileActivity", "state saved: " + title);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_file, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.dialog_confirm_delete) + " \"" + title + "\"")
                        .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //Delete File
                                FlowManager.notifyRemoveFile(getApplicationContext(), workspace, title);
                                Toast.makeText(getApplicationContext(), getString(R.string.file_removed_successfully), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                // Display the AlertDialog object
                builder.create();
                builder.show();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void editButtonPressed(View view){
        Intent intent = new Intent(getApplicationContext(), CreateEditFileActivity.class);
        intent.putExtra(CreateEditFileActivity.ACTIVITY_TITLE, title);
        intent.putExtra(CreateEditFileActivity.ACTIVITY_MODE, CreateEditFileActivity.MODE.EDIT);
        intent.putExtra(CreateEditFileActivity.FILE_WORKSPACE, workspace);
        intent.putExtra(CreateEditFileActivity.FILE_TITLE, title);
        intent.putExtra(CreateEditFileActivity.FILE_CONTENT, text);
        startActivity(intent);
        finish();
    }
}
