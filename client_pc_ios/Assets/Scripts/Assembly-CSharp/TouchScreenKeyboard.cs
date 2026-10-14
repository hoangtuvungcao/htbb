/// <summary>
/// Bọc bàn phím ảo của Unity. Trên PC trả null (gõ bằng bàn phím thật như cũ);
/// trên điện thoại mở bàn phím hệ thống, TField đọc text / done như trước.
/// </summary>
public class TouchScreenKeyboard
{
	public static bool hideInput;

	public static bool visible
	{
		get
		{
			return UnityEngine.TouchScreenKeyboard.visible;
		}
	}

	private UnityEngine.TouchScreenKeyboard kb;

	public string text
	{
		get
		{
			return (kb == null || kb.text == null) ? string.Empty : kb.text;
		}
		set
		{
			if (kb != null && kb.active)
			{
				kb.text = value ?? string.Empty;
			}
		}
	}

	public bool done
	{
		get
		{
			if (kb == null)
			{
				return true;
			}
			UnityEngine.TouchScreenKeyboard.Status s = kb.status;
			return s == UnityEngine.TouchScreenKeyboard.Status.Done || s == UnityEngine.TouchScreenKeyboard.Status.Canceled || s == UnityEngine.TouchScreenKeyboard.Status.LostFocus;
		}
	}

	public static TouchScreenKeyboard Open(string text, TouchScreenKeyboardType t, bool b1, bool b2, bool type, bool b3, string caption)
	{
		if (!UnityEngine.Application.isMobilePlatform)
		{
			return null;
		}
		UnityEngine.TouchScreenKeyboard.hideInput = hideInput;
		UnityEngine.TouchScreenKeyboardType kieu = (t == TouchScreenKeyboardType.NumberPad) ? UnityEngine.TouchScreenKeyboardType.NumberPad : UnityEngine.TouchScreenKeyboardType.ASCIICapable;
		TouchScreenKeyboard k = new TouchScreenKeyboard();
		k.kb = UnityEngine.TouchScreenKeyboard.Open(text ?? string.Empty, kieu, false, false, type, false, caption ?? string.Empty);
		return k;
	}
}
