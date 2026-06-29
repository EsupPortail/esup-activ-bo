package org.esupportail.activbo.services.kerberos;

import java.io.Serial;

public class KRBIllegalArgumentException extends KRBException {

    /**
     * 
     */
    @Serial private static final long serialVersionUID = 1430632946330444041L;

    public KRBIllegalArgumentException(String msg) {
        super(msg);     
    }

    
}
