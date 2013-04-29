package at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions;

/**
 * 
 * @author Sandra Kreuzhuber
 * @author Thomas Zefferer
 * 
 */

public class NoInternetConnectionAvailableException extends Exception {

	private static final long serialVersionUID = 1L;

	public NoInternetConnectionAvailableException(String msg) {
		super(msg);
	}
}
