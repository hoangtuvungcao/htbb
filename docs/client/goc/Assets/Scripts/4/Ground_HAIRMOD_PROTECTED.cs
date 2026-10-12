public class Ground_HAIRMOD_PROTECTED : Actor_HAIRMOD_PROTECTED
{
	public MyTree_HAIRMOD_PROTECTED myTree;

	public short idTree;

	public new int timeLive;

	public int minutes;

	private long currentTime;

	private sbyte count;

	private sbyte addyMytree;

	private sbyte colorInfo;

	public bool isBuyOk;

	private string clockInfo = string.Empty;

	public Ground_HAIRMOD_PROTECTED(int idFarm, int id, int x, int y, string info, string name, int idImg, int timeLive, bool isBuyOk, sbyte addyMytree, sbyte colorInfo)
	{
		ID = (short)idFarm;
		idTree = (short)id;
		base.x = (short)x;
		base.y = (short)y;
		catagory = Actor_HAIRMOD_PROTECTED.CAT_MY_GROUND;
		infoGround = info;
		this.timeLive = timeLive / 60;
		minutes = timeLive % 60;
		myTree = null;
		this.isBuyOk = isBuyOk;
		currentTime = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
		this.addyMytree = addyMytree;
		this.colorInfo = colorInfo;
		if (id > -1)
		{
			myTree = new MyTree_HAIRMOD_PROTECTED(id, x + 16, y + 16 + addyMytree, idImg);
			Canvas_HAIRMOD_PROTECTED.gameScr.actors.addElement(myTree);
		}
	}

	public void setInfo(int idFarm, int id, int x, int y, string info, string name, int idImgTree, int timeLive, bool isBuyOk, sbyte addyMytree, sbyte colorInfo)
	{
		ID = (short)idFarm;
		idTree = (short)id;
		base.x = (short)x;
		base.y = (short)y;
		catagory = Actor_HAIRMOD_PROTECTED.CAT_MY_GROUND;
		infoGround = info;
		this.timeLive = timeLive / 60;
		minutes = timeLive % 60;
		this.isBuyOk = isBuyOk;
		currentTime = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
		this.addyMytree = addyMytree;
		this.colorInfo = colorInfo;
		if (id > -1)
		{
			if (myTree != null)
			{
				myTree.himg = 1;
				myTree.idTree = (short)id;
				myTree.idImg = (byte)idImgTree;
			}
			else
			{
				myTree = new MyTree_HAIRMOD_PROTECTED(id, x + 16, y + 16 + addyMytree, idImgTree);
				Canvas_HAIRMOD_PROTECTED.gameScr.actors.addElement(myTree);
			}
		}
		else if (myTree != null)
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.actors.removeElement(myTree);
			myTree = null;
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (Res_HAIRMOD_PROTECTED.imgGround != null)
		{
			if (isBuyOk)
			{
				if (myTree == null)
				{
					g.drawImage(Res_HAIRMOD_PROTECTED.imgGround[0], x, y, 0);
				}
				else
				{
					g.drawImage(Res_HAIRMOD_PROTECTED.imgGround[2], x, y, 0);
				}
			}
			else
			{
				g.drawImage(Res_HAIRMOD_PROTECTED.imgGround[1], x, y, 0);
			}
		}
		if (isFocus)
		{
			if (timeLive > -1 && myTree != null)
			{
				int num = MFont_HAIRMOD_PROTECTED.normalFont[colorInfo].getWidth(clockInfo);
				g.setColor(1593912);
				g.fillRect(x + 16 - num / 2 - 3, y - ((myTree != null) ? (myTree.himg + 15) : 8), num + 5, 14);
				g.setColor(16772608);
				g.drawRect(x + 16 - num / 2 - 3, y - ((myTree != null) ? (myTree.himg + 15) : 8), num + 5, 14);
				MFont_HAIRMOD_PROTECTED.normalFont[colorInfo].drawString(g, clockInfo, x + 16, y - ((myTree != null) ? (myTree.himg + 15) : 8), 2);
			}
			if (PopupName_HAIRMOD_PROTECTED.imgArrow != null)
			{
				PopupName_HAIRMOD_PROTECTED.imgArrow.drawFrame(0, x + 16, y - ((myTree != null) ? myTree.himg : 8) + Canvas_HAIRMOD_PROTECTED.gameTick % 8, 0, 33, g);
			}
		}
	}

	public override void update()
	{
		count = (sbyte)(minutes - (mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() - currentTime) / 1000);
		if (count <= 0)
		{
			timeLive--;
			currentTime = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
			minutes = 59;
		}
		if (isFocus && timeLive > -1 && myTree != null)
		{
			clockInfo = ((timeLive >= 10) ? (timeLive + string.Empty) : ("0" + timeLive)) + " : " + ((count >= 10) ? (count + string.Empty) : ("0" + count));
		}
		base.update();
	}

	public new void setPosTo(short x, short y)
	{
	}
}
