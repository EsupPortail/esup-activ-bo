package org.esupportail.activbo.domain.beans.channels;


/**
 * @author aanli
 * Canal permettant l'envoi du code d'activation pour un utilisateur donné
 */
public interface Channel{
	/**
	 * @param id login de l'utilisateur concerné par le code
	 * @throws ChannelException 
	 */
	public void send(String id) throws ChannelException;
	
	/**
	 * @param name nom du canal
	 */
	public void setName(String name);
	
	/**
	 * @return name
	 */
	public String getName();

}
