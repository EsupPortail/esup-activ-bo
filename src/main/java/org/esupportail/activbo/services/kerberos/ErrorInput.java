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
public class ErrorInput extends Thread{
	
	
	/**
	 * Log4j logger.
	 */

	private final Logger logger = new LoggerImpl(getClass());
	
	private final BufferedReader reader;

	static void background_log(Process process) {
	    (new ErrorInput(process)).start();
	}

	static String getFirstLine_and_log_the_rest(Process process) {
	    ErrorInput errorIn = new ErrorInput(process);
	    ArrayList<String> lines = errorIn.readLines(1);
	    errorIn.start();
	    return lines.isEmpty() ? null : lines.get(0);
	}

	/**
	 * @param p
	 */
	private ErrorInput(Process process)
	{
		reader=new BufferedReader(new InputStreamReader(process.getErrorStream()));
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
				//error
				logger.warn(line);
				arrayLine.add(line);
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
					// Traitement du flux de sortie de l'application
					logger.warn(line);										
				}
			} finally {
				reader.close();				
			}
		} catch(final IOException ioe) {
			logger.error(ioe);
		}
	}
}
