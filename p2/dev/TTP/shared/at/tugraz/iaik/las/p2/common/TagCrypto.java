package at.tugraz.iaik.las.p2.common;

import iaik.asn1.ObjectID;
import iaik.asn1.structures.AlgorithmID;
import iaik.asn1.structures.Name;
import iaik.x509.X509Certificate;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Collection of cryptographic operations related to a crypto tag.
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class TagCrypto {
	public static final int CRYPTO_TAG_NONCE_LENGTH = 16;
	public static final int CRYPTO_TAG_UID_LENGTH = 7;

	public static byte[] createRandomNonce() {
		return TagCrypto.createRandomBytes(CRYPTO_TAG_NONCE_LENGTH);
	}

	public static byte[] createRandomUid() {
		return TagCrypto.createRandomBytes(CRYPTO_TAG_UID_LENGTH);
	}

	public static boolean verifySig(byte[] data, PublicKey key, byte[] sig)
			throws Exception {
		Signature signer = Signature.getInstance("ECDSA", "IAIK_ECC");
		signer.initVerify(key);
		signer.update(data);
		return (signer.verify(sig));
	}

	public static byte[] signData(byte[] data, PrivateKey key) {
		byte[] signedData = null;
		try {
			Signature signer = Signature.getInstance("ECDSA", "IAIK_ECC");
			signer.initSign(key);
			signer.update(data);
			signedData = signer.sign();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return signedData;
	}

	private static byte[] createRandomBytes(int nrOfBytes) {
		byte[] nonce = new byte[nrOfBytes];

		try {
			SecureRandom.getInstance("SHA1PRNG").nextBytes(nonce);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return (nonce);
	}

	public static KeyPair generateKeyPair() throws Exception {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA",
				"IAIK_ECC");
		SecureRandom rng = SecureRandom.getInstance("SHA1PRNG");
		rng.setSeed((new Date().getTime()));
		keyGen.initialize(1024);
		return keyGen.generateKeyPair();
	}

	public static X509Certificate generateMockTagCert(KeyPair kp)
			throws Exception {

		X509Certificate c = new X509Certificate();

		// serial
		c.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		// subject & issuer
		Name issuer = new Name();
		// issuer.addRDN(ObjectID.country, "AT");
		// issuer.addRDN(ObjectID.organization, "TU Graz");
		// issuer.addRDN(ObjectID.organizationalUnit, "IAIK");
		issuer.addRDN(ObjectID.commonName, "IAIK Test CA");
		c.setSubjectDN(issuer);
		c.setIssuerDN(issuer); // use the same
		// validity
		GregorianCalendar date = (GregorianCalendar) Calendar.getInstance();
		c.setValidNotBefore(date.getTime());
		date.add(Calendar.MONTH, 6);
		c.setValidNotAfter(date.getTime());
		// public key
		c.setPublicKey(kp.getPublic());
		// signature algorithm
		// c.setSignatureAlgorithm(AlgorithmID.ecdsa_With_SHA1);

		c.sign(AlgorithmID.ecdsa, kp.getPrivate());
		try {
			c.checkValidity();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return c;

	}
}
