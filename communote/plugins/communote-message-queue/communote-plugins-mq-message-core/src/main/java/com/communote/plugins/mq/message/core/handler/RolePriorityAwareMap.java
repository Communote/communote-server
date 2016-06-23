package com.communote.plugins.mq.message.core.handler;

import java.util.HashMap;

import com.communote.server.core.blog.helper.BlogRoleHelper;
import com.communote.server.model.blog.BlogRole;

/**
 * Map is controlling topic roles, taking into account their priorities. That
 * is, if several roles are assigned to one external object, the more powerful
 * will be used
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
class RolePriorityAwareMap extends HashMap<String, BlogRole> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see java.util.HashMap#put(Object, Object)
	 */
	@Override
	public BlogRole put(String key, BlogRole value) {
		if (containsKey(key)) {
			value = BlogRoleHelper.getUpperRole(get(key), value);
		}
		return super.put(key, value);
	}
}
