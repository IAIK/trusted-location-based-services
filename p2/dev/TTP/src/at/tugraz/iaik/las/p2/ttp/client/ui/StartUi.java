package at.tugraz.iaik.las.p2.ttp.client.ui;

import at.tugraz.iaik.las.p2.ttp.client.BaseUi;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class StartUi extends BaseUi {

	public StartUi() {
		super("Welcome.", "");
	}

	protected Widget createAndFormatContentPanel() {
		FlowPanel panel = new FlowPanel();

		Label label1 = new Label();
		label1.setText("Generate Fake Tags (Mock Tags)");
		label1.setStylePrimaryName("header2");
		panel.add(label1);

		Label label2 = new Label();
		label2.setText("In the Provera app, choose \"Create Mock Tag\" from the menu. The generated tag will be store on the external storage of the smart phone (sdcard/LAS-Prover/mocktags/<UID>/).");
		panel.add(label2);
		
		Label label3 = new Label();
		label3.setText("Add tags to the TTP");
		label3.setStylePrimaryName("header2");
		panel.add(label3);

		Label label4 = new Label();
		label4.setText("To run the protocol, add either real crypto tags or mock tags to the TTP. On the web interface go to Management/Add Tag. Fill out the data accordingly, and upload the tags certificate (for generated mock tags from the phones external storage in sdcard/LAS-Prover/mocktags/<UID>/PublicKeyCert.cer.");
		panel.add(label4);
		
		Label label5 = new Label();
		label5.setText("Acquire an T-LTT");
		label5.setStylePrimaryName("header2");
		panel.add(label5);

		Label label6 = new Label();
		label6.setText("To initiate the protocol to acquire a Trusted Location Time Ticket (T-LTT), open the Prover app and either touch a real crypto tag or select a mock tag from the menu.");
		panel.add(label6);
		Label label7 = new Label();
		label7.setText("The acquired T-LTT can be viewed on the web interface of the TTP under History/Issued T-LTTs.");
		panel.add(label7);
		
		return panel;
	}
}
