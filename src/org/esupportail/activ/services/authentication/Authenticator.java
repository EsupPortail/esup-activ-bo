package org.esupportail.activ.services.authentication;

import org.esupportail.activ.domain.beans.User;

/**
 * The interface of authenticators.
 */
public interface Authenticator {

	/**
	 * @return the authenticated user.
	 */
	User getUser();

}