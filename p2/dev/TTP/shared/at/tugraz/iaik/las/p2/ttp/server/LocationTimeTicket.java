package at.tugraz.iaik.las.p2.ttp.server;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;
import org.w3c.dom.Document;

/**
 * A Location/Time Ticket.
 * 
 * @author cmlesjak
 * 
 */
@Root
@Namespace(reference = "http://p2.las.iaik.tugraz.at/LocationTimeTicket")
public class LocationTimeTicket implements Serializable {

	public boolean isEqual(LocationTimeTicket ltt) {
		return ltt.toString().compareTo(this.toString()) == 0;
	}

	private static final long serialVersionUID = 1L;

	@Element
	public Double LocationLatitude;

	@Element
	public Double LocationLongitude;

	@Element
	public Date Time;

	public String toString() {
		return String.format(Locale.US, "LTT: (%f, %f), %s",
				this.LocationLatitude, this.LocationLongitude, Time.toString());
	}

	public static LocationTimeTicket extractLttFromSignedLtt(String signedLtt) {
		ByteArrayInputStream bais = new ByteArrayInputStream(
				signedLtt.getBytes());

		LocationTimeTicket ltt = null;
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			docFactory.setNamespaceAware(true); // never forget this!
			DocumentBuilder builder = docFactory.newDocumentBuilder();
			Document doc = builder.parse(bais);
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			// http://xpath.online-toolz.com/tools/xpath-editor.php
			XPathExpression expr = xpath.compile("//*[@Id='signed-data-1-1']");
			org.w3c.dom.Element result = (org.w3c.dom.Element) expr.evaluate(
					doc, XPathConstants.NODE);
			// Element lttElement = doc.getElementById("signed-data-1-1");

			// make xml string from element
			// http://stackoverflow.com/questions/7299752/get-full-xml-text-from-node-instance
			StreamResult xmlOutput = new StreamResult(new StringWriter());
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");
			transformer.transform(new DOMSource((result).getFirstChild()),
					xmlOutput);
			String lttXmlString = xmlOutput.getWriter().toString();
			ltt = TtpApiUtils.deserializeFromXmlString(lttXmlString,
					LocationTimeTicket.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ltt;
	}
}
