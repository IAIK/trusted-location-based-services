package at.tugraz.iaik.las.p2.prover.cryptotag.mock;

import iaik.x509.X509Certificate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;
import android.util.Log;
import at.tugraz.iaik.las.p2.common.TagCrypto;
import at.tugraz.iaik.las.p2.common.Utils;
import at.tugraz.iaik.las.p2.prover.ProverApp;

/**
 * Responsible for creating, storing and retrieving mock tags from the external
 * storage of the phone.
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class MockTagManager {
	private static final String mockTagParentDirectoryName = "mocktags";
	private static final String certificateFileName = "PublicKeyCert.cer";
	private static final String publicKeyFileName = "public.key";
	private static final String privateKeyFileName = "private.key";

	private String directory;

	public MockTagManager() {
		this.directory = String.format("%s/%s/%s",
				Environment.getExternalStorageDirectory(),
				ProverApp.extUsbDataDirectory,
				MockTagManager.mockTagParentDirectoryName);
	}

	public static void saveCertificate(String folder,
			X509Certificate certificate) {
		String path = String.format("%s/%s", folder,
				MockTagManager.certificateFileName);

		FileOutputStream fos;
		try {
			fos = new FileOutputStream(path);
			fos.write(certificate.getEncoded());
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static X509Certificate loadCertificate(String folder) {
		String path = String.format("%s/%s", folder,
				MockTagManager.certificateFileName);

		X509Certificate certificate = null;
		try {
			FileInputStream fis = new FileInputStream(path);
			certificate = new X509Certificate(fis);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return certificate;
	}

	private String getMockTagDir(String uidString) {
		return String.format("%s/%s", this.directory, uidString);
	}

	public List<MockCryptoTag> getAvailableMockTags() {
		List<MockCryptoTag> tags = new ArrayList<MockCryptoTag>();

		File dir = new File(String.format("%s", this.directory));

		if (!dir.exists()) {
			dir.mkdirs();
		}

		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				MockCryptoTag mockTag;
				try {
					mockTag = new MockCryptoTag(this, file.getName());
					tags.add(mockTag);
				} catch (Exception e) {
					Log.w(ProverApp.P,
							"Could not load Mock Tag: " + file.getPath());
					e.printStackTrace();
				}
			}
		}

		return tags;
	}

	public String createAnotherMockTag() throws Exception {
		byte[] uid = TagCrypto.createRandomUid();
		String path = this.getMockTagDir(Utils.byteArrayToHexString(uid));
		new File(path).mkdirs();
		KeyPair kp = TagCrypto.generateKeyPair();
		X509Certificate cert = TagCrypto.generateMockTagCert(kp);
		MockTagManager.saveCertificate(path, cert);
		MockTagManager.saveKeyPair(path, kp);

		// test
		byte[] nonce = TagCrypto.createRandomNonce();
		// sign
		KeyPair kp2 = loadKeyPair(Utils.byteArrayToHexString(uid));
		byte[] sig = TagCrypto.signData(nonce, kp2.getPrivate());
		// verify

		X509Certificate certLoaded = MockTagManager.loadCertificate(path);
		System.out.println(TagCrypto.verifySig(nonce,
				certLoaded.getPublicKey(), sig));
		Log.d(ProverApp.P, String.format("Mock Tag created at %s", path));
		return path;
	}

	/**
	 * http://snipplr.com/view/18368/
	 * 
	 * @param path
	 * @param keyPair
	 * @throws IOException
	 */
	public static void saveKeyPair(String path, KeyPair keyPair)
			throws IOException {
		PrivateKey privateKey = keyPair.getPrivate();
		PublicKey publicKey = keyPair.getPublic();

		// Store Public Key.
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
				publicKey.getEncoded());
		FileOutputStream fos = new FileOutputStream(String.format("%s/%s",
				path, MockTagManager.publicKeyFileName));
		fos.write(x509EncodedKeySpec.getEncoded());
		fos.close();

		// Store Private Key.
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
				privateKey.getEncoded());
		fos = new FileOutputStream(String.format("%s/%s", path,
				MockTagManager.privateKeyFileName));
		fos.write(pkcs8EncodedKeySpec.getEncoded());
		fos.close();
	}

	/**
	 * http://snipplr.com/view/18368/
	 * 
	 * @param path
	 * @param algorithm
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public KeyPair loadKeyPair(String uidString) {
		PublicKey publicKey = null;
		PrivateKey privateKey = null;

		String path = this.getMockTagDir(uidString);

		try {

			// Read Public Key.
			String pubFilePath = String.format("%s/%s", path,
					MockTagManager.publicKeyFileName);
			File filePublicKey = new File(pubFilePath);
			FileInputStream fis = new FileInputStream(pubFilePath);
			byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
			fis.read(encodedPublicKey);
			fis.close();

			// Read Private Key.
			String privFilePath = String.format("%s/%s", path,
					MockTagManager.privateKeyFileName);
			File filePrivateKey = new File(privFilePath);
			fis = new FileInputStream(privFilePath);
			byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
			fis.read(encodedPrivateKey);
			fis.close();

			// Generate KeyPair.
			KeyFactory keyFactory = KeyFactory.getInstance("ECDSA");
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
					encodedPublicKey);
			publicKey = keyFactory.generatePublic(publicKeySpec);
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
					encodedPrivateKey);
			privateKey = keyFactory.generatePrivate(privateKeySpec);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return new KeyPair(publicKey, privateKey);
	}
}
