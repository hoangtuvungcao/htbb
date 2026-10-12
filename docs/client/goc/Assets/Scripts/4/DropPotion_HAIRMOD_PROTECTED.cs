public class DropPotion_HAIRMOD_PROTECTED : Dropable_HAIRMOD_PROTECTED
{
	private byte startItemQuest = 10;

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(MainChar_HAIRMOD_PROTECTED.listPotion[item_template_id].index + 5500), x, y - deltaH);
	}

	public override string getDisplayName()
	{
		if (item_template_id < 7 || item_template_id >= 14)
		{
			return MainChar_HAIRMOD_PROTECTED.listPotion[item_template_id].name;
		}
		if (item_template_id > 9 && item_template_id < 14)
		{
			return Res_HAIRMOD_PROTECTED.PORTION_ITEM_NAME;
		}
		return Res_HAIRMOD_PROTECTED.PORTION_ITEM_NAME_CLAN;
	}

	public string getDisplayName2()
	{
		if (item_template_id < 7 || item_template_id >= 14)
		{
			return MainChar_HAIRMOD_PROTECTED.listPotion[item_template_id].name2;
		}
		if (item_template_id > 9 && item_template_id < 14)
		{
			return Res_HAIRMOD_PROTECTED.PORTION_ITEM_NAME2;
		}
		return Res_HAIRMOD_PROTECTED.PORTION_ITEM_NAME_CLAN;
	}

	public override bool isItemCanGet()
	{
		return true;
	}
}
