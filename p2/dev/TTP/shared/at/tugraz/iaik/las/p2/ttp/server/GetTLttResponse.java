package at.tugraz.iaik.las.p2.ttp.server;

public class GetTLttResponse extends Response {

	private static final long serialVersionUID = 1L;

	public static GetTLttResponse ok(String tLttAsXml) {
		return new GetTLttResponse(//
				false, //
				"", //
				tLttAsXml);
	}
	
	public static GetTLttResponse error(String message) {
		return new GetTLttResponse(//
				true, //
				message, //
				null);
	}
	
	private GetTLttResponse(boolean hasError, String message, String tLttXmlString) {
		super(hasError, message);
		this.TLttXmlString = tLttXmlString;
	}
	
	public final String TLttXmlString;

}
