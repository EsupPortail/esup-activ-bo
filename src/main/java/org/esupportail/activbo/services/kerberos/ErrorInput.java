package org.esupportail.activbo.services.kerberos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ErrorInput extends Thread {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final BufferedReader reader;

    private ErrorInput(Process process) {
        reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    }
    
    static void background_log(Process process) {
        (new ErrorInput(process)).start();
    }

    static String getFirstLine_and_log_the_rest(Process process) {
        var errorIn = new ErrorInput(process);
        var lines = errorIn.readLines(1);
        errorIn.start(); // log the rest
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
                logger.warn(line);
                lines.add(line);
            }
        } catch(final IOException ioe) {logger.error("", ioe);}
    
        return lines;
    }

    @Override
    public void run() {
        try {           
            String line;
            try {
                while((line = reader.readLine()) != null) {
                    // Traitement du flux de sortie de l'application
                    logger.warn(line);                                      
                }
            } finally {
                reader.close();             
            }
        } catch(final IOException ioe) {
            logger.error("", ioe);
        }
    }
}
