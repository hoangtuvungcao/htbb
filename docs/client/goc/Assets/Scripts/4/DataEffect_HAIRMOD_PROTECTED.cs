using System;

public class DataEffect_HAIRMOD_PROTECTED
{
	public mVector_HAIRMOD_PROTECTED listFrame = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED listAnima = new mVector_HAIRMOD_PROTECTED();

	public SmallImage_HAIRMOD_PROTECTED[] smallImage;

	public sbyte[] sequence;

	public static sbyte[] indexAction = new sbyte[9] { 0, 0, 1, 2, 3, 1, 1, 1, 1 };

	public DataEffect_HAIRMOD_PROTECTED(sbyte[] array)
	{
		try
		{
			DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(array);
			int num = dataInputStream_HAIRMOD_PROTECTED.readByte();
			smallImage = new SmallImage_HAIRMOD_PROTECTED[num];
			for (int i = 0; i < num; i++)
			{
				smallImage[i] = new SmallImage_HAIRMOD_PROTECTED(dataInputStream_HAIRMOD_PROTECTED.readUnsignedByte(), dataInputStream_HAIRMOD_PROTECTED.readUnsignedByte(), dataInputStream_HAIRMOD_PROTECTED.readUnsignedByte(), dataInputStream_HAIRMOD_PROTECTED.readUnsignedByte(), dataInputStream_HAIRMOD_PROTECTED.readUnsignedByte());
			}
			int num2 = dataInputStream_HAIRMOD_PROTECTED.readShort();
			for (int j = 0; j < num2; j++)
			{
				sbyte b = dataInputStream_HAIRMOD_PROTECTED.readByte();
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
				for (int k = 0; k < b; k++)
				{
					PartFrame_HAIRMOD_PROTECTED o = new PartFrame_HAIRMOD_PROTECTED(dataInputStream_HAIRMOD_PROTECTED.readShort(), dataInputStream_HAIRMOD_PROTECTED.readShort(), dataInputStream_HAIRMOD_PROTECTED.readByte());
					mVector_HAIRMOD_PROTECTED2.addElement(o);
				}
				listFrame.addElement(new FrameEff_HAIRMOD_PROTECTED(mVector_HAIRMOD_PROTECTED2));
			}
			short num3 = dataInputStream_HAIRMOD_PROTECTED.readShort();
			sequence = new sbyte[num3];
			for (int l = 0; l < num3; l++)
			{
				sequence[l] = (sbyte)dataInputStream_HAIRMOD_PROTECTED.readShort();
			}
			sbyte b2 = dataInputStream_HAIRMOD_PROTECTED.readByte();
			sbyte b3 = dataInputStream_HAIRMOD_PROTECTED.readByte();
			num3 = dataInputStream_HAIRMOD_PROTECTED.readByte();
			sbyte[] data = new sbyte[num3];
			dataInputStream_HAIRMOD_PROTECTED.read(ref data);
			Animation_HAIRMOD_PROTECTED o2 = new Animation_HAIRMOD_PROTECTED(data);
			listAnima.addElement(o2);
			num3 = dataInputStream_HAIRMOD_PROTECTED.readByte();
			data = new sbyte[num3];
			dataInputStream_HAIRMOD_PROTECTED.read(ref data);
			o2 = new Animation_HAIRMOD_PROTECTED(data);
			listAnima.addElement(o2);
			num3 = dataInputStream_HAIRMOD_PROTECTED.readByte();
			data = new sbyte[num3];
			dataInputStream_HAIRMOD_PROTECTED.read(ref data);
			o2 = new Animation_HAIRMOD_PROTECTED(data);
			listAnima.addElement(o2);
			num3 = dataInputStream_HAIRMOD_PROTECTED.readByte();
			data = new sbyte[num3];
			dataInputStream_HAIRMOD_PROTECTED.read(ref data);
			o2 = new Animation_HAIRMOD_PROTECTED(data);
			listAnima.addElement(o2);
		}
		catch (Exception)
		{
		}
	}

	public Animation_HAIRMOD_PROTECTED getAnim(int action)
	{
		return (Animation_HAIRMOD_PROTECTED)listAnima.elementAt(indexAction[action]);
	}

	public int getFrame(int f, int action)
	{
		Animation_HAIRMOD_PROTECTED animation_HAIRMOD_PROTECTED = (Animation_HAIRMOD_PROTECTED)listAnima.elementAt(indexAction[action]);
		if (f < animation_HAIRMOD_PROTECTED.frame.Length)
		{
			return animation_HAIRMOD_PROTECTED.frame[f];
		}
		return 0;
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
			for (int i = 0; i < frameEff_HAIRMOD_PROTECTED.listPart.size(); i++)
			{
				PartFrame_HAIRMOD_PROTECTED partFrame_HAIRMOD_PROTECTED = (PartFrame_HAIRMOD_PROTECTED)frameEff_HAIRMOD_PROTECTED.listPart.elementAt(i);
				SmallImage_HAIRMOD_PROTECTED smallImage_HAIRMOD_PROTECTED = smallImage[partFrame_HAIRMOD_PROTECTED.idSmallImg];
				int num = partFrame_HAIRMOD_PROTECTED.dx;
				if (way == 2)
				{
					num = -num - smallImage_HAIRMOD_PROTECTED.w;
				}
				g.drawRegion(img, smallImage_HAIRMOD_PROTECTED.x, smallImage_HAIRMOD_PROTECTED.y, smallImage_HAIRMOD_PROTECTED.w, smallImage_HAIRMOD_PROTECTED.h, way, x + num, y + partFrame_HAIRMOD_PROTECTED.dy, 0);
			}
		}
		catch (Exception)
		{
		}
	}
}
