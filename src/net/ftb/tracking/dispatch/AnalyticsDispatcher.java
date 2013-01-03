package net.ftb.tracking.dispatch;

import java.net.URI;

import net.ftb.log.Logger;

public abstract class AnalyticsDispatcher {
	private String userAgent;
	private String host;
	private int port;

	public AnalyticsDispatcher(String userAgent, String host, int port) {
		this.userAgent = userAgent;
		this.host = host;
		this.port = port;
	}

	public void dispatch(String analyticsString) {
		URI uri = URI.create(analyticsString);
		String timeDispatched = getQueryParameter(uri.getQuery(), "utmht");
		if(timeDispatched != null) {
			try {
				Long time = Long.valueOf(Long.parseLong(timeDispatched));
				analyticsString = analyticsString + "&utmqt="
						+ (System.currentTimeMillis() - time.longValue());
			} catch (NumberFormatException e) {
				Logger.logError("Error parsing utmht parameter: ", e);
			}
		} else {
			Logger.logWarn("Unable to find utmht parameter: " + analyticsString);
		}
		dispatchToNetwork(analyticsString);
	}

	protected abstract void dispatchToNetwork(String analyticsString);

	protected static String getQueryParameter(String query, String parameter) {
		String[] params = query.split("&");
		for(String param : params) {
			String[] nameValue = param.split("=");
			if(nameValue[0].equals(parameter)) {
				return nameValue[1];
			}
		}
		return null;
	}
}