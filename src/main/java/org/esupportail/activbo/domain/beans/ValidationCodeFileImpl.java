package org.esupportail.activbo.domain.beans;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
        try {
            writeMap(codeFileName, validationCodes);
        } catch (IOException e) {logger.error(e.getMessage(), e);}
    }
    
    
     /**
     * Serialisation d'un HashMap dans un fichier
     * @param filename chemin du fichier
       @param HashMap  map a serialiser
   
     */
    private synchronized void writeMap(String fileName,Map<String,UserData> map) throws IOException {
        try {
            var fos = new FileOutputStream(fileName);
            var oos = new ObjectOutputStream(fos);
            // on force le format de sérialisation HashMap<String, UserData>
            oos.writeObject(new HashMap<String, UserData>(map));
            oos.close();
        } catch (FileNotFoundException e) {logger.error(e.getMessage(), e);} 
          catch (IOException e) {logger.error(e.getMessage(), e); }
     
    }
    /**
     * Retourner une HashMap déserialisé depuis un fichier
     * @param filename chemin du fichier
     */
    public HashMap<String,UserData> readMap(String fileName) throws IOException, ClassNotFoundException {  
        var map=new HashMap<String,UserData>();
        try {
                FileInputStream fis = new FileInputStream(fileName);
                ObjectInputStream ois = new ObjectInputStream(fis);
                map = (HashMap<String,UserData> ) ois.readObject();
                ois.close();
        } 
        catch (FileNotFoundException e) {logger.debug("Si le fichier n'exsite pas, il va être créé automatiquement");}
        catch (IOException e) {logger.error(e.getMessage(), e);}
        catch (ClassNotFoundException e) {logger.error(e.getMessage(), e);}

        return map;
    }

    
}
