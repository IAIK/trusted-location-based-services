package at.tugraz.iaik.las.p2.ttp.client;

import at.tugraz.iaik.las.p2.ttp.client.cmd.AddTagCmd;
import at.tugraz.iaik.las.p2.ttp.client.cmd.AllTagsCmd;
import at.tugraz.iaik.las.p2.ttp.client.cmd.CompleteLogCmd;
import at.tugraz.iaik.las.p2.ttp.client.cmd.IssuedTLttsCmd;
import at.tugraz.iaik.las.p2.ttp.client.cmd.LogOutCmd;
import at.tugraz.iaik.las.p2.ttp.client.cmd.StartCmd;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class MainMenu {
	public static void createUi(Widget mainContent, boolean showMenu) {
		RootLayoutPanel.get().clear();
		DockLayoutPanel panel = new DockLayoutPanel(Unit.EM);
		FlowPanel flowPanel = new FlowPanel();
		flowPanel
				.add(new HTML(
						"<h1>Location Aware Signature Prototype Variant 2 (Cryptotag/NFC)</h1>"));
		if (showMenu) {
			flowPanel.add(MainMenu.create());
		}
		panel.addNorth(flowPanel, 6);
		//panel.addSouth(new HTML("christian.lesjak@student.tugraz.at"), 2);
		// panel.addWest(new HTML("navigation"), 10);
		panel.add(mainContent);
		RootLayoutPanel.get().add(panel);
	}

	public static MenuBar create() {
		MenuBar menuBar = new MenuBar();

		menuBar.addItem(new MenuItem("Start", new StartCmd()));

		MenuBar mbLog = new MenuBar();
		mbLog.setAutoOpen(true);
		mbLog.addItem(new MenuItem("Issued LTTs", new IssuedTLttsCmd()));
		mbLog.addItem(new MenuItem("Complete Log", new CompleteLogCmd()));
		menuBar.addItem(new MenuItem("History", mbLog));

		MenuBar mbMgmt = new MenuBar();
		mbLog.setAutoOpen(true);
		mbMgmt.addItem(new MenuItem("All Tags", new AllTagsCmd()));
		mbMgmt.addItem(new MenuItem("Add new Tag", new AddTagCmd()));
		menuBar.addItem(new MenuItem("Management", mbMgmt));

		menuBar.addItem(new MenuItem("Log Out", new LogOutCmd()));

		return menuBar;
	}
}
