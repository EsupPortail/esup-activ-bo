/**
 * ESUP-Portail esup-activ-bo - Copyright (c) 2006 ESUP-Portail consortium
 * http://sourcesup.cru.fr/projects/esup-activ-bo
 */
package org.esupportail.activbo.services.application; 

import org.esupportail.commons.services.application.VersionException;
import org.esupportail.commons.services.application.VersionningService;

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
     * @param firstAdministratorId the firstAdministratorId to set
     */
    public void setFirstAdministratorId(final String firstAdministratorId) {
    }

}
