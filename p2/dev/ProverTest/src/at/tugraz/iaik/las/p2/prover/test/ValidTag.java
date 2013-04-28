package at.tugraz.iaik.las.p2.prover.test;

import java.security.Security;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import android.util.Log;
import at.tugraz.iaik.las.p2.common.Utils;
import at.tugraz.iaik.las.p2.prover.ProtocolSession;
import at.tugraz.iaik.las.p2.prover.ProverApp;
import at.tugraz.iaik.las.p2.prover.TtpApiAsyncTask;
import at.tugraz.iaik.las.p2.prover.cryptotag.mock.MockCryptoTag;
import at.tugraz.iaik.las.p2.prover.cryptotag.mock.MockTagManager;
import at.tugraz.iaik.las.p2.prover.server.ProxyFactory;
import at.tugraz.iaik.las.p2.ttp.server.GetLttResponse;
import at.tugraz.iaik.las.p2.ttp.server.GetNonceResponse;
import at.tugraz.iaik.las.p2.ttp.server.TtpApi;

/**
 * Before running these tests, make sure the corrects UIDs and Public Key
 * Certificates are in the TTPs database (the server must know the tags used in
 * this test case).
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class ValidTag extends TestCase {

	private MockTagManager manager;

	static {
		Security.addProvider(new iaik.security.provider.IAIK());
		Security.addProvider(new iaik.security.ecc.provider.ECCProvider(true));
		// Android includes BouncyCastle, hence we must remove it to not
		// interfere with the IAIK JCE
		Security.removeProvider("BC");
	}

	public void setUp() {
		ClassLoader cl = TtpApi.class.getClassLoader();
		if (cl == null) {
			Log.e(ProverApp.PT, "No class loader.");
		}
		ProxyFactory.init(cl, Preferences.ttpApiUrl);
		Log.d(ProverApp.P, "Server: " + Preferences.ttpApiUrl);

		this.manager = new MockTagManager();
	}

	public void testMockTag13C8376DD5E5B6() throws Exception {
		this.mockTagTestGeneric(new MockCryptoTag(this.manager,
				"13C8376DD5E5B6"));
	}

	public void testMockTag55C6193F64D016() throws Exception {
		this.mockTagTestGeneric(new MockCryptoTag(this.manager,
				"55C6193F64D016"));
	}

	public void mockTagTestGeneric(final MockCryptoTag mockTag)
			throws Exception {
		ProtocolSession session = new ProtocolSession();

		TtpApiAsyncTask<GetNonceResponse> taskGetNonce = new TtpApiAsyncTask<GetNonceResponse>(
				session) {
			@Override
			protected void onSuccess(GetNonceResponse response,
					ProtocolSession protocolSession) {
				assertFalse(response.HasError);
				assertNotNull(response.Nonce);
				assertEquals(16, response.Nonce.length);
				Log.d(ProverApp.PT,
						String.format("Received Nonce: %s",
								Utils.byteArrayToHexString(response.Nonce)));
				protocolSession.Nonce = response.Nonce;
			}

			@Override
			protected void onError(GetNonceResponse response) {
				assertTrue(false);
			}

			@Override
			protected void onException(Exception e2) {
				assertTrue(false);
			}

			@Override
			protected GetNonceResponse doApiCall(TtpApi api,
					ProtocolSession protocolSession) {
				return api.getNonce(mockTag.getUid());
			}

		};
		taskGetNonce.execute();
		taskGetNonce.get(30, TimeUnit.SECONDS);

		TtpApiAsyncTask<GetLttResponse> taskGetLtt = new TtpApiAsyncTask<GetLttResponse>(
				session) {

			@Override
			protected void onSuccess(GetLttResponse response,
					ProtocolSession protocolSession) {
				assertFalse(response.HasError);
				assertNotNull(response.Ltt);
				Log.d(ProverApp.PT,
						String.format("Received LTT: %s", response.Ltt));
				assertTrue(true);
			}

			@Override
			protected void onError(GetLttResponse response) {
				assertTrue(false);
			}

			@Override
			protected void onException(Exception e2) {
				assertTrue(false);
			}

			@Override
			protected GetLttResponse doApiCall(TtpApi api,
					ProtocolSession protocolSession) {
				byte[] signedNonce = null;
				try {
					signedNonce = mockTag.sign(protocolSession.Nonce);
				} catch (Exception e) {
					e.printStackTrace();
					assertTrue(false);
				}
				return api.getLtt(protocolSession.Nonce, signedNonce);
			}
		};
		taskGetLtt.execute();
		taskGetLtt.get(30, TimeUnit.SECONDS);
	}
}
