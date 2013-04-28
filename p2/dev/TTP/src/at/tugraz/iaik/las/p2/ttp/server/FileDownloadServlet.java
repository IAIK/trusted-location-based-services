package at.tugraz.iaik.las.p2.ttp.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.tugraz.iaik.las.p2.ttp.client.data.FileJdo;

/**
 * http://stackoverflow.com/questions/935262/gwt-making-get-requests
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class FileDownloadServlet extends HttpServlet {

	private static final long serialVersionUID = -2050211562565976377L;

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

		FileJdo cert = this.pm.getObjectById(FileJdo.class,
				key);

		res.setContentType("application/x-download");
		res.setHeader("Content-Disposition", "attachment; filename="
				+ cert.SourceFilename);
		res.getOutputStream().write(cert.Bytes);
		
		log.warning("Starting download for file with key="
				+ req.getParameter("key") + " ("
				+ cert.Bytes.length + " Bytes) ...");
	}
}
