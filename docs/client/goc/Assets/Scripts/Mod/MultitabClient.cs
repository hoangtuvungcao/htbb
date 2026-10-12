
using System;
using UnityEngine;
using UnityEngine.SceneManagement;

public class MultitabClient 
{
    public static Image ic;
    public static int xI, yI = Canvas.h / 2, lastX, lastY;
    public static Command command;
    public static bool isPointerDown, isCMD;
   static  Scene scene;
    public MultitabClient()
    {
        scene = SceneManager.GetActiveScene();
        xI = 0;
        yI = Canvas.h / 2;
    }
    public static Image loadImage(string path)
    {
        TextAsset textAsset = Resources.Load(path) as TextAsset;
        Image result = Image.__createImage(textAsset.bytes);
        return result;
    }
    public static string cutPng(string str)
    {
        string result = str;
        if (str.Contains(".png"))
        {
            result = str.Replace(".png", string.Empty);
        }
        return result;
    }

    public static void paint(MyGraphics g)
    {
        if (ic == null)
        {
            ic = loadImage("res/ic");
            return;
        }
      
        g.drawImage(ic, xI, yI);
    }
    public static void updateKeyTouch()
    {
        try
        {
            if (ic == null)
            {
                ic = loadImage("res/ic");
               
                return;
            }
            if (Canvas.isPointerHoldIn(xI, yI, ic.getWidth(), ic.getHeight()) && !isPointerDown)
            {
                isPointerDown = true;
                lastX = Canvas.px;
                lastY = Canvas.py;
                Canvas.clearKeyPressed();
                return;
            }
            if (Canvas.isPointerJustRelease)
            {
                isPointerDown = false;
                Canvas.isPointerDown = false;
                if (Canvas.isPointerHoldIn(xI, yI, ic.getWidth(), ic.getHeight()))
                {
                    try
                    {
                        ActionPerform action = delegate
                        {
                            Scene scene2 = SceneManager.GetActiveScene();
                            if (scene2.name is "test")
                            {
                                SceneManager.LoadScene("test2");
                                Main.tab = TabType.tab2;
                            }
                            else
                            {
                                SceneManager.LoadScene("test");
                                Main.tab = TabType.tab1;
                            }
                        };
                        Canvas.startYesNoDlg((Main.tab == TabType.tab1) ? "Chuyển Tab 2" : "Chuyển Tab 1", action);
                    }
                    catch(Exception ex)
                    {
                        Debug.LogException(ex);
                    }
                    Canvas.clearKeyPressed();
                    return;
                }
            }
            else if (Canvas.isMove)
            {
                if (Canvas.isPointerDown && isPointerDown)
                {
                    int disX = Canvas.px - lastX;
                    int disY = Canvas.py - lastY;
                    lastX = Canvas.px;
                    lastY = Canvas.py;

                    xI += disX;
                    yI += disY;
                    Canvas.clearKeyPressed();
                }

                if (Canvas.isPointerHoldIn(xI, yI, ic.getWidth(), ic.getHeight()) && Canvas.isPointerDown && Canvas.isPointerJustRelease)
                {
                    isPointerDown = false;
                    Canvas.clearKeyPressed();
                }
            }
        }
        catch(Exception ex) { Debug.LogException(ex); }
    }
}