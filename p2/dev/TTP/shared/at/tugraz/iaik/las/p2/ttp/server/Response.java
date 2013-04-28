package at.tugraz.iaik.las.p2.ttp.server;

import java.io.Serializable;

public abstract class Response implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public Response(boolean hasError, String message) {
		this.HasError = hasError;
		this.Message = message;
	}
	
	public final boolean HasError;
	
	public final String Message;

}
