package org.esupportail.activbo.services.kerberos;


public class KRBAdminMock implements KRBAdmin {
    
    
    /** 
     * @see org.esupportail.activbo.services.kerberos.KRBAdmin#add(String, String)
     */
    public void add(String principal,String passwd)throws KRBPrincipalAlreadyExistsException,KRBException{
        throw new KRBPrincipalAlreadyExistsException("");
        //return ADDED; 
    }
    
    
    /** 
     * @see org.esupportail.activbo.services.kerberos.KRBAdmin#del(String)
     */
    public void del(final String principal) throws KRBException{
        //return DELETED; 
    } 
    
    /** 
     * @see org.esupportail.activbo.services.kerberos.KRBAdmin#changePasswd(String, String)
     */
    public void changePasswd(String principal,String passwd)throws KRBException,KRBIllegalArgumentException{
        //return CHANGED;
    }
    
    
    /** 
     * @see org.esupportail.activbo.services.kerberos.KRBAdmin#changePasswd(String, String, String)
     */
    public void changePasswd(String principal, String oldPasswd, String newPasswd)throws KRBException{
        
        //return CHANGED;
    }
            
    /** 
     * @see org.esupportail.activbo.services.kerberos.KRBAdmin#exists(String)
     */
    public boolean exists(String principal)throws KRBException {
        return true;
    }
    
    public void rename(String oldPrincipal,String newPrincipal)throws KRBException,KRBPrincipalAlreadyExistsException{
        
    }

    public String validatePassword(String principal, String password) throws KRBException {
        return "Fonction non traitee";
    }   

}
