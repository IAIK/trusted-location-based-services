package at.tugraz.iaik.las.p2.ttp.server;

public class GetNonceResponse extends Response {
	
	private static final long serialVersionUID = 1L;

	public static GetNonceResponse ok(byte[] nonce) {
		return new GetNonceResponse(//
				false, //
				"", //
				nonce);
	}
	
	public static GetNonceResponse error(String message) {
		return new GetNonceResponse(//
				true, //
				message, //
				null);
	}
	
	private GetNonceResponse(boolean hasError, String message, byte[] nonce) {
		super(hasError, message);
		this.Nonce = nonce;
	}
		
	public final byte[] Nonce;
}
