using System;

public class DataEffectThanThu_HAIRMOD_PROTECTED
{
	public mVector_HAIRMOD_PROTECTED listFrame = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED listAnima = new mVector_HAIRMOD_PROTECTED();

	public SmallImage_HAIRMOD_PROTECTED[] smallImage;

	public sbyte[] sequence;

	private short FrameWith;

	private short FrameHeight;

	private short idimg;

	public string name = string.Empty;

	public static sbyte[][] indexAction = new sbyte[2][]
	{
		new sbyte[13]
		{
			0, 0, 1, 2, 3, 1, 1, 1, 1, 1,
			1, 1, 1
		},
		new sbyte[13]
		{
			4, 4, 5, 6, 7, 5, 5, 5, 5, 5,
			5, 5, 5
		}
	};

	public DataEffectThanThu_HAIRMOD_PROTECTED(sbyte[] array, int idimg)
	{
		this.idimg = (short)idimg;
		setDataType1(array);
	}

	public DataEffectThanThu_HAIRMOD_PROTECTED(sbyte[] array)
	{
		setDataType1(array);
	}

	public void setDataType1(sbyte[] array)
	{
		short num = 0;
		try
		{
			DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(array);
			int num2 = dataInputStream_HAIRMOD_PROTECTED.readByte();
			smallImage = new SmallImage_HAIRMOD_PROTECTED[num2];
			for (int i = 0; i < num2; i++)
			{
				smallImage[i] = new SmallImage_HAIRMOD_PROTECTED(dataInputStream_HAIRMOD_PROTECTED.readUnsignedByte(), dataInputStream_HAIRMOD_PROTECTED.readUnsignedByte(), dataInputStream_HAIRMOD_PROTECTED.readUnsignedByte(), dataInputStream_HAIRMOD_PROTECTED.readUnsignedByte(), dataInputStream_HAIRMOD_PROTECTED.readUnsignedByte());
			}
			int num3 = 0;
			int num4 = dataInputStream_HAIRMOD_PROTECTED.readShort();
			for (int j = 0; j < num4; j++)
			{
				sbyte b = dataInputStream_HAIRMOD_PROTECTED.readByte();
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED3 = new mVector_HAIRMOD_PROTECTED();
				for (int k = 0; k < b; k++)
				{
					PartFrame_HAIRMOD_PROTECTED partFrame_HAIRMOD_PROTECTED = new PartFrame_HAIRMOD_PROTECTED(dataInputStream_HAIRMOD_PROTECTED.readShort(), dataInputStream_HAIRMOD_PROTECTED.readShort(), dataInputStream_HAIRMOD_PROTECTED.readByte());
					partFrame_HAIRMOD_PROTECTED.flip = dataInputStream_HAIRMOD_PROTECTED.readByte();
					partFrame_HAIRMOD_PROTECTED.onTop = dataInputStream_HAIRMOD_PROTECTED.readByte();
					if (partFrame_HAIRMOD_PROTECTED.onTop == 0)
					{
						mVector_HAIRMOD_PROTECTED2.addElement(partFrame_HAIRMOD_PROTECTED);
					}
					else
					{
						mVector_HAIRMOD_PROTECTED3.addElement(partFrame_HAIRMOD_PROTECTED);
					}
					if (num3 < Canvas_HAIRMOD_PROTECTED.abs(partFrame_HAIRMOD_PROTECTED.dy))
					{
						num3 = Canvas_HAIRMOD_PROTECTED.abs(partFrame_HAIRMOD_PROTECTED.dy);
					}
				}
				listFrame.addElement(new FrameEff_HAIRMOD_PROTECTED(mVector_HAIRMOD_PROTECTED2, mVector_HAIRMOD_PROTECTED3));
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
			sbyte[] array2 = new sbyte[num];
			for (int m = 0; m < num; m++)
			{
				array2[m] = dataInputStream_HAIRMOD_PROTECTED.readByte();
			}
			Animation_HAIRMOD_PROTECTED o = new Animation_HAIRMOD_PROTECTED(array2);
			listAnima.addElement(o);
			num = dataInputStream_HAIRMOD_PROTECTED.readByte();
			array2 = new sbyte[num];
			for (int n = 0; n < num; n++)
			{
				array2[n] = dataInputStream_HAIRMOD_PROTECTED.readByte();
			}
			o = new Animation_HAIRMOD_PROTECTED(array2);
			listAnima.addElement(o);
			num = dataInputStream_HAIRMOD_PROTECTED.readByte();
			array2 = new sbyte[num];
			for (int num5 = 0; num5 < num; num5++)
			{
				array2[num5] = dataInputStream_HAIRMOD_PROTECTED.readByte();
			}
			o = new Animation_HAIRMOD_PROTECTED(array2);
			listAnima.addElement(o);
			num = dataInputStream_HAIRMOD_PROTECTED.readByte();
			array2 = new sbyte[num];
			for (int num6 = 0; num6 < num; num6++)
			{
				array2[num6] = dataInputStream_HAIRMOD_PROTECTED.readByte();
			}
			o = new Animation_HAIRMOD_PROTECTED(array2);
			listAnima.addElement(o);
			num = dataInputStream_HAIRMOD_PROTECTED.readByte();
			array2 = new sbyte[num];
			for (int num7 = 0; num7 < num; num7++)
			{
				array2[num7] = dataInputStream_HAIRMOD_PROTECTED.readByte();
			}
			o = new Animation_HAIRMOD_PROTECTED(array2);
			listAnima.addElement(o);
			num = dataInputStream_HAIRMOD_PROTECTED.readByte();
			array2 = new sbyte[num];
			for (int num8 = 0; num8 < num; num8++)
			{
				array2[num8] = dataInputStream_HAIRMOD_PROTECTED.readByte();
			}
			o = new Animation_HAIRMOD_PROTECTED(array2);
			listAnima.addElement(o);
			num = dataInputStream_HAIRMOD_PROTECTED.readByte();
			array2 = new sbyte[num];
			for (int num9 = 0; num9 < num; num9++)
			{
				array2[num9] = dataInputStream_HAIRMOD_PROTECTED.readByte();
			}
			o = new Animation_HAIRMOD_PROTECTED(array2);
			listAnima.addElement(o);
			num = dataInputStream_HAIRMOD_PROTECTED.readByte();
			array2 = new sbyte[num];
			for (int num10 = 0; num10 < num; num10++)
			{
				array2[num10] = dataInputStream_HAIRMOD_PROTECTED.readByte();
			}
			o = new Animation_HAIRMOD_PROTECTED(array2);
			listAnima.addElement(o);
		}
		catch (Exception)
		{
		}
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

	public void paintType2(MyGraphics_HAIRMOD_PROTECTED g, int idFrame, int x, int y, int way)
	{
	}

	public void paint(MyGraphics_HAIRMOD_PROTECTED g, int idFrame, int x, int y, int way, Image_HAIRMOD_PROTECTED img)
	{
		if (img == null)
		{
			return;
		}
		FrameEff_HAIRMOD_PROTECTED frameEff_HAIRMOD_PROTECTED = (FrameEff_HAIRMOD_PROTECTED)listFrame.elementAt(idFrame);
		try
		{
			mVector_HAIRMOD_PROTECTED listPartPaint = frameEff_HAIRMOD_PROTECTED.getListPartPaint();
			for (int i = 0; i < listPartPaint.size(); i++)
			{
				PartFrame_HAIRMOD_PROTECTED partFrame_HAIRMOD_PROTECTED = (PartFrame_HAIRMOD_PROTECTED)listPartPaint.elementAt(i);
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

	public short getWith()
	{
		return FrameWith;
	}

	public short getHeight()
	{
		return FrameHeight;
	}
}
