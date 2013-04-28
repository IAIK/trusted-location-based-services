package at.tugraz.iaik.las.p2.ttp.client;

import at.tugraz.iaik.las.p2.ttp.client.cmd.LogOutCmd;

import com.google.gwt.core.client.EntryPoint;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 * 
 * @author christian.lesjak@student.tugraz.at
 */
public class Ttp implements EntryPoint {

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		(new LogOutCmd()).execute();
	}
}
