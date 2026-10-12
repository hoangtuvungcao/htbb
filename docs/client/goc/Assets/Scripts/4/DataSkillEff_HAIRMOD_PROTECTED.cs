using System;

public class DataSkillEff_HAIRMOD_PROTECTED
{
	public const sbyte ACTOR_NORMAL = 0;

	public const sbyte ACTOR_CAN_NOT_MOVE = 1;

	public const sbyte ACTOR_CAN_NOT_PAINT = 2;

	public const sbyte NORMAL = 0;

	public const sbyte REMOVE_BY_FRAME = 1;

	public const sbyte REMOVE_BY_TIME = 2;

	public const sbyte NO_REMOVE = 3;

	public bool isMatna;

	public mVector_HAIRMOD_PROTECTED listFrame = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED listAnima = new mVector_HAIRMOD_PROTECTED();

	public SmallImage_HAIRMOD_PROTECTED[] smallImage;

	public sbyte[][] frameChar = new sbyte[4][]
	{
		new sbyte[1],
		new sbyte[1],
		new sbyte[1],
		new sbyte[1]
	};

	public sbyte[] sequence;

	public sbyte Frame;

	public sbyte f;

	public static byte TYPE_EFFECT_END = 0;

	public static byte TYPE_EFFECT_STARTSKILL = 1;

	public static byte TYPE_EFFECT_BUFF = 2;

	public bool wantDetroy;

	public short x;

	public short y;

	public sbyte waitLoop;

	public sbyte indexStartSkill;

	public int idEff;

	private int dxx;

	private int dyy;

	public long timeRemove;

	public int typRequestImg = 112;

	public static MyHashTable_HAIRMOD_PROTECTED ALL_DATA_EFF = new MyHashTable_HAIRMOD_PROTECTED();

	public sbyte Typemove;

	public bool canremove;

	public bool canActorMove;

	public bool canPaintActor;

	public bool canActorFight;

	public sbyte typeupdate;

	public sbyte dyhorse;

	private bool isremovebyTime;

	private sbyte loop;

	private long lasttime;

	public long timelive;

	public DataSkillEff_HAIRMOD_PROTECTED(int id, int x, int y, long timelive, bool canmove, bool canpaint, bool canFight, int loop, sbyte dyhorse)
	{
		this.timelive = timelive;
		idEff = (short)id;
		this.dyhorse = dyhorse;
		switch (timelive)
		{
		case -1L:
			typeupdate = 3;
			break;
		case 0L:
			typeupdate = 1;
			break;
		default:
			typeupdate = 2;
			break;
		}
		canActorMove = canmove;
		canPaintActor = canpaint;
		canActorFight = canFight;
		PartFrame_HAIRMOD_PROTECTED partFrame_HAIRMOD_PROTECTED = (PartFrame_HAIRMOD_PROTECTED)ALL_DATA_EFF.get(id + string.Empty);
		if (partFrame_HAIRMOD_PROTECTED != null)
		{
			loadData(partFrame_HAIRMOD_PROTECTED.data);
		}
	}

	public DataSkillEff_HAIRMOD_PROTECTED(sbyte[] array, short ideff, int dxx, int dyy)
	{
		idEff = ideff;
		this.dxx = dxx;
		this.dyy = dyy;
		loadData(array);
	}

	public DataSkillEff_HAIRMOD_PROTECTED(int id, int x, int y, long timelive)
	{
		this.timelive = timelive;
		idEff = (short)id;
		switch (timelive)
		{
		case -1L:
			typeupdate = 3;
			break;
		case 0L:
			typeupdate = 1;
			break;
		default:
			typeupdate = 2;
			break;
		}
		PartFrame_HAIRMOD_PROTECTED partFrame_HAIRMOD_PROTECTED = (PartFrame_HAIRMOD_PROTECTED)ALL_DATA_EFF.get(id + string.Empty);
		if (partFrame_HAIRMOD_PROTECTED != null)
		{
			loadData(partFrame_HAIRMOD_PROTECTED.data);
		}
	}

	public DataSkillEff_HAIRMOD_PROTECTED(sbyte[] array, short ideff, int dxx, int dyy, long time, sbyte typemove)
	{
		idEff = ideff;
		this.dxx = dxx;
		this.dyy = dyy;
		Typemove = typemove;
		timelive = time;
		isremovebyTime = true;
		loadData(array);
	}

	public DataSkillEff_HAIRMOD_PROTECTED(int id)
	{
		idEff = id;
		PartFrame_HAIRMOD_PROTECTED partFrame_HAIRMOD_PROTECTED = (PartFrame_HAIRMOD_PROTECTED)ALL_DATA_EFF.get(id + string.Empty);
		if (partFrame_HAIRMOD_PROTECTED != null)
		{
			loadData(partFrame_HAIRMOD_PROTECTED.data);
		}
	}

	public void setWaitLoop(sbyte wait)
	{
		waitLoop = wait;
	}

	public void loadData(sbyte[] array)
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
				}
				listFrame.addElement(new FrameEff_HAIRMOD_PROTECTED(mVector_HAIRMOD_PROTECTED2, mVector_HAIRMOD_PROTECTED3));
			}
			short num3 = (short)dataInputStream_HAIRMOD_PROTECTED.readUnsignedByte();
			sequence = new sbyte[num3];
			for (int l = 0; l < num3; l++)
			{
				sequence[l] = (sbyte)dataInputStream_HAIRMOD_PROTECTED.readShort();
			}
			indexStartSkill = dataInputStream_HAIRMOD_PROTECTED.readByte();
			num3 = dataInputStream_HAIRMOD_PROTECTED.readByte();
			frameChar[0] = new sbyte[num3];
			for (int m = 0; m < num3; m++)
			{
				frameChar[0][m] = dataInputStream_HAIRMOD_PROTECTED.readByte();
			}
			num3 = dataInputStream_HAIRMOD_PROTECTED.readByte();
			frameChar[1] = new sbyte[num3];
			for (int n = 0; n < num3; n++)
			{
				frameChar[1][n] = dataInputStream_HAIRMOD_PROTECTED.readByte();
			}
			num3 = dataInputStream_HAIRMOD_PROTECTED.readByte();
			frameChar[3] = new sbyte[num3];
			for (int num4 = 0; num4 < num3; num4++)
			{
				frameChar[3][num4] = dataInputStream_HAIRMOD_PROTECTED.readByte();
			}
		}
		catch (Exception)
		{
		}
	}

	public void paintBottomHorse(MyGraphics_HAIRMOD_PROTECTED g, int x, int y, int Frame, int rotale)
	{
		if (Frame >= listFrame.size())
		{
			return;
		}
		FrameEff_HAIRMOD_PROTECTED frameEff_HAIRMOD_PROTECTED = (FrameEff_HAIRMOD_PROTECTED)listFrame.elementAt(Frame);
		try
		{
			mVector_HAIRMOD_PROTECTED listPartBottom = frameEff_HAIRMOD_PROTECTED.listPartBottom;
			for (int i = 0; i < listPartBottom.size(); i++)
			{
				PartFrame_HAIRMOD_PROTECTED partFrame_HAIRMOD_PROTECTED = (PartFrame_HAIRMOD_PROTECTED)listPartBottom.elementAt(i);
				SmallImage_HAIRMOD_PROTECTED smallImage_HAIRMOD_PROTECTED = smallImage[partFrame_HAIRMOD_PROTECTED.idSmallImg];
				ImageIcon_HAIRMOD_PROTECTED imgIcon = GameData_HAIRMOD_PROTECTED.getImgIcon((short)idEff);
				if (imgIcon != null && imgIcon.img != null)
				{
					int num = partFrame_HAIRMOD_PROTECTED.dx;
					int num2 = smallImage_HAIRMOD_PROTECTED.w;
					int num3 = smallImage_HAIRMOD_PROTECTED.h;
					int num4 = smallImage_HAIRMOD_PROTECTED.x;
					int num5 = smallImage_HAIRMOD_PROTECTED.y;
					int width = imgIcon.img.getWidth();
					int height = imgIcon.img.getHeight();
					if (num4 > width)
					{
						num4 = 0;
					}
					if (num5 > height)
					{
						num5 = 0;
					}
					if (num4 + num2 > width)
					{
						num2 = width - num4;
					}
					if (num5 + num3 > height)
					{
						num3 = height - num5;
					}
					int num6 = ((partFrame_HAIRMOD_PROTECTED.flip == 1) ? 2 : 0);
					if (rotale == 2 || rotale == 6)
					{
						num6 = ((num6 != 2) ? 2 : 0);
						num = -(num + num2);
					}
					g.drawRegion(imgIcon.img, num4, num5, num2, num3, num6, x + num + dxx, y + partFrame_HAIRMOD_PROTECTED.dy + dyy, 0);
				}
			}
		}
		catch (Exception)
		{
		}
	}

	public void paintTopHorse(MyGraphics_HAIRMOD_PROTECTED g, int x, int y, int Frame, int rotale)
	{
		if (Frame >= listFrame.size())
		{
			return;
		}
		FrameEff_HAIRMOD_PROTECTED frameEff_HAIRMOD_PROTECTED = (FrameEff_HAIRMOD_PROTECTED)listFrame.elementAt(Frame);
		try
		{
			mVector_HAIRMOD_PROTECTED listPartTop = frameEff_HAIRMOD_PROTECTED.listPartTop;
			for (int i = 0; i < listPartTop.size(); i++)
			{
				PartFrame_HAIRMOD_PROTECTED partFrame_HAIRMOD_PROTECTED = (PartFrame_HAIRMOD_PROTECTED)listPartTop.elementAt(i);
				SmallImage_HAIRMOD_PROTECTED smallImage_HAIRMOD_PROTECTED = smallImage[partFrame_HAIRMOD_PROTECTED.idSmallImg];
				ImageIcon_HAIRMOD_PROTECTED imgIcon = GameData_HAIRMOD_PROTECTED.getImgIcon((short)idEff);
				if (imgIcon != null && imgIcon.img != null)
				{
					int num = partFrame_HAIRMOD_PROTECTED.dx;
					int num2 = smallImage_HAIRMOD_PROTECTED.w;
					int num3 = smallImage_HAIRMOD_PROTECTED.h;
					int num4 = smallImage_HAIRMOD_PROTECTED.x;
					int num5 = smallImage_HAIRMOD_PROTECTED.y;
					int width = imgIcon.img.getWidth();
					int height = imgIcon.img.getHeight();
					if (num4 > width)
					{
						num4 = 0;
					}
					if (num5 > height)
					{
						num5 = 0;
					}
					if (num4 + num2 > width)
					{
						num2 = width - num4;
					}
					if (num5 + num3 > height)
					{
						num3 = height - num5;
					}
					int num6 = ((partFrame_HAIRMOD_PROTECTED.flip == 1) ? 2 : 0);
					if (rotale == 2 || rotale == 6)
					{
						num6 = ((num6 != 2) ? 2 : 0);
						num = -(num + num2);
					}
					g.drawRegion(imgIcon.img, num4, num5, num2, num3, num6, x + num + dxx, y + partFrame_HAIRMOD_PROTECTED.dy + dyy, 0);
				}
			}
		}
		catch (Exception)
		{
		}
	}

	public void paintTop(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
	{
		if (Frame >= listFrame.size())
		{
			return;
		}
		FrameEff_HAIRMOD_PROTECTED frameEff_HAIRMOD_PROTECTED = (FrameEff_HAIRMOD_PROTECTED)listFrame.elementAt(Frame);
		try
		{
			mVector_HAIRMOD_PROTECTED listPartTop = frameEff_HAIRMOD_PROTECTED.listPartTop;
			listPartTop = frameEff_HAIRMOD_PROTECTED.listPartBottom;
			for (int i = 0; i < listPartTop.size(); i++)
			{
				PartFrame_HAIRMOD_PROTECTED partFrame_HAIRMOD_PROTECTED = (PartFrame_HAIRMOD_PROTECTED)listPartTop.elementAt(i);
				SmallImage_HAIRMOD_PROTECTED smallImage_HAIRMOD_PROTECTED = smallImage[partFrame_HAIRMOD_PROTECTED.idSmallImg];
				ImageIcon_HAIRMOD_PROTECTED imgIcon = GameData_HAIRMOD_PROTECTED.getImgIcon((short)idEff);
				if (imgIcon != null && imgIcon.img != null)
				{
					int dx = partFrame_HAIRMOD_PROTECTED.dx;
					int num = smallImage_HAIRMOD_PROTECTED.w;
					int num2 = smallImage_HAIRMOD_PROTECTED.h;
					int num3 = smallImage_HAIRMOD_PROTECTED.x;
					int num4 = smallImage_HAIRMOD_PROTECTED.y;
					int width = imgIcon.img.getWidth();
					int height = imgIcon.img.getHeight();
					if (num3 > width)
					{
						num3 = 0;
					}
					if (num4 > height)
					{
						num4 = 0;
					}
					if (num3 + num > width)
					{
						num = width - num3;
					}
					if (num4 + num2 > height)
					{
						num2 = height - num4;
					}
					g.drawRegion(imgIcon.img, num3, num4, num, num2, (partFrame_HAIRMOD_PROTECTED.flip == 1) ? 2 : 0, x + dx + dxx, y + partFrame_HAIRMOD_PROTECTED.dy + dyy, 0);
				}
			}
		}
		catch (Exception)
		{
		}
	}

	public void paintBottom(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
	{
		if (Frame >= listFrame.size())
		{
			return;
		}
		FrameEff_HAIRMOD_PROTECTED frameEff_HAIRMOD_PROTECTED = (FrameEff_HAIRMOD_PROTECTED)listFrame.elementAt(Frame);
		try
		{
			mVector_HAIRMOD_PROTECTED listPartTop = frameEff_HAIRMOD_PROTECTED.listPartTop;
			for (int i = 0; i < listPartTop.size(); i++)
			{
				PartFrame_HAIRMOD_PROTECTED partFrame_HAIRMOD_PROTECTED = (PartFrame_HAIRMOD_PROTECTED)listPartTop.elementAt(i);
				SmallImage_HAIRMOD_PROTECTED smallImage_HAIRMOD_PROTECTED = smallImage[partFrame_HAIRMOD_PROTECTED.idSmallImg];
				ImageIcon_HAIRMOD_PROTECTED imgIcon = GameData_HAIRMOD_PROTECTED.getImgIcon((short)idEff);
				if (imgIcon != null && imgIcon.img != null)
				{
					int dx = partFrame_HAIRMOD_PROTECTED.dx;
					int num = smallImage_HAIRMOD_PROTECTED.w;
					int num2 = smallImage_HAIRMOD_PROTECTED.h;
					int num3 = smallImage_HAIRMOD_PROTECTED.x;
					int num4 = smallImage_HAIRMOD_PROTECTED.y;
					int width = imgIcon.img.getWidth();
					int height = imgIcon.img.getHeight();
					if (num3 > width)
					{
						num3 = 0;
					}
					if (num4 > height)
					{
						num4 = 0;
					}
					if (num3 + num > width)
					{
						num = width - num3;
					}
					if (num4 + num2 > height)
					{
						num2 = height - num4;
					}
					g.drawRegion(imgIcon.img, num3, num4, num, num2, (partFrame_HAIRMOD_PROTECTED.flip == 1) ? 2 : 0, x + dx + dxx, y + partFrame_HAIRMOD_PROTECTED.dy + dyy, 0);
				}
			}
		}
		catch (Exception)
		{
		}
	}

	public void paintTop(MyGraphics_HAIRMOD_PROTECTED g, int x, int y, int Frame, int rotale)
	{
		if (Frame >= listFrame.size())
		{
			return;
		}
		FrameEff_HAIRMOD_PROTECTED frameEff_HAIRMOD_PROTECTED = (FrameEff_HAIRMOD_PROTECTED)listFrame.elementAt(Frame);
		try
		{
			mVector_HAIRMOD_PROTECTED listPartTop = frameEff_HAIRMOD_PROTECTED.listPartTop;
			for (int i = 0; i < listPartTop.size(); i++)
			{
				PartFrame_HAIRMOD_PROTECTED partFrame_HAIRMOD_PROTECTED = (PartFrame_HAIRMOD_PROTECTED)listPartTop.elementAt(i);
				SmallImage_HAIRMOD_PROTECTED smallImage_HAIRMOD_PROTECTED = smallImage[partFrame_HAIRMOD_PROTECTED.idSmallImg];
				ImageIcon_HAIRMOD_PROTECTED imgIcon = GameData_HAIRMOD_PROTECTED.getImgIcon((short)idEff);
				if (imgIcon != null && imgIcon.img != null)
				{
					int num = partFrame_HAIRMOD_PROTECTED.dx;
					int num2 = smallImage_HAIRMOD_PROTECTED.w;
					int num3 = smallImage_HAIRMOD_PROTECTED.h;
					int num4 = smallImage_HAIRMOD_PROTECTED.x;
					int num5 = smallImage_HAIRMOD_PROTECTED.y;
					int width = imgIcon.img.getWidth();
					int height = imgIcon.img.getHeight();
					if (num4 > width)
					{
						num4 = 0;
					}
					if (num5 > height)
					{
						num5 = 0;
					}
					if (num4 + num2 > width)
					{
						num2 = width - num4;
					}
					if (num5 + num3 > height)
					{
						num3 = height - num5;
					}
					int num6 = ((partFrame_HAIRMOD_PROTECTED.flip == 1) ? 2 : 0);
					if (rotale == 2 || rotale == 6)
					{
						num6 = ((num6 != 2) ? 2 : 0);
						num = -(num + num2);
					}
					g.drawRegion(imgIcon.img, num4, num5, num2, num3, num6, x + num + dxx, y + partFrame_HAIRMOD_PROTECTED.dy + dyy, 0);
				}
			}
		}
		catch (Exception)
		{
		}
	}

	public void paintBottom(MyGraphics_HAIRMOD_PROTECTED g, int x, int y, int Frame, int rotale)
	{
		if (Frame >= listFrame.size())
		{
			return;
		}
		FrameEff_HAIRMOD_PROTECTED frameEff_HAIRMOD_PROTECTED = (FrameEff_HAIRMOD_PROTECTED)listFrame.elementAt(Frame);
		try
		{
			mVector_HAIRMOD_PROTECTED listPartBottom = frameEff_HAIRMOD_PROTECTED.listPartBottom;
			for (int i = 0; i < listPartBottom.size(); i++)
			{
				PartFrame_HAIRMOD_PROTECTED partFrame_HAIRMOD_PROTECTED = (PartFrame_HAIRMOD_PROTECTED)listPartBottom.elementAt(i);
				SmallImage_HAIRMOD_PROTECTED smallImage_HAIRMOD_PROTECTED = smallImage[partFrame_HAIRMOD_PROTECTED.idSmallImg];
				ImageIcon_HAIRMOD_PROTECTED imgIcon = GameData_HAIRMOD_PROTECTED.getImgIcon((short)idEff);
				if (imgIcon != null && imgIcon.img != null)
				{
					int num = partFrame_HAIRMOD_PROTECTED.dx;
					int num2 = smallImage_HAIRMOD_PROTECTED.w;
					int num3 = smallImage_HAIRMOD_PROTECTED.h;
					int num4 = smallImage_HAIRMOD_PROTECTED.x;
					int num5 = smallImage_HAIRMOD_PROTECTED.y;
					int width = imgIcon.img.getWidth();
					int height = imgIcon.img.getHeight();
					if (num4 > width)
					{
						num4 = 0;
					}
					if (num5 > height)
					{
						num5 = 0;
					}
					if (num4 + num2 > width)
					{
						num2 = width - num4;
					}
					if (num5 + num3 > height)
					{
						num3 = height - num5;
					}
					int num6 = ((partFrame_HAIRMOD_PROTECTED.flip == 1) ? 2 : 0);
					if (rotale == 2 || rotale == 6)
					{
						num6 = ((num6 != 2) ? 2 : 0);
						num = -(num + num2);
					}
					g.drawRegion(imgIcon.img, num4, num5, num2, num3, num6, x + num + dxx, y + partFrame_HAIRMOD_PROTECTED.dy + dyy, 0);
				}
			}
		}
		catch (Exception)
		{
		}
	}

	public bool lockmoveByeffAuto()
	{
		return Typemove == 1;
	}

	public bool CanpaintByeffauto()
	{
		return Typemove == 2;
	}

	public bool isTanghinhbyEffauto()
	{
		return Typemove == 17;
	}

	public void update()
	{
		try
		{
			switch (typeupdate)
			{
			case 0:
			case 1:
				f++;
				if (f < sequence.Length)
				{
					Frame = sequence[f];
				}
				if (f >= sequence.Length)
				{
					f = 0;
					wantDetroy = true;
				}
				break;
			case 2:
				f++;
				if (f > sequence.Length - 1)
				{
					f = 0;
				}
				if (f < sequence.Length)
				{
					Frame = sequence[f];
				}
				if (timelive - mSystem_HAIRMOD_PROTECTED.currentTimeMillis() < 0)
				{
					wantDetroy = true;
				}
				break;
			case 3:
				f++;
				if (f < sequence.Length)
				{
					Frame = sequence[f];
				}
				if (f <= sequence.Length)
				{
					break;
				}
				if (waitLoop > 0)
				{
					if (mSystem_HAIRMOD_PROTECTED.currentTimeMillis() - lasttime > waitLoop * 1000)
					{
						f = 0;
						lasttime = mSystem_HAIRMOD_PROTECTED.currentTimeMillis();
					}
				}
				else
				{
					f = 0;
				}
				break;
			}
		}
		catch (Exception)
		{
		}
	}
}
