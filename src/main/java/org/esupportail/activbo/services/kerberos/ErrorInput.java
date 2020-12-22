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
	private final ArrayList<String> arrayLine=new ArrayList<String>();

	static void background_log(Process process) {
	    new ErrorInput(process);
	}

	static String getFirstLine_and_log_the_rest(Process process) {
	    ErrorInput errorIn = new ErrorInput(process, 1);
	    return errorIn.getLines().isEmpty() ? null : errorIn.getLines().get(0);
	}

	/**
	 * @param p
	 */
	private ErrorInput(Process process)
	{
		reader=new BufferedReader(new InputStreamReader(process.getErrorStream()));
		start();
	}

	/**
	 * @param p
	 * @param n nb minimun de lignes de sortie 
	 */
	private ErrorInput(Process process, int n)
	{
		reader=new BufferedReader(new InputStreamReader(process.getErrorStream()));
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
		start();
	}
	
	public ArrayList<String> getLines()
	{		
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
