package at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions;

/**
 * 
 * @author Sandra Kreuzhuber
 * @author Thomas Zefferer
 * 
 */

public class SignatureCreationFailedException extends Exception {

	private static final long serialVersionUID = 1L;

	public SignatureCreationFailedException(String msg) {
		super(msg);
	}

}
