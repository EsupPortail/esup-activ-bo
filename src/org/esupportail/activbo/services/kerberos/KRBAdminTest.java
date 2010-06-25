package org.esupportail.activbo.services.kerberos;


/**
 * @author aanli
 *
 */
public class KRBAdminTest implements KRBAdmin {
	
	
	/** 
	 * @see org.esupportail.activbo.services.kerberos.KRBAdmin#add(String, String)
	 */
	public int add(String principal,String passwd){ return ADDED; }
	
	
	/** 
	 * @see org.esupportail.activbo.services.kerberos.KRBAdmin#del(String)
	 */
	public int del(final String principal) {return DELETED; } 
	
	/** 
	 * @see org.esupportail.activbo.services.kerberos.KRBAdmin#changePasswd(String, String)
	 */
	public int changePasswd(String principal,String passwd){return CHANGED;}
	
	
	/** 
	 * @see org.esupportail.activbo.services.kerberos.KRBAdmin#changePasswd(String, String, String)
	 */
	public int changePasswd(String principal, String oldPasswd, String newPasswd){return CHANGED;}
			
	/** 
	 * @see org.esupportail.activbo.services.kerberos.KRBAdmin#exists(String)
	 */
	public boolean exists(String principal) {return true;}	

}
