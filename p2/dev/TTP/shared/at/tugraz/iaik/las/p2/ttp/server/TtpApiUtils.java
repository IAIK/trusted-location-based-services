package at.tugraz.iaik.las.p2.ttp.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

/**
 * Helpers for serializing/deserializing the LTT into/from binary or XML
 * representation.
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class TtpApiUtils {

	public static <T> byte[] serializeToByteArray(T object) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {

			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return baos.toByteArray();
	}

	public static <T> String SerializeToXmlString(T object) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Serializer serializer = TtpApiUtils.getPersister();
		try {
			serializer.write(object, baos);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return baos.toString();
	}

	public static <T> T deserializeFromXmlString(String xmlString,
			Class<T> clazz) {
		if (xmlString == null) {
			return null;
		}

		ByteArrayInputStream bais = new ByteArrayInputStream(
				xmlString.getBytes());
		Serializer serializer = TtpApiUtils.getPersister();
		T object = null;
		try {
			object = serializer.read(clazz, bais);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return object;
	}

	@SuppressWarnings("unchecked")
	public static <T> T deserializeFromByteArray(byte[] bytes) {
		if (bytes == null) {
			return null;
		}

		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		T object = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(bais);
			object = (T) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return object;
	}

	private static Persister getPersister() {
		final GmtDateTransform transform = new GmtDateTransform();
		return new Persister(new Matcher() {
			@SuppressWarnings("rawtypes")
			@Override
			public Transform match(Class cls) throws Exception {
				if (cls == Date.class)
					return transform;
				return null;
			}
		});
	}
}
