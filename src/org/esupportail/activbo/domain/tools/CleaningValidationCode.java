package org.esupportail.activbo.domain.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.esupportail.activbo.domain.beans.ValidationCode;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.springframework.beans.factory.InitializingBean;

public class CleaningValidationCode extends Thread implements InitializingBean{
	
	private ValidationCode validationCode;
	private String formatDateConv;
	private String validationCodeDateKey;

	private int cleaningTimeInterval;
	
	private final Logger logger = new LoggerImpl(getClass());
	
	public void afterPropertiesSet() throws Exception {
		start();
	}
	
	public void run(){
	
		try {
			while(true){
				logger.info("Boucle de nettoyage lanc�e");
				if (!validationCode.isEmpty()){
					logger.info("La table de hashage n'est pas vide");
					Iterator<Map.Entry<String, HashMap<String,String>>> it=validationCode.entrySet().iterator();
					while(it.hasNext()){
						Map.Entry<String, HashMap<String,String>> e=it.next();
						HashMap<String,String> hash=e.getValue();
						logger.info("Utilisateur "+e.getKey()+"(Code --> "+hash.get("code")+"  ||  Date d'expiration --> "+hash.get(validationCodeDateKey)+")");
						
						Date date=stringToDate(dateToString(new Date()));
						
						if (date.getTime()>this.stringToDate(hash.get(validationCodeDateKey)).getTime()){
							logger.debug("Expiration code, Ligne utilisateur "+e.getKey()+" supprim�e");
							it.remove();
						}
					}
				}	
				else{
					logger.debug("La table de hashage est vide");
				}	
				sleep(cleaningTimeInterval);	
			}
		
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
		
	
	private String dateToString(Date sDate) throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat(formatDateConv);//formatDateConv);
        return sdf.format(sDate);
    }
    
	private Date stringToDate(String sDate) throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat(formatDateConv);//formatDateConv);
        return sdf.parse(sDate);
    }
	
	/*public void putHashCode(HashCode hash){
		this.hashCode=hash;
	}*/

	public String getFormatDateConv() {
		return formatDateConv;
	}

	public void setFormatDateConv(String formatDateConv) {
		this.formatDateConv = formatDateConv;
	}


	public int getCleaningTimeInterval() {
		return cleaningTimeInterval;
	}

	public void setCleaningTimeInterval(int cleaningTimeInterval) {
		this.cleaningTimeInterval = cleaningTimeInterval*1000; //en secondes
	}

	/**
	 * @param validationCodeDateKey the validationCodeDateKey to set
	 */
	public void setValidationCodeDateKey(String validationCodeDateKey) {
		this.validationCodeDateKey = validationCodeDateKey;
	}

	/**
	 * @param validationCode the validationCode to set
	 */
	public void setValidationCode(ValidationCode validationCode) {
		this.validationCode = validationCode;
	}
	
}
