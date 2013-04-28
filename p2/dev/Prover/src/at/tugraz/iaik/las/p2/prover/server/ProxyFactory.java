package at.tugraz.iaik.las.p2.prover.server;

import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;

import com.caucho.hessian.client.HessianProxy;
import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.io.HessianRemoteObject;

/**
 * A factory to construct a proxy that talks to the TTP API using Hessian.
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public enum ProxyFactory {
	;
	private static ClassLoader classLoader;
	private static HessianProxyFactory factory = new HessianProxyFactory();
	private static String defaultServerUrl;

	public static void init(ClassLoader classLoader, String defaultServerUrl) {
		ProxyFactory.classLoader = classLoader;
		ProxyFactory.defaultServerUrl = defaultServerUrl;
		// ProxyFactory.factory.setDebug(true);
		ProxyFactory.factory.setHessian2Request(false);
		ProxyFactory.factory.setHessian2Reply(false);
	}

	public static String lastUsedApiUrl;

	public static <T> T getProxy(final Class<T> proxyClass,
			final String relativePath) {
		URL url;
		try {
			url = new URL(String.format("http://%s/%s",
					ProxyFactory.defaultServerUrl, relativePath));
			ProxyFactory.lastUsedApiUrl = url.toString();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		// factory.setHessian2Reply(false);
		HessianProxy proxy = new MyHessianProxy(factory, url);
		@SuppressWarnings("unchecked")
		T o = (T) Proxy.newProxyInstance(classLoader, new Class[] { proxyClass,
				HessianRemoteObject.class }, proxy);
		return o;
	}

	private static class MyHessianProxy extends HessianProxy {
		public MyHessianProxy(HessianProxyFactory factory, URL url) {
			super(url, factory);
		}
	}
}