package idv.lawrence.zoobalancer;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;

/**
 * Application Lifecycle Listener implementation class ZKHttpRequestListener
 * When the tomcat get a HTTP request, this listener will increase the tomcat
 * loading weight recoreded in the ZooKeeper. When the tomcat respond the
 * request, this listener will reduce the tomcat loading weight recored in the
 * ZooKeeper.
 * 
 */
@WebListener
public class ZooHttpRequestListener implements ServletRequestListener {

	private ZooManager zooManager = null;

	/**
	 * Default constructor.
	 */
	public ZooHttpRequestListener() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see ServletRequestListener#requestDestroyed(ServletRequestEvent)
	 */
	public void requestDestroyed(ServletRequestEvent event) {
		// TODO Auto-generated method stub
		if (zooManager == null)
			zooManager = (ZooManager) event.getServletContext().getAttribute(
					"ZooManager");

		zooManager.decreaseLoad(1);
		;

	}

	/**
	 * @see ServletRequestListener#requestInitialized(ServletRequestEvent)
	 */
	public void requestInitialized(ServletRequestEvent event) {
		// TODO Auto-generated method stub
		if (zooManager == null)
			zooManager = (ZooManager) event.getServletContext().getAttribute(
					"ZooManager");

		try {
			System.out.println(zooManager.getCurrentLoad());
		} catch (Exception e) {
			e.printStackTrace();
		}
		zooManager.increseLoad(1);

	}

}