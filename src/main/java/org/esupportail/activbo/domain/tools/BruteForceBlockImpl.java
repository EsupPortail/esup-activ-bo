
/**
 * 
 */
package org.esupportail.activbo.domain.tools;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author aanli
 *
 */
public class BruteForceBlockImpl implements BruteForceBlock, Runnable, InitializingBean 
{
private final Logger logger = new LoggerImpl(getClass());
private Thread thread;
private HashMap<String,LoginBlocked> logins=new HashMap<String,LoginBlocked>();

private int nbMaxFail; //Nbre d'essai avant de bloquer le login

private int wait; //durée de blocage en seconde

private long cleaningTime=1000L; //temps d'attente entre 2 passages du nettoyeur

/* (non-Javadoc)
 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
 */
public void afterPropertiesSet() throws Exception {	
	
}

public boolean isBlocked(String id){
	boolean blocked=true;
	LoginBlocked login=logins.get(id);
	if(login==null) blocked=false;
	else{
		Date currentDate=new Date();
		if(currentDate.getTime()>login.date.getTime()){
			blocked=false;
			logins.remove(id);			
		}
		else if(login.nbFail<nbMaxFail) 
			blocked=false;
	}	
	return blocked;		
}

public void setFail(String id)
{
	LoginBlocked login=logins.get(id);
	if(login==null) login=new LoginBlocked();
	Calendar c = new GregorianCalendar();	
	c.add(Calendar.SECOND, wait);	
	login.date=c.getTime();
	login.nbFail++;
	logins.put(id, login);
	
	if(thread==null){
		thread = new Thread(this); 
		thread.start();
	}
}

/**
 * @param wait the wait to set
 */
public void setWait(int wait) {
	this.wait = wait;
}

/**
 * @param nbMaxFail the nbMaxFail to set
 */
public void setNbMaxFail(int nbMaxFail) {
	this.nbMaxFail = nbMaxFail;
}

/**
 * @return the cleaningTime
 */
public long getCleaningTime() {
	return cleaningTime;
}

/**
 * @param cleaningTime the cleaningTime to set
 */
public void setCleaningTime(long cleaningTime) {
	this.cleaningTime = cleaningTime*1000;
}

/* (non-Javadoc)
 * @see java.lang.Runnable#run()
 */
public void run() {
	while(true){		
		if(logins.isEmpty()) logger.debug("Pas d'utilisateurs bloqués");
		Iterator<String> i=logins.keySet().iterator();		
		Date currentDate=new Date();
		while(i.hasNext())
		{		
			String key=i.next();
			LoginBlocked loginBlocked=logins.get(key);
			logger.info(key+" a fait "+loginBlocked.nbFail+" tentative(s) échouée(s)");
			if(currentDate.getTime()>loginBlocked.date.getTime())
			{
				logins.remove(key);
				logger.debug("Déblocage de l'utilisateur "+key);
			}
		}
		try {
			Thread.sleep(cleaningTime);
		} catch (InterruptedException e) {		
			logger.error(e);
		}
	}	
}


}

class LoginBlocked {
	Date date; // date de fin de blocage
	int nbFail;
}
