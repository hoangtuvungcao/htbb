using System;
using UnityEngine;

public class MyImage_HAIRMOD_PROTECTED
{
	public Image_HAIRMOD_PROTECTED me;

	public int x;

	public int y;

	public int w;

	public int h;

	public int sumH;

	public int sumC;

	public SonImage_HAIRMOD_PROTECTED[] arrImg2;

	public SonImage_HAIRMOD_PROTECTED[][] arrImg;

	public int[][] xTex;

	public int[][] yTex;

	public Image_HAIRMOD_PROTECTED[][] imgScr;

	public Image_HAIRMOD_PROTECTED emty;

	public MyImage_HAIRMOD_PROTECTED()
	{
	}

	public MyImage_HAIRMOD_PROTECTED(int w, int h)
	{
	}

	public void setInfo(int w0, int h0)
	{
		w = w0;
		h = h0;
		me = Image_HAIRMOD_PROTECTED.createImage(w, h);
		int num = w / 16;
		int num2 = h / 16;
		xTex = new int[num][];
		yTex = new int[num][];
		for (int i = 0; i < num; i++)
		{
			xTex[i] = new int[num2];
			yTex[i] = new int[num2];
		}
		imgScr = new Image_HAIRMOD_PROTECTED[num][];
		for (int j = 0; j < num; j++)
		{
			imgScr[j] = new Image_HAIRMOD_PROTECTED[num2];
		}
		for (int k = 0; k < num; k++)
		{
			for (int l = 0; l < num2; l++)
			{
				imgScr[k][l] = new Image_HAIRMOD_PROTECTED();
				xTex[k][l] = -1;
				yTex[k][l] = -1;
			}
		}
		emty = Image_HAIRMOD_PROTECTED.createImage(16, 16);
	}

	public void getImg(int x, int y, Image_HAIRMOD_PROTECTED scr, int indexa, int indexb)
	{
		if (scr != null)
		{
			imgScr[indexa][indexb] = scr;
		}
		else
		{
			imgScr[indexa][indexb] = emty;
		}
		xTex[indexa][indexb] = x;
		yTex[indexa][indexb] = y;
	}

	public void getTexture()
	{
		int num = w / 16;
		int num2 = h / 16;
		try
		{
			for (int i = 0; i < num; i++)
			{
				for (int j = 0; j < num2; j++)
				{
					me.texture.SetPixels(xTex[i][j], yTex[i][j], 16, 16, imgScr[i][j].texture.GetPixels());
				}
			}
		}
		catch (Exception ex)
		{
			Debug.LogError(" loi roi" + ex.StackTrace);
		}
		me.texture.Apply();
	}
}
