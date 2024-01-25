package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ServidorModel implements Serializable {

    private String nick;
    private String IP;
    private Map<String, String> IPs;
    private String mensaje;

    public ServidorModel() {
        this.IPs = new HashMap<>();
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public Map<String, String> getIPs() {
        return IPs;
    }

    public void setIPs(Map<String, String> IPs) {
        this.IPs = IPs;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String obtenerIp(String nombre) {
        return this.getIPs().get(nombre);
    }

    public Map<String, String> BorrarIPs(String nickDesconectado) {
        IPs.remove(nickDesconectado);
        return IPs;
    }
}
