package at.gv.egiz.android.tltt.ticket;

import javax.security.cert.X509Certificate;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Base64;
import android.util.Log;

/**
 * Extracts the details contained in a signature. Creates a X509Certificate out
 * of the signature to access the data of the signatory.
 * 
 * @author sandra.kreuzhuber@iaik.tugraz.at
 * 
 */
public class Signature {

	private String signingTime = null; // etsi:SigningTime
	private String subjectDN = ""; // SubjectDN
	private String issuerDN = ""; // issuer of certificate
	private X509Certificate certificate = null; // dsig:X509Certificate
	private String serialNumber = ""; // serialnumber of certificate
	private String signatureValue = ""; // dsig:SignatureValue
	private String validTill = ""; // validity of certificate

	public Signature(Document xmlDocument) {
		parseSignature(xmlDocument);
		if (this.certificate != null) {
			this.serialNumber = certificate.getSerialNumber().toString();
			this.subjectDN = certificate.getSubjectDN().toString();
			this.issuerDN = certificate.getIssuerDN().toString();
			this.validTill = certificate.getNotAfter().toString();
		}
	}

	private void parseSignature(Document document) {
		NodeList list = document.getElementsByTagName("dsig:X509Certificate");
		Node nodeX509Certificate = null;
		if (list != null && list.getLength() > 0)
			nodeX509Certificate = list.item(0);

		list = document.getElementsByTagName("dsig:SignatureValue");
		if (list != null && list.getLength() > 0)
			this.signatureValue = list.item(0).getTextContent();

		list = document.getElementsByTagName("etsi:SigningTime");
		if (list != null && list.getLength() > 0)
			this.signingTime = list.item(0).getTextContent();

		try {
			if (nodeX509Certificate != null
					&& nodeX509Certificate.getTextContent() != null) {
				byte[] c = Base64.decode(nodeX509Certificate.getTextContent()
						.getBytes(), 0);
				this.certificate = X509Certificate.getInstance(c);
			}
		} catch (NullPointerException exp) {
			// TODO Errorhandling
			Log.d("Error", "XML File is corrupted.");
		} catch (DOMException e) {
			// TODO Errorhandling
			e.printStackTrace();
			Log.d("Error", "XML File is corrupted.");
		} catch (javax.security.cert.CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String getSigningTime() {
		return signingTime;
	}

	public String getSubjectDN() {
		return subjectDN;
	}

	public String getIssuerDN() {
		return issuerDN;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public String getSignatureValue() {
		return signatureValue;
	}

	public String getValidTill() {
		return validTill;
	}

	public X509Certificate getCertificate() {
		return certificate;
	}

}
