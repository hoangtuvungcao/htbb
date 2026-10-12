using System;

public class ThanThu_HAIRMOD_PROTECTED : Char_HAIRMOD_PROTECTED
{
	public const sbyte STAND = 0;

	public const sbyte DEAD = 1;

	public const sbyte WALK = 2;

	public const sbyte ATTACK = 3;

	public const sbyte INJURE = 4;

	public const sbyte DEADFLY = 5;

	public static MyHashTable_HAIRMOD_PROTECTED ALL_DATA_THAN_THU = new MyHashTable_HAIRMOD_PROTECTED();

	public sbyte idThanThu;

	public mVector_HAIRMOD_PROTECTED data = new mVector_HAIRMOD_PROTECTED();

	private new short p1;

	private new short p2;

	private new short p3;

	private short lastDir = -100;

	private short vx;

	private short vy;

	private sbyte huongY;

	private sbyte flip;

	private new short xTo;

	private new short yTo;

	public ThanThu_HAIRMOD_PROTECTED()
	{
		catagory = 0;
		speed = 3;
		centerX = 12;
		centerY = 12;
		height = 55;
		width = 40;
		hp = 1;
		dyThanthu = 15;
		realHP = 1;
		dir = (short)LiveActor_HAIRMOD_PROTECTED.r.nextInt(4);
		isDie = false;
	}

	public override int getIdThanThu()
	{
		return idThanThu;
	}

	public static void setDataThanThu(int id, sbyte[] array)
	{
		try
		{
			try
			{
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED3 = (mVector_HAIRMOD_PROTECTED)ALL_DATA_THAN_THU.get(string.Empty + id);
				if (mVector_HAIRMOD_PROTECTED3 != null)
				{
					mVector_HAIRMOD_PROTECTED2 = mVector_HAIRMOD_PROTECTED3;
					return;
				}
				DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(array);
				if (dataInputStream_HAIRMOD_PROTECTED == null)
				{
					return;
				}
				sbyte b = 1;
				for (int i = 0; i < b; i++)
				{
					short num = (short)dataInputStream_HAIRMOD_PROTECTED.available();
					sbyte[] array2 = new sbyte[num];
					for (int j = 0; j < array2.Length; j++)
					{
						array2[j] = dataInputStream_HAIRMOD_PROTECTED.readByte();
					}
					mVector_HAIRMOD_PROTECTED2.addElement(new DataEffectThanThu_HAIRMOD_PROTECTED(array2));
				}
				ALL_DATA_THAN_THU.put(string.Empty + id, mVector_HAIRMOD_PROTECTED2);
				dataInputStream_HAIRMOD_PROTECTED.close();
			}
			catch (Exception)
			{
			}
		}
		catch (Exception)
		{
		}
	}

	public override void setIdThanThu(int id)
	{
		idThanThu = (sbyte)id;
		try
		{
			data = (mVector_HAIRMOD_PROTECTED)ALL_DATA_THAN_THU.get(string.Empty + id);
		}
		catch (Exception)
		{
		}
	}

	public override void setPosTo(short x, short y)
	{
		moveTo(x, y);
	}

	public override void moveTo(short xTo, short yTo)
	{
		this.xTo = xTo;
		this.yTo = yTo;
		if (x == xTo && y == yTo)
		{
			if (state != 3 && state != 1)
			{
				state = 0;
			}
		}
		else
		{
			state = 2;
		}
	}

	public void setMove(int i, int j)
	{
		speed = 4;
		bool flag = false;
		bool flag2 = false;
		int num = Math_HAIRMOD_PROTECTED.abs(x - i);
		int num2 = Math_HAIRMOD_PROTECTED.abs(y - j);
		if (num <= speed)
		{
			x = (short)i;
			flag = true;
		}
		if (num2 < speed)
		{
			y = (short)j;
			flag2 = true;
		}
		if (flag && flag2)
		{
			p1 = (p2 = (p3 = 0));
			state = 0;
			vx = (vy = 0);
			lastDir = dir;
		}
		else if (x < i)
		{
			x += speed;
			dir = Char_HAIRMOD_PROTECTED.RIGHT;
		}
		else if (x > i)
		{
			x -= speed;
			dir = Char_HAIRMOD_PROTECTED.LEFT;
		}
		else if (y > j)
		{
			y -= speed;
			dir = Char_HAIRMOD_PROTECTED.UP;
		}
		else if (y < j)
		{
			dir = Char_HAIRMOD_PROTECTED.DOWN;
			y += speed;
		}
		setHuong(i, j);
	}

	public void setHuong(int xto, int yto)
	{
		huongY = (sbyte)((y > yto) ? 1 : 0);
		flip = (sbyte)((x - xto <= 0) ? 2 : 0);
		if (data.size() <= 0)
		{
			return;
		}
		flip = 0;
		if (dir == Char_HAIRMOD_PROTECTED.RIGHT)
		{
			flip = 2;
		}
		else if (dir != Char_HAIRMOD_PROTECTED.LEFT)
		{
			if (dir == Char_HAIRMOD_PROTECTED.DOWN)
			{
				huongY = 0;
			}
			else if (dir == Char_HAIRMOD_PROTECTED.UP)
			{
				huongY = 1;
			}
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (isDie || data.size() == 0)
		{
			return;
		}
		try
		{
			DataEffectThanThu_HAIRMOD_PROTECTED dataEffectThanThu_HAIRMOD_PROTECTED = (DataEffectThanThu_HAIRMOD_PROTECTED)data.elementAt(0);
			if (dataEffectThanThu_HAIRMOD_PROTECTED != null)
			{
				paintTopDataEff(g);
				int action = state;
				ImageIcon_HAIRMOD_PROTECTED imgIcon = GameData_HAIRMOD_PROTECTED.getImgIcon((short)(idThanThu + 12000));
				g.drawImage(Res_HAIRMOD_PROTECTED.imgShadow, x + ((flip != 2) ? 10 : (-10)), y - 10, 3);
				if (imgIcon != null && imgIcon.img != null)
				{
					dataEffectThanThu_HAIRMOD_PROTECTED.paint(g, dataEffectThanThu_HAIRMOD_PROTECTED.getFrame(frame, action, huongY), x + vangx, y + vangy, flip, imgIcon.img);
				}
			}
			else if (dataEffectThanThu_HAIRMOD_PROTECTED == null)
			{
			}
			paintBottomDataEff(g);
		}
		catch (Exception)
		{
		}
	}

	public override void paintCorner(MyGraphics_HAIRMOD_PROTECTED g, int corner, int corner2)
	{
	}

	public override void startAttack(LiveActor_HAIRMOD_PROTECTED target, AttackResult_HAIRMOD_PROTECTED atr, sbyte skillID, sbyte level)
	{
		state = 3;
		frame = 0;
	}

	public override void update()
	{
		DataEffectThanThu_HAIRMOD_PROTECTED dataEffectThanThu_HAIRMOD_PROTECTED = null;
		if (data.size() > 0)
		{
			dataEffectThanThu_HAIRMOD_PROTECTED = (DataEffectThanThu_HAIRMOD_PROTECTED)data.elementAt(0);
			int action = state;
			if (state == 3 && frame >= dataEffectThanThu_HAIRMOD_PROTECTED.getAnim(action, huongY).frame.Length - 1)
			{
				action = 0;
				state = 0;
			}
			frame = (sbyte)((frame + 1) % dataEffectThanThu_HAIRMOD_PROTECTED.getAnim(action, huongY).frame.Length);
		}
		switch (state)
		{
		case 0:
			break;
		case 3:
			break;
		case 2:
			setMove(xTo, yTo);
			break;
		case 1:
			if (hp > 0 && realHP > 0)
			{
				state = 0;
			}
			break;
		case 5:
			break;
		case 4:
			break;
		}
	}
}
