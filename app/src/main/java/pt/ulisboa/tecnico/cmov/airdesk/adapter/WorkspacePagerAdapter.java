package pt.ulisboa.tecnico.cmov.airdesk.adapter;

import pt.ulisboa.tecnico.cmov.airdesk.OwnedFragment;
import pt.ulisboa.tecnico.cmov.airdesk.ForeignFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

public class WorkspacePagerAdapter extends FragmentPagerAdapter {

    private static final String[] names = {"Owned", "Foreign"};
    private static final int OWNED = 0;
    private static final int FOREIGN = 1;

    public WorkspacePagerAdapter(FragmentManager fm){
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return names[position];
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 2;
    }

    @Override
    public Fragment getItem(int index) {
        switch (index) {
            case OWNED:
                return new OwnedFragment();
            case FOREIGN:
                return new ForeignFragment();
            default:
                Log.e("Testing", "Deu erro");
        }
        return null;
    }
}
