package org.esupportail.activbo.services.kerberos;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;


public class KRBAdminImpl implements KRBAdmin, InitializingBean{
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
        
    private String principalAdmin;
    private String principalAdminKeyTab;
    private String addOptions="";   
    private String kadminCmd;
    private String realm;

    public void setPrincipalAdmin(final String principalAdmin) { this.principalAdmin = principalAdmin; }
    public void setPrincipalAdminKeyTab(final String principalAdminKeyTab) { this.principalAdminKeyTab = principalAdminKeyTab; }
    public void setAddOptions(String addOptions) { this.addOptions = addOptions; }
    public void setKadminCmd(String kadminCmd) { this.kadminCmd = kadminCmd; }
    public void setRealm(String realm) { this.realm = realm; }
    
    public void afterPropertiesSet() throws Exception {
        if (this.principalAdmin == null)
                throw new Exception("property principalAdmin of class " + this.getClass().getName() + " can not be null");
        if (this.principalAdminKeyTab == null)
                throw new Exception("property principalAdminKeyTab of class " + this.getClass().getName() + " can not be null");
        if (this.kadminCmd == null)
                throw new Exception("property kadminCmd of class " + this.getClass().getName() + " can not be null");   
    }
    

    public void add(String principal,String passwd) throws KRBException {
        if (principal.contains(" "))
            throw new KRBIllegalArgumentException("Illegal argument");      

        if (exists(principal))
            throw new KRBPrincipalAlreadyExistsException("Principal exists");

        //Passer cmd sous forme de tableau de String permet de gerer les mots de passe avec espace.
        //Si cmd est un String, kerberos refusera un mot de passe contenant un espace car il fait une concatenation de parametres sans escaping
        var cmd = kadminCmd("add", "--password="+passwd);
        cmd = (String[]) ArrayUtils.add(ArrayUtils.addAll(cmd, addOptions.split(" ")), principal);
        logger.debug((StringUtils.join(cmd, " ")).replaceFirst("--password=.* ", "--password=****** "));
            
        Process process = run_cmd(cmd);           
        //this command must be silent. if not something unknown happened
        if (verboseProcess(process)) 
            throw new KRBException("Unknown error. See log files for more information");            
    }
    
    public void del(final String principal) throws KRBException{
        var cmd = kadminCmd("del", principal);  
        logger.debug(StringUtils.join(cmd, ","));
            
        Process process = run_cmd(cmd);
        //this command must be silent if not something unknown happened
        if (verboseProcess(process)) 
            throw new KRBException("Unknown error. See log files for more information");
            
    }
    
    public void changePasswd(String principal,String passwd) throws KRBException {
        //eliminer les requetes par injection de code
        if ( principal.contains(" "))
            throw new KRBIllegalArgumentException("Illegal argument");          

        var cmd = kadminCmd("passwd", "--password="+passwd,principal);

        //debug
        logger.debug((StringUtils.join(cmd, " ")).replaceFirst("--password=.* ", "--password=****** "));

        var process = run_cmd(cmd);
        //this command must be silent. if not something unknown happened
        if (verboseProcess(process)) 
            throw new KRBException("Unknown error. See log files for more information");
    }
    
    public void changePasswd(String principal, String oldPasswd, String newPasswd) throws KRBException{
        String[] cmd= { "kpasswd", principal };
        logger.debug(StringUtils.join(cmd, " "));           

        Process process = run_cmd(cmd);

        ErrorInput.background_log(process);
            
        //changement du mot de passe
        var pw = new PrintWriter(process.getOutputStream());
        pw.println(oldPasswd);
        pw.println(newPasswd);
        pw.println(newPasswd);
        pw.flush();
        pw.close();
            
        /*StandardInput input=StandardInput.background_log(process,1);
        if (input.getLines().size()>0 && input.getLines().get(0).contains("Password changed"))
            state=CHANGED;*/                                                                                    
    }

    public void rename(String oldPrincipal,String newPrincipal)throws KRBException {
        if (exists(newPrincipal)) throw new KRBPrincipalAlreadyExistsException("The new principal "+newPrincipal+" already exists");

        var cmd = kadminCmd("rename", oldPrincipal,newPrincipal);       
        logger.debug(StringUtils.join(cmd, " "));           
        var process = run_cmd(cmd);
        //this command must be silence if not something unknown happened    
        if (verboseProcess(process)) 
            throw new KRBException("Unknown error. See log files for more information. oldPrincipal="+oldPrincipal+", newPrincipal="+newPrincipal);
            
    }
        
    public boolean exists(String principal) throws KRBException{    
        var cmd = kadminCmd("list", "-s", principal);
        
        var process = run_cmd(cmd);
        String err = ErrorInput.getFirstLine_and_log_the_rest(process);
        StandardInput.background_log(process);
        boolean notExist = err != null && err.contains("Principal does not exist");
        return !notExist;
    }

    public String validatePassword(String principal, String password) throws KRBException {
        String []cmd = kadminCmd("verify-password-quality", principal,password);
        logger.debug(StringUtils.join(cmd, " "));

        var process = run_cmd(cmd);
        var stdout = ErrorInput.getFirstLine_and_log_the_rest(process);
        StandardInput.background_log(process);
    
        return stdout;
    }

    private Process run_cmd(String[] cmd) throws KRBException {
        try {
            return Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            logger.error("", e);
            throw new KRBException("Unknown error. See log files for more information");
        }
    }

    private String[] kadminCmd(String cmd, String param1) {
        return kadminCmd(cmd, new String[] { param1 });
    }
    private String[] kadminCmd(String cmd, String param1, String param2) {
        return kadminCmd(cmd, new String[] { param1, param2 });
    }
    private String[] kadminCmd(String cmd, String[] params) {
        //Ajouter realm, permet de travailler sur le domain test(TEST.UNIV-PARIS1.FR) ou prod
        String[] kadmin = {kadminCmd,"-r",realm,"-p",principalAdmin,"-K",principalAdminKeyTab, cmd};
        return (String[]) ArrayUtils.addAll(kadmin, params);
    }

    /**
     * is used for silence process
     * @param process
     * @return true if this process is verbose 
     */
    private boolean verboseProcess(Process process) {               
        String input1 = StandardInput.getFirstLine_and_log_the_rest(process);
        String input2 = ErrorInput.getFirstLine_and_log_the_rest(process);
        return input1 != null || input2 != null;
    }
    
}
