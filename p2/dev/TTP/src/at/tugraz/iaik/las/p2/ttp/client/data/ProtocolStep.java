package at.tugraz.iaik.las.p2.ttp.client.data;

/**
 * Identifies the step to which a Protocol Log belongs.
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public enum ProtocolStep {
	
	/**
	 * A simple hello test request, not part of protocol.
	 */
	HELLO,
	
	/**
	 * The step where the Prover requests a nonce.
	 */
	GET_NONCE, 
	
	/**
	 * The step where the Prover request an LTT.
	 */
	GET_LTT, 
	
	/**
	 * The step where the Prover requests an T-LTT.
	 */
	GET_TLTT
}
