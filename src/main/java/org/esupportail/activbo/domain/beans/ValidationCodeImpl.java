package org.esupportail.activbo.domain.beans;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.activbo.exceptions.UserPermissionException;

import org.esupportail.activbo.domain.tools.BruteForceBlock;

import org.springframework.beans.factory.InitializingBean;

public class ValidationCodeImpl implements ValidationCode, InitializingBean{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * A logger.
	 */
	private final Logger logger = new LoggerImpl(getClass());
	
	protected  HashMap<String,HashMap<String,String>> validationCodes=new HashMap<String,HashMap<String,String>>();
	
	protected Thread thread;
	
	private String codeKey;
	private int codeDelay;
	private int codeLenght;
	private String dateKey;
	private String dateFormat;
	
	private long cleaningTimeInterval;
	
	private BruteForceBlock bruteForceBlock;
	
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
    public boolean verify(String id,String code) throws UserPermissionException{    	
				
		if (bruteForceBlock.isBlocked(id))
			throw new UserPermissionException ("Nombre de tentative de validation de code atteint pour l'utitilisateur "+id);
		
		//Recuperation des données correspondant de l'id de l'utilisateur
		HashMap <String,String>userData=validationCodes.get(id);
		
		if (userData!=null){
			logger.debug("L'utilisateur "+id+" poss�de un code");
			if(userData.get(codeKey).equals(code)){			
				logger.debug("Code utilisateur "+id+" valide");				
				return true;
			}
			else{			
				logger.warn("Code pour l'utilisateur "+id+" invalide");				
				bruteForceBlock.setFail(id);
			}
		}
		else{
			logger.info("Pas de code pour l'utilisateur "+id);

		}
		return false;
		
    }
    
    public String getCode(String id)
    {
    	if(validationCodes.get(id)!=null)
    		return validationCodes.get(id).get(codeKey);
    	else return null;
    }
    public String getDate(String id)
    {
    	if(validationCodes.get(id)!=null)
    		return validationCodes.get(id).get(dateKey);
    	else return null;
    }
    
	public String generateCode(String id,int codeDelay){
			
		String code=getRandomCode();			
		
		Calendar c = new GregorianCalendar();
		c.add(Calendar.SECOND,codeDelay);
		Date date=c.getTime();
			
		HashMap<String,String> userData= validationCodes.get(id);		
		if(userData==null) userData= new HashMap<String,String>();	
		else 
			{
				code=userData.get(codeKey);
				try {
					date=this.stringToDate(getDate(id));
				} catch (ParseException e) {
					logger.error(e.getMessage());
				}
			}
		if(c.getTime().getTime()>date.getTime()) date=c.getTime();
		logger.trace("Code de vadidation pour l'utilisateur : "+id+" est :"+ code);							
		userData.put(codeKey,code);
		userData.put(dateKey,this.dateToString(date));
				
		validationCodes.put(id, userData);
		validationCodeCleanning();
		return code;
				
	}
	
	
	
	private void validationCodeCleanning(){
		if(thread==null){
			ValidationCodeCleanning cleaning = new ValidationCodeCleanning(this);
			thread = new Thread(cleaning);
			thread.start();
		}
		
	}
	
	public String generateCode(String id){
		return generateCode(id,codeDelay);
	}

	private String getRandomCode()
	{
		Random r = new Random();
		String code="";
		for(int i=0;i<codeLenght;i++)
		{
		  int num=r.nextInt(10);
		  code+=String.valueOf(num);
		}	
		return code;
	}
	
	private String dateToString(Date sDate){
		
	    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
	    return sdf.format(sDate);
	}
	
	protected Date stringToDate(String sDate) throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.parse(sDate);
    }


	/**
	 * @return the codeKey
	 */
	public String getCodeKey() {
		return codeKey;
	}

	/**
	 * @param codeKey the codeKey to set
	 */
	public void setCodeKey(String codeKey) {
		this.codeKey = codeKey;
	}

	/**
	 * @return the dateKey
	 */
	public String getDateKey() {
		return dateKey;
	}

	/**
	 * @param dateKey the dateKey to set
	 */
	public void setDateKey(String dateKey) {
		this.dateKey = dateKey;
	}

	/**
	 * @return the codeLenght
	 */
	public int getCodeLenght() {
		return codeLenght;
	}

	/**
	 * @param codeLenght the codeLenght to set
	 */
	public void setCodeLenght(int codeLenght) {
		this.codeLenght = codeLenght;
	}

	/**
	 * @param dateFormat the dateFormat to set
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * @param codeDelay the codeDelay to set
	 */
	public void setCodeDelay(int codeDelay) {
		this.codeDelay = codeDelay;
	}

	/**
	 * @param bruteForceBlock the bruteForceBlock to set
	 */
	public void setBruteForceBlock(BruteForceBlock bruteForceBlock) {
		this.bruteForceBlock = bruteForceBlock;
	}

	/**
	 * @return the cleaningTimeInterval
	 */
	public long getCleaningTimeInterval() {
		return cleaningTimeInterval;
	}

	/**
	 * @param cleaningTimeInterval the cleaningTimeInterval to set
	 */
	public void setCleaningTimeInterval(long cleaningTimeInterval) {
		this.cleaningTimeInterval = cleaningTimeInterval*1000;
	}
	
	public HashMap<String, HashMap<String, String>> getValidationCodes() {
		return validationCodes;
	}

	public void setValidationCodes(
			HashMap<String, HashMap<String, String>> validationCodes) {
		this.validationCodes = validationCodes;
	}
	
	public void removeCode(Iterator<Map.Entry<String, HashMap<String,String>>> it)
	{
		it.remove();
	}
	
}
