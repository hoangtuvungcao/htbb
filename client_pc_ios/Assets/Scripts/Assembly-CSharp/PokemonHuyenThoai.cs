using System;

public class PokemonHuyenThoai : Char
{
	public const sbyte STAND = 0;

	public const sbyte DEAD = 1;

	public const sbyte WALK = 2;

	public const sbyte ATTACK = 3;

	public const sbyte INJURE = 4;

	public const sbyte DEADFLY = 5;

	public static MyHashTable ALL_DATA_THAN_THU = new MyHashTable();

	public sbyte idThanThu;

	public mVector data = new mVector();

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

	public PokemonHuyenThoai()
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
		dir = (short)LiveActor.r.nextInt(4);
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
				mVector mVector2 = new mVector();
				mVector mVector3 = (mVector)ALL_DATA_THAN_THU.get(string.Empty + id);
				if (mVector3 != null)
				{
					mVector2 = mVector3;
					return;
				}
				DataInputStream dataInputStream = new DataInputStream(array);
				if (dataInputStream == null)
				{
					return;
				}
				sbyte b = 1;
				for (int i = 0; i < b; i++)
				{
					short num = (short)dataInputStream.available();
					sbyte[] array2 = new sbyte[num];
					for (int j = 0; j < array2.Length; j++)
					{
						array2[j] = dataInputStream.readByte();
					}
					mVector2.addElement(new DataEffectPokemonHuyenThoai(array2));
				}
				ALL_DATA_THAN_THU.put(string.Empty + id, mVector2);
				dataInputStream.close();
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
			data = (mVector)ALL_DATA_THAN_THU.get(string.Empty + id);
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
		int num = Math.abs(x - i);
		int num2 = Math.abs(y - j);
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
			dir = Char.RIGHT;
		}
		else if (x > i)
		{
			x -= speed;
			dir = Char.LEFT;
		}
		else if (y > j)
		{
			y -= speed;
			dir = Char.UP;
		}
		else if (y < j)
		{
			dir = Char.DOWN;
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
		if (dir == Char.RIGHT)
		{
			flip = 2;
		}
		else if (dir != Char.LEFT)
		{
			if (dir == Char.DOWN)
			{
				huongY = 0;
			}
			else if (dir == Char.UP)
			{
				huongY = 1;
			}
		}
	}

	/// <summary>
	/// Che do Pokemon: than thu (Hoa ky lan cap 1..3 = idThanThu 0..2, item 681..683) ve thanh
	/// Pokemon theo du_lieu/bang_thu.csv: Ponyta, Rapidash, Rapidash shiny.
	/// </summary>
	public static readonly short[] POKE_DEX_THAN_THU = new short[] { 77, 78, 78 };

	public static readonly bool[] POKE_SHINY_THAN_THU = new bool[] { false, false, true };

	private bool paintPokemonThanThu(MyGraphics g)
	{
		if (idThanThu < 0 || idThanThu >= POKE_DEX_THAN_THU.Length)
		{
			return false;
		}
		int dex = POKE_DEX_THAN_THU[idThanThu];
		bool shiny = POKE_SHINY_THAN_THU[idThanThu];
		if (!PokeBody.coSprite(dex, shiny))
		{
			return false;
		}
		int dongTac;
		switch (state)
		{
		case DEAD:
		case DEADFLY:
			dongTac = PokeDuLieu.GUC_NGA;
			break;
		case WALK:
			dongTac = PokeDuLieu.CHAY;
			break;
		case ATTACK:
			dongTac = PokeDuLieu.DANH_GAN;
			break;
		case INJURE:
			dongTac = PokeDuLieu.TRUNG_DON;
			break;
		default:
			dongTac = PokeDuLieu.DUNG;
			break;
		}
		g.drawImage(Res.imgShadow, x, y - 2, MyGraphics.HCENTER | MyGraphics.VCENTER);
		PokeBody.paint(g, dex, shiny, x + vangx, y + vangy, dir, dongTac, pokeTickTrongDongTac(dongTac), 100);
		return true;
	}

	public override void paint(MyGraphics g)
	{
		if (isDie)
		{
			return;
		}
		if (paintPokemonThanThu(g))
		{
			return;
		}
		if (data.size() == 0)
		{
			return;
		}
		try
		{
			DataEffectPokemonHuyenThoai dataEffectThanThu = (DataEffectPokemonHuyenThoai)data.elementAt(0);
			if (dataEffectThanThu != null)
			{
				paintTopDataEff(g);
				int action = state;
				ImageIcon imgIcon = GameData.getImgIcon((short)(idThanThu + 12000));
				g.drawImage(Res.imgShadow, x + ((flip != 2) ? 10 : (-10)), y - 10, 3);
				if (imgIcon != null && imgIcon.img != null)
				{
					dataEffectThanThu.paint(g, dataEffectThanThu.getFrame(frame, action, huongY), x + vangx, y + vangy, flip, imgIcon.img);
				}
			}
			else if (dataEffectThanThu != null)
			{
			}
			paintBottomDataEff(g);
		}
		catch (Exception)
		{
		}
	}

	public override void paintCorner(MyGraphics g, int corner, int corner2)
	{
	}

	public override void startAttack(LiveActor target, AttackResult atr, sbyte skillID, sbyte level)
	{
		state = 3;
		frame = 0;
	}

	public override void update()
	{
		DataEffectPokemonHuyenThoai dataEffectThanThu = null;
		if (data.size() > 0)
		{
			dataEffectThanThu = (DataEffectPokemonHuyenThoai)data.elementAt(0);
			int action = state;
			if (state == 3 && frame >= dataEffectThanThu.getAnim(action, huongY).frame.Length - 1)
			{
				action = 0;
				state = 0;
			}
			frame = (sbyte)((frame + 1) % dataEffectThanThu.getAnim(action, huongY).frame.Length);
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
