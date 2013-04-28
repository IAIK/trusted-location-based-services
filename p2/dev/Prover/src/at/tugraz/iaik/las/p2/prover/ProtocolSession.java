package at.tugraz.iaik.las.p2.prover;

import at.tugraz.iaik.las.p2.ttp.server.LocationTimeTicket;

public class ProtocolSession {
	public byte[] Uid;
	public byte[] Nonce;
	public byte[] SignedNonce;
	public LocationTimeTicket Ltt;
	public String SignedLttAsXmlString;
	public String TLttAsXmlString;
	public String TLttPath;
}
