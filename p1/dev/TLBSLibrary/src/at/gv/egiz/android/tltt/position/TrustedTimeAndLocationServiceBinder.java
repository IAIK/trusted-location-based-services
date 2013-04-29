package at.gv.egiz.android.tltt.position;

import android.os.Binder;

/**
 * <a href=
 * "http://developer.android.com/guide/topics/fundamentals/bound-services.html#Binder"
 * >Extends the Binder class.</a> Enables direct access to the {@link #service
 * TrustedTimeAndLocationService} public methods.
 * 
 */
public class TrustedTimeAndLocationServiceBinder extends Binder {
	protected TrustedTimeAndLocationService service;

	TrustedTimeAndLocationServiceBinder(TrustedTimeAndLocationService service) {
		this.service = service;
	}

	public TrustedTimeAndLocationService getService() {
		return service;
	}

}
