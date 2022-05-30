package org.esupportail.activbo.domain.beans;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.esupportail.activbo.domain.tools.BruteForceBlock;
import org.esupportail.activbo.exceptions.UserPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ValidationCodeImpl {

    public static class UserData implements Serializable {
        public String code;
        public String date;
        public String channel; // optional

        // to help inspecting validation.code.file.name
        public String toString() {
            return code + "|" + date + "|" + channel;
        }
    }
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected ConcurrentHashMap<String,UserData> validationCodes = new ConcurrentHashMap<>();
    protected Thread validationCodeCleaningThread;
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";
    public long cleaningTimeIntervalMillis = 15 /* minutes */ * 60 * 1000;  
    
    private int codeDelay;
    private int codeLenght;
    private BruteForceBlock bruteForceBlock;

    public void setCodeLenght(int codeLenght) { this.codeLenght = codeLenght; }
    public void setCodeDelay(int codeDelay) { this.codeDelay = codeDelay; }
    public void setBruteForceBlock(BruteForceBlock bruteForceBlock) { this.bruteForceBlock = bruteForceBlock; }
    
    
    public boolean verify(String id,String code) throws UserPermissionException{        
                
        if (bruteForceBlock.isBlocked(id))
            throw new UserPermissionException ("Nombre de tentative de validation de code atteint pour l'utitilisateur "+id);
        
        //Recuperation des donnees correspondant de l'id de l'utilisateur
        var userData = validationCodes.get(id);
        
        if (userData!=null) {
            logger.debug("L'utilisateur "+id+" possede un code");
            if (userData.code.equals(code)) {           
                logger.debug("Code utilisateur "+id+" valide");             
                return true;
            } else {
                logger.warn(id + "@" + code + ": Code invalide");
                bruteForceBlock.setFail(id);
            }
        } else {
            logger.warn(id + "@" + code + ": Code invalide (aucun code pour cet utilisateur)");
        }
        return false;
        
    }
    
    protected UserData generateCode(String id,int codeDelay,String channel) {
            
        String code=generateRandomCode();
        Date date = nowPlusSeconds(codeDelay);      
        logger.trace("Code de validation pour l'utilisateur : "+id+" est :"+ code + " avec duree de vie " + codeDelay + " secondes");

        var userData = new UserData();  
        userData.code = code;
        userData.date = this.dateToString(date);
        if (channel != null) userData.channel = channel; // only useful to differentiate channel codes (sent to user) and service codes (when CASified)
                
        validationCodes.put(id, userData);
        mayStartValidationCodeCleanningThread();

        return userData;
                
    }

    private Date nowPlusSeconds(int codeDelay) {
        Calendar c = new GregorianCalendar();
        c.add(Calendar.SECOND,codeDelay);
        return c.getTime();
    }
    
    private void mayStartValidationCodeCleanningThread() {
        if (validationCodeCleaningThread == null) {
            var cleaning = new ValidationCodeCleanning(this);
            validationCodeCleaningThread = new Thread(cleaning);
            validationCodeCleaningThread.start();
        }
    }

    public UserData generateChannelCode(String id, int codeDelay, String channel) {
        return generateCode(id,codeDelay, channel);
    }

    public String generateCode(String id) {
        return generateCode(id,codeDelay, null).code;
    }

    private String generateRandomCode()
    {
        var r = new Random();
        var code="";
        for (int i = 0; i < codeLenght; i++) {
          code += String.valueOf(r.nextInt(10));
        }   
        return code;
    }
    
    private String dateToString(Date sDate) {
        var sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(sDate);
    }
    
    protected Date stringToDate(String sDate) throws ParseException{
        var sdf = new SimpleDateFormat(dateFormat);
        return sdf.parse(sDate);
    }

    public void removeCode(String userId) {
        validationCodes.remove(userId);
    }

    public void afterRemoveCode() {
    }
    
}
