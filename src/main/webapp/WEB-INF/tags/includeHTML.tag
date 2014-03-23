<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:if test="${not empty initParam.includeHTML}">
	<%
		ServletContext ctx = application.getContext("/");
		String myUrl =  application.getInitParameter("includeHTML");
		out.flush();
		RequestDispatcher dispatcher = ctx.getRequestDispatcher(myUrl);
		dispatcher.include(request, response);
		out.flush();
	%>
</c:if>	