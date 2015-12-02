<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<h1>Server Address</h1>
	<h2><%=request.getServletContext().getInitParameter(
					"ServerAddress")%></h2>
	<h1>Session Id</h1>
	<h2><%=session.getId()%></h2>
	<%-- In order to show the load balancer whether works, I make respond slowly. --%>
	<%
		Thread.sleep(10000);
	%>
</body>
</html>
