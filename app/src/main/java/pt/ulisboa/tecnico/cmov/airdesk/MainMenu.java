package pt.ulisboa.tecnico.cmov.airdesk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;


import pt.ulisboa.tecnico.cmov.airdesk.other.FlowManager;
import pt.ulisboa.tecnico.cmov.airdesk.other.User;
import pt.ulisboa.tecnico.cmov.airdesk.slidingTab.SlidingTabLayout;

import pt.ulisboa.tecnico.cmov.airdesk.adapter.WorkspacePagerAdapter;


public class MainMenu extends ActionBarActivity {

    public static final int OWNED = 1;
    public static final int FOREIGN = 2;

    public static final String NICKNAME = "nickname";
    public static final String EMAIL = "email";

    private ApplicationContext appState;
    private WorkspacePagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        adapter = new WorkspacePagerAdapter(getSupportFragmentManager(), getApplicationContext());
        mViewPager.setAdapter(adapter);
        SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);

        appState = (ApplicationContext) getApplicationContext();

        //Restore data
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras(): savedInstanceState;
        final String nickname = bundle.getString(NICKNAME);
        final String email = bundle.getString(EMAIL);

        loadUser(email, nickname);

        //Force overflow menu on actionBar
        forceMenuOverflow();
    }

    @Override
    protected void onPause() {
        if(appState.hasActiveUser()) {
            appState.getActiveUser().commit(getApplicationContext());
        }
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        User user = appState.getActiveUser();
        outState.putString(NICKNAME, user.getNickname());
        outState.putString(EMAIL, user.getEmail());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_logout:
                logout();
                break;

             default:
                 return super.onOptionsItemSelected(item);
        }

        return true;
    }

    public void onSettingsButtonPressed(View view){
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        String workspaceName = ((TextView) ((ViewGroup) view.getParent()).findViewById(R.id.groupHeader)).getText().toString();
        intent.putExtra(SettingsActivity.WORKSPACE_NAME, workspaceName);
        startActivity(intent);
    }

    public void onAddFileButtonPressed(View view){
        String workspaceName = ((TextView) ((ViewGroup) view.getParent()).findViewById(R.id.groupHeader)).getText().toString();
        Intent intent = new Intent(getApplicationContext(), CreateEditFileActivity.class);
        intent.putExtra(CreateEditFileActivity.ACTIVITY_TITLE, getString(R.string.create_new_file));
        intent.putExtra(CreateEditFileActivity.ACTIVITY_MODE, CreateEditFileActivity.MODE.CREATE);
        intent.putExtra(CreateEditFileActivity.FILE_WORKSPACE, workspaceName);
        startActivity(intent);
    }

    private void logout(){
        Log.e("MainMenu", "Logout: " + appState.getActiveUser().getID());
        appState.removeUser();
        Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
        intent.putExtra(LogInActivity.LOGOUT, true);
        startActivity(intent);
        finish();
    }

    //Force overflow menu on actionBar
    private void forceMenuOverflow(){
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        }
        catch (Exception e) {
            Log.e("MainMenu", "Force action bar menu error");
        }
    }

    //Load current user
    private void loadUser(final String email, final String nickname){
        final ProgressDialog dialog = ProgressDialog.show(this,"Please Wait", "Loading User...", false, false);
        if(!appState.isActiveUser(email)){
            dialog.show();
            /*Thread thread = new Thread(){
                @Override
                public void run() {
                    try {
                        sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    appState.setActiveUser(User.LoadUser(email, nickname, getApplicationContext()));
                    dialog.dismiss();
                }
           /* };
            thread.start();
        }
        else*/ dialog.dismiss();
        setTitle(nickname + ": " + email);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data != null && resultCode == RESULT_OK) {
            if (requestCode == FOREIGN) {
                FlowManager.notifySubscriptionsChange(getApplicationContext(), data.getCharSequenceArrayListExtra(ListActivity.LIST));
                Toast.makeText(getApplicationContext(), getString(R.string.subscriptions_changed_successfully), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
