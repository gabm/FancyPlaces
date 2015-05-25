package com.gabm.fancyplaces.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.gabm.fancyplaces.FancyPlacesApplication;
import com.gabm.fancyplaces.R;
import com.gabm.fancyplaces.data.FancyPlace;
import com.gabm.fancyplaces.data.ImageFile;
import com.gabm.fancyplaces.functional.FancyPlaceListViewAdapter;
import com.gabm.fancyplaces.functional.FancyPlacesDatabase;
import com.gabm.fancyplaces.functional.MainWindowViewpagerAdapter;
import com.gabm.fancyplaces.functional.OnFancyPlaceSelectedListener;

import java.io.File;
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
        toolbar.setPadding(0, curAppContext.getStatusBarHeight(), 0, 0);

        // store data
        fancyPlacesDatabase = new FancyPlacesDatabase(getApplicationContext());
        fancyPlacesDatabase.open();
        fancyPlaces = (ArrayList<FancyPlace>) fancyPlacesDatabase.getAllFancyPlaces();
        fancyPlaceArrayAdapter = new FancyPlaceListViewAdapter(getApplicationContext(), R.layout.list_item_fancy_place, fancyPlaces);

        com.gabm.fancyplaces.FancyPlacesApplication.TMP_IMAGE_FULL_PATH = curAppContext.getExternalCacheDir() + File.separator + com.gabm.fancyplaces.FancyPlacesApplication.TMP_IMAGE_FILENAME;
        ImageFile.curAppContext = curAppContext;

        // viewpager
        viewpagerAdapter = new MainWindowViewpagerAdapter(getSupportFragmentManager(), createTabList());
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

        Uri uri = getIntent().getData();
        if (uri != null) {
            FancyPlace fp = FancyPlace.loadFromFile(getContentResolver(), uri);
            showSEPActivityForResult(getApplicationContext(), fp, ShowEditPlace.MODE_PREVIEW);
        }
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
                }
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

}
