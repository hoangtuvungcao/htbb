public class DropGemItem_HAIRMOD_PROTECTED : Dropable_HAIRMOD_PROTECTED
{
	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (catagory == 6)
		{
			Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(item_template_id).idImage, x, y - deltaH);
		}
		else
		{
			GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(Res_HAIRMOD_PROTECTED.getShopByID((byte)item_template_id).idImage + 5500), x, y - deltaH);
		}
	}

	public override string getDisplayName()
	{
		if (catagory == 6)
		{
			return Res_HAIRMOD_PROTECTED.getGemByID(item_template_id).name;
		}
		return Res_HAIRMOD_PROTECTED.getShopByID(item_template_id).name;
	}

	public override bool isItemCanGet()
	{
		return true;
	}
}
