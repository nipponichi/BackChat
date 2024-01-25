package controller;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import model.*;
import view.FrmMainMenu;
import view.FrmCliente;
import view.FrmServidor;

public class MainMenuCtrl implements ActionListener {

    FrmMainMenu frmMainMenu;
    ClienteModel clienteModel = new ClienteModel();
    ServidorModel servidorModel = new ServidorModel();
    Map<String, String> IPs = new HashMap<>();
    String nick, ip, ipServidor, ipAddress;

    public MainMenuCtrl(FrmMainMenu frmMainMenu) {
        this.frmMainMenu = frmMainMenu;
        this.frmMainMenu.btnAcceder.addActionListener(this);
        this.frmMainMenu.cbSelectorModo.addActionListener(this);

        ImageIcon logoIcon = new ImageIcon(getClass().getResource("/resources/logo.png"));
        Image image = logoIcon.getImage();
        Image newimg = image.getScaledInstance(150, 150, java.awt.Image.SCALE_SMOOTH);
        ImageIcon finalLogoIcon = new ImageIcon(newimg);
        frmMainMenu.lblLogo.setIcon(finalLogoIcon);
        
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == frmMainMenu.cbSelectorModo) {
            selectorModo();
        } else if (ae.getSource() == frmMainMenu.btnAcceder) {

            boolean modo = selectorModo();

            nick = frmMainMenu.txtNick.getText();
            ip = frmMainMenu.txtIpCliente.getText();
            ipServidor = frmMainMenu.txtIpServidor.getText();
            clienteModel.setNick(nick);
            clienteModel.setIp(ip);
            servidorModel.setIP(ipServidor);
            modoSeleccionado(modo, clienteModel, servidorModel);
        }
    }
    /**
     * Habilida o desabilita campos de la pantalla de inicio segun el modo
     * de uso escogido
     * @return Boolean
     */
    public boolean selectorModo() {
        if (frmMainMenu.cbSelectorModo.getSelectedIndex() == 0) {
            frmMainMenu.txtNick.setEnabled(true);
            frmMainMenu.txtIpCliente.setEnabled(true);
            return true;
        } else {

            frmMainMenu.txtNick.setEnabled(false);
            frmMainMenu.txtIpCliente.setEnabled(false);
            return false;
        }
    }

    /**
     * Toma los datos de cliente y/o servidor segun el modo seleccionado
     * @param modo
     * @param clienteModel
     * @param servidorModel 
     */
    public void modoSeleccionado(boolean modo, ClienteModel clienteModel, ServidorModel servidorModel) {
        if (modo) {
            FrmCliente frmCliente = new FrmCliente();
            System.out.println("IP Cliente: " + clienteModel.getIp());
            System.out.println("IP Servidor: " + servidorModel.getIP());
            ClienteCtrl clienteCtrl = new ClienteCtrl(frmCliente, clienteModel, servidorModel);
            frmCliente.setVisible(true);
        } else {
            FrmServidor frmServidor = new FrmServidor();
            ServidorCtrl servidorCtrl = new ServidorCtrl(frmServidor, servidorModel);
            frmServidor.setVisible(true);
        }
    }
}
