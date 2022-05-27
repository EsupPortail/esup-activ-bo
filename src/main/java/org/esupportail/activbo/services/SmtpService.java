package org.esupportail.activbo.services;

import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SmtpService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String smtpServerHost;
    private int smtpServerPort; 
    private InternetAddress fromAddress;
    /**
     * The address to which _all_ the emails should be sent (if null, all the
     * emails are sent normally).
     */
    private InternetAddress interceptEmail;

    public void setSmtpServerHost(String smtpServerHost) { this.smtpServerHost = smtpServerHost; }
    public void setSmtpServerPort(int smtpServerPort) { this.smtpServerPort = smtpServerPort; }
    public void setFromAddress(String fromAddress) throws AddressException, UnsupportedEncodingException { this.fromAddress = newInternetAddress(fromAddress); }
    public void setInterceptEmail(String interceptEmail) throws AddressException, UnsupportedEncodingException { 
        this.interceptEmail = StringUtils.isBlank(interceptEmail) ? null : newInternetAddress(interceptEmail);
    }

    public void sendEmail(InternetAddress to, String subject, String body, boolean bodyIsHtml) {
        try {
            sendEmail(new InternetAddress[] { to }, null, null, subject, body, bodyIsHtml);
        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("an exception occured while sending the email to '" + to.toString() 
                    + "' using SMTP server '" + smtpServerHost + ":" + smtpServerPort + "'.", e);
            throw new RuntimeException(e);
        }
    } 

    private void sendEmail(
        InternetAddress[] tos, InternetAddress[] ccs, InternetAddress[] bccs,
        String subject, String body, boolean bodyIsHtml) throws MessagingException, UnsupportedEncodingException {

        if (body == null) {
            throw new InvalidParameterException("body should not be null");
        }
        if (logger.isDebugEnabled()) {
            for (var to: tos) logger.debug("preparing an email for to '" + to.getAddress() + "'...");
        }

        var message = createMessage(smtpServerHost, smtpServerPort);        
        if (bodyIsHtml) {
            message.setContent(body, "text/html; charset=UTF-8");
            message.setHeader("Content-Type", "text/html; charset=UTF-8");
        } else {
            message.setText(body);
        }
        message.setFrom(fromAddress);
        if (interceptEmail != null) {
            var to = new InternetAddress(
                interceptEmail.getAddress(),
                getPersonalOrAddress(interceptEmail) + " (normally sent to " + StringUtils.join(tos, " et ") + ")");
            message.addRecipient(Message.RecipientType.TO, to);
        } else {
            message.addRecipients(Message.RecipientType.TO, tos);
            if (ccs != null) {
                message.addRecipients(Message.RecipientType.CC, ccs);
            }
            if (bccs != null) {
                message.addRecipients(Message.RecipientType.BCC, bccs);
            }
        }
        message.setSubject(subject);

        Transport.send(message);
        for (var iAdr: tos) logger.info("an email has been sent to '" + iAdr.getAddress() + "'...");
    }
    
    private static MimeMessage createMessage(String smtpServerHost, int smtpServerPort) {
        var props = new java.util.Properties();
        props.put("mail.smtp.host", smtpServerHost);
        props.put("mail.smtp.port", Integer.toString(smtpServerPort));
        var session = jakarta.mail.Session.getInstance(props, null);
        return new MimeMessage(session);
    }

    // workaround InternetAddress constructor which handles "foo <bar@boo.com>", BUT will expect the "foo" part to be encoded already
    private InternetAddress newInternetAddress(String addressString) throws AddressException, UnsupportedEncodingException {
        var address = new InternetAddress(addressString); // this constructor will parse the string, BUT will expect the "personal" part to be encoded already
        var personal = address.getPersonal(); // get it non-encoded
        if (personal != null) {
            address.setPersonal(personal); // will encode it :-)
        }
        return address;
    }
    private String getPersonalOrAddress(InternetAddress address) {
        var s = address.getPersonal();
        return s != null ? s : address.getAddress();
    }

}
