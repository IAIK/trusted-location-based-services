package at.tugraz.iaik.las.p2.ttp.client;

import java.util.List;

import at.tugraz.iaik.las.p2.ttp.client.data.ProtocolLogJdo;
import at.tugraz.iaik.las.p2.ttp.client.data.TagJdo;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public interface TtpDataServiceAsync {

	public void login(String username, String password,
			AsyncCallback<Boolean> callback);

	public void addTag(TagJdo tag, String certificateDbKey,
			AsyncCallback<Boolean> callback);

	public void getAllTags(AsyncCallback<List<TagJdo>> callback);

	public void deleteTag(TagJdo tag, AsyncCallback<Void> callback);

	public void getAllProtocolLogs(
			AsyncCallback<List<ProtocolLogJdo>> asyncCallback);

	public void getIssuedTLttProtocolLogs(
			AsyncCallback<List<ProtocolLogJdo>> asyncCallback);

	public void clearLog(AsyncCallback<Void> asyncCallback);
}
