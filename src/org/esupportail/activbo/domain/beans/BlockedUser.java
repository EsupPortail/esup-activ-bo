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

public class BlockedUser extends HashMap<String,String> implements InitializingBean{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * A logger.
	 */
	private final Logger logger = new LoggerImpl(getClass());
	
	private int blockingTime;
	
	private String dateFormat;

	public String getDateFormat() {
		return dateFormat;
	}


	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}


	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
		
	public void insertUser(String id){
		
		Calendar c = new GregorianCalendar();
		c.add(Calendar.SECOND,blockingTime);
		
							
		this.put(id,this.dateToString(c.getTime()));
	}

	private String dateToString(Date sDate){
		
	    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
	    return sdf.format(sDate);
	}


	public int getBlockingTime() {
		return blockingTime;
	}


	public void setBlockingTime(int blockingTime) {
		this.blockingTime = blockingTime;
	}	
	
    	

}
