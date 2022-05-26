package org.esupportail.activbo.services.ldap;

import org.springframework.beans.factory.InitializingBean;


public class LdapSchema implements InitializingBean {
    public String password;
    public String shadowLastChange;
    public String mail;
    public String usernameAdmin;
    public String passwordAdmin;
    public String login;
    public String krbPrincipal;

    public void afterPropertiesSet() throws Exception {
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setShadowLastChange(String shadowLastChange) {
        this.shadowLastChange = shadowLastChange;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setUsernameAdmin(String usernameAdmin) {
        this.usernameAdmin = usernameAdmin;
    }

    public void setPasswordAdmin(String passwordAdmin) {
        this.passwordAdmin = passwordAdmin;
    }

    public void setKrbPrincipal(String krbPrincipal) {
        this.krbPrincipal = krbPrincipal;
    }


}