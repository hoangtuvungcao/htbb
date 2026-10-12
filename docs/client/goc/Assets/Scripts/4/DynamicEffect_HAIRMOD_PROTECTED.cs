using System;

public class DynamicEffect_HAIRMOD_PROTECTED : Actor_HAIRMOD_PROTECTED
{
	public static MyHashTable_HAIRMOD_PROTECTED ALL_DATA_DYNAMIC_EFFECT = new MyHashTable_HAIRMOD_PROTECTED();

	public short id;

	public long timeExist = -1L;

	private sbyte index;

	public int power = 2000000000;

	public int dam;

	public sbyte stop;

	public DynamicEffect_HAIRMOD_PROTECTED(int id)
	{
		this.id = (short)id;
	}

	public void paint(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
	{
		DataEffect_HAIRMOD_PROTECTED dataEffect_HAIRMOD_PROTECTED = (DataEffect_HAIRMOD_PROTECTED)ALL_DATA_DYNAMIC_EFFECT.get(id + string.Empty);
		if (dataEffect_HAIRMOD_PROTECTED != null)
		{
			ImageIcon_HAIRMOD_PROTECTED imgIcon = GameData_HAIRMOD_PROTECTED.getImgIcon((short)(id + 8700));
			if (imgIcon != null && imgIcon.img != null)
			{
				sbyte[] sequence = getSequence(id);
				dataEffect_HAIRMOD_PROTECTED.paint(g, sequence[index], x, y, 0, imgIcon.img);
			}
		}
	}

	public static void setDataEffect(sbyte[][] array)
	{
		for (int i = 0; i < array.Length; i++)
		{
			DataEffect_HAIRMOD_PROTECTED v = new DataEffect_HAIRMOD_PROTECTED(array[i]);
			ALL_DATA_DYNAMIC_EFFECT.put(i + string.Empty, v);
		}
	}

	public static sbyte[] getSequence(int id)
	{
		return ((DataEffect_HAIRMOD_PROTECTED)ALL_DATA_DYNAMIC_EFFECT.get(id + string.Empty))?.sequence;
	}

	public bool canDestroy()
	{
		if (timeExist == -1)
		{
			return false;
		}
		return mSystem_HAIRMOD_PROTECTED.currentTimeMillis() - timeExist >= 0 && index == 0;
	}

	public override void update()
	{
		try
		{
			sbyte[] sequence = getSequence(id);
			if (sequence != null)
			{
				index = (sbyte)((index + 1) % sequence.Length);
				if (index >= sequence.Length / 2 && dam > 0)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + dam, 0, x, y - 15, 1, -2);
					dam = 0;
				}
			}
		}
		catch (Exception)
		{
		}
		if (canDestroy())
		{
			wantDestroy = true;
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		DataEffect_HAIRMOD_PROTECTED dataEffect_HAIRMOD_PROTECTED = (DataEffect_HAIRMOD_PROTECTED)ALL_DATA_DYNAMIC_EFFECT.get(id + string.Empty);
		if (dataEffect_HAIRMOD_PROTECTED != null)
		{
			ImageIcon_HAIRMOD_PROTECTED imgIcon = GameData_HAIRMOD_PROTECTED.getImgIcon((short)(id + 8700));
			if (imgIcon != null && imgIcon.img != null)
			{
				sbyte[] sequence = getSequence(id);
				dataEffect_HAIRMOD_PROTECTED.paint(g, sequence[index], x, y, 0, imgIcon.img);
			}
		}
	}

	public override void setPosTo(short x, short y)
	{
	}
}
