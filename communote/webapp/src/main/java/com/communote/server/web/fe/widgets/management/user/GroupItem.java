package com.communote.server.web.fe.widgets.management.user;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

import com.communote.server.model.user.group.Group;

/**
 * Group item
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class GroupItem {

    /**
     * Compare the group items by name of the group. Simple, not perfect.
     * 
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     * 
     */
    public static class GroupItemComparator implements Comparator<GroupItem> {

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(GroupItem o1, GroupItem o2) {
            String name1 = o1.getGroup() == null ? null : o1.getGroup().getName();
            String name2 = o2.getGroup() == null ? null : o2.getGroup().getName();

            if (StringUtils.equals(name1, name2)) {
                return 0;
            }
            if (name1 == null) {
                return -1;
            } else if (name2 == null) {
                return 1;
            }
            return name1.compareToIgnoreCase(name2);
        }
    }

    private boolean isExternal = false;
    private Group group;

    /**
     * @return the group
     */
    public Group getGroup() {
        return group;
    }

    /**
     * @return the isExternal
     */
    public boolean getIsExternal() {
        return isExternal;
    }

    /**
     * @param group
     *            the group to set
     */
    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     * @param isExternal
     *            the isExternal to set
     */
    public void setIsExternal(boolean isExternal) {
        this.isExternal = isExternal;
    }

    @Override
    public String toString() {
        return "GroupItem [isExternal=" + isExternal + ", group=" + group == null ? null : group
                .attributesToString() + "]";
    }
}