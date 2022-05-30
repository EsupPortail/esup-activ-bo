/**
 * ESUP-Portail esup-activ-bo - Copyright (c) 2006 ESUP-Portail consortium
 * http://sourcesup.cru.fr/projects/esup-activ-bo
 */
package org.esupportail.activbo.services.application; 

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.esupportail.activbo.domain.beans.User;
import org.esupportail.commons.exceptions.ConfigException;
import org.esupportail.commons.services.application.Version;
import org.esupportail.commons.services.application.VersionException;
import org.esupportail.commons.services.application.VersionningService;
import org.esupportail.commons.services.database.DatabaseUtils;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.commons.utils.Assert;

/**
 * A bean for versionning management.
 */
public class VersionningServiceImpl implements VersionningService { 
    /**
     * Set the database version.
     * @param version 
     * @param silent true to omit info messages
     */
    public void setDatabaseVersion(
            final String version, 
            final boolean silent) {
    }

    /**
     * @return the database version.
     */
    public Version getDatabaseVersion() {
            return null;
    }

    /**
     * @see org.esupportail.commons.services.application.VersionningService#initDatabase()
     */
    public void initDatabase() {
    }

    /**
     * @see org.esupportail.commons.services.application.VersionningService#checkVersion(boolean, boolean)
     */
    public void checkVersion(
            final boolean throwException,
            final boolean printLatestVersion) throws VersionException {
    }
    
    /**
     * Upgrade the database to version 0.1.0.
     */
    public void upgrade0d1d0() {
        // nothing to do yet
    }

    /**
     * @see org.esupportail.commons.services.application.VersionningService#upgradeDatabase()
     */
    public boolean upgradeDatabase() {
        return false;
    }

    /**
     * @return the firstAdministratorId
     */
    public String getFirstAdministratorId() {
        return null;
    }

    /**
     * @param firstAdministratorId the firstAdministratorId to set
     */
    public void setFirstAdministratorId(final String firstAdministratorId) {
    }

}
