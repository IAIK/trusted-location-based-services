package at.tugraz.iaik.las.p2.ttp.server;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

/**
 * 
 * @author christian.lesjak@student.tugraz.at
 *
 */
public class PMF {
	private static final PersistenceManagerFactory PMF = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");
	
	public static final PersistenceManager get() {
		return PMF.getPersistenceManager();
	}
}
