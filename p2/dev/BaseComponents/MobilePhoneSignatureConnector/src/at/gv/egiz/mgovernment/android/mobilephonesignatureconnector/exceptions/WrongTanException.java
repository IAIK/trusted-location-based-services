package at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions;

/**
 * 
 * @author Sandra Kreuzhuber
 * @author Thomas Zefferer
 * 
 */

public class WrongTanException extends Exception {

	private static final long serialVersionUID = 1L;

	public WrongTanException(String msg) {
		super(msg);
	}
}
