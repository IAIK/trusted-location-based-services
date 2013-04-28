package at.tugraz.iaik.las.p2.ttp.server;

public interface TtpApi {
	public String hallo(String clientId);

	/**
	 * Requests a Nonce from the TTP server which will subsequently be signed by
	 * the Tag.
	 * 
	 * @param uid
	 *            An 7 Byte UID identifying the Tag.
	 * @return An 16 Byte Nonce if the Tag is known to the server and the
	 *         request is plausible, otherwise null.
	 */
	public GetNonceResponse getNonce(byte[] uid);

	/**
	 * Requests a Location Time Ticket from the TTP server by proving the Tags
	 * authenticity with the provided signed nonce.
	 * 
	 * @param nonce
	 *            The 16 Byte Nonce previously replied by the server.
	 * @param signedNonce
	 *            The Nonce signed by the Tag's private key.
	 * @return An XML string encoding the LTT.
	 */
	public GetLttResponse getLtt(byte[] nonce, byte[] signedNonce);

	/**
	 * Requests a Location Time Certificate from the TTP server by providing a
	 * signed Location Time Ticket (previously requested from server).
	 * 
	 * @param signedLtt
	 *            The LTT previously provided by the TTP server signed using the
	 *            austrian mobile phone signature.
	 * @return An XML string encoding the mutually singed LTT, which is called
	 *         T-LTT.
	 */
	public GetTLttResponse getTLtt(String signedLtt, byte[] nonce);
}
