/**
 * ESUP-Portail Application bo - Copyright (c) 2006 ESUP-Portail consortium
 * http://sourcesup.cru.fr/projects/esup-activ-bo
 */
package org.esupportail.activ.dao;

import java.util.List;

import org.esupportail.activ.domain.beans.User;
import org.esupportail.activ.domain.beans.VersionManager;
import org.esupportail.commons.dao.AbstractJdbcJndiHibernateDaoService;
import org.esupportail.commons.dao.HibernateFixedQueryPaginator;
import org.esupportail.commons.dao.HqlUtils;
import org.esupportail.commons.services.application.UninitializedDatabaseException;
import org.esupportail.commons.services.application.VersionningUtils;
import org.esupportail.commons.web.beans.Paginator;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.BadSqlGrammarException;

/**
 * The Hiberate implementation of the DAO service.
 */
public class HibernateDaoServiceImpl 
extends AbstractJdbcJndiHibernateDaoService
implements DaoService, InitializingBean {

	/**
	 * The serialization id.
	 */
	private static final long serialVersionUID = 3152554337896617315L;

	/**
	 * The name of the 'id' attribute.
	 */
	private static final String ID_ATTRIBUTE = "id";

	/**
	 * The name of the 'admin' attribute.
	 */
	private static final String ADMIN_ATTRIBUTE = "admin";

	/**
	 * Bean constructor.
	 */
	public HibernateDaoServiceImpl() {
		super();
	}

	//////////////////////////////////////////////////////////////
	// User
	//////////////////////////////////////////////////////////////

	/**
	 * @see org.esupportail.activ.dao.DaoService#getUser(java.lang.String)
	 */
	public User getUser(final String id) {
		return (User) getHibernateTemplate().get(User.class, id);
	}

	/**
	 * @see org.esupportail.activ.dao.DaoService#getUsers()
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUsers() {
		return getHibernateTemplate().loadAll(User.class);
	}

	/**
	 * @see org.esupportail.activ.dao.DaoService#addUser(org.esupportail.activ.domain.beans.User)
	 */
	public void addUser(final User user) {
		addObject(user);
	}

	/**
	 * @see org.esupportail.activ.dao.DaoService#deleteUser(org.esupportail.activ.domain.beans.User)
	 */
	public void deleteUser(final User user) {
		deleteObject(user);
	}

	/**
	 * @see org.esupportail.activ.dao.DaoService#updateUser(org.esupportail.activ.domain.beans.User)
	 */
	public void updateUser(final User user) {
		updateObject(user);
	}

	/**
	 * @see org.esupportail.activ.dao.DaoService#getAdminPaginator()
	 */
	public Paginator<User> getAdminPaginator() {
		String queryStr = HqlUtils.fromWhereOrderByAsc(
				User.class.getSimpleName(),
				HqlUtils.isTrue(ADMIN_ATTRIBUTE),
				ID_ATTRIBUTE);
		return new HibernateFixedQueryPaginator<User>(this, queryStr);
	}

	//////////////////////////////////////////////////////////////
	// VersionManager
	//////////////////////////////////////////////////////////////

	/**
	 * @see org.esupportail.activ.dao.DaoService#getVersionManager()
	 */
	@SuppressWarnings("unchecked")
	public VersionManager getVersionManager() {
		DetachedCriteria criteria = DetachedCriteria.forClass(VersionManager.class);
		criteria.addOrder(Order.asc(ID_ATTRIBUTE));
		List<VersionManager> versionManagers;
		try {
			versionManagers = getHibernateTemplate().findByCriteria(criteria);
		} catch (BadSqlGrammarException e) {
			throw new UninitializedDatabaseException(
					"your database is not initialized, please run 'ant init-data'", e);
		}
		if (versionManagers.isEmpty()) {
			VersionManager versionManager = new VersionManager();
			versionManager.setVersion(VersionningUtils.VERSION_0);
			addObject(versionManager);
			return versionManager;
		}
		return versionManagers.get(0);
	}

	/**
	 * @see org.esupportail.activ.dao.DaoService#updateVersionManager(
	 * org.esupportail.activ.domain.beans.VersionManager)
	 */
	public void updateVersionManager(final VersionManager versionManager) {
		updateObject(versionManager);
	}

	//////////////////////////////////////////////////////////////
	// misc
	//////////////////////////////////////////////////////////////

}
