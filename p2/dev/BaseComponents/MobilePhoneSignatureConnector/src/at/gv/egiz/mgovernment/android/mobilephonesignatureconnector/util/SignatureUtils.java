package at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.ParseException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.PasswordMissingOrTooShortException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.SignatureCreationFailedException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.WrongTanException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.WrongUserCredentialsException;

/**
 * 
 * @author Sandra Kreuzhuber
 * @author Thomas Zefferer
 * 
 */

public class SignatureUtils {

	private static final String VIEWSTATE = "__VIEWSTATE";
	private static final String EVENT_VALIDATION = "__EVENTVALIDATION";
	private static final String FORM_ID = "form1";
	private static final String FORM_ATTR = "action";
	private static final String SID_PREFIX = "identification.aspx?sid=";
	private static final String REFERENCE_VALUE = "vergleichswert";
	private static final String REFERENCE_VALUE_COLON = "vergleichswert: ";
	private static final String REFERENCE_VALUE_PARSE_ERROR_MESSAGE = "Could not parse reference value.";
	private static final String TITLE_ID = "title";
	private static final String TITLE_CONTENT = "Handy Signatur - Signatur";
	private static final String HTML_PARSE_ERROR_MESSAGE = "Error parsing HTML. Cannot get DataURL.";
	private static final String SIGNATURE_DATA = "Signaturdaten";
	private static final String HREF = "href";
	private static final String TITLE_ERROR_PAGE = "Handy Signatur - Fehlerseite";
	private static final String ERROR_LABEL = "Label1";
	private static final String ERROR_MSG_WRONG_PASSWORD = "Fehler: Mobiltelefonnummer oder Passwort nicht korrekt.";
	private static final String ERROR_MSG_MISSING_PASSWORD = "Fehler: Signaturpasswort fehlt oder zu kurz";
	private static final String ERROR_MSG_WRONG_TAN = "Fehler: Der eingegebene TAN ist falsch.";
	private static final String ERROR_TAG = "sl:ErrorResponse";
	private static final String ERROR_TRY = "Versuch";
	
	/*
	 * Extracts the Viewstate, Eventvalidation and Session Id.
	 * 
	 * @returns null: if one of the fields could not be parsed, otherwise the 3
	 * parameters wrapped in a sessiondata Object
	 */
	public static SessionData extractSessionData(String html) {
		SessionData data = new SessionData();
		org.jsoup.nodes.Document doc = Jsoup.parse(html);
		try {
			data.setViewstate(doc.getElementById(VIEWSTATE).val());

			data.setEventvalidation(doc.getElementById(EVENT_VALIDATION)
					.val());

			String sessionId = doc.getElementById(FORM_ID).attr(FORM_ATTR);
			sessionId = sessionId.substring(sessionId
					.lastIndexOf(SID_PREFIX)
					+ SID_PREFIX.length());
			data.setSessionID(sessionId);
		} catch (NullPointerException e) {
			return null;
		}
		return data;
	}

	public static String extractReferenceValue(String html)
			throws SignatureCreationFailedException {
		org.jsoup.nodes.Document doc = Jsoup.parse(html);
		try {
			String ref = doc.getElementById(REFERENCE_VALUE).text();
			ref = ref.substring(ref.indexOf(REFERENCE_VALUE_COLON)
					+ REFERENCE_VALUE_COLON.length());
			return ref;
		} catch (NullPointerException e) {
			throw new SignatureCreationFailedException(
					REFERENCE_VALUE_PARSE_ERROR_MESSAGE);
		}

	}

	public static String extractSignatureDataURL(String html) throws ParseException {

		org.jsoup.nodes.Document doc = Jsoup.parse(html);

		String title;

		Elements elements = doc.head().getElementsByTag(TITLE_ID);
		if (elements.size() > 0) {
			title = elements.get(0).text();
		}
		else {
			throw new ParseException(HTML_PARSE_ERROR_MESSAGE);
		}
			
		if (!title.contains(TITLE_CONTENT)) {		
			
			throw new ParseException(HTML_PARSE_ERROR_MESSAGE);
		}
		

		Elements el = doc.getElementsContainingText(SIGNATURE_DATA);

		Element e = el.get(el.size()-1);
		String result = e.attr(HREF);
		
		return result;
	}

	public static void checkForErrorResponse(String html)
			throws WrongTanException, SignatureCreationFailedException,
			WrongUserCredentialsException, PasswordMissingOrTooShortException {

		org.jsoup.nodes.Document doc = Jsoup.parse(html);
		// first parse for errors like wrong password, tan etc.
		String title = "";

		Elements elements = doc.head().getElementsByTag(TITLE_ID);
		if (elements.size() > 0) {
			title = elements.get(0).text();
		} else {
			return;
		}


		if (title.contains(TITLE_ERROR_PAGE)) {

			Element element = doc.getElementById(ERROR_LABEL);
			if (element != null) {
				String errorMsg = element.text();

				if (errorMsg
						.contains(ERROR_MSG_WRONG_PASSWORD)) {
					throw new WrongUserCredentialsException(errorMsg);
				} else if (errorMsg
						.contains(ERROR_MSG_MISSING_PASSWORD)) {
					throw new PasswordMissingOrTooShortException(errorMsg);
				} else if (errorMsg
						.contains(ERROR_MSG_WRONG_TAN)) {
					throw new WrongTanException(errorMsg);
				} else {
					throw new SignatureCreationFailedException(errorMsg);
				}
			}

		}

		// look for unexpected errors like timeouts
		Elements errorElements = doc.getElementsByTag(ERROR_TAG);
		if (errorElements != null && errorElements.size() > 0) {
			String errorMsg = "";
			for (Element element : errorElements) {
				errorMsg.concat(element.text() + " ;");
			}
			throw new SignatureCreationFailedException(errorMsg);
		}

	}

	public static int extractNumberOfTriesLeft(String msg) {
		if (msg.contains(ERROR_MSG_WRONG_TAN)) {
			int index = msg.indexOf(ERROR_TRY) - 2;
			if (index != -1) {
				return Character.getNumericValue(msg.charAt(index));
			}

		}
		return 0;
	}
}
