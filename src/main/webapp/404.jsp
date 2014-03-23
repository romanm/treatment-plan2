<%@ page pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="static" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html lang="de">
<head>
	<title>OncoWorkstation </title>
	<static:htmlhead/>
</head>
<body>
<div id="page">
	<static:header/>
	<div id="container">

		<h1>Resource nicht gefunden</h1>
		<h2>Fehlercode ${pageContext.errorData.statusCode}</h2>
		<p>Die angeforderte Resource <strong>${pageContext.errorData.requestURI}</strong>
			kann nicht gefunden werden.
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
	</div>

	<static:footer/>
	
</div>
	
</body>

</html>


