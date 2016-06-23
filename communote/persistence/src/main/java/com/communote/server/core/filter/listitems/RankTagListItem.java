package com.communote.server.core.filter.listitems;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RankTagListItem
        extends com.communote.server.api.core.tag.TagData
        implements java.io.Serializable
        , com.communote.server.core.filter.listitems.RankListItem {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 4076563616132649968L;

    private Number rank;

    /**
     * Constructor.
     */
    public RankTagListItem() {
        super();
        this.rank = null;
    }

    /**
     * Constructor.
     * 
     * @param id
     *            The id.
     * @param rank
     *            The rank.
     * @param name
     *            The name.
     */
    public RankTagListItem(Long id, Number rank, String name) {
        super(name);
        setId(id);
        this.rank = rank;
    }

    /**
     * Constructor.
     * 
     * @param rank
     *            The rank.
     * @param defaultName
     *            The default name of the tag
     */
    public RankTagListItem(Number rank, String defaultName) {
        super(defaultName);
        this.rank = rank;
    }

    /**
     * Copies constructor from other RankTagListItem
     * 
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public RankTagListItem(RankTagListItem otherBean) {
        this(otherBean.getRank(), otherBean.getDefaultName());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     * 
     * @param otherBean
     *            Object to copy from.
     */
    public void copy(RankTagListItem otherBean) {
        if (otherBean != null) {
            this.setRank(otherBean.getRank());
            this.setName(otherBean.getName());
            super.copy(otherBean);
        }
    }

    /**
     * @return The rank.
     */
    @Override
    public Number getRank() {
        return this.rank;
    }

    /**
     * @param rank
     *            The rank.
     */
    public void setRank(Number rank) {
        this.rank = rank;
    }

}