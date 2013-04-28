package at.tugraz.iaik.las.p2.prover.cryptotag;

/***
 * Common interface for both, actual hardware tags (like the "CryptaTag"), and
 * for software mock objects.
 * 
 * The term "crypto tag" refers to any real or simulated NFC-tag, whereas the
 * term "CryptaTag" refers to the IAIK product, the
 * "CRYptographic Protected TAg".
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public interface ICryptoTag {
	/**
	 * Returns the UID of the tag as a byte array.
	 * 
	 * @return UID of Tag. In case of an error null is returned.
	 */
	public byte[] getUid();

	/**
	 * Signs the data byte array using the tag's private ECC key.
	 * 
	 * @param data
	 *            byte array data to sign.
	 * @return signed byte array. Returns null in case of an error.
	 */
	public byte[] sign(byte[] data);

}
