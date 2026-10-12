using UnityEngine;

public class TouchScreenKeyboard_HAIRMOD_PROTECTED
{
	public static bool hideInput;

	public static bool visible;

	public bool done;

	public string text;

	public static UnityEngine.TouchScreenKeyboard Open(string text, TouchScreenKeyboardType_HAIRMOD_PROTECTED t, bool b1, bool b2, bool type, bool b3, string caption)
	{
		if (Application.platform == RuntimePlatform.Android || Application.platform == RuntimePlatform.IPhonePlayer)
		{
			hideInput = true;
			return UnityEngine.TouchScreenKeyboard.Open(text, (UnityEngine.TouchScreenKeyboardType)t, b1, b2, type, b3, caption);
		}
		return null;
	}
}
