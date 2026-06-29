package org.esupportail.activbo.exceptions;

import java.io.Serial;

public class LoginException extends Exception {
    @Serial private static final long serialVersionUID = 8197090501242229324L;

    public LoginException(final String message) {
        super(message);
    }

}
