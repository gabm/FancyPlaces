package com.example.gabm.fancyplaces;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.Time;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by gabm on 29.12.14.
 */
public class LocationHandler implements LocationListener {

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static final int TEN_MINUTES = 1000 * 60 * 10;
    private static final int THIRTY_SECONDS = 1000 * 30;
    private static final float MIN_ACCURACY = 500;
    private static Location curLocation = null;
    List<String> locationProviders = null;
    private Boolean searchingForLocation = false;
    private android.location.LocationManager locationManager = null;
    private OnLocationUpdatedListener onLocationUpdatedListener = null;
    private Timer timeoutTimer = null;
    private Activity parentActivity = null;

    public LocationHandler(Activity activity) {
        parentActivity = activity;
        locationManager = (android.location.LocationManager) parentActivity.getSystemService(Context.LOCATION_SERVICE);

        locationProviders = locationManager.getProviders(true);

        initLocation();
    }

    public Location getCurLocation() {
        return curLocation;
    }

    public void updateLocation() {
        startLocationUpdate();
    }

    protected void initLocation() {
        Location netLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Location gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        Boolean initializedLoc = false;

        if (isValidLocation(netLoc)) {
            curLocation = netLoc;
            initializedLoc = true;
        }

        if (isValidLocation(gpsLoc)) {
            curLocation = gpsLoc;
            initializedLoc = true;
        }

        if (initializedLoc)
            onLocationChanged(curLocation);
    }

    protected Boolean isValidLocation(Location location) {
        if (location == null)
            return false;

        Time now = new Time();
        now.setToNow();

        return now.toMillis(true) - location.getTime() <= TEN_MINUTES;

    }

    public void setOnLocationUpdatedListener(OnLocationUpdatedListener locationUpdatedListener) {
        onLocationUpdatedListener = locationUpdatedListener;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    public Boolean requireNewLocationUpdate() {
        if (curLocation != null) {
            Time now = new Time();
            now.setToNow();
            if (now.toMillis(true) - curLocation.getTime() > TWO_MINUTES)
                return true;

            if (curLocation.getAccuracy() > MIN_ACCURACY)
                return true;
        } else {
            return true;
        }

        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);

    }

    @Override
    public void onLocationChanged(Location location) {

        if (isBetterLocation(location, curLocation)) {
            curLocation = location;
            if (onLocationUpdatedListener != null)
                onLocationUpdatedListener.onLocationUpdated(curLocation);
        }

        if (isValidLocation(curLocation))
            stopLocationUpdate();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private void startLocationUpdate() {
        if (locationProviders.isEmpty())
            return;

        // search for new location
        requestLocationUpdateOnUiThread();

        // start timeout
        timeoutTimer = new Timer();
        timeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopLocationUpdate();
            }
        }, TWO_MINUTES);

        searchingForLocation = true;
    }

    private void requestLocationUpdateOnUiThread() {
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < locationProviders.size(); i++)
                    locationManager.requestLocationUpdates(locationProviders.get(i), 0, 0, LocationHandler.this);

            }
        });
    }

    private void stopLocationUpdate() {
        locationManager.removeUpdates(this);
        searchingForLocation = false;
        stopTimer();
    }

    private void stopTimer() {
        if (timeoutTimer == null)
            return;

        timeoutTimer.cancel();
        timeoutTimer.purge();
        timeoutTimer = null;
    }

    public void onResume() {
        if (searchingForLocation)
            startLocationUpdate();
    }

    public void onPause() {
        Boolean stillSearching = searchingForLocation;

        stopLocationUpdate();

        searchingForLocation = stillSearching;
    }

    public void onSaveInstanceState(Bundle bundle) {
        bundle.putBoolean("searchingForLocation", searchingForLocation);
    }

    public void onRestoreInstanceState(Bundle bundle) {
        searchingForLocation = bundle.getBoolean("searchingForLocation");
    }

    public interface OnLocationUpdatedListener {
        void onLocationUpdated(Location location);
    }
}
