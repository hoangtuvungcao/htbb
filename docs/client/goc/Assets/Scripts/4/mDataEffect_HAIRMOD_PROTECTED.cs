using System;
using UnityEngine;

public class mDataEffect_HAIRMOD_PROTECTED
{
	public mVector_HAIRMOD_PROTECTED listFrame = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED listAnima = new mVector_HAIRMOD_PROTECTED();

	public SmallImage_HAIRMOD_PROTECTED[] smallImage;

	public sbyte[] sequence;

	private short FrameWith;

	private short FrameHeight;

	private short idimg;

	public string name = string.Empty;

	public sbyte isFly;

	public sbyte idShadow;

	public static sbyte[][] indexAction = new sbyte[3][]
	{
		new sbyte[14]
		{
			0, 0, 1, 2, 3, 1, 1, 1, 1, 1,
			1, 1, 1, 1
		},
		new sbyte[14]
		{
			4, 4, 5, 6, 7, 5, 5, 5, 5, 5,
			5, 5, 5, 5
		},
		new sbyte[14]
		{
			8, 8, 9, 10, 11, 9, 9, 9, 9, 9,
			9, 9, 9, 9
		}
	};

	public mDataEffect_HAIRMOD_PROTECTED(sbyte[] array, int idimg, bool isMonster)
	{
		this.idimg = (short)idimg;
		setDataType1(array);
	}

	public mDataEffect_HAIRMOD_PROTECTED(sbyte[] array)
	{
		setDataType1(array);
	}

	public void setDataType1(sbyte[] array)
	{
		short num = 0;
		DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(array);
		listFrame.removeAllElements();
		listAnima.removeAllElements();
		try
		{
			int num2 = dataInputStream_HAIRMOD_PROTECTED.readByte();
			smallImage = new SmallImage_HAIRMOD_PROTECTED[num2];
			for (int i = 0; i < num2; i++)
			{
				smallImage[i] = new SmallImage_HAIRMOD_PROTECTED(dataInputStream_HAIRMOD_PROTECTED.readUnsignedByte(), dataInputStream_HAIRMOD_PROTECTED.readUnsignedByte(), dataInputStream_HAIRMOD_PROTECTED.readUnsignedByte(), dataInputStream_HAIRMOD_PROTECTED.readUnsignedByte(), dataInputStream_HAIRMOD_PROTECTED.readUnsignedByte());
			}
			int num3 = 0;
			int num4 = 0;
			int num5 = -1000000;
			int num6 = dataInputStream_HAIRMOD_PROTECTED.readShort();
			for (int j = 0; j < num6; j++)
			{
				sbyte b = dataInputStream_HAIRMOD_PROTECTED.readByte();
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
				for (int k = 0; k < b; k++)
				{
					PartFrame_HAIRMOD_PROTECTED partFrame_HAIRMOD_PROTECTED = new PartFrame_HAIRMOD_PROTECTED(dataInputStream_HAIRMOD_PROTECTED.readShort(), dataInputStream_HAIRMOD_PROTECTED.readShort(), dataInputStream_HAIRMOD_PROTECTED.readByte());
					partFrame_HAIRMOD_PROTECTED.flip = dataInputStream_HAIRMOD_PROTECTED.readByte();
					partFrame_HAIRMOD_PROTECTED.onTop = dataInputStream_HAIRMOD_PROTECTED.readByte();
					mVector_HAIRMOD_PROTECTED2.addElement(partFrame_HAIRMOD_PROTECTED);
					if (j == 0)
					{
						if (num5 < partFrame_HAIRMOD_PROTECTED.dy + smallImage[partFrame_HAIRMOD_PROTECTED.idSmallImg].h)
						{
							num5 = partFrame_HAIRMOD_PROTECTED.dy + smallImage[partFrame_HAIRMOD_PROTECTED.idSmallImg].h;
						}
						if (num3 < Util_HAIRMOD_PROTECTED.abs(partFrame_HAIRMOD_PROTECTED.dy))
						{
							num3 = Util_HAIRMOD_PROTECTED.abs(partFrame_HAIRMOD_PROTECTED.dy);
						}
					}
				}
				if (j == 0 && num5 <= -5)
				{
					isFly = (sbyte)num5;
				}
				listFrame.addElement(new FrameEff_HAIRMOD_PROTECTED(mVector_HAIRMOD_PROTECTED2, null));
			}
			FrameWith = smallImage[0].w;
			FrameHeight = (short)num3;
			num = dataInputStream_HAIRMOD_PROTECTED.readShort();
			sequence = new sbyte[num];
			for (int l = 0; l < num; l++)
			{
				sequence[l] = (sbyte)dataInputStream_HAIRMOD_PROTECTED.readShort();
			}
			num = dataInputStream_HAIRMOD_PROTECTED.readByte();
			sbyte[] data = new sbyte[num];
			dataInputStream_HAIRMOD_PROTECTED.read(ref data);
			Animation_HAIRMOD_PROTECTED o = new Animation_HAIRMOD_PROTECTED(data);
			listAnima.addElement(o);
			num = dataInputStream_HAIRMOD_PROTECTED.readByte();
			data = new sbyte[num];
			dataInputStream_HAIRMOD_PROTECTED.read(ref data);
			o = new Animation_HAIRMOD_PROTECTED(data);
			listAnima.addElement(o);
			num = dataInputStream_HAIRMOD_PROTECTED.readByte();
			data = new sbyte[num];
			dataInputStream_HAIRMOD_PROTECTED.read(ref data);
			o = new Animation_HAIRMOD_PROTECTED(data);
			listAnima.addElement(o);
			num = dataInputStream_HAIRMOD_PROTECTED.readByte();
			data = new sbyte[num];
			dataInputStream_HAIRMOD_PROTECTED.read(ref data);
			o = new Animation_HAIRMOD_PROTECTED(data);
			listAnima.addElement(o);
			num = dataInputStream_HAIRMOD_PROTECTED.readByte();
			data = new sbyte[num];
			dataInputStream_HAIRMOD_PROTECTED.read(ref data);
			o = new Animation_HAIRMOD_PROTECTED(data);
			listAnima.addElement(o);
			num = dataInputStream_HAIRMOD_PROTECTED.readByte();
			data = new sbyte[num];
			dataInputStream_HAIRMOD_PROTECTED.read(ref data);
			o = new Animation_HAIRMOD_PROTECTED(data);
			listAnima.addElement(o);
			num = dataInputStream_HAIRMOD_PROTECTED.readByte();
			data = new sbyte[num];
			dataInputStream_HAIRMOD_PROTECTED.read(ref data);
			o = new Animation_HAIRMOD_PROTECTED(data);
			listAnima.addElement(o);
			num = dataInputStream_HAIRMOD_PROTECTED.readByte();
			data = new sbyte[num];
			dataInputStream_HAIRMOD_PROTECTED.read(ref data);
			o = new Animation_HAIRMOD_PROTECTED(data);
			listAnima.addElement(o);
			if (dataInputStream_HAIRMOD_PROTECTED.available() > 0)
			{
				idShadow = dataInputStream_HAIRMOD_PROTECTED.readByte();
				for (int m = 0; m < num6; m++)
				{
					FrameEff_HAIRMOD_PROTECTED frameEff_HAIRMOD_PROTECTED = (FrameEff_HAIRMOD_PROTECTED)listFrame.elementAt(m);
					frameEff_HAIRMOD_PROTECTED.xShadow = dataInputStream_HAIRMOD_PROTECTED.readByte();
					frameEff_HAIRMOD_PROTECTED.yShadow = dataInputStream_HAIRMOD_PROTECTED.readByte();
				}
			}
			if (dataInputStream_HAIRMOD_PROTECTED.available() > 0)
			{
				num = dataInputStream_HAIRMOD_PROTECTED.readByte();
				data = new sbyte[num];
				dataInputStream_HAIRMOD_PROTECTED.read(ref data);
				o = new Animation_HAIRMOD_PROTECTED(data);
				listAnima.addElement(o);
				num = dataInputStream_HAIRMOD_PROTECTED.readByte();
				data = new sbyte[num];
				dataInputStream_HAIRMOD_PROTECTED.read(ref data);
				o = new Animation_HAIRMOD_PROTECTED(data);
				listAnima.addElement(o);
				num = dataInputStream_HAIRMOD_PROTECTED.readByte();
				data = new sbyte[num];
				dataInputStream_HAIRMOD_PROTECTED.read(ref data);
				o = new Animation_HAIRMOD_PROTECTED(data);
				listAnima.addElement(o);
				num = dataInputStream_HAIRMOD_PROTECTED.readByte();
				data = new sbyte[num];
				dataInputStream_HAIRMOD_PROTECTED.read(ref data);
				o = new Animation_HAIRMOD_PROTECTED(data);
				listAnima.addElement(o);
			}
		}
		catch (Exception)
		{
		}
		finally
		{
			try
			{
				dataInputStream_HAIRMOD_PROTECTED.close();
			}
			catch (Exception)
			{
			}
			try
			{
			}
			catch (Exception)
			{
			}
		}
	}

	public int getIndexAction(int action, int way)
	{
		return indexAction[way][action];
	}

	public Animation_HAIRMOD_PROTECTED getAnim(int action, int way)
	{
		return (Animation_HAIRMOD_PROTECTED)listAnima.elementAt(indexAction[way][action]);
	}

	public int getFrame(int f, int action, int way)
	{
		Animation_HAIRMOD_PROTECTED animation_HAIRMOD_PROTECTED = (Animation_HAIRMOD_PROTECTED)listAnima.elementAt(indexAction[way][action]);
		if (f < animation_HAIRMOD_PROTECTED.frame.Length)
		{
			return animation_HAIRMOD_PROTECTED.frame[f];
		}
		return 0;
	}

	public void paintType2(Graphics g, int idFrame, int x, int y, int way)
	{
	}

	public FrameEff_HAIRMOD_PROTECTED getFrame(int idFrame)
	{
		return (FrameEff_HAIRMOD_PROTECTED)listFrame.elementAt(idFrame);
	}

	public sbyte[] getDxDyShadow(int idFrame)
	{
		FrameEff_HAIRMOD_PROTECTED frameEff_HAIRMOD_PROTECTED = (FrameEff_HAIRMOD_PROTECTED)listFrame.elementAt(idFrame);
		if (frameEff_HAIRMOD_PROTECTED != null)
		{
			return new sbyte[2] { frameEff_HAIRMOD_PROTECTED.xShadow, frameEff_HAIRMOD_PROTECTED.yShadow };
		}
		return new sbyte[2];
	}

	public void paintnoHeight(MyGraphics_HAIRMOD_PROTECTED g, int idFrame, int x, int y, int way, Image_HAIRMOD_PROTECTED img, bool isFly)
	{
		if (img == null)
		{
			return;
		}
		FrameEff_HAIRMOD_PROTECTED frameEff_HAIRMOD_PROTECTED = (FrameEff_HAIRMOD_PROTECTED)listFrame.elementAt(idFrame);
		if (frameEff_HAIRMOD_PROTECTED == null || frameEff_HAIRMOD_PROTECTED.listPartTop == null)
		{
		}
		if (frameEff_HAIRMOD_PROTECTED.listPartTop == null)
		{
			return;
		}
		try
		{
			for (int i = 0; i < frameEff_HAIRMOD_PROTECTED.listPartTop.size(); i++)
			{
				PartFrame_HAIRMOD_PROTECTED partFrame_HAIRMOD_PROTECTED = (PartFrame_HAIRMOD_PROTECTED)frameEff_HAIRMOD_PROTECTED.listPartTop.elementAt(i);
				SmallImage_HAIRMOD_PROTECTED smallImage_HAIRMOD_PROTECTED = smallImage[partFrame_HAIRMOD_PROTECTED.idSmallImg];
				int num = partFrame_HAIRMOD_PROTECTED.dx;
				int num2 = partFrame_HAIRMOD_PROTECTED.dy;
				if (isFly)
				{
					num2 += 35;
				}
				int num3 = smallImage_HAIRMOD_PROTECTED.w;
				int h = smallImage_HAIRMOD_PROTECTED.h;
				int num4 = smallImage_HAIRMOD_PROTECTED.x;
				int num5 = smallImage_HAIRMOD_PROTECTED.y;
				if (num4 > img.getWidth())
				{
					num4 = 0;
				}
				if (num5 > img.getHeight())
				{
					num5 = 0;
				}
				if (num4 + num3 > img.getWidth())
				{
					num3 = img.getWidth() - num4;
				}
				if (way == 2)
				{
					num = -num - num3;
				}
				if (partFrame_HAIRMOD_PROTECTED.flip != 1)
				{
					g.drawRegion(img, num4, num5, num3, h, way, x + num, y + num2, 0);
				}
				else
				{
					g.drawRegion(img, num4, num5, num3, h, (way != 2) ? 2 : 0, x + num, y + num2, 0);
				}
			}
		}
		catch (Exception)
		{
		}
	}

	public void paintPet(MyGraphics_HAIRMOD_PROTECTED g, int idFrame, int x, int y, int way, Image_HAIRMOD_PROTECTED img, int ysai)
	{
		if (img == null)
		{
			return;
		}
		FrameEff_HAIRMOD_PROTECTED frameEff_HAIRMOD_PROTECTED = (FrameEff_HAIRMOD_PROTECTED)listFrame.elementAt(idFrame);
		if (frameEff_HAIRMOD_PROTECTED == null || frameEff_HAIRMOD_PROTECTED.listPartTop == null)
		{
		}
		if (frameEff_HAIRMOD_PROTECTED.listPartTop == null)
		{
			return;
		}
		try
		{
			for (int i = 0; i < frameEff_HAIRMOD_PROTECTED.listPartTop.size(); i++)
			{
				PartFrame_HAIRMOD_PROTECTED partFrame_HAIRMOD_PROTECTED = (PartFrame_HAIRMOD_PROTECTED)frameEff_HAIRMOD_PROTECTED.listPartTop.elementAt(i);
				SmallImage_HAIRMOD_PROTECTED smallImage_HAIRMOD_PROTECTED = smallImage[partFrame_HAIRMOD_PROTECTED.idSmallImg];
				int num = partFrame_HAIRMOD_PROTECTED.dx;
				int num2 = partFrame_HAIRMOD_PROTECTED.dy;
				if (num2 < -40)
				{
					num2 += ysai;
				}
				int num3 = smallImage_HAIRMOD_PROTECTED.w;
				int num4 = smallImage_HAIRMOD_PROTECTED.h;
				int num5 = smallImage_HAIRMOD_PROTECTED.x;
				int num6 = smallImage_HAIRMOD_PROTECTED.y;
				if (num5 > img.getWidth())
				{
					num5 = 0;
				}
				if (num6 > img.getHeight())
				{
					num6 = 0;
				}
				if (num5 + num3 > img.getWidth())
				{
					num3 = img.getWidth() - num5;
				}
				if (num6 + num4 > img.getHeight())
				{
					num4 = img.getHeight() - num6;
				}
				if (way == 2)
				{
					num = -num - num3;
				}
				if (partFrame_HAIRMOD_PROTECTED.flip != 1)
				{
					g.drawRegion(img, num5, num6, num3, num4, way, x + num, y + num2, 0);
				}
				else
				{
					g.drawRegion(img, num5, num6, num3, num4, (way != 2) ? 2 : 0, x + num, y + num2, 0);
				}
			}
		}
		catch (Exception)
		{
		}
	}

	public void paint(MyGraphics_HAIRMOD_PROTECTED g, int idFrame, int x, int y, int way, Image_HAIRMOD_PROTECTED img)
	{
		if (img == null)
		{
			return;
		}
		FrameEff_HAIRMOD_PROTECTED frameEff_HAIRMOD_PROTECTED = (FrameEff_HAIRMOD_PROTECTED)listFrame.elementAt(idFrame);
		if (frameEff_HAIRMOD_PROTECTED == null || frameEff_HAIRMOD_PROTECTED.listPartTop == null)
		{
		}
		if (frameEff_HAIRMOD_PROTECTED.listPartTop == null)
		{
			return;
		}
		try
		{
			for (int i = 0; i < frameEff_HAIRMOD_PROTECTED.listPartTop.size(); i++)
			{
				PartFrame_HAIRMOD_PROTECTED partFrame_HAIRMOD_PROTECTED = (PartFrame_HAIRMOD_PROTECTED)frameEff_HAIRMOD_PROTECTED.listPartTop.elementAt(i);
				SmallImage_HAIRMOD_PROTECTED smallImage_HAIRMOD_PROTECTED = smallImage[partFrame_HAIRMOD_PROTECTED.idSmallImg];
				int num = partFrame_HAIRMOD_PROTECTED.dx;
				int num2 = smallImage_HAIRMOD_PROTECTED.w;
				int num3 = smallImage_HAIRMOD_PROTECTED.h;
				int num4 = smallImage_HAIRMOD_PROTECTED.x;
				int num5 = smallImage_HAIRMOD_PROTECTED.y;
				if (num4 > img.getWidth())
				{
					num4 = 0;
				}
				if (num5 > img.getHeight())
				{
					num5 = 0;
				}
				if (num4 + num2 > img.getWidth())
				{
					num2 = img.getWidth() - num4;
				}
				if (num5 + num3 > img.getHeight())
				{
					num3 = img.getHeight() - num5;
				}
				if (way == 2)
				{
					num = -num - num2;
				}
				if (partFrame_HAIRMOD_PROTECTED.flip != 1)
				{
					g.drawRegion(img, num4, num5, num2, num3, way, x + num, y + partFrame_HAIRMOD_PROTECTED.dy, 0);
				}
				else
				{
					g.drawRegion(img, num4, num5, num2, num3, (way != 2) ? 2 : 0, x + num, y + partFrame_HAIRMOD_PROTECTED.dy, 0);
				}
			}
		}
		catch (Exception)
		{
		}
	}

	public int getPointStart()
	{
		return isFly;
	}

	public short getWith()
	{
		return FrameWith;
	}

	public short getHeight()
	{
		return FrameHeight;
	}
}
