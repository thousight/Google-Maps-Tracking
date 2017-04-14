package www.markwen.space.google_maps_tracking;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import www.markwen.space.google_maps_tracking.fragments.GridFragment;
import www.markwen.space.google_maps_tracking.fragments.RecordFragment;

/**
 * Created by markw on 4/13/2017.
 */

public class FragmentPagerItemAdapter extends FragmentStatePagerAdapter {
    private Fragment[] fragments;

    public FragmentPagerItemAdapter(FragmentManager fm) {
        super(fm);
        fragments = new Fragment[2];
        fragments[0] = new RecordFragment();
        fragments[1] = new GridFragment();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }
}
