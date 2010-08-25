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
	
	private BlockedUser blockedUser;
	
	private int nbMaxFail;
	
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public void setFail(String id){
		int nbFail=0;
		if(this.containsKey(id)){ 
			nbFail=this.get(id);
		}
		nbFail++;
		System.out.println("incremente"+nbFail);
		this.put(id,nbFail);
	}
	
	public boolean verify(String id){
		
		if (!blockedUser.containsKey(id)){
			
			int nbFail;
			if(this.containsKey(id)){ 
				nbFail=this.get(id);

				if(nbFail>nbMaxFail) {
					//System.out.println("test"+nbFail);
					blockedUser.insertUser(id);
					return false;
				}
				System.out.println("test"+nbFail);
			}
			else{
				return true;
			}
		}
		else{
		
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

	public BlockedUser getBlockedUser() {
		return blockedUser;
	}

	public void setBlockedUser(BlockedUser blockedUser) {
		this.blockedUser = blockedUser;
	}
	
	
    	

}
