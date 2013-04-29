package at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions;

/**
 * 
 * @author Sandra Kreuzhuber
 * @author Thomas Zefferer
 * 
 */

public class WrongUserCredentialsException extends Exception {

	private static final long serialVersionUID = 1L;

	public WrongUserCredentialsException(String msg) {
		super(msg);
	}

}
