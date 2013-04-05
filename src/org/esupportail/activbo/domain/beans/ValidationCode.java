package org.esupportail.activbo.domain.beans;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.esupportail.activbo.exceptions.UserPermissionException;

public interface ValidationCode{
		
    public boolean verify(String id,String code) throws UserPermissionException;    	
		
    public String getCode(String id);
	/**
	 * @param id identifiant de l'utilisateur
	 * @return la date de fin de validité du code associé à l'id
	 */  
    public String getDate(String id);
    
	public String generateCode(String id,int codeDelay);
			
	public String generateCode(String id);
	
	public void removeCode(Iterator<Map.Entry<String, HashMap<String,String>>>it);
	
}
