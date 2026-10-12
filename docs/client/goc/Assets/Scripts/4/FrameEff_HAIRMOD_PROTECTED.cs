public class FrameEff_HAIRMOD_PROTECTED
{
	public mVector_HAIRMOD_PROTECTED listPart = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED listPartBottom = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED listPartTop = new mVector_HAIRMOD_PROTECTED();

	public sbyte xShadow;

	public sbyte yShadow;

	public FrameEff_HAIRMOD_PROTECTED(mVector_HAIRMOD_PROTECTED list)
	{
		listPart = list;
	}

	public FrameEff_HAIRMOD_PROTECTED(mVector_HAIRMOD_PROTECTED listtop, mVector_HAIRMOD_PROTECTED listbottom)
	{
		listPartTop = listtop;
		listPartBottom = listbottom;
		listPart = listtop;
	}

	public mVector_HAIRMOD_PROTECTED getListPartPaint()
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		for (int i = 0; i < listPartBottom.size(); i++)
		{
			mVector_HAIRMOD_PROTECTED2.addElement(listPartBottom.elementAt(i));
		}
		for (int j = 0; j < listPart.size(); j++)
		{
			mVector_HAIRMOD_PROTECTED2.addElement(listPart.elementAt(j));
		}
		return mVector_HAIRMOD_PROTECTED2;
	}
}
