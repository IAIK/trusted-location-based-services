package at.tugraz.iaik.las.p2.ttp.server;

import iaik.x509.X509Certificate;

import java.io.ByteArrayInputStream;
import java.security.Security;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import at.tugraz.iaik.las.p2.common.TagCrypto;

import com.caucho.hessian.server.HessianServlet;
import com.google.appengine.api.datastore.Text;

import at.tugraz.iaik.las.p2.ttp.client.data.ProtocolLogJdo;
import at.tugraz.iaik.las.p2.ttp.client.data.ProtocolStep;
import at.tugraz.iaik.las.p2.ttp.client.data.TagJdo;
import at.tugraz.iaik.las.p2.ttp.client.Utils;

/**
 * Implementation of the TTP API interface.
 * 
 * Remark: Unfortunately, with HessianServlet it seems not to be possible to get
 * information about the client that sent the request (e.g. IP). Would have been
 * nice for logging purposes...
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
@SuppressWarnings("serial")
public class TtpApiImpl extends HessianServlet implements TtpApi {

	static {
		// add IAIK JCE and ECC security provider
		Security.addProvider(new iaik.security.provider.IAIK());
		Security.addProvider(new iaik.security.ecc.provider.ECCProvider(true));
	}

	private static final Logger log = Logger.getLogger(TtpDataServiceImpl.class
			.getName());

	@Override
	public String hallo(String clientId) {
		log.info("TtpApiImpl.hallo");

		PersistenceManager pm = PMF.get();

		ProtocolLogJdo plog = new ProtocolLogJdo();
		plog.Timestamp = new java.util.Date();
		plog.ProtocolStep = ProtocolStep.HELLO;
		plog.StatusMessage = "Hello test request by Prover.";

		this.persistProtocolLogJdo(pm, plog);

		return String.format("Hallo %s, it is " + new java.util.Date(),
				clientId);
	}

	@Override
	public GetNonceResponse getNonce(byte[] uid) {
		log.info("TtpApiImpl.getMyNonce");

		// variable used to check whether an error occurred (if not null)
		GetNonceResponse response = null;

		PersistenceManager pm = PMF.get();

		ProtocolLogJdo plog = new ProtocolLogJdo();
		plog.Timestamp = new java.util.Date();
		plog.ProtocolStep = ProtocolStep.GET_NONCE;

		// validate parameters
		if (uid == null || uid.length != TagCrypto.CRYPTO_TAG_UID_LENGTH) {
			plog.StatusMessage = "Invalid parameters for getNonce request.";
			response = GetNonceResponse.error(plog.StatusMessage);
		}

		// look for tag UID
		if (response == null) {
			plog.Uid = Utils.byteArrayToHexString(uid);
			TagJdo tag = this.getTagByUid(pm, Utils.byteArrayToHexString(uid));
			if (tag == null) {
				// unknown tag/invalid uid
				plog.StatusMessage = String.format(
						"Tag (UID=%s) is not known to server.", plog.Uid);
				plog.Nonce = null;
				response = GetNonceResponse.error(plog.StatusMessage);
			}
		}

		if (response == null) {
			// known tag/valid uid
			plog.Nonce = Utils.byteArrayToHexString(TagCrypto
					.createRandomNonce());
			plog.StatusMessage = String.format(
					"Prover (UID=%s) requested a Nonce (%s).", plog.Uid,
					plog.Nonce);
			response = GetNonceResponse.ok(Utils
					.hexStringToByteArray(plog.Nonce));
		}

		this.persistProtocolLogJdo(pm, plog);
		pm.close();
		return response;
	}

	@Override
	public GetLttResponse getLtt(byte[] nonce, byte[] signedNonce) {
		log.info("TtpApiImpl.getTheLtt");

		// variable used to check whether an error occurred (if not null)
		GetLttResponse response = null;

		PersistenceManager pm = PMF.get();

		ProtocolLogJdo plogGetNonce = null;
		TagJdo tag = null;

		ProtocolLogJdo plogGetLtt = new ProtocolLogJdo();
		plogGetLtt.Timestamp = new java.util.Date();
		plogGetLtt.ProtocolStep = ProtocolStep.GET_LTT;

		// validate parameters
		if (nonce == null || nonce.length != TagCrypto.CRYPTO_TAG_NONCE_LENGTH
				|| signedNonce == null || signedNonce.length <= 0) {
			plogGetLtt.StatusMessage = "Invalid parameters for getLtt request.";
			response = GetLttResponse.error(plogGetLtt.StatusMessage);
		}

		// get previous getNonce request for this getLtt request
		if (response == null) {
			plogGetLtt.Nonce = Utils.byteArrayToHexString(nonce);
			plogGetNonce = this.getProtocolLogByNonce(pm, nonce,
					ProtocolStep.GET_NONCE);
			if (plogGetNonce == null) {
				// no previous request for this nonce
				plogGetLtt.StatusMessage = String
						.format("Request for LTT with Nonce=%s invalid. No matching GET_NONCE request found.",
								Utils.byteArrayToHexString(nonce));
				response = GetLttResponse.error(plogGetLtt.StatusMessage);
			}
		}

		// check for timeout
		if (response == null) {
			if (ServerLogic.isTimeoutBetweenGetNonceAndGetLtt(plogGetNonce,
					plogGetLtt)) {
				// timeout
				plogGetLtt.StatusMessage = String.format(
						"Timeout for LTT Request with Nonce=%s.",
						Utils.byteArrayToHexString(nonce));
				plogGetLtt.Uid = plogGetNonce.Uid;
				response = GetLttResponse.error(plogGetLtt.StatusMessage);
			}
		}

		// check nonce signature
		if (response == null) {
			tag = this.getTagByUid(pm, plogGetNonce.Uid);
			boolean validSignature = false;
			X509Certificate x509cert = null;
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(
						tag.PublicKeyCertificateFile.Bytes);
				x509cert = new X509Certificate(bais);
				x509cert.checkValidity();
				if (TagCrypto.verifySig(
						Utils.hexStringToByteArray(plogGetNonce.Nonce),
						x509cert.getPublicKey(), signedNonce)) {
					validSignature = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
				validSignature = false;
			}
			if (!validSignature) {
				// invalid signature
				plogGetLtt.StatusMessage = String.format(
						"Invalid signature or Tag certificate invalid.",
						Utils.byteArrayToHexString(nonce));
				plogGetLtt.Uid = plogGetNonce.Uid;
				response = GetLttResponse.error(plogGetLtt.StatusMessage);
			}
		}

		// nonce signature verified successfully
		if (response == null) {
			// successful authentication with tag
			LocationTimeTicket ltt = new LocationTimeTicket();
			ltt.Time = new Date();
			ltt.LocationLatitude = tag.LocationLatitude;
			ltt.LocationLongitude = tag.LocationLongitude;
			String serializedLtt = TtpApiUtils.SerializeToXmlString(ltt);
			plogGetLtt.StatusMessage = String.format(
					"Tag authenticated. Created LTT: %s", serializedLtt);
			plogGetLtt.Uid = plogGetNonce.Uid;
			plogGetLtt.Ltt = serializedLtt;
			response = GetLttResponse.ok(ltt);
		}

		this.persistProtocolLogJdo(pm, plogGetLtt);
		pm.close();
		return response;
	}

	@Override
	public GetTLttResponse getTLtt(String signedLtt, byte[] nonce) {
		log.info("TtpApiImpl.getTLtt");

		// variable used to check whether an error occurred (if not null)
		GetTLttResponse response = null;

		ProtocolLogJdo plogGetLtt = null;
		LocationTimeTicket ltt = null;

		PersistenceManager pm = PMF.get();

		ProtocolLogJdo plogGetTLtt = new ProtocolLogJdo();
		plogGetTLtt.Timestamp = new java.util.Date();
		plogGetTLtt.ProtocolStep = ProtocolStep.GET_TLTT;

		// validate parameters
		if (signedLtt == null || signedLtt.length() <= 0 || nonce == null
				|| nonce.length != TagCrypto.CRYPTO_TAG_NONCE_LENGTH) {
			// no previous request for this nonce
			plogGetTLtt.StatusMessage = "Invalid parameters for getTLtt request.";
			response = GetTLttResponse.error(plogGetTLtt.StatusMessage);
		}

		if (response == null) {
			plogGetTLtt.Nonce = Utils.byteArrayToHexString(nonce);
			plogGetLtt = this.getProtocolLogByNonce(pm, nonce,
					ProtocolStep.GET_LTT);
			if (plogGetLtt == null) {
				// no previous request for this nonce
				plogGetTLtt.StatusMessage = String
						.format("Request for T-LTT with Nonce=%s invalid. No matching GET_LTT request found.",
								Utils.byteArrayToHexString(nonce));
				response = GetTLttResponse.error(plogGetTLtt.StatusMessage);
			}
		}

		// check for timeout
		if (response == null) {
			plogGetTLtt.Uid = plogGetLtt.Uid;
			if (ServerLogic.isTimeoutBetweenGetLttAndGetTLtt(plogGetLtt,
					plogGetTLtt)) {
				// timeout
				plogGetTLtt.StatusMessage = String.format(
						"Timeout for T-LTT Request with Nonce=%s.",
						Utils.byteArrayToHexString(nonce));
				response = GetTLttResponse.error(plogGetTLtt.StatusMessage);
			}
		}

		// get ltt from signedLtt
		if (response == null) {
			ltt = LocationTimeTicket.extractLttFromSignedLtt(signedLtt);
			if (ltt == null) {
				plogGetTLtt.StatusMessage = String
						.format("Could not extract LTT from signed LTT.");
				response = GetTLttResponse.error(plogGetTLtt.StatusMessage);
			}
		}

		// check if ltt was modified
		if (response == null) {
			plogGetTLtt.Ltt = plogGetLtt.Ltt;
			LocationTimeTicket lttOld = TtpApiUtils.deserializeFromXmlString(
					plogGetLtt.Ltt, LocationTimeTicket.class);
			if (!lttOld.isEqual(ltt)) {
				plogGetTLtt.StatusMessage = String.format(
						"LTT was modified since previous request. (%s vs. %s)",
						plogGetLtt.Ltt, TtpApiUtils.SerializeToXmlString(ltt));
				response = GetTLttResponse.error(plogGetTLtt.StatusMessage);
			}
		}

		// check prover signature on LTT
		if (response == null) {
			if (!ServerLogic.isProverSignatureValid(signedLtt)) {
				plogGetTLtt.StatusMessage = "Signature on LTT by Prover could not be validated.";
				response = GetTLttResponse.error(plogGetTLtt.StatusMessage);
			}
		}

		// sign the signed LTT
		if (response == null) {
			String tLtt = ServerLogic.signLtt(signedLtt);
			if (tLtt == null || tLtt.length() <= 0) {
				plogGetTLtt.StatusMessage = "TTP could not sign the LTT.";
				response = GetTLttResponse.error(plogGetTLtt.StatusMessage);
			} else {
				plogGetTLtt.TLtt = new Text(signedLtt);
			}
		}

		// protocol step finished successfully
		if (response == null) {
			plogGetTLtt.StatusMessage = String.format(
					"Successfully created T-LTT.",
					Utils.byteArrayToHexString(nonce));
			response = GetTLttResponse.ok(signedLtt);
		}

		this.persistProtocolLogJdo(pm, plogGetTLtt);
		pm.close();
		return response;
	}

	private ProtocolLogJdo getProtocolLogByNonce(PersistenceManager pm,
			byte[] nonce, ProtocolStep protocolStep) {
		Query q = pm
				.newQuery(ProtocolLogJdo.class,
						"this.Nonce == nonceString && this.ProtocolStep == protocolStep");
		q.declareParameters("java.lang.String nonceString, at.tugraz.iaik.las.p2.ttp.client.data.ProtocolStep protocolStep");
		String nonceParam = Utils.byteArrayToHexString(nonce);
		String stepParam = protocolStep.toString();
		@SuppressWarnings("unchecked")
		List<ProtocolLogJdo> result = (List<ProtocolLogJdo>) q.execute(
				nonceParam, stepParam);
		ProtocolLogJdo plog = null;
		if (result.isEmpty() || result.size() > 1) {
			plog = null;
		} else {
			plog = result.get(0);
		}
		q.closeAll();
		return plog;
	}

	private TagJdo getTagByUid(PersistenceManager pm, String uid) {
		Query q = pm.newQuery(TagJdo.class, "this.Uid == uidString");
		q.declareParameters("java.lang.String uidString");
		@SuppressWarnings("unchecked")
		List<TagJdo> result = (List<TagJdo>) q.execute(uid);
		TagJdo tag = null;
		if (result.isEmpty() || result.size() > 1) {
			tag = null;
		} else {
			tag = result.get(0);
		}
		q.closeAll();
		return tag;
	}

	private void persistProtocolLogJdo(PersistenceManager pm,
			ProtocolLogJdo plog) {
		try {
			pm.makePersistent(plog);
		} catch (Exception e) {
			// ignore, because:
			// When an exception is thrown on GAE (a servlet that extends
			// HessianServlet) it is not forwarded to the client, instead a
			// SecurityException is thrown.
			// http://bugs.caucho.com/view.php?id=4080
			log.warning(e.toString());
			log.warning(e.getMessage());
		}
	}
}
