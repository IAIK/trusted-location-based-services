package at.gv.egiz.android.tltt;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import at.gv.egiz.android.R;
import at.gv.egiz.android.signature.exception.XMLCorruptedException;
import at.gv.egiz.android.tltt.constants.TLTT;
import at.gv.egiz.android.tltt.ticket.LocationTimeTicket;
import at.gv.egiz.android.tltt.ticket.Signature;
import at.gv.egiz.android.tltt.ticket.bitmap.BitmapWorkerTask;
import at.gv.egiz.android.tltt.utils.FileUtils;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * Displays the LocationTimeTicket with its attachment.
 * 
 * Starting intent: requires TLTT.LOCATION_TIME_TICKET (the TLTT in XML format
 * as String) and TLTT.ACTIVITY_MENU_ID (the menu that should be displayed in
 * this Activity)
 * 
 * @author sandra.kreuzhuber.iaik.tugraz.at
 */
public class ShowTicketActivity extends MapActivity {

	String tag = "Egiz ShowTicketActivity";

	// Ticket
	private TextView positionInfo = null;
	private TextView accuracyInfo = null;
	private TextView timeInfo = null;
	private MapView mapView = null;
	private MapController mapController = null;

	private File file = null;

	private LocationTimeTicket tltt = null;

	// Signatory 1
	private TextView subjectDN_1 = null;
	private TextView issuerDN_1 = null;
	private TextView validTill_1 = null;
	private TextView signingTime_1 = null;
	private TextView serialNumber_1 = null;

	// Signatory 2
	private TextView subjectDN_2 = null;
	private TextView issuerDN_2 = null;
	private TextView validTill_2 = null;
	private TextView signingTime_2 = null;
	private TextView serialNumber_2 = null;

	private Intent startingIntent = null;
	/** menu used in this activity **/
	private int menuName = 0;

	/**
	 * Called when the activity is first created. Extracts the content of the
	 * passed intent and initializes the User Interface.
	 **/
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		startingIntent = this.getIntent();

		// if the TLTT is added to the intent, display the ticket
		if (startingIntent.hasExtra(TLTT.LOCATION_TIME_TICKET)
				&& startingIntent.hasExtra(TLTT.ACTIVITY_MENU_ID)) {
			Log.d(tag, "LTT included in intent.");
			String tlttString = startingIntent.getExtras().getString(
					TLTT.LOCATION_TIME_TICKET);
			try {
				this.tltt = new LocationTimeTicket(tlttString);
			} catch (XMLCorruptedException e) {
				Log.d(tag, "Exception during ticket creation.");
				Toast.makeText(getApplicationContext(), "Error",
						Toast.LENGTH_LONG);
				setResult(RESULT_CANCELED);
				finish();
			}

			menuName = startingIntent.getExtras().getInt(TLTT.ACTIVITY_MENU_ID);

			// starting app from file explorer:
			// parse if file is xml file:
			// e.g. file:///mnt/sdcard/testy/test7.xml
		} else if (FileUtils.hasFileEnding(startingIntent.getDataString(),
				"xml")) {
			Log.d(tag, "File is an xml file. Start parsing.");
			try {
				String xml = FileUtils.readFile(startingIntent.getDataString());
				try {
					this.tltt = new LocationTimeTicket(xml);
				} catch (XMLCorruptedException e) {
					Log.d(tag, "Exception during ticket creation.");
					Toast.makeText(getApplicationContext(), "Error",
							Toast.LENGTH_LONG);
					setResult(RESULT_CANCELED);
					finish();
				}
				menuName = R.menu.show_ticket_menu;
			} catch (IOException e) {
				Log.d(tag, e.getMessage() + e.toString());
				displayErrorMsg();
			}
		} else {
			Log.d(tag,
					"LTT is not included. Display an error msg and leave activity.");
			displayErrorMsg();
		}

		// set the layout
		if (tltt.getNumberOfSignatures() == 1) {
			setContentView(R.layout.signed_ticket);
			this.subjectDN_1 = (TextView) findViewById(R.id.signatory1_subjectDN);
			this.validTill_1 = (TextView) findViewById(R.id.signatory1_validTill);
			this.issuerDN_1 = (TextView) findViewById(R.id.signatory1_issuer);
			this.signingTime_1 = (TextView) findViewById(R.id.signatory1_signingTime);
			this.serialNumber_1 = (TextView) findViewById(R.id.signatory1_serialNumber);
			((TextView) findViewById(R.id.signedTicket))
					.setText(R.string.tltt_signature_ticket);
		} else if (tltt.getNumberOfSignatures() == 2) {
			setContentView(R.layout.signed_ticket2);
			this.subjectDN_1 = (TextView) findViewById(R.id.signatory1_subjectDN);
			this.validTill_1 = (TextView) findViewById(R.id.signatory1_validTill);
			this.issuerDN_1 = (TextView) findViewById(R.id.signatory1_issuer);
			this.signingTime_1 = (TextView) findViewById(R.id.signatory1_signingTime);
			this.serialNumber_1 = (TextView) findViewById(R.id.signatory1_serialNumber);
			this.subjectDN_2 = (TextView) findViewById(R.id.signatory2_subjectDN);
			this.validTill_2 = (TextView) findViewById(R.id.signatory2_validTill);
			this.issuerDN_2 = (TextView) findViewById(R.id.signatory2_issuer);
			this.signingTime_2 = (TextView) findViewById(R.id.signatory2_signingTime);
			this.serialNumber_2 = (TextView) findViewById(R.id.signatory2_serialNumber);
			((TextView) findViewById(R.id.signedTicket))
					.setText(R.string.tltt_signature_ticket);
			((TextView) findViewById(R.id.signedTicket1))
					.setText(R.string.tltt_signature_ticket);
		} else {
			setContentView(R.layout.ticket);
		}
		this.mapView = (MapView) findViewById(R.id.mapview);
		this.mapView.setBuiltInZoomControls(true);
		this.mapController = mapView.getController();
		this.mapController.setZoom(14); // Zoom 1 is world view
		this.positionInfo = (TextView) findViewById(R.id.positionInfo);
		this.accuracyInfo = (TextView) findViewById(R.id.accuracyInfo);
		this.timeInfo = (TextView) findViewById(R.id.timeInfo);

		initUI(tltt.getLongitude(), tltt.getLatitude(), tltt.getAccuracy(),
				tltt.getTime(), tltt.getAttachment(), tltt.getFileEnding());

	}

	private void displayErrorMsg() {
		new AlertDialog.Builder(this).setIcon(R.drawable.icon)
				.setTitle(getString(R.string.tltt_error_fileisnotatltt))
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						setResult(RESULT_CANCELED);
						finish();
					}
				}).show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(menuName, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.quit) {
			// quit activity
			setResult(RESULT_CANCELED);
			finish();
			return true;
		} else if (item.getItemId() == R.id.sign) {
			// start signature creation
			Intent resultIntent = new Intent();
			resultIntent.putExtra(TLTT.COMMAND, TLTT.COMMAND_SIGN);
			resultIntent.putExtra(TLTT.LOCATION_TIME_TICKET, startingIntent
					.getExtras().getString(TLTT.LOCATION_TIME_TICKET));
			setResult(RESULT_OK, resultIntent);
			finish();
			return true;
		} else if (item.getItemId() == R.id.share) {
			// share
			Intent resultIntent = new Intent();
			resultIntent.putExtra(TLTT.COMMAND, TLTT.COMMAND_SHARE);
			resultIntent.putExtra(TLTT.LOCATION_TIME_TICKET, startingIntent
					.getExtras().getString(TLTT.LOCATION_TIME_TICKET));
			setResult(RESULT_OK, resultIntent);
			finish();
			return true;
		} else
			return super.onOptionsItemSelected(item);

	}

	/** displays the information stored in the TLTT **/
	private void initUI(double longitude, double latitude, double accuracy,
			Date time, String attachment, String fileEnding) {

		// first show location data as text:
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ssZ");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		timeInfo.setText(getString(R.string.tltt_text_time,
				simpleDateFormat.format(time)));

		positionInfo.setText(getString(R.string.tltt_text_location,
				Double.toString(latitude), Double.toString(longitude)));
		accuracyInfo.setText(getString(R.string.tltt_text_accuracy,
				Double.toString(accuracy)));

		// second show on map
		GeoPoint geoPoint = new GeoPoint((int) (latitude * 1000000),
				(int) (longitude * 1000000));

		List<Overlay> overlays = mapView.getOverlays();
		overlays.clear(); // clear the overlay list so that only one point
							// is displayed
		CircleOverlay circleOverlay = new CircleOverlay(geoPoint, accuracy);
		overlays.add(circleOverlay);

		mapController.animateTo(geoPoint);

		// display the attachment if there is one
		if (attachment != "") {

			// recover image from ticket
			String path = TLTT.PATH_ATTACHMENTS + FileUtils.getTimestamp()
					+ "." + fileEnding;
			File folder = new File("/sdcard/locationTimeTickets/attachments");

			if (!folder.exists())
				folder.mkdirs();

			try {
				file = FileUtils.writeBase64StringToFile(attachment, path);
				if (fileEnding.toLowerCase().contains("jpg".toLowerCase())
						|| fileEnding.toLowerCase().contains(
								"png".toLowerCase())
						|| fileEnding.toLowerCase().contains(
								"jpeg".toLowerCase())) {

					// display attachment
					TextView attachmentText = (TextView) findViewById(R.id.textViewAttachment);
					attachmentText.setVisibility(0);

					ImageView attachmentImage = (ImageView) findViewById(R.id.imageAttachment);
					Log.d(tag, "Load bitmap...");
					BitmapWorkerTask task = new BitmapWorkerTask(
							attachmentImage,
							findViewById(R.id.linearLayoutTicket),
							ShowTicketActivity.this);
					task.execute(path);

				} else {
					// add a button that enables the user to view the file in a
					// different program
					Button showAttachment = (Button) findViewById(R.id.buttonShowAttachment);
					showAttachment.setVisibility(View.VISIBLE);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		ArrayList<Signature> signatures = tltt.getSignatures();
		if (signatures == null)
			return;

		Signature sig;
		if (signatures.size() == 1) {
			sig = signatures.get(0);
			if (sig != null) {
				subjectDN_1
						.setText(getString(R.string.tltt_signature_subject_dn,
								sig.getSubjectDN()));
				serialNumber_1.setText(getString(
						R.string.tltt_signature_serial_number,
						sig.getSerialNumber()));
				signingTime_1.setText(getString(
						R.string.tltt_signature_signing_time,
						sig.getSigningTime()));
				issuerDN_1.setText(getString(R.string.tltt_signature_issuer_dn,
						sig.getIssuerDN()));
				validTill_1
						.setText(getString(R.string.tltt_signature_valid_till,
								sig.getValidTill()));
			}
		}
		if (signatures.size() == 2) {
			sig = signatures.get(1);
			if (sig != null) {
				subjectDN_1
						.setText(getString(R.string.tltt_signature_subject_dn,
								sig.getSubjectDN()));
				serialNumber_1.setText(getString(
						R.string.tltt_signature_serial_number,
						sig.getSerialNumber()));
				signingTime_1.setText(getString(
						R.string.tltt_signature_signing_time,
						sig.getSigningTime()));
				issuerDN_1.setText(getString(R.string.tltt_signature_issuer_dn,
						sig.getIssuerDN()));
				validTill_1
						.setText(getString(R.string.tltt_signature_valid_till,
								sig.getValidTill()));
			}
			sig = signatures.get(0);
			if (sig != null) {
				subjectDN_2
						.setText(getString(R.string.tltt_signature_subject_dn,
								sig.getSubjectDN()));
				serialNumber_2.setText(getString(
						R.string.tltt_signature_serial_number,
						sig.getSerialNumber()));
				signingTime_2.setText(getString(
						R.string.tltt_signature_signing_time,
						sig.getSigningTime()));
				issuerDN_2.setText(getString(R.string.tltt_signature_issuer_dn,
						sig.getIssuerDN()));
				validTill_2
						.setText(getString(R.string.tltt_signature_valid_till,
								sig.getValidTill()));
			}
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public void showAttachment(View view) {
		Log.d(tag, "Open right program...");
		if (file == null)
			return;
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
				file.getPath());
		Log.d(tag, "Mime type: " + mimeType);
		Intent i = new Intent();
		i.setAction(android.content.Intent.ACTION_VIEW);
		Uri uri = Uri.fromFile(file);
		Log.d(tag, uri.getEncodedPath());
		i.setData(Uri.fromFile(file));

		startActivity(i);
	}

}
