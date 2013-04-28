package at.tugraz.iaik.las.p2.ttp.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class FailedLoginDialogBox extends DialogBox {
	public FailedLoginDialogBox() {
		final FormPanel form = new FormPanel();

		form.add(this.createAndFormatContentPanel());

		this.setWidget(form);
		this.center();
	}

	private Widget createAndFormatContentPanel() {
		final DockPanel panel = new DockPanel();

		final Label label = new Label("Login failed.");

		final Button ok = new Button("OK");
		ok.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				FailedLoginDialogBox.this.hide();
			}
		});

		panel.add(label, DockPanel.NORTH);
		panel.add(ok, DockPanel.EAST);

		return panel;
	}
}
