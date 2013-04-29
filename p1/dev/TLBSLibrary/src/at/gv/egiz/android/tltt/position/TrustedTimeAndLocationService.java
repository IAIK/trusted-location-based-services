package at.gv.egiz.android.tltt.position;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * TrustedTimeAndLocationService determines the current best location of the
 * device.
 * 
 * After binding to this Service, the location can be determined by calling
 * {@link #getCurrentLocation()}.
 * 
 */
public class TrustedTimeAndLocationService extends Service {

	String tag = "Egiz TrustedTimeAndLocationService";

	/**
	 * <a href=
	 * "http://developer.android.com/guide/topics/fundamentals/bound-services.html#Binder"
	 * >Extends the Binder class.</a> Enables direct access to the Service
	 * public methods.
	 * 
	 */
	protected TrustedTimeAndLocationServiceBinder trustedTimeAndLocationServerBinder;
	/** The current best location. **/
	protected Location currentLocation;
	/** Accesses the systems location sensors. **/
	LocationManager locationManager;
	/** Listener for location updates. **/
	LocationListener locationListener;

	private static final int TIME_DELTA = 1000 * 60 * 2;

	@Override
	public IBinder onBind(Intent arg0) {
		if (trustedTimeAndLocationServerBinder == null) {
			trustedTimeAndLocationServerBinder = new TrustedTimeAndLocationServiceBinder(
					this);
		}
		init(); // init the location and start listening for location updates
		return trustedTimeAndLocationServerBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(tag, "onUnbind()");
		shutdown();
		return super.onUnbind(intent);
	}

	/**
	 * Waits until a location is available, then returns the current best
	 * location.
	 * 
	 * @return the current best location
	 */
	public Location getCurrentLocation() {
		// get location
		while (currentLocation == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return currentLocation;
	}

	private void init() {
		currentLocation = null;
		initLocationSensors();

	}

	/**
	 * Initializes the local instance of LocationManager and adds a custom
	 * implementation of LocationListener. If a location update is better than
	 * the currently stored best location, currentLocation is assigned the new
	 * best location.
	 */
	protected void initLocationSensors() {
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				Log.d(tag, "onLocationChanged()");
				if (isBetterLocation(location, currentLocation)) {
					currentLocation = location;
				}
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {

			}

			public void onProviderEnabled(String provider) {
				Log.d(tag, "Enabled Provider: " + provider);
			}

			public void onProviderDisabled(String provider) {
				Log.d(tag, "Disabled Provider: " + provider);
			}
		};

		// Register the listener with the Location Manager to receive location
		// updates
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, locationListener);

		// to speed up initialization and to avoid annoying waiting periods for
		// the user, start with the lastKnownLocation
		Location lastKnownLocationGPS = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		Location lastKnownLocationNetwork = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (isBetterLocation(lastKnownLocationGPS, lastKnownLocationNetwork)) {
			currentLocation = lastKnownLocationGPS;
		} else {
			currentLocation = lastKnownLocationNetwork;
		}
		Log.d(tag, "done initializing...");
	}

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 * @return <code>true</code> if new location is better than current best
	 *         location <code>false</code> otherwise.
	 * 
	 */
	protected boolean isBetterLocation(Location location,
			Location currentBestLocation) {
		if (location == null) {
			Log.d(tag, "The new location fix has been null. Take the old one.");
			return false;
		}
		if (currentBestLocation == null) {
			// A new location is always better than no location
			Log.d(tag,
					"Currently there is no location stored. Take the new location.");
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TIME_DELTA;
		boolean isSignificantlyOlder = timeDelta < -TIME_DELTA;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	/**
	 * Stop listening for location updates.
	 */
	public void shutdown() {
		locationManager.removeUpdates(locationListener);
	}

}
