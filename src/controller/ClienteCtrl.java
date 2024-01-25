package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.*;
import view.*;

public class ClienteCtrl implements ActionListener {

    private FrmCliente viewCliente;
    private FrmMainMenu viewMainMenu;
    private ClienteModel clienteModel;
    private ServidorModel servidorModel;
    String ipCliente, nick, ip, ipServidor, mensaje, ipPrivada;
    List<String> nicks = new ArrayList();
    List<String> ips = new ArrayList();

    public ClienteCtrl(FrmCliente view, ClienteModel clienteModel, ServidorModel servidorModel) {
        this.viewCliente = view;
        this.clienteModel = clienteModel;
        this.servidorModel = servidorModel;
        view.btnEnviar.addActionListener(this);
        view.btnPrivado.addActionListener(this);
        addWindowCloseListener();

        Thread threadCliente = new Thread(new Runnable() {
            @Override
            public void run() {
                clienteConectado();
            }
        });

        Thread threadMensaje = new Thread(new Runnable() {
            @Override
            public void run() {
                mensajeChat();
            }
        });

        Thread threadClientePrivado = new Thread(new Runnable() {
            @Override
            public void run() {
                escuchaClientePrivado();
            }
        });

        Thread threadClienteDesconectado = new Thread(new Runnable() {
            @Override
            public void run() {
                escuchaClienteDesconectado();
            }
        });

        threadMensaje.start();
        threadCliente.start();
        threadClientePrivado.start();
        threadClienteDesconectado.start();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == viewCliente.btnEnviar) {
            enviarMensaje();
        } else if (ae.getSource() == viewCliente.btnPrivado) {
            enviarMensajePrivado();
        }
    }

    /**
     * Queda en escucha para recibir mensajes de chat grupal
     */
    public void mensajeChat() {
        try {
            ServerSocket socketEscucha = new ServerSocket(9090);
            Socket socketClienteRecibe;
            ClienteModel clienteRecibido;
            while (true) {
                socketClienteRecibe = socketEscucha.accept();
                ObjectInputStream flujoEntrada = new ObjectInputStream(socketClienteRecibe.getInputStream());
                clienteRecibido = (ClienteModel) flujoEntrada.readObject();
                viewCliente.txtChat.append("\n" + clienteRecibido.getNick() + " escribió: " + clienteRecibido.getMensaje());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Queda en escucha para notificar clientes conectados
     */
    public void clienteConectado() {
        try {
            ipServidor = (String) servidorModel.getIP();
            Socket socketCliente = new Socket(ipServidor, 9993);
            ObjectOutputStream flujoSalida = new ObjectOutputStream((socketCliente.getOutputStream()));
            flujoSalida.writeObject(clienteModel);

            ServerSocket socketEscucha = new ServerSocket(9091);
            Socket socketClienteRecibe;
            ServidorModel clienteRecibido;

            DefaultTableModel model = (DefaultTableModel) viewCliente.tbClientes.getModel();

            while (true) {
                socketClienteRecibe = socketEscucha.accept();
                ObjectInputStream flujoEntrada = new ObjectInputStream(socketClienteRecibe.getInputStream());
                clienteRecibido = (ServidorModel) flujoEntrada.readObject();
                servidorModel = clienteRecibido;
                llenarTabla(clienteRecibido.getIPs(), model);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Envia mensajes privados a otro usuario
     */
    public void enviarMensajePrivado() {
        ClienteModel clientePrivado = new ClienteModel();
        int filaSeleccionada = viewCliente.tbClientes.getSelectedRow();

        if (filaSeleccionada != -1) {

            try {
                for (String valor : servidorModel.getIPs().values()) {
                    System.out.println("Valores Map: " + valor);
                }

                String nick = (String) viewCliente.tbClientes.getValueAt(filaSeleccionada, 0);
                String ip = servidorModel.obtenerIp(nick);

                clientePrivado.setNick(nick);
                clientePrivado.setIp(ip);
                ips.add(ip);
                clientePrivado.setIpPrivadas(ips);

                if (clientePrivado != null && clientePrivado.getIpPrivadas() != null) {
                    for (int i = 0; i < ips.size(); i++) {
                        System.out.println(i + ".IPS: " + ips.get(i));
                    }
                }
                mensaje = viewCliente.txtMensaje.getText();
                clientePrivado.setMensaje(mensaje);
                ipServidor = servidorModel.getIP();
                limpiarMensaje();
                Socket socketCliente = new Socket(ipServidor, 9997);
                Socket socketCliente2 = new Socket(ipServidor, 9996);
                viewCliente.txtChat.append("\nMP para " + clientePrivado.getNick() + ": " + mensaje);
                ObjectOutputStream flujoSalida = new ObjectOutputStream((socketCliente.getOutputStream()));
                DataOutputStream flujoCliente = new DataOutputStream((socketCliente2.getOutputStream()));
                flujoSalida.writeObject(clientePrivado);
                flujoCliente.writeUTF(clienteModel.getNick());
                socketCliente.close();
                socketCliente2.close();
            } catch (IOException ex) {
                Logger.getLogger(ClienteCtrl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /*
      Queda en escucha para recibir mensajes de chat privado
     */
    public void escuchaClientePrivado() {
        try {
            ServerSocket socketEscucha = new ServerSocket(9095);
            ServerSocket socketEscucha2 = new ServerSocket(9094);
            Socket socketClienteRecibe;
            Socket socketClienteRecibe2;
            ClienteModel clienteRecibido;
            String nick;

            while (true) {
                socketClienteRecibe = socketEscucha.accept();
                socketClienteRecibe2 = socketEscucha2.accept();
                ObjectInputStream flujoEntrada = new ObjectInputStream(socketClienteRecibe.getInputStream());
                DataInputStream clienteEntrada = new DataInputStream(socketClienteRecibe2.getInputStream());
                clienteRecibido = (ClienteModel) flujoEntrada.readObject();
                nick = (String) clienteEntrada.readUTF();
                viewCliente.txtChat.append("\nMP de " + nick + " : " + clienteRecibido.getMensaje());
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Elimina el texto del mensaje enviado por el usuario
     */
    public void limpiarMensaje() {
        viewCliente.txtMensaje.setText("");
    }

    /**
     * Publica el mensaje enviado en primera persona en el chat general
     */
    public void escribeMensaje() {
        viewCliente.txtChat.append("\nDijiste: " + mensaje);
    }

    /**
     * Introduce en la tabla al usuario que se conecta al chat
     *
     * @param IPs
     * @param model
     */
    public void llenarTabla(Map<String, String> IPs, DefaultTableModel model) {
        model.setRowCount(0);

        for (Map.Entry<String, String> entry : IPs.entrySet()) {
            String nick = entry.getKey();
            ipPrivada = entry.getValue();

            bienvenidaNick(nick);

            model.addRow(new Object[]{nick});
        }
    }

    /**
     * Notifica en chat grupal la conexión de un usuario
     *
     * @param nick
     */
    public void bienvenidaNick(String nick) {
        if (!nicks.contains(nick)) {
            nicks.add(nick);
            viewCliente.txtChat.append("\n" + nick + " se ha unido al chat");
        }
    }

    /**
     * Envía un mensaje al chat general
     */
    public void enviarMensaje() {
        try {
            ipServidor = (String) servidorModel.getIP();
            mensaje = viewCliente.txtMensaje.getText().trim();
            clienteModel.setMensaje(mensaje);

            Socket socketCliente = new Socket(ipServidor, 9999);

            ObjectOutputStream flujoSalida = new ObjectOutputStream((socketCliente.getOutputStream()));

            flujoSalida.writeObject(clienteModel);

            socketCliente.close();
            escribeMensaje();
            limpiarMensaje();

        } catch (ConnectException ex) {
            ex.printStackTrace();
            System.out.println("Conexion rechazada: " + ex.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
    }

    public void addWindowCloseListener() {
        viewCliente.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showOptionDialog(viewCliente,
                        "¿Estás seguro de que quieres abandonar el chat?",
                        "Confirmación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, null, null);
                if (confirm == 0) {
                    clienteDesconectado();
                    viewCliente.dispose();
                }
            }
        });
    }

    public void clienteDesconectado() {
        try {

            ipServidor = (String) servidorModel.getIP();

            Socket socketCliente = new Socket(ipServidor, 9987);

            ObjectOutputStream flujoSalida = new ObjectOutputStream((socketCliente.getOutputStream()));

            flujoSalida.writeObject(clienteModel);

            socketCliente.close();

        } catch (ConnectException ex) {
            ex.printStackTrace();
            System.out.println("Conexion rechazada: " + ex.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
    }

    public void escuchaClienteDesconectado() {
        try {
            ServerSocket socketEscucha = new ServerSocket(9081);
            Socket socketClienteRecibe;
            ServidorModel clienteRecibido;
            ClienteModel clienteDesconectado;
            DefaultTableModel model = (DefaultTableModel) viewCliente.tbClientes.getModel();

            while (true) {
                socketClienteRecibe = socketEscucha.accept();
                ObjectInputStream flujoEntrada = new ObjectInputStream(socketClienteRecibe.getInputStream());
                clienteRecibido = (ServidorModel) flujoEntrada.readObject();
                clienteDesconectado = (ClienteModel) flujoEntrada.readObject();
                viewCliente.txtChat.append("\n" + clienteDesconectado.getNick() + " se ha desconectado");
                llenarTabla(clienteRecibido.getIPs(), model);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}
