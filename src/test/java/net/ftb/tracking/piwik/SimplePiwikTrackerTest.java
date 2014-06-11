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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Locale;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Martin Fochler
 */
public class SimplePiwikTrackerTest {

	private static final String TEST_VISITORID = "1f3e4069f7a5f882";
	private SimplePiwikTracker tracker;
	
	@Before
	public void setUp() throws PiwikException {		
		tracker = new SimplePiwikTracker("http://localhost/piwik");
	}

	/**
	 * Test of readRequestInfos method, of class SimplePiwikTracker.
	 */
	@Test
	public void testReadRequestInfos() throws PiwikException, MalformedURLException {
		HttpServletRequest request = null;
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.readRequestInfos(request);
	}

	/**
	 * Test of setAcceptLanguage method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetAcceptLanguage_String() throws PiwikException, MalformedURLException {
		String language = "";
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.setAcceptLanguage(language);
	}

	/**
	 * Test of setAcceptLanguage method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetAcceptLanguage_Locale() throws PiwikException, MalformedURLException {
		Locale locale = Locale.getDefault();
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.setAcceptLanguage(locale);
	}

	/**
	 * Test of setApiurl method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetApiurl_String() throws PiwikException, MalformedURLException {
		String apiurl = "http://localhost/piwik";
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.setApiurl(apiurl);
	}

	/**
	 * Test of setApiurl method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetApiurl_URL() throws PiwikException, MalformedURLException {
		URL apiurl = new URL("http://localhost/piwik");
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.setApiurl(apiurl);
	}

	/**
	 * Test of setCustomData method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetCustomData() throws PiwikException, MalformedURLException {
		String customData = "";
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.setCustomData(customData);
	}

	/**
	 * Test of setDebug_append_url method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetDebug_append_url() throws PiwikException, MalformedURLException {
		String debug_append_url = "";
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.setDebugAppendUrl(debug_append_url);
	}

	/**
	 * Test of setForcedDatetime method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetForcedDatetime() throws PiwikException, MalformedURLException {
		Date forcedDatetime = null;
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.setForcedDatetime(forcedDatetime);
	}

	/**
	 * Test of setIp method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetIp() throws PiwikException, MalformedURLException {
		String ip = "";
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.setIp(ip);
	}

	/**
	 * Test of setIdSite method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetIdSite() throws PiwikException, MalformedURLException {
		int idSite = 0;
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.setIdSite(idSite);
	}

	/**
	 * Test of setPageUrl method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetPageUrl() throws PiwikException, MalformedURLException {
		String pageUrl = "";
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.setPageUrl(pageUrl);
	}

	/**
	 * Test of setResolution method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetResolution() throws PiwikException, MalformedURLException {
		int width = 0;
		int height = 0;
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.setResolution(width, height);
	}

	/**
	 * Test of setRequestCookie method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetRequestCookie() throws PiwikException, MalformedURLException {
		Cookie requestCookie = null;
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		boolean expResult = false;
		boolean result = tracker.setRequestCookie(requestCookie);
		Assert.assertEquals(expResult, result);
	}

	/**
	 * Test of setToken_auth method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetToken_auth() throws PiwikException, MalformedURLException {
		String token_auth = "";
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.setTokenAuth(token_auth);
	}

	/**
	 * Test of setUrlReferrer method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetUrlReferrer_String() throws PiwikException, MalformedURLException {
		String urlReferrer = "";
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.setUrlReferrer(urlReferrer);
	}

	/**
	 * Test of setUrlReferrer method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetUrlReferrer_URL() throws PiwikException, MalformedURLException {
		URL urlReferrer = null;
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.setUrlReferrer(urlReferrer);
	}

	/**
	 * Test of setUserAgent method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetUserAgent() throws PiwikException, MalformedURLException {
		String userAgent = "";
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.setUserAgent(userAgent);
	}

	/**
	 * Test of setVisitorId method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetVisitorId() throws PiwikException, MalformedURLException {
		String visitorId = "";
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.setVisitorId(visitorId);
	}

	/**
	 * Test of setCustomVariable method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetCustomVariable() throws PiwikException, MalformedURLException {
		String name = "testvar";
		String value = "testvalue";
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		int expResult = 1;
		int result = tracker.setPageCustomVariable(name, value);
		Assert.assertEquals(expResult, result);
	}

	/**
	 * Test of clearCustomVariables method, of class SimplePiwikTracker.
	 */
	@Test
	public void testClearCustomVariables() throws PiwikException, MalformedURLException {
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.clearCustomVariables();
	}

	/**
	 * Test of setPlugin method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetPlugin() throws PiwikException, MalformedURLException {
		BrowserPlugins plugin = BrowserPlugins.FLASH;
		boolean enabled = true;
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.setPlugin(plugin, enabled);
	}

	/**
	 * Test of clearPluginList method, of class SimplePiwikTracker.
	 */
	@Test
	public void testClearPluginList() throws PiwikException, MalformedURLException {
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.clearPluginList();
	}

	/**
	 * Test of setLocalTime method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetLocalTime_String() throws PiwikException, MalformedURLException {
		String time = "20:12:53";
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.setLocalTime(time);
	}

	/**
	 * Test of setLocalTime method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSetLocalTime_Date() throws PiwikException, MalformedURLException {
		Date time = null;
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		tracker.setLocalTime(time);
	}

	/**
	 * Test of getGeneralQuery method, of class SimplePiwikTracker.
	 */
	@Test
	public void testGetGeneralQuery() throws PiwikException, MalformedURLException {
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		String result = tracker.getGeneralQuery();
		Assert.assertEquals(0, this.assertGeneralURLPart(result));
	}

	private int assertGeneralURLPart(final String result) {
		String expResult = "idsite=0&rec=1&apiv=1&_id=" + SimplePiwikTrackerTest.TEST_VISITORID + "&cookie=false&r=";
		String generalpart = result.substring(0, expResult.length());
		String rest = result.substring(generalpart.length());
		int index = rest.indexOf("&");
		if (index == -1) {
			index = rest.length();
		}
		String random = result.substring(generalpart.length(), generalpart.length() + index);
		Assert.assertEquals(expResult, generalpart);
		//        Assert.assertTrue(random.matches("[0-9]\\.[0-9]{" + (index - 2) + "}"));
		Assert.assertTrue(random.matches("[0-9]{6}"));
		Assert.assertEquals(index, random.length());
		return result.length() - generalpart.length() - random.length();
	}

	private void assertFullUrl(final String expEnding, final String expApiurl, final String result) {
		String apiurl = result.substring(0, expApiurl.length());
		Assert.assertEquals(expApiurl, apiurl);
		int restlength = this.assertGeneralURLPart(result.substring(apiurl.length()));
		Assert.assertEquals(expEnding, result.substring(result.length() - restlength));
	}

	/**
	 * Test of getGoalTrackURL method, of class SimplePiwikTracker.
	 */
	@Test
	public void testGetGoalTrackURL_String() throws PiwikException, MalformedURLException {
		String goal = "testgoal";
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		String expResult = "&idgoal=testgoal";
		this.assertFullUrl(expResult, "http://localhost/piwik/piwik.php?", tracker.getGoalTrackURL(goal).toString());
	}

	/**
	 * Test of getGoalTrackURL method, of class SimplePiwikTracker.
	 */
	@Test
	public void testGetGoalTrackURL_String_String() throws PiwikException, MalformedURLException {
		String goal = "testgoal";
		String revenue = "1";
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		String expResult = "&idgoal=testgoal&revenue=1";
		this.assertFullUrl(expResult, "http://localhost/piwik/piwik.php?", tracker.getGoalTrackURL(goal, revenue).toString());
	}

	/**
	 * Test of getDownloadTrackURL method, of class SimplePiwikTracker.
	 */
	@Test
	public void testGetDownloadTrackURL() throws PiwikException, MalformedURLException {
		String downloadurl = "http://localhost/testdownload.pdf";
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		String expResult = "&download=http%3A%2F%2Flocalhost%2Ftestdownload.pdf";
		this.assertFullUrl(expResult, "http://localhost/piwik/piwik.php?", tracker.getDownloadTrackURL(downloadurl).toString());
	}

	/**
	 * Test of getLinkTrackURL method, of class SimplePiwikTracker.
	 */
	@Test
	public void testGetLinkTrackURL() throws PiwikException, MalformedURLException {
		String linkurl = "http://localhost/testlink";
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		String expResult = "&link=http%3A%2F%2Flocalhost%2Ftestlink";
		this.assertFullUrl(expResult, "http://localhost/piwik/piwik.php?", tracker.getLinkTrackURL(linkurl).toString());
	}

	/**
	 * Test of getPageTrackURL method, of class SimplePiwikTracker.
	 */
	@Test
	public void testGetPageTrackURL() throws PiwikException, MalformedURLException {
		String pagename = "testpage";
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		String expResult = "&action_name=testpage";
		this.assertFullUrl(expResult, "http://localhost/piwik/piwik.php?", tracker.getPageTrackURL(pagename).toString());
	}

	/**
	 * Test of sendRequest method, of class SimplePiwikTracker.
	 */
	@Test
	public void testSendRequest() throws PiwikException, MalformedURLException {
		URL destination = null;
		tracker.setVisitorId(SimplePiwikTrackerTest.TEST_VISITORID);
		ResponseData expResult = null;
		ResponseData result = tracker.sendRequest(destination);
		Assert.assertEquals(expResult, result);
	}
}
