package at.tugraz.iaik.las.p2.ttp.server;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import at.tugraz.iaik.las.p2.ttp.client.TtpDataService;
import at.tugraz.iaik.las.p2.ttp.client.data.FileJdo;
import at.tugraz.iaik.las.p2.ttp.client.data.ProtocolLogJdo;
import at.tugraz.iaik.las.p2.ttp.client.data.TagJdo;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * 
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
@SuppressWarnings("serial")
public class TtpDataServiceImpl extends RemoteServiceServlet implements
		TtpDataService {

	static {
		// add IAIK JCE and ECC security provider
		Security.addProvider(new iaik.security.provider.IAIK());
		Security.addProvider(new iaik.security.ecc.provider.ECCProvider(true));
	}

	private static final Logger log = Logger.getLogger(TtpDataServiceImpl.class
			.getName());

	@Override
	public Boolean login(String username, String password) {
		// remark: just a simple hardcoded check
		// not intended to provide security
		// just for demonstration purposes
		if (username == null || !username.equals("admin")) {
			return false;
		}
		if (password == null || !password.equals("iaik")) {
			return false;
		}
		return true;
	}

	@Override
	public Boolean addTag(TagJdo tag, String certificateDbKey) {
		PersistenceManager pm = PMF.get();
		try {
			// Key certKey = KeyFactory.stringToKey(certificateDbKey);
			FileJdo cert = pm.getObjectById(FileJdo.class, certificateDbKey);
			FileJdo cert2 = new FileJdo();
			cert2.Bytes = cert.Bytes.clone();
			cert2.SourceFilename = cert.SourceFilename;
			cert2.UploadDate = cert.UploadDate;
			tag.PublicKeyCertificateFile = cert2;
			pm.makePersistent(tag);
		} catch (Exception e) {
			// do nothing
			// important because of http://bugs.caucho.com/view.php?id=4080
			log.warning(e.toString());
			log.warning(e.getMessage());
			return false;
		} finally {
			pm.close();
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TagJdo> getAllTags() {
		PersistenceManager pm = PMF.get();
		Query query = pm.newQuery(TagJdo.class);
		List<TagJdo> result = null;
		List<TagJdo> allTags = null;
		result = (List<TagJdo>) query.execute();
		if (result != null) {
			// allTags.addAll(result);
			allTags = (List<TagJdo>) pm.detachCopyAll(result);
		} else {
			allTags = null;
		}
		pm.close();
		return allTags;
	}

	@Override
	public void deleteTag(TagJdo tag) {
		PersistenceManager pm = PMF.get();
		pm.deletePersistent(pm.getObjectById(tag.getClass(), tag.Key));
		pm.close();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void clearLog() {
		PersistenceManager pm = PMF.get();
		Query query = pm.newQuery(ProtocolLogJdo.class);
		List<ProtocolLogJdo> result = null;
		result = (List<ProtocolLogJdo>) query.execute();
		if (result != null) {
			pm.deletePersistentAll(result);
		}
		pm.close();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProtocolLogJdo> getAllProtocolLogs() {
		PersistenceManager pm = PMF.get();
		Query query = pm.newQuery(ProtocolLogJdo.class);
		query.setOrdering("Timestamp desc");
		List<ProtocolLogJdo> result = null;
		List<ProtocolLogJdo> allLogs = null;
		result = (List<ProtocolLogJdo>) query.execute();
		if (result != null) {
			allLogs = (List<ProtocolLogJdo>) pm.detachCopyAll(result);
		} else {
			allLogs = null;
		}
		pm.close();
		return allLogs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProtocolLogJdo> getIssuedTLttProtocolLogs() {
		PersistenceManager pm = PMF.get();
		Query query = pm.newQuery(ProtocolLogJdo.class);
		query.setOrdering("Timestamp descending");
		List<ProtocolLogJdo> result = null;
		List<ProtocolLogJdo> allLogs = new ArrayList<ProtocolLogJdo>();
		result = (List<ProtocolLogJdo>) query.execute();
		if (result != null) {
			for (ProtocolLogJdo plog : result) {
				if (plog.TLtt != null && plog.TLtt.getValue().length() > 0) {
					allLogs.add(pm.detachCopy(plog));
				}
			}
		} else {
			allLogs = null;
		}
		pm.close();
		return allLogs;
	}
}
