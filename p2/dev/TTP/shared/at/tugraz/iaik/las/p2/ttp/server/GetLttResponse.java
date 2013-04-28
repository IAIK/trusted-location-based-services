package at.tugraz.iaik.las.p2.ttp.server;

public class GetLttResponse extends Response {

	private static final long serialVersionUID = 1L;

	public static GetLttResponse ok(LocationTimeTicket ltt) {
		return new GetLttResponse(//
				false, //
				"", //
				ltt);
	}
	
	public static GetLttResponse error(String message) {
		return new GetLttResponse(//
				true, //
				message, //
				null);
	}
	
	private GetLttResponse(boolean hasError, String message, LocationTimeTicket ltt) {
		super(hasError, message);
		this.Ltt = ltt;
	}
	
	public final LocationTimeTicket Ltt;
}
