package idv.lawrence.zoobalancer;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Application Lifecycle Listener implementation class ZKSessionListener When
 * sessions are created by Tomcat, this SessionLister will register the session
 * Id to the ZooKeeper. In a similar way, when session are destroyed by Tomcat,
 * this SessionLister will unregister from the ZooKeeper. Therefore,
 * ZooDispather can use these information in the ZooKeeper for conducting sticky
 * session.
 */
@WebListener
public class ZooSessionListener implements HttpSessionListener {
	private ZooManager zooManager = null;

	/**
	 * Default constructor.
	 */
	public ZooSessionListener() {
		// TODO Auto-generated constructor stub

	}

	/**
	 * @see HttpSessionListener#sessionCreated(HttpSessionEvent)
	 */
	public void sessionCreated(HttpSessionEvent event) {
		// TODO Auto-generated method stub
		if (zooManager == null)
			zooManager = (ZooManager) event.getSession().getServletContext()
					.getAttribute("ZooManager");

		zooManager.registerSession(event.getSession().getId());

	}

	/**
	 * @see HttpSessionListener#sessionDestroyed(HttpSessionEvent)
	 */
	public void sessionDestroyed(HttpSessionEvent event) {
		// TODO Auto-generated method stub
		if (zooManager == null)
			zooManager = (ZooManager) event.getSession().getServletContext()
					.getAttribute("ZooManager");

		zooManager.unregisterSession(event.getSession().getId());

	}

}
