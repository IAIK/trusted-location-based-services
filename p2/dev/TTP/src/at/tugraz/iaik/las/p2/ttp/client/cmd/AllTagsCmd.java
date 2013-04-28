package at.tugraz.iaik.las.p2.ttp.client.cmd;

import at.tugraz.iaik.las.p2.ttp.client.MainMenu;
import at.tugraz.iaik.las.p2.ttp.client.ui.AllTagsUi;

import com.google.gwt.user.client.Command;

public class AllTagsCmd implements Command {

	@Override
	public void execute() {
		MainMenu.createUi(new AllTagsUi(), true);
	}

}
