public class InfoAttributeItem_HAIRMOD_PROTECTED
{
	private short id;

	private short value;

	public InfoAttributeItem_HAIRMOD_PROTECTED(short id, short value)
	{
		this.id = id;
		this.value = value;
	}

	public sbyte getColorPaint(bool kick)
	{
		return ((NameAttributeItem_HAIRMOD_PROTECTED)ItemTemplate_HAIRMOD_PROTECTED.ALL_NAME_ATTRIBUTE_ITEM.get(id + string.Empty)).getColorPaint(kick);
	}

	public string getName(int clazz)
	{
		string text = ((NameAttributeItem_HAIRMOD_PROTECTED)ItemTemplate_HAIRMOD_PROTECTED.ALL_NAME_ATTRIBUTE_ITEM.get(id + string.Empty)).name;
		if (id >= 43 && id <= 57)
		{
			text = SkillManager_HAIRMOD_PROTECTED.SKILL_NAME[clazz][id - 43];
		}
		return text + ": ";
	}

	public bool isPercent()
	{
		sbyte ispercent = ((NameAttributeItem_HAIRMOD_PROTECTED)ItemTemplate_HAIRMOD_PROTECTED.ALL_NAME_ATTRIBUTE_ITEM.get(id + string.Empty)).ispercent;
		return ispercent == 1 || ispercent == 2;
	}

	public string getValue()
	{
		string result = ((id != 33 && id != 34) ? value : (value * 1000)) + string.Empty;
		if (((NameAttributeItem_HAIRMOD_PROTECTED)ItemTemplate_HAIRMOD_PROTECTED.ALL_NAME_ATTRIBUTE_ITEM.get(id + string.Empty)).ispercent == 2)
		{
			result = value / 10 + "." + value % 10;
		}
		return result;
	}

	public int getValueAtt()
	{
		return value;
	}

	public short getID()
	{
		return id;
	}
}
