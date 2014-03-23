<%@ page pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="static" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html lang="de">
<head>
	<title>OncoWorkstation Fehler</title>
	<static:htmlhead/>
</head>
<body>
<div id="page">
	<static:header/>
	<div id="container">
		
		<static:includeHTML/>
		
		<h1>Es ist ein Fehler aufgetreten.</h1>
		<h2>Fehlercode ${pageContext.errorData.statusCode}</h2>

		<p>
			Ihre Anfrage konnte leider aufgrund eines internen Fehlers
			nicht bearbeitet werden.
		</p>
		<p>
			Fehlermeldung: ${pageContext.errorData.throwable}
		</p>

		<h2>Was sie jetzt tun können:</h2>
		<ul class="txtlist">	
			<li>
				Gehen Sie im Browser zurück oder klicken Sie <a href="javascript:window.back()" title="eine Seite zur�ck gehen">hier</a>.
			</li>
			<li>
				Gehen Sie zur <a href="${contextPath}/home">Oncoworkstation-Startseite</a>.
			</li>
		</ul>
		<hr/>
		<h2>Detaillierte Informationen zum Fehler:</h2>
		<p>Status Code: ${pageContext.errorData.statusCode}</p>
		<p>URI: ${pageContext.errorData.requestURI}</p>
		<p>Servlet: ${pageContext.errorData.servletName}</p>
		<h3>Stacktrace:</h3>
		<p><small>
		<c:forEach var="element" items="${pageContext.exception.stackTrace}">
				${element} <br/>
		</c:forEach>
		
		<c:choose>
  		<c:when test="${!empty pageContext.errorData.throwable.cause}">
     		${pageContext.errorData.throwable.cause} <br/>
  		</c:when>
		</c:choose>
		
		</small></p>
		
		</div>
		<static:footer/>
		
	 </div>
</body>

</html>


