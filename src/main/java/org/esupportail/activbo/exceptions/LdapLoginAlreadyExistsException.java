package org.esupportail.activbo.exceptions;

public class LdapLoginAlreadyExistsException extends Exception {
    
    private static final long serialVersionUID = 8197090501242229324L;

    public LdapLoginAlreadyExistsException(final String message) {
        super(message);
    }

}
