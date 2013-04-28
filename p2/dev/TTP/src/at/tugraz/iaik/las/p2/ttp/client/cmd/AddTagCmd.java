package at.tugraz.iaik.las.p2.ttp.client.cmd;

import at.tugraz.iaik.las.p2.ttp.client.MainMenu;
import at.tugraz.iaik.las.p2.ttp.client.ui.AddTagUi;

import com.google.gwt.user.client.Command;

public class AddTagCmd implements Command {

	@Override
	public void execute() {
		MainMenu.createUi(new AddTagUi(), true);
	}

}
