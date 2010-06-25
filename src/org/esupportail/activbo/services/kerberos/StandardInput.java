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
	private final ArrayList<String> arrayLine=new ArrayList<String>();

	/**
	 * @param p
	 */
	public StandardInput(Process process)
	{
		reader=new BufferedReader(new InputStreamReader(process.getInputStream()));
		start();
	}
	
	/**
	 * @param p
	 * @param n nb minimun de lignes de sortie 
	 */
	public StandardInput(Process process, int n)
	{
		reader=new BufferedReader(new InputStreamReader(process.getInputStream()));
		int i=0;
		String line="";
		try{
			while(i<n && (line = reader.readLine())!=null)
			{
				//INFO	
				logger.info(line);
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
