package org.esupportail.activbo.exceptions;

import java.io.Serial;

public class KerberosException extends Exception {

    @Serial private static final long serialVersionUID = 8197090501242229324L;

    public KerberosException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
}
