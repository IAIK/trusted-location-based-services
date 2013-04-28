package at.tugraz.iaik.las.p2.prover;

/**
 * Indicate the stage on which the protocol currently executes.
 * 
 * @author christian.lesjak@student.tugraz.at
 *
 */
public enum ProtocolState {
	READY,
	GET_UID_FROM_TAG,
	GET_NONCE_FROM_TTP,
	GET_SIGNED_NONCE_FROM_TAG,
	GET_LTT_FROM_TTP,
	GET_LTT_SIGNED_BY_MOBILE_PHONE_SIGNATURE,
	GET_TLTT_FROM_TTP,
	DONE;
}
