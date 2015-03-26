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

    public static final String NICKNAME = "nickname";
    public static final String EMAIL = "email";

    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;
    private String nickname;
    private String email;

    //private ArrayList<CharSequence> tags = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new WorkspacePagerAdapter(getSupportFragmentManager()));
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
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
        
        setTitle(nickname + " | " + email);
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
            case R.id.action_settings:
                break;

            case R.id.action_refresh:
                break;

            case R.id.action_logout:
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra(MainActivity.LOGOUT, true);
                startActivity(intent);
                finish();

             default:
                 return super.onOptionsItemSelected(item);
        }

        return true;
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        tags = data.getCharSequenceArrayListExtra(TagsActivity.TAGS);
    }*/

    public void openFile(View view){

        /*Intent intent = new Intent(getApplicationContext(), ShowFileActivity.class);
        intent.putExtra(ShowFileActivity.TITLE, "TitleTest");
        intent.putExtra(ShowFileActivity.TEXT, "TextTest");
        startActivity(intent);*/


        /*Intent intent = new Intent(getApplicationContext(), TagsActivity.class);
        intent.putCharSequenceArrayListExtra(TagsActivity.TAGS, tags);
        intent.putExtra(TagsActivity.TITLE, "TagsActivity");
        startActivityForResult(intent, 1);*/
    }
}
