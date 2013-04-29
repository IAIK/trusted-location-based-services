package at.gv.egiz.mgovernment.android.mobilephonesignatureconnectortest;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.activities.HandySignaturActivity;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.constants.Constants;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.constants.SignatureCreationConstants;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.StartingParameterException;

public class TestMobilePhoneSignature extends Activity {

	private static final Boolean USE_TEST_SIGNATURE = Boolean.FALSE;
	private static final Boolean CAPTURE_TAN = Boolean.TRUE;
	
	public static final String RESULT_IDENTIFIER = "result";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_mobile_phone_signature);

		final Button button = (Button) findViewById(R.id.sendButton);

		button.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				startSignatureCreation(
						((EditText) findViewById(R.id.inputText))
								.getEditableText().toString(), CAPTURE_TAN,
						USE_TEST_SIGNATURE);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_test_mobile_phone_signature,
				menu);
		return true;
	}

	@Override
	public void onResume() {

		super.onResume();

		EditText input = (EditText) findViewById(R.id.inputText);
		input.setText("");
	}

	private void startSignatureCreation(String contentToSign,
			Boolean captureTan, Boolean useTestSignature) {
		Intent intent;
		try {
			intent = HandySignaturActivity.buildStartingIntent(
					TestMobilePhoneSignature.this, contentToSign,
					useTestSignature, captureTan);
			startActivityForResult(intent,
					SignatureCreationConstants.CREATE_SIGNATURE);
		} catch (StartingParameterException e) {

			Toast toast = Toast.makeText(this.getApplicationContext(),
					getString(R.string.errorMissingParameter),
					Toast.LENGTH_LONG);
			toast.show();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {

		super.onActivityResult(requestCode, resultCode, intent);

		if (requestCode == SignatureCreationConstants.CREATE_SIGNATURE) {

			if (resultCode == RESULT_OK
					&& intent.hasExtra(SignatureCreationConstants.SIGNATURE)) {

				String signature = intent.getExtras().getString(
						SignatureCreationConstants.SIGNATURE);
				if (signature != null) {

					displaySignature(signature);

				}

			} else if (resultCode == RESULT_CANCELED) {
				Toast toast = Toast.makeText(this.getApplicationContext(),
						getString(R.string.errorCanceled), Toast.LENGTH_LONG);
				toast.show();

			} else if (resultCode == Constants.RESULT_ERROR) {
				Toast toast = Toast.makeText(this.getApplicationContext(),
						getString(R.string.errorGeneric), Toast.LENGTH_LONG);
				toast.show();

			} else {

				Toast toast = Toast.makeText(this.getApplicationContext(),
						getString(R.string.errorGeneric), Toast.LENGTH_LONG);
				toast.show();

			}

		}
	}

	private void displaySignature(String signature) {

		Intent intent = new Intent(TestMobilePhoneSignature.this,
				PresentResultActivity.class);
		intent.putExtra(RESULT_IDENTIFIER, signature);
		startActivity(intent);

	}

}
