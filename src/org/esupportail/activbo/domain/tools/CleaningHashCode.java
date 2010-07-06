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
	private String accessDateKey;
	private String accessCodeKey;
	private int cleaningTimeInterval;
	
	private final Logger logger = new LoggerImpl(getClass());
	
	public CleaningHashCode(){
		//this.hashCode=hashCode;
	}
	
	public void run(){
	
		try {
			while(true){
				//logger.info("Boucle de nettoyage lancée");
				if (!hashCode.isEmpty()){
					logger.info("La table de hashage n'est pas vide");
					logger.info("=======================================================================");
					logger.info("             TABLE DE CODES D'ACCES POUR L'APPLICATION BO            ");
					logger.info("=======================================================================");
					
					Iterator<Map.Entry<String, HashMap<String,String>>> it=hashCode.entrySet().iterator();
					while(it.hasNext()){
						Map.Entry<String, HashMap<String,String>> e=it.next();
						HashMap<String,String> hash=e.getValue();
						logger.info("======= Utilisateur "+e.getKey()+"(Code --> "+hash.get("code")+"  ||  Date d'expiration --> "+hash.get(accessDateKey)+")");
						
						Date date=stringToDate(dateToString(new Date()));
						
						if (date.getTime()>this.stringToDate(hash.get(accessDateKey)).getTime()){
							logger.info("Expiration code, Ligne utilisateur "+e.getKey()+" supprimée");
							it.remove();
						}
					}
					logger.info("=========================================================================");
					
					/*for (Map.Entry<String, HashMap<String,String>> e : hashCode.entrySet()){
						HashMap<String,String> hash=e.getValue();
						logger.info("====== Utilisateur "+e.getKey()+"============");
						logger.info("Code --> "+hash.get("code"));
						logger.info("Date d'expiration --> "+hash.get("date"));
						Date date=stringToDate(dateToString(new Date()));
						
						if (date.getTime()>this.stringToDate(hash.get(accessDateKey)).getTime()){
							logger.info("Delai de durée du code dépassé, Suppression du code correspondant à l'utilisateur"+e.getKey());
=======
							System.out.println("temps depassï¿½");
>>>>>>> .r317
							hashCode.remove(e.getKey());
							//System.out.println(e.getValue().toString());
						}  
					} 
					logger.info("===================================");*/
					
				}
				//mettre le temps dans un fichier de properties
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

	public String getAccessDateKey() {
		return accessDateKey;
	}

	public void setAccessDateKey(String accessDateKey) {
		this.accessDateKey = accessDateKey;
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

	public String getAccessCodeKey() {
		return accessCodeKey;
	}

	public void setAccessCodeKey(String accessCodeKey) {
		this.accessCodeKey = accessCodeKey;
	}
	
}
