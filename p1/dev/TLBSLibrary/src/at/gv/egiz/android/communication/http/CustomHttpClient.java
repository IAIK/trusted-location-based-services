package at.gv.egiz.android.communication.http;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

/**
 * This class represents a custom implementation of
 * org.apache.http.impl.client.DefaultHttpClient. As Android does not contain
 * various certificates from trusted CA's we have to provide the system with our
 * own certstore and the corresponding truststore in order to establish a SSL
 * connection with the ATrust server for the creation of the Mobile Phone
 * Signature.
 * 
 * Code adapted from
 * http://blog.antoine.li/index.php/2010/10/android-trusting-ssl-certificates/
 * 
 * @author sandra.kreuzhuber@iaik.tugraz.at
 */
public class CustomHttpClient extends DefaultHttpClient {

	private InputStream certstore;
	private String certstorePassword;
	private InputStream truststore;
	private String truststorePassword;

	/**
	 * 
	 * @param certstore
	 * @param certstorePassword
	 * @param truststore
	 * @param truststorePassword
	 */
	public CustomHttpClient(InputStream certstore, String certstorePassword,
			InputStream truststore, String truststorePassword) {
		this.certstore = certstore;
		this.certstorePassword = certstorePassword;
		this.truststore = truststore;
		this.truststorePassword = truststorePassword;
	}

	@Override
	protected ClientConnectionManager createClientConnectionManager() {
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		// Register for port 443 our SSLSocketFactory with our keystore
		// to the ConnectionManager
		registry.register(new Scheme("https", newSslSocketFactory(), 443));
		return new SingleClientConnManager(getParams(), registry);
	}

	private SSLSocketFactory newSslSocketFactory() {
		try {
			// Get an instance of the Bouncy Castle KeyStores
			KeyStore trustStore = KeyStore.getInstance("BKS");
			KeyStore certStore = KeyStore.getInstance("BKS");

			// Initialize the keystore with the provided trusted
			// certificates
			// Also provide the password of the keystore
			certStore.load(certstore, certstorePassword.toCharArray());

			// Initialize the keystore with the provided trusted
			// certificates
			// Also provide the password of the keystore
			trustStore.load(truststore, truststorePassword.toCharArray());

			// Pass the keystore to the SSLSocketFactory. The factory is
			// responsible
			// for the verification of the server certificate.
			SSLSocketFactory sf = new SSLSocketFactory(certStore,
					certstorePassword, trustStore);

			// Hostname verification from certificate
			// http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e506
			sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
			return sf;

		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
