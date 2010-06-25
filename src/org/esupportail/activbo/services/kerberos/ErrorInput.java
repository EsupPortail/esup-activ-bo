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

	/**
	 * @param p
	 */
	public ErrorInput(Process process)
	{
		reader=new BufferedReader(new InputStreamReader(process.getErrorStream()));
		start();
	}
	
	/**
	 * @param p
	 * @param n nb minimun de lignes de sortie 
	 */
	public ErrorInput(Process process, int n)
	{
		reader=new BufferedReader(new InputStreamReader(process.getErrorStream()));
		int i=0;
		String line="";
		try{
			while(i<n && (line = reader.readLine())!=null)
			{
				//error
				logger.error(line);
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
					logger.error(line);										
				}
			} finally {
				reader.close();				
			}
		} catch(final IOException ioe) {
			logger.error(ioe);
		}
	}
}
