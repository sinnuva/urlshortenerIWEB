package es.unizar.urlshortener.core
/**
 * Holds global constants for the application.
 */

/**
* Maximum number of allowed redirections for a short URL
* within the specified time period.
*/
const val MAX_REDIRECTIONS = 5

/**
* Time period, in minutes, within which redirections are counted.
* This controls how long a short URL can accumulate redirection 
* counts before the limit is reset.
*/
const val PERIOD_IN_MINUTES = 2
