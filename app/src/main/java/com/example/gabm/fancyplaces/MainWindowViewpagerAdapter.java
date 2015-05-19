package com.example.gabm.fancyplaces;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hp1 on 21-01-2015.
 */


public class MainWindowViewpagerAdapter extends FragmentStatePagerAdapter {

    private List<TabItem> tabItemList = new ArrayList<>();

    // Build a Constructor and assign the passed Values to appropriate values in the class
    public MainWindowViewpagerAdapter(FragmentManager fm, List<TabItem> newTabItemList) {
        super(fm);

        tabItemList = newTabItemList;

    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
        return tabItemList.get(position);
    }

    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public CharSequence getPageTitle(int position) {
        return tabItemList.get(position).getTitle();
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return tabItemList.size();
    }
}
