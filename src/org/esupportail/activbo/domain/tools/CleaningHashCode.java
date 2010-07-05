package org.esupportail.activbo.domain.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import org.esupportail.activbo.domain.beans.HashCode;
import org.springframework.beans.factory.InitializingBean;

public class CleaningHashCode extends Thread implements InitializingBean{
	
	private HashCode hashCode;
	private String formatDateConv;
	private String accessDateKey;
	
	
	public CleaningHashCode(){
		//this.hashCode=hashCode;
	}
	
	public void run(){
	
		try {
			while(true){
				System.out.println("888");
				
				
				if (!hashCode.isEmpty()){
					System.out.println("111");
					for (Map.Entry<String, HashMap<String,String>> e : hashCode.entrySet()){
						System.out.println("222");
						HashMap<String,String> hash=e.getValue();
						Date date=stringToDate(dateToString(new Date()));
						
						if (date.getTime()>this.stringToDate(hash.get(accessDateKey)).getTime()){
							System.out.println("temps depass�");
							hashCode.remove(e.getKey());
						}  
					} 
				}
				//mettre le temps dans un fichier de properties
				sleep(10);
				
			}
		
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	
	private String dateToString(Date sDate) throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat(formatDateConv);//formatDateConv);
        return sdf.format(sDate);
    }
    
	private Date stringToDate(String sDate) throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat(formatDateConv);//formatDateConv);
        return sdf.parse(sDate);
    }
	
	/*public void putHashCode(HashCode hash){
		this.hashCode=hash;
	}*/

	public String getFormatDateConv() {
		return formatDateConv;
	}

	public void setFormatDateConv(String formatDateConv) {
		this.formatDateConv = formatDateConv;
	}

	public String getAccessDateKey() {
		return accessDateKey;
	}

	public void setAccessDateKey(String accessDateKey) {
		this.accessDateKey = accessDateKey;
	}

	public HashCode getHashCode() {
		return hashCode;
	}

	public void setHashCode(HashCode hashCode) {
		this.hashCode = hashCode;
	}
	
}
