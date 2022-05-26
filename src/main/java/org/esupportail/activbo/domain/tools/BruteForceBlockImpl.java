
package org.esupportail.activbo.domain.tools;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.springframework.beans.factory.InitializingBean;

public class BruteForceBlockImpl implements BruteForceBlock, Runnable, InitializingBean {
    class LoginInfo {
        Date date; // date de fin de blocage
        int nbFail;

        private boolean isExpired() {
            return new Date().getTime() > date.getTime();
        }
    }
    
    private final Logger logger = new LoggerImpl(getClass());
    private Thread purgeExpiredThread;
    private HashMap<String,LoginInfo> loginsInfo = new HashMap<>();

    private int wait; //durée de blocage en seconde
    private int nbMaxFail; //Nbre d'essai avant de bloquer le login
    private long cleaningTimeMillis = 1000L; //temps d'attente entre 2 passages du nettoyeur

    public void setWait(int wait) { this.wait = wait; }
    public void setNbMaxFail(int nbMaxFail) { this.nbMaxFail = nbMaxFail; }
    public void setCleaningTime(long cleaningTimeSecond) { this.cleaningTimeMillis = cleaningTimeSecond * 1000; }


    public void afterPropertiesSet() throws Exception {
    }

    private LoginInfo removeExpired_or_get(String id) {
        var info = loginsInfo.get(id);
        if (info != null && info.isExpired()) {
            logger.debug("Deblocage de l'utilisateur " + id);
            loginsInfo.remove(id);
            return null;
        }
        return info;
    }

    public boolean isBlocked(String id) {
        LoginInfo info = removeExpired_or_get(id);
        return info != null && info.nbFail >= nbMaxFail;
    }

    private Date nowPlusSeconds(int codeDelay) {
        Calendar c = new GregorianCalendar();
        c.add(Calendar.SECOND,codeDelay);
        return c.getTime();
    }

    public void setFail(String id)
    {
        LoginInfo info = loginsInfo.get(id);
        if (info==null) info = new LoginInfo();

        info.date = nowPlusSeconds(wait);
        info.nbFail++;
        loginsInfo.put(id, info);
        
        mayStartPurgeExpiredThread();
    }
    private void mayStartPurgeExpiredThread() {
        if (purgeExpiredThread == null) {
            purgeExpiredThread = new Thread(this); 
            purgeExpiredThread.start();
        }
    }

    // remove expired LoginInfo
    public void run() {
        while(true) {       
            if (loginsInfo.isEmpty()) logger.debug("Pas d'utilisateurs bloqués");

            for (String id: loginsInfo.keySet()) {
                logger.info(id + " a fait " + loginsInfo.get(id).nbFail + " tentative(s) échouée(s)");
                removeExpired_or_get(id);
            }

            try {
                Thread.sleep(cleaningTimeMillis);
            } catch (InterruptedException e) {      
                logger.error(e);
            }
        }   
    }
}
