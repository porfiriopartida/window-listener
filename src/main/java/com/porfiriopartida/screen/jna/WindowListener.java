package com.porfiriopartida.screen.jna;
import com.porfiriopartida.screen.application.ScreenApplication;
import com.sun.jna.Native;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;

import java.util.Observable;
import java.util.Observer;

public class WindowListener extends Observable implements Runnable {
    //TODO: Every once in a while verify that the current window is actually the right scene.
    private int delay = 200; //TODO: Implement delay acc, if you tab switch check every n ms for the next m seconds. Meaning that you may have switched and then back again.
    private String lastWindow = "";
    private WindowListener.User32 INSTANCE;
    public WindowListener(){
        INSTANCE = Native.load("user32", User32.class);
    }
    public String getLastWindow() {
        return lastWindow;
    }
    public void run(){
        try {
            while(true){
                Thread.sleep(delay);
                PointerType hwnd = INSTANCE.GetForegroundWindow(); // then you can call it!
                byte[] windowText = new byte[512];
                INSTANCE.GetWindowTextA(hwnd, windowText, 512);
                String newString = Native.toString(windowText);
                if(newString != null && newString.equalsIgnoreCase(this.lastWindow)){
                    continue;
                }

                this.lastWindow = newString;
                setChanged();
                notifyObservers(this.lastWindow);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public interface User32 extends StdCallLibrary {
        WinDef.HWND GetForegroundWindow();
        int GetWindowTextA(PointerType hWnd, byte[] lpString, int nMaxCount);
    }

    /**
     * Just prints the screen you are focusing.
     * @see ScreenApplication For app handling
     * @param args Not used.
     */
    public static void main(String[] args) {
        WindowListener windowListener = new WindowListener();
        Observer soutObserver = (o, s) -> System.out.println(s);
        windowListener.addObserver(soutObserver);
        new Thread(windowListener).start();
    }
}
