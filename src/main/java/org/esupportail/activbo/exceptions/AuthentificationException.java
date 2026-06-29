package org.esupportail.activbo.exceptions;

import java.io.Serial;

public class AuthentificationException extends Exception {

    @Serial private static final long serialVersionUID = 8197090501242229324L;

    public AuthentificationException() {
        super("Authentification failed");
    }

    public AuthentificationException(final String message) {
        super(message);
    }

}
