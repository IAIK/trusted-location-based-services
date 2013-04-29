package at.gv.egiz.android.util;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;
import at.gv.egiz.android.debug.DebugTags;

/**
 * XMLUtils. Helper methods.
 * 
 * @author Sandra Kreuzhuber
 * 
 */
public class XMLUtils {

	/**
	 * Returns a org.w3c.dom.Document representation of the xml file.
	 * 
	 * @param xml
	 *            - a xml file as string
	 * @return org.w3c.dom.Document representation of the xml file,
	 *         <code>null</code> if an error occurred
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Document getXMLfromString(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		Document document = null;

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		DocumentBuilder db = dbf.newDocumentBuilder();

		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xml));
		document = db.parse(is);
		Log.d("Egiz XML XMLUtils", "getXMLfromString end");
		return document;
	}

	public static String extractSignatureFromResponse(String xmlDocument) {
		String signatureStart = "<dsig:Signature";
		String signatureEnd = "</sl:CreateXMLSignatureResponse>";
		int start = xmlDocument.indexOf(signatureStart);
		int end = xmlDocument.indexOf(signatureEnd);
		Log.d(DebugTags.EGIZ + " XMLUtils", "cut off string beginning by: "
				+ start);
		Log.d(DebugTags.EGIZ + " XMLUtils",
				"Signature: " + xmlDocument.substring(start, end));
		return xmlDocument.substring(start, end);

	}
}
