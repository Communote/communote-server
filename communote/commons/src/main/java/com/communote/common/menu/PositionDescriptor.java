package com.communote.common.menu;

/**
 * Describes the position of the entry in the menu.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PositionDescriptor {

    /**
     * Possible types of positioning a menu entry
     *
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     *
     */
    public enum PositionType {
        /**
         * The entry is added after another entry which is identified by an ID
         */
        AFTER,
        /**
         * The entry is added to the bottom of the menu or menu item.
         */
        BOTTOM,
        /**
         * The entry is added to the top of the menu or menu item.
         */
        TOP
    }

    private String entryId;
    private final PositionType type;

    /**
     * Create a new entry descriptor with <code>BOTTOM</code> positioning
     */
    public PositionDescriptor() {
        this.type = PositionType.BOTTOM;
    }

    /**
     * Create a new descriptor
     *
     * @param positionType
     *            how to position the entry
     * @param entryId
     *            the ID of another menu entry to position this item after if position type is
     *            <code>AFTER</code>
     */
    public PositionDescriptor(PositionType positionType, String entryId) {
        this.type = positionType;
        this.entryId = entryId;
    }

    /**
     * @return the ID of the menu entry to position this entry after if position type is
     *         <code>AFTER</code>. Should be null otherwise.
     */
    public String getMenuEntryId() {
        return this.entryId;
    }

    /**
     * @return the type of the position
     */
    public PositionType getPositionType() {
        return this.type;
    }
}
