package org.esupportail.activbo.domain.tools;

import java.text.Collator;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;

import sun.text.Normalizer;

public class StringTools {
	
	
	public static boolean compareInsensitive(String str1, String str2) {

//		Logger.debug("Comparing : " + str1 + " and " + str2);

		String strTmp1 = str1.replaceAll("[-|']+", " ");
		String strTmp2 = str2.replaceAll("[-|']+", " ");
		Collator collator = Collator.getInstance();
		collator.setStrength(Collator.PRIMARY);

		if (collator.compare(strTmp1, strTmp2) == 0) {
//			logger.debug("Strings are equivalent");
			return true;
		}

//		logger.debug("Strings  are different");
		return false;
	}
	
	public static String cleanAllSpecialChar(String str) {

//		Logger.debug("Comparing : " + str1 + " and " + str2);

		String strTmp1 = str.toLowerCase();
		strTmp1 = strTmp1.replaceAll("[^a-z]+", "");

		//strTmp1 = Normalizer.normalize(strTmp1, Normalizer.DECOMP, 0);
	    return strTmp1.replaceAll("[^\\p{ASCII}]","");

	}

}
