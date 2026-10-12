using System;

public class EffectData_HAIRMOD_PROTECTED
{
	public sbyte[] arrFrame;

	public ImageInfo_HAIRMOD_PROTECTED[] imgImfo;

	public Image_HAIRMOD_PROTECTED img;

	public Frame_HAIRMOD_PROTECTED[] frame;

	public short ID;

	public void setInfo(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			short num = msg.reader().readShort();
			sbyte[] data = new sbyte[num];
			msg.reader().read(ref data);
			img = Res_HAIRMOD_PROTECTED.createImgByByteArray(data);
			sbyte b = msg.reader().readByte();
			imgImfo = new ImageInfo_HAIRMOD_PROTECTED[b];
			for (int i = 0; i < b; i++)
			{
				imgImfo[i] = new ImageInfo_HAIRMOD_PROTECTED();
				imgImfo[i].ID = msg.reader().readByte();
				imgImfo[i].x0 = msg.reader().readByte();
				imgImfo[i].y0 = msg.reader().readByte();
				imgImfo[i].w = msg.reader().readByte();
				imgImfo[i].h = msg.reader().readByte();
			}
			sbyte b2 = msg.reader().readByte();
			frame = new Frame_HAIRMOD_PROTECTED[b2];
			for (int j = 0; j < b2; j++)
			{
				frame[j] = new Frame_HAIRMOD_PROTECTED();
				sbyte b3 = msg.reader().readByte();
				frame[j].dx = new sbyte[b3];
				frame[j].dy = new sbyte[b3];
				frame[j].idImg = new sbyte[b3];
				for (int k = 0; k < b3; k++)
				{
					frame[j].dx[k] = msg.reader().readByte();
					frame[j].dy[k] = msg.reader().readByte();
					frame[j].idImg[k] = msg.reader().readByte();
				}
			}
			sbyte b4 = msg.reader().readByte();
			arrFrame = new sbyte[b4];
			for (int l = 0; l < b4; l++)
			{
				arrFrame[l] = msg.reader().readByte();
			}
		}
		catch (Exception)
		{
		}
	}

	public ImageInfo_HAIRMOD_PROTECTED getImgInfo(sbyte id)
	{
		for (int i = 0; i < imgImfo.Length; i++)
		{
			if (imgImfo[i].ID == id)
			{
				return imgImfo[i];
			}
		}
		return null;
	}

	public void paint(MyGraphics_HAIRMOD_PROTECTED g, int x, int y, int index)
	{
		Frame_HAIRMOD_PROTECTED frame_HAIRMOD_PROTECTED = frame[arrFrame[index]];
		for (int i = 0; i < frame_HAIRMOD_PROTECTED.dx.Length; i++)
		{
			ImageInfo_HAIRMOD_PROTECTED imgInfo = getImgInfo(frame_HAIRMOD_PROTECTED.idImg[i]);
			g.drawRegion(img, imgInfo.x0, imgInfo.y0, imgInfo.w, imgInfo.h, 0, x + frame_HAIRMOD_PROTECTED.dx[i], y + frame_HAIRMOD_PROTECTED.dy[i], 0);
		}
	}
}
