package com.communote.server.api.core.tag;

/**
 * Type definition for TagStores.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface TagStoreType {

    /** Default types */
    public enum Types implements TagStoreType {

        /** Type of a store that contains note tags */
        NOTE("DefaultNoteTagStore"),
        /** Type of a store that contains topic tags */
        BLOG("DefaultBlogTagStore"),
        /** Type of a store that contains entity (user) tags */
        ENTITY("DefaultEntityTagStore");

        private final String defaultTagStoreId;

        /**
         * @param defaultTagStoreId
         *            The ID the default TagStore of this type should have. This TagStore will be
         *            available by default.
         */
        private Types(String defaultTagStoreId) {
            this.defaultTagStoreId = defaultTagStoreId;
        }

        /**
         * @return the ID of the TagStore of this type that exists by default
         */
        public String getDefaultTagStoreId() {
            return defaultTagStoreId;
        }
    }

}
