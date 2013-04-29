package at.gv.egiz.android.tltt.ticket;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;
import at.gv.egiz.android.debug.DebugTags;
import at.gv.egiz.android.signature.exception.XMLCorruptedException;
import at.gv.egiz.android.util.XMLUtils;

public class LocationTimeTicket {

	private ArrayList<Signature> signatures = new ArrayList<Signature>();

	private double longitude;
	private double latitude;
	private double accuracy;
	private Date time;
	private String attachment = null;
	private String fileEnding = null;

	public static final String EGIZ_NAMESPACE = "http://www.egiz.gv.at/namespaces/tltt/1.0#";
	public static final String EGIZ_PREFIX = "tltt";
	public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";

	private String tag = DebugTags.EGIZ + " " + DebugTags.XML
			+ " LocationTimeTicket";

	public LocationTimeTicket(double longitude, double latitude,
			double accuracy, Date time, String attachment, String fileEnding) {
		Log.d(tag, "LTT constructor...");
		this.longitude = longitude;
		this.latitude = latitude;
		this.accuracy = accuracy;
		this.time = time;
		this.attachment = attachment;
		this.fileEnding = fileEnding;
	}

	public LocationTimeTicket(String xmlDocument) throws XMLCorruptedException {
		Log.d(tag, "Construct LTT from xmlDocument.");
		try {
			Document document = XMLUtils.getXMLfromString(xmlDocument);

			Node nodeLongitude = document.getElementsByTagName(
					EGIZ_PREFIX + ":" + "longitude").item(0);
			Node nodeLatitude = document.getElementsByTagName(
					EGIZ_PREFIX + ":" + "latitude").item(0);
			Node nodeAccuracy = document.getElementsByTagName(
					EGIZ_PREFIX + ":" + "accuracy").item(0);
			Node nodeTime = document.getElementsByTagName(
					EGIZ_PREFIX + ":" + "time").item(0);
			Node nodeFileEnding = document.getElementsByTagName(
					EGIZ_PREFIX + ":" + "fileEnding").item(0);
			Node nodeData = document.getElementsByTagName(
					EGIZ_PREFIX + ":" + "data").item(0);

			this.longitude = Double.parseDouble(nodeLongitude.getTextContent());
			this.latitude = Double.parseDouble(nodeLatitude.getTextContent());
			this.accuracy = Double.parseDouble(nodeAccuracy.getTextContent());
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					DATE_PATTERN);
			this.time = simpleDateFormat.parse(nodeTime.getTextContent());
			if (nodeFileEnding.getTextContent() != null)
				this.fileEnding = nodeFileEnding.getTextContent();
			if (nodeData.getTextContent() != null)
				this.attachment = nodeData.getTextContent();

			checkForSignatures(document);
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new XMLCorruptedException(e.getMessage());
		} catch (DOMException e) {
			e.printStackTrace();
			throw new XMLCorruptedException(e.getMessage());
		} catch (ParseException e) {
			e.printStackTrace();
			throw new XMLCorruptedException(e.getMessage());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new XMLCorruptedException(e.getMessage());
		} catch (SAXException e) {
			e.printStackTrace();
			throw new XMLCorruptedException(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new XMLCorruptedException(e.getMessage());
		}

	}

	private void checkForSignatures(Document document)
			throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);

		NodeList sigList = document.getElementsByTagName("dsig:Signature");
		if (sigList != null) {
			for (int i = 0; i < sigList.getLength(); i++) {
				Node node = sigList.item(i);
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document docSub = db.newDocument();
				Node copiedNode = docSub.importNode(node, true);

				// Adds the node to the end of the list of children of this node
				docSub.appendChild(copiedNode);

				signatures.add(new Signature(docSub));
			}
		}

	}

	public ArrayList<Signature> getSignatures() {
		return signatures;
	}

	public int getNumberOfSignatures() {
		return signatures.size();
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getAccuracy() {
		return accuracy;
	}

	public Date getTime() {
		return time;
	}

	public String getAttachment() {
		return attachment;
	}

	public String getFileEnding() {
		return fileEnding;
	}

	public void setFileEnding(String fileEnding) {
		this.fileEnding = fileEnding;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}

	public String getContentToSign() {
		return getTicketinXMLFormat();
	}

	private String getTicketinXMLFormat() {

		StringWriter writer = new StringWriter();
		XmlSerializer serializer = Xml.newSerializer();
		try {
			serializer.setOutput(writer);

			// set indentation
			serializer.setFeature(
					"http://xmlpull.org/v1/doc/features.html#indent-output",
					true);

			serializer.setPrefix(EGIZ_PREFIX, EGIZ_NAMESPACE);
			// TODO
			// removed call to startDocument because Handy BKU does not sign
			// enveloped text with
			// <?xml version='1.0' ?> --> results in "4126Response dieses
			// Befehls
			// an den Browser nicht erlaubt."
			// Problem is, that I don't know how to put the namespace
			// declaration to
			// the xml declaration tag in the first line. Then it would also
			// work
			// with the xml declaration.
			// serializer.startDocument(null, null);

			serializer.startTag(EGIZ_NAMESPACE, "ticket");

			serializer.startTag(EGIZ_NAMESPACE, "location");
			serializer.startTag(EGIZ_NAMESPACE, "longitude");
			serializer.text(Double.toString(this.longitude));
			serializer.endTag(EGIZ_NAMESPACE, "longitude");
			serializer.startTag(EGIZ_NAMESPACE, "latitude");
			serializer.text(Double.toString(this.latitude));
			serializer.endTag(EGIZ_NAMESPACE, "latitude");
			serializer.startTag(EGIZ_NAMESPACE, "accuracy");
			serializer.text(Double.toString(this.accuracy));
			serializer.endTag(EGIZ_NAMESPACE, "accuracy");
			serializer.endTag(EGIZ_NAMESPACE, "location");

			serializer.startTag(EGIZ_NAMESPACE, "time");
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					DATE_PATTERN);
			simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			StringBuilder dateTime = new StringBuilder(
					simpleDateFormat.format(this.time));
			serializer.text(dateTime.toString());
			serializer.endTag(EGIZ_NAMESPACE, "time");

			serializer.startTag(EGIZ_NAMESPACE, "attachment");
			serializer.startTag(EGIZ_NAMESPACE, "data");
			if (this.attachment != null) {
				serializer.text(this.attachment);
			}
			serializer.endTag(EGIZ_NAMESPACE, "data");
			serializer.startTag(EGIZ_NAMESPACE, "fileEnding");
			if (this.fileEnding != null)
				serializer.text(this.fileEnding);
			serializer.endTag(EGIZ_NAMESPACE, "fileEnding");
			serializer.endTag(EGIZ_NAMESPACE, "attachment");

			serializer.endTag(EGIZ_NAMESPACE, "ticket");
			serializer.endDocument();

			serializer.flush();

			return writer.toString();

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	// TODO
	public static boolean isValidTLTT(String tltt) {
		return true;
	}

}
