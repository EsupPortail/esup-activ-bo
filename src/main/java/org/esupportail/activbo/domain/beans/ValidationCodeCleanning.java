package org.esupportail.activbo.domain.beans;

import java.util.Date;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;

public class ValidationCodeCleanning implements Runnable {  
    private final Logger logger = new LoggerImpl(getClass());
    private ValidationCodeImpl vc;
    
    ValidationCodeCleanning(ValidationCodeImpl validationCodeImpl) {
        this.vc = validationCodeImpl;
    }
    
    @Override
    public void run() {
        try {
            while(true) {
                logger.debug("Boucle de nettoyage lancée");
                if (!vc.validationCodes.isEmpty()) {
                    boolean removed = false;
                    logger.debug("La table de hashage n'est pas vide");
                    var it=vc.validationCodes.entrySet().iterator();
                    Date date=new Date();
                    while(it.hasNext()) {
                        var e=it.next();
                        var userData=e.getValue();
                        logger.debug("Utilisateur "+e.getKey()+"(Code --> "+userData.code+"  ||  Date d'expiration --> "+userData.date+")");
                                            
                        if (date.getTime() > vc.stringToDate(userData.date).getTime()) {
                            String log = e.getKey() + "@" + userData.code + ": expiration";
                            if (userData.channel != null) logger.info(log); else logger.debug(log);
                            it.remove();
                            removed = true;
                        }
                    }
                    if (removed) vc.afterRemoveCode();
                }   
                else{
                    logger.debug("La table de hashage est vide");
                }
                Thread.sleep(vc.cleaningTimeIntervalMillis);    
            }
        
        } catch (Exception e) {
            logger.error(e);
        }
        
    }
}
