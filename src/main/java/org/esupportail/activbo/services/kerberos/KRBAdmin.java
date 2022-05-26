package org.esupportail.activbo.services.kerberos;


public interface KRBAdmin {
        
    public void add(String principal,String passwd) throws KRBException, KRBPrincipalAlreadyExistsException;
    
    public void del(final String principal) throws KRBException;
    
    public void changePasswd(String principal,String passwd) throws KRBException,KRBIllegalArgumentException;   
    
    /**
     * uses principal privilege
     */
    public void changePasswd(String principal, String oldPasswd, String newPasswd) throws KRBException;
            
    public boolean exists(String principal) throws KRBException ;   
    
    public void rename(String oldPrincipal,String newPrincipal)throws KRBException,KRBPrincipalAlreadyExistsException;

    public String validatePassword (String principal, String password) throws KRBException ;

}
