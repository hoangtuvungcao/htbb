public class AttackResult
{
	public int mpLost;

	public int dam;

	public sbyte effect;

	public int buffEffect = -1;

	public static readonly sbyte EFF_NORMAL = 0;

	public static readonly sbyte EFF_MISS = 1;

	public static readonly sbyte EFF_CRITICAL = 2;

	public static readonly sbyte EFF_XUYEN_GIAP = 3;

	public static readonly sbyte EFF_BAO_KICH = 4;

	/// <summary>Pokemon: khac he (hieu qua cao) - server gui effect 5; PhanHoiChienDau ve "Hiệu quả cao!".</summary>
	public static readonly sbyte EFF_HIEU_QUA_CAO = 5;

	public static readonly string[] EFF_NAME = new string[6]
	{
		string.Empty,
		"miss",
		"CHI MANG",
		"XUYEN GIAP",
		"BAOKICH",
		"HIEU QUA CAO"
	};

	public AttackResult(int dam, sbyte effect)
	{
		this.dam = dam;
		this.effect = effect;
	}

	public AttackResult()
	{
	}

	public static void startEff(sbyte eff, int x, int y)
	{
		if (eff > 0 && eff < EFF_NAME.Length)
		{
			Canvas.gameScr.startFlyText(EFF_NAME[eff], 0, x, y, 1, -2);
		}
	}
}
