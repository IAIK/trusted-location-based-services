package at.tugraz.iaik.las.p2.ttp.client.cmd;

import at.tugraz.iaik.las.p2.ttp.client.MainMenu;
import at.tugraz.iaik.las.p2.ttp.client.ui.IssuedTLttsUi;

import com.google.gwt.user.client.Command;

/**
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class IssuedTLttsCmd implements Command {

	@Override
	public void execute() {
		MainMenu.createUi(new IssuedTLttsUi(), true);
	}

}
