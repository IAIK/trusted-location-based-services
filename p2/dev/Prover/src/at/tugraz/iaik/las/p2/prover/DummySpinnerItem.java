package at.tugraz.iaik.las.p2.prover;

import at.tugraz.iaik.las.p2.prover.cryptotag.ICryptoTag;

/**
 * A dummy entry for the Spinner UI Element to display a header entry.
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class DummySpinnerItem implements ICryptoTag {

	public void disconnect() {
	}

	public byte[] getUid() {
		return null;
	}

	@Override
	public String toString() {
		return "Select Mock Tag...";
	}

	@Override
	public byte[] sign(byte[] nonce) {
		return null;
	}

}
