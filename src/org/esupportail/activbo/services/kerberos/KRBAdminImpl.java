package org.esupportail.activbo.services.kerberos;

import java.io.IOException;
import java.io.PrintWriter;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.commons.utils.Assert;
import org.springframework.beans.factory.InitializingBean;


/**
 * @author aanli
 *
 */
public class KRBAdminImpl implements KRBAdmin, InitializingBean{
	
	/**
	 * Log4j logger.
	 */
	private final Logger logger = new LoggerImpl(getClass());
		
	private String  principalAdmin,principalAdminKeyTab;

	private String options="";
	
	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.principalAdmin, 
				"property principalAdmin of class " + this.getClass().getName() + " can not be null");
		Assert.notNull(this.principalAdminKeyTab, 
				"property principalAdminKeyTab of class " + this.getClass().getName() + " can not be null");			
	}
	
	public KRBAdminImpl(){
		super();		
	}
	
	
	/**
	 *  
	 * @throws KRBException 
	 * @see org.esupportail.activbo.services.kerberos.KRBAdmin#add(String, String)
	 * 
	 * AJOUT d'UN UTILISATEUR
	 * 
	 */
	public void add(String principal,String passwd) throws KRBException, KRBPrincipalAlreadyExistsException{
		
		//int state=ADDED;		
		String kadmin="kadmin -p "+principalAdmin+" -K "+principalAdminKeyTab;
		
		if( !(principal.contains(" ") || passwd.contains(" "))){
			if(!exists(principal)){
			
				String cmd =kadmin+" add --password="+passwd+" "+options+" "+principal;
				Runtime runtime = Runtime.getRuntime();
				Process process;
				try {
					//debug
					logger.debug(cmd.replaceFirst("--password=.* ", "--password=****** "));
					
					process = runtime.exec(cmd);
					
					//this command must be silence if not something unknown happened
					if(verboseProcess(process)) 
						throw new KRBException("Unknown error. See log files for more information");
					
				 }catch (IOException e) {				
					 logger.error(e);				 
					 throw new KRBException("IOException : "+e);				
				}
			
			}else 
				throw new KRBPrincipalAlreadyExistsException("Principal exists");
		
		}else 
			throw new KRBIllegalArgumentException("Illegal argument");		
		
		//return state;
	}
	
	
	/** 
	 * @see org.esupportail.activbo.services.kerberos.KRBAdmin#del(String)
	 * 
	 * SUPPRESSION D'UN UTILISATEUR
	 * 
	 */
	public void del(final String principal) throws KRBException{
		//int state=DELETED;
		String kadmin="kadmin -p "+principalAdmin+" -K "+principalAdminKeyTab;
		
		String cmd =kadmin+" del "+principal;
		Runtime runtime = Runtime.getRuntime();
		Process process;
		try {
			//debug
			logger.debug(cmd);
			
			process = runtime.exec(cmd);
			//this command must be silence if not something unknown happened
			if(verboseProcess(process)) 
				throw new KRBException("Unknown error. See log files for more information");
			
		 }catch (IOException e) {
			 logger.error(e);
			 throw new KRBException("IOException : "+e);		
		}
		
		//return state;
	}
	
	
	/** 
	 * @throws KRBException 
	 * @see org.esupportail.activbo.services.kerberos.KRBAdmin#changePasswd(String, String)
	 * 
	 * UTILISE pour l'ACTIVATION et la REINITIALISATION
	 * 
	 */
	public void changePasswd(String principal,String passwd) throws KRBException,KRBIllegalArgumentException{
		
		//int state=CHANGED;
		String kadmin="kadmin -p "+principalAdmin+" -K "+principalAdminKeyTab;
		
		//eliminer les requetes par injection de code
		if( !(principal.contains(" ") || passwd.contains(" "))){
			String cmd =kadmin+" passwd --password="+passwd+" "+principal;
			Runtime runtime = Runtime.getRuntime();
			Process process;
			try {
				//debug				
				logger.debug(cmd.replaceFirst("--password=.* ", "--password=****** "));

				process = runtime.exec(cmd);
				//this command must be silence if not something unknown happened
				if(verboseProcess(process)) 
					throw new KRBException("Unknown error. See log files for more information");

			}catch (IOException e){
				logger.error(e);
				throw new KRBException("IOException : "+e);		
			}
			
		}else 
			throw new KRBIllegalArgumentException("Illegal argument");		
		
		//return state;
	
	}
	
	/** 
	 * @throws KRBException 
	 * @see org.esupportail.activbo.services.kerberos.KRBAdmin#changePasswd(String, String, String)
	 *  
	 * UTILSISE pour le CHANGEMENT DE MOT DE PASSE
	 * 
	 */
	public void changePasswd(String principal, String oldPasswd, String newPasswd) throws KRBException{
		//int state=NOT_CHANGED;
		String cmd="kpasswd "+principal;
		Runtime runtime = Runtime.getRuntime();
		PrintWriter pw=null;	
		Process process=null;
		try {
			//debug
			logger.debug(cmd);
			process = runtime.exec(cmd);
			new ErrorInput(process);			
			
			//changement du mot de passe
			pw=new PrintWriter(process.getOutputStream());
			pw.println(oldPasswd);
			pw.println(newPasswd);
			pw.println(newPasswd);
			pw.flush();
			
			/*StandardInput input=new StandardInput(process,1);
			if(input.getLines().size()>0 && input.getLines().get(0).contains("Password changed"))
				state=CHANGED;*/																	
		
		}catch(IOException e) {
			logger.error(e);
			throw new KRBException("Unknown error. See log files for more information");
		
		}finally{
			if(pw!=null) 
				pw.close();
		}
		
		//return state;
	}
		
	/** 
	 * @throws KRBException 
	 * @see org.esupportail.activbo.services.kerberos.KRBAdmin#exists(String)
	 */
	public boolean exists(String principal) throws KRBException{
		
		boolean exist=false; 	
		String kadmin="kadmin -p "+principalAdmin+" -K "+principalAdminKeyTab;
		
		String cmd=kadmin+" get "+principal;
		Runtime runtime = Runtime.getRuntime();
		Process process=null;
		try {
			//debug
			logger.debug(cmd);
			
			process = runtime.exec(cmd);
			new ErrorInput(process);
			StandardInput input=new StandardInput(process,1);
			if(input.getLines().size()>0 && input.getLines().get(0).contains("Principal: "+principal))
				exist=true;
			
		} catch (IOException e) { 
			logger.error(e);
			throw new KRBException("Unknown error. See log files for more information");
		}
		return exist;
	}
	
	/**
	 * is used for silence process
	 * @param process
	 * @return true if this process is verbose 
	 */
	private boolean verboseProcess(Process process)
	{
				
		StandardInput input1=new StandardInput(process,1);
		ErrorInput input2=new ErrorInput(process,1);
		if(input1.getLines().size()>0 || input2.getLines().size()>0)
			return true;
		else 
			return false;					
	}
	


	public void setPrincipalAdmin(final String principalAdmin) {
		this.principalAdmin = principalAdmin;
	}


	public void setPrincipalAdminKeyTab(final String principalAdminKeyTab) {
		this.principalAdminKeyTab = principalAdminKeyTab;
	}

	public void setOptions(String options) {
		this.options = options;
	}

}
