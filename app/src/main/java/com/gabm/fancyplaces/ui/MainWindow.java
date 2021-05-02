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

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
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
import com.gabm.fancyplaces.functional.FancyPlacesArrayAdapter;
import com.gabm.fancyplaces.functional.FancyPlacesDatabase;
import com.gabm.fancyplaces.functional.io.GPXExporter;
import com.gabm.fancyplaces.functional.io.GPXImporterSax;
import com.gabm.fancyplaces.functional.IOnListModeChangeListener;
import com.gabm.fancyplaces.functional.MainWindowViewpagerAdapter;
import com.gabm.fancyplaces.functional.OnFancyPlaceSelectedListener;
import com.gabm.fancyplaces.functional.Utilities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gabm on 15/05/15.
 */


public class MainWindow extends AppCompatActivity implements OnFancyPlaceSelectedListener, IOnListModeChangeListener {
    // gps-permission stuff
    private static final int REQUEST_ID_READ_GPS = 21;
    private static final String PERMISSION_READ_GPS = Manifest.permission.ACCESS_FINE_LOCATION;
    int RESULT_NO_PERMISSIONS = -22;

    public static int REQUEST_SHOW_EDIT_PLACE = 0;
    public static int REQUEST_FILE_SELECTION = 1;
    private static FancyPlacesApplication curAppContext = null;
    public FancyPlacesArrayAdapter fancyPlaceArrayAdapter = null;
    ViewPager pager;
    MainWindowViewpagerAdapter viewpagerAdapter;
    SlidingTabLayout tabs;
    private FancyPlacesDatabase fancyPlacesDatabase = null;
    private LFPState curState = new LFPState();
    private ArrayList<FancyPlace> fancyPlaces = null;
    private FPListView fpListView = null;
    private Bundle firstSavedInstanceState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        curAppContext = (FancyPlacesApplication) getApplicationContext();

        if (FancyPlacesApplication.getLocationHandler(getApplication()) == null) {
                // if app wants to display my logcation: ask for permissions
                if (ActivityCompat.checkSelfPermission(this, PERMISSION_READ_GPS)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermission(PERMISSION_READ_GPS, REQUEST_ID_READ_GPS);
                    firstSavedInstanceState = savedInstanceState;
                    return;
                }
            }

        onCreateWithPermission(savedInstanceState);
    }

    protected void onCreateWithPermission(Bundle savedInstanceState) {

        setContentView(R.layout.activity_main_window);

        setDefaultTitle();

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
        fancyPlaceArrayAdapter = new FancyPlacesArrayAdapter(getApplicationContext(), R.layout.list_item_fancy_place, fancyPlaces);

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

        // set current menu
        curState.curMenu = R.menu.menu_main_window;

        // check if we got called through an intent
        Uri u = getIntent().getData();
        if (u != null)
            loadFromFile(u.getPath());

    }

    private void requestPermission(final String permission, final int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_READ_GPS: {
                if (isGrantSuccess(grantResults)) {
                    // don-t ask again
                    onCreateWithPermission(firstSavedInstanceState);
                    firstSavedInstanceState = null;
                } else {
                    Toast.makeText(this, R.string.permission_error, Toast.LENGTH_LONG).show();
                    setResult(RESULT_NO_PERMISSIONS, null);
                    finish();
                    return;
                }
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean isGrantSuccess(int[] grantResults) {
        boolean success = (grantResults != null)
                && (grantResults.length > 0)
                && (grantResults[0] == PackageManager.PERMISSION_GRANTED);

        return success;
    }

    protected void setDefaultTitle() {
        String debugTitle = getResources().getString(R.string.debug_title);
        if (!debugTitle.equals(""))
            setTitle(debugTitle);
        else
            setTitle(getResources().getString(R.string.title_activity_list_fancy_places));
    }

    private List<TabItem> createTabList() {
        ArrayList<TabItem> tabList = new ArrayList<>();

        // add to list
        fpListView = FPListView.newInstance();
        tabList.add(fpListView);
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

    protected void updateFPDatabase(List<FancyPlace> fancyPlaceList)
    {
        for (FancyPlace fp : fancyPlaceList)
            updateFPDatabase(fp);
    }

    protected void updateFPDatabase(FancyPlace fancyPlace)
    {
        // move image to appropriate location
        ImageFile originalImage = fancyPlace.getImage();
        fancyPlace.setImage(fancyPlace.getImage().copy(getFilesDir().getAbsolutePath() + File.separator + Utilities.shuffleFileName("IMG_", ".png")));
        originalImage.delete();

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
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_SHOW_EDIT_PLACE) {
            Bundle res = data.getExtras();
            FancyPlace fancyPlace = res.getParcelable("data");

            if (resultCode == ShowEditPlace.RESULT_DATA_CHANGED) {

                updateFPDatabase(fancyPlace);
                // clean up
                if (curState.OriginalImageFile != null)
                    curState.OriginalImageFile.delete();
            } else {
                fancyPlace.setImage(curState.OriginalImageFile);
            }
            (new ImageFile(com.gabm.fancyplaces.FancyPlacesApplication.TMP_IMAGE_FULL_PATH)).delete();

        } else if (requestCode == REQUEST_FILE_SELECTION)
        {
            if (resultCode == RESULT_OK)
                loadFromFile(data.getData().getPath());

        }
    }

    protected void loadFromFile(final String path)
    {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // import the places
                        GPXImporterSax gpxImporterSax = new GPXImporterSax();
                        List<FancyPlace> fpList = gpxImporterSax.ReadFancyPlaces(path);
                        if (!fpList.isEmpty())
                        {
                            updateFPDatabase(fpList);
                            Toast.makeText(getApplicationContext(), getString(R.string.fp_import_successful), Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.fp_import_failed), Toast.LENGTH_LONG).show();
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //ignore the places
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.FPAlertDialogStyle);
        builder.setMessage(R.string.alert_confirm_import)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener)
                .show();
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

        TextView content = (TextView) messageView.findViewById(R.id.about_content);
        content.setText(Html.fromHtml(readText(R.raw.about_content)));
        content.setMovementMethod(LinkMovementMethod.getInstance());

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
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        getMenuInflater().inflate(curState.curMenu, menu);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_window_toolbar);
        if (curState.curMenu == R.menu.menu_main_window_multi_select) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setTitle(getString(R.string.main_multi_selection_title));
            toolbar.setBackgroundColor(getResources().getColor(R.color.ColorPrimaryDark));
            int noOfChild = toolbar.getChildCount();
            View view;

            // animate toolbar elements
            for (int i = 1; i < noOfChild; i++) {
                view = toolbar.getChildAt(i);
                view.setAlpha(0);
                view.setScaleY(0);
                view.setPivotY((float) 0.5 * view.getHeight());
                view.animate().setDuration(200).scaleY(1).alpha(1);
            }

        } else if (curState.curMenu == R.menu.menu_main_window) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            setDefaultTitle();
            toolbar.setBackgroundColor(getResources().getColor(R.color.ColorPrimary));
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                // set mode back to normal
                fpListView.setMultiSelectMode(IOnListModeChangeListener.MODE_NORMAL);
                return true;

            case R.id.main_window_about:
                showAbout();
                return true;

            case R.id.main_window_delete:
                // get selected list
                final List<FancyPlace> fpList = fancyPlaceArrayAdapter.getSelectedFancyPlaces();

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                // delete them
                                for (int i = 0; i < fpList.size(); i++) {
                                    fancyPlaceArrayAdapter.remove(fpList.get(i));
                                    fancyPlacesDatabase.deleteFancyPlace(fpList.get(i), true);
                                }

                                // set mode back to normal
                                fpListView.setMultiSelectMode(IOnListModeChangeListener.MODE_NORMAL);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.FPAlertDialogStyle);
                builder.setMessage(R.string.alert_want_to_delete)
                        .setPositiveButton(R.string.yes, dialogClickListener)
                        .setNegativeButton(R.string.no, dialogClickListener)
                        .show();

                return true;

            case R.id.main_window_share:
                GPXExporter exporter = new GPXExporter();

                File exportFile = new File(FancyPlacesApplication.EXTERNAL_EXPORT_DIR, Utilities.shuffleFileName("FancyPlaces_", "") + ".zip");
                if (exporter.WriteToFile(fancyPlaceArrayAdapter.getSelectedFancyPlaces(), exportFile.getAbsolutePath())) {
                    Toast.makeText(getApplicationContext(), getString(R.string.gpx_export_successful) + exportFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.gpx_export_failed), Toast.LENGTH_LONG).show();

                }

                // set mode back to normal
                fpListView.setMultiSelectMode(IOnListModeChangeListener.MODE_NORMAL);
                return true;
            case R.id.main_window_import:
                showFileSelector();
                return true;
        }

        return false;
    }

    protected void showFileSelector() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/zip");
        startActivityForResult(intent, REQUEST_FILE_SELECTION);
    }

    @Override
    public void onBackPressed() {
        if (curState.curMenu == R.menu.menu_main_window_multi_select) {
            // set mode back to normal mode
            fpListView.setMultiSelectMode(IOnListModeChangeListener.MODE_NORMAL);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onListModeChange(int newMode) {
        if (newMode == IOnListModeChangeListener.MODE_NORMAL)
            curState.curMenu = R.menu.menu_main_window;
        else if (newMode == IOnListModeChangeListener.MODE_MULTI_SELECT)
            curState.curMenu = R.menu.menu_main_window_multi_select;

        invalidateOptionsMenu();

    }
}
