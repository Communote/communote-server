package com.communote.plugins.api.rest.resource.validation;


/**
 * abstract class, defining validator for the rest parameters. To be implemented for each resource,
 * that need to verify its parameters.
 * 
 * Appropriate method is invoked during the request processing. In order to tell that some errors
 * have been occurred, the method implementation is supposed to throw ParameterValidationException.
 * The exception includes list of all the errors, that could have been discovered
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <C>
 *            create resource parameter
 * @param <E>
 *            edit resource parameter
 * @param <D>
 *            delete resource parameter
 * @param <G>
 *            get resource parameter
 * @param <L>
 *            list resource parameter
 */
public abstract class Validator<C, E, D, G, L> {

    /**
     * validates create parameter
     * 
     * @param createPrameter
     *            parameter to be validated
     * @throws ParameterValidationException
     *             is thrown if validation fails
     */
    public abstract void validateCreate(C createPrameter) throws ParameterValidationException;

    /**
     * validates delete parameter
     * 
     * @param deletePrameter
     *            parameter to be validated
     * @throws ParameterValidationException
     *             is thrown if validation fails
     */
    public abstract void validateDelete(D deletePrameter) throws ParameterValidationException;;

    /**
     * validates edit parameter
     * 
     * @param editPrameter
     *            parameter to be validated
     * @throws ParameterValidationException
     *             is thrown if validation fails
     */
    public abstract void validateEdit(E editPrameter) throws ParameterValidationException;;

    /**
     * validates get parameter
     * 
     * @param getSinglePrameter
     *            parameter to be validated
     * @throws ParameterValidationException
     *             is thrown if validation fails
     */
    public abstract void validateGetSingle(G getSinglePrameter) throws ParameterValidationException;;

    /**
     * validates list parameter
     * 
     * @param listPrameter
     *            parameter to be validated
     * @throws ParameterValidationException
     *             is thrown if validation fails
     */
    public abstract void validateList(L listPrameter) throws ParameterValidationException;;
}
