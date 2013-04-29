package at.gv.egiz.android.prototype;

import sample.android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import at.gv.egiz.android.communication.bluetooth.BluetoothConstants;
import at.gv.egiz.android.signature.SignatureCreationConstants;
import at.gv.egiz.android.signature.activities.HandySignaturActivity;
import at.gv.egiz.android.signature.exception.StartingParameterException;
import at.gv.egiz.android.storage.SDCardAdapter;
import at.gv.egiz.android.storage.StorageAdapter;
import at.gv.egiz.android.tltt.CreateTicketActivity;
import at.gv.egiz.android.tltt.ShowTicketActivity;
import at.gv.egiz.android.tltt.constants.TLTT;
import at.gv.egiz.android.tltt.utils.FileUtils;
import at.gv.egiz.android.util.ConnectionUtils;

/**
 * THIS IS ONLY A PROJECT FOR TESTING THE DIFFERENT MODULES.
 * 
 * @author sandra
 * 
 */
public class P2PLocationProver extends Activity {

	String tag = "Egiz P2PLocationProver";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.samplestart);

	}

	@Override
	public void onStart() {
		super.onStart();
		// check if Internet connection is available
		// internet access is required for all operations: creating new tickets,
		// displaying tickets, ...
		// because google maps requires internet access in order to display the
		// map accordingly
		if (!ConnectionUtils
				.isInternetConnectionAvailable(getApplicationContext())) {
			Log.d(tag,
					"No internet connection available. Prompt user to enable it.");
			promptUserToEnableInternetConnection();
		}

	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		Log.d(tag, "onActivitiyResult()...with request code:" + requestCode);
		if (requestCode == TLTT.GET_NEW_TICKET) { // STEP 1: create ticket
			if (resultCode == RESULT_OK
					&& intent.hasExtra(TLTT.LOCATION_TIME_TICKET)) {
				Log.d(tag, "Retrieved location time ticket:");
				Log.d(tag,
						intent.getExtras().getString(TLTT.LOCATION_TIME_TICKET));

				// now display the ticket so that the user can decide to sign it
				Intent intentShow = new Intent(P2PLocationProver.this,
						ShowTicketActivity.class);
				intentShow.putExtra(TLTT.LOCATION_TIME_TICKET, intent
						.getExtras().getString(TLTT.LOCATION_TIME_TICKET));
				intentShow.putExtra(TLTT.ACTIVITY_MENU_ID,
						R.menu.sign_ticket_menu);
				startActivityForResult(intentShow, TLTT.SHOW_TICKET);

			}
		} else if (requestCode == TLTT.SHOW_TICKET) { // STEP 2: show ticket
			if (resultCode == RESULT_OK && intent.hasExtra(TLTT.COMMAND)) {
				switch (intent.getExtras().getInt(TLTT.COMMAND)) {
				case TLTT.COMMAND_SIGN: // STEP 3: sign ticket
					startSignatureCreation(P2PLocationProver.this, intent
							.getExtras().getString(TLTT.LOCATION_TIME_TICKET),
							false, false, null);
					break;
				case TLTT.COMMAND_SHARE: // STEP 5: share ticket
					Intent intentNFC = new Intent(P2PLocationProver.this,
							InitiateNFCActivity.class);
					intentNFC.putExtra(BluetoothConstants.BT_MESSAGE, intent
							.getExtras().getString(TLTT.LOCATION_TIME_TICKET));
					startActivityForResult(intentNFC,
							TLTT.SEND_TICKET_TO_PEER_DEVICE);
					break;
				default:

				}
			}
		} else if (requestCode == SignatureCreationConstants.CREATE_SIGNATURE) { // STEP
																					// 4:
																					// store
																					// signature,
																					// display
																					// signed
																					// ticket

			if (resultCode == RESULT_OK
					&& intent.hasExtra(SignatureCreationConstants.SIGNATURE)) {

				Log.d(tag, "Store retrieved signature in file.");
				String signature = intent.getExtras().getString(
						SignatureCreationConstants.SIGNATURE);
				if (signature != null) {
					Log.d(tag, signature);
					StorageAdapter storage = new SDCardAdapter(
							getApplicationContext());
					String filename = FileUtils.getTimestamp() + ".xml";
					storage.write(signature, "/locationTimeTickets/", filename);

					// display signed ticket
					Intent intentShow = new Intent(P2PLocationProver.this,
							ShowTicketActivity.class);
					intentShow.putExtra(TLTT.LOCATION_TIME_TICKET, signature);

					intentShow.putExtra(TLTT.ACTIVITY_MENU_ID,
							R.menu.share_ticket_menu);
					startActivityForResult(intentShow, TLTT.SHOW_TICKET);

				}// TODO else error

			} else if (resultCode == RESULT_CANCELED) {
				if (intent.hasExtra(SignatureCreationConstants.ERROR_BOX)) {
					Log.d(tag, "Error occured. Start creation again.");

					startSignatureCreation(
							this,
							intent.getStringExtra(SignatureCreationConstants.TEXT_TO_SIGN),
							intent.getBooleanExtra(
									SignatureCreationConstants.CAPTURE_TAN,
									Boolean.FALSE),
							intent.getBooleanExtra(
									SignatureCreationConstants.USE_TEST_SIGNATURE,
									Boolean.FALSE),
							intent.getStringExtra(SignatureCreationConstants.ERROR_BOX));
				} else {
					Log.d(tag, "quitting the application...");
					finish();
				}
			} else {

			}

		} else if (requestCode == at.gv.egiz.android.application.Constants.CHANCE_INTERNET_SETTINGS) {
			if (!ConnectionUtils
					.isInternetConnectionAvailable(getApplicationContext()))
				promptUserToEnableInternetConnection();
		}
	}

	public void createTicket(View view) {

		Intent intentTicket = new Intent(P2PLocationProver.this,
				CreateTicketActivity.class);
		startActivityForResult(intentTicket, TLTT.GET_NEW_TICKET);

	}

	/**
	 * Creates the intent for starting the signature creation.
	 * 
	 * @param originalActivity
	 * @param contentToSign
	 * @param captureTan
	 * @param useTestSignature
	 * @param errormsg
	 *            if null, it is not added to the starting intent. so call it
	 *            with null if no error has occured
	 */
	public static void startSignatureCreation(Activity originalActivity,
			String contentToSign, Boolean captureTan, Boolean useTestSignature,
			String errormsg) {
		Intent intent;
		try {
			intent = HandySignaturActivity.buildStartingIntent(
					originalActivity, "signature_certstore", "bak_test",
					"signature_truststore", "bak_test", "sample.android",
					contentToSign, "application/xml", useTestSignature,
					captureTan);
			if (errormsg != null) {
				intent.putExtra(SignatureCreationConstants.ERROR_BOX, errormsg);
			}
			originalActivity.startActivityForResult(intent,
					SignatureCreationConstants.CREATE_SIGNATURE);
		} catch (StartingParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// public void startSignatureCaptureTan(View view) {
	// startSignatureCreation(P2PLocationProver.this, "testtest", true, false);
	// }

	// public void startTestSignature(View view) {
	// startSignatureCreation(P2PLocationProver.this, "blablabla", false, true);
	// }

	// public void startSignatureOhneCaptureTan(View view) {
	// startSignatureCreation(P2PLocationProver.this, "blablabla", false,
	// false);
	// }

	private void promptUserToEnableInternetConnection() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"No Internet Connection. Enable WLAN or data traffic.")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								ConnectionUtils
										.enableInternetConnection(P2PLocationProver.this);
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						P2PLocationProver.this.finish();
					}
				}).show();

	}

}