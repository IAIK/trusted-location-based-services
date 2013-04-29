package at.gv.egiz.mgovernment.android.mobilephonesignatureconnectortest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PresentResultActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.present_result_layout);

		Intent intent = getIntent();
		String signature = intent.getExtras().getString(
				TestMobilePhoneSignature.RESULT_IDENTIFIER);

		TextView result = (TextView) findViewById(R.id.result);
		result.setText(signature);
		result.setMovementMethod(new ScrollingMovementMethod());

		final Button button = (Button) findViewById(R.id.okButton);
		button.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				finish();
			}
		});

	}
}
