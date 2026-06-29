package org.esupportail.activbo.services.kerberos;

import java.io.Serial;

public class KRBPrincipalAlreadyExistsException extends KRBException {


        /**
     * 
     */
    @Serial private static final long serialVersionUID = 6990794529333306003L;

        public KRBPrincipalAlreadyExistsException(String msg)
        {
            super(msg);
        }
}
