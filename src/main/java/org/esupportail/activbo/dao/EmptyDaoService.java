package org.esupportail.activbo.dao;

import java.util.List;

import org.esupportail.activbo.domain.beans.User;
import org.esupportail.activbo.domain.beans.VersionManager;
import org.esupportail.commons.utils.Assert;
import org.esupportail.commons.web.beans.Paginator;
import org.esupportail.commons.services.application.ApplicationService;
import org.springframework.beans.factory.InitializingBean;

public class EmptyDaoService implements DaoService, InitializingBean {

	private static final long serialVersionUID = 3429875756876395115L;
	
	private static VersionManager versionManager;
    private ApplicationService applicationService;
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.applicationService, 
				"property applicationService of class " + this.getClass().getName() + " can not be null");			
	}
	
	public void addUser(User user) {
		// TODO Auto-generated method stub

	}

	public void addVersionManager(VersionManager versionManager) {
		// TODO Auto-generated method stub

	}

	public void deleteUser(User user) {
		// TODO Auto-generated method stub

	}

	public Paginator<User> getAdminPaginator() {
		// TODO Auto-generated method stub
		return null;
	}

	public User getUser(String id) {
		User user = new User();
		user.setId(id);
		// TODO Auto-generated method stub
		return user;
	}

	public List<User> getUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<VersionManager> getVersionManagers() {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateUser(User user) {
		// TODO Auto-generated method stub

	}

	public void updateVersionManager(VersionManager versionManager) {
		// TODO Auto-generated method stub

	}

	public VersionManager getVersionManager() {
		// TODO Auto-generated method stub
		if(versionManager==null)		{
			versionManager = new VersionManager();
			versionManager.setVersion(applicationService.getVersion().toString());
		}
		return versionManager;		
	}

	/**
	 * @return the applicationService
	 */
	public ApplicationService getApplicationService() {
		return applicationService;
	}

	/**
	 * @param applicationService the applicationService to set
	 */
	public void setApplicationService(ApplicationService applicationService) {
		this.applicationService = applicationService;
	}
}