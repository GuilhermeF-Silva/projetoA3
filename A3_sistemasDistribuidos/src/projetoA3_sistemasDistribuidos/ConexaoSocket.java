package projetoA3_sistemasDistribuidos;
import java.io.*;
import java.net.*;
import java.util.Random;

public class ConexaoSocket extends Thread { // Classe para gerenciar a conexão com o cliente, estendendo Thread para permitir execução em paralelo
    private Socket socket;
    private PrintWriter escritor;
    private BufferedReader leitor;
    private static final Object lock = new Object(); 
    private static ConexaoSocket primeiroCliente = null;
    private static ConexaoSocket segundoCliente = null;
    private static int rodada = 0;
    private static final int totalRodadas = 3;
    private static String[] escolhas = new String[2];
    private int[] estatisticas = {0, 0, 0};

    public ConexaoSocket(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream input = socket.getInputStream();
            leitor = new BufferedReader(new InputStreamReader(input, "UTF-8"));

            OutputStream output = socket.getOutputStream();
            escritor = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true);

            escritor.println("Bem vindo ao jokenpo, por favor escolha se deseja jogar contra um robo ou contra outro jogador \n 1 - Robo (CPU) \n 2 - Outro Jogador");

            String escolhaModo = leitor.readLine();
            System.out.println("Escolha do cliente: " + escolhaModo); // Depuração

            if ("1".equals(escolhaModo)) {
                jogarContraRobo();
            } else if ("2".equals(escolhaModo)) {
                jogarContraOutroJogador();
            } else {
                escritor.println("Escolha inválida. Encerrando conexão.");
            }

        } catch (IOException | InterruptedException ex) {
            System.out.println("Erro: " + ex.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
                System.out.println("Erro ao fechar socket: " + ex.getMessage());
            }
        }
    }

    private void jogarContraRobo() throws IOException {
        Random random = new Random();
        String[] opcoes = {"pedra", "papel", "tesoura"};
        int rodada = 0;
        int totalRodadas = 3;
        int[] estatisticas = {0, 0, 0}; // Vitórias, Derrotas, Empates

        escritor.println("O jogo começou. Escolha entre pedra, papel ou tesoura");

        while (rodada < totalRodadas) {
            String escolhaJogador = leitor.readLine();
            String escolhaRobo = opcoes[random.nextInt(opcoes.length)];

            escritor.println("Robô escolheu: " + escolhaRobo);

            if (escolhaJogador.equals(escolhaRobo)) {
                escritor.println("Empate!");
                estatisticas[2]++;
            } else if ((escolhaJogador.equals("pedra") && escolhaRobo.equals("tesoura")) ||
                       (escolhaJogador.equals("papel") && escolhaRobo.equals("pedra")) ||
                       (escolhaJogador.equals("tesoura") && escolhaRobo.equals("papel"))) {
                escritor.println("Você ganhou a rodada!");
                estatisticas[0]++;
            } else {
                escritor.println("Robô ganhou a rodada!");
                estatisticas[1]++;
            }

            escritor.println("Estatísticas da rodada - Vitórias: " + estatisticas[0] + ", Derrotas: " + estatisticas[1] + ", Empates: " + estatisticas[2]);
            rodada++;
            if (rodada < totalRodadas) {
                escritor.println("Próxima rodada. Escolha entre pedra, papel ou tesoura");
            }
        }

        escritor.println("Estatísticas da partida - Vitórias: " + estatisticas[0] + ", Derrotas: " + estatisticas[1] + ", Empates: " + estatisticas[2]);
    }

    private void jogarContraOutroJogador() throws IOException, InterruptedException {
        synchronized (lock) {
            if (primeiroCliente == null) {
                primeiroCliente = this;
                escritor.println("Esperando outro jogador se conectar...");
                while (segundoCliente == null) {
                    try {
                        lock.wait(); 
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                iniciarJogo();
            } else {
                segundoCliente = this;
                lock.notifyAll(); 
            }
        }

        RespostaJogadores();
        
        if (rodada == totalRodadas) {
            finalizarJogo();
        }
    }

    private void iniciarJogo() {
        primeiroCliente.escritor.println("O jogo começou. Escolha entre pedra, papel ou tesoura");
        segundoCliente.escritor.println("O jogo começou. Escolha entre pedra, papel ou tesoura");
    }

    private void determinarVencedorRodada() throws InterruptedException, IOException {
        rodada++;
        String resultado;
        if (escolhas[0].equals(escolhas[1])) {
            resultado = "Empate!";
            primeiroCliente.estatisticas[2]++;
            segundoCliente.estatisticas[2]++;
        } else if ((escolhas[0].equals("pedra") && escolhas[1].equals("tesoura")) ||
                   (escolhas[0].equals("papel") && escolhas[1].equals("pedra")) ||
                   (escolhas[0].equals("tesoura") && escolhas[1].equals("papel"))) {
            resultado = "Primeiro cliente ganhou a rodada!";
            primeiroCliente.estatisticas[0]++;
            segundoCliente.estatisticas[1]++;
        } else {
            resultado = "Segundo cliente ganhou a rodada!";
            primeiroCliente.estatisticas[1]++;
            segundoCliente.estatisticas[0]++;
        }

        primeiroCliente.escritor.println("Estatísticas da rodada - Vitórias: " + primeiroCliente.estatisticas[0] + ", Derrotas: " + primeiroCliente.estatisticas[1] + ", Empates: " + primeiroCliente.estatisticas[2]);
        segundoCliente.escritor.println("Estatísticas da rodada - Vitórias: " + segundoCliente.estatisticas[0] + ", Derrotas: " + segundoCliente.estatisticas[1] + ", Empates: " + segundoCliente.estatisticas[2]);

        if (rodada < totalRodadas) {
            primeiroCliente.escritor.println("Próxima rodada. Escolha entre pedra, papel ou tesoura");
            segundoCliente.escritor.println("Próxima rodada. Escolha entre pedra, papel ou tesoura");
        }
    }

    private void finalizarJogo() {
        primeiroCliente.escritor.println("Estatísticas da partida - Vitórias: " + primeiroCliente.estatisticas[0] + ", Derrotas: " + primeiroCliente.estatisticas[1] + ", Empates: " + primeiroCliente.estatisticas[2]);
        segundoCliente.escritor.println("Estatísticas da partida - Vitórias: " + segundoCliente.estatisticas[0] + ", Derrotas: " + segundoCliente.estatisticas[1] + ", Empates: " + segundoCliente.estatisticas[2]);
    }

    private void RespostaJogadores() throws InterruptedException, IOException {
        while (rodada < totalRodadas) {
            String escolha = leitor.readLine(); 
            synchronized (lock) {
                if (this == primeiroCliente) {
                    escolhas[0] = escolha;
                    escritor.println("Esperando resposta do outro jogador...");
                } else {
                    escolhas[1] = escolha;
                    escritor.println("Esperando resposta do outro jogador...");
                }
                
                if (escolhas[0] != null && escolhas[1] != null) {
                    determinarVencedorRodada();
                    escolhas[0] = null;
                    escolhas[1] = null;
                    lock.notifyAll();
                } else {
                    while (escolhas[0] == null && escolhas[1] == null) {
                        lock.wait();
                    }
                }
            }
        }
    }
}