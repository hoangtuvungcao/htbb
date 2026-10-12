using System;

public class MiniGame_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	public static MiniGame_HAIRMOD_PROTECTED me;

	private mVector_HAIRMOD_PROTECTED modelCell = new mVector_HAIRMOD_PROTECTED();

	private int idCell;

	private bool isClose;

	private bool isOpen;

	private bool isPointer;

	private Command_HAIRMOD_PROTECTED cmdPlay;

	private Command_HAIRMOD_PROTECTED cmdSelect;

	private Command_HAIRMOD_PROTECTED cmdSelcetAll;

	private Command_HAIRMOD_PROTECTED cmd;

	private long timeBegin;

	public string[] arrInfo;

	public string[] result;

	public int xchar;

	public int ychar;

	public int yTo;

	public MiniGame_HAIRMOD_PROTECTED()
	{
		cmd = new Command_HAIRMOD_PROTECTED(string.Empty);
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
		};
		cmd.action = action;
		ActionPerform_HAIRMOD_PROTECTED action2 = delegate
		{
			isOpen = true;
			timeBegin = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 + 2;
			doClose();
			center = null;
		};
		cmdPlay = new Command_HAIRMOD_PROTECTED("Bắt đầu");
		cmdPlay.action = action2;
		ActionPerform_HAIRMOD_PROTECTED action3 = delegate
		{
			ActionPerform_HAIRMOD_PROTECTED noAction2 = delegate
			{
				Canvas_HAIRMOD_PROTECTED.endDlg();
			};
			ActionPerform_HAIRMOD_PROTECTED yesAction2 = delegate
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.gameService.playMiniGame(1, (sbyte)idCell);
				Canvas_HAIRMOD_PROTECTED.endDlg();
			};
			Canvas_HAIRMOD_PROTECTED.startYesNoDlgPutArr("Bạn có muốn mở không? ", arrInfo, yesAction2, noAction2);
		};
		cmdSelect = new Command_HAIRMOD_PROTECTED("Chọn");
		cmdSelect.action = action3;
		center = cmdPlay;
		ActionPerform_HAIRMOD_PROTECTED action4 = delegate
		{
			ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.gameService.playMiniGame(2, 100);
				Canvas_HAIRMOD_PROTECTED.endDlg();
				idCell = 0;
				isClose = false;
				isPointer = false;
				isOpen = false;
				center = cmdPlay;
				right = cmd;
				xchar = 0;
				ychar = (yTo = 0);
				if (result != null)
				{
					for (sbyte b = 0; b < result.Length; b++)
					{
						result[b] = string.Empty;
					}
				}
				modelCell.removeAllElements();
				if (arrInfo != null)
				{
					for (sbyte b2 = 0; b2 < arrInfo.Length; b2++)
					{
						arrInfo[b2] = string.Empty;
					}
				}
			};
			ActionPerform_HAIRMOD_PROTECTED noAction = delegate
			{
				Canvas_HAIRMOD_PROTECTED.endDlg();
			};
			Canvas_HAIRMOD_PROTECTED.startYesNoDlgPutArr("Bạn có muốn mở hết không? ", arrInfo, yesAction, noAction);
		};
		cmdSelcetAll = new Command_HAIRMOD_PROTECTED("Mở hết");
		cmdSelcetAll.action = action4;
	}

	public static MiniGame_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new MiniGame_HAIRMOD_PROTECTED());
	}

	public override void switchToMe()
	{
		base.switchToMe();
		init();
	}

	public void reSetGame()
	{
		idCell = 0;
		isClose = false;
		isPointer = false;
		isOpen = false;
		center = cmdPlay;
		right = cmd;
		xchar = 0;
		ychar = (yTo = 0);
		if (result != null)
		{
			for (sbyte b = 0; b < result.Length; b++)
			{
				result[b] = string.Empty;
			}
		}
		modelCell.removeAllElements();
		if (arrInfo != null)
		{
			for (sbyte b2 = 0; b2 < arrInfo.Length; b2++)
			{
				arrInfo[b2] = string.Empty;
			}
		}
	}

	public void updatePoint()
	{
		if (!Canvas_HAIRMOD_PROTECTED.isPointerClick || modelCell.size() <= 0)
		{
			return;
		}
		for (int i = 0; i < modelCell.size(); i++)
		{
			ModelCell_HAIRMOD_PROTECTED modelCell_HAIRMOD_PROTECTED = (ModelCell_HAIRMOD_PROTECTED)modelCell.elementAt(i);
			if (modelCell_HAIRMOD_PROTECTED != null && Canvas_HAIRMOD_PROTECTED.px >= modelCell_HAIRMOD_PROTECTED.x - 16 && Canvas_HAIRMOD_PROTECTED.px <= modelCell_HAIRMOD_PROTECTED.x + 16 && Canvas_HAIRMOD_PROTECTED.py >= modelCell_HAIRMOD_PROTECTED.y - 16 && Canvas_HAIRMOD_PROTECTED.py <= modelCell_HAIRMOD_PROTECTED.y + 16)
			{
				Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
				if (isPointer)
				{
					idCell = modelCell_HAIRMOD_PROTECTED.id;
					center.perform();
					break;
				}
			}
		}
	}

	public void loadModelCell(int sum)
	{
		modelCell.removeAllElements();
		for (int i = 0; i < sum; i++)
		{
			ModelCell_HAIRMOD_PROTECTED modelCell_HAIRMOD_PROTECTED = new ModelCell_HAIRMOD_PROTECTED();
			modelCell_HAIRMOD_PROTECTED.id = i;
			modelCell.addElement(modelCell_HAIRMOD_PROTECTED);
		}
	}

	public void getInfoBox(sbyte id, string infoBox, string[] updateInfo)
	{
		arrInfo = updateInfo;
		string[] info = MFont_HAIRMOD_PROTECTED.arialFontW.splitFontBStrInLine(infoBox, 100);
		for (int i = 0; i < modelCell.size(); i++)
		{
			ModelCell_HAIRMOD_PROTECTED modelCell_HAIRMOD_PROTECTED = (ModelCell_HAIRMOD_PROTECTED)modelCell.elementAt(i);
			if (modelCell_HAIRMOD_PROTECTED.id == id)
			{
				modelCell_HAIRMOD_PROTECTED.index = 1;
				starFlyString(info, modelCell_HAIRMOD_PROTECTED.x, modelCell_HAIRMOD_PROTECTED.y);
				break;
			}
		}
	}

	public void onMiniGame(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			sbyte sum = msg.reader().readByte();
			sbyte b = msg.reader().readByte();
			arrInfo = new string[b];
			for (sbyte b2 = 0; b2 < arrInfo.Length; b2++)
			{
				arrInfo[b2] = msg.reader().readUTF();
			}
			loadModelCell(sum);
			init();
		}
		catch (Exception)
		{
		}
	}

	public void setSit()
	{
		int x = Canvas_HAIRMOD_PROTECTED.w / 2;
		int y = Canvas_HAIRMOD_PROTECTED.h / 2;
		int num = 0;
		int[][] array = new int[3][]
		{
			new int[5]
			{
				Canvas_HAIRMOD_PROTECTED.w / 2 - Canvas_HAIRMOD_PROTECTED.w / 5,
				Canvas_HAIRMOD_PROTECTED.w / 2 + Canvas_HAIRMOD_PROTECTED.w / 5,
				Canvas_HAIRMOD_PROTECTED.w / 2,
				Canvas_HAIRMOD_PROTECTED.w / 2 - Canvas_HAIRMOD_PROTECTED.w / 5,
				Canvas_HAIRMOD_PROTECTED.w / 2 + Canvas_HAIRMOD_PROTECTED.w / 5
			},
			new int[7]
			{
				Canvas_HAIRMOD_PROTECTED.w / 2 - Canvas_HAIRMOD_PROTECTED.w / 5,
				Canvas_HAIRMOD_PROTECTED.w / 2,
				Canvas_HAIRMOD_PROTECTED.w / 2 + Canvas_HAIRMOD_PROTECTED.w / 5,
				Canvas_HAIRMOD_PROTECTED.w / 2,
				Canvas_HAIRMOD_PROTECTED.w / 2 - Canvas_HAIRMOD_PROTECTED.w / 5,
				Canvas_HAIRMOD_PROTECTED.w / 2,
				Canvas_HAIRMOD_PROTECTED.w / 2 + Canvas_HAIRMOD_PROTECTED.w / 5
			},
			new int[9]
			{
				Canvas_HAIRMOD_PROTECTED.w / 2 - Canvas_HAIRMOD_PROTECTED.w / 5,
				Canvas_HAIRMOD_PROTECTED.w / 2,
				Canvas_HAIRMOD_PROTECTED.w / 2 + Canvas_HAIRMOD_PROTECTED.w / 5,
				Canvas_HAIRMOD_PROTECTED.w / 2 - Canvas_HAIRMOD_PROTECTED.w / 5,
				Canvas_HAIRMOD_PROTECTED.w / 2,
				Canvas_HAIRMOD_PROTECTED.w / 2 + Canvas_HAIRMOD_PROTECTED.w / 5,
				Canvas_HAIRMOD_PROTECTED.w / 2 - Canvas_HAIRMOD_PROTECTED.w / 5,
				Canvas_HAIRMOD_PROTECTED.w / 2,
				Canvas_HAIRMOD_PROTECTED.w / 2 + Canvas_HAIRMOD_PROTECTED.w / 5
			}
		};
		int[][] array2 = new int[3][]
		{
			new int[5]
			{
				Canvas_HAIRMOD_PROTECTED.h / 2 - Canvas_HAIRMOD_PROTECTED.h / 5,
				Canvas_HAIRMOD_PROTECTED.h / 2 - Canvas_HAIRMOD_PROTECTED.h / 5,
				Canvas_HAIRMOD_PROTECTED.h / 2,
				Canvas_HAIRMOD_PROTECTED.h / 2 + Canvas_HAIRMOD_PROTECTED.h / 5,
				Canvas_HAIRMOD_PROTECTED.h / 2 + Canvas_HAIRMOD_PROTECTED.h / 5
			},
			new int[7]
			{
				Canvas_HAIRMOD_PROTECTED.h / 2 - Canvas_HAIRMOD_PROTECTED.h / 5,
				Canvas_HAIRMOD_PROTECTED.h / 2 - Canvas_HAIRMOD_PROTECTED.h / 5,
				Canvas_HAIRMOD_PROTECTED.h / 2 - Canvas_HAIRMOD_PROTECTED.h / 5,
				Canvas_HAIRMOD_PROTECTED.h / 2,
				Canvas_HAIRMOD_PROTECTED.h / 2 + Canvas_HAIRMOD_PROTECTED.h / 5,
				Canvas_HAIRMOD_PROTECTED.h / 2 + Canvas_HAIRMOD_PROTECTED.h / 5,
				Canvas_HAIRMOD_PROTECTED.h / 2 + Canvas_HAIRMOD_PROTECTED.h / 5
			},
			new int[9]
			{
				Canvas_HAIRMOD_PROTECTED.h / 2 - Canvas_HAIRMOD_PROTECTED.h / 5,
				Canvas_HAIRMOD_PROTECTED.h / 2 - Canvas_HAIRMOD_PROTECTED.h / 5,
				Canvas_HAIRMOD_PROTECTED.h / 2 - Canvas_HAIRMOD_PROTECTED.h / 5,
				Canvas_HAIRMOD_PROTECTED.h / 2,
				Canvas_HAIRMOD_PROTECTED.h / 2,
				Canvas_HAIRMOD_PROTECTED.h / 2,
				Canvas_HAIRMOD_PROTECTED.h / 2 + Canvas_HAIRMOD_PROTECTED.h / 5,
				Canvas_HAIRMOD_PROTECTED.h / 2 + Canvas_HAIRMOD_PROTECTED.h / 5,
				Canvas_HAIRMOD_PROTECTED.h / 2 + Canvas_HAIRMOD_PROTECTED.h / 5
			}
		};
		if (modelCell.size() == 7)
		{
			num = 1;
		}
		if (modelCell.size() == 9)
		{
			num = 2;
		}
		for (int i = 0; i < modelCell.size(); i++)
		{
			ModelCell_HAIRMOD_PROTECTED modelCell_HAIRMOD_PROTECTED = (ModelCell_HAIRMOD_PROTECTED)modelCell.elementAt(i);
			modelCell_HAIRMOD_PROTECTED.x = x;
			modelCell_HAIRMOD_PROTECTED.y = y;
			modelCell_HAIRMOD_PROTECTED.xTo = array[num][i];
			modelCell_HAIRMOD_PROTECTED.yTo = array2[num][i];
			modelCell_HAIRMOD_PROTECTED.saveX = modelCell_HAIRMOD_PROTECTED.xTo;
			modelCell_HAIRMOD_PROTECTED.saveY = modelCell_HAIRMOD_PROTECTED.yTo;
		}
	}

	public override void init()
	{
		if (modelCell != null && modelCell.size() > 0)
		{
			setSit();
		}
		base.init();
	}

	public void starFlyString(string[] info, int x, int y)
	{
		result = info;
		xchar = x;
		ychar = y;
		yTo = y - 20;
	}

	public void updateFlyText()
	{
		if (ychar != yTo)
		{
			ychar--;
			return;
		}
		ychar = yTo;
		if (result != null)
		{
			for (sbyte b = 0; b < result.Length; b++)
			{
				result[b] = string.Empty;
			}
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Canvas_HAIRMOD_PROTECTED.gameScr.paint(g);
		Res_HAIRMOD_PROTECTED.paintDlgFull(g, 10, 20, Canvas_HAIRMOD_PROTECTED.w - 20, Canvas_HAIRMOD_PROTECTED.h - 60);
		MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, "Hộp may mắn", Canvas_HAIRMOD_PROTECTED.hw, 25, 2);
		for (int i = 0; i < modelCell.size(); i++)
		{
			ModelCell_HAIRMOD_PROTECTED modelCell_HAIRMOD_PROTECTED = (ModelCell_HAIRMOD_PROTECTED)modelCell.elementAt(i);
			if (modelCell_HAIRMOD_PROTECTED != null)
			{
				modelCell_HAIRMOD_PROTECTED.paint(g);
				if (Canvas_HAIRMOD_PROTECTED.gameTick % 4 == 0 && idCell == i && !isClose)
				{
					g.setColor(16777215);
					g.drawRect(modelCell_HAIRMOD_PROTECTED.x - 17, modelCell_HAIRMOD_PROTECTED.y - 17, 34, 34);
				}
			}
		}
		if (ychar != yTo)
		{
			for (sbyte b = 0; b < result.Length; b++)
			{
				MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, result[b], xchar, ychar + b * 14, 2);
			}
		}
		base.paint(g);
	}

	public override void update()
	{
		for (int i = 0; i < modelCell.size(); i++)
		{
			((ModelCell_HAIRMOD_PROTECTED)modelCell.elementAt(i))?.update();
		}
		if (isClose && timeBegin - mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 <= 0 && isOpen)
		{
			isOpen = false;
			doClose();
			center = cmdSelect;
			right = cmdSelcetAll;
			isPointer = true;
		}
		updateFlyText();
		base.update();
	}

	public override void updateKey()
	{
		if (Canvas_HAIRMOD_PROTECTED.keyPressed[4])
		{
			Canvas_HAIRMOD_PROTECTED.keyPressed[4] = false;
			if (!isClose)
			{
				idCell--;
				if (idCell < 0)
				{
					idCell = 0;
				}
			}
		}
		else if (Canvas_HAIRMOD_PROTECTED.keyPressed[6])
		{
			Canvas_HAIRMOD_PROTECTED.keyPressed[6] = false;
			if (!isClose)
			{
				idCell++;
				if (idCell > modelCell.size() - 1)
				{
					idCell = modelCell.size() - 1;
				}
			}
		}
		updatePoint();
		base.updateKey();
	}

	public void doClose()
	{
		isClose = !isClose;
		for (int i = 0; i < modelCell.size(); i++)
		{
			ModelCell_HAIRMOD_PROTECTED modelCell_HAIRMOD_PROTECTED = (ModelCell_HAIRMOD_PROTECTED)modelCell.elementAt(i);
			if (isClose)
			{
				modelCell_HAIRMOD_PROTECTED.xTo = Canvas_HAIRMOD_PROTECTED.w / 2;
				modelCell_HAIRMOD_PROTECTED.yTo = Canvas_HAIRMOD_PROTECTED.h / 2;
			}
			else
			{
				modelCell_HAIRMOD_PROTECTED.xTo = modelCell_HAIRMOD_PROTECTED.saveX;
				modelCell_HAIRMOD_PROTECTED.yTo = modelCell_HAIRMOD_PROTECTED.saveY;
			}
		}
	}
}
