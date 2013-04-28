package at.tugraz.iaik.las.p2.prover;

import android.os.AsyncTask;
import at.tugraz.iaik.las.p2.prover.server.ProxyFactory;
import at.tugraz.iaik.las.p2.ttp.server.Response;
import at.tugraz.iaik.las.p2.ttp.server.TtpApi;

public abstract class TtpApiAsyncTask<T3 extends Response> extends AsyncTask<Void, Void, T3> {

	private Exception e = null;
	private ProtocolSession protocolSession = null;
	
	public TtpApiAsyncTask(ProtocolSession protocolSession) {
		super();
		this.protocolSession = protocolSession;
	}

	@Override
	protected T3 doInBackground(Void... params) {
		TtpApi api = ProxyFactory.getProxy(TtpApi.class, "api");
		try {
			return this.doApiCall(api, this.protocolSession);
		} catch (Exception e) {
			this.e = e;
			return null;
		}
	}
	
	protected void onPostExecute(T3 response) {
		if (this.e != null) {
			this.e.printStackTrace();
			this.onException(this.e);
			return;
		}

		if (((Response) response).HasError) {
			this.onError(response);
			return;
		}
		
		this.onSuccess(response, this.protocolSession);
	}
	
	protected abstract void onSuccess(T3 response, ProtocolSession protocolSession);

	protected abstract void onError(T3 response);

	protected abstract void onException(Exception e2);

	protected abstract T3 doApiCall(TtpApi api, ProtocolSession protocolSession);

}
