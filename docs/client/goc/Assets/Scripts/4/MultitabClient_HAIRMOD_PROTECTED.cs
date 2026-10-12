using System;
using UnityEngine;
using UnityEngine.SceneManagement;

public class MultitabClient_HAIRMOD_PROTECTED
{
	public static Image_HAIRMOD_PROTECTED ic;

	public static int xI;

	public static int yI = Canvas_HAIRMOD_PROTECTED.h / 2;

	public static int lastX;

	public static int lastY;

	public static Command_HAIRMOD_PROTECTED command;

	public static bool isPointerDown;

	public static bool isCMD;

	private static Scene scene;

	public MultitabClient_HAIRMOD_PROTECTED()
	{
		scene = SceneManager.GetActiveScene();
		xI = 0;
		yI = Canvas_HAIRMOD_PROTECTED.h / 2;
	}

	public static Image_HAIRMOD_PROTECTED loadImage(string path)
	{
		TextAsset textAsset = Resources.Load(path) as TextAsset;
		return Image_HAIRMOD_PROTECTED.__createImage(textAsset.bytes);
	}

	public static string cutPng(string str)
	{
		string result = str;
		if (str.Contains(".png"))
		{
			result = str.Replace(".png", string.Empty);
		}
		return result;
	}

	public static void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (ic == null)
		{
			ic = loadImage("res/ic");
		}
		else
		{
			g.drawImage(ic, xI, yI);
		}
	}

	public static void updateKeyTouch()
	{
		try
		{
			if (ic == null)
			{
				ic = loadImage("res/ic");
			}
			else if (Canvas_HAIRMOD_PROTECTED.isPointerHoldIn(xI, yI, ic.getWidth(), ic.getHeight()) && !isPointerDown)
			{
				isPointerDown = true;
				lastX = Canvas_HAIRMOD_PROTECTED.px;
				lastY = Canvas_HAIRMOD_PROTECTED.py;
				Canvas_HAIRMOD_PROTECTED.clearKeyPressed();
			}
			else if (Canvas_HAIRMOD_PROTECTED.isPointerJustRelease)
			{
				isPointerDown = false;
				Canvas_HAIRMOD_PROTECTED.isPointerDown = false;
				if (!Canvas_HAIRMOD_PROTECTED.isPointerHoldIn(xI, yI, ic.getWidth(), ic.getHeight()))
				{
					return;
				}
				ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
				{
					if (scene.name == "test")
					{
						SceneManager.LoadScene("test2");
						Main.tab = TabType.tab2;
					}
					else
					{
						SceneManager.LoadScene("test");
						Main.tab = TabType.tab1;
					}
				};
				Canvas_HAIRMOD_PROTECTED.startYesNoDlg((Main.tab == TabType.tab1) ? "Chuyển Tab 2" : "Chuyển Tab 1", yesAction);
				Canvas_HAIRMOD_PROTECTED.clearKeyPressed();
			}
			else if (Canvas_HAIRMOD_PROTECTED.isMove)
			{
				if (Canvas_HAIRMOD_PROTECTED.isPointerDown && isPointerDown)
				{
					int num = Canvas_HAIRMOD_PROTECTED.px - lastX;
					int num2 = Canvas_HAIRMOD_PROTECTED.py - lastY;
					lastX = Canvas_HAIRMOD_PROTECTED.px;
					lastY = Canvas_HAIRMOD_PROTECTED.py;
					xI += num;
					yI += num2;
					Canvas_HAIRMOD_PROTECTED.clearKeyPressed();
				}
				if (Canvas_HAIRMOD_PROTECTED.isPointerHoldIn(xI, yI, ic.getWidth(), ic.getHeight()) && Canvas_HAIRMOD_PROTECTED.isPointerDown && Canvas_HAIRMOD_PROTECTED.isPointerJustRelease)
				{
					isPointerDown = false;
					Canvas_HAIRMOD_PROTECTED.clearKeyPressed();
				}
			}
		}
		catch (Exception exception)
		{
			Debug.LogException(exception);
		}
	}
}
