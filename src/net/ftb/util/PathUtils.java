package net.ftb.util;

public class PathUtils
{
	public static String combine(String... paths)
	{
		String sep = System.getProperty("file.separator");
		
		StringBuilder pathBuilder = new StringBuilder();
		int i = 0;
		pathBuilder.append(paths[i++]);
		do
		{
			pathBuilder.append(sep);
			pathBuilder.append(paths[i++]);
		} while (i < paths.length);
		
		return pathBuilder.toString();
	}
}
