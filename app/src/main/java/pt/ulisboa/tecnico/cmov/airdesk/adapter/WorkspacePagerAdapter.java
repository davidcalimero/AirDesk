package pt.ulisboa.tecnico.cmov.airdesk.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.fragment.ForeignFragment;
import pt.ulisboa.tecnico.cmov.airdesk.fragment.OwnedFragment;

public class WorkspacePagerAdapter extends FragmentPagerAdapter {

    private static final int OWNED = 0;
    private static final int FOREIGN = 1;

    private Context context;

    public WorkspacePagerAdapter(FragmentManager fm, Context c) {
        super(fm);
        context = c;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case OWNED:
                return context.getString(R.string.owned);
            case FOREIGN:
                return context.getString(R.string.foreign);
            default:
                return "";
        }
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
                Log.e("WorkspacePagerAdapter", "Wrong ID");
                return null;
        }
    }
}
