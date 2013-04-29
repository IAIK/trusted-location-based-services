package at.gv.egiz.android.signature;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;
import at.gv.egiz.android.debug.DebugTags;
import at.gv.egiz.android.signature.exception.PasswordMissingOrTooShortException;
import at.gv.egiz.android.signature.exception.SignatureCreationFailedException;
import at.gv.egiz.android.signature.exception.WrongTanException;
import at.gv.egiz.android.signature.exception.WrongUserCredentialsException;

public class SignatureUtils {

	private static String tag = DebugTags.EGIZ + " " + DebugTags.SIGNATURE
			+ " SignatureUtils";

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
			data.setViewstate(doc.getElementById("__VIEWSTATE").val());

			data.setEventvalidation(doc.getElementById("__EVENTVALIDATION")
					.val());

			String sessionId = doc.getElementById("form1").attr("action");
			sessionId = sessionId.substring(sessionId
					.lastIndexOf("identification.aspx?sid=")
					+ "identification.aspx?sid=".length());
			data.setSessionID(sessionId);
		} catch (NullPointerException e) {
			Log.d(tag, "Error while parsing session data.");
			return null;
		}
		return data;
	}

	public static String extractReferenceValue(String html)
			throws SignatureCreationFailedException {
		org.jsoup.nodes.Document doc = Jsoup.parse(html);
		try {
			String ref = doc.getElementById("vergleichswert").text();
			ref = ref.substring(ref.indexOf("Vergleichswert: ")
					+ "Vergleichswert: ".length());
			return ref;
		} catch (NullPointerException e) {
			throw new SignatureCreationFailedException(
					"Could not parse reference value.");
		}

	}

	public static void checkForErrorResponse(String html)
			throws WrongTanException, SignatureCreationFailedException,
			WrongUserCredentialsException, PasswordMissingOrTooShortException {

		org.jsoup.nodes.Document doc = Jsoup.parse(html);
		// first parse for errors like wrong password, tan etc.
		String title = "";

		Elements elements = doc.head().getElementsByTag("title");
		if (elements.size() > 0)
			title = elements.get(0).text();
		else
			return;

		Log.d(tag, "Document title: " + title);
		if (title.contains("Handy Signatur - Fehlerseite")) {

			Element element = doc.getElementById("Label1");
			if (element != null) {
				String errorMsg = element.text();
				Log.d(tag, "Error message: " + errorMsg);
				if (errorMsg
						.contains("Fehler: Mobiltelefonnummer oder Passwort nicht korrekt.")) {
					throw new WrongUserCredentialsException(errorMsg);
				} else if (errorMsg
						.contains("Fehler: Signaturpasswort fehlt oder zu kurz")) {
					throw new PasswordMissingOrTooShortException(errorMsg);
				} else if (errorMsg
						.contains("Fehler: Der eingegebene TAN ist falsch.")) {
					throw new WrongTanException(errorMsg);
					// TODO anzahl der Versuche
				} else {
					throw new SignatureCreationFailedException(errorMsg);
				}
			}

		}

		// look for unexpected errors like timeouts
		Elements errorElements = doc.getElementsByTag("sl:ErrorResponse");
		if (errorElements != null && errorElements.size() > 0) {
			String errorMsg = "";
			for (Element element : errorElements) {
				errorMsg.concat(element.text() + " ;");
			}
			throw new SignatureCreationFailedException(errorMsg);
		}

	}

	public static int extractNumberOfTriesLeft(String msg) {
		if (msg.contains("Fehler: Der eingegebene TAN ist falsch.")) {
			int index = msg.indexOf("Versuch") - 2;
			if (index != -1) {
				return Character.getNumericValue(msg.charAt(index));
			}

		}
		return 0;
	}
}
