package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import pt.ulisboa.tecnico.cmov.airdesk.slidingTab.SlidingTabLayout;

import pt.ulisboa.tecnico.cmov.airdesk.adapter.WorkspacePagerAdapter;


public class MainMenu extends ActionBarActivity {

    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new WorkspacePagerAdapter(getSupportFragmentManager()));
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);

        mSlidingTabLayout.setViewPager(mViewPager);

        Intent intent = getIntent();
        String nickname = intent.getStringExtra("nickname");
        String email = intent.getStringExtra("email");

        setTitle(nickname + " | " + email);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        /* TODO */
        // Aqui põe-se as opções da ActionBar! :D

        return super.onOptionsItemSelected(item);
    }

    public void openFile(View view){
        Intent intent = new Intent(getApplicationContext(), ShowFileActivity.class);
        intent.putExtra("title", "TitleTest");
        intent.putExtra("text", "TextTest");
        startActivity(intent);
    }
}
