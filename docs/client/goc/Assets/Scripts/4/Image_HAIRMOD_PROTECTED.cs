using System;
using System.Threading;
using UnityEngine;

public class Image_HAIRMOD_PROTECTED
{
	private const int INTERVAL = 5;

	private const int MAXTIME = 500;

	public Texture2D texture = new Texture2D(1, 1);

	public static Image_HAIRMOD_PROTECTED imgTemp;

	public static string filenametemp;

	public static byte[] datatemp;

	public static Image_HAIRMOD_PROTECTED imgSrcTemp;

	public static int xtemp;

	public static int ytemp;

	public static int wtemp;

	public static int htemp;

	public static int transformtemp;

	public int w;

	public int h;

	public static int status;

	public static Image_HAIRMOD_PROTECTED createEmptyImage()
	{
		return __createEmptyImage();
	}

	public static Image_HAIRMOD_PROTECTED createImage(string filename)
	{
		return __createImage(filename);
	}

	public static Image_HAIRMOD_PROTECTED createImage(byte[] imageData)
	{
		return __createImage(imageData);
	}

	public static Image_HAIRMOD_PROTECTED createImage(Image_HAIRMOD_PROTECTED src, int x, int y, int w, int h, int transform)
	{
		return __createImage(src, x, y, w, h, transform);
	}

	public static Image_HAIRMOD_PROTECTED createImage(int w, int h)
	{
		return __createImage(w, h);
	}

	public static void update()
	{
		if (status == 2)
		{
			status = 1;
			imgTemp = __createEmptyImage();
			status = 0;
		}
		else if (status == 3)
		{
			status = 1;
			imgTemp = __createImage(filenametemp);
			status = 0;
		}
		else if (status == 4)
		{
			status = 1;
			imgTemp = __createImage(datatemp);
			status = 0;
		}
		else if (status == 5)
		{
			status = 1;
			imgTemp = __createImage(imgSrcTemp, xtemp, ytemp, wtemp, htemp, transformtemp);
			status = 0;
		}
		else if (status == 6)
		{
			status = 1;
			imgTemp = __createImage(wtemp, htemp);
			status = 0;
		}
	}

	private static Image_HAIRMOD_PROTECTED _createEmptyImage()
	{
		if (status != 0)
		{
			Debug.LogError("CANNOT CREATE EMPTY IMAGE WHEN CREATING OTHER IMAGE");
			return null;
		}
		imgTemp = null;
		status = 2;
		int i;
		for (i = 0; i < 500; i++)
		{
			Thread.Sleep(5);
			if (status == 0)
			{
				break;
			}
		}
		if (i == 500)
		{
			Debug.LogError("TOO LONG FOR CREATE EMPTY IMAGE");
			status = 0;
		}
		return imgTemp;
	}

	private static Image_HAIRMOD_PROTECTED _createImage(string filename)
	{
		if (status != 0)
		{
			Debug.LogError("CANNOT CREATE IMAGE " + filename + " WHEN CREATING OTHER IMAGE");
			return null;
		}
		imgTemp = null;
		filenametemp = filename;
		status = 3;
		int i;
		for (i = 0; i < 500; i++)
		{
			Thread.Sleep(5);
			if (status == 0)
			{
				break;
			}
		}
		if (i == 500)
		{
			Debug.LogError("TOO LONG FOR CREATE IMAGE " + filename);
			status = 0;
		}
		return imgTemp;
	}

	private static Image_HAIRMOD_PROTECTED _createImage(byte[] imageData)
	{
		if (status != 0)
		{
			Debug.LogError("CANNOT CREATE IMAGE(FromArray) WHEN CREATING OTHER IMAGE");
			return null;
		}
		imgTemp = null;
		datatemp = imageData;
		status = 4;
		int i;
		for (i = 0; i < 500; i++)
		{
			Thread.Sleep(5);
			if (status == 0)
			{
				break;
			}
		}
		if (i == 500)
		{
			Debug.LogError("TOO LONG FOR CREATE IMAGE(FromArray)");
			status = 0;
		}
		return imgTemp;
	}

	private static Image_HAIRMOD_PROTECTED _createImage(Image_HAIRMOD_PROTECTED src, int x, int y, int w, int h, int transform)
	{
		if (status != 0)
		{
			Debug.LogError("CANNOT CREATE IMAGE(FromSrcPart) WHEN CREATING OTHER IMAGE");
			return null;
		}
		imgTemp = null;
		imgSrcTemp = src;
		xtemp = x;
		ytemp = y;
		wtemp = w;
		htemp = h;
		transformtemp = transform;
		status = 5;
		int i;
		for (i = 0; i < 500; i++)
		{
			Thread.Sleep(5);
			if (status == 0)
			{
				break;
			}
		}
		if (i == 500)
		{
			Debug.LogError("TOO LONG FOR CREATE IMAGE(FromSrcPart)");
			status = 0;
		}
		return imgTemp;
	}

	private static Image_HAIRMOD_PROTECTED _createImage(int w, int h)
	{
		if (status != 0)
		{
			Debug.LogError("CANNOT CREATE IMAGE(w,h) WHEN CREATING OTHER IMAGE");
			return null;
		}
		imgTemp = null;
		wtemp = w;
		htemp = h;
		status = 6;
		int i;
		for (i = 0; i < 500; i++)
		{
			Thread.Sleep(5);
			if (status == 0)
			{
				break;
			}
		}
		if (i == 500)
		{
			Debug.LogError("TOO LONG FOR CREATE IMAGE(w,h)");
			status = 0;
		}
		return imgTemp;
	}

	private static Image_HAIRMOD_PROTECTED __createImage(string filename)
	{
		Image_HAIRMOD_PROTECTED image_HAIRMOD_PROTECTED = new Image_HAIRMOD_PROTECTED();
		TextAsset textAsset = (TextAsset)Resources.Load(filename, typeof(TextAsset));
		if (textAsset == null || textAsset.bytes == null || textAsset.bytes.Length == 0)
		{
			Debug.LogError("Create Image " + filename + " fail");
			return null;
		}
		image_HAIRMOD_PROTECTED.texture.LoadImage(textAsset.bytes);
		image_HAIRMOD_PROTECTED.w = image_HAIRMOD_PROTECTED.texture.width;
		image_HAIRMOD_PROTECTED.h = image_HAIRMOD_PROTECTED.texture.height;
		setTextureQuality(image_HAIRMOD_PROTECTED);
		return image_HAIRMOD_PROTECTED;
	}

	public static Image_HAIRMOD_PROTECTED __createImage(byte[] imageData)
	{
		if (imageData == null || imageData.Length == 0)
		{
			Debug.LogError("Create Image from byte array fail");
			return null;
		}
		Image_HAIRMOD_PROTECTED image_HAIRMOD_PROTECTED = new Image_HAIRMOD_PROTECTED();
		try
		{
			image_HAIRMOD_PROTECTED.texture.LoadImage(imageData);
			image_HAIRMOD_PROTECTED.w = image_HAIRMOD_PROTECTED.texture.width;
			image_HAIRMOD_PROTECTED.h = image_HAIRMOD_PROTECTED.texture.height;
			setTextureQuality(image_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
			Debug.LogError("CREAT IMAGE FROM ARRAY FAIL \n" + Environment.StackTrace);
		}
		return image_HAIRMOD_PROTECTED;
	}

	private static Image_HAIRMOD_PROTECTED __createImage(Image_HAIRMOD_PROTECTED src, int x, int y, int w, int h, int transform)
	{
		Image_HAIRMOD_PROTECTED image_HAIRMOD_PROTECTED = new Image_HAIRMOD_PROTECTED();
		image_HAIRMOD_PROTECTED.texture = new Texture2D(w, h);
		y = src.texture.height - y - h;
		for (int i = 0; i < w; i++)
		{
			for (int j = 0; j < h; j++)
			{
				int num = i;
				if (transform == 2)
				{
					num = w - i;
				}
				int num2 = j;
				image_HAIRMOD_PROTECTED.texture.SetPixel(i, j, src.texture.GetPixel(x + num, y + num2));
			}
		}
		image_HAIRMOD_PROTECTED.texture.Apply();
		image_HAIRMOD_PROTECTED.w = image_HAIRMOD_PROTECTED.texture.width;
		image_HAIRMOD_PROTECTED.h = image_HAIRMOD_PROTECTED.texture.height;
		setTextureQuality(image_HAIRMOD_PROTECTED);
		return image_HAIRMOD_PROTECTED;
	}

	private static Image_HAIRMOD_PROTECTED __createEmptyImage()
	{
		return new Image_HAIRMOD_PROTECTED();
	}

	public static Image_HAIRMOD_PROTECTED __createImage(int w, int h)
	{
		Image_HAIRMOD_PROTECTED image_HAIRMOD_PROTECTED = new Image_HAIRMOD_PROTECTED();
		image_HAIRMOD_PROTECTED.texture = new Texture2D(w, h);
		image_HAIRMOD_PROTECTED.w = w;
		image_HAIRMOD_PROTECTED.h = h;
		setTextureQuality(image_HAIRMOD_PROTECTED);
		return image_HAIRMOD_PROTECTED;
	}

	public static Image_HAIRMOD_PROTECTED creatImageByImage(Image_HAIRMOD_PROTECTED i)
	{
		int width = i.getWidth();
		int height = i.getHeight();
		Image_HAIRMOD_PROTECTED image_HAIRMOD_PROTECTED = new Image_HAIRMOD_PROTECTED();
		image_HAIRMOD_PROTECTED.texture = new Texture2D(width, height);
		return image_HAIRMOD_PROTECTED;
	}

	public static byte convertSbyteToByte(sbyte var)
	{
		if (var > 0)
		{
			return (byte)var;
		}
		return (byte)(var + 256);
	}

	public static byte[] convertArrSbyteToArrByte(sbyte[] var)
	{
		byte[] array = new byte[var.Length];
		for (int i = 0; i < var.Length; i++)
		{
			if (var[i] > 0)
			{
				array[i] = (byte)var[i];
			}
			else
			{
				array[i] = (byte)(var[i] + 256);
			}
		}
		return array;
	}

	public static Image_HAIRMOD_PROTECTED createImage(sbyte[] imageData, int offset, int lenght)
	{
		if (offset + lenght > imageData.Length)
		{
			return null;
		}
		sbyte[] array = new sbyte[lenght];
		for (int i = 0; i < lenght; i++)
		{
			array[i] = imageData[i + offset];
		}
		byte[] imageData2 = convertArrSbyteToArrByte(array);
		return createImage(imageData2);
	}

	public int getWidth()
	{
		return w;
	}

	public int getHeight()
	{
		return h;
	}

	private static void setTextureQuality(Image_HAIRMOD_PROTECTED img)
	{
		setTextureQuality(img.texture);
	}

	private static void setTextureQuality(Texture2D texture)
	{
		texture.anisoLevel = 0;
		texture.filterMode = FilterMode.Point;
		texture.mipMapBias = 0f;
		texture.wrapMode = TextureWrapMode.Clamp;
	}
}
