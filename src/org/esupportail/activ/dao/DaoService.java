/**
 * ESUP-Portail Application bo - Copyright (c) 2006 ESUP-Portail consortium
 * http://sourcesup.cru.fr/projects/esup-activ-bo
 */
package org.esupportail.activ.dao;

import java.io.Serializable;
import java.util.List;

import org.esupportail.activ.domain.beans.User;
import org.esupportail.activ.domain.beans.VersionManager;
import org.esupportail.commons.web.beans.Paginator;

/**
 * The DAO service interface.
 */
public interface DaoService extends Serializable {

	//////////////////////////////////////////////////////////////
	// User
	//////////////////////////////////////////////////////////////
	
	/**
	 * @param id
	 * @return the User instance that corresponds to an id.
	 */
	User getUser(String id);

	/**
	 * @return the list of all the users.
	 */
	List<User> getUsers();

	/**
	 * Add a user.
	 * @param user
	 */
	void addUser(User user);

	/**
	 * Delete a user.
	 * @param user
	 */
	void deleteUser(User user);

	/**
	 * Update a user.
	 * @param user
	 */
	void updateUser(User user);

	/**
	 * @return a paginator for administrators.
	 */
	Paginator<User> getAdminPaginator();

	//////////////////////////////////////////////////////////////
	// VersionManager
	//////////////////////////////////////////////////////////////
	
	/**
	 * @return the VersionManager of the database.
	 */
	VersionManager getVersionManager();

	/**
	 * Update a VersionManager.
	 * @param versionManager
	 */
	void updateVersionManager(VersionManager versionManager);

}
