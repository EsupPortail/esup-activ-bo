package org.esupportail.activbo.services.authentication;

import org.esupportail.activbo.domain.beans.User;

/**
 * The interface of authenticators.
 */
public interface Authenticator {

	/**
	 * @return the authenticated user.
	 */
	User getUser();

}