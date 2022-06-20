package org.esupportail.activbo.services.ldap;


public class LdapSchema {
    public String password;
    public String shadowLastChange;
    public String mail;
    public String login;
    public String krbPrincipal;
    public String sambaNTPassword;
    public String sambaPwdLastSet;


    public void setLogin(String login) { this.login = login; }
    public void setPassword(String password) { this.password = password; }
    public void setShadowLastChange(String shadowLastChange) { this.shadowLastChange = shadowLastChange; }
    public void setMail(String mail) { this.mail = mail; }
    public void setKrbPrincipal(String krbPrincipal) { this.krbPrincipal = krbPrincipal; }
    public void setSambaNTPassword(String sambaNTPassword) { this.sambaNTPassword = sambaNTPassword; }
    public void setSambaPwdLastSet(String sambaPwdLastSet) { this.sambaPwdLastSet = sambaPwdLastSet; }
}