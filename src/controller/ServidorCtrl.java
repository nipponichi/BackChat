package controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import model.ClienteModel;
import model.ServidorModel;
import view.FrmServidor;

public class ServidorCtrl {

    private FrmServidor view;
    private ServidorModel servidorModel;
    private ClienteModel clienteModel;
    private ServerSocket socketServidor;
    private Socket miSocketServidor;
    private ObjectInputStream flujoEntrada;

    public ServidorCtrl(FrmServidor view, ServidorModel servidorModel) {
        this.view = view;
        this.servidorModel = servidorModel;

        Thread threadServidor = new Thread(new Runnable() {
            @Override
            public void run() {
                servidorConectado();
            }
        });

        Thread threadCliente = new Thread(new Runnable() {
            @Override
            public void run() {
                clienteConectado();
            }
        });

        Thread threadServidorPrivado = new Thread(new Runnable() {
            @Override
            public void run() {
                clientePrivado();
            }
        });

        Thread threadClienteDesconectado = new Thread(new Runnable() {
            @Override
            public void run() {
                clienteDesconectado();
            }
        });

        threadServidor.start();
        threadCliente.start();
        threadServidorPrivado.start();
        threadClienteDesconectado.start();
    }

    /**
     * Envia mensaje a chat grupal del resto de clientes
     */
    public void servidorConectado() {
        try {
            ServerSocket socketServidor = new ServerSocket(9999);
            while (true) {
                Socket miSocketServidor = socketServidor.accept();
                ObjectInputStream flujoEntrada = new ObjectInputStream(miSocketServidor.getInputStream());
                clienteModel = (ClienteModel) flujoEntrada.readObject();

                view.txtChat.append("\n" + clienteModel.getNick() + " : " + clienteModel.getMensaje() + " desde " + clienteModel.getIp());

                for (String ip : servidorModel.getIPs().values()) {
                    if (!ip.equals(clienteModel.getIp())) {
                        System.out.println("Mensaje enviado a: " + ip);
                        try (Socket enviaMensaje = new Socket(ip, 9090);
                                ObjectOutputStream paqueteSalida = new ObjectOutputStream(enviaMensaje.getOutputStream())) {
                            paqueteSalida.writeObject(clienteModel);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                miSocketServidor.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Envia objeto cliente al resto de usuarios conectados para notificar la
     * conexión del usuario
     */
    public void clienteConectado() {
        try {
            ServerSocket socketServidor = new ServerSocket(9993);

            while (true) {
                Socket miSocketServidor = socketServidor.accept();
                ObjectInputStream flujoEntrada = new ObjectInputStream(miSocketServidor.getInputStream());
                clienteModel = (ClienteModel) flujoEntrada.readObject();

                view.txtChat.append("\n" + clienteModel.getNick() + " se ha conectado al chat");

                servidorModel.getIPs().put(clienteModel.getNick(), clienteModel.getIp());

                //Envia el objeto cliente para notificar que el usuario se ha conectado
                for (String ip : servidorModel.getIPs().values()) {
                    try (Socket enviaCliente = new Socket(ip, 9091);
                            ObjectOutputStream paqueteSalida = new ObjectOutputStream(enviaCliente.getOutputStream())) {
                        paqueteSalida.writeObject(servidorModel);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                miSocketServidor.close();
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Envia mensaje privado y nick del emisor a cliente destinatario
     */
    public void clientePrivado() {
        try {
            ServerSocket socketServidor = new ServerSocket(9997);
            ServerSocket socketServidor2 = new ServerSocket(9996);
            String nick, IP, mensaje;
            ClienteModel clienteRecibido;

            while (true) {

                Socket miSocketServidor = socketServidor.accept();
                Socket miSocketServidor2 = socketServidor2.accept();
                ObjectInputStream flujoEntrada = new ObjectInputStream(miSocketServidor.getInputStream());
                DataInputStream clienteEntrada = new DataInputStream(miSocketServidor2.getInputStream());
                clienteRecibido = (ClienteModel) flujoEntrada.readObject();
                nick = (String) clienteEntrada.readUTF();
                view.txtChat.append("\nMensaje privado: " + clienteRecibido.getMensaje() + " para " + clienteRecibido.getNick() + " de " + nick);

                Socket enviaCliente = new Socket(clienteRecibido.getIp(), 9095);
                Socket enviaNick = new Socket(clienteRecibido.getIp(), 9094);
                ObjectOutputStream paqueteSalida = new ObjectOutputStream(enviaCliente.getOutputStream());
                DataOutputStream clienteSalida = new DataOutputStream(enviaNick.getOutputStream());
                paqueteSalida.writeObject(clienteRecibido);
                clienteSalida.writeUTF(nick);

                paqueteSalida.close();
                clienteSalida.close();
                enviaCliente.close();
                miSocketServidor.close();
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Actualiza el Map de clientes del server cuando un cliente se desconecta.
     */
    public void clienteDesconectado() {
        try {
            ServerSocket socketServidor = new ServerSocket(9987);

            while (true) {
                Socket miSocketServidor = socketServidor.accept();
                ObjectInputStream flujoEntrada = new ObjectInputStream(miSocketServidor.getInputStream());
                clienteModel = (ClienteModel) flujoEntrada.readObject();

                view.txtChat.append("\n" + clienteModel.getNick() + " se ha desconectado del chat");

                servidorModel.setIPs(servidorModel.BorrarIPs(clienteModel.getNick()));

                //Envia el objeto servidor y clienteModel con el MAP actualizado para notificar que el usuario se ha desconectado
                for (String ip : servidorModel.getIPs().values()) {
                    try (Socket enviaCliente = new Socket(ip, 9081);
                        ObjectOutputStream paqueteSalida = new ObjectOutputStream(enviaCliente.getOutputStream())) {
                            paqueteSalida.writeObject(servidorModel);
                            paqueteSalida.writeObject(clienteModel);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                miSocketServidor.close();
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
