
package org.esupportail.activbo.domain.tools;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BruteForceBlockImpl implements BruteForceBlock, Runnable {
    class LoginInfo {
        Date date; // date de fin de blocage
        int nbFail;

        private boolean isExpired() {
            return new Date().getTime() > date.getTime();
        }
    }
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Thread purgeExpiredThread;
    private HashMap<String,LoginInfo> loginsInfo = new HashMap<>();

    private int wait; //duree de blocage en seconde
    private int nbMaxFail; //Nbre d'essai avant de bloquer le login
    private long cleaningTimeMillis = 1000L; //temps d'attente entre 2 passages du nettoyeur

    public void setWait(int wait) { this.wait = wait; }
    public void setNbMaxFail(int nbMaxFail) { this.nbMaxFail = nbMaxFail; }
    public void setCleaningTime(long cleaningTimeSecond) { this.cleaningTimeMillis = cleaningTimeSecond * 1000; }


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
        logger.debug("Nombre d'échecs pour " + id + " : " + info.nbFail);
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
            if (loginsInfo.isEmpty()) logger.debug("Pas d'utilisateurs bloques");

            for (String id: loginsInfo.keySet()) {
                logger.info(id + " a fait " + loginsInfo.get(id).nbFail + " tentative(s) echouee(s)");
                removeExpired_or_get(id);
            }

            try {
                Thread.sleep(cleaningTimeMillis);
            } catch (InterruptedException e) {      
                logger.error("", e);
            }
        }   
    }
}
