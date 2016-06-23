package com.communote.server.core.filter.listitems;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RankUserListItem extends com.communote.server.api.core.user.UserData
        implements java.io.Serializable,
        com.communote.server.core.filter.listitems.RankListItem {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 5631981984440779416L;

    private Number rank;

    /**
     * Default contructor
     */
    public RankUserListItem() {
        super();
        this.rank = null;
    }

    /**
     * Construct {@link RankUserListItem}
     * 
     * @param rank
     *            of user
     * @param userId
     *            of user
     * @param email
     *            of user
     * @param alias
     *            of user
     * @param firstName
     *            of user
     * @param lastName
     *            of user
     * @param salutation
     *            of user
     */
    public RankUserListItem(Number rank, Long userId, String email,
            String alias, String firstName, String lastName,
            String salutation) {
        super(userId, email, alias, firstName, lastName, salutation, null);
        this.rank = rank;
    }

    /**
     * Copies constructor from other RankUserListItem
     * 
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public RankUserListItem(RankUserListItem otherBean) {
        this(otherBean.getRank(), otherBean.getId(), otherBean.getEmail(), otherBean.getAlias(),
                otherBean.getFirstName(), otherBean.getLastName(), otherBean.getSalutation());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     * 
     * @param otherBean
     *            {@link RankUserListItem}
     */
    public void copy(RankUserListItem otherBean) {
        if (otherBean != null) {
            this.setRank(otherBean.getRank());
            this.setId(otherBean.getId());
            this.setEmail(otherBean.getEmail());
            this.setAlias(otherBean.getAlias());
            this.setFirstName(otherBean.getFirstName());
            this.setLastName(otherBean.getLastName());
            this.setSalutation(otherBean.getSalutation());
            this.setStatus(otherBean.getStatus());
        }
    }

    /**
     * Getter of user rank
     * 
     * @return number of user rank
     */
    public Number getRank() {
        return this.rank;
    }

    /**
     * Setter user ranke
     * 
     * @param rank
     *            number of rank
     */
    public void setRank(Number rank) {
        this.rank = rank;
    }

}