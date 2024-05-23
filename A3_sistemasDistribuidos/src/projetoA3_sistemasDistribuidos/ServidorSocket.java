package projetoA3_sistemasDistribuidos;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorSocket {
    private final int porta;

    public ServidorSocket(int porta) {
        this.porta = porta;
    }

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            System.out.println("Servidor rodando na porta " + porta);

            while (true) {
                Socket socket = serverSocket.accept(); // Aceitando novas conexões
                System.out.println("Novo cliente conectado");

                ConexaoSocket clienteThread = new ConexaoSocket(socket);
                clienteThread.start(); // Iniciando uma nova thread para o cliente conectado
            }
        } catch (IOException ex) {
            System.out.println("Erro no servidor: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        int porta = 12345; // Porta do servidor
        ServidorSocket servidor = new ServidorSocket(porta);
        servidor.iniciar(); // Iniciando o servidor
    }
}