package org.esupportail.activbo.services.kerberos;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class StandardInput extends Thread{
    
    private final Logger logger = new LoggerImpl(getClass());
    
    private final BufferedReader reader;

    private StandardInput(Process process) {
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    }
    
    static void background_log(Process process) {
        (new StandardInput(process)).start();
    }

    static String getFirstLine_and_log_the_rest(Process process) {
        var o = new StandardInput(process);
        var lines = o.readLines(1);
        o.start(); // log the rest
        return lines.isEmpty() ? null : lines.get(0);
    }
    
    /**
     * @param n nb minimun de lignes de sortie 
     */
    private ArrayList<String> readLines(int n)
    {
        var lines = new ArrayList<String>();
        try {
            for(int i = 0; i<n; i++) {
                String line = reader.readLine();
                if (line == null) break;
                logger.info(line);
                lines.add(line);
            }
        } catch(final IOException ioe) {logger.error(ioe);}

        return lines;
    }

    @Override
    public void run() {
        try {
            String line;
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
