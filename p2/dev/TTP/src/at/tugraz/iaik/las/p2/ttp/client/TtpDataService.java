package at.tugraz.iaik.las.p2.ttp.client;

import at.tugraz.iaik.las.p2.ttp.client.data.ProtocolLogJdo;

import java.util.List;

import at.tugraz.iaik.las.p2.ttp.client.data.TagJdo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
@RemoteServiceRelativePath("data")
public interface TtpDataService extends RemoteService {
	public final static String START_TAG = "::BeginCertKey::";
	public final static String END_TAG = "::EndCertKey::";

	public Boolean login(String username, String password);

	public Boolean addTag(TagJdo tag, String certificateDbKey);

	public List<TagJdo> getAllTags();

	public void deleteTag(TagJdo tag);

	public List<ProtocolLogJdo> getAllProtocolLogs();

	public List<ProtocolLogJdo> getIssuedTLttProtocolLogs();

	public void clearLog();
}
