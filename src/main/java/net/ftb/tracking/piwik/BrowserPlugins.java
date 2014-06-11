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
public enum BrowserPlugins {

	/**
	 *  Browser plugins.
	 */
	FLASH("fla"), JAVA("java"), DIRECTOR("dir"), QUICKTIME("qt"),
	REALPLAYER("realp"), PDF("pdf"), WINDOWSMEDIA("wma"), GEARS("gears"),
	SILVERLIGHT("ag");

	/**
	 * The short URL.
	 */
	private String urlshort;

	/**
	 * Constructor that sets the short URL.
	 * @param urlshort 
	 */
	BrowserPlugins(final String urlshort) {
		this.urlshort = urlshort;
	}

	@Override
	public String toString() {
		return this.urlshort + "=true";
	}
}
