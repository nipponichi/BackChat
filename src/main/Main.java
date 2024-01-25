package main;

import controller.MainMenuCtrl;
import view.FrmMainMenu;

public class Main {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmMainMenu frmMainMenu = new FrmMainMenu();
                MainMenuCtrl mainMenuCtrl = new MainMenuCtrl(frmMainMenu);
                frmMainMenu.setVisible(true);
            }
        });
    }
}
