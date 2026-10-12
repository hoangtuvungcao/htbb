public class Tree_HAIRMOD_PROTECTED : Actor_HAIRMOD_PROTECTED
{
	public int type;

	public Tree_HAIRMOD_PROTECTED(int x, int y, int type)
	{
		base.x = (short)x;
		base.y = (short)y;
		this.type = type;
		catagory = Actor_HAIRMOD_PROTECTED.CAT_TREE;
	}

	public void removeImage()
	{
		Res_HAIRMOD_PROTECTED.removeTreeImage(type);
	}

	public int getH()
	{
		return Res_HAIRMOD_PROTECTED.getHeightTree(type);
	}

	public int getW()
	{
		return Res_HAIRMOD_PROTECTED.getwidthTree(type);
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		TreeInfo_HAIRMOD_PROTECTED tree = Res_HAIRMOD_PROTECTED.getTree(type);
		int num = (x << 4) + tree.dx;
		int num2 = (y << 4) + tree.dy;
		if (num >= GameScr_HAIRMOD_PROTECTED.cmx - tree.w && num2 >= GameScr_HAIRMOD_PROTECTED.cmy - tree.h && num <= GameScr_HAIRMOD_PROTECTED.cmx + GameScr_HAIRMOD_PROTECTED.gsw && num2 <= GameScr_HAIRMOD_PROTECTED.cmy + GameScr_HAIRMOD_PROTECTED.gsh)
		{
			tree.paint(g, num, num2);
		}
	}

	public override int getX()
	{
		return base.getX();
	}

	public override int getY()
	{
		int num = 0;
		return (y << 4) + Res_HAIRMOD_PROTECTED.getDyTree(type) + Res_HAIRMOD_PROTECTED.getHeightTree(type);
	}

	public override void setPosTo(short x, short y)
	{
	}
}
