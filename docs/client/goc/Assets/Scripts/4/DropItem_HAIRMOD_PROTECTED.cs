public class DropItem_HAIRMOD_PROTECTED : Dropable_HAIRMOD_PROTECTED
{
	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		ItemTemplate_HAIRMOD_PROTECTED itemTemplate_HAIRMOD_PROTECTED = ((ItemTemplate_HAIRMOD_PROTECTED[])Res_HAIRMOD_PROTECTED.itemTemplates.elementAt(itemClass))[item_template_id];
		GameData_HAIRMOD_PROTECTED.paintIcon(g, itemTemplate_HAIRMOD_PROTECTED.idIcon, x, y - deltaH);
	}

	public override string getDisplayName()
	{
		return ((ItemTemplate_HAIRMOD_PROTECTED[])Res_HAIRMOD_PROTECTED.itemTemplates.elementAt(itemClass))[item_template_id].name;
	}

	public override bool isItemCanGet()
	{
		return true;
	}
}
