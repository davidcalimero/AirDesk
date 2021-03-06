package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.HashSet;

import pt.ulisboa.tecnico.cmov.airdesk.adapter.WorkspacePagerAdapter;
import pt.ulisboa.tecnico.cmov.airdesk.dto.TextFileDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.WorkspaceDto;
import pt.ulisboa.tecnico.cmov.airdesk.utility.FlowManager;
import pt.ulisboa.tecnico.cmov.airdesk.utility.FlowProxy;
import pt.ulisboa.tecnico.cmov.airdesk.widget.SlidingTabLayout;


public class MainMenu extends AppCompatActivity {

    public static final int SUBSCRIPTIONS = 1;

    public static final String NICKNAME = "nickname";
    public static final String EMAIL = "email";

    private String nickname;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        //Force overflow menu on actionBar
        forceMenuOverflow();

        //Initialize view
        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        WorkspacePagerAdapter adapter = new WorkspacePagerAdapter(getSupportFragmentManager(), getApplicationContext());
        mViewPager.setAdapter(adapter);
        SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);

        //Restore data
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        nickname = bundle.getString(NICKNAME);
        email = bundle.getString(EMAIL);
        setTitle(nickname + ": " + email);
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
        int id = item.getItemId();
        switch (id) {
            case R.id.action_logout:
                Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                intent.putExtra(LogInActivity.LOGOUT, true);
                startActivity(intent);
                finish();
                FlowProxy.getInstance().send_userLeft(getApplicationContext());
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == RESULT_OK) {
            if (requestCode == SUBSCRIPTIONS) {
                FlowManager.setSubscriptions(getApplicationContext(), (HashSet <String>) data.getSerializableExtra(ListActivity.LIST));
                Toast.makeText(getApplicationContext(), getString(R.string.subscriptions_changed_successfully), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onSettingsButtonPressed(View view) {
        WorkspaceDto workspaceDto = new WorkspaceDto();
        workspaceDto.owner = email;
        workspaceDto.name = ((TextView) ((ViewGroup) view.getParent()).findViewById(R.id.groupHeader)).getText().toString();

        Intent intent = new Intent(getApplicationContext(), CreateEditWorkspaceActivity.class);
        intent.putExtra(CreateEditWorkspaceActivity.ACTIVITY_TITLE, workspaceDto.name);
        intent.putExtra(CreateEditWorkspaceActivity.ACTIVITY_MODE, CreateEditWorkspaceActivity.MODE.EDIT);
        intent.putExtra(CreateEditWorkspaceActivity.WORKSPACE_DTO, workspaceDto);
        startActivity(intent);
    }

    public void onAddFileButtonPressed(View view) {
        String workspaceName = ((TextView) ((ViewGroup) view.getParent()).findViewById(R.id.groupHeader)).getText().toString();
        String ownerName = ((TextView) ((ViewGroup) view.getParent()).findViewById(R.id.tagHeader)).getText().toString();

        TextFileDto textFileDto = new TextFileDto();
        textFileDto.owner = ownerName;
        textFileDto.workspace = workspaceName;

        Intent intent = new Intent(getApplicationContext(), CreateEditFileActivity.class);
        intent.putExtra(CreateEditFileActivity.ACTIVITY_TITLE, getString(R.string.create_new_file));
        intent.putExtra(CreateEditFileActivity.ACTIVITY_MODE, CreateEditFileActivity.MODE.CREATE);
        intent.putExtra(CreateEditFileActivity.FILE_DTO, textFileDto);
        startActivity(intent);
    }

    public void onRemoveWorkspaceButtonPressed(View view){
        String workspaceName = ((TextView) ((ViewGroup) view.getParent()).findViewById(R.id.groupHeader)).getText().toString();
        String owner = ((TextView) ((ViewGroup) view.getParent()).findViewById(R.id.tagHeader)).getText().toString();

        WorkspaceDto workspaceDto = new WorkspaceDto();
        workspaceDto.owner = owner;
        workspaceDto.name = workspaceName;

        FlowProxy.getInstance().send_userLeftWorkspace(getApplicationContext(), workspaceDto, null);
    }

    //Force overflow menu on actionBar
    private void forceMenuOverflow() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            Log.e("MainMenu", "Force action bar menu error");
        }
    }
}
