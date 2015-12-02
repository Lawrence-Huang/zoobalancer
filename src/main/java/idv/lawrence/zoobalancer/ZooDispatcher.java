package idv.lawrence.zoobalancer;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet Filter implementation class ZKDispatcher When a client asks tomcat,
 * the filter will decide where the client had better to go. This filter also
 * implements sticky session. When the client has been decided where to go, all
 * the HTTP requests the client make will ask to the same tomcat.
 */
@WebFilter("/*")
public class ZooDispatcher implements Filter {
	private String ipAddress;
	private ZooManager zooManager = null;

	/**
	 * Default constructor.
	 */
	public ZooDispatcher() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	private String encapsulateRedirection(HttpServletRequest httpRequest,
			String destination) {
		String url = httpRequest.getRequestURL().toString();
		String newUrl = url.replace(httpRequest.getServerName(), destination);
		String refToken = "?from=" + ipAddress;
		return newUrl + refToken;
	}

	/**
	 * First, this method checks session id. If the session id belongs this
	 * tomcat, the tomcat will accept this request. If there isn't session id
	 * within the HTTP request, this filter will further check whether the
	 * request have been forwarded by the HTTP parameter. If it have been
	 * forwarded, the tomcat will accept this request. Beyond above mentioned,
	 * this filter will find the best tomcat from the ZooKeeper and forward the
	 * HTTP request.
	 * 
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		// TODO Auto-generated method stub
		// place your code here
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String sessionId = httpRequest.getHeader("cookie");
		String destination;

		if (sessionId == null) {
			String source = httpRequest.getParameter("from");
			if (source == null) {
				try {
					// destination = getBestServer();
					destination = zooManager.getBestServer();
				} catch (Exception e) {
					e.printStackTrace();
					httpResponse.sendError(404);
					return;
				}
				if (!ipAddress.equals(destination)) {
					httpResponse.sendRedirect(encapsulateRedirection(
							httpRequest, destination));
					return;
				}
			}

		} else {
			sessionId = sessionId.split("=")[1];
			try {
				// destination = lookUpSession(sessionId);
				destination = zooManager.lookupSession(sessionId);
				if (!destination.equals(ipAddress)) {
					httpResponse.sendRedirect(encapsulateRedirection(
							httpRequest, destination));
					return;
				}

			} catch (Exception e) {
				HttpSession session = httpRequest.getSession(false);
				if (session != null) {
					boolean flag;
					do {
						try {
							zooManager.registerSession(sessionId);
							flag = false;
						} catch (Exception e2) {
							e2.printStackTrace();
							flag = true;
							try {
								Thread.sleep(1000);
							} catch (Exception e3) {
							}
							;
						}
					} while (flag);
				} else {
					httpRequest.getSession().setMaxInactiveInterval(60);
					;
				}
			}
		}
		chain.doFilter(request, response);

	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
		ipAddress = (String) fConfig.getServletContext().getInitParameter(
				"ServerAddress");
		zooManager = (ZooManager) fConfig.getServletContext().getAttribute(
				"ZooManager");
	}

}
