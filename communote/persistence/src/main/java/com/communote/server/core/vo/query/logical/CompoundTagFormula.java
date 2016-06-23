package com.communote.server.core.vo.query.logical;

import java.util.HashSet;
import java.util.Set;

/**
 * An logical tag formula that can contain atoms and other compound formulas. The sub elements are
 * connected by logical disjunction or conjunction.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class CompoundTagFormula extends LogicalTagFormula {

    public enum CompoundFormulaType {
        DISJUNCTION, CONJUNCTION
    }

    private final Set<AtomicTagFormula> atoms;

    private final Set<AtomicTagFormula> negatedAtoms;
    private final Set<CompoundTagFormula> subformulas;
    private final CompoundFormulaType type;

    /**
     * Creates a new compound formula.
     * 
     * @param type
     *            determines whether the sub-elements (atoms or other compound formulas) should be
     *            connected by disjunction or conjunction
     * @param negated
     *            determines whether the whole formula should be negated
     */
    public CompoundTagFormula(CompoundFormulaType type, boolean negated) {
        this.type = type;
        setNegated(negated);
        atoms = new HashSet<AtomicTagFormula>();
        negatedAtoms = new HashSet<AtomicTagFormula>();
        subformulas = new HashSet<CompoundTagFormula>();
    }

    /**
     * Adds an atom to this formula.
     * 
     * @param atom
     *            an atomic formula
     */
    public void addAtomicFormula(AtomicTagFormula atom) {
        if (atom.isNegated()) {
            negatedAtoms.add(atom);
        } else {
            atoms.add(atom);
        }
    }

    /**
     * Adds a compound sub-formula to this formula.
     * 
     * @param cf
     *            a compound formula
     */
    public void addCompoundFormula(CompoundTagFormula cf) {
        subformulas.add(cf);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countAtomicTags() {
        int count = 0;
        for (LogicalTagFormula formula : atoms) {
            count += formula.countAtomicTags();
        }
        for (LogicalTagFormula formula : negatedAtoms) {
            count += formula.countAtomicTags();
        }
        for (LogicalTagFormula formula : subformulas) {
            count += formula.countAtomicTags();
        }
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CompoundTagFormula other = (CompoundTagFormula) obj;
        if (atoms == null) {
            if (other.atoms != null) {
                return false;
            }
        } else if (!atoms.equals(other.atoms)) {
            return false;
        }
        if (negatedAtoms == null) {
            if (other.negatedAtoms != null) {
                return false;
            }
        } else if (!negatedAtoms.equals(other.negatedAtoms)) {
            return false;
        }
        if (subformulas == null) {
            if (other.subformulas != null) {
                return false;
            }
        } else if (!subformulas.equals(other.subformulas)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

    /**
     * Returns an array with all atoms which are negated. The array might be empty.
     * 
     * @return an array of atoms
     */
    public AtomicTagFormula[] getNegatedAtoms() {
        AtomicTagFormula[] atomicFormulas = new AtomicTagFormula[negatedAtoms.size()];
        int i = 0;
        for (Object o : negatedAtoms.toArray()) {
            atomicFormulas[i] = (AtomicTagFormula) o;
            i++;
        }
        return atomicFormulas;
    }

    /**
     * Returns an array with all atoms which are not negated. The array might be empty.
     * 
     * @return an array of atoms
     */
    public AtomicTagFormula[] getPositiveAtoms() {
        AtomicTagFormula[] atomicFormulas = new AtomicTagFormula[atoms.size()];
        int i = 0;
        for (Object o : atoms.toArray()) {
            atomicFormulas[i] = (AtomicTagFormula) o;
            i++;
        }
        return atomicFormulas;
    }

    /**
     * Returns an array with all compound sub-formulas of this formula. The array might be empty.
     * 
     * @return an array with compound sub-formulas
     */
    public CompoundTagFormula[] getSubformulas() {
        CompoundTagFormula[] formulas = new CompoundTagFormula[subformulas.size()];
        int i = 0;
        for (Object o : subformulas.toArray()) {
            formulas[i] = (CompoundTagFormula) o;
            i++;
        }
        return formulas;
    }

    /**
     * Returns the type of the compound formula
     * 
     * @return the formula type
     */
    public CompoundFormulaType getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (atoms == null ? 0 : atoms.hashCode());
        result = prime * result + (negatedAtoms == null ? 0 : negatedAtoms.hashCode());
        result = prime * result + (subformulas == null ? 0 : subformulas.hashCode());
        result = prime * result + (type == null ? 0 : type.hashCode());
        return result;
    }

    /**
     * Returns whether this compound formula is a disjunction or conjunction.
     * 
     * @return true if the formula is a disjunction, false if it is a conjunction
     */
    public boolean isDisjunction() {
        return type.equals(CompoundFormulaType.DISJUNCTION);
    }
}
