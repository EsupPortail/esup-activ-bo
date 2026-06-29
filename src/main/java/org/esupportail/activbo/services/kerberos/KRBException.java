package org.esupportail.activbo.services.kerberos;

import java.io.Serial;

public class KRBException extends Exception {

    /**
     * 
     */
    @Serial private static final long serialVersionUID = -1785260918948542278L;

    public KRBException(String msg)
    {
        super(msg);
    }
}
