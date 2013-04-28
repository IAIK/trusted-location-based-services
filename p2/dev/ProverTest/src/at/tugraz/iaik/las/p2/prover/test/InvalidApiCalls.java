package at.tugraz.iaik.las.p2.prover.test;

import java.security.Security;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import android.util.Log;
import at.tugraz.iaik.las.p2.common.TagCrypto;
import at.tugraz.iaik.las.p2.prover.ProtocolSession;
import at.tugraz.iaik.las.p2.prover.ProverApp;
import at.tugraz.iaik.las.p2.prover.TtpApiAsyncTask;
import at.tugraz.iaik.las.p2.prover.server.ProxyFactory;
import at.tugraz.iaik.las.p2.ttp.server.GetLttResponse;
import at.tugraz.iaik.las.p2.ttp.server.GetNonceResponse;
import at.tugraz.iaik.las.p2.ttp.server.GetTLttResponse;
import at.tugraz.iaik.las.p2.ttp.server.TtpApi;

/**
 * Test case to check for invalid parameters on API calls. Server always has to
 * return an error!
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class InvalidApiCalls extends TestCase {

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
	}

	public void testGetNonceWithNonceInvalidLength() throws Exception {

		TtpApiAsyncTask<GetNonceResponse> taskGetNonce = new TtpApiAsyncTask<GetNonceResponse>(
				null) {
			@Override
			protected void onSuccess(GetNonceResponse response,
					ProtocolSession protocolSession) {
				assertTrue(false);
				return;
			}

			@Override
			protected void onError(GetNonceResponse response) {
				assertTrue(true);
			}

			@Override
			protected void onException(Exception e) {
				assertTrue(false);
			}

			@Override
			protected GetNonceResponse doApiCall(TtpApi api,
					ProtocolSession protocolSession) {
				return api.getNonce(new byte[] { 0x00, 0x01 });
			}

		};
		taskGetNonce.execute();
		taskGetNonce.get(30, TimeUnit.SECONDS);
	}

	public void testGetNonceWithNonceIsNull() throws Exception {

		TtpApiAsyncTask<GetNonceResponse> taskGetNonce = new TtpApiAsyncTask<GetNonceResponse>(
				null) {
			@Override
			protected void onSuccess(GetNonceResponse response,
					ProtocolSession protocolSession) {
				assertTrue(false);
				return;
			}

			@Override
			protected void onError(GetNonceResponse response) {
				assertTrue(true);
			}

			@Override
			protected void onException(Exception e) {
				assertTrue(false);
			}

			@Override
			protected GetNonceResponse doApiCall(TtpApi api,
					ProtocolSession protocolSession) {
				return api.getNonce(null);
			}

		};
		taskGetNonce.execute();
		taskGetNonce.get(30, TimeUnit.SECONDS);
	}

	public void testGetLttAllParamsNull() throws Exception {

		TtpApiAsyncTask<GetLttResponse> taskGetNonce = new TtpApiAsyncTask<GetLttResponse>(
				null) {
			@Override
			protected void onSuccess(GetLttResponse response,
					ProtocolSession protocolSession) {
				assertTrue(false);
				return;
			}

			@Override
			protected void onError(GetLttResponse response) {
				assertTrue(true);
			}

			@Override
			protected void onException(Exception e) {
				assertTrue(false);
			}

			@Override
			protected GetLttResponse doApiCall(TtpApi api,
					ProtocolSession protocolSession) {
				return api.getLtt(null, null);
			}

		};
		taskGetNonce.execute();
		taskGetNonce.get(30, TimeUnit.SECONDS);
	}

	public void testGetLttParamsInvalidLength() throws Exception {

		TtpApiAsyncTask<GetLttResponse> taskGetNonce = new TtpApiAsyncTask<GetLttResponse>(
				null) {
			@Override
			protected void onSuccess(GetLttResponse response,
					ProtocolSession protocolSession) {
				assertTrue(false);
				return;
			}

			@Override
			protected void onError(GetLttResponse response) {
				assertTrue(true);
			}

			@Override
			protected void onException(Exception e) {
				assertTrue(false);
			}

			@Override
			protected GetLttResponse doApiCall(TtpApi api,
					ProtocolSession protocolSession) {
				return api.getLtt(new byte[] { 0x00 }, new byte[] { 0x00 });
			}

		};
		taskGetNonce.execute();
		taskGetNonce.get(30, TimeUnit.SECONDS);
	}

	public void testGetTLttAllParamsNull() throws Exception {

		TtpApiAsyncTask<GetTLttResponse> taskGetNonce = new TtpApiAsyncTask<GetTLttResponse>(
				null) {
			@Override
			protected void onSuccess(GetTLttResponse response,
					ProtocolSession protocolSession) {
				assertTrue(false);
				return;
			}

			@Override
			protected void onError(GetTLttResponse response) {
				assertTrue(true);
			}

			@Override
			protected void onException(Exception e) {
				assertTrue(false);
			}

			@Override
			protected GetTLttResponse doApiCall(TtpApi api,
					ProtocolSession protocolSession) {
				return api.getTLtt(null, null);
			}

		};
		taskGetNonce.execute();
		taskGetNonce.get(30, TimeUnit.SECONDS);
	}

	public void testGetTLttAllParamsInvalid() throws Exception {

		TtpApiAsyncTask<GetTLttResponse> taskGetNonce = new TtpApiAsyncTask<GetTLttResponse>(
				null) {
			@Override
			protected void onSuccess(GetTLttResponse response,
					ProtocolSession protocolSession) {
				assertTrue(false);
				return;
			}

			@Override
			protected void onError(GetTLttResponse response) {
				assertTrue(true);
			}

			@Override
			protected void onException(Exception e) {
				assertTrue(false);
			}

			@Override
			protected GetTLttResponse doApiCall(TtpApi api,
					ProtocolSession protocolSession) {
				return api.getTLtt("", new byte[] { 0x00 });
			}

		};
		taskGetNonce.execute();
		taskGetNonce.get(30, TimeUnit.SECONDS);
	}

	public void testGetTLttUnknownNonce() throws Exception {

		TtpApiAsyncTask<GetTLttResponse> taskGetNonce = new TtpApiAsyncTask<GetTLttResponse>(
				null) {
			@Override
			protected void onSuccess(GetTLttResponse response,
					ProtocolSession protocolSession) {
				assertTrue(false);
				return;
			}

			@Override
			protected void onError(GetTLttResponse response) {
				assertTrue(true);
			}

			@Override
			protected void onException(Exception e) {
				assertTrue(false);
			}

			@Override
			protected GetTLttResponse doApiCall(TtpApi api,
					ProtocolSession protocolSession) {
				return api.getTLtt("XML", TagCrypto.createRandomNonce());
			}

		};
		taskGetNonce.execute();
		taskGetNonce.get(30, TimeUnit.SECONDS);
	}
}
