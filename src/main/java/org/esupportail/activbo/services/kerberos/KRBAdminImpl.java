package org.esupportail.activbo.services.kerberos;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
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
	
	private String kadminCmd;
	private String realm;
	
	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.principalAdmin, 
				"property principalAdmin of class " + this.getClass().getName() + " can not be null");
		Assert.notNull(this.principalAdminKeyTab, 
				"property principalAdminKeyTab of class " + this.getClass().getName() + " can not be null");
		Assert.notNull(this.kadminCmd, 
				"property kadminCmd of class " + this.getClass().getName() + " can not be null");	
	}
	
	public KRBAdminImpl(){
		super();		
	}
	
	
	/**
	 *  
	 * @throws KRBException 
	 * @see org.esupportail.activbo.services.kerberos.KRBAdmin#add(String, String)
	 * 
	 * 
	 * 
	 */
	public void add(String principal,String passwd) throws KRBException, KRBPrincipalAlreadyExistsException{
		if( !(principal.contains(" "))){
			if(!exists(principal)){
				//Passer cmd sous forme de tableau de String permet de gérer les mots de passe avec espace.
				//Si cmd est un String, kerberos refusera un mot de passe contenant un espace car il fait une concaténation de paramètres sans escaping
				String []opt=options.split(" ");
				String []cmd= {"add","--password="+passwd};
				cmd = (String[]) ArrayUtils.add(ArrayUtils.addAll(ArrayUtils.addAll(kadminCmd(),cmd),opt),principal);

				Runtime runtime = Runtime.getRuntime();
				Process process;
				try {
					//debug
					logger.debug((StringUtils.join(cmd, ",")).replaceFirst("--password=.* ", "--password=****** "));
					
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
		
	}
	
	
	/** 
	 * @see org.esupportail.activbo.services.kerberos.KRBAdmin#del(String)
	 */
	public void del(final String principal) throws KRBException{
		String []cmd = {"del",principal};
		cmd = (String[]) ArrayUtils.addAll(kadminCmd(),cmd);
		
		Runtime runtime = Runtime.getRuntime();
		Process process;
		try {
			//debug
			logger.debug(StringUtils.join(cmd, ","));
			
			process = runtime.exec(cmd);
			//this command must be silence if not something unknown happened
			if(verboseProcess(process)) 
				throw new KRBException("Unknown error. See log files for more information");
			
		 }catch (IOException e) {
			 logger.error(e);
			 throw new KRBException("IOException : "+e);		
		}
	}
	
	
	/** 
	 * @throws KRBException 
	 * @see org.esupportail.activbo.services.kerberos.KRBAdmin#changePasswd(String, String)
	 * 
*/
	public void changePasswd(String principal,String passwd) throws KRBException,KRBIllegalArgumentException{
		//eliminer les requetes par injection de code
		if( !(principal.contains(" "))){
			String []cmd = {"passwd","--password="+passwd,principal};
			cmd = (String[]) ArrayUtils.addAll(kadminCmd(),cmd);

			Runtime runtime = Runtime.getRuntime();
			Process process;
			try {
				//debug
				logger.debug((StringUtils.join(cmd, ",")).replaceFirst("--password=.* ", "--password=****** "));

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
	}
	
	/** 
	 * @throws KRBException 
	 * @see org.esupportail.activbo.services.kerberos.KRBAdmin#changePasswd(String, String, String)
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
		
	}
	/** 
	 * @throws KRBException,KRBPrincipalAlreadyExistsException 
	 * @see org.esupportail.activbo.services.kerberos.KRBAdmin#rename(String,String)
	 */
	public void rename(String oldPrincipal,String newPrincipal)throws KRBException,KRBPrincipalAlreadyExistsException{
		String []cmd = {"rename",oldPrincipal,newPrincipal};
		cmd = (String[]) ArrayUtils.addAll(kadminCmd(),cmd);
		
		if(this.exists(newPrincipal)) throw new KRBPrincipalAlreadyExistsException("The new principal "+newPrincipal+" already exists");
		
		Runtime runtime = Runtime.getRuntime();
		Process process;
		try {
			//debug
			logger.debug(StringUtils.join(cmd, " "));
			
			process = runtime.exec(cmd);
			//this command must be silence if not something unknown happened
	
			if(verboseProcess(process)) 
				throw new KRBException("Unknown error. See log files for more information. oldPrincipal="+oldPrincipal+", newPrincipal="+newPrincipal);
			
		 }catch (IOException e) {
			 logger.error(e);
			 throw new KRBException("IOException : "+e);		
		}
	}
		
	/** 
	 * @throws KRBException 
	 * @see org.esupportail.activbo.services.kerberos.KRBAdmin#exists(String)
	 */
	public boolean exists(String principal) throws KRBException{
		
		boolean exist=true; 	
		String []cmd = {"list","-s",principal};
		cmd = (String[]) ArrayUtils.addAll(kadminCmd(),cmd);
		Runtime runtime = Runtime.getRuntime();
		Process process=null;
		try {
			//debug
			logger.debug(StringUtils.join(cmd, " "));
			
			process = runtime.exec(cmd);
			ErrorInput errorIn=new ErrorInput(process,1);
			new StandardInput(process);
			 if(errorIn.getLines().size()>0 && errorIn.getLines().get(0).contains("Principal does not exist"))
                  exist=false;
			
		} catch (IOException e) { 
			logger.error(e);
			throw new KRBException("Unknown error. See log files for more information");
		}
		return exist;
	}
	

	public String validatePassword(String principal, String password) throws KRBException{
		String stdout = null;
		String []kadmin={kadminCmd};
		String []cmd = {"verify-password-quality",principal,password};
		cmd = (String[]) ArrayUtils.addAll(kadmin,cmd);
		Runtime runtime = Runtime.getRuntime();
		Process process=null;
		try {
			//debug
			logger.debug(StringUtils.join(cmd, " "));

			process = runtime.exec(cmd);
			ErrorInput errorIn=new ErrorInput(process,1);
			new StandardInput(process);
			if(errorIn.getLines().size()>0 )
			 stdout=errorIn.getLines().get(0).toString();

		} catch (IOException e) {
			logger.error(e);
			throw new KRBException("Unknown error. See log files for more information");
		}
		return stdout;
	}

	private String[] kadminCmd() {
		//Ajouter realm, permet de travailler sur le domain test(TEST.UNIV-PARIS1.FR) ou prod
		String []kadmin={kadminCmd,"-r",realm,"-p",principalAdmin,"-K",principalAdminKeyTab};
		return kadmin;
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

	/**
	 * @param kadminCmd the kadminCmd to set
	 */
	public void setKadminCmd(String kadminCmd) {
		this.kadminCmd = kadminCmd;
	}
	public void setRealm(String realm) {
		this.realm = realm;
	}

}
