package org.esupportail.activbo.exceptions;

import java.io.Serial;

public class LdapLoginAlreadyExistsException extends LdapProblemException {

    @Serial private static final long serialVersionUID = 8197090501242229324L;

    public LdapLoginAlreadyExistsException(final String message) {
        super(message);
    }

}
