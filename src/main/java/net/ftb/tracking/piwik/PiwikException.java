/**
 * Piwik - Open source web analytics
 * 
 * @license released under BSD License http://www.opensource.org/licenses/bsd-license.php
 * @link http://piwik.org/docs/tracking-api/
 *
 * @category Piwik
 * @package PiwikTracker
 */
package net.ftb.tracking.piwik;

/**
 *
 * @author Martin Fochler
 */
public class PiwikException extends Exception {

	public PiwikException(final String message) {
		super(message);
	}

	public PiwikException(final String message, final Throwable e) {
		super(message, e);
	}
}
