package org.esupportail.activbo.services.kerberos;



/**
 * @author aanli
 *
 */
public interface KRBAdmin {
	
	/*public final static int ADDED=0,
							ALREADY_EXIST=1,
							NOT_CHANGED=2,
							CHANGED=3,
							ILLEGAL_ARGUMENT=4,
							DELETED=5,
							ERROR=6;*/
	
	/**
	 * @param principal
	 * @param passwd
	 * @return ADDED
	 */
	public void add(String principal,String passwd) throws KRBException, KRBPrincipalAlreadyExistsException;
	
	
	/**
	 * @param principal
	 * @return DELETED
	 * @throws KRBException 
	 */
	public void del(final String principal) throws KRBException;
	
	/**
	 * @param principal
	 * @param passwd
	 * @return CHANGED
	 * @throws KRBException 
	 */
	public void changePasswd(String principal,String passwd) throws KRBException,KRBIllegalArgumentException;
	
	
	/**
	 * uses principal privilege
	 * @param principal
	 * @param oldPasswd
	 * @param newPasswd
	 * @return NOT_CHANGED, CHANGED
	 * @throws KRBException 
	 */
	public void changePasswd(String principal, String oldPasswd, String newPasswd) throws KRBException;
			
	/**
	 * @param principal
	 * @return true if principal exists
	 * @throws KRBException 
	 */
	public boolean exists(String principal) throws KRBException ;	
	
	
	public void rename(String oldPrincipal,String newPrincipal)throws KRBException,KRBPrincipalAlreadyExistsException;

}
