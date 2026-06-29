package org.esupportail.activbo.exceptions;

import java.io.Serial;

public class LdapProblemException extends Exception {
    
    /**
     * The id for serialization.
     */
    @Serial private static final long serialVersionUID = 8197090501242229324L;

    public LdapProblemException(final String message) {
        super(message);
    }

}
