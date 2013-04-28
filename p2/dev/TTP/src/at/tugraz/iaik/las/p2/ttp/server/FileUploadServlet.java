package at.tugraz.iaik.las.p2.ttp.server;

import iaik.x509.X509Certificate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import at.tugraz.iaik.las.p2.ttp.client.TtpDataService;
import at.tugraz.iaik.las.p2.ttp.client.data.FileJdo;

/**
 * Inspired by http://stackoverflow.com/a/4481770
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class FileUploadServlet extends HttpServlet {

	private static final long serialVersionUID = -417860693069131354L;

	private static final Logger log = Logger.getLogger(FileUploadServlet.class
			.getName());

	static {
		// add IAIK JCE and ECC security provider
		Security.addProvider(new iaik.security.provider.IAIK());
		Security.addProvider(new iaik.security.ecc.provider.ECCProvider(true));
		//Security.addProvider(new BouncyCastleProvider());
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		try {
			ServletFileUpload upload = new ServletFileUpload();
			res.setContentType("text/plain");
			// res.setContentType("application/json");
			// res.set

			FileItemIterator iterator = upload.getItemIterator(req);
			while (iterator.hasNext()) {
				FileItemStream item = iterator.next();
				InputStream stream = item.openStream();

				if (item.isFormField()) {
					log.warning("Got a form field: " + item.getFieldName());
				} else {
					log.warning("Got an uploaded file: " + item.getFieldName()
							+ ", name = " + item.getName());

					// You now have the filename (item.getName() and the
					// contents (which you can read from stream). Here we just
					// print them back out to the servlet output stream, but you
					// will probably want to do something more interesting (for
					// example, wrap them in a Blob and commit them to the
					// datastore).

					FileJdo cert = new FileJdo();
					cert.Bytes = IOUtils.toByteArray(stream);
					cert.SourceFilename = item.getName();
					cert.UploadDate = new java.util.Date();
					// check whether uploaded file is a valid certificate
					X509Certificate x509cert = null;
					try {
						// IAIK JCE
						x509cert = new X509Certificate(
								new ByteArrayInputStream(
										cert.Bytes));

						// Bouncycastle
						// CertificateFactory certFactory =
						// CertificateFactory.getInstance("X.509");
						// InputStream in = new
						// ByteArrayInputStream(cert.PublicKeyFileBytes);
						// X509Certificate x509cert =
						// (X509Certificate)certFactory.generateCertificate(in);
					} catch (Exception e) {
						x509cert = null;
						log.warning(e.toString());
						log.warning(e.getMessage());
					}
					if (x509cert != null) {

						PersistenceManager pm = PMF.get();
						FileJdo certPersisted = pm
								.makePersistent(cert);
						pm.close();
						res.getWriter().write(
								TtpDataService.START_TAG + certPersisted.Key
										+ TtpDataService.END_TAG);
					} else {
						res.sendError(HttpServletResponse.SC_FORBIDDEN,
								"File is not a valid X.509 Certificate.");
						log.warning("File is not a valid X.509 Certificate.");
						return;
					}
				}
			}
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
	}
}
