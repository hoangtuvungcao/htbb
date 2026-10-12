using System;
using System.Collections;
using UnityEngine;

public class Char_HAIRMOD_PROTECTED : LiveActor_HAIRMOD_PROTECTED
{
	public const sbyte A_STAND = 0;

	public const sbyte A_RUN = 1;

	public const sbyte A_ATTACK = 2;

	public const sbyte A_DEAD = 3;

	public const sbyte A_COME_HOME = 4;

	public const sbyte MOVETOFIRST = 5;

	public static short CHAR_POTION = 25;

	public static Image_HAIRMOD_PROTECTED imgFlgPk = null;

	public static Image_HAIRMOD_PROTECTED imgLight;

	public int isusing = 1;

	public int IDDB;

	public static bool dissolved = false;

	public sbyte isMaster = 3;

	public SkillAnimate_HAIRMOD_PROTECTED currentSkillAnimate;

	public sbyte currentSkillType;

	public int lastXAuto;

	public int lastYAuto;

	public bool justStopFromRun;

	public bool paintHat = true;

	public sbyte idAnimal = -1;

	public sbyte idPaintIconAnimal;

	public string name = string.Empty;

	public string infoAnimal = string.Empty;

	public LiveActor_HAIRMOD_PROTECTED attackTarget;

	public sbyte upSpeed;

	public sbyte gender;

	public long[] timeLastUseSkills = new long[11];

	public long timeComeHome;

	public long timeWearScarves;

	public long[] coolDownSkill = new long[11];

	public short p1;

	public short p2;

	public short p3;

	public short p4;

	public short xTo;

	public short yTo;

	public short weapon_frame = -1;

	public short wName;

	public short basePointLeft;

	public short skillPointLeft;

	public short strength;

	public short spirit;

	public short agility;

	public short health;

	public short luck;

	public static readonly sbyte DOWN = 0;

	public static readonly sbyte UP = 1;

	public static readonly sbyte LEFT = 2;

	public static readonly sbyte RIGHT = 3;

	public static readonly sbyte REAL_FRAME_EACH_SIZE = 5;

	public short bodyStyle = -1;

	public short legStyle = -1;

	public short hatStyle = -1;

	public short currentHead = -1;

	public short coatStyle = -1;

	public int attkPower;

	public int wimg;

	public int himg;

	public sbyte clazz;

	public sbyte idFashion = -1;

	public sbyte timeChangeFrame = 1;

	public short lvpercent;

	public static sbyte[] skillLevelLearnt;

	public mVector_HAIRMOD_PROTECTED wearingItems;

	public mVector_HAIRMOD_PROTECTED wearingAnimal;

	public int luong;

	public int luongKhoa = -1;

	public Image_HAIRMOD_PROTECTED imgWpa;

	public Image_HAIRMOD_PROTECTED imgAnimal;

	public short dxWear;

	public short dyWear;

	public short idhorse = -1;

	public short typehorse = -1;

	public short idWeapon = -1;

	public short typeWeapon = -1;

	public short idImageWeapon = -1;

	public int weaponStyle = -1;

	public int weaponType = -1;

	public sbyte indexPP = -1;

	public sbyte nFrameWP;

	public sbyte nFramePP;

	public sbyte nFrameAVT;

	public sbyte indexAVT = -1;

	public short idImgPP = -1;

	public short idImgAVT = -1;

	public short idHair = -1;

	public short idBody = -1;

	public short idLeg = -1;

	public short idPhiPhong = -1;

	public sbyte weaponIndex;

	public sbyte numFrame;

	public new sbyte nationID = -1;

	public int[] potions = new int[CHAR_POTION];

	public long charXu;

	public long[] potionLastTimeUse = new long[CHAR_POTION];

	public static sbyte[] nSkill = new sbyte[5];

	public static mVector_HAIRMOD_PROTECTED inventory = new mVector_HAIRMOD_PROTECTED();

	public static mVector_HAIRMOD_PROTECTED animal = new mVector_HAIRMOD_PROTECTED();

	public static mVector_HAIRMOD_PROTECTED party = new mVector_HAIRMOD_PROTECTED();

	public static mVector_HAIRMOD_PROTECTED itembag = new mVector_HAIRMOD_PROTECTED();

	public int maxmp;

	public int mp;

	public sbyte attkEffect;

	public int lockUpdate;

	public sbyte[] buffType = new sbyte[7] { -1, -1, -1, -1, -1, -1, -1 };

	public sbyte[] lvBuff = new sbyte[7];

	public short[] countDown = new short[7];

	public bool useBuff;

	public short questID = -1;

	public short idQuestNPC = -1;

	public bool receiveQuest;

	public bool finishQuest;

	public int[][] monster_kill;

	public sbyte[] npc_response;

	public sbyte[] npc_quest;

	public bool[] talked;

	public sbyte nextNpc;

	public string[][] info_response;

	public string[] info_npc_quest;

	public sbyte typeQuest = 3;

	public sbyte anmFly;

	public bool isTrade;

	public bool gotoBoard;

	public bool isNo;

	public bool isDoing;

	public ItemInInventory_HAIRMOD_PROTECTED itemImbue;

	public long timeWait2Board;

	public long timeUseGolgTicket;

	public static int goldTimeMinute = 0;

	public long delay = -1L;

	public static int idGoldTicket = 0;

	public static string goldTime = string.Empty;

	public static readonly sbyte KIEM = 0;

	public static readonly sbyte DAO = 1;

	public static readonly sbyte PS = 2;

	public static readonly sbyte DS = 3;

	public static readonly sbyte CUNG = 4;

	public Effect_HAIRMOD_PROTECTED effPhap;

	public Effect_HAIRMOD_PROTECTED effAnimal;

	public bool isAnimal;

	public IsAnimal_HAIRMOD_PROTECTED myAnimal;

	public AnimalMove_HAIRMOD_PROTECTED myPet;

	public short[] idModel = new short[5] { -1, -1, -1, -1, -1 };

	public short baokich;

	public int congTrang;

	public int lienTram;

	public int hoatdong;

	public sbyte idImgHorse;

	public short[] infoPaint = new short[6] { -1, -1, -1, -1, -1, -1 };

	private static sbyte[][][] ORDER_PAINT = new sbyte[2][][]
	{
		new sbyte[4][]
		{
			new sbyte[6] { 5, 4, 0, 1, 2, 3 },
			new sbyte[5] { 1, 4, 2, 3, 5 },
			new sbyte[6] { 4, 5, 0, 1, 2, 3 },
			new sbyte[6] { 4, 5, 0, 1, 2, 3 }
		},
		new sbyte[4][]
		{
			new sbyte[6] { 4, 0, 1, 2, 3, 5 },
			new sbyte[6] { 0, 1, 4, 2, 3, 5 },
			new sbyte[6] { 4, 0, 1, 2, 3, 5 },
			new sbyte[6] { 4, 0, 1, 2, 3, 5 }
		}
	};

	public int frameThuCuoi;

	public int Fhorse;

	public int fh;

	public static sbyte[] horseMove = new sbyte[8] { 2, 2, 2, 3, 3, 4, 4, 4 };

	public sbyte dyThanthu;

	public static sbyte char_speed = 3;

	public long stand;

	public static readonly sbyte[] NEXTX = new sbyte[4] { 0, 0, -1, 1 };

	public static readonly sbyte[] NEXTY = new sbyte[4] { 1, -1, 0, 0 };

	public static readonly sbyte[] STANDFRAME = new sbyte[12]
	{
		0, 0, 0, 0, 0, 0, 0, 0, 1, 1,
		1, 1
	};

	public static readonly sbyte[] RUNFRAME = new sbyte[12]
	{
		2, 2, 2, 0, 0, 0, 3, 3, 3, 0,
		0, 0
	};

	public static readonly sbyte[] RUNFRAMEWAL = new sbyte[10] { 2, 2, 2, 0, 0, 3, 3, 3, 0, 0 };

	public static readonly sbyte[] GENDER_OF_CLAZZ = new sbyte[5] { 1, 1, 2, 1, 2 };

	public bool comeHome;

	public long realTime;

	public long timeDelayRequestCharInfo;

	public long timeGoToLastXY;

	public long timeRemoEffanimal;

	private int aaaa;

	public static MyHashTable_HAIRMOD_PROTECTED ALL_PART_EFF = new MyHashTable_HAIRMOD_PROTECTED();

	private int idColor;

	public int ifHorse;

	public static short[][][] fHorse = new short[8][][]
	{
		new short[4][]
		{
			new short[3] { 95, 112, 130 },
			new short[3] { 246, 263, 280 },
			new short[3] { 216, 182, 149 },
			new short[3] { 298, 329, 363 }
		},
		new short[4][]
		{
			new short[3] { 119, 138, 158 },
			new short[3] { 308, 325, 341 },
			new short[3] { 267, 223, 178 },
			new short[3] { 359, 400, 444 }
		},
		new short[4][]
		{
			new short[3] { 107, 124, 140 },
			new short[3] { 276, 292, 309 },
			new short[3] { 239, 197, 158 },
			new short[3] { 324, 362, 404 }
		},
		new short[4][]
		{
			new short[3] { 115, 133, 150 },
			new short[3] { 296, 313, 330 },
			new short[3] { 254, 210, 167 },
			new short[3] { 347, 387, 433 }
		},
		new short[4][]
		{
			new short[4] { 129, 152, 179, 152 },
			new short[4] { 210, 233, 260, 233 },
			new short[4] { 0, 43, 86, 43 },
			new short[4] { 291, 334, 377, 334 }
		},
		new short[4][]
		{
			new short[4] { 180, 210, 255, 210 },
			new short[4] { 303, 333, 379, 333 },
			new short[4] { 0, 60, 120, 60 },
			new short[4] { 427, 487, 547, 487 }
		},
		new short[4][]
		{
			new short[3] { 0, 24, 48 },
			new short[3] { 171, 192, 213 },
			new short[3] { 72, 106, 140 },
			new short[3] { 234, 265, 299 }
		},
		new short[4][]
		{
			new short[4] { 180, 210, 255, 210 },
			new short[4] { 303, 333, 379, 333 },
			new short[4] { 0, 60, 120, 60 },
			new short[4] { 427, 487, 547, 487 }
		}
	};

	public static readonly sbyte[][] dxh = new sbyte[8][]
	{
		new sbyte[4] { 0, 0, -3, 3 },
		new sbyte[4] { 0, 0, -3, 3 },
		new sbyte[4] { 0, 0, -3, 3 },
		new sbyte[4] { 0, 0, -3, 3 },
		new sbyte[4] { 0, 0, -3, 3 },
		new sbyte[4] { 0, 0, 3, -4 },
		new sbyte[4] { 0, 0, -3, 3 },
		new sbyte[4] { 0, 0, 3, -4 }
	};

	public static readonly sbyte[][] dyh = new sbyte[8][]
	{
		new sbyte[4],
		new sbyte[4],
		new sbyte[4],
		new sbyte[4],
		new sbyte[4],
		new sbyte[4] { -2, 0, 7, 7 },
		new sbyte[4],
		new sbyte[4] { -2, 0, 7, 7 }
	};

	public static readonly sbyte[][] dyb = new sbyte[8][]
	{
		new sbyte[4] { -12, -7, -10, -10 },
		new sbyte[4] { -12, -7, -10, -10 },
		new sbyte[4] { -12, -7, -10, -10 },
		new sbyte[4] { -12, -7, -10, -10 },
		new sbyte[4] { -12, -7, -10, -10 },
		new sbyte[4] { -12, -7, -10, -10 },
		new sbyte[4] { -12, -7, -10, -10 },
		new sbyte[4] { -12, -7, -10, -10 }
	};

	public static readonly sbyte[][][] dyr = new sbyte[8][][]
	{
		new sbyte[4][]
		{
			new sbyte[3] { 0, -2, 0 },
			new sbyte[3] { 0, -2, 0 },
			new sbyte[3] { 0, -2, 2 },
			new sbyte[3] { 0, -2, 2 }
		},
		new sbyte[4][]
		{
			new sbyte[3] { 0, -2, 0 },
			new sbyte[3] { 0, -2, 0 },
			new sbyte[3] { 0, -2, 2 },
			new sbyte[3] { 0, -2, 2 }
		},
		new sbyte[4][]
		{
			new sbyte[3] { 0, -2, 0 },
			new sbyte[3] { 0, -2, 0 },
			new sbyte[3] { 0, -2, 2 },
			new sbyte[3] { 0, -2, 2 }
		},
		new sbyte[4][]
		{
			new sbyte[3] { 0, -2, 0 },
			new sbyte[3] { 0, -2, 0 },
			new sbyte[3] { 0, -2, 2 },
			new sbyte[3] { 0, -2, 2 }
		},
		new sbyte[4][]
		{
			new sbyte[4] { 0, -2, 0, -2 },
			new sbyte[4] { 0, -2, 0, -2 },
			new sbyte[4] { 0, -2, 0, -2 },
			new sbyte[4] { 0, -2, 0, -2 }
		},
		new sbyte[4][]
		{
			new sbyte[4] { -3, -2, 1, -2 },
			new sbyte[4] { 0, -2, 1, -2 },
			new sbyte[4] { 0, -2, 1, -2 },
			new sbyte[4] { 0, -2, 1, -2 }
		},
		new sbyte[4][]
		{
			new sbyte[3] { 0, -2, 0 },
			new sbyte[3] { 0, -2, 0 },
			new sbyte[3] { 0, -2, 2 },
			new sbyte[3] { 0, -2, 2 }
		},
		new sbyte[4][]
		{
			new sbyte[4] { -3, -2, 1, -2 },
			new sbyte[4] { 0, -2, 1, -2 },
			new sbyte[4] { 0, -2, 1, -2 },
			new sbyte[4] { 0, -2, 1, -2 }
		}
	};

	public static sbyte[][][] dxr = new sbyte[8][][]
	{
		new sbyte[4][]
		{
			new sbyte[3],
			new sbyte[3],
			new sbyte[3] { 0, -2, 0 },
			new sbyte[3] { 0, 2, 0 }
		},
		new sbyte[4][]
		{
			new sbyte[3],
			new sbyte[3],
			new sbyte[3] { 0, -2, 0 },
			new sbyte[3] { 0, 2, 0 }
		},
		new sbyte[4][]
		{
			new sbyte[3],
			new sbyte[3],
			new sbyte[3] { -2, -2, -2 },
			new sbyte[3] { 2, 2, 2 }
		},
		new sbyte[4][]
		{
			new sbyte[3],
			new sbyte[3],
			new sbyte[3] { -2, -2, -2 },
			new sbyte[3] { 2, 2, 2 }
		},
		new sbyte[4][]
		{
			new sbyte[4],
			new sbyte[4],
			new sbyte[4] { -4, -4, -4, -4 },
			new sbyte[4] { 2, 2, 2, 2 }
		},
		new sbyte[4][]
		{
			new sbyte[4],
			new sbyte[4],
			new sbyte[4] { -4, -4, -2, -4 },
			new sbyte[4] { 2, 2, 1, 2 }
		},
		new sbyte[4][]
		{
			new sbyte[3],
			new sbyte[3],
			new sbyte[3] { 0, -2, 0 },
			new sbyte[3] { 0, 2, 0 }
		},
		new sbyte[4][]
		{
			new sbyte[4],
			new sbyte[4],
			new sbyte[4] { -4, -4, -2, -4 },
			new sbyte[4] { 2, 2, 1, 2 }
		}
	};

	public static readonly sbyte[][] dxs = new sbyte[8][]
	{
		new sbyte[4] { 0, 0, -2, 2 },
		new sbyte[4] { 0, 0, -2, 2 },
		new sbyte[4] { 0, 0, -2, 2 },
		new sbyte[4] { 0, 0, -2, 2 },
		new sbyte[4] { 0, 0, -2, 2 },
		new sbyte[4] { 0, 0, -2, 2 },
		new sbyte[4] { 0, 0, -2, 2 },
		new sbyte[4] { 0, 0, -2, 2 }
	};

	private static readonly sbyte[][][] sizeHorse = new sbyte[8][][]
	{
		new sbyte[4][]
		{
			new sbyte[3] { 18, 18, 18 },
			new sbyte[3] { 17, 17, 17 },
			new sbyte[3] { 31, 34, 33 },
			new sbyte[3] { 31, 34, 33 }
		},
		new sbyte[4][]
		{
			new sbyte[3] { 20, 20, 20 },
			new sbyte[3] { 17, 17, 17 },
			new sbyte[3] { 41, 44, 44 },
			new sbyte[3] { 41, 44, 44 }
		},
		new sbyte[4][]
		{
			new sbyte[3] { 17, 17, 17 },
			new sbyte[3] { 16, 16, 16 },
			new sbyte[3] { 37, 42, 40 },
			new sbyte[3] { 37, 42, 40 }
		},
		new sbyte[4][]
		{
			new sbyte[3] { 17, 17, 17 },
			new sbyte[3] { 17, 17, 17 },
			new sbyte[3] { 41, 45, 43 },
			new sbyte[3] { 41, 45, 43 }
		},
		new sbyte[4][]
		{
			new sbyte[4] { 23, 27, 31, 27 },
			new sbyte[4] { 23, 27, 31, 27 },
			new sbyte[4] { 43, 43, 43, 43 },
			new sbyte[4] { 43, 43, 43, 43 }
		},
		new sbyte[4][]
		{
			new sbyte[4] { 30, 45, 48, 45 },
			new sbyte[4] { 30, 45, 48, 45 },
			new sbyte[4] { 60, 60, 60, 60 },
			new sbyte[4] { 60, 60, 60, 60 }
		},
		new sbyte[4][]
		{
			new sbyte[3] { 24, 24, 24 },
			new sbyte[3] { 21, 21, 21 },
			new sbyte[3] { 34, 34, 31 },
			new sbyte[3] { 31, 34, 34 }
		},
		new sbyte[4][]
		{
			new sbyte[4] { 30, 45, 48, 45 },
			new sbyte[4] { 30, 45, 48, 45 },
			new sbyte[4] { 60, 60, 60, 60 },
			new sbyte[4] { 60, 60, 60, 60 }
		}
	};

	public static sbyte[] hHorse = new sbyte[8] { 28, 26, 23, 27, 35, 45, 37, 45 };

	public sbyte useHorse = -1;

	public Effect_HAIRMOD_PROTECTED EffFace;

	private static int count = 0;

	private sbyte countNo;

	private int time;

	private int idLight;

	public short[] listEffect;

	public sbyte[] Tick;

	private static readonly sbyte[] lightFrame = new sbyte[6] { 0, 1, 2, 3, 2, 1 };

	private static readonly sbyte[][] skill_type = new sbyte[5][]
	{
		new sbyte[9] { 0, 1, 2, 0, 0, 2, 2, 2, 2 },
		new sbyte[9] { 0, 1, 2, 0, 0, 2, 2, 2, 2 },
		new sbyte[11]
		{
			0, 1, 2, 0, 0, 0, 2, 2, 2, 2,
			2
		},
		new sbyte[9] { 0, 1, 2, 0, 0, 2, 2, 2, 2 },
		new sbyte[9] { 0, 1, 2, 0, 0, 2, 2, 2, 2 }
	};

	public static WeaponInfo_HAIRMOD_PROTECTED[][][] imgWeapone = new WeaponInfo_HAIRMOD_PROTECTED[5][][];

	private static readonly sbyte[][] tWeapone = new sbyte[5][]
	{
		new sbyte[4] { 2, 0, 0, 0 },
		new sbyte[4] { 0, 2, 2, 0 },
		new sbyte[4] { 2, 0, 0, 2 },
		new sbyte[4] { 0, 2, 0, 0 },
		new sbyte[4] { 0, 2, 2, 0 }
	};

	private static readonly sbyte[][] pWeapone = new sbyte[5][]
	{
		new sbyte[28]
		{
			0, 0, 21, 21, 0, 0, 21, 21, 0, 0,
			21, 21, 0, 0, 21, 21, 0, 0, 21, 21,
			0, 0, 21, 21, 0, 0, 29, 14
		},
		new sbyte[28]
		{
			12, 12, 0, 0, 12, 12, 0, 0, 12, 12,
			0, 0, 12, 12, 0, 0, 12, 12, 0, 0,
			12, 12, 0, 0, 11, 11, 0, 0
		},
		new sbyte[28],
		new sbyte[28]
		{
			15, 15, 38, 0, 15, 15, 38, 0, 15, 15,
			38, 0, 15, 15, 38, 0, 15, 15, 38, 0,
			15, 15, 38, 0, 16, 16, 43, 0
		},
		new sbyte[28]
		{
			14, 14, 0, 0, 15, 15, 0, 0, 15, 15,
			0, 0, 15, 15, 0, 0, 16, 16, 0, 0,
			19, 19, 0, 0, 21, 21, 0, 0
		}
	};

	private static readonly sbyte[][] sw = new sbyte[5][]
	{
		new sbyte[28]
		{
			21, 21, 11, 11, 21, 21, 11, 11, 21, 21,
			11, 11, 21, 21, 11, 11, 21, 21, 11, 11,
			21, 21, 11, 11, 14, 14, 5, 15
		},
		new sbyte[28]
		{
			19, 19, 9, 11, 19, 19, 9, 11, 19, 19,
			9, 11, 19, 19, 9, 11, 20, 20, 12, 12,
			19, 19, 9, 11, 20, 20, 12, 12
		},
		new sbyte[28]
		{
			14, 14, 14, 14, 14, 14, 14, 14, 14, 14,
			14, 14, 14, 14, 14, 14, 15, 15, 15, 15,
			14, 14, 14, 14, 15, 15, 15, 15
		},
		new sbyte[28]
		{
			24, 24, 8, 15, 24, 24, 8, 15, 24, 24,
			8, 15, 24, 24, 8, 15, 24, 24, 8, 15,
			24, 24, 8, 15, 24, 24, 8, 15
		},
		new sbyte[28]
		{
			21, 21, 14, 14, 21, 21, 15, 15, 21, 21,
			15, 15, 21, 21, 15, 15, 22, 22, 16, 16,
			21, 21, 15, 15, 26, 26, 20, 20
		}
	};

	private static readonly sbyte[][] dwx = new sbyte[5][]
	{
		new sbyte[28]
		{
			0, 0, -6, 0, 0, 0, -6, 0, 0, 0,
			-6, 0, 0, 0, -6, 0, 0, 0, -6, 0,
			0, 0, -6, 0, 6, 2, 0, 1
		},
		new sbyte[28]
		{
			0, 0, -5, 0, 0, 0, -5, 0, 0, 0,
			-5, 0, 0, 0, -5, 0, 0, 0, -7, 0,
			0, 0, -5, 0, 0, 0, -7, 0
		},
		new sbyte[28],
		new sbyte[28],
		new sbyte[28]
		{
			0, 0, 0, 0, 0, 0, -1, 0, 0, 0,
			-1, 0, 0, 0, -1, 0, 0, 0, -1, 0,
			3, -2, 1, 1, 0, -2, -2, -2
		}
	};

	private static readonly sbyte[][] dwy = new sbyte[5][]
	{
		new sbyte[28]
		{
			0, 0, 1, 0, 0, 0, 1, 0, 0, 0,
			1, 0, 0, 0, 1, 0, 0, 0, 1, 0,
			0, 0, 1, 0, 0, 0, 1, 0
		},
		new sbyte[28]
		{
			0, 0, -3, 0, 0, 0, -3, 0, 0, 0,
			-3, 0, 0, 0, -3, 0, 0, 0, -3, 0,
			0, 0, -3, 0, 0, 0, -3, 0
		},
		new sbyte[28],
		new sbyte[28],
		new sbyte[28]
	};

	private static readonly sbyte[] hwp = new sbyte[5] { 21, 40, 14, 28, 20 };

	private static readonly sbyte[] fixY = new sbyte[5] { 0, -1, 0, 0, 0 };

	private static sbyte[][] countX = new sbyte[5][]
	{
		new sbyte[4] { 7, -15, 7, -12 },
		new sbyte[4] { 6, -14, -4, -1 },
		new sbyte[4] { -12, 3, 8, -12 },
		new sbyte[4] { 9, -15, 7, -10 },
		new sbyte[4] { -14, -13, 7, -11 }
	};

	private static sbyte[][] countY = new sbyte[5][]
	{
		new sbyte[4] { -12, -8, -10, -1 },
		new sbyte[4] { -12, -8, -15, -11 },
		new sbyte[4] { -6, -6, -6, -6 },
		new sbyte[4] { -12, -5, -19, 0 },
		new sbyte[4] { -18, 3, 0, -4 }
	};

	public static sbyte[][] dx_horse = new sbyte[1][] { new sbyte[4] { 0, 0, 7, -7 } };

	public static sbyte[][] dy_horse = new sbyte[1][] { new sbyte[4] { -20, -15, -15, -15 } };

	public static sbyte[][] Dx_Dy_WP = new sbyte[2][]
	{
		new sbyte[1],
		new sbyte[1]
	};

	public static sbyte[][] Dx_Dy_PP = new sbyte[2][]
	{
		new sbyte[1],
		new sbyte[1]
	};

	public static sbyte[][] Dx_Dy_AVT = new sbyte[2][]
	{
		new sbyte[1],
		new sbyte[1]
	};

	public byte FrameHair;

	public byte Framebody;

	public byte frameLeg;

	public byte framePP;

	public sbyte FrameWP;

	private byte ppAvartar;

	private byte wpAvartar;

	private byte AVT;

	public static int[][] mainDef = new int[5][]
	{
		new int[5],
		new int[5] { 2, 0, 0, 3, 0 },
		new int[5],
		new int[5],
		new int[5] { 0, 4, 0, 0, 0 }
	};

	public static int[][] mainAt = new int[5][]
	{
		new int[5],
		new int[5],
		new int[5],
		new int[5],
		new int[5] { 1, 0, 0, 0, 0 }
	};

	public static int[][] pcAttack = new int[5][]
	{
		new int[5] { 0, -25, 0, 25, 15 },
		new int[5] { 10, 0, -15, 10, -10 },
		new int[5] { 5, 0, 0, -35, -5 },
		new int[5] { -20, 5, 35, 0, -10 },
		new int[5] { 80, -5, 5, 30, 0 }
	};

	public static int[][] pcDef = new int[5][]
	{
		new int[5] { 0, -5, -15, 20, 0 },
		new int[5] { -90, 0, -15, 40, -35 },
		new int[5] { 0, 0, 0, -5, -15 },
		new int[5] { -20, 10, 5, 0, -10 },
		new int[5] { -25, 0, 0, 20, 0 }
	};

	private bool xuyengiap;

	private sbyte Frameeff;

	public string infoQuest = string.Empty;

	public static readonly sbyte[] BUFFTYPE = new sbyte[7] { 19, 20, 22, 23, 24, 25, 27 };

	public Char_HAIRMOD_PROTECTED()
	{
		catagory = 0;
		speed = char_speed;
		upSpeed = 0;
		centerX = 12;
		centerY = 12;
		height = 32;
		width = 40;
		hp = 1;
		realHP = 1;
		dir = (short)LiveActor_HAIRMOD_PROTECTED.r.nextInt(4);
		timeDelayRequestCharInfo = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
		isDie = false;
	}

	public override bool isPaint()
	{
		if (x < GameScr_HAIRMOD_PROTECTED.cmx)
		{
			return false;
		}
		if (x > GameScr_HAIRMOD_PROTECTED.cmx + Canvas_HAIRMOD_PROTECTED.w)
		{
			return false;
		}
		if (y < GameScr_HAIRMOD_PROTECTED.cmy)
		{
			return false;
		}
		if (y > GameScr_HAIRMOD_PROTECTED.cmy + Canvas_HAIRMOD_PROTECTED.h + 30)
		{
			return false;
		}
		return true;
	}

	public sbyte[] getOrderPaint(int dir, int typeAnimal)
	{
		bool flag = dir != 1 && frame < 4;
		return ORDER_PAINT[(!flag) ? 1u : 0u][dir];
	}

	public bool canPaint()
	{
		if (bodyStyle == -1 || legStyle == -1 || currentHead == -1)
		{
			return false;
		}
		return true;
	}

	public int getIdPartPaint(int type_part)
	{
		int result = -1;
		if (!isLoadAnimalOk())
		{
			switch (type_part)
			{
			case 1:
				if (bodyStyle != -1)
				{
					result = bodyStyle;
					if (idModel[2] != -1)
					{
						result = idModel[2];
					}
				}
				break;
			case 0:
			{
				sbyte horse = getHorse();
				if (legStyle != -1 && horse == -1)
				{
					result = legStyle;
					if (idModel[3] != -1)
					{
						result = idModel[3];
					}
				}
				break;
			}
			case 2:
				if (currentHead != -1)
				{
					result = currentHead;
					if (idModel[0] != -1)
					{
						result = idModel[0];
					}
				}
				break;
			case 3:
				if (idModel[1] != -1)
				{
					hatStyle = idModel[1];
				}
				if (hatStyle != -1)
				{
					result = hatStyle;
					if (idModel[1] != -1)
					{
						result = idModel[1];
					}
				}
				break;
			case 4:
				if (coatStyle > -1)
				{
					result = coatStyle;
				}
				break;
			}
		}
		return result;
	}

	public int getDyAnimal(int type, int dir, int typeAnimal)
	{
		if (idhorse != -1)
		{
			return getdyParHorse();
		}
		int result = 0;
		sbyte horse = getHorse();
		switch (type)
		{
		case 0:
			result = z + dy;
			break;
		case 1:
			result = ((horse <= -1) ? (z + dy) : ((typeAnimal == 0) ? (z + (dyb[horse][dir] + dyr[horse][dir][ifHorse]) + dy) : ((dir >= 2) ? (z + (dyb[horse][dir] + dyr[horse][dir][ifHorse]) + dy) : (z + (dyb[horse][dir] - ((dir == 1) ? 5 : 0) + dyr[horse][dir][ifHorse]) + dy))));
			break;
		case 2:
			result = z + ((horse != -1) ? (dyb[horse][dir] - ((dir == 1 && anmFly == 1) ? 5 : 0) + dyr[horse][dir][ifHorse]) : 0) + dy;
			break;
		case 3:
			result = z + ((horse != -1) ? (dyb[horse][dir] + dyr[horse][dir][ifHorse]) : 0) - ((dir == 1 && anmFly == 1) ? 5 : 0) + dy;
			break;
		case 4:
			result = z + ((horse != -1) ? (dyb[horse][dir] + dyr[horse][dir][ifHorse]) : 0) + dy;
			break;
		}
		return result;
	}

	public int getdxHorse()
	{
		if (typehorse != -1)
		{
			return dx_horse[typehorse][dir];
		}
		return 0;
	}

	public int getDxAnimal(int type, int dir, int typeAnimal)
	{
		if (idhorse != -1)
		{
			return getdxHorse();
		}
		int result = 0;
		sbyte horse = getHorse();
		switch (type)
		{
		case 1:
			if (horse > -1 && dir > 1)
			{
				result = dxr[horse][dir][ifHorse];
			}
			break;
		case 2:
			result = ((horse != -1) ? dxr[horse][dir][ifHorse] : 0);
			break;
		case 3:
			result = ((horse != -1) ? dxr[horse][dir][ifHorse] : 0);
			break;
		case 4:
			result = ((horse != -1) ? dxr[horse][dir][ifHorse] : 0);
			break;
		}
		return result;
	}

	public int getFrameHorse()
	{
		if (dir == DOWN)
		{
			return Fhorse + frameThuCuoi * 21;
		}
		if (dir == UP)
		{
			return Fhorse + 7 + frameThuCuoi * 21;
		}
		return Fhorse + 14 + frameThuCuoi * 21;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		infoPaint[2] = currentHead;
		if (!isPaint())
		{
			return;
		}
		bool isHorse = false;
		if (idhorse > -1 || useHorse != -1)
		{
			isHorse = true;
		}
		try
		{
			if (!isFreeze)
			{
				sbyte b = getHorse();
				if (GameData_HAIRMOD_PROTECTED.imgHorse == null)
				{
					b = -1;
				}
				if (effPhap != null)
				{
					effPhap.x = x;
					effPhap.y = y;
					if (!isLoadAnimalOk() && b != -1)
					{
						effPhap.x += dxs[b][dir];
						effPhap.y -= ((anmFly != 0) ? (-6) : 3);
					}
					effPhap.paint(g);
				}
				if (state == 3)
				{
					g.drawImage(Res_HAIRMOD_PROTECTED.imgDie, x - 10, y - 27 + dy, 0);
					if (Canvas_HAIRMOD_PROTECTED.gameTick % 20 != 17 && Canvas_HAIRMOD_PROTECTED.gameTick % 20 != 13)
					{
						g.drawImage(Res_HAIRMOD_PROTECTED.imgEye, x - 4, y - 19 + dy, 0);
					}
					return;
				}
				paintDataEff_Top(g, x, y - ((anmFly != 0) ? 6 : 0) + getdyParHorse(), isHorse);
				paintListEff(g, 0);
				bool flag = (dir == DOWN || dir == LEFT || dir == RIGHT) && frame < 4;
				int num = 0;
				int f = 0;
				if (!isLoadAnimalOk() && can_Paint() && b != -1)
				{
					z = 0;
					if (anmFly == 1)
					{
						sbyte[] array = new sbyte[4] { -1, -1, -5, -1 };
						num = array[ifHorse];
						array = new sbyte[4] { -1, -2, -3, -2 };
						f = array[ifHorse];
					}
				}
				sbyte[] orderPaint = getOrderPaint(dir, anmFly);
				if (!isLoadAnimalOk())
				{
					for (int i = 0; i < orderPaint.Length; i++)
					{
						if (getIdPartPaint(orderPaint[i]) <= -1 && (orderPaint[i] != 4 || idPhiPhong == -1) && (orderPaint[i] != 5 || infoPaint[orderPaint[i]] <= -1) && idWeapon == -1)
						{
							continue;
						}
						if (orderPaint[i] == 5)
						{
							paintWeapon(g, f);
							continue;
						}
						if (orderPaint[i] == 0 && dynamicEffBottom != null)
						{
							for (int j = 0; j < dynamicEffBottom.size(); j++)
							{
								((DynamicEffect_HAIRMOD_PROTECTED)dynamicEffBottom.elementAt(j)).paint(g, x, y);
							}
						}
						if (orderPaint[i] == 0 && idhorse != -1)
						{
							continue;
						}
						if (orderPaint[i] == 2 && idHair != -1)
						{
							paintHair(g, num);
						}
						else if (orderPaint[i] == 0 && idLeg != -1)
						{
							paintLeg(g, f);
						}
						else if (orderPaint[i] == 4 && idPhiPhong != -1)
						{
							paintPP(g, f);
						}
						else if (orderPaint[i] == 1)
						{
							DataSkillEff_HAIRMOD_PROTECTED partEff = getPartEff(idhorse);
							if (partEff != null)
							{
								if (dir == LEFT || dir == RIGHT || dir == DOWN)
								{
									if (idBody == -1)
									{
										Res_HAIRMOD_PROTECTED.getCharPartInfo(orderPaint[i], getIdPartPaint(orderPaint[i])).paint(g, x + getDxAnimal(orderPaint[i], dir, anmFly), num + y + getDyAnimal(orderPaint[i], dir, anmFly), dir, frame);
									}
									else
									{
										paintBody(g, f);
									}
								}
								partEff.paintTopHorse(g, x, y, getFrameHorse(), (dir == RIGHT) ? 2 : 0);
								partEff.paintBottomHorse(g, x, y, getFrameHorse(), (dir == RIGHT) ? 2 : 0);
								if (dir == UP)
								{
									if (idBody == -1)
									{
										Res_HAIRMOD_PROTECTED.getCharPartInfo(orderPaint[i], getIdPartPaint(orderPaint[i])).paint(g, x + getDxAnimal(orderPaint[i], dir, anmFly), num + y + getDyAnimal(orderPaint[i], dir, anmFly), dir, frame);
									}
									else
									{
										paintBody(g, f);
									}
								}
							}
							if (b > -1)
							{
								if (dir < 2 && anmFly == 0)
								{
									if (idBody == -1)
									{
										Res_HAIRMOD_PROTECTED.getCharPartInfo(orderPaint[i], getIdPartPaint(orderPaint[i])).paint(g, x + getDxAnimal(orderPaint[i], dir, anmFly), num + y + getDyAnimal(orderPaint[i], dir, anmFly), dir, frame);
									}
									else
									{
										paintBody(g, f);
									}
								}
								g.drawImage(Res_HAIRMOD_PROTECTED.imgShadow, x + dxs[b][dir], y - ((anmFly != 0) ? (-6) : 3), MyGraphics_HAIRMOD_PROTECTED.HCENTER | MyGraphics_HAIRMOD_PROTECTED.VCENTER);
								if (GameData_HAIRMOD_PROTECTED.imgHorse.size() > 0)
								{
									g.drawRegion((Image_HAIRMOD_PROTECTED)GameData_HAIRMOD_PROTECTED.imgHorse.elementAt(idImgHorse), fHorse[b][dir][ifHorse], 0, sizeHorse[b][dir][ifHorse], hHorse[b], 0, x + dxh[b][dir], y + dyh[b][dir] + dy, MyGraphics_HAIRMOD_PROTECTED.HCENTER | MyGraphics_HAIRMOD_PROTECTED.BOTTOM);
								}
								if (dir > 1 && anmFly == 0)
								{
									g.drawImage(Res_HAIRMOD_PROTECTED.imgLeg[dir - 2], x - ((dir != 2) ? 1 : (-2)) + dxr[b][dir][ifHorse], y - 10 + dyr[b][dir][ifHorse] + dy, 3);
								}
								if ((dir > 1 && anmFly == 0) || anmFly == 1)
								{
									if (idBody == -1)
									{
										Res_HAIRMOD_PROTECTED.getCharPartInfo(orderPaint[i], getIdPartPaint(orderPaint[i])).paint(g, x + getDxAnimal(orderPaint[i], dir, anmFly), num + y + getDyAnimal(orderPaint[i], dir, anmFly), dir, frame);
									}
									else
									{
										paintBody(g, f);
									}
								}
							}
							else if (idhorse == -1)
							{
								if (idBody == -1)
								{
									Res_HAIRMOD_PROTECTED.getCharPartInfo(orderPaint[i], getIdPartPaint(orderPaint[i])).paint(g, x + getDxAnimal(orderPaint[i], dir, anmFly), num + y + getDyAnimal(orderPaint[i], dir, anmFly), dir, frame);
								}
								else
								{
									paintBody(g, f);
								}
							}
						}
						else
						{
							bool flag2 = true;
							if (orderPaint[i] == 3)
							{
								flag2 = paintHat;
							}
							if (flag2)
							{
								Res_HAIRMOD_PROTECTED.getCharPartInfo(orderPaint[i], getIdPartPaint(orderPaint[i])).paint(g, x + getDxAnimal(orderPaint[i], dir, anmFly), num + y + getDyAnimal(orderPaint[i], dir, anmFly), dir, frame);
							}
						}
					}
				}
				if (isLoadAnimalOk() && myAnimal != null)
				{
					myAnimal.paint(g, x, y + z + dy);
					g.drawImage(Res_HAIRMOD_PROTECTED.imgShadow, x, y - 2, MyGraphics_HAIRMOD_PROTECTED.HCENTER | MyGraphics_HAIRMOD_PROTECTED.VCENTER);
				}
				if (isAnimal && effAnimal != null)
				{
					effAnimal.x = x;
					effAnimal.y = y;
					effAnimal.paint(g);
				}
				if (comeHome)
				{
					g.setColor(0);
					g.fillRect(x - 16, y - 46, 32, 4);
					g.setColor(1165236);
					g.fillRect(x - 16, y - 46, time, 4);
					g.setColor(3363085);
					g.drawRect(x - 16, y - 46, 32, 4);
				}
				paintbuff(g);
			}
		}
		catch (Exception)
		{
		}
		base.paint(g);
		if (EffFace != null && !isFreeze)
		{
			EffFace.paint(g);
		}
		if (isNPC())
		{
			Image_HAIRMOD_PROTECTED imgQuest = GameScr_HAIRMOD_PROTECTED.getImgQuest(idBoss);
			if (!imgQuest.Equals(Res_HAIRMOD_PROTECTED.imgSelect))
			{
				g.drawImage(imgQuest, x, y - height - 5, MyGraphics_HAIRMOD_PROTECTED.HCENTER | MyGraphics_HAIRMOD_PROTECTED.BOTTOM);
			}
		}
		if (dynamicEffTop != null)
		{
			for (int k = 0; k < dynamicEffTop.size(); k++)
			{
				((DynamicEffect_HAIRMOD_PROTECTED)dynamicEffTop.elementAt(k)).paint(g, x, y);
			}
		}
		paintDataEff_Bot(g, x, y - ((anmFly != 0) ? 6 : 0) + getdyParHorse(), isHorse);
		paintListEff(g, 0);
	}

	public void paintAvatar(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (!isFreeze)
		{
			sbyte b = getHorse();
			if (name.ToLower().Equals("loivu") && GameData_HAIRMOD_PROTECTED.imgHorse == null)
			{
				b = -1;
			}
			if (effPhap != null)
			{
				effPhap.x = x;
				effPhap.y = y;
				if (!isLoadAnimalOk() && b != -1)
				{
					effPhap.x += dxs[b][dir];
					effPhap.y -= ((anmFly != 0) ? (-6) : 3);
				}
				effPhap.paint(g);
			}
			if (state == 3)
			{
				g.drawImage(Res_HAIRMOD_PROTECTED.imgDie, x - 10, y - 27 + dy, 0);
				if (Canvas_HAIRMOD_PROTECTED.gameTick % 20 != 17 && Canvas_HAIRMOD_PROTECTED.gameTick % 20 != 13)
				{
					g.drawImage(Res_HAIRMOD_PROTECTED.imgEye, x - 4, y - 19 + dy, 0);
				}
				return;
			}
			bool flag = (dir == 0 || dir == 2 || dir == 3) && frame < 4;
			int num = 0;
			int f = 0;
			if (!isLoadAnimalOk() && b != -1)
			{
				z = 0;
				if (anmFly == 1)
				{
					sbyte[] array = new sbyte[4] { -1, -1, -5, -1 };
					num = array[ifHorse];
					array = new sbyte[4] { -1, -2, -3, -2 };
					f = array[ifHorse];
				}
			}
			if (!isLoadAnimalOk() && flag)
			{
				paintWeapon(g, f);
			}
			if (!isLoadAnimalOk() && legStyle != -1 && b == -1)
			{
				int id = legStyle;
				if (idModel[3] != -1)
				{
					id = idModel[3];
				}
				Res_HAIRMOD_PROTECTED.getCharPartInfo(0, id).paint(g, x, y + z + dy, dir, frame);
			}
			if (!isLoadAnimalOk() && bodyStyle != -1)
			{
				int id2 = bodyStyle;
				if (idModel[2] != -1)
				{
					id2 = idModel[2];
				}
				if (b != -1)
				{
					if (dir < 2 && anmFly == 0)
					{
						Res_HAIRMOD_PROTECTED.getCharPartInfo(1, id2).paint(g, x, num + y + z + (dyb[b][dir] + dyr[b][dir][ifHorse]) + dy, dir, frame);
					}
					g.drawImage(Res_HAIRMOD_PROTECTED.imgShadow, x + dxs[b][dir], y - ((anmFly != 0) ? (-6) : 3), MyGraphics_HAIRMOD_PROTECTED.HCENTER | MyGraphics_HAIRMOD_PROTECTED.VCENTER);
					if (GameData_HAIRMOD_PROTECTED.imgHorse.size() > 0)
					{
						g.drawRegion((Image_HAIRMOD_PROTECTED)GameData_HAIRMOD_PROTECTED.imgHorse.elementAt(idImgHorse), fHorse[b][dir][ifHorse], 0, sizeHorse[b][dir][ifHorse], hHorse[b], 0, x + dxh[b][dir], y + dyh[b][dir] + dy, MyGraphics_HAIRMOD_PROTECTED.HCENTER | MyGraphics_HAIRMOD_PROTECTED.BOTTOM);
					}
					if (dir > 1 && anmFly == 0)
					{
						g.drawImage(Res_HAIRMOD_PROTECTED.imgLeg[dir - 2], x - ((dir != 2) ? 1 : (-2)) + dxr[b][dir][ifHorse], y - 10 + dyr[b][dir][ifHorse] + dy, 3);
					}
					if (dir > 1 && anmFly == 0)
					{
						Res_HAIRMOD_PROTECTED.getCharPartInfo(1, id2).paint(g, x + dxr[b][dir][ifHorse], num + y + z + (dyb[b][dir] + dyr[b][dir][ifHorse]) + dy, dir, frame);
					}
					else if (anmFly == 1)
					{
						if (dir < 2)
						{
							Res_HAIRMOD_PROTECTED.getCharPartInfo(1, id2).paint(g, x, num + y + z + (dyb[b][dir] - ((dir == 1 && anmFly == 1) ? 5 : 0) + dyr[b][dir][ifHorse]) + dy, dir, frame);
						}
						else
						{
							Res_HAIRMOD_PROTECTED.getCharPartInfo(1, id2).paint(g, x + dxr[b][dir][ifHorse], num + y + z + (dyb[b][dir] + dyr[b][dir][ifHorse]) + dy, dir, frame);
						}
					}
				}
				else if (!isLoadAnimalOk())
				{
					Res_HAIRMOD_PROTECTED.getCharPartInfo(1, id2).paint(g, x, num + y + z + dy, dir, frame);
				}
			}
			if (!isLoadAnimalOk() && currentHead != -1)
			{
				int id3 = currentHead;
				if (idModel[0] != -1)
				{
					id3 = idModel[0];
				}
				Res_HAIRMOD_PROTECTED.getCharPartInfo(2, id3).paint(g, x + ((b != -1) ? dxr[b][dir][ifHorse] : 0), num + y + z + ((b != -1) ? (dyb[b][dir] - ((dir == 1 && anmFly == 1) ? 5 : 0) + dyr[b][dir][ifHorse]) : 0) + dy, dir, frame);
			}
			if (!isLoadAnimalOk())
			{
				if (idModel[1] != -1)
				{
					hatStyle = idModel[1];
				}
				if (hatStyle != -1)
				{
					int id4 = hatStyle;
					if (idModel[1] != -1)
					{
						id4 = idModel[1];
					}
					if ((idModel[0] == 26 && idModel[1] == 50) || idModel[0] != 26)
					{
						Res_HAIRMOD_PROTECTED.getCharPartInfo(3, id4).paint(g, x + ((b != -1) ? dxr[b][dir][ifHorse] : 0), num + y + z + ((b != -1) ? (dyb[b][dir] + dyr[b][dir][ifHorse]) : 0) + dy, dir, frame);
					}
				}
			}
			if (!isLoadAnimalOk() && !flag)
			{
				paintWeapon(g, f);
			}
			if (isLoadAnimalOk() && myAnimal != null)
			{
				myAnimal.paint(g, x, y + z + dy);
				g.drawImage(Res_HAIRMOD_PROTECTED.imgShadow, x, y - 2, MyGraphics_HAIRMOD_PROTECTED.HCENTER | MyGraphics_HAIRMOD_PROTECTED.VCENTER);
			}
			if (isAnimal && effAnimal != null)
			{
				effAnimal.x = x;
				effAnimal.y = y;
				effAnimal.paint(g);
			}
			if (comeHome)
			{
				g.setColor(0);
				g.fillRect(x - 16, y - 46, 32, 4);
				g.setColor(1165236);
				g.fillRect(x - 16, y - 46, time, 4);
				g.setColor(3363085);
				g.drawRect(x - 16, y - 46, 32, 4);
			}
			paintbuff(g);
		}
		base.paint(g);
		if (EffFace != null && !isFreeze)
		{
			EffFace.paint(g);
		}
	}

	public override void paintName(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (Tilemap_HAIRMOD_PROTECTED.isOfflineMap)
		{
			return;
		}
		if (name != null && Canvas_HAIRMOD_PROTECTED.gameScr.hideGUI != 2)
		{
			int num = -((getHorse() == -1 || GameData_HAIRMOD_PROTECTED.imgHorse == null) ? 2 : 10);
			int num2 = 0;
			if (idhorse != -1)
			{
				num = dy_horse[0][dir];
			}
			if (idClan != -1)
			{
				num2 = 8;
				ImageIcon_HAIRMOD_PROTECTED imgIcon = GameData_HAIRMOD_PROTECTED.getImgIcon((short)(idClan + 1000));
				int num3 = 14;
				if (imgIcon != null)
				{
					if (!imgIcon.isLoad)
					{
						g.drawImage(imgIcon.img, x - wName / 2 - 6, y - height + num - 2, 3);
						num3 = imgIcon.img.getWidth();
					}
					if (isMaster >= 0 && isMaster < 3)
					{
						int[][] array = new int[3][]
						{
							new int[2] { 16450576, 14548228 },
							new int[2] { 14548228, 16450576 },
							new int[2] { 1049853, 16450576 }
						};
						Res_HAIRMOD_PROTECTED.paintRectColor(g, x - wName / 2 - num3 / 2 - 8, y - height + num - num3 / 2 - 3, num3 + 2, num3 + 2, count, array[isMaster][0], array[isMaster][1]);
						count += 3;
						if (count > 46)
						{
							count = 0;
						}
					}
				}
			}
			if (nationID > -1 && idBoss == -1 && (nationID != inCountry || ID == Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID) && (nationID != inCountry || ID == Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID))
			{
				g.drawRegion(Res_HAIRMOD_PROTECTED.imgNation, 0, nationID * 11, 11, 11, 0, x + wName / 2 + 10, y - height - 8 + num, 20);
			}
			if (ID == Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID || nationID == Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.nationID || idBoss != -1)
			{
				MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, name, x + num2, y - 40 + num - dyThanthu, 2);
			}
			else
			{
				MFont_HAIRMOD_PROTECTED.smallFontColor[4].drawString(g, name, x + num2, y - 40 + num - dyThanthu, 2);
			}
			int num4 = 0;
			int num5 = 0;
			if (isNo)
			{
				num5 = 5;
			}
			if (pk > 0)
			{
				num4 = -5;
				g.drawRegion(imgFlgPk, 0, (pk - 14) * 10, 11, 10, 0, x + num5, y - 41 + num, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
			}
			else if (killer > 0 || iskiller)
			{
				num4 = -5;
				g.drawRegion(imgFlgPk, 0, 50, 11, 10, 0, x + num5, y - 41 + num, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
			}
			if (isNo)
			{
				Res_HAIRMOD_PROTECTED.imgNo.drawFrame(countNo / 5, x + num4, y - 41 + num - 2, 0, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER, g);
			}
			countNo++;
			if (countNo >= 10)
			{
				countNo = 0;
			}
		}
		base.paintName(g);
	}

	public override void setPosTo(short x, short y)
	{
		moveTo(x, y);
	}

	public void resetQuest()
	{
		nextNpc = 0;
		questID = -1;
		idQuestNPC = -1;
		receiveQuest = false;
		finishQuest = false;
		info_npc_quest = null;
		info_response = null;
		typeQuest = -1;
		monster_kill = null;
		npc_response = null;
		npc_quest = null;
	}

	public void cancelQuest()
	{
		nextNpc = 0;
		idQuestNPC = -1;
		receiveQuest = false;
		finishQuest = false;
		info_npc_quest = null;
		info_response = null;
		typeQuest = -1;
		monster_kill = null;
		npc_response = null;
		npc_quest = null;
	}

	public bool isFullInventory()
	{
		return FullInventory() > GameScr_HAIRMOD_PROTECTED.MAX_INVENTORY * Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.limItemBag;
	}

	public int FullInventory()
	{
		return inventory.size() + MainChar_HAIRMOD_PROTECTED.gemItem.size() + getCountPotion() + MainChar_HAIRMOD_PROTECTED.shopItem.size();
	}

	public sbyte getnSkill()
	{
		return nSkill[clazz];
	}

	public string getGoldTime()
	{
		string result = string.Empty;
		int num = goldTimeMinute * 60000;
		long num2 = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() - timeUseGolgTicket;
		long num3 = num - num2;
		if (num3 > 0)
		{
			long num4 = num3 / 3600000;
			long num5 = num3 % 3600000 / 1000 / 60;
			result = num4 + " : " + num5;
			if (num4 == 0L && num5 == 0)
			{
				result = string.Empty;
			}
		}
		return result;
	}

	public bool isLKD(int idTemplate)
	{
		return idTemplate == 8 || idTemplate == 9 || idTemplate == 10 || idTemplate == 11 || idTemplate == 66 || idTemplate == 245 || idTemplate == 157 || idTemplate == 158;
	}

	public int countLKD()
	{
		int num = 0;
		for (int i = 0; i < MainChar_HAIRMOD_PROTECTED.gemitemImbue.size(); i++)
		{
			RealID_HAIRMOD_PROTECTED realID_HAIRMOD_PROTECTED = (RealID_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemitemImbue.elementAt(i);
			if ((realID_HAIRMOD_PROTECTED.ID > 7 && realID_HAIRMOD_PROTECTED.ID < 12) || realID_HAIRMOD_PROTECTED.ID == 158 || realID_HAIRMOD_PROTECTED.ID == 157)
			{
				num++;
			}
		}
		return num;
	}

	public int countBaoHiem()
	{
		int num = 0;
		for (int i = 0; i < MainChar_HAIRMOD_PROTECTED.gemitemImbue.size(); i++)
		{
			RealID_HAIRMOD_PROTECTED realID_HAIRMOD_PROTECTED = (RealID_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemitemImbue.elementAt(i);
			if (realID_HAIRMOD_PROTECTED.ID < 5)
			{
				num++;
			}
		}
		return num;
	}

	public bool haveItemImbue()
	{
		for (int i = 0; i < MainChar_HAIRMOD_PROTECTED.gemItem.size(); i++)
		{
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemItem.elementAt(i);
			if ((gemTemplate_HAIRMOD_PROTECTED.id > 7 && gemTemplate_HAIRMOD_PROTECTED.id < 12) || gemTemplate_HAIRMOD_PROTECTED.id == 66 || gemTemplate_HAIRMOD_PROTECTED.id == 245 || gemTemplate_HAIRMOD_PROTECTED.id == 158 || gemTemplate_HAIRMOD_PROTECTED.id == 157)
			{
				return true;
			}
		}
		return false;
	}

	public bool hadPutItemImbue()
	{
		for (int i = 0; i < MainChar_HAIRMOD_PROTECTED.gemitemImbue.size(); i++)
		{
			RealID_HAIRMOD_PROTECTED realID_HAIRMOD_PROTECTED = (RealID_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemitemImbue.elementAt(i);
			if ((realID_HAIRMOD_PROTECTED.ID > 7 && realID_HAIRMOD_PROTECTED.ID < 12) || realID_HAIRMOD_PROTECTED.ID == 158 || realID_HAIRMOD_PROTECTED.ID == 66 || realID_HAIRMOD_PROTECTED.ID == 157 || (realID_HAIRMOD_PROTECTED.ID >= 12 && realID_HAIRMOD_PROTECTED.ID <= 66))
			{
				return true;
			}
		}
		return false;
	}

	public int isExistInvector(mVector_HAIRMOD_PROTECTED v, ItemInInventory_HAIRMOD_PROTECTED cii)
	{
		for (int i = 0; i < v.size(); i++)
		{
			if (v.elementAt(i) is ItemInInventory_HAIRMOD_PROTECTED)
			{
				ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)v.elementAt(i);
				if (itemInInventory_HAIRMOD_PROTECTED.itemID == cii.itemID)
				{
					return i;
				}
			}
		}
		return -1;
	}

	public new void dead()
	{
		state = 3;
	}

	public override string getDisplayName()
	{
		return (name != null) ? name : string.Empty;
	}

	public override void paintCorner(MyGraphics_HAIRMOD_PROTECTED g, int xCorner, int yCorner)
	{
		g.translate(-x + xCorner + 2, -y + yCorner - 10);
		if (currentHead != -1)
		{
			Res_HAIRMOD_PROTECTED.getCharPartInfo(2, currentHead).paintStatic(g, x, (short)(y + ((hatStyle != -1) ? 5 : 0)), dir, frame);
		}
		if (hatStyle != -1)
		{
			Res_HAIRMOD_PROTECTED.getCharPartInfo(3, hatStyle).paintStatic(g, x, y, dir, frame);
		}
	}

	public virtual void moveTo(short xTo, short yTo)
	{
		if (!FuntioncanMove())
		{
			return;
		}
		this.xTo = xTo;
		this.yTo = yTo;
		if (x == xTo && y == yTo)
		{
			stand = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
			if (state != 2 && state != 3)
			{
				state = 0;
			}
		}
		else
		{
			state = 1;
		}
	}

	public override void updateBuff()
	{
		for (int i = 0; i < buff.size(); i++)
		{
			CharBuff_HAIRMOD_PROTECTED charBuff_HAIRMOD_PROTECTED = (CharBuff_HAIRMOD_PROTECTED)buff.elementAt(i);
			charBuff_HAIRMOD_PROTECTED.update();
			if (charBuff_HAIRMOD_PROTECTED.wantDetroy)
			{
				buffType[i] = -1;
				lvBuff[i] = -1;
				buff.removeElementAt(i);
			}
			else
			{
				charBuff_HAIRMOD_PROTECTED.setXY(x, (!charBuff_HAIRMOD_PROTECTED.isCenter()) ? y : (y - 12));
			}
		}
	}

	public void removeBuff(int id)
	{
		for (int i = 0; i < buff.size(); i++)
		{
			if (((CharBuff_HAIRMOD_PROTECTED)buff.elementAt(i)).type == id)
			{
				buff.removeElementAt(i);
				break;
			}
		}
	}

	public bool isExistBuff(int id)
	{
		for (int i = 0; i < buff.size(); i++)
		{
			if (((CharBuff_HAIRMOD_PROTECTED)buff.elementAt(i)).type == id)
			{
				return true;
			}
		}
		return false;
	}

	public void setPercentSpeed(int percent)
	{
		upSpeed = (sbyte)(percent * speed / 100);
		if (percent * speed % 100 >= 5)
		{
			upSpeed++;
		}
	}

	public long[] getTime()
	{
		long num = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() - timeWait2Board;
		return new long[2]
		{
			delay,
			59 - (int)num / 1000 % 60
		};
	}

	public override void actorDie()
	{
		hp = 0;
		timeOutPoinson = 0L;
		tDelay = 0;
		totalTime = 36;
		state = 3;
		poinson = 0;
	}

	public override void isPoinson()
	{
		if (mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() - timeOutPoinson >= tDelay * 1000 && tDelay > 0)
		{
			hp -= poinson;
			totalTime -= tDelay;
			timeOutPoinson = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
			if (totalTime == 0)
			{
				tDelay = 0;
				totalTime = 36;
			}
			if (hp <= 0 && state != 3)
			{
				hp = 1;
				poinson = 0;
				tDelay = 0;
				totalTime = 36;
			}
		}
	}

	public new sbyte getHorse()
	{
		if (Tilemap_HAIRMOD_PROTECTED.isOfflineMap)
		{
			return -1;
		}
		return useHorse;
	}

	public static DataSkillEff_HAIRMOD_PROTECTED getPartEff(short id)
	{
		if (id == -1)
		{
			return null;
		}
		DataSkillEff_HAIRMOD_PROTECTED dataSkillEff_HAIRMOD_PROTECTED = (DataSkillEff_HAIRMOD_PROTECTED)ALL_PART_EFF.get(id + string.Empty);
		if (dataSkillEff_HAIRMOD_PROTECTED == null)
		{
			dataSkillEff_HAIRMOD_PROTECTED = new DataSkillEff_HAIRMOD_PROTECTED(id);
			ALL_PART_EFF.put(id + string.Empty, dataSkillEff_HAIRMOD_PROTECTED);
			dataSkillEff_HAIRMOD_PROTECTED.timeRemove = (int)(mSystem_HAIRMOD_PROTECTED.currentTimeMillis() / 1000);
		}
		else
		{
			dataSkillEff_HAIRMOD_PROTECTED.timeRemove = mSystem_HAIRMOD_PROTECTED.currentTimeMillis();
		}
		return dataSkillEff_HAIRMOD_PROTECTED;
	}

	public static void SetRemove()
	{
		IDictionaryEnumerator enumerator = ALL_PART_EFF.GetEnumerator();
		while (enumerator.MoveNext())
		{
			string k = enumerator.Key.ToString();
			DataSkillEff_HAIRMOD_PROTECTED dataSkillEff_HAIRMOD_PROTECTED = (DataSkillEff_HAIRMOD_PROTECTED)ALL_PART_EFF.get(k);
			if ((mSystem_HAIRMOD_PROTECTED.currentTimeMillis() - dataSkillEff_HAIRMOD_PROTECTED.timeRemove) / 1000 > 200)
			{
				ALL_PART_EFF.remove(k);
			}
		}
	}

	public void updateHorse()
	{
		if (Canvas_HAIRMOD_PROTECTED.gameTick % 5 != 0 || idhorse == -1)
		{
			return;
		}
		DataSkillEff_HAIRMOD_PROTECTED partEff = getPartEff(idhorse);
		if (partEff != null)
		{
			int num = partEff.listFrame.size() / 21;
			if (num == 0)
			{
				num = 1;
			}
			frameThuCuoi = (sbyte)((frameThuCuoi + 1) % num);
		}
	}

	public override void update()
	{
		if (ID == 32001)
		{
			return;
		}
		if (hp <= 0 || realHP <= 0)
		{
		}
		if (dynamicEffTop != null)
		{
			for (int i = 0; i < dynamicEffTop.size(); i++)
			{
				DynamicEffect_HAIRMOD_PROTECTED dynamicEffect_HAIRMOD_PROTECTED = (DynamicEffect_HAIRMOD_PROTECTED)dynamicEffTop.elementAt(i);
				dynamicEffect_HAIRMOD_PROTECTED.update();
				if (dynamicEffect_HAIRMOD_PROTECTED.canDestroy())
				{
					dynamicEffTop.removeElementAt(i);
					if (dynamicEffect_HAIRMOD_PROTECTED.power < 2000000000)
					{
						Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + dynamicEffect_HAIRMOD_PROTECTED.power, 0, x, y + dy - 15, 1, -2);
					}
				}
			}
		}
		if (dynamicEffBottom != null)
		{
			for (int j = 0; j < dynamicEffBottom.size(); j++)
			{
				DynamicEffect_HAIRMOD_PROTECTED dynamicEffect_HAIRMOD_PROTECTED2 = (DynamicEffect_HAIRMOD_PROTECTED)dynamicEffBottom.elementAt(j);
				dynamicEffect_HAIRMOD_PROTECTED2.update();
				if (dynamicEffect_HAIRMOD_PROTECTED2.canDestroy())
				{
					dynamicEffBottom.removeElementAt(j);
					if (dynamicEffect_HAIRMOD_PROTECTED2.power < 2000000000)
					{
						Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + dynamicEffect_HAIRMOD_PROTECTED2.power, 0, x, y + dy - 15, 1, -2);
					}
				}
			}
		}
		if (!isFreeze)
		{
			if (EffFace != null)
			{
				EffFace.update();
			}
			if (effPhap != null)
			{
				effPhap.update();
			}
			if (beStune && mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() > timeBeStune)
			{
				beStune = false;
			}
			if (mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() - timeDelayRequestCharInfo > 10000)
			{
				int num = fullSet();
				if (num != 1)
				{
					if (ID != Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID)
					{
						if (GameScr_HAIRMOD_PROTECTED.paintChar == 1)
						{
							GameService_HAIRMOD_PROTECTED.gI().requestCharInfo(ID);
						}
					}
					else
					{
						GameService_HAIRMOD_PROTECTED.gI().requestMainCharInfo();
					}
					timeDelayRequestCharInfo = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
				}
			}
			isPoinson();
			if (delay >= 0)
			{
				long num2 = (mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() - timeWait2Board) / 60000;
				delay = realTime - num2;
			}
			if (lockUpdate > 0)
			{
				lockUpdate--;
				return;
			}
			bool flag = false;
			if (ID == Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID)
			{
				if (GameScr_HAIRMOD_PROTECTED.autoFight && GameScr_HAIRMOD_PROTECTED.typeOptionFocus == 1 && mSystem_HAIRMOD_PROTECTED.currentTimeMillis() - timeGoToLastXY < 0)
				{
					timeGoToLastXY = mSystem_HAIRMOD_PROTECTED.currentTimeMillis() + 30000;
					flag = true;
				}
				if (flag && Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.posTransRoad == null && GameScr_HAIRMOD_PROTECTED.timeRemovePos <= 0 && (Math_HAIRMOD_PROTECTED.abs(x - lastXAuto) > 120 || Math_HAIRMOD_PROTECTED.abs(y - lastYAuto) > 120))
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.findRoad2(lastXAuto, lastYAuto);
				}
			}
		}
		base.update();
		if (realHPSyncTime > 0)
		{
			realHPSyncTime--;
			if (realHPSyncTime == 0)
			{
				if (realHP < 0)
				{
					realHP = 0;
				}
				hp = realHP;
			}
		}
		if (state != 3 && realHP <= 0 && attack != 0)
		{
			state = 3;
			p1 = (p2 = (p3 = 0));
			Canvas_HAIRMOD_PROTECTED.gameScr.startExplosionAt(x, y);
		}
		if (isAnimal)
		{
			if (effAnimal != null)
			{
				effAnimal.update();
			}
			if (mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 - timeRemoEffanimal > 0)
			{
				effAnimal = null;
			}
			if (myAnimal != null)
			{
				myAnimal.update();
				myAnimal.dir = dir;
				myAnimal.status = state;
			}
		}
		switch (state)
		{
		case 4:
			if (comeHome)
			{
				long num7 = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() - timeComeHome;
				if (num7 > 10000)
				{
					resetBuff();
					GameService_HAIRMOD_PROTECTED.gI().comeHomeWhenDie();
					comeHome = false;
				}
				time = (int)(num7 / 1000);
				time = time * 32 / 10;
				if (time > 32)
				{
					time = 32;
				}
			}
			break;
		case 0:
			if (catagory == 0)
			{
				long currentTimeMillis = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
				if (currentTimeMillis - stand > 300000 && Canvas_HAIRMOD_PROTECTED.gameScr.mainChar != null && ID != Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID)
				{
					wantDestroy = true;
					Out_HAIRMOD_PROTECTED.println("huy char dung lau " + aaaa++);
				}
				if (currentTimeMillis - stand > 600000 && stand > 0 && !GameScr_HAIRMOD_PROTECTED.autoFight)
				{
					GameMidlet_HAIRMOD_PROTECTED.destroy();
				}
			}
			if (ID == Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID && GameScr_HAIRMOD_PROTECTED.saveAutoFight)
			{
				GameScr_HAIRMOD_PROTECTED.autoFight = GameScr_HAIRMOD_PROTECTED.saveAutoFight;
			}
			p1++;
			if (getHorse() == -1 || anmFly == 0)
			{
				if (p1 >= STANDFRAME.Length)
				{
					p1 = 0;
				}
				frame = STANDFRAME[p1];
				weapon_frame = frame;
				ifHorse = 0;
			}
			else
			{
				if (p1 >= ((getAttack() <= -1) ? RUNFRAMEWAL.Length : RUNFRAME.Length))
				{
					p1 = 0;
				}
				if (getHorse() == -1 || GameData_HAIRMOD_PROTECTED.imgHorse == null)
				{
					frame = RUNFRAMEWAL[p1];
				}
				else if (p1 % 3 == 0)
				{
					ifHorse = (ifHorse + 1) % ((anmFly != 1) ? 3 : 4);
				}
			}
			if (frame > 3)
			{
				frame = 0;
			}
			Fhorse = frame;
			break;
		case 2:
			if (currentSkillAnimate != null)
			{
				currentSkillAnimate.updateSkill(this);
			}
			break;
		case 1:
		{
			short num3 = 0;
			if (GameScr_HAIRMOD_PROTECTED.cheat)
			{
				num3 = 0;
			}
			p1++;
			if (p1 >= ((getHorse() <= -1) ? RUNFRAMEWAL.Length : RUNFRAME.Length))
			{
				p1 = 0;
			}
			if (getHorse() == -1 || GameData_HAIRMOD_PROTECTED.imgHorse == null)
			{
				frame = RUNFRAMEWAL[p1];
			}
			else if (p1 % 3 == 0)
			{
				ifHorse = (ifHorse + 1) % ((anmFly != 1) ? 3 : 4);
			}
			bool flag2 = false;
			bool flag3 = false;
			int num4 = Canvas_HAIRMOD_PROTECTED.abs(x - xTo);
			int num5 = Canvas_HAIRMOD_PROTECTED.abs(y - yTo);
			short num6 = (short)(speed + upSpeed + num3);
			if (num4 > 100 || num5 > 100)
			{
				num6 = (short)(speed + upSpeed + 4 + num3);
			}
			else if (num4 > 70 || num5 > 70)
			{
				num6 = (short)(speed + upSpeed + 3 + num3);
			}
			else if (num4 > 50 || num5 > 50)
			{
				num6 = (short)(speed + upSpeed + 2 + num3);
			}
			else if (num4 > 30 || num5 > 30)
			{
				num6 = (short)(speed + upSpeed + 1 + num3);
			}
			if (num4 < num6)
			{
				x = xTo;
				flag2 = true;
			}
			if (num5 < num6)
			{
				y = yTo;
				flag3 = true;
			}
			if (flag2 && flag3)
			{
				p1 = (p2 = (p3 = 0));
				state = 0;
				justStopFromRun = true;
				stand = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
				break;
			}
			if (x < xTo)
			{
				x += num6;
				dir = RIGHT;
			}
			else if (x > xTo)
			{
				x -= num6;
				dir = LEFT;
			}
			else if (y > yTo)
			{
				y -= num6;
				dir = UP;
			}
			else if (y < yTo)
			{
				y += num6;
				dir = DOWN;
			}
			if (frame > 3)
			{
				frame = 0;
			}
			weapon_frame = frame;
			if (ID == Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID)
			{
				GameScr_HAIRMOD_PROTECTED.autoFight = false;
			}
			if (idhorse != -1)
			{
				fh++;
				if (fh > horseMove.Length - 1)
				{
					fh = 0;
				}
				Fhorse = horseMove[fh];
			}
			break;
		}
		case 3:
			if (hp > 0 && realHP > 0)
			{
				state = 0;
			}
			p1++;
			if (p1 == 40 && ID == Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID)
			{
				Canvas_HAIRMOD_PROTECTED.msgdlg.isIcon = false;
				delay = -1L;
				EffFace = null;
			}
			isAnimal = false;
			myAnimal = null;
			break;
		}
		updateHorse();
		updateBuff();
		base.update();
	}

	public bool isLoadAnimalOk()
	{
		if (myAnimal != null && IsAnimal_HAIRMOD_PROTECTED.img[myAnimal.idMonster] != null)
		{
			return true;
		}
		return false;
	}

	public void resetBuff()
	{
		buff.removeAllElements();
		for (int i = 0; i < buffType.Length; i++)
		{
			buffType[i] = -1;
			lvBuff[i] = 0;
			countDown[i] = 0;
		}
	}

	public void paintbuff(MyGraphics_HAIRMOD_PROTECTED g)
	{
		for (int i = 0; i < buff.size(); i++)
		{
			CharBuff_HAIRMOD_PROTECTED charBuff_HAIRMOD_PROTECTED = (CharBuff_HAIRMOD_PROTECTED)buff.elementAt(i);
			charBuff_HAIRMOD_PROTECTED.paint(g);
		}
	}

	public int fullSet()
	{
		if (legStyle == -1 || bodyStyle == -1)
		{
			return 0;
		}
		if (currentHead == -1)
		{
			return 2;
		}
		return 1;
	}

	private int getStyleWeapon()
	{
		for (int i = 0; i < wearingItems.size(); i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)wearingItems.elementAt(i);
			ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
			if (item.type == 3 || item.type == 4 || item.type == 5 || item.type == 6 || item.type == 7)
			{
				return item.style;
			}
		}
		return -1;
	}

	private void paintEffectVukhi(MyGraphics_HAIRMOD_PROTECTED g, int id, WPSplashInfo_HAIRMOD_PROTECTED wp, int clazz)
	{
		int num = 0;
		if (getHorse() == -1 || GameData_HAIRMOD_PROTECTED.imgHorse == null)
		{
			num = 9;
		}
		g.drawRegion(imgLight, 0, id * 28 + lightFrame[idLight] * 7, 7, 7, 0, x + countX[clazz][dir], 5 + y + num + wp.PF_Y[dir][weapon_frame] + countY[clazz][dir] + dy, 0);
	}

	public int getdyParHorse()
	{
		if (typehorse != -1)
		{
			return dy_horse[typehorse][dir];
		}
		return 0;
	}

	public int getFrameBody()
	{
		if (dir == DOWN)
		{
			return frame + Framebody * 18;
		}
		if (dir == UP)
		{
			return dir * 6 + frame + Framebody * 18;
		}
		return frame + 12 + Framebody * 18;
	}

	public int getFramePP()
	{
		if (dir == DOWN)
		{
			return frame + framePP * 18;
		}
		if (dir == UP)
		{
			return dir * 6 + frame + framePP * 18;
		}
		return frame + 12 + framePP * 18;
	}

	public int getFrameLeg()
	{
		if (dir == DOWN)
		{
			return frame + frameLeg * 18;
		}
		if (dir == UP)
		{
			return dir * 6 + frame + frameLeg * 18;
		}
		return frame + 12 + frameLeg * 18;
	}

	public int getFrameHair()
	{
		if (dir == DOWN)
		{
			return frame + FrameHair * 18;
		}
		if (dir == UP)
		{
			return dir * 6 + frame + FrameHair * 18;
		}
		return frame + 12 + FrameHair * 18;
	}

	public int getFrameWP()
	{
		if (dir == DOWN)
		{
			return frame + FrameWP * 18;
		}
		if (dir == UP)
		{
			return dir * 6 + frame + FrameWP * 18;
		}
		return frame + 12 + FrameWP * 18;
	}

	public void paintHair(MyGraphics_HAIRMOD_PROTECTED g, int f)
	{
		sbyte horse = getHorse();
		if (GameData_HAIRMOD_PROTECTED.imgHorse == null)
		{
			horse = -1;
		}
		DataSkillEff_HAIRMOD_PROTECTED partEff = getPartEff(idHair);
		if (partEff != null && Canvas_HAIRMOD_PROTECTED.gameTick % 6 == 0)
		{
			int num = partEff.listFrame.size() / 18;
			if (num == 0)
			{
				num = 1;
			}
			FrameHair = (byte)((FrameHair + 1) % num);
		}
		if (partEff != null)
		{
			partEff.paintTopHorse(g, x + ((idhorse == -1) ? getDxAnimal(2, dir, anmFly) : 0) + getdxHorse(), y + ((idhorse == -1) ? getDyAnimal(2, dir, anmFly) : 0) + getdyParHorse() + f, getFrameHair(), (dir == RIGHT) ? 2 : 0);
			partEff.paintBottomHorse(g, x + ((idhorse == -1) ? getDxAnimal(2, dir, anmFly) : 0) + getdxHorse(), y + ((idhorse == -1) ? getDyAnimal(2, dir, anmFly) : 0) + getdyParHorse() + f, getFrameHair(), (dir == RIGHT) ? 2 : 0);
		}
	}

	public void paintBody(MyGraphics_HAIRMOD_PROTECTED g, int f)
	{
		sbyte horse = getHorse();
		if (GameData_HAIRMOD_PROTECTED.imgHorse == null)
		{
			horse = -1;
		}
		DataSkillEff_HAIRMOD_PROTECTED partEff = getPartEff(idBody);
		if (partEff != null && Canvas_HAIRMOD_PROTECTED.gameTick % 6 == 0)
		{
			int num = partEff.listFrame.size() / 18;
			if (num == 0)
			{
				num = 1;
			}
			Framebody = (byte)((Framebody + 1) % num);
		}
		if (partEff != null)
		{
			partEff.paintTopHorse(g, x + ((idhorse == -1) ? getDxAnimal(1, dir, anmFly) : 0) + getdxHorse(), y + ((idhorse == -1) ? getDyAnimal(1, dir, anmFly) : 0) + getdyParHorse() + f, getFrameBody(), (dir == RIGHT) ? 2 : 0);
			partEff.paintBottomHorse(g, x + ((idhorse == -1) ? getDxAnimal(1, dir, anmFly) : 0) + getdxHorse(), y + ((idhorse == -1) ? getDyAnimal(1, dir, anmFly) : 0) + getdyParHorse() + f, getFrameBody(), (dir == RIGHT) ? 2 : 0);
		}
	}

	public void paintLeg(MyGraphics_HAIRMOD_PROTECTED g, int f)
	{
		sbyte horse = getHorse();
		if (GameData_HAIRMOD_PROTECTED.imgHorse == null)
		{
			horse = -1;
		}
		DataSkillEff_HAIRMOD_PROTECTED partEff = getPartEff(idLeg);
		if (partEff != null && Canvas_HAIRMOD_PROTECTED.gameTick % 6 == 0)
		{
			int num = partEff.listFrame.size() / 18;
			if (num == 0)
			{
				num = 1;
			}
			frameLeg = (byte)((frameLeg + 1) % num);
		}
		if (partEff != null)
		{
			partEff.paintTopHorse(g, x + ((idhorse == -1) ? getDxAnimal(0, dir, anmFly) : 0) + getdxHorse(), y + ((idhorse == -1) ? getDyAnimal(0, dir, anmFly) : 0) + getdyParHorse() + f, getFrameLeg(), (dir == RIGHT) ? 2 : 0);
			partEff.paintBottomHorse(g, x + ((idhorse == -1) ? getDxAnimal(0, dir, anmFly) : 0) + getdxHorse(), y + ((idhorse == -1) ? getDyAnimal(0, dir, anmFly) : 0) + getdyParHorse() + f, getFrameLeg(), (dir == RIGHT) ? 2 : 0);
		}
	}

	public void paintPP(MyGraphics_HAIRMOD_PROTECTED g, int f)
	{
		sbyte horse = getHorse();
		if (GameData_HAIRMOD_PROTECTED.imgHorse == null)
		{
			horse = -1;
		}
		DataSkillEff_HAIRMOD_PROTECTED partEff = getPartEff(idPhiPhong);
		if (partEff != null && Canvas_HAIRMOD_PROTECTED.gameTick % 10 == 0)
		{
			int num = partEff.listFrame.size() / 18;
			if (num == 0)
			{
				num = 1;
			}
			framePP = (byte)((framePP + 1) % num);
		}
		if (partEff != null)
		{
			partEff.paintTopHorse(g, x + ((idhorse == -1) ? getDxAnimal(4, dir, anmFly) : 0) + getdxHorse(), y + ((idhorse == -1) ? getDyAnimal(4, dir, anmFly) : 0) + getdyParHorse() + f, getFramePP(), (dir == RIGHT) ? 2 : 0);
			partEff.paintBottomHorse(g, x + ((idhorse == -1) ? getDxAnimal(4, dir, anmFly) : 0) + getdxHorse(), y + ((idhorse == -1) ? getDyAnimal(4, dir, anmFly) : 0) + getdyParHorse() + f, getFramePP(), (dir == RIGHT) ? 2 : 0);
		}
	}

	public void paintVK(MyGraphics_HAIRMOD_PROTECTED g, int f)
	{
		sbyte b = getHorse();
		if (GameData_HAIRMOD_PROTECTED.imgHorse == null)
		{
			b = -1;
		}
		DataSkillEff_HAIRMOD_PROTECTED partEff = getPartEff(idWeapon);
		if (partEff != null && Canvas_HAIRMOD_PROTECTED.gameTick % 5 == 0)
		{
			int num = partEff.listFrame.size() / 18;
			if (num == 0)
			{
				num = 1;
			}
			FrameWP = (sbyte)((FrameWP + 1) % num);
		}
		if (partEff != null)
		{
			partEff.paintTopHorse(g, x + ((b != -1) ? dxr[b][dir][ifHorse] : 0) + getdxHorse(), y + ((b != -1) ? dyb[b][dir] : 0) + getdyParHorse() + f, getFrameWP(), (dir == RIGHT) ? 2 : 0);
			partEff.paintBottomHorse(g, x + ((b != -1) ? dxr[b][dir][ifHorse] : 0) + getdxHorse(), y + ((b != -1) ? dyb[b][dir] : 0) + getdyParHorse() + f, getFrameWP(), (dir == RIGHT) ? 2 : 0);
		}
	}

	private void paintWeapon(MyGraphics_HAIRMOD_PROTECTED g, int f)
	{
		try
		{
			if (idWeapon != -1)
			{
				paintVK(g, f);
				return;
			}
			int num = weaponType - 3;
			if (num < 0 || num >= 5)
			{
				return;
			}
			sbyte b = getHorse();
			if (GameData_HAIRMOD_PROTECTED.imgHorse == null)
			{
				b = -1;
			}
			int num2 = clazz;
			int index = weaponIndex;
			if (idModel[4] > -1)
			{
				num = 1;
				index = 0;
				num2 = 1;
			}
			if (isDoing)
			{
				num2 = 3;
				index = 0;
				num = 3;
			}
			int num3 = skill_type[num2][currentSkillType];
			if (state != 2)
			{
				num3 = 0;
			}
			if (weapon_frame < 0 || num < 0)
			{
				return;
			}
			WPSplashInfo_HAIRMOD_PROTECTED wPSplashInfo_HAIRMOD_PROTECTED = Res_HAIRMOD_PROTECTED.wpSplashInfos[num][num3];
			if (wPSplashInfo_HAIRMOD_PROTECTED == null)
			{
				wPSplashInfo_HAIRMOD_PROTECTED = Res_HAIRMOD_PROTECTED.GetWPSplashInfo(num, num3);
				return;
			}
			int num4 = weaponStyle;
			if (idModel[4] > -1 && !isDoing)
			{
				num4 = idModel[4];
			}
			if (isDoing)
			{
				num4 = 0;
			}
			if (state != 2)
			{
				if (weapon_frame > 1)
				{
					weapon_frame = 0;
				}
				if (weapon_frame < 0)
				{
					weapon_frame = 0;
				}
				if (num4 != -1 && Res_HAIRMOD_PROTECTED.loadImgWeaPone(num2, num4, index) != null)
				{
					WeaponInfo_HAIRMOD_PROTECTED weaponInfo_HAIRMOD_PROTECTED = Res_HAIRMOD_PROTECTED.loadImgWeaPone(num2, num4, index);
					if (idModel[4] > -1 && !isDoing)
					{
						num4 = 4;
					}
					g.drawRegion(weaponInfo_HAIRMOD_PROTECTED.img, pWeapone[num2][dir + num4 * 4], 0, sw[num2][dir + num4 * 4], hwp[num2], tWeapone[num2][dir], x + dwx[num2][dir + num4 * 4] + wPSplashInfo_HAIRMOD_PROTECTED.PF_X[dir][weapon_frame] + ((b != -1) ? dxr[b][dir][ifHorse] : 0), f + y + dwy[num2][dir + num4 * 4] + wPSplashInfo_HAIRMOD_PROTECTED.PF_Y[dir][weapon_frame] + ((dir == 2 && weapon_frame == 1) ? fixY[num2] : 0) + z + ((b != -1) ? dyb[b][dir] : 0) - ((dir == 1 && anmFly == 1) ? 5 : 0) + dy + getdyParHorse(), 0);
				}
			}
			else
			{
				if (!isDoing)
				{
					num = weaponType - 3;
					num3 = skill_type[num2][currentSkillType];
					num2 = clazz;
					index = weaponIndex;
					wPSplashInfo_HAIRMOD_PROTECTED = Res_HAIRMOD_PROTECTED.wpSplashInfos[num][num3];
					if (wPSplashInfo_HAIRMOD_PROTECTED == null)
					{
						wPSplashInfo_HAIRMOD_PROTECTED = Res_HAIRMOD_PROTECTED.GetWPSplashInfo(num, num3);
						return;
					}
				}
				if (weapon_frame < wPSplashInfo_HAIRMOD_PROTECTED.P0_X[dir].Length && wPSplashInfo_HAIRMOD_PROTECTED.image != null)
				{
					g.drawRegion(wPSplashInfo_HAIRMOD_PROTECTED.image, wPSplashInfo_HAIRMOD_PROTECTED.P0_X[dir][weapon_frame], wPSplashInfo_HAIRMOD_PROTECTED.P0_Y[dir][weapon_frame], wPSplashInfo_HAIRMOD_PROTECTED.PF_W[dir][weapon_frame], wPSplashInfo_HAIRMOD_PROTECTED.PF_H[dir][weapon_frame], 0, x + wPSplashInfo_HAIRMOD_PROTECTED.PF_X[dir][weapon_frame] + ((b != -1) ? dxr[b][dir][ifHorse] : 0), f + y + wPSplashInfo_HAIRMOD_PROTECTED.PF_Y[dir][weapon_frame] + z + ((b != -1) ? dyb[b][dir] : 0) + dy, 0);
				}
			}
			if (state != 0 && state != 1 && state != 4)
			{
				return;
			}
			int styleWeapon = getStyleWeapon();
			if (Canvas_HAIRMOD_PROTECTED.gameTick % 2 == 0)
			{
				idLight = (idLight + 1) % lightFrame.Length;
			}
			if (styleWeapon > 1)
			{
				int num5 = styleWeapon - 1;
				if (num5 > 3)
				{
					num5 = 3;
				}
				paintEffectVukhi(g, num5, wPSplashInfo_HAIRMOD_PROTECTED, num2);
			}
		}
		catch (Exception ex)
		{
			Debug.Log("Bi sai tai ham paintweap" + ex.ToString());
		}
	}

	public void paintAvatar(MyGraphics_HAIRMOD_PROTECTED g, short x, short y)
	{
		int num = legStyle;
		int num2 = bodyStyle;
		int num3 = currentHead;
		int num4 = hatStyle;
		int num5 = coatStyle;
		if (idModel[0] != -1)
		{
			num3 = idModel[0];
		}
		if (idModel[1] != -1)
		{
			num4 = idModel[1];
		}
		if (idModel[2] != -1)
		{
			num2 = idModel[2];
		}
		if (idModel[3] != -1)
		{
			num = idModel[3];
		}
		if (num5 != -1)
		{
			if (idImgPP == -1)
			{
				Res_HAIRMOD_PROTECTED.getCharPartInfo(4, num5).paintAvatar(g, x, y, frame);
			}
			else
			{
				ImageIcon_HAIRMOD_PROTECTED imgIcon = GameData_HAIRMOD_PROTECTED.getImgIcon(idImgPP);
				if (imgIcon != null && imgIcon.img != null)
				{
					if (Canvas_HAIRMOD_PROTECTED.gameTick % 5 == 0)
					{
						ppAvartar = (byte)((ppAvartar + 1) % nFramePP);
					}
					g.drawRegion(imgIcon.img, 0, imgIcon.img.getHeight() / nFramePP * ppAvartar, imgIcon.img.getWidth(), imgIcon.img.getHeight() / nFramePP, 0, x + Dx_Dy_PP[0][indexPP], y + Dx_Dy_PP[1][indexPP], 0);
				}
			}
		}
		if (num != -1 && idImgAVT == -1)
		{
			Res_HAIRMOD_PROTECTED.getCharPartInfo(0, num).paintAvatar(g, x, y, frame);
		}
		if (num2 != -1 && idImgAVT == -1)
		{
			Res_HAIRMOD_PROTECTED.getCharPartInfo(1, num2).paintAvatar(g, x, y, frame);
		}
		if (num3 != -1 && idImgAVT == -1)
		{
			Res_HAIRMOD_PROTECTED.getCharPartInfo(2, num3).paintAvatar(g, x, y, frame);
		}
		if (num4 != -1 && idImgAVT == -1)
		{
			Res_HAIRMOD_PROTECTED.getCharPartInfo(3, num4).paintAvatar(g, x, y, frame);
		}
		if (idImgAVT != -1)
		{
			ImageIcon_HAIRMOD_PROTECTED imgIcon2 = GameData_HAIRMOD_PROTECTED.getImgIcon(idImgAVT);
			if (imgIcon2 != null && imgIcon2.img != null)
			{
				if (Canvas_HAIRMOD_PROTECTED.gameTick % 5 == 0)
				{
					AVT = (byte)((AVT + 1) % nFrameAVT);
				}
				g.drawRegion(imgIcon2.img, 0, imgIcon2.img.getHeight() / nFrameAVT * AVT, imgIcon2.img.getWidth(), imgIcon2.img.getHeight() / nFrameAVT, 0, x + Dx_Dy_AVT[0][indexAVT], y + Dx_Dy_AVT[1][indexAVT], 0);
			}
		}
		if (weaponType != -1 && weaponStyle != -1 && idImageWeapon == -1 && imgWpa != null)
		{
			g.drawImage(imgWpa, x + dxWear, y + dyWear, 0);
		}
		if (idImageWeapon == -1)
		{
			return;
		}
		ImageIcon_HAIRMOD_PROTECTED imgIcon3 = GameData_HAIRMOD_PROTECTED.getImgIcon(idImageWeapon);
		if (imgIcon3 != null && imgIcon3.img != null)
		{
			if (Canvas_HAIRMOD_PROTECTED.gameTick % 5 == 0)
			{
				wpAvartar = (byte)((wpAvartar + 1) % nFrameWP);
			}
			g.drawRegion(imgIcon3.img, 0, imgIcon3.img.getHeight() / nFrameWP * wpAvartar, imgIcon3.img.getWidth(), imgIcon3.img.getHeight() / nFrameWP, 0, x + Dx_Dy_WP[0][typeWeapon], y + Dx_Dy_WP[1][typeWeapon], 0);
		}
	}

	public void setCurrentSkillByID(sbyte skillID)
	{
		currentSkillType = skillID;
		if (clazz < SkillManager_HAIRMOD_PROTECTED.SKILL_ANIMATE.Length && currentSkillType < SkillManager_HAIRMOD_PROTECTED.SKILL_ANIMATE[clazz].Length)
		{
			if (isDoing)
			{
				currentSkillAnimate = SkillManager_HAIRMOD_PROTECTED.getSkillAnimate(0, clazz);
			}
			else
			{
				currentSkillAnimate = SkillManager_HAIRMOD_PROTECTED.getSkillAnimate(currentSkillType, clazz);
			}
			if (currentSkillType < coolDownSkill.Length && skillID < skillLevelLearnt.Length)
			{
				coolDownSkill[currentSkillType] = SkillManager_HAIRMOD_PROTECTED.getSkillCooldown(currentSkillType, clazz, skillLevelLearnt[skillID]);
			}
		}
	}

	public sbyte getSkillIDFromType(sbyte skillType)
	{
		return (sbyte)(skillType * 10 + skillLevelLearnt[skillType]);
	}

	public virtual void startAttack(LiveActor_HAIRMOD_PROTECTED target, AttackResult_HAIRMOD_PROTECTED atr, sbyte skillID, sbyte level)
	{
		setCurrentSkillByID(skillID);
		if (currentSkillAnimate != null)
		{
			currentSkillAnimate.setLvSkill(level);
		}
		attackTarget = target;
		if (currentSkillType < timeLastUseSkills.Length)
		{
			timeLastUseSkills[currentSkillType] = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
		}
		state = 2;
		attkPower = 0;
		if (currentSkillAnimate != null)
		{
			currentSkillAnimate.getX2();
		}
		attkPower = atr.dam;
		attkEffect = atr.effect;
		p1 = (p2 = (p3 = 0));
	}

	public int getBuffEffAttack()
	{
		MyRandom_HAIRMOD_PROTECTED myRandom_HAIRMOD_PROTECTED = new MyRandom_HAIRMOD_PROTECTED();
		if (clazz == 0 && skillLevelLearnt[4] > 0)
		{
			int num = 0;
			if ((num = myRandom_HAIRMOD_PROTECTED.nextInt(100)) < SkillManager_HAIRMOD_PROTECTED.SKILL_DAM_PERCENT[0][4][skillLevelLearnt[4]])
			{
				return 0;
			}
		}
		return -1;
	}

	public void setWearingInfo(mVector_HAIRMOD_PROTECTED items)
	{
		wearingItems = items;
		isDoing = false;
		bodyStyle = -1;
		legStyle = -1;
		hatStyle = -1;
		weaponStyle = -1;
		weaponType = -1;
		coatStyle = -1;
		for (int i = 0; i < items.size(); i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)items.elementAt(i);
			ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
			if (item.type == 0)
			{
				bodyStyle = item.style;
				infoPaint[1] = item.style;
			}
			if (item.type == 1)
			{
				legStyle = item.style;
				infoPaint[0] = item.style;
			}
			if (item.type == 2)
			{
				hatStyle = item.style;
				infoPaint[3] = item.style;
			}
			if (item.type == 3 || item.type == 4 || item.type == 5 || item.type == 6 || item.type == 7)
			{
				weaponType = item.type;
				weaponStyle = item.style;
				weaponIndex = item.colorItem;
				infoPaint[5] = item.style;
			}
			if (item.type == 19)
			{
				coatStyle = item.style;
				infoPaint[4] = item.style;
			}
			if (item.type == 13)
			{
				isDoing = true;
			}
		}
		if (hp <= 0)
		{
			state = 3;
		}
		isWearing = true;
	}

	public void setAnimalWearingInfo(mVector_HAIRMOD_PROTECTED items)
	{
		wearingAnimal = items;
	}

	public void setInventory(long charXu0, int[] potions, mVector_HAIRMOD_PROTECTED items, mVector_HAIRMOD_PROTECTED anm, int type)
	{
		if (type == 0)
		{
			this.potions = potions;
			charXu = charXu0;
		}
		if (type == 1)
		{
			inventory.removeAllElements();
			inventory = items;
		}
		if (type == 2)
		{
			animal.removeAllElements();
			animal = anm;
		}
	}

	public int getCountPotion()
	{
		int num = 0;
		for (int i = 1; i < potions.Length; i++)
		{
			if (potions[i] > 0)
			{
				num++;
			}
		}
		return num;
	}

	public int getAttack()
	{
		int num = 0;
		return attack + attack * num / 100;
	}

	public int[] calInfoAttackPlayer(Char_HAIRMOD_PROTECTED actor, int def)
	{
		int[] array = new int[2] { def, 0 };
		array[0] -= array[0] / 3;
		array[1] = attack;
		if (actor.clazz == clazz)
		{
			array[1] /= 2;
		}
		else
		{
			int num = mainAt[clazz][actor.clazz];
			int num2 = mainDef[clazz][actor.clazz];
			int num3 = pcAttack[clazz][actor.clazz];
			int num4 = pcDef[clazz][actor.clazz];
			switch (num)
			{
			case 0:
				array[1] += array[1] * num3 / 100;
				break;
			case 1:
				array[1] = attack * num3 / 100;
				break;
			}
			switch (num2)
			{
			case 0:
				array[0] -= array[0] * num4 / 100;
				break;
			case 2:
				array[0] -= def * num4 / 100;
				break;
			case 3:
				array[0] = def;
				array[0] -= def * num4 / 100;
				break;
			}
		}
		return array;
	}

	public override int attackDam(LiveActor_HAIRMOD_PROTECTED actor)
	{
		int num = attack - actor.defend;
		if (num < 5)
		{
			num = 5;
		}
		num = num * (90 + nextInt(20)) / 100;
		if ((he + 2) % 5 == actor.he)
		{
			num = num * 12 / 10;
		}
		if (num <= 0)
		{
			num = 1;
		}
		return num;
	}

	public bool isFullParty()
	{
		if (party.size() < 4)
		{
			return false;
		}
		int[] array = new int[5];
		array[Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz]++;
		for (int i = 0; i < party.size(); i++)
		{
			PartyInfo_HAIRMOD_PROTECTED partyInfo_HAIRMOD_PROTECTED = (PartyInfo_HAIRMOD_PROTECTED)party.elementAt(i);
			if (partyInfo_HAIRMOD_PROTECTED != null)
			{
				array[partyInfo_HAIRMOD_PROTECTED.clazz]++;
				if (array[partyInfo_HAIRMOD_PROTECTED.clazz] > 1)
				{
					return false;
				}
			}
		}
		return true;
	}

	public AttackResult_HAIRMOD_PROTECTED getFinalDamTo(LiveActor_HAIRMOD_PROTECTED attackedLiveActor, sbyte skillID, sbyte level)
	{
		int skillDamPercent = SkillManager_HAIRMOD_PROTECTED.getSkillDamPercent(skillID, level, clazz);
		AttackResult_HAIRMOD_PROTECTED attackResult_HAIRMOD_PROTECTED = new AttackResult_HAIRMOD_PROTECTED();
		int num = attackDam(attackedLiveActor);
		num = num * skillDamPercent / 100;
		if (xuyengiap && attackedLiveActor.catagory == 0 && ((Char_HAIRMOD_PROTECTED)attackedLiveActor).clazz != PS)
		{
			num /= 2;
		}
		if (num <= 0)
		{
			num = 1;
		}
		bool flag = isCritical();
		flag = false;
		if (attackMiss(attackedLiveActor))
		{
			attackResult_HAIRMOD_PROTECTED.effect = AttackResult_HAIRMOD_PROTECTED.EFF_MISS;
			attackResult_HAIRMOD_PROTECTED.dam = 0;
		}
		else if (flag)
		{
			attackResult_HAIRMOD_PROTECTED.effect = AttackResult_HAIRMOD_PROTECTED.EFF_CRITICAL;
			attackResult_HAIRMOD_PROTECTED.dam = (short)(num * 2);
		}
		else
		{
			attackResult_HAIRMOD_PROTECTED.effect = AttackResult_HAIRMOD_PROTECTED.EFF_NORMAL;
			attackResult_HAIRMOD_PROTECTED.dam = (short)num;
		}
		attackResult_HAIRMOD_PROTECTED.mpLost = SkillManager_HAIRMOD_PROTECTED.getSkillMP(skillID, level, clazz);
		attackResult_HAIRMOD_PROTECTED.buffEffect = getBuffEffAttack();
		return attackResult_HAIRMOD_PROTECTED;
	}

	public void setProperty(Property_HAIRMOD_PROTECTED p)
	{
		if (p.key.Equals("hp"))
		{
			realHP = (hp = p.value);
		}
		if (p.key.Equals("mp"))
		{
			mp = p.value;
		}
		if (p.key.Equals("bp"))
		{
			basePointLeft = (short)p.value;
		}
		if (p.key.Equals("sp"))
		{
			skillPointLeft = (short)p.value;
		}
	}

	public void setBuff(sbyte[] bufftype, short[] countdown)
	{
		for (int i = 0; i < bufftype.Length; i++)
		{
			if (bufftype[i] <= 0)
			{
				if (!isExistBuff(BUFFTYPE[i]))
				{
					removeBuff(BUFFTYPE[i]);
				}
			}
			else if (!isExistBuff(bufftype[i]))
			{
				CharBuff_HAIRMOD_PROTECTED charBuff_HAIRMOD_PROTECTED = new CharBuff_HAIRMOD_PROTECTED(x, y, bufftype[i]);
				charBuff_HAIRMOD_PROTECTED.setTimeLive(countdown[i]);
				buff.addElement(charBuff_HAIRMOD_PROTECTED);
			}
		}
	}

	public void setCharInfo()
	{
		xTo = x;
		yTo = x;
		wName = (short)MFont_HAIRMOD_PROTECTED.smallFont.getWidth(name);
		realHP = hp;
		gender = GENDER_OF_CLAZZ[clazz];
		setBuff(buffType, countDown);
		if (killer > 0)
		{
			iskiller = true;
		}
		if (hp <= 0)
		{
			state = 3;
		}
		isMaininfo = true;
	}

	public bool kilAllMonsterQuest(int id)
	{
		if (monster_kill != null && receiveQuest && typeQuest == GameScr_HAIRMOD_PROTECTED.KILL_MOSTER)
		{
			for (int i = 0; i < monster_kill.Length; i++)
			{
				if (id == monster_kill[i][0])
				{
					if (monster_kill[i][2] < monster_kill[i][1])
					{
						monster_kill[i][2]++;
						infoQuest = monster_kill[i][2] + "/" + monster_kill[i][1];
						return true;
					}
					break;
				}
			}
		}
		return false;
	}

	public bool getItemQuest(int idItem)
	{
		if (monster_kill != null && receiveQuest && typeQuest == GameScr_HAIRMOD_PROTECTED.GET_ITEM)
		{
			for (int i = 0; i < monster_kill.Length; i++)
			{
				if (idItem == monster_kill[i][2])
				{
					if (monster_kill[i][3] < monster_kill[i][1])
					{
						monster_kill[i][3]++;
						infoQuest = monster_kill[i][3] + "/" + monster_kill[i][1];
						return true;
					}
					break;
				}
			}
		}
		return false;
	}

	public int getDurableWeapone()
	{
		for (int i = 0; i < wearingItems.size(); i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)wearingItems.elementAt(i);
			ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
			if (item.type == 3 || item.type == 4 || item.type == 5 || item.type == 6 || item.type == 7)
			{
				return itemInInventory_HAIRMOD_PROTECTED.durable;
			}
		}
		return 0;
	}

	public int getPriceRepair(int type)
	{
		int num = 0;
		int num2 = 0;
		int num3 = 0;
		for (int i = 0; i < wearingItems.size(); i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)wearingItems.elementAt(i);
			ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
			if (item.type == 3 || item.type == 4 || item.type == 5 || item.type == 6 || item.type == 7)
			{
				num2 += item.price / 10;
			}
			else
			{
				num += item.price / 10;
			}
		}
		switch (type)
		{
		case 0:
			return num;
		case 1:
			return num2;
		default:
			return num + num2;
		}
	}

	public void downDurableWeapone()
	{
		for (int i = 0; i < wearingItems.size(); i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)wearingItems.elementAt(i);
			ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
			if ((item.type == 3 || item.type == 4 || item.type == 5 || item.type == 6 || item.type == 7) && itemInInventory_HAIRMOD_PROTECTED.mDurable > 0)
			{
				itemInInventory_HAIRMOD_PROTECTED.mDurable--;
				if (itemInInventory_HAIRMOD_PROTECTED.mDurable % 10 == 0)
				{
					itemInInventory_HAIRMOD_PROTECTED.durable--;
				}
			}
		}
	}

	public void downDuarable()
	{
		for (int i = 0; i < wearingItems.size(); i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)wearingItems.elementAt(i);
			ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
			if ((item.type == 0 || item.type == 1 || item.type == 2) && itemInInventory_HAIRMOD_PROTECTED.mDurable > 0)
			{
				itemInInventory_HAIRMOD_PROTECTED.mDurable--;
				if (itemInInventory_HAIRMOD_PROTECTED.mDurable % 10 == 0)
				{
					itemInInventory_HAIRMOD_PROTECTED.durable--;
				}
			}
		}
	}

	public void paintListEff(MyGraphics_HAIRMOD_PROTECTED g, int lv)
	{
		if (listEffect == null || listEffect.Length == 0)
		{
			return;
		}
		for (int i = 0; i < listEffect.Length; i++)
		{
			DataSkillEff_HAIRMOD_PROTECTED partEff = getPartEff(listEffect[i]);
			if (partEff == null)
			{
				continue;
			}
			if (Canvas_HAIRMOD_PROTECTED.gameTick % Tick[i] == 0 && partEff != null)
			{
				int num = partEff.listFrame.size();
				if (num == 0)
				{
					num = 1;
				}
				Frameeff = (sbyte)((Frameeff + 1) % num);
			}
			if (lv == 0)
			{
				partEff.paintTopHorse(g, x, y - ((anmFly != 0) ? 6 : 0) + getdyParHorse(), Frameeff, 0);
			}
			else
			{
				partEff.paintBottomHorse(g, x, y - ((anmFly != 0) ? 6 : 0) + getdyParHorse(), Frameeff, 0);
			}
		}
	}

	public override int getHp()
	{
		return hp;
	}

	public override void setState(int state)
	{
		base.state = (sbyte)state;
	}

	public override int getRealHp()
	{
		return realHP;
	}

	public bool getAllItemQuest(int id)
	{
		return false;
	}

	public override bool isNPC()
	{
		return idBoss != -1;
	}

	public override bool isNpcChar()
	{
		return isNPC();
	}

	public override bool isPlayer()
	{
		return true;
	}

	public override int getTypeNpc()
	{
		if (isNPC())
		{
			return idBoss;
		}
		return base.getTypeNpc();
	}

	public new virtual string getCharName()
	{
		return name;
	}

	public override void setHorse(int idhorse, int typehorse)
	{
		this.idhorse = (short)idhorse;
		this.typehorse = (short)typehorse;
	}

	public override void setWeapon(int id, int type, int idImg)
	{
		idWeapon = (short)id;
		typeWeapon = (short)type;
		idImageWeapon = (short)idImg;
	}

	public override void setPartWearing(short idHair, short idBody, short idLeg)
	{
		this.idHair = idHair;
		this.idBody = idBody;
		this.idLeg = idLeg;
	}

	public override void setEff(short[] arrID, sbyte[] arrTick)
	{
		listEffect = arrID;
		Tick = arrTick;
	}

	public override short getyHorse()
	{
		if (useHorse != -1 || idhorse > -1)
		{
			return 35;
		}
		return 0;
	}

	public override short getx()
	{
		return x;
	}

	public override short gety()
	{
		return y;
	}
}
