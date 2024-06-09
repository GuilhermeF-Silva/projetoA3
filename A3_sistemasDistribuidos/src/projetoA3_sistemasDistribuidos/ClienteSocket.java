package projetoA3_sistemasDistribuidos;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClienteSocket {
    public static void main(String[] args) {
        int port = 12345; // Porta do servidor

        Scanner s = new Scanner(System.in);

        while (true) {
            System.out.println("Digite o ip do servidor: ");
            String hostname = s.nextLine();

            try (Socket socket = new Socket(hostname, port)) { // Conectando ao servidor
                BufferedReader leitor = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8")); // Leitor para receber mensagens do servidor
                PrintWriter escritor = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true); // Escritor para enviar mensagens ao servidor
                BufferedReader entradaUsuario = new BufferedReader(new InputStreamReader(System.in, "UTF-8")); // Leitor para receber entrada do usuário

                String mensagemServidor;
                while ((mensagemServidor = leitor.readLine()) != null) { // Lendo mensagens do servidor
                    System.out.println("Servidor: " + mensagemServidor);
                    if (mensagemServidor.contains("Faça sua escolha:")) {
                        String escolha = entradaUsuario.readLine(); // Lendo a escolha do usuário
                        escritor.println(escolha); // Enviando a escolha ao servidor
                    } 
                    
                }
                break; // Se conectou com sucesso e escolheu sair, sai do loop
            } catch (UnknownHostException ex) {
                System.out.println("Servidor não encontrado: " + ex.getMessage()); // Tratando erro de host desconhecido
            } catch (IOException ex) {
                System.out.println("Erro de I/O: " + ex.getMessage()); // Tratando erro de I/O
            }

            System.out.println("Não foi possível se comunicar com o servidor, tente novamente.");
        }
    }
}
