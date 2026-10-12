public class FrameImage_HAIRMOD_PROTECTED
{
	public short frameWidth;

	public short frameHeight;

	public short nFrame;

	public Image_HAIRMOD_PROTECTED imgFrame;

	public FrameImage_HAIRMOD_PROTECTED()
	{
	}

	public FrameImage_HAIRMOD_PROTECTED(Image_HAIRMOD_PROTECTED img, int width, int height)
	{
		imgFrame = img;
		frameWidth = (short)width;
		frameHeight = (short)height;
		nFrame = (short)(img.getHeight() / height);
	}

	public static FrameImage_HAIRMOD_PROTECTED init(string path, int w, int h)
	{
		return new FrameImage_HAIRMOD_PROTECTED(FilePack_HAIRMOD_PROTECTED.getImg(path), w, h);
	}

	public void drawFrame(int idx, int x, int y, int trans, int orthor, MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (imgFrame != null)
		{
			g.drawRegion(imgFrame, 0, idx * frameHeight, frameWidth, frameHeight, trans, x, y, orthor);
		}
	}
}
