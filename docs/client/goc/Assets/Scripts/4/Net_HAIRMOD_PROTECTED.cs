using UnityEngine;

internal class Net_HAIRMOD_PROTECTED
{
	public static WWW www;

	public static Command_HAIRMOD_PROTECTED h;

	public static void update()
	{
		if (www != null && www.isDone)
		{
			string str = string.Empty;
			if (www.error == null || www.error.Equals(string.Empty))
			{
				str = www.text;
			}
			www = null;
			if (h != null)
			{
				h.perform(str);
			}
		}
	}

	public static void connectHTTP(string link, Command_HAIRMOD_PROTECTED h)
	{
		if (www != null)
		{
			Debug.LogError("GET HTTP BUSY");
		}
		Debug.LogWarning("REQUEST " + link);
		www = new WWW(link);
		Net_HAIRMOD_PROTECTED.h = h;
	}
}
