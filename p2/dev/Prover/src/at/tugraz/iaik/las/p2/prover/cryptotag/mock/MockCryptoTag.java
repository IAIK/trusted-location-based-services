package at.tugraz.iaik.las.p2.prover.cryptotag.mock;

import java.security.KeyPair;
import at.tugraz.iaik.las.p2.common.TagCrypto;
import at.tugraz.iaik.las.p2.common.Utils;
import at.tugraz.iaik.las.p2.prover.cryptotag.ICryptoTag;

/**
 * A dynamic mock tag created at runtime for various test purposes.
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class MockCryptoTag implements ICryptoTag {

	private String uidString;
	private KeyPair keyPair;

	private MockTagManager manager;

	public MockCryptoTag(MockTagManager manager, String uidString)
			throws Exception {
		this.uidString = uidString;
		this.manager = manager;
		this.keyPair = this.manager.loadKeyPair(uidString);
		if (this.keyPair == null) {
			throw new Exception("Could not load Key Pair for Mock Tag.");
		}
	}

	public String toString() {
		return this.uidString;
	}

	public byte[] Nonce;

	@Override
	public byte[] getUid() {
		return Utils.hexStringToByteArray(this.uidString);
	}

	@Override
	public byte[] sign(byte[] nonce) {
		return TagCrypto.signData(nonce, this.keyPair.getPrivate());
	}

}
