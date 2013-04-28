package at.tugraz.iaik.las.p2.ttp.client.cmd;

import at.tugraz.iaik.las.p2.ttp.client.MainMenu;
import at.tugraz.iaik.las.p2.ttp.client.ui.LoginUi;

import com.google.gwt.user.client.Command;

/**
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class LogOutCmd implements Command {

	@Override
	public void execute() {
		MainMenu.createUi(new LoginUi(), false);
	}
}
