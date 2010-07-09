package org.esupportail.activbo.domain.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.esupportail.activbo.domain.beans.HashCode;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.springframework.beans.factory.InitializingBean;

public class CleaningHashCode extends Thread implements InitializingBean{
	
	private HashCode hashCode;
	private String formatDateConv;
	private String hashCodeDateKey;
	private String hashCodeCodeKey;
	private int cleaningTimeInterval;
	
	private final Logger logger = new LoggerImpl(getClass());
	
	public CleaningHashCode(){

	}
	
	public void run(){
	
		try {
			while(true){
				//logger.info("Boucle de nettoyage lancée");
				if (!hashCode.isEmpty()){
					logger.info("La table de hashage n'est pas vide");
					Iterator<Map.Entry<String, HashMap<String,String>>> it=hashCode.entrySet().iterator();
					while(it.hasNext()){
						Map.Entry<String, HashMap<String,String>> e=it.next();
						HashMap<String,String> hash=e.getValue();
						logger.info("======= Utilisateur "+e.getKey()+"(Code --> "+hash.get("code")+"  ||  Date d'expiration --> "+hash.get(hashCodeDateKey)+")");
						
						Date date=stringToDate(dateToString(new Date()));
						
						if (date.getTime()>this.stringToDate(hash.get(hashCodeDateKey)).getTime()){
							logger.info("Expiration code, Ligne utilisateur "+e.getKey()+" supprimée");
							it.remove();
						}
					}
				}	
				else{
					logger.info("La table de hashage est vide");
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

	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
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

	public HashCode getHashCode() {
		return hashCode;
	}

	public void setHashCode(HashCode hashCode) {
		this.hashCode = hashCode;
	}

	public int getCleaningTimeInterval() {
		return cleaningTimeInterval;
	}

	public void setCleaningTimeInterval(int cleaningTimeInterval) {
		this.cleaningTimeInterval = cleaningTimeInterval;
	}

	
	public String getHashCodeDateKey() {
		return hashCodeDateKey;
	}

	public void setHashCodeDateKey(String hashCodeDateKey) {
		this.hashCodeDateKey = hashCodeDateKey;
	}

	public String getHashCodeCodeKey() {
		return hashCodeCodeKey;
	}

	public void setHashCodeCodeKey(String hashCodeCodeKey) {
		this.hashCodeCodeKey = hashCodeCodeKey;
	}
	
}
