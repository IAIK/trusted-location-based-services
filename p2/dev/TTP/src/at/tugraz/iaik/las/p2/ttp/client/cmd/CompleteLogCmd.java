package at.tugraz.iaik.las.p2.ttp.client.cmd;

import at.tugraz.iaik.las.p2.ttp.client.MainMenu;
import at.tugraz.iaik.las.p2.ttp.client.ui.CompleteLogUi;

import com.google.gwt.user.client.Command;

/**
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class CompleteLogCmd implements Command {

	@Override
	public void execute() {
		MainMenu.createUi(new CompleteLogUi(), true);
	}

}
