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
		validationCodes=readMap(getCodeFileName());
		logger.debug("validationCodes:"+validationCodes.size());
	}
	
	@Override
	public String generateCode(String id, int codeDelay) {
		String code = null;
		code=super.generateCode(id, codeDelay);
		try {
			this.writeMap(getCodeFileName(),validationCodes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return code;
		
	}
	
	@Override
	public void removeCode(Iterator<Map.Entry<String, HashMap<String,String>>> it)
	{
		it.remove();
		try {
			writeMap(getCodeFileName(), validationCodes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	 /**
     * Serialisation d'un HashMap dans un fichier
     * @param filename chemin du fichier
       @param HashMap  map a serialiser
   
     */
    public void writeMap(String fileName,HashMap<String,HashMap<String,String>> map) throws IOException
    {	FileOutputStream fos;
 	    try {
 	        fos = new FileOutputStream(fileName);
 	        ObjectOutputStream oos = new ObjectOutputStream(fos);
 	        oos.writeObject(map);
	    	oos.close();
	
 	    } catch (FileNotFoundException e) {
 	    	e.printStackTrace();
 	 		} catch (IOException e) {
 	 			e.printStackTrace(); }
     
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

	 	 } catch (FileNotFoundException e) {
	 		 e.printStackTrace();
	 		 }catch (IOException e) {e.printStackTrace();
	 		 }catch (ClassNotFoundException e) {e.printStackTrace();
	 	    }
     return map;
     }

 	public String getCodeFileName() {
		return codeFileName;
	}


	public void setCodeFileName(String codeFileName) {
		this.codeFileName = codeFileName;
	}
    
}
