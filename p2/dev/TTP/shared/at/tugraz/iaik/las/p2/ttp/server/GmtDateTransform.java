package at.tugraz.iaik.las.p2.ttp.server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.simpleframework.xml.transform.Transform;

/**
 * Using SimpleXML on different platforms (Android, GWT Development Mode, GAE/J)
 * the serializer may format dates using different formats (CET, GMT, ...) which
 * produces errors on deserialization. To avoid these problems, specify the date
 * transformation explicitly, as explained here:
 * http://stackoverflow.com/questions/9926049/simplexml-deserializing-object
 * -serialized-elsewhere-gives-unparseabledateexcepti?lq=1
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
final class GmtDateTransform implements Transform<Date> {
	ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat r = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss.SSS zzz");
			r.setTimeZone(TimeZone.getTimeZone("GMT"));
			return r;
		}
	};

	public Date read(String source) throws Exception {
		return sdf.get().parse(source);
	}

	public String write(Date source) throws Exception {
		return sdf.get().format(source);
	}
}