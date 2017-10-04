package org.esupportail.activbo.domain.beans;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;

public class ValidationCodeCleanning implements Runnable {
	
	private final Logger logger = new LoggerImpl(getClass());
	ValidationCodeImpl vc;
	
	ValidationCodeCleanning(ValidationCodeImpl validationCodeImpl){
		this.vc=validationCodeImpl;
	}
	


	@Override
	public void run() {
		try {
			while(true){
				logger.debug("Boucle de nettoyage lancée");
				if (!vc.validationCodes.isEmpty()){
					logger.debug("La table de hashage n'est pas vide");
					Iterator<Map.Entry<String, HashMap<String,String>>> it=vc.validationCodes.entrySet().iterator();
					while(it.hasNext()){
						Map.Entry<String, HashMap<String,String>> e=it.next();
						HashMap<String,String> hash=e.getValue();
						logger.info("Utilisateur "+e.getKey()+"(Code --> "+hash.get(vc.getCodeKey())+"  ||  Date d'expiration --> "+hash.get(vc.getDateKey())+")");
						
						Date date=new Date();
						
						if (date.getTime()>vc.stringToDate(hash.get(vc.getDateKey())).getTime()){
							logger.debug("Expiration code, Ligne utilisateur "+e.getKey()+" supprim�e");
							vc.removeCode(it);
						}
					}
				}	
				else{
					logger.debug("La table de hashage est vide");
				}
				Thread.sleep(vc.getCleaningTimeInterval());	
			}
		
		} catch (Exception e) {
			logger.error(e);
		}
		
	}
}
