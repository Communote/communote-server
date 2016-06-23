package com.communote.plugins.api.rest.resource.validation;

/**
 *
 * Default implementation of the validator. It is used, if no others are specified. By default all
 * validations are passed. A concrete validator can inherit from this class. In this case only the
 * relevant methods should be overridden.
 *
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
public class DefaultValidator<C, E, D, G, L> extends Validator<C, E, D, G, L> {

    @Override
    public void validateCreate(C createPrameter) throws ParameterValidationException {

    }

    @Override
    public void validateDelete(D deletePrameter) throws ParameterValidationException {

    }

    @Override
    public void validateEdit(E editPrameter) throws ParameterValidationException {

    }

    @Override
    public void validateGetSingle(G getSinglePrameter) throws ParameterValidationException {

    }

    @Override
    public void validateList(L listPrameter) throws ParameterValidationException {

    }

}
