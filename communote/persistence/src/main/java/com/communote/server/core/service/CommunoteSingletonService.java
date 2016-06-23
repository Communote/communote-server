package com.communote.server.core.service;

/**
 * Interface for marking a service in a clustered environment as a service which should only run on
 * one of the Communote instances.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface CommunoteSingletonService extends CommunoteService {

}
