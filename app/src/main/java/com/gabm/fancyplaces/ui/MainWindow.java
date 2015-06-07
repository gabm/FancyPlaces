/*
 * Copyright (C) 2015 Matthias Gabriel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gabm.fancyplaces.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gabm.fancyplaces.FancyPlacesApplication;
import com.gabm.fancyplaces.R;
import com.gabm.fancyplaces.data.FancyPlace;
import com.gabm.fancyplaces.data.ImageFile;
import com.gabm.fancyplaces.functional.FancyPlaceListViewAdapter;
import com.gabm.fancyplaces.functional.FancyPlacesDatabase;
import com.gabm.fancyplaces.functional.MainWindowViewpagerAdapter;
import com.gabm.fancyplaces.functional.OnFancyPlaceSelectedListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by gabm on 15/05/15.
 */


public class MainWindow extends AppCompatActivity implements OnFancyPlaceSelectedListener {

    public static int REQUEST_SHOW_EDIT_PLACE = 0;
    private static FancyPlacesApplication curAppContext = null;
    public FancyPlaceListViewAdapter fancyPlaceArrayAdapter = null;
    ViewPager pager;
    MainWindowViewpagerAdapter viewpagerAdapter;
    SlidingTabLayout tabs;
    private FancyPlacesDatabase fancyPlacesDatabase = null;
    private LFPState curState = new LFPState();
    private ArrayList<FancyPlace> fancyPlaces = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_window);

        curAppContext = (FancyPlacesApplication) getApplicationContext();

        // inflate toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_window_toolbar);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= 19)
        {
            int padding_right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
            toolbar.setPadding(toolbar.getPaddingLeft(), curAppContext.getStatusBarHeight(), padding_right, 0);
        }

        // store data
        fancyPlacesDatabase = new FancyPlacesDatabase(getApplicationContext());
        fancyPlacesDatabase.open();
        fancyPlaces = (ArrayList<FancyPlace>) fancyPlacesDatabase.getAllFancyPlaces();
        fancyPlaceArrayAdapter = new FancyPlaceListViewAdapter(getApplicationContext(), R.layout.list_item_fancy_place, fancyPlaces);

        ImageFile.curAppContext = curAppContext;

        // viewpager
        viewpagerAdapter = new MainWindowViewpagerAdapter(getApplicationContext(), getSupportFragmentManager(), createTabList());
        pager = (ViewPager) findViewById(R.id.main_window_viewpager);
        pager.setAdapter(viewpagerAdapter);

        // Asiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.main_window_tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        // handle sharing data
        /*
        Uri uri = getIntent().getData();
        if (uri != null) {
            FancyPlace fp = FancyPlace.loadFromFile(getContentResolver(), uri);
            showSEPActivityForResult(getApplicationContext(), fp, ShowEditPlace.MODE_PREVIEW);
        }*/
    }

    private List<TabItem> createTabList() {
        ArrayList<TabItem> tabList = new ArrayList<>();

        // add to list
        tabList.add(FPListView.newInstance());
        tabList.add(FPOsmDroidView.newInstance());
        return tabList;
    }


    public void onFancyPlaceSelected(int id, int intent) {
        FancyPlace fp = null;
        if (id < fancyPlaces.size())
            fp = fancyPlaceArrayAdapter.getItem(id);

        switch (intent) {
            case OnFancyPlaceSelectedListener.INTENT_VIEW:
                showSEPActivityForResult(getApplicationContext(), fp.clone(), ShowEditPlace.MODE_VIEW);
                break;
            case OnFancyPlaceSelectedListener.INTENT_EDIT:
                showSEPActivityForResult(getApplicationContext(), fp.clone(), ShowEditPlace.MODE_EDIT);
                break;
            case OnFancyPlaceSelectedListener.INTENT_DELETE:
                fancyPlaceArrayAdapter.remove(fp);
                fancyPlacesDatabase.deleteFancyPlace(fp, true);
                break;
            case OnFancyPlaceSelectedListener.INTENT_SHARE:
                /*
                try {
                    String fileName = fp.saveToFile(getApplicationContext().getExternalCacheDir().getAbsolutePath());

                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);

                    sharingIntent.setType("application/fancyplace");
                    sharingIntent.putExtra(
                            Intent.EXTRA_STREAM,
                            Uri.parse("file://" + fileName));
                    startActivity(Intent.createChooser(sharingIntent, "Share FancyPlace using"));
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                break;
            case OnFancyPlaceSelectedListener.INTENT_CREATE_NEW:
                showSEPActivityForResult(getApplicationContext(), new FancyPlace(), ShowEditPlace.MODE_EDIT);
                break;
        }
    }

    protected void copyImageToTmp(FancyPlace fp) {
        // copy img to tmp location
        if (fp.getImage().exists()) {
            curState.OriginalImageFile = fp.getImage();
            fp.setImage(curState.OriginalImageFile.copy(com.gabm.fancyplaces.FancyPlacesApplication.TMP_IMAGE_FULL_PATH));
        } else {
            curState.OriginalImageFile = null;
            fp.setImage(new ImageFile(com.gabm.fancyplaces.FancyPlacesApplication.TMP_IMAGE_FULL_PATH));
        }
    }

    protected void showSEPActivityForResult(Context context, FancyPlace fp, int mode) {
        copyImageToTmp(fp);

        Intent intent = new Intent(context, ShowEditPlace.class);
        intent.putExtra("data", (Parcelable) fp);
        intent.putExtra("mode", mode);

        startActivityForResult(intent, REQUEST_SHOW_EDIT_PLACE);
    }

    private String shuffleFileName() {
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return "IMG_" + timeStamp + ".png";
    }


    protected int findElementPosition(long id) {
        int result = -1;
        for (int i = 0; i < fancyPlaceArrayAdapter.getCount(); i++) {
            if (fancyPlaceArrayAdapter.getItem(i).getId() == id) {
                result = i;
                break;
            }

        }
        return result;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_SHOW_EDIT_PLACE) {
            Bundle res = data.getExtras();
            FancyPlace fancyPlace = res.getParcelable("data");

            if (resultCode == ShowEditPlace.RESULT_DATA_CHANGED) {
                // move image to appropriate location
                fancyPlace.setImage(fancyPlace.getImage().copy(getFilesDir().getAbsolutePath() + File.separator + shuffleFileName()));
                if (curState.OriginalImageFile != null)
                    curState.OriginalImageFile.delete();

                // open database connection
                fancyPlacesDatabase.open();


                if (fancyPlace.isInDatabase()) {
                    // update
                    int pos = findElementPosition(fancyPlace.getId());
                    fancyPlacesDatabase.updateFancyPlace(fancyPlace);
                    fancyPlaceArrayAdapter.remove(fancyPlaceArrayAdapter.getItem(pos));
                    fancyPlaceArrayAdapter.insert(fancyPlace, pos);

                } else {
                    // create new
                    fancyPlace = fancyPlacesDatabase.createFancyPlace(fancyPlace);
                    fancyPlaceArrayAdapter.add(fancyPlace);
                }
            } else {
                fancyPlace.setImage(curState.OriginalImageFile);
            }
            (new ImageFile(com.gabm.fancyplaces.FancyPlacesApplication.TMP_IMAGE_FULL_PATH)).delete();

        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putParcelable("state", curState);
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        curState = bundle.getParcelable("state");
    }

    protected void showAbout() {
        // Inflate the about message contents
        View messageView = getLayoutInflater().inflate(R.layout.about_window, null, false);

        TextView description = (TextView) messageView.findViewById(R.id.about_description);
        description.setText(Html.fromHtml(readText(R.raw.about_description)));
        description.setMovementMethod(LinkMovementMethod.getInstance());

        TextView credits = (TextView) messageView.findViewById(R.id.about_credits);
        credits.setText(Html.fromHtml(readText(R.raw.about_credits)));
        credits.setMovementMethod(LinkMovementMethod.getInstance());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
    }

    private String readText(int id) {

        InputStream inputStream = getResources().openRawResource(id);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i;
        try {
            i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return byteArrayOutputStream.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_window, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.main_window_about) {

            showAbout();
            return true;
        }
        return false;
    }
}
