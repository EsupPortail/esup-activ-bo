package org.esupportail.activbo.services.kerberos;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * @author aanli
 *
 */
public class StandardInput extends Thread{
    
    /**
     * Log4j logger.
     */
    private final Logger logger = new LoggerImpl(getClass());
    
    private final BufferedReader reader;

    static void background_log(Process process) {
        (new StandardInput(process)).start();
    }

    static String getFirstLine_and_log_the_rest(Process process) {
        StandardInput o = new StandardInput(process);
        ArrayList<String> lines = o.readLines(1);
        o.start();
        return lines.isEmpty() ? null : lines.get(0);
    }

    /**
     * @param p
     */
    private StandardInput(Process process)
    {
        reader=new BufferedReader(new InputStreamReader(process.getInputStream()));
    }
    
    /**
     * @param p
     * @param n nb minimun de lignes de sortie 
     */
    private ArrayList<String> readLines(int n)
    {
        ArrayList<String> arrayLine=new ArrayList<String>();
        int i=0;
        String line="";
        try{
            while(i<n && (line = reader.readLine())!=null)
            {
                //INFO  
                logger.info(line);
                arrayLine.add(line);
                i++;
            }
        }catch(final IOException ioe) {logger.error(ioe);}
        return arrayLine;
    }
    

    /** (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run()
    {
        try {
            String line = "";
            try {
                while((line = reader.readLine()) != null) {
                    // INFO Traitement du flux de sortie de l'application
                    logger.info(line);              
                }
            } finally {
                reader.close();             
            }
        } catch(final IOException ioe) {
            logger.error(ioe);
        }
    }
}
