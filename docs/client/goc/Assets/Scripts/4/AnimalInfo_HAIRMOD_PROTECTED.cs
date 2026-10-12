public class AnimalInfo_HAIRMOD_PROTECTED
{
	public string info = string.Empty;

	public string name = string.Empty;

	public sbyte idImg;

	public sbyte level;

	public sbyte typeAnimal;

	public sbyte typePet = -1;

	public short id;

	public static Image_HAIRMOD_PROTECTED imgAnimal;

	private static bool isGetImg;

	public long timeStart;

	public int totalTimeCon;

	public sbyte idPaint;

	public void paint(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
	{
		GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(idPaint + 7500), x, y);
	}

	public static void getImg()
	{
		if (!isGetImg)
		{
			GameService_HAIRMOD_PROTECTED.gI().doGetImage(4, 0);
		}
		isGetImg = true;
	}
}
