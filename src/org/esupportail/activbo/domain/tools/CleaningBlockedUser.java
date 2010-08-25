package org.esupportail.activbo.domain.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.esupportail.activbo.domain.beans.BlockedUser;
import org.esupportail.activbo.domain.beans.FailValidation;
import org.esupportail.activbo.domain.beans.ValidationCode;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.springframework.beans.factory.InitializingBean;

public class CleaningBlockedUser extends Thread implements InitializingBean{
	
	private BlockedUser blockedUser;
	private String formatDateConv;
	private FailValidation failValidation;


	private int cleaningTimeInterval;
	
	private final Logger logger = new LoggerImpl(getClass());
	
	public CleaningBlockedUser(){

	}
	
	public void run(){
	
		try {
			while(true){
				logger.info("Boucle de nettoyage lancï¿½e");
				if (!blockedUser.isEmpty()){
					logger.info("La table de hashage n'est pas vide");
					Iterator<Map.Entry<String, String>> it=blockedUser.entrySet().iterator();
					while(it.hasNext()){
						Map.Entry<String, String> e=it.next();
						String dateString=e.getValue();
						logger.info("======= Utilisateur "+e.getKey()+"(Date --> "+dateString+")");
						
						Date date=stringToDate(dateToString(new Date()));
						
						if (date.getTime()>this.stringToDate(dateString).getTime()){
							logger.info("Expiration blockage, Ligne blockage "+e.getKey()+" supprimée");
							it.remove();
							failValidation.remove(e.getKey());
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


	public int getCleaningTimeInterval() {
		return cleaningTimeInterval;
	}

	public void setCleaningTimeInterval(int cleaningTimeInterval) {
		this.cleaningTimeInterval = cleaningTimeInterval*1000; //en secondes
	}

	public BlockedUser getBlockedUser() {
		return blockedUser;
	}

	public void setBlockedUser(BlockedUser blockedUser) {
		this.blockedUser = blockedUser;
	}

	public FailValidation getFailValidation() {
		return failValidation;
	}

	public void setFailValidation(FailValidation failValidation) {
		this.failValidation = failValidation;
	}

	
	
}
