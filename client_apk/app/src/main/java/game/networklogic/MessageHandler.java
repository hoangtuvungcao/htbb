package game.networklogic;

import game.model.Actor;
import game.model.Ground;
import game.render.GCanvas;
import game.render.screen.NqshMod;
import java.util.Vector;
import network.IMessageHandler;

public class MessageHandler extends Cmd_message implements IMessageHandler {
    public static MessageHandler instance = new MessageHandler();

    public static MessageHandler gI() {
        return instance;
    }

    @Override
    public void onConnectOK() {
        GCanvas.gameScr.onConnectOK();
    }

    @Override
    public void onConnectionFail() {
        GCanvas.gameScr.onConnectFail();
    }

    @Override
    public void onDisconnected() {
        GCanvas.gameScr.onDisconnect();
    }

    @Override
    public void onMessage(network.Message msg) {
        // mod: gói -88 (server ký xác thực) và các gói mod xử lý riêng
        if (NqshMod.goiGuard(msg)) {
            return;
        }
        switch (msg.command) {
            case -128:
            case -76: case -75: case -74: case -73:
            case -71: case -70: case -69: case -68: case -67: case -66:
            case -64: case -63: case -62: case -61: case -60: case -59:
            case -57: case -56: case -55:
            case -52: case -51: case -50: case -49: case -48: case -47: case -46:
            case -43: case -42:
            case -39: case -38: case -37:
            case -33: case -32: case -31: case -30: case -29: case -28: case -27: case -26:
            case -24:
            case 62: case 87: case 105: case 123:
                MessageRecieve2.Message2(msg);
                break;
            case -65: case -53: case -41: case -22:
            case -20: case -19: case -18: case -17:
            case -13: case -12: case -11: case -10: case -9: case -8: case -7:
            case -5:
            case 11: case 13: case 14: case 15: case 16: case 17: case 18: case 19: case 20:
            case 21: case 22: case 23: case 24: case 25: case 26: case 27: case 28: case 30:
            case 32: case 33: case 34: case 35: case 36: case 37: case 38: case 39:
            case 48: case 49: case 50: case 51: case 52: case 54: case 56: case 57: case 58: case 59:
            case 61: case 64: case 78: case 83: case 100:
                MessageRecieve1.Message1(msg);
                break;
            case -36: case -35: case -34:
            case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9: case 10: case 12:
            case 60: case 65: case 66: case 67: case 68: case 69: case 71: case 72: case 73:
            case 76: case 77: case 80: case 81: case 82: case 85: case 86:
            case 89: case 90: case 91: case 92: case 94: case 95:
            case 101: case 102: case 103: case 104: case 106:
                MessageRecieve0.Message0(msg);
                break;
            default:
                break;
        }
    }

    public int getMinMax(Vector vector, byte b, int i) {
        int size = vector.size();
        for (int i2 = 0; i2 < size; i2++) {
            Actor actor = (Actor) vector.elementAt(i2);
            if (actor.catagory == 10) {
                Ground ground = (Ground) actor;
                if (b != 0) {
                    if (b != 1) {
                        if (b == 2) {
                            if (i < ground.x) {
                                i = ground.x;
                            }
                        } else if (b == 3 && i < ground.y) {
                            i = ground.y;
                        }
                    } else if (i > ground.y) {
                        i = ground.y;
                    }
                } else if (i > ground.x) {
                    i = ground.x;
                }
            }
        }
        return i;
    }

    public int getIndexClassFromItemID(int i) {
        if (i >= 79 && i <= 113) {
            return (i - 79) / 7;
        }
        if (i >= 174 && i <= 213) {
            return (i - 174) / 8;
        }
        if (i < 214 || i > 263) {
            return 0;
        }
        return (i - 214) / 10;
    }
}
