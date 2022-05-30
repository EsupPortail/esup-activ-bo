package org.esupportail.activbo.domain.beans;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;

public class ValidationCodeFileImpl extends ValidationCodeImpl  {
    private final Logger logger = new LoggerImpl(getClass());
    private String codeFileName;

    
    // construteur
    ValidationCodeFileImpl(){
        super();
    };
    
    
    @Override
    public void afterPropertiesSet() throws Exception {
        validationCodes= new ConcurrentHashMap<String, HashMap<String, String>>(readMap(getCodeFileName()));
        logger.debug("validationCodes:"+validationCodes.size());
    }
    
    @Override
    public String generateCode(String id, int codeDelay, String channel) {
        String code = super.generateCode(id, codeDelay, channel);
        writeMap();
        return code;
    }
        
    @Override
    public void removeCode(String userId)
    {
        super.removeCode(userId);
        writeMap();
    }
    
    @Override
    public void removeCode(Iterator<Map.Entry<String, HashMap<String,String>>> it)
    {
        super.removeCode(it);
        writeMap();
    }
    
    private void writeMap() {
        try {
            writeMap(getCodeFileName(), validationCodes);
        } catch (IOException e) {logger.error(e.getMessage(), e);}
    }
    
    
     /**
     * Serialisation d'un HashMap dans un fichier
     * @param filename chemin du fichier
       @param HashMap  map a serialiser
   
     */
    private synchronized void writeMap(String fileName,Map<String,HashMap<String,String>> map) throws IOException
    {   FileOutputStream fos;
        try {
            fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            // on force le format de sérialisation HashMap<String, HashMap<String,String>>
            oos.writeObject(new HashMap<String, HashMap<String,String>>(map));
            oos.close();
    
        } catch (FileNotFoundException e) {logger.error(e.getMessage(), e);} 
          catch (IOException e) {logger.error(e.getMessage(), e); }
     
    }
    /**
     * Retourner une HashMap déserialisé depuis un fichier
     * @param filename chemin du fichier
     */
    public HashMap<String,HashMap<String,String>> readMap(String fileName) throws IOException, ClassNotFoundException {  
        HashMap<String,HashMap<String,String>> map=new HashMap<String,HashMap<String,String>>();
        try {
                FileInputStream fis = new FileInputStream(fileName);
                ObjectInputStream ois = new ObjectInputStream(fis);
                map = (HashMap<String,HashMap<String,String>> ) ois.readObject();
                ois.close();
    
       } 
      catch (FileNotFoundException e) {logger.debug("Si le fichier n'exsite pas, il va être créé automatiquement");}
      catch (IOException e) {logger.error(e.getMessage(), e);}
      catch (ClassNotFoundException e) {logger.error(e.getMessage(), e);}
      return map;
     }

    public String getCodeFileName() {
        return codeFileName;
    }


    public void setCodeFileName(String codeFileName) {
        this.codeFileName = codeFileName;
    }
    
}
