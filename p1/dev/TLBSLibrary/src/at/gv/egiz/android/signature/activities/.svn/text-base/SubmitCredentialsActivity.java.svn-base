package at.gv.egiz.android.signature.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import at.gv.egiz.android.R;
import at.gv.egiz.android.application.Constants;
import at.gv.egiz.android.signature.SignatureCreationConstants;

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
		/*
		 * NOTE: normally for testsignature you should use 10301 as prefix, but
		 * it also works with any other prefix; actually when sending the 10301
		 * as prefix I was not able to create a signature, as the prefix is not
		 * contained in the list of prefixes that is sent after posting the
		 * initial request the error I got:
		 * 
		 * Invalid postback or callback argument. &nbsp;Event validation is
		 * enabled using &lt;pages enableEventValidation=&quot;true&quot;/&gt;
		 * in configuration or &lt;%@ Page
		 * EnableEventValidation=&quot;true&quot; %&gt; in a page. &nbsp;For
		 * security purposes, this feature verifies that arguments to postback
		 * or callback events originate from the server control that originally
		 * rendered them. &nbsp;If the data is valid and expected, use the
		 * ClientScriptManager.RegisterForEventValidation method in order to
		 * register the postback or callback data for validation.
		 */

		if (useTestSignature) {
			numberField.setText("1122334455");
			pwField.setText("123456789");
			// prefixArray = R.array.prefix_array_test;
		}// else {
		prefixArray = R.array.prefix_array;
		// }

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, prefixArray, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.general_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.reset) {
			setResult(Constants.RESULT_RESET_APPLICATION);
			finish();
			return true;
		} else if (item.getItemId() == R.id.quit) {
			setResult(RESULT_CANCELED);
			finish();
			return true;
		} else
			return super.onOptionsItemSelected(item);

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
