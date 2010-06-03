<%@include file="_include.jsp"%>
<e:menuItem id="welcome" value="#{msgs['NAVIGATION.TEXT.WELCOME']}"
	action="#{welcomeController.enter}"
	accesskey="#{msgs['NAVIGATION.ACCESSKEY.WELCOME']}" />
<e:menuItem id="administrators"
	value="#{msgs['NAVIGATION.TEXT.ADMINISTRATION']}"
	action="#{administratorsController.enter}"
	accesskey="#{msgs['NAVIGATION.ACCESSKEY.ADMINISTRATION']}"
	rendered="#{administratorsController.pageAuthorized}" />
<e:menuItem id="preferences"
	value="#{msgs['NAVIGATION.TEXT.PREFERENCES']}"
	accesskey="#{msgs['NAVIGATION.ACCESSKEY.PREFERENCES']}"
	action="#{preferencesController.enter}"
	rendered="#{preferencesController.pageAuthorized}" />
<e:menuItem id="about" value="#{msgs['NAVIGATION.TEXT.ABOUT']}"
	accesskey="#{msgs['NAVIGATION.ACCESSKEY.ABOUT']}"
	action="#{aboutController.enter}" />
<e:menuItem id="login" action="casLogin"
	value="#{msgs['NAVIGATION.TEXT.LOGIN']}"
	accesskey="#{msgs['NAVIGATION.ACCESSKEY.LOGIN']}"
	rendered="#{sessionController.printLogin}" />
<e:menuItem id="logout" action="#{sessionController.logout}"
	value="#{msgs['NAVIGATION.TEXT.LOGOUT']}"
	accesskey="#{msgs['NAVIGATION.ACCESSKEY.LOGOUT']}"
	rendered="#{sessionController.printLogout}" />