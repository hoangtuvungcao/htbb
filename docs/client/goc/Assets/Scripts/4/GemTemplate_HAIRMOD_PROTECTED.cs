public class GemTemplate_HAIRMOD_PROTECTED
{
	public short id;

	public short rID;

	public int number;

	public int price;

	public bool ilock;

	public GemTemplate_HAIRMOD_PROTECTED()
	{
	}

	public GemTemplate_HAIRMOD_PROTECTED(int id)
	{
		this.id = (sbyte)id;
	}

	public static GemTemplate_HAIRMOD_PROTECTED getGemByID(short id)
	{
		for (int i = 0; i < MainChar_HAIRMOD_PROTECTED.gemItem.size(); i++)
		{
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemItem.elementAt(i);
			if (gemTemplate_HAIRMOD_PROTECTED.id == id)
			{
				return gemTemplate_HAIRMOD_PROTECTED;
			}
		}
		return null;
	}

	public static GemTemplate_HAIRMOD_PROTECTED getGemByRealID(short rID)
	{
		for (int i = 0; i < MainChar_HAIRMOD_PROTECTED.gemItem.size(); i++)
		{
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemItem.elementAt(i);
			if (gemTemplate_HAIRMOD_PROTECTED.rID == rID)
			{
				return gemTemplate_HAIRMOD_PROTECTED;
			}
		}
		return null;
	}

	public static GemTemplate_HAIRMOD_PROTECTED copyData(GemTemplate_HAIRMOD_PROTECTED from, GemTemplate_HAIRMOD_PROTECTED to)
	{
		to.id = from.id;
		to.number = from.number;
		to.price = from.price;
		to.rID = from.rID;
		return to;
	}
}
