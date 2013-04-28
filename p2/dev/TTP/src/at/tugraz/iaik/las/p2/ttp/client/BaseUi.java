package at.tugraz.iaik.las.p2.ttp.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public abstract class BaseUi extends Composite {
	/**
	 * Create a remote service proxy to talk to the server-side data service.
	 */
	protected final TtpDataServiceAsync ttpDataService = GWT
			.create(TtpDataService.class);

	protected final Label lbTitle = new Label();

	protected final Label lbMessage = new Label();

	public BaseUi(String title, String message) {
		this.lbTitle.setText(title);
		this.lbTitle.setStylePrimaryName("header1");
		this.lbMessage.setText(message);
		
		
		final DockLayoutPanel panel = new DockLayoutPanel(Unit.EM);
		FlowPanel header = new FlowPanel();
		header.setStylePrimaryName("header");
		header.add(this.lbTitle);
		header.add(this.lbMessage);
		panel.addNorth(header, 4);		
		
		panel.add(this.createAndFormatContentPanel());
		this.initWidget(panel);
	}

	protected abstract Widget createAndFormatContentPanel();

	public BaseUi() {

	}
}
