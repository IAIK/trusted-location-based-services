package at.tugraz.iaik.las.p2.prover.cryptotag.mock;

import java.security.KeyPair;
import at.tugraz.iaik.las.p2.common.TagCrypto;
import at.tugraz.iaik.las.p2.common.Utils;
import at.tugraz.iaik.las.p2.prover.cryptotag.ICryptoTag;

/**
 * A mock tag that generates a random UID upon instantiation. Used to test the
 * server that it declines to run the protocol with an unknown tag UID.
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class RandomMockCryptoTag implements ICryptoTag {

	private String name;
	private byte[] uid;
	private KeyPair keyPair;

	public RandomMockCryptoTag() {
		this.uid = TagCrypto.createRandomUid();
		this.name = String.format("Random Mock Tag %s",
				Utils.byteArrayToHexString(this.uid));
		try {
			this.keyPair = TagCrypto.generateKeyPair();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void disconnect() {
	}

	public byte[] getUid() {
		return this.uid;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public byte[] sign(byte[] nonce) {
		return TagCrypto.signData(nonce, this.keyPair.getPrivate());
	}
}
