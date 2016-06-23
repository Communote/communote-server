package com.communote.server.core.blog.export;

import org.apache.commons.lang.NotImplementedException;

import com.communote.server.core.tag.TagParserFactory;
import com.communote.server.core.vo.query.logical.AtomicTagFormula;
import com.communote.server.core.vo.query.logical.CompoundTagFormula;
import com.communote.server.core.vo.query.logical.LogicalTagFormula;

/**
 * Implements the mapping between parameters and names, to resolve the name for example a given
 * userId.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class FilterParameterResolver {

    private final static FilterParameterResolver INSTANCE = new FilterParameterResolver();

    /**
     * Runs this class as a singleton.
     *
     * @return Returns always the same object of this class.
     */
    public final static FilterParameterResolver getInstance() {
        return INSTANCE;
    }

    /**
     * Resolves the tags by a given structure to one String.
     *
     * @param formula
     *            The tag formula to resolve
     * @return String representation of the tags
     */
    public String resolveTags(LogicalTagFormula formula) {
        // get the filtered tags
        if (formula != null) {
            return tagFormulaToString(formula, TagParserFactory.instance().getDefaultTagParser()
                    .getSeparator());
        }
        return "";
    }

    /**
     * transform a tag formula to a string Note: only supports non negative conjunction, which will
     * result in a comma separated list
     *
     * @param f
     *            the formula
     * @param separator
     *            the separator used during tag parsing
     * @return the string representation
     */
    public String tagFormulaToString(LogicalTagFormula f, String separator) {
        if (f instanceof AtomicTagFormula) {
            return ((AtomicTagFormula) f).getTag();
        }
        CompoundTagFormula cf = (CompoundTagFormula) f;
        if (cf.isNegated() || cf.isDisjunction()) {
            throw new NotImplementedException("Creating string representation of disjunction or"
                    + " negations is not supported.");
        }
        String prefix = "";
        StringBuilder sb = new StringBuilder();
        AtomicTagFormula[] atoms = cf.getPositiveAtoms();
        for (int i = 0; i < atoms.length; i++) {
            sb.append(prefix);
            sb.append(atoms[i].getTag());
            prefix = separator + " ";
        }
        return sb.toString();
    }
}
