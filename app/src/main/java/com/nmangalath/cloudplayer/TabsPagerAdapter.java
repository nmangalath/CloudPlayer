package com.nmangalath.cloudplayer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Narayanan on 26/07/2015.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {

    PlayFragment pf;
    public TabsPagerAdapter(FragmentManager fm) {

        super(fm);
    }

    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 1:
                // Top Rated fragment activity
                return new ArtistsFragment();
            case 2:
                // Games fragment activity
                return new SongsFragment();
            case 3:
                // Movies fragment activity
                return new RagasFragment();
            case 0:
                // Movies fragment activity
                pf = new PlayFragment();
                return pf;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public int getItemPosition(Object object) {
//        return super.getItemPosition(object);
        return POSITION_NONE;
    }
}
