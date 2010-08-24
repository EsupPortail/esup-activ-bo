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

public class FailValidation extends HashMap<String,Integer> implements InitializingBean{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * A logger.
	 */
	private final Logger logger = new LoggerImpl(getClass());
	
	
	
	private int nbMaxFail;
	
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public void incrementFail(String id){
		int nbFail=0;
		if(this.containsKey(id)){ 
			nbFail=this.get(id);
		}
		nbFail++;
		this.put(id,nbFail);
	}
	
	public boolean verify(String id){
		
		int nbFail=0;
		if(this.containsKey(id)) 
			nbFail=this.get(id);
		

		if(nbFail>nbMaxFail) {
			return false;
		}
		return true;
	}

	public int getNbMaxFail() {
		return nbMaxFail;
	}

	public void setNbMaxFail(int nbMaxFail) {
		this.nbMaxFail = nbMaxFail;
	}
	
	
    	

}
