package at.tugraz.iaik.las.p2.ttp.server;

import at.tugraz.iaik.las.p2.ttp.client.data.ProtocolLogJdo;

public class ServerLogic {
	public static boolean isTimeoutBetweenGetLttAndGetTLtt(
			ProtocolLogJdo pLogGetLtt, ProtocolLogJdo pLogGetTLtt) {
		if ((pLogGetTLtt.Timestamp.getTime() - pLogGetLtt.Timestamp
					.getTime()) > 90000) {
			return true;
		}
		return false;
	}
	
	public static boolean isTimeoutBetweenGetNonceAndGetLtt(ProtocolLogJdo pLogGetNonce, ProtocolLogJdo pLogGetLtt) {
		 if ((pLogGetLtt.Timestamp.getTime() - pLogGetNonce.Timestamp
				.getTime()) > 20000) {
			 return true;
		 }
		 return false;
	}
	
	public static boolean isProverSignatureValid(String signedLtt) {
		// TODO check Prover signature on LTT
		return true;
	}
	
	public static String signLtt(String signedLtt) {
		// TODO sign LTT (which is already signed by Prover)
		return signedLtt;
	}
}
