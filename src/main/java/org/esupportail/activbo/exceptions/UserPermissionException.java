package org.esupportail.activbo.exceptions;

import java.io.Serial;

public class UserPermissionException extends Exception {
    @Serial private static final long serialVersionUID = 8375439753015128832L;

    public UserPermissionException(final String message) {
        super(message);
    }
}
