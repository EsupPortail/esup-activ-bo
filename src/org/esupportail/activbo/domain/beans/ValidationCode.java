package org.esupportail.activbo.domain.beans;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;

import org.esupportail.activbo.exceptions.UserPermissionException;

import org.springframework.beans.factory.InitializingBean;

public class ValidationCode extends Hashtable<String,HashMap<String,String>> implements InitializingBean{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * A logger.
	 */
	private final Logger logger = new LoggerImpl(getClass());
	
	private String codeKey;
	private int codeDelay;
	private int codeLenght;
	private String dateKey;
	private String dateFormat;


	
	private FailValidation failValidation;
	
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
    public boolean verify(String id,String code) throws UserPermissionException{    	
				
		if (!failValidation.verify(id))
			throw new UserPermissionException ("Nombre de tentative de validation de code atteint pour l'utitilisateur "+id);
		
		//Recuperation des données correspondant de l'id de l'utilisateur
		HashMap <String,String>userData=this.get(id);
		
		if (userData!=null){
			logger.debug("L'utilisateur "+id+" poss�de un code");
			if (code.equalsIgnoreCase(userData.get(codeKey))){
				logger.debug("Code utilisateur "+id+" valide");
				failValidation.remove(id);
				return true;
			}
			else{
				System.out.println("Code Faux");
				logger.warn("Code pour l'utilisateur "+id+" invalide");				
				failValidation.setFail(id);
			}
		}
		else{
			logger.info("Pas de code pour l'utilisateur "+id);

		}
		return false;
		
    }
    
    public String getCode(String id)
    {
    	return this.get(id).get(codeKey);    	
    }
    public String getDate(String id)
    {
    	return this.get(id).get(dateKey);
    }
    
	public String generateCode(String id,int codeDelay){
		
		
		String code=getRandomCode();
		logger.debug(code);
		
		Calendar c = new GregorianCalendar();
		c.add(Calendar.SECOND,codeDelay);
		
		HashMap<String,String> userData= this.get(id);
		
		if(userData==null) userData= new HashMap<String,String>();	
		else code=userData.get(codeKey);
							
		userData.put(codeKey,code);
		userData.put(dateKey,this.dateToString(c.getTime()));
				
		this.put(id, userData);
		
		return code;
				
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

	public FailValidation getFailValidation() {
		return failValidation;
	}

	public void setFailValidation(FailValidation failValidation) {
		this.failValidation = failValidation;
	}


}
