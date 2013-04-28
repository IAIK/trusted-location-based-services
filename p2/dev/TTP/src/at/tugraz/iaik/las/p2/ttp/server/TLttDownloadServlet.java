package at.tugraz.iaik.las.p2.ttp.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.tugraz.iaik.las.p2.ttp.client.data.ProtocolLogJdo;

public class TLttDownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 8394506648307053191L;

	private static final Logger log = Logger.getLogger(TtpDataServiceImpl.class
			.getName());

	private PersistenceManager pm = PMF.get();

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException {

		String key = req.getParameter("key");

		if (key == null || key == "") {
			log.warning(FileDownloadServlet.class.getName() + ": key=null");
			return;
		}

		ProtocolLogJdo plog = this.pm.getObjectById(ProtocolLogJdo.class,
				key);
		byte[] bytes = plog.TLtt.getValue().getBytes();

		res.setContentType("application/x-download");
		res.setHeader("Content-Disposition", "attachment; filename=T-LTT.xml");
		res.getOutputStream().write(bytes);
	}
}
