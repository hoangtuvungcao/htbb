public class SkillManager : MyHashTable
{
	public static SkillManager instance = new SkillManager();

	private static string strTancong = "Tấn công tăng: ";

	private static string strPhongThu = "Phòng thủ tăng: ";

	public static string[][] infoString = new string[5][]
	{
		new string[9] { strTancong, strTancong, strTancong, strTancong, strPhongThu, strTancong, strTancong, strTancong, strTancong },
		new string[9] { strTancong, strTancong, strTancong, strTancong, strPhongThu, "Tăng sức công: ", strTancong, strTancong, strTancong },
		new string[11]
		{
			strTancong, strTancong, strTancong, strTancong, "HP+MP tăng: ", "MP tăng: ", "Hồi sinh: ", "Chia sẻ thiệt hại: ", strTancong, strTancong,
			strTancong
		},
		new string[9] { strTancong, strTancong, strTancong, strTancong, "Tốc độ tăng: ", strPhongThu, strTancong, strTancong, strTancong },
		new string[9] { strTancong, strTancong, strTancong, strTancong, "Tốc độ tăng: ", "HP tăng: ", strTancong, strTancong, strTancong }
	};

	public static sbyte[][] LEVEL_ADD_SKILL;

	public static readonly sbyte[][] EFF_BUFF_SKILL = new sbyte[5][]
	{
		new sbyte[2] { 20, 24 },
		new sbyte[2] { 20, 21 },
		new sbyte[4] { 25, 30, 27, 23 },
		new sbyte[2] { 19, 20 },
		new sbyte[2] { 22, 19 }
	};

	public static readonly sbyte[][] SKILL_CAN_BUFF_TO_USER = new sbyte[5][]
	{
		new sbyte[2] { -1, 0 },
		new sbyte[2] { 0, -1 },
		new sbyte[4] { 0, -1, 1, 0 },
		new sbyte[2] { 0, -1 },
		new sbyte[2] { 0, -1 }
	};

	public static sbyte[][] SKILL_AEO;

	public static readonly sbyte[][][] RANGE_SKILL_AEO = new sbyte[1][][] { new sbyte[3][]
	{
		new sbyte[11]
		{
			0, 64, 64, 80, 80, 96, 96, 102, 118, 124,
			124
		},
		new sbyte[11]
		{
			0, 64, 64, 80, 80, 96, 96, 102, 118, 124,
			124
		},
		new sbyte[11]
		{
			0, 64, 64, 80, 80, 96, 96, 102, 118, 124,
			124
		}
	} };

	public static short[][][] SKILL_DAM_PERCENT;

	public static short[][] TIME_LIFE_BUFF_SKILL;

	public static int[][][] SKILL_COOLDOWN;

	public static short[][][] SKILL_MP;

	public static short[][] SKILL_RANGE;

	public static readonly string[][] SKILL_NAME = new string[5][]
	{
		new string[9] { "Cào", "Than hồng", "Nanh lửa", "Xoáy lửa", "Màn khói", "Múa kiếm", "Súng phun lửa", "Vuốt rồng", "Đại hỏa" },
		new string[9] { "Húc", "Roi dây leo", "Lá dao", "Bom bùn", "Tự vệ", "Sinh trưởng", "Bom hạt", "Bão lá", "Tia mặt trời" },
		new string[11] { "Húc", "Tấn công nhanh", "Cắn", "Sao băng", "Mong ước", "Nghỉ ngơi", "Phước lành hồi sinh", "Chia sẻ nỗi đau", "Đuôi sắt", "Bứt phá cuối cùng", "Tia phá hủy" },
		new string[9] { "Húc", "Súng nước", "Bong bóng", "Xoáy nước", "Phá mai", "Rút vào mai", "Đuôi nước", "Bơm nước", "Đại pháo nước" },
		new string[9] { "Tia lửa điện", "Sóng điện", "Cầu điện", "Tia chớp", "Nhanh nhẹn", "Nạp điện", "Lưới điện", "Sấm sét", "Đâm sầm điện" }
	};

	public static SkillAnimate[][] SKILL_ANIMATE = new SkillAnimate[5][]
	{
		new SkillAnimate[9]
		{
			new Skill_DanhThuong(700),
			new Skill_Charmander_Type1(),
			new Skill_Charmander_Type2(),
			new Skill_Charmander_Type3(0),
			null,
			null,
			new Skill_Charmander_TypeAEO(4),
			new Skill_Charmander_TypeAEO(5),
			new Skill_Charmander_Type4()
		},
		new SkillAnimate[9]
		{
			new Skill_DanhThuong(1200),
			new Skill_Bulbasaur_Type1(),
			new Skill_Bulbasaur_Type2(),
			new Skill_Bulbasaur_Type3(0),
			null,
			null,
			new Skill_Bulbasaur_TypeAEO(false),
			new Skill_Bulbasaur_TypeAEO(true),
			new Skill_Bulbasaur_Type4()
		},
		new SkillAnimate[11]
		{
			new Skill_DanhThuong(1000),
			new Skill_Eevee_Type1(),
			new Skill_Eevee_Type2(),
			new Skill_Eevee_Type3(0),
			null,
			null,
			null,
			null,
			new Skill_Eevee_TypeAEO(false),
			new Skill_Eevee_TypeAEO(true),
			new Skill_Eevee_Type4(0)
		},
		new SkillAnimate[9]
		{
			new Skill_DanhThuong(1500),
			new Skill_Squirtle_Type1(),
			new Skill_Squirtle_Type2(),
			new Skill_Squirtle_Type3(0),
			null,
			null,
			new Skill_Squirtle_TypeAEO(false),
			new Skill_Squirtle_TypeAEO(true),
			new Skill_Squirtle_Type4(0)
		},
		new SkillAnimate[10]
		{
			new Skill_Pichu_Type0(),
			new Skill_Pichu_Type1(),
			new Skill_Pichu_Type2(),
			new Skill_Pichu_Type3(0),
			null,
			null,
			new Skill_Pichu_TypeAEO(4, false, 2),
			new Skill_Pichu_TypeAEO(5, true, 2),
			new Skill_Pichu_Type4(0),
			new Skill_Pichu_TypeAEO(4, false, 5)
		}
	};

	public static int getSkillDamPercent(int skillType, int skillLevel, int clazz)
	{
		return (skillLevel > -1) ? SKILL_DAM_PERCENT[0][skillType][skillLevel] : 0;
	}

	public static int getSkillMP(int skillType, int skillLevel, int clazz)
	{
		return (skillLevel > -1) ? SKILL_MP[0][skillType][skillLevel] : 0;
	}

	public static long getSkillCooldown(sbyte skillType, sbyte clazz, int level)
	{
		return (level > -1) ? SKILL_COOLDOWN[0][skillType][level] : 0;
	}

	public static int getSkillRange(sbyte skillType, sbyte clazz)
	{
		return SKILL_RANGE[0][skillType];
	}

	public static short getLvAddSkill(int clazz, int idSkill, int lvSkill)
	{
		return (short)((lvSkill > -1) ? LEVEL_ADD_SKILL[idSkill][lvSkill] : 0);
	}

	public static sbyte getBuffToUser(int clazz, int id)
	{
		return SKILL_CAN_BUFF_TO_USER[clazz][id];
	}

	public static bool isSkillAeo(int cClass, int skill)
	{
		for (int i = 0; i < SKILL_AEO[cClass].Length; i++)
		{
			if (skill == SKILL_AEO[cClass][i])
			{
				return true;
			}
		}
		return false;
	}

	public static int getRangeSkillAeo(int charClass, int skill)
	{
		int num = Char.skillLevelLearnt[skill];
		if (num > -1)
		{
			for (int i = 0; i < SKILL_AEO[charClass].Length; i++)
			{
				if (skill == SKILL_AEO[charClass][i])
				{
					return RANGE_SKILL_AEO[0][i][num];
				}
			}
		}
		return 1984;
	}

	public static SkillAnimate getSkillAnimate(int skillType, int clazz)
	{
		if (clazz > SKILL_ANIMATE.Length)
		{
			return null;
		}
		if (skillType > SKILL_ANIMATE[clazz].Length)
		{
			return null;
		}
		return SKILL_ANIMATE[clazz][skillType];
	}

	public static SkillAnimate setSkillBoss_Xmas()
	{
		return new Skill_Boss_Xmas(false);
	}
}
