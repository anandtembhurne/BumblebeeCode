<!DOCTYPE html>
<!-- Nuxeo Enterprise Platform -->
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page language="java"%>
<%@ page import="java.util.List"%>
<%@ page import="org.joda.time.DateTime"%>
<%@ page import="org.nuxeo.ecm.platform.ui.web.auth.LoginScreenHelper"%>
<%@ page import="org.nuxeo.ecm.platform.web.common.MobileBannerHelper"%>
<%@ page import="org.nuxeo.ecm.platform.ui.web.auth.NXAuthConstants"%>
<%@ page import="org.nuxeo.ecm.platform.ui.web.auth.service.LoginProviderLink"%>
<%@ page import="org.nuxeo.ecm.platform.ui.web.auth.service.LoginScreenConfig"%>
<%@ page import="org.nuxeo.ecm.platform.web.common.admin.AdminStatusHelper"%>
<%@ page import="org.nuxeo.common.Environment"%>
<%@ page import="org.nuxeo.runtime.api.Framework"%>
<%@ page import="org.nuxeo.ecm.platform.ui.web.auth.service.LoginVideo" %>
<%@ page import="org.nuxeo.ecm.platform.web.common.locale.LocaleProvider"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%
String productName = Framework.getProperty(Environment.PRODUCT_NAME);
String productVersion = Framework.getProperty(Environment.PRODUCT_VERSION);
String testerName = Framework.getProperty("org.nuxeo.ecm.tester.name");
boolean isTesting = "Nuxeo-Selenium-Tester".equals(testerName);
String context = request.getContextPath();

HttpSession httpSession = request.getSession(false);
if (httpSession!=null && httpSession.getAttribute(NXAuthConstants.USERIDENT_KEY)!=null) {
  response.sendRedirect(context + "/" + LoginScreenHelper.getStartupPagePath());
}

// Read Seam locale cookie
String localeCookieName = "org.jboss.seam.core.Locale";
Cookie localeCookie = null;
Cookie cookies[] = request.getCookies();
if (cookies != null) {
  for (int i = 0; i < cookies.length; i++) {
    if (localeCookieName.equals(cookies[i].getName())) {
      localeCookie = cookies[i];
      break;
    }
  }
}
String selectedLanguage = null;
if (localeCookie != null) {
  selectedLanguage = localeCookie.getValue();
}
selectedLanguage = Framework.getLocalService(LocaleProvider.class).getLocaleWithDefault(selectedLanguage).getLanguage();

boolean maintenanceMode = AdminStatusHelper.isInstanceInMaintenanceMode();
String maintenanceMessage = AdminStatusHelper.getMaintenanceMessage();

LoginScreenConfig screenConfig = LoginScreenHelper.getConfig();
List<LoginProviderLink> providers = screenConfig.getProviders();
boolean useExternalProviders = providers!=null && providers.size()>0;

// fetch Login Screen config and manage default
boolean showNews = screenConfig.getDisplayNews();
String iframeUrl = screenConfig.getNewsIframeUrl();

String backgroundPath = LoginScreenHelper.getValueWithDefault(screenConfig.getBackgroundImage(), context + "/img/login_bg.svg");
String bodyBackgroundStyle = LoginScreenHelper.getValueWithDefault(screenConfig.getBodyBackgroundStyle(), "url('" + backgroundPath + "') no-repeat center center fixed #006ead");
String loginButtonBackgroundColor = LoginScreenHelper.getValueWithDefault(screenConfig.getLoginButtonBackgroundColor(), "#ff452a");
String loginBoxBackgroundStyle = LoginScreenHelper.getValueWithDefault(screenConfig.getLoginBoxBackgroundStyle(), "none repeat scroll 0 0");
String footerStyle = LoginScreenHelper.getValueWithDefault(screenConfig.getFooterStyle(), "");
boolean disableBackgroundSizeCover = Boolean.TRUE.equals(screenConfig.getDisableBackgroundSizeCover());
String fieldAutocomplete = screenConfig.getFieldAutocomplete() ? "on" : "off";

String logoWidth = LoginScreenHelper.getValueWithDefault(screenConfig.getLogoWidth(), "113");
String logoHeight = LoginScreenHelper.getValueWithDefault(screenConfig.getLogoHeight(), "20");
String logoAlt = LoginScreenHelper.getValueWithDefault(screenConfig.getLogoAlt(), "Nuxeo");
String logoUrl = LoginScreenHelper.getValueWithDefault(screenConfig.getLogoUrl(), context + "/img/login_logo.png");
String currentYear = new DateTime().toString("Y");

boolean hasVideos = screenConfig.hasVideos();
String muted = screenConfig.getVideoMuted() ? "muted " : "";
String loop = screenConfig.getVideoLoop() ? "loop " : "";

String androidApplicationURL = MobileBannerHelper.getURLForAndroidApplication(request);
String iOSApplicationURL = MobileBannerHelper.getURLForIOSApplication(request);
String appStoreURL = MobileBannerHelper.getAppStoreURL();
%>

<html>
<%
if (selectedLanguage != null) { %>
<fmt:setLocale value="<%= selectedLanguage %>"/>
<%
}%>
<fmt:setBundle basename="messages" var="messages"/>

<head>
<title><%=productName%></title>
<link rel="icon" type="image/png" href="<%=context%>/icons/favicon.png" />
<link rel="shortcut icon" type="image/x-icon" href="<%=context%>/icons/favicon.ico" />
<script type="text/javascript" src="<%=context%>/scripts/detect_timezone.js"></script>
<script type="text/javascript" src="<%=context%>/scripts/nxtimezone.js"></script>
<script type="text/javascript" src="<%=context%>/scripts/mobile-banner.js"></script>
<script type="text/javascript">
  nxtz.resetTimeZoneCookieIfNotSet();
</script>

<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="css/layout.css">


</head> 

<body>
<!--<% if (hasVideos && !isTesting) { %>
<video autoplay <%= muted + loop %> preload="auto" poster="<%=backgroundPath%>" id="bgvid">
  <% for (LoginVideo video : screenConfig.getVideos()) { %>
  <source src="<%= video.getSrc() %>" type="<%= video.getType() %>">
  <% } %>
</video>
<% } %>-->
<!-- Locale: <%= selectedLanguage %> -->
	<div class="topbar">
		<div class="container">
			<div class="logo"><img src="img/login_logo.png"></div>
		</div>		
	</div>
	

<div class="container log-out-div">
 
<div class="login-con">

	<div class="log-box">
		<div class="log-head"><h2>Login To DMS</h2></div>
		
		<div class="log-inner-con">
			
			<form method="post" action="startup" autocomplete="<%= fieldAutocomplete %>">
               <!-- To prevent caching -->
        <%
          response.setHeader("Cache-Control", "no-cache"); // HTTP 1.1
          response.setHeader("Pragma", "no-cache"); // HTTP 1.0
          response.setDateHeader("Expires", -1); // Prevents caching at the proxy server
        %>
        <!-- ;jsessionid=<%=request.getSession().getId()%> -->
        <% if (maintenanceMode) { %>
          <div class="maintenanceModeMessage">
            <div class="warnMessage">
              <fmt:message bundle="${messages}" key="label.maintenancemode.active" /><br/>
              <fmt:message bundle="${messages}" key="label.maintenancemode.adminLoginOnly" />
            </div>
            <div class="infoMessage">
              <fmt:message bundle="${messages}" key="label.maintenancemode.message" /> : <br/>
            </div>
          </div>
        <%} %>
        <c:if test="${param.nxtimeout}">
          <div class="feedbackMessage">
            <fmt:message bundle="${messages}" key="label.login.timeout" />
          </div>
        </c:if>
        <c:if test="${param.connectionFailed}">
          <div class="feedbackMessage errorMessage">
           <fmt:message bundle="${messages}" key="label.login.connectionFailed" />
          </div>
        </c:if>
        <c:if test="${param.loginFailed == 'true' and param.connectionFailed != 'true'}">
         <div class="feedbackMessage errorMessage">
           <fmt:message bundle="${messages}" key="label.login.invalidUsernameOrPassword" />
         </div>
        </c:if>
        <c:if test="${param.loginMissing}">
         <div class="feedbackMessage errorMessage">
           <fmt:message bundle="${messages}" key="label.login.missingUsername" />
         </div>
        </c:if>
        <c:if test="${param.securityError}">
         <div class="feedbackMessage errorMessage">
           <fmt:message bundle="${messages}" key="label.login.securityError" />
         </div>
        </c:if>
        <input class="user-log" type="text" name="user_name" id="username"
          placeholder="<fmt:message bundle="${messages}" key="label.login.username" />"/>
        <input class="pwd-log" type="password" name="user_password" id="password"
          placeholder="<fmt:message bundle="${messages}" key="label.login.password" />">
        <% if (selectedLanguage != null) { %>
        <input type="hidden" name="language" id="language" value="<%= selectedLanguage %>" />
        <% } %>
        <input type="hidden" name="requestedUrl" id="requestedUrl" value="${fn:escapeXml(param.requestedUrl)}" />
        <input type="hidden" name="forceAnonymousLogin" id="true" />
        <input type="hidden" name="form_submitted_marker" id="form_submitted_marker" />
        <input class="login_button" type="submit" name="Submit"
          value="<fmt:message bundle="${messages}" key="label.login.logIn" />" />
        <% if (useExternalProviders) {%>
        <div class="loginOptions">
          <p><fmt:message bundle="${messages}" key="label.login.loginWithAnotherId" /></p>
          <div class="idList">
            <% for (LoginProviderLink provider : providers) { %>
            <div class="idItem">
              <a href="<%= provider.getLink(request, request.getContextPath() + request.getParameter("requestedUrl")) %>"
                style="background-image:url('<%=(context + provider.getIconPath())%>')" title="<%=provider.getDescription()%>"><%=provider.getLabel()%>
              </a>
            </div>
            <%}%>
          </div>
        </div>
        <%}%>
      </form>
			
		</div>
		
	</div>

 

	
	
</div>


<div class="log-bg"></div> 
 
</div>



<div class="logfooter">
<div class="container">
	<fmt:message bundle="${messages}" key="label.login.copyright">
	  <fmt:param value="<%=currentYear %>" />
	</fmt:message>
<!-- 	<div style="float: right;">Powered by:Nuxeo <strong>Bumblebee</strong></div> -->
</div>
</div>

</body>
</html>