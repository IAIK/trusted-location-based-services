package at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.util;

/**
 * 
 * @author Sandra Kreuzhuber
 * @author Thomas Zefferer
 * 
 */
public class SessionData {

	private String sessionID = null;
	private String viewstate = null;
	private String eventvalidation = null;
	private String prefix = null;
	private String mobilePhoneNumber = null;
	private String signaturePassword = null;
	private String buttonIdentification = "Identifizieren";
	private String signButton = "Signieren";
	private String buttonBack = "Zurück";
	private String referenceValue = null;
	private String tan = null;
	private String signature = null;

	public SessionData() {

	}

	public String getSignaturePassword() {
		return signaturePassword;
	}

	public void setSignaturePassword(String signaturePassword) {
		this.signaturePassword = signaturePassword;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public String getViewstate() {
		return viewstate;
	}

	public void setViewstate(String viewstate) {
		this.viewstate = viewstate;
	}

	public String getEventvalidation() {
		return eventvalidation;
	}

	public void setEventvalidation(String eventvalidation) {
		this.eventvalidation = eventvalidation;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getTan() {
		return tan;
	}

	public void setTan(String tan) {
		this.tan = tan;
	}

	public String getReferenceValue() {
		return referenceValue;
	}

	public void setReferenceValue(String referenceValue) {
		this.referenceValue = referenceValue;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getMobilePhoneNumber() {
		return mobilePhoneNumber;
	}

	public void setMobilePhoneNumber(String mobilePhoneNumber) {
		this.mobilePhoneNumber = mobilePhoneNumber;
	}

	public String getButtonIdentification() {
		return buttonIdentification;
	}

	public String getSignButton() {
		return signButton;
	}

	private SessionData(SessionData toCopy) {
		if (this.buttonIdentification != null)
			this.buttonIdentification = new String(
					toCopy.getButtonIdentification());
		if (this.eventvalidation != null)
			this.eventvalidation = new String(toCopy.getEventvalidation());
		if (this.mobilePhoneNumber != null)
			this.mobilePhoneNumber = new String(toCopy.getMobilePhoneNumber());
		if (this.prefix != null)
			this.prefix = new String(toCopy.getPrefix());
		if (this.referenceValue != null)
			this.referenceValue = new String(toCopy.getReferenceValue());
		if (this.sessionID != null)
			this.sessionID = new String(toCopy.getSessionID());
		if (this.signaturePassword != null)
			this.signaturePassword = new String(toCopy.getSignaturePassword());
		if (this.signButton != null)
			this.signButton = new String(toCopy.getSignButton());
		if (this.tan != null)
			this.tan = new String(toCopy.getTan());
		if (this.viewstate != null)
			this.viewstate = new String(toCopy.getViewstate());
		if (this.signature != null)
			this.signature = new String(toCopy.getSignature());
	}

	/**
	 * Returns a deep copy.
	 * 
	 * @return a deep copy
	 */
	public SessionData getCopy() {
		return new SessionData(this);
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getSignature() {
		return this.signature;
	}

	public String getBackButton() {
		return buttonBack;
	}

}