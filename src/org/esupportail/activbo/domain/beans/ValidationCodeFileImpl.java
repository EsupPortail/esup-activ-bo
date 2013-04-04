package org.esupportail.activbo.domain.beans;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;

public class ValidationCodeFileImpl extends ValidationCodeImpl  {
	private final Logger logger = new LoggerImpl(getClass());
	
	
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
		Iterator<Map.Entry<String, HashMap<String,String>>> it=validationCodes.entrySet().iterator();
		
		if (validationCodes.size()==0)code=super.generateCode(id, codeDelay);
		else
		try {
			while(it.hasNext()){
				Map.Entry<String, HashMap<String,String>> e=it.next();
				HashMap<String,String> hash=e.getValue();
				Date date=new Date();
				// Ne générer le code que lorsque : le code n'est plus valide pour l'utilisateur connecté ou utilisateur ne possédant pas encore de code
				if ( (date.getTime()>stringToDate(hash.get(getDateKey())).getTime()&& hash.get(getCode(id))!=null) || (hash.get(getCode(id))==null &&hash.get(getDate(id))==null))	{code=super.generateCode(id, codeDelay);}
			}
			try {
				this.writeMap(getCodeFileName(),validationCodes);
			} catch (IOException e) {
			e.printStackTrace();
			}
		} catch (Exception e) {
			logger.error(e);
		}
				
		return code;
		
	}
	
	@Override
	public void validationCodeCleanning() {
		if(thread==null){
			ValidationCodeFileCleanning cleaning = new ValidationCodeFileCleanning(this);
			thread = new Thread(cleaning);
			thread.start();
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
}
