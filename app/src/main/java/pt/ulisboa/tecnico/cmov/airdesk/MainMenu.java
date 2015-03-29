package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;


import pt.ulisboa.tecnico.cmov.airdesk.slidingTab.SlidingTabLayout;

import pt.ulisboa.tecnico.cmov.airdesk.adapter.WorkspacePagerAdapter;


public class MainMenu extends ActionBarActivity {

    public static final String NICKNAME = "nickname";
    public static final String EMAIL = "email";

    private String nickname;
    private String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new WorkspacePagerAdapter(getSupportFragmentManager(), getApplicationContext()));
        SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);

        if(savedInstanceState == null) {
            //Default sate
            Intent intent = getIntent();
            nickname = intent.getStringExtra(NICKNAME);
            email = intent.getStringExtra(EMAIL);
        }
        else{
            //Saved state
            nickname = savedInstanceState.getString(NICKNAME);
            email = savedInstanceState.getString(EMAIL);
        }
        
        setTitle(nickname + ": " + email);

        //Force overflow menu on actionBar
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(NICKNAME, nickname);
        outState.putString(EMAIL, email);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.action_logout:
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra(MainActivity.LOGOUT, true);
                startActivity(intent);
                finish();
                break;

             default:
                 return super.onOptionsItemSelected(item);
        }

        return true;
    }
}
