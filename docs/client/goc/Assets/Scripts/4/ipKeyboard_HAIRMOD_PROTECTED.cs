using UnityEngine;

public class ipKeyboard_HAIRMOD_PROTECTED
{
	private static UnityEngine.TouchScreenKeyboard tk;

	public static int TEXT;

	public static int NUMBERIC = 1;

	public static int PASS = 2;

	private static Command_HAIRMOD_PROTECTED act;

	public static void openKeyBoard(string caption, int type, string text, Command_HAIRMOD_PROTECTED action)
	{
		act = action;
		TouchScreenKeyboardType_HAIRMOD_PROTECTED t = ((type == 0 || type == 2) ? TouchScreenKeyboardType_HAIRMOD_PROTECTED.ASCIICapable : TouchScreenKeyboardType_HAIRMOD_PROTECTED.NumberPad);
		TouchScreenKeyboard_HAIRMOD_PROTECTED.hideInput = false;
		tk = TouchScreenKeyboard_HAIRMOD_PROTECTED.Open(text, t, false, false, type == 2, false, caption);
	}

	public static void update()
	{
		if (tk != null && tk.done)
		{
			if (act != null)
			{
				act.perform(tk.text);
			}
			tk.text = string.Empty;
			tk = null;
		}
	}
}
