import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorDistribuidoNQueens {

    private static final int PORTA = 12345; //Define o numero da porta utilizada 
    private static List<int[]> solucoes = new ArrayList<>(); //Instancia o vetor de soluções

    public static void main(String[] args) throws IOException {
        ServerSocket servidorSocket = new ServerSocket(PORTA);
        System.out.println("Servidor iniciado na porta " + PORTA);

        // Aguarda conexões de clientes e processa as soluções
        while (true) {
            Socket clienteSocket = servidorSocket.accept();
            new ManipuladorCliente(clienteSocket).start();
        }
    }

    // Adiciona uma solução ao servidor
    private static synchronized void adicionarSolucao(int[] solucao) {
        solucoes.add(solucao);
    }

    static class ManipuladorCliente extends Thread {
        private Socket clienteSocket;

        ManipuladorCliente(Socket socket) {
            this.clienteSocket = socket;
        }

        @Override
        public void run() {
            try {
                ObjectInputStream entrada = new ObjectInputStream(clienteSocket.getInputStream());
                List<int[]> solucoesCliente = (List<int[]>) entrada.readObject();
                for (int[] solucao : solucoesCliente) {
                    adicionarSolucao(solucao);
                }
                entrada.close();
                clienteSocket.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
