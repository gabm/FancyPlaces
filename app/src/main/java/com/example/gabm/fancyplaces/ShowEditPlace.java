package com.example.gabm.fancyplaces;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;


public class ShowEditPlace extends FragmentActivity implements GoogleMap.OnMapLongClickListener, LocationHandler.OnLocationUpdatedListener {


    static public final int MODE_VIEW = 0;
    static public final int MODE_EDIT = 1;
    static public final int MODE_PREVIEW = 2;
    static public final int RESULT_DATA_NOT_CHANGED = 0;
    static public final int RESULT_DATA_CHANGED = 1;
    static private final int LOCATION_CHANGED_GPS = 0;
    static private final int LOCATION_CHANGED_USER = 1;
    static private final int LOCATION_CHANGED_INIT = 2;
    static private final int REQUEST_IMAGE_CAPTURE = 0;
    static private MyFancyPlacesApplication curAppContext = null;
    ViewElements currentViewElements = new ViewElements();
    private SEPState currentState = new SEPState();
    private LocationHandler locationHandler = null;
    private MapFragmentHandler mapHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_edit_place);

        curAppContext = (MyFancyPlacesApplication) getApplicationContext();

        updateElementIDs();

        currentViewElements.mapFragment.setListener(
                new WorkaroundMapFragment.OnTouchListener() {
                    @Override
                    public void onTouch() {
                        ((ScrollView) findViewById(R.id.sep_scroll_view)).requestDisallowInterceptTouchEvent(true);
                    }
                });

        mapHandler = new MapFragmentHandler(currentViewElements.mapFragment.getMap());
        mapHandler.setOnMapLongClickListener(this);

        locationHandler = new LocationHandler(this);
        locationHandler.setOnLocationUpdatedListener(this);

        currentState.result_code = RESULT_DATA_NOT_CHANGED;

        if (savedInstanceState == null) {
            setStateFromIntent(getIntent());
            onActivityModeChanged();
        }
    }

    protected void onNotesChanged() {
        if (currentState.mode == MODE_EDIT) {
            currentViewElements.notesEditText.setText(currentState.data.getNotes());
        } else if (currentState.mode == MODE_VIEW) {
            currentViewElements.notesTextView.setText(currentState.data.getNotes());
        }

    }

    protected void onImageChanged() {
        if (currentState.image != null) {
            currentViewElements.imageView.setImageBitmap(currentState.image);
        }
    }

    protected void onLocationChanged(int reason) {
        String title = getString(R.string.new_fancy_place);
        if (currentState.data.getTitle().equals(""))
            title = currentState.data.getTitle();

        double lat = Double.valueOf(currentState.data.getLocationLat());
        double lng = Double.valueOf(currentState.data.getLocationLong());

        setMarker(lat, lng, title);


        switch (reason) {
            case LOCATION_CHANGED_GPS:
                mapHandler.animateCamera(lat, lng, MyFancyPlacesApplication.MAP_DEFAULT_ZOOM_NEAR, MyFancyPlacesApplication.MAP_DEFAULT_DURATION);
                break;
            case LOCATION_CHANGED_USER:
                mapHandler.animateCamera(lat, lng, MyFancyPlacesApplication.MAP_DEFAULT_DURATION);
                break;
            case LOCATION_CHANGED_INIT:
                mapHandler.setCamera(lat, lng, MyFancyPlacesApplication.MAP_DEFAULT_ZOOM_NEAR);
                break;
        }
    }

    protected void onActivityModeChanged() {

        FancyPlace data = currentState.data;

        SEPState.ViewElementVisibility visibility = currentState.viewElementVisibility;
        ViewElements viewElements = currentViewElements;


        int mode = currentState.mode;

        ///////////////////////////////////
        // Title
        if (mode == MODE_VIEW) {
            setTitle(data.getTitle());
            visibility.titleCardVisibility = View.GONE;

        } else if (mode == MODE_EDIT) {

            setTitle(R.string.edit_fancy_place);
            viewElements.titleEditText.setText(data.getTitle());
            visibility.titleCardVisibility = View.VISIBLE;
            visibility.titleEditTextVisibility = View.VISIBLE;
        } else if (mode == MODE_PREVIEW) {
            setTitle(R.string.preview_fancy_place);
            viewElements.titleEditText.setText(data.getTitle());
            visibility.titleCardVisibility = View.VISIBLE;
            visibility.titleEditTextVisibility = View.VISIBLE;
        }

        ///////////////////////////////////
        // Map
        visibility.mapCardVisibility = View.VISIBLE;
        if (data.isLocationSet()) {
            onLocationChanged(LOCATION_CHANGED_INIT);
        } else if (!data.isLocationSet() && mode == MODE_EDIT) {
            requestLocationUpdate();
        }

        if (mode == MODE_EDIT || mode == MODE_PREVIEW) {
            visibility.mapUpdateButtonVisibility = View.VISIBLE;
        } else {
            visibility.mapUpdateButtonVisibility = View.GONE;
        }


        ///////////////////////////////////
        // Notes
        visibility.notesCardVisibility = View.VISIBLE;
        if (mode == MODE_VIEW) {
            visibility.notesTextViewVisibility = View.VISIBLE;
            visibility.notesEditTextVisibility = View.GONE;

        } else if (mode == MODE_EDIT || mode == MODE_PREVIEW) {
            visibility.notesTextViewVisibility = View.GONE;
            visibility.notesEditTextVisibility = View.VISIBLE;
        }
        onNotesChanged();

        ///////////////////////////////////
        // Image
        visibility.imageCardVisibility = View.VISIBLE;
        if (mode == MODE_VIEW) {

            if (currentState.image == null)
                visibility.imageCardVisibility = View.GONE;

            visibility.imageTakePhotoButtonVisibility = View.GONE;
        } else if (mode == MODE_EDIT || mode == MODE_PREVIEW) {
            visibility.imageTakePhotoButtonVisibility = View.VISIBLE;

        }


        if (currentState.image != null) {
            visibility.imageViewVisibility = View.VISIBLE;

        } else {
            visibility.imageViewVisibility = View.GONE;
        }

        onImageChanged();

        ///////////////////////////////////
        // Buttons
        if (mode == MODE_VIEW) {
            visibility.buttonVisibility = View.GONE;
        } else if (mode == MODE_EDIT || mode == MODE_PREVIEW) {
            visibility.buttonVisibility = View.VISIBLE;
        }

        onVisibilitiesChanged();
    }

    protected void requestLocationUpdate() {
        locationHandler.updateLocation();
        Toast.makeText(this, R.string.updating_location, Toast.LENGTH_SHORT).show();
    }

    protected void onVisibilitiesChanged() {
        SEPState.ViewElementVisibility viewElementVisibility = currentState.viewElementVisibility;
        ViewElements viewElements = currentViewElements;

        // title
        viewElements.titleCard.setVisibility(viewElementVisibility.titleCardVisibility);
        viewElements.titleEditText.setVisibility(viewElementVisibility.titleEditTextVisibility);

        // map
        viewElements.mapCard.setVisibility(viewElementVisibility.mapCardVisibility);
        viewElements.mapUpdateButton.setVisibility(viewElementVisibility.mapUpdateButtonVisibility);

        // notes
        viewElements.notesCard.setVisibility(viewElementVisibility.notesCardVisibility);
        viewElements.notesTextView.setVisibility(viewElementVisibility.notesTextViewVisibility);
        viewElements.notesEditText.setVisibility(viewElementVisibility.notesEditTextVisibility);

        // image
        viewElements.imageCard.setVisibility(viewElementVisibility.imageCardVisibility);
        viewElements.imageView.setVisibility(viewElementVisibility.imageViewVisibility);
        viewElements.imageTakePhotoButton.setVisibility(viewElementVisibility.imageTakePhotoButtonVisibility);

        // buttons
        viewElements.buttons.setVisibility(viewElementVisibility.buttonVisibility);
    }

    protected void updateElementIDs() {
        // title
        currentViewElements.titleCard = (VerticalCardView) findViewById(R.id.sep_title_card);
        currentViewElements.titleEditText = (EditText) findViewById(R.id.sep_title_edit_text);

        // map
        currentViewElements.mapCard = (VerticalCardView) findViewById(R.id.sep_map_card);
        currentViewElements.mapFragment = ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.sep_map));
        currentViewElements.mapUpdateButton = (Button) findViewById(R.id.sep_map_update_button);

        // notes
        currentViewElements.notesCard = (VerticalCardView) findViewById(R.id.sep_notes_card);
        currentViewElements.notesEditText = (EditText) findViewById(R.id.sep_notes_edit_text);
        currentViewElements.notesTextView = (TextView) findViewById(R.id.sep_notes_text_view);

        // image
        currentViewElements.imageCard = (VerticalCardView) findViewById(R.id.sep_image_card);
        currentViewElements.imageView = (ImageView) findViewById(R.id.sep_image);
        currentViewElements.imageTakePhotoButton = (Button) findViewById(R.id.sep_image_photo_button);

        // buttons
        currentViewElements.buttons = (LinearLayout) findViewById(R.id.sep_buttons);

        // scrollview
        currentViewElements.scrollView = (ObservableScrollView) findViewById(R.id.sep_scroll_view);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (currentState.mode != MODE_EDIT)
            return;

        currentState.data.setLocationLat(String.valueOf(latLng.latitude));
        currentState.data.setLocationLong(String.valueOf(latLng.longitude));

        onLocationChanged(LOCATION_CHANGED_USER);
    }

    protected void setStateFromIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        currentState.mode = extras.getInt("mode");
        currentState.data = extras.getParcelable("data");
        currentState.image = currentState.data.getImage().loadFullSizeImage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_edit_place, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        saveInputFieldsToState();
        bundle.putParcelable("state", currentState);
        locationHandler.onSaveInstanceState(bundle);
        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        currentState = bundle.getParcelable("state");
        onActivityModeChanged();
        locationHandler.onRestoreInstanceState(bundle);
        super.onRestoreInstanceState(bundle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            if (currentState.mode == MODE_VIEW || currentState.mode == MODE_PREVIEW) {
                currentState.mode = MODE_EDIT;
                onActivityModeChanged();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        Bundle conData = new Bundle();
        conData.putParcelable("data", currentState.data);
        Intent intent = new Intent();
        intent.putExtras(conData);

        setResult(currentState.result_code, intent);
        super.finish();
    }

    protected void saveInputFieldsToState() {
        if (currentState.mode == MODE_EDIT || currentState.mode == MODE_PREVIEW) {
            currentState.data.setTitle(currentViewElements.titleEditText.getText().toString());
            currentState.data.setNotes(currentViewElements.notesEditText.getText().toString());
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sep_button_save:
                saveInputFieldsToState();
                if (!currentState.data.isValid())
                    return;

                currentState.result_code = RESULT_DATA_CHANGED;
                finish();
                break;

            case R.id.sep_image_photo_button:
                // create Intent to take a picture and return control to the calling application
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse("file://" + currentState.data.getImage().getFileName())); // set the image file name


                // start the image capture Intent
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                break;
            case R.id.sep_map_update_button:
                requestLocationUpdate();
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            currentState.data.getImage().scaleDown(MyFancyPlacesApplication.TARGET_PIX_SIZE);
            currentState.image = currentState.data.getImage().loadFullSizeImage();
            onActivityModeChanged();
        }
    }

    @Override
    public void onResume() {
        locationHandler.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        locationHandler.onPause();

        super.onPause();
    }

    @Override
    public void onLocationUpdated(Location location) {
        currentState.data.setLocationLat(String.valueOf(location.getLatitude()));
        currentState.data.setLocationLong(String.valueOf(location.getLongitude()));

        onLocationChanged(LOCATION_CHANGED_GPS);
    }

    protected void setMarker(double lat, double lng, String title) {
        mapHandler.clearMarkers();
        mapHandler.addMarker(lat, lng, title);
        mapHandler.animateCamera(lat, lng, MyFancyPlacesApplication.MAP_DEFAULT_ZOOM_NEAR, MyFancyPlacesApplication.MAP_DEFAULT_DURATION);
    }

    protected class ViewElements {
        // title
        public VerticalCardView titleCard = null;
        public EditText titleEditText = null;

        // map
        public VerticalCardView mapCard = null;
        public WorkaroundMapFragment mapFragment = null;
        public Button mapUpdateButton = null;

        // notes
        public VerticalCardView notesCard = null;
        public TextView notesTextView = null;
        public EditText notesEditText = null;


        // image
        public VerticalCardView imageCard = null;
        public ImageView imageView = null;
        public Button imageTakePhotoButton = null;

        // buttons
        public LinearLayout buttons = null;

        // scroll view
        public ObservableScrollView scrollView = null;
    }
}
