package at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.R;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.constants.SignatureCreationConstants;

/**
 * 
 * @author Sandra Kreuzhuber
 * @author Thomas Zefferer
 * 
 */

public class SubmitCredentialsActivity extends Activity {

	private boolean useTestSignature = false;
	private Spinner spinner;
	private TextView errorbox;
	private EditText numberField;
	private EditText pwField;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.egiz_enter_number_pwd);

		Bundle bundle = this.getIntent().getExtras(); 
		if (bundle != null) {
			if (bundle
					.containsKey(SignatureCreationConstants.USE_TEST_SIGNATURE)) {
				useTestSignature = bundle
						.getBoolean(SignatureCreationConstants.USE_TEST_SIGNATURE);
			}
			if (bundle.containsKey(SignatureCreationConstants.ERROR_BOX)) {
				errorbox = (TextView) findViewById(R.id.Errorbox);
				errorbox.setVisibility(View.VISIBLE);
				errorbox.setText(bundle
						.getString(SignatureCreationConstants.ERROR_BOX));
			}
		}

		numberField = (EditText) findViewById(R.id.editTextNummer);
		pwField = (EditText) findViewById(R.id.editTextPasswort);
		spinner = (Spinner) findViewById(R.id.spinnerVorwahl);

		int prefixArray;

		if (useTestSignature) {
			numberField.setText(getString(R.string.testSignatureNumber));
			pwField.setText(getString(R.string.testSignaturePassword));			
		}
		
		prefixArray = R.array.prefix_array;
		

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, prefixArray, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		// set focus to spinner to avoid that keyboard is shown when activity is started
		spinner.setFocusable(Boolean.TRUE);
		spinner.setFocusableInTouchMode(Boolean.TRUE);
		spinner.requestFocus();
		
		if(useTestSignature) {
			spinner.setSelection(1);
		}

	}

	public void submit(View view) {

		EditText numberField = (EditText) findViewById(R.id.editTextNummer);
		EditText pwField = (EditText) findViewById(R.id.editTextPasswort);
		
		Intent intent = new Intent();
		intent.putExtra(SignatureCreationConstants.STATE,
				SignatureCreationConstants.USER_ENTERED_CREDENTIALS);
		intent.putExtra(SignatureCreationConstants.PREFIX, spinner
				.getSelectedItem().toString());
		intent.putExtra(SignatureCreationConstants.TELEPHONE_NUMBER,
				numberField.getText().toString());
		intent.putExtra(SignatureCreationConstants.SIGNATURE_PASSWORD, pwField
				.getText().toString());

		setResult(RESULT_OK, intent);
		finish();
	}

	public void cancel(View view) {
		setResult(RESULT_CANCELED);
		finish();
	}

}
