package at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.constants;

/**
 * 
 * @author Sandra Kreuzhuber
 * @author Thomas Zefferer
 * 
 */

public class ATrustConstants {

	/**
	 * the name/number that is displayed in the sender field when receiving a
	 * mTan message from the MobileCCE
	 */
	public static final String mTanSender = "A-Trust";

	/**
	 * the flag if tan should be captured
	 */
	public static final String mTanFlag = "CaptureMTan";
	/**
	 * the captured mTan
	 */
	public static final String mTanSequence = "mTAN";

	/**
	 * The basic request URL used for the initial request to the MobileCCE.
	 * requestUrl + extension is used for further communication with the
	 * MobileCCE.
	 */
	public static final String requestUrl = "https://www.a-trust.at/mobile/https-security-layer-request/";
	public static final String requestTestUrl = "https://test1.a-trust.at/https-security-layer-request/";

	/**
	 * The extension needed for the action to send the user data to the
	 * MobileCCE.
	 */
	public static final String identificationExtension = "identification.aspx?sid=";

	/**
	 * The extension needed for sending the mTan to the MobileCCE.
	 */
	public static final String signatureExtension = "signature.aspx?sid=";

	/**
	 * The extension needed for resending the mTan.
	 */
	public static final String errorExtrension = "error.aspx?sid=";

	/**
	 * Http param: XMLRequest
	 */
	public static final String paramXMLRequest = "XMLRequest";

	/**
	 * Http param: VIEWSTATE
	 */
	public static final String paramViewstate = "__VIEWSTATE";

	/**
	 * Http param: EVENTVALIDATION
	 */
	public static final String paramEventValidation = "__EVENTVALIDATION";

	/**
	 * Http param: vorwahl
	 */
	public static final String paramPrefix = "vorwahl";

	/**
	 * Http param: handynummer
	 */
	public static final String paramTelNumber = "handynummer";

	/**
	 * Http param: signaturpasswort
	 */
	public static final String paramSignPassword = "signaturpasswort";

	/**
	 * Http param: Button_Identification
	 */
	public static final String paramBtnIdentification = "Button_Identification";

	/**
	 * Http param: input_tan
	 */
	public static final String paramInputTan = "input_tan";

	/**
	 * Http param: SignButton
	 */
	public static final String paramBtnSign = "SignButton";

	/**
	 * Http param: Button_Back
	 */
	public static final String paramBtnBack = "Button_Back";
}