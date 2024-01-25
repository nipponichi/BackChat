package model;

import java.util.List;
import java.io.Serializable;

public class ClienteModel implements Serializable {

    private String nick;
    private String ip;
    private String mensaje;
    private List<String> ipPrivadas;

    public ClienteModel() {

    }

    public ClienteModel(String nick, String ip) {
        this.nick = nick;
        this.ip = ip;
    }

    public ClienteModel(String nick, String ip, String mensaje) {
        this.nick = nick;
        this.ip = ip;
        this.mensaje = mensaje;
    }

    public ClienteModel(String nick, String ip, String mensaje, List<String> ipPrivadas) {
        this.nick = nick;
        this.ip = ip;
        this.mensaje = mensaje;
        this.ipPrivadas = ipPrivadas;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String message) {
        this.mensaje = message;
    }

    public List<String> getIpPrivadas() {
        return ipPrivadas;
    }

    public void setIpPrivadas(List<String> ipPrivadas) {
        this.ipPrivadas = ipPrivadas;
    }

}
