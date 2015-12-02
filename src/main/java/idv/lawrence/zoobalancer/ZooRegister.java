package idv.lawrence.zoobalancer;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;


/**
 * Application Lifecycle Listener implementation class ZKRegister
 * It connect to ZooKeeper cluter while the Tomcat is starting.
 *
 */
@WebListener
public class ZooRegister implements ServletContextListener {


	/**
	 * Default constructor.
	 */
	public ZooRegister() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent event) {
		// TODO Auto-generated method stub
		String projectName = event.getServletContext().getInitParameter("ProjectName");
		String zkCluster = event.getServletContext().getInitParameter("ZookeeperCluster");
		String ipAddress = event.getServletContext().getInitParameter("ServerAddress");
		try{
			event.getServletContext().setAttribute("ZooManager", 
					new ZooManager( projectName,ipAddress,zkCluster));
		}catch(Exception e){
			e.printStackTrace();			
		}

	}
}
