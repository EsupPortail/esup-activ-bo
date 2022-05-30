package org.esupportail.activbo.domain.beans;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationCodeCleanning implements Runnable {  
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private ValidationCodeImpl vc;
    
    ValidationCodeCleanning(ValidationCodeImpl validationCodeImpl) {
        this.vc = validationCodeImpl;
    }
    
    @Override
    public void run() {
        try {
            while(true) {
                logger.debug("Boucle de nettoyage lancee");
                if (!vc.validationCodes.isEmpty()) {
                    logger.debug("La table de hashage n'est pas vide");

                    boolean removedAtLeastOne = false;
                    var now = new Date().getTime();

                    var it = vc.validationCodes.entrySet().iterator();
                    while (it.hasNext()) {
                        var e = it.next();
                        var userData = e.getValue();
                        logger.debug("Utilisateur "+e.getKey()+"(Code --> "+userData.code+"  ||  Date d'expiration --> "+userData.date+")");
                                            
                        if (now > vc.stringToDate(userData.date).getTime()) {
                            String logMsg = e.getKey() + "@" + userData.code + ": expiration";
                            if (userData.channel != null) logger.info(logMsg); else logger.debug(logMsg);

                            it.remove();
                            removedAtLeastOne = true;
                        }
                    }
                    if (removedAtLeastOne) vc.afterRemoveCode();
                } else {
                    logger.debug("La table de hashage est vide");
                }
                Thread.sleep(vc.cleaningTimeIntervalMillis);    
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        
    }
}
