package org.esupportail.activbo.domain.beans;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;


public class ValidationCodeFileImpl extends ValidationCodeImpl implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String codeFileName;

    public void setCodeFileName(String codeFileName) { this.codeFileName = codeFileName; }

    
    @Override
    public void afterPropertiesSet() throws Exception {
        validationCodes = new ConcurrentHashMap<String, UserData>(readMap(codeFileName));
        logger.debug("validationCodes:"+validationCodes.size());
    }
    
    @Override
    public UserData generateCode(String id, int codeDelay, String channel) {
        var code = super.generateCode(id, codeDelay, channel);
        writeMap();
        return code;
    }
        
    @Override
    public void removeCode(String userId) {
        super.removeCode(userId);
        writeMap();
    }
    
    @Override
    public void afterRemoveCode() {
        writeMap();
    }
    
    private void writeMap() {
        writeMap(codeFileName, validationCodes);
    }
    
    
     /**
     * Serialisation d'un HashMap dans un fichier
     * @param filename chemin du fichier
       @param HashMap  map a serialiser
   
     */
    private synchronized void writeMap(String fileName,Map<String,UserData> map) {
        try {
            var pw = new PrintWriter(fileName);
            for (var entry : map.entrySet()) {
                var userData = entry.getValue();
                pw.println(entry.getKey() + "|" + userData.code + "|" + userData.date + "|" + userData.channel);
            }
            pw.close();
        } catch (FileNotFoundException e) {logger.error(e.getMessage(), e);} 
    }
    /**
     * Retourner une HashMap deserialise depuis un fichier
     * @param filename chemin du fichier
     */
    public HashMap<String,UserData> readMap(String fileName) {  
        var map=new HashMap<String,UserData>();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(fileName));
        }
        catch (FileNotFoundException e) {
            logger.debug("Le fichier n'existe pas, il va etre cree automatiquement");
            return map;
        }
        try {
            while (true) {
                var line = br.readLine();
                if (line == null) break;
                var vals = line.split("\\|");
                if (vals.length == 4) {
                    var userData = new UserData();
                    userData.code = vals[1];
                    userData.date = vals[2];
                    userData.channel = vals[3].equals("null") ? null : vals[3];
                    map.put(vals[0], userData);
                } else {
                    logger.error("skipping weird line " + line + " " + vals.length);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try { br.close(); } catch (IOException e) { logger.error("", e); }
        }
        return map;
    }
    
}
