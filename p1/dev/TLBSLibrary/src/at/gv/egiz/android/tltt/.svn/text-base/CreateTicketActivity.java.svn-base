package at.gv.egiz.android.tltt;

import group.pals.android.lib.ui.filechooser.FileChooserActivity;
import group.pals.android.lib.ui.filechooser.io.LocalFile;
import group.pals.android.lib.ui.filechooser.services.IFileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import at.gv.egiz.android.R;
import at.gv.egiz.android.tltt.constants.TLTT;
import at.gv.egiz.android.tltt.position.TrustedTimeAndLocationService;
import at.gv.egiz.android.tltt.position.TrustedTimeAndLocationServiceBinder;
import at.gv.egiz.android.tltt.ticket.LocationTimeTicket;
import at.gv.egiz.android.tltt.ticket.bitmap.BitmapWorkerTask;
import at.gv.egiz.android.tltt.utils.FileUtils;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * Constructs a LocationTimeTicket with the current location, the corresponding
 * timestamp and an optional attachment.
 * 
 * Starting intent: does not require any data
 * 
 * @author sandra.kreuzhuber@iaik.tugraz.at
 * 
 */
public class CreateTicketActivity extends MapActivity {
	String tag = "Egiz CreateTicketActivity";

	private ServiceConnection serviceConnection;
	/**
	 * indicates if the TrustedTimeAndLocationService is currently bound
	 */
	private Boolean isBound = false;
	/**
	 * The service that access the physical location of the device.
	 */
	private TrustedTimeAndLocationService trustedTimeAndLocationService;
	/**
	 * The best location that is currently available.
	 */
	private Location currentBestLocation = null;
	/**
	 * The date corresponding to the currentBestLocation.
	 */
	private Date currentDate = null;
	/**
	 * Base64 encoded content of the file that should be added to the TLTT.
	 */
	private String attachment = null;
	/**
	 * Ending of the added file.
	 */
	private String fileEnding = null;
	/**
	 * The added file.
	 */
	private File file = null;

	// GUI stuff
	private TextView positionInfo = null;
	private TextView accuracyInfo = null;
	private TextView timeInfo = null;
	private MapView mapView = null;
	private MapController mapController = null;

	/**
	 * Handler that periodically queries the TrustedTimeAndLocationService for
	 * location updates.
	 */
	private Handler handler;
	/**
	 * Starts background process to update the location.
	 */
	private Runnable runnable;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ticket);
		this.mapView = (MapView) findViewById(R.id.mapview);
		this.mapView.setBuiltInZoomControls(true);
		this.mapController = mapView.getController();
		this.mapController.setZoom(14); // Zoom 1 is world view
		this.positionInfo = (TextView) findViewById(R.id.positionInfo);
		this.accuracyInfo = (TextView) findViewById(R.id.accuracyInfo);
		this.timeInfo = (TextView) findViewById(R.id.timeInfo);

	}

	@Override
	public void onStart() {
		super.onStart();
		if (enableLocationProvider()) {
			init();
			updateUI(null, new Date());
		}

	}

	@Override
	public void onStop() {
		super.onStop();
		// stop TrustedTimeAndLocationService, location no longer needed
		if (trustedTimeAndLocationService != null) {
			trustedTimeAndLocationService.shutdown();
		}
		doUnbindService();
	}

	/**
	 * Handles the return value of activities started by this activity.
	 * 
	 */
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		// handle return value of the filechooser
		if (requestCode == TLTT.ADD_FILE_TO_TICKET) {
			if (resultCode == RESULT_OK) {

				IFileProvider.FilterMode filterMode = (IFileProvider.FilterMode) intent
						.getSerializableExtra(FileChooserActivity._FilterMode);

				/*
				 * a list of files will always return, if selection mode is
				 * single, the list contains one file
				 */
				List<LocalFile> files = (List<LocalFile>) intent
						.getSerializableExtra(FileChooserActivity._Results);
				for (File f : files) {
					file = f;
				}
				Log.d(tag, "Add file " + file.getPath() + " to LTT...");

				fileEnding = FileUtils.getFileEnding(file.getPath());
				Log.d(tag, "Parsed fileending: " + fileEnding);
				// converts the file to a base64 string
				// set the attachment field for the ticket
				AddFileTask addFileTask = new AddFileTask(
						CreateTicketActivity.this);
				addFileTask.execute(file.getPath());

				if (fileEnding.toLowerCase().contains("jpg".toLowerCase())
						|| fileEnding.toLowerCase().contains(
								"png".toLowerCase())
						|| fileEnding.toLowerCase().contains(
								"jpeg".toLowerCase())) {
					// and display the attachment
					TextView attachmentText = (TextView) findViewById(R.id.textViewAttachment);
					attachmentText.setVisibility(0);
					ImageView attachmentImage = (ImageView) findViewById(R.id.imageAttachment);

					Log.d(tag, "Load bitmap to display jpg/png/jpeg in GUI...");
					BitmapWorkerTask task = new BitmapWorkerTask(
							attachmentImage,
							findViewById(R.id.linearLayoutTicket),
							CreateTicketActivity.this);
					task.execute(file.getPath());
				} else {
					// add a button that enables the user to view the file in a
					// different program
					Button showAttachment = (Button) findViewById(R.id.buttonShowAttachment);
					showAttachment.setVisibility(View.VISIBLE);
				}
			}
			if (resultCode == RESULT_CANCELED) {
				// do nothing
			}
		}
		if (requestCode == TLTT.CHANGE_LOCATION_SETTINGS) {
			if (enableLocationProvider()) {
				init();
				updateUI(null, new Date());
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.create_ticket_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.quit) {
			// quit activity
			setResult(RESULT_CANCELED);
			finish();
			return true;

		} else if (item.getItemId() == R.id.file) {
			// add file to ticket, therefor open the filechooser
			Intent addFileIntent = new Intent(
					CreateTicketActivity.this,
					group.pals.android.lib.ui.filechooser.FileChooserActivity.class);
			Log.d(tag, "start file chooser");
			startActivityForResult(addFileIntent, TLTT.ADD_FILE_TO_TICKET);
			return true;

		} else if (item.getItemId() == R.id.ticket) {
			// create the LTT
			LocationTimeTicket ltt = new LocationTimeTicket(
					currentBestLocation.getLongitude(),
					currentBestLocation.getLatitude(),
					currentBestLocation.getAccuracy(), currentDate, attachment,
					fileEnding);

			// add the ltt in xml format to the intent and return the result
			Intent resultIntent = new Intent();
			resultIntent.putExtra(TLTT.LOCATION_TIME_TICKET,
					ltt.getContentToSign());
			setResult(RESULT_OK, resultIntent);
			finish();
			return true;

		} else
			return super.onOptionsItemSelected(item);

	}

	/**
	 * Setter for the TLTT attachment.
	 * 
	 * @param fileContent
	 *            - base64 String representation of a file.
	 */
	public void setAttachment(String fileContent) {
		attachment = fileContent;
	}

	/**
	 * binds to TrustedTimeAndLocationService, creates a new Handler that
	 * periodically queries the service for location updates
	 */
	private void init() {
		serviceConnection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName className,
					IBinder service) {
				// We've bound to LocalService, cast the IBinder and get
				// LocalService instance
				Log.d(tag,
						"bound to LocalService TrustedTimeAndLocationServer...");
				TrustedTimeAndLocationServiceBinder binder = (TrustedTimeAndLocationServiceBinder) service;
				trustedTimeAndLocationService = binder.getService();

				handler = new Handler();
				runnable = new Runnable() {

					public void run() {
						currentBestLocation = trustedTimeAndLocationService
								.getCurrentLocation();
						currentDate = new Date();
						updateUI(currentBestLocation, currentDate);
						handler.postDelayed(this, 200);
					}
				};

				handler.removeCallbacks(runnable);
				handler.postDelayed(runnable, 200);

			}

			@Override
			public void onServiceDisconnected(ComponentName arg0) {
				Log.d(tag, "Disconnected TTLService.");
			}
		};

		// Bind to LocalService
		Intent intent = new Intent(CreateTicketActivity.this,
				TrustedTimeAndLocationService.class);
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		isBound = true;
	}

	/** updates the location on the map and the current time **/
	private void updateUI(Location location, Date time) {
		Log.d(tag, "update UI()");
		// first show location data as text:
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ssZ");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		timeInfo.setText(getString(R.string.tltt_text_time,
				simpleDateFormat.format(time)));

		if (location == null) {
			positionInfo
					.setText(getString(R.string.tltt_progress_waitingforlocation));
			accuracyInfo
					.setText(getString(R.string.tltt_progress_waitingforlocation));
		} else {

			positionInfo.setText(getString(R.string.tltt_text_location,
					Double.toString(location.getLatitude()),
					Double.toString(location.getLongitude())));
			accuracyInfo.setText(getString(R.string.tltt_text_accuracy,
					Double.toString(location.getAccuracy())));

			// second show on map

			GeoPoint geoPoint = new GeoPoint(
					(int) (location.getLatitude() * 1000000),
					(int) (location.getLongitude() * 1000000));

			List<Overlay> overlays = mapView.getOverlays();
			overlays.clear(); // clear the overlay list so that only one point
								// is displayed
			CircleOverlay circleOverlay = new CircleOverlay(geoPoint,
					location.getAccuracy());
			overlays.add(circleOverlay);

			mapController.animateTo(geoPoint);
		}

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/**
	 * unbinds the TrustedTimeAndLocationService and stops the handler
	 */
	private void doUnbindService() {
		if (isBound) {
			Log.d(tag, "unbind TrustedTimeAndLocationService...");
			// Detach our existing connection.
			unbindService(serviceConnection);
			// stop the handler
			handler.removeCallbacks(runnable);
			isBound = false;
		}
	}

	/**
	 * 
	 * Checks if there is at least one provider enabled. If not the user is
	 * guided to the Location settings.
	 * 
	 * @return true if GPS or Network Provider are enabled, false if no provider
	 *         is enabled
	 */
	private boolean enableLocationProvider() {
		LocationManager lm = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Log.d(tag, "GPS Provider is enabled...");
			return true;
		} else if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			Log.d(tag, "Network Provider is enabled...");
			return true;
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.tltt_enablelocationprovider))
					.setCancelable(false)
					.setPositiveButton(
							getString(R.string.tltt_enablelocationprovider_yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Intent intent = new Intent(
											Settings.ACTION_SETTINGS);
									startActivityForResult(intent,
											TLTT.CHANGE_LOCATION_SETTINGS);
								}
							})
					.setNegativeButton(
							getString(R.string.tltt_enablelocationprovider_no),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									CreateTicketActivity.this.finish();
								}
							}).show();
			Log.d(tag,
					"No provider is enabled. Prompt user to change settings...");
			return false;
		}

	}

	/**
	 * On Click Handler. Opens the correct program to display the ticket
	 * attachment.
	 * 
	 * @param view
	 */
	public void showAttachment(View view) {
		if (file == null)
			return;
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
				file.getPath());
		Intent i = new Intent();
		i.setAction(android.content.Intent.ACTION_VIEW);
		Uri uri = Uri.fromFile(file);
		i.setData(Uri.fromFile(file));
		Log.d(tag,
				"Open program to display file attachment. File is of mimetype: "
						+ mimeType + " File path: " + uri.getEncodedPath());
		startActivity(i);
	}

}