package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;


import pt.ulisboa.tecnico.cmov.airdesk.other.User;
import pt.ulisboa.tecnico.cmov.airdesk.slidingTab.SlidingTabLayout;

import pt.ulisboa.tecnico.cmov.airdesk.adapter.WorkspacePagerAdapter;


public class MainMenu extends ActionBarActivity {

    public static final String NICKNAME = "nickname";
    public static final String EMAIL = "email";
    //public static final String WORKSPACE = "workspace";

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
        String nickname = bundle.getString(NICKNAME);
        String email = bundle.getString(EMAIL);

        //Load current user
        appState.setActiveUser(User.LoadUser(email, nickname, getApplicationContext()));
        setTitle(nickname + ": " + email);

        //Force overflow menu on actionBar
        forceMenuOverflow();
    }

    @Override
    protected void onPause() {
        if(appState.hasActiveUser()) {
            appState.getActiveUser().commit(getApplicationContext());
            Log.e("MainMenu", "user committed:" + appState.getActiveUser().getID());
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
        /*Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        byte[] byteArray = null; //chamar método;
        intent.putExtra(WORKSPACE, byteArray);
        startActivity(intent);*/
    }

    private void logout(){
        Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
        intent.putExtra(LogInActivity.LOGOUT, true);
        startActivity(intent);
        Log.e("MainMenu", "Logout: " + appState.getActiveUser().getID());
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

    public void refresh(){
        adapter.notifyDataSetChanged();
    }
}
