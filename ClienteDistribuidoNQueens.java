import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;

public class ClienteDistribuidoNQueens {
    private static final int PORTA = 12345;
    private static final String HOST = "localhost"; // Substitua pelo IP do servidor, se necessário
    private static int n;
    private static List<int[]> solucoes;
    private static int indiceAtual = 0;
    private static JFrame janela;
    private static JPanel painelTabuleiro;

    public static void main(String[] args) {
        try {
            iniciarSimulacao();
        } catch (ExcecaoSemSolucao e) {
            fecharJanelaSeExistir();  // Certifica-se de que a janela está fechada antes de exibir o pop-up
            JOptionPane.showMessageDialog(null, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            perguntarNovaSimulacao(); // Pergunta se o usuário deseja realizar outra simulação após a exceção
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para iniciar a simulação
    private static void iniciarSimulacao() throws IOException, ExcecaoSemSolucao {
        // Solicita ao usuário o número de rainhas
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite o número de rainhas: ");
        n = scanner.nextInt();

        // Marca o tempo de início
        long inicioTempo = System.nanoTime();

        // Conecta ao servidor
        Socket socket = new Socket(HOST, PORTA);
        ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());

        // Lista para armazenar as soluções encontradas
        solucoes = new ArrayList<>();
        
        // Calcula todas as soluções possíveis
        for (int i = 0; i < n; i++) {
            int[] rainhas = new int[n];
            rainhas[0] = i;
            resolverNQueens(rainhas, 1, solucoes);
        }

        // Calcula o tempo de execução
        long fimTempo = System.nanoTime();
        long duracao = (fimTempo - inicioTempo) / 1_000_000; // Tempo em milissegundos

        // Exibe o tempo de execução no console, mesmo se não houver soluções
        System.out.println("Tempo de execução (ms): " + duracao);

        // Verifica se há soluções encontradas
        if (solucoes.isEmpty()) {
            throw new ExcecaoSemSolucao("Nenhuma solução possível encontrada para " + n + " rainhas.");
        }

        // Exibe o número de soluções no console
        System.out.println("Número total de soluções: " + solucoes.size());

        // Envia as soluções para o servidor
        saida.writeObject(solucoes);
        saida.close();
        socket.close();

        // Exibe a interface gráfica com as soluções encontradas
        SwingUtilities.invokeLater(() -> criarInterfaceGrafica());
    }

    // Método recursivo para resolver o problema das N Rainhas
    private static void resolverNQueens(int[] rainhas, int linha, List<int[]> solucoes) {
        if (linha == rainhas.length) {
            solucoes.add(rainhas.clone()); // Adiciona a solução encontrada
        } else {
            for (int coluna = 0; coluna < rainhas.length; coluna++) {
                if (posicaoSegura(rainhas, linha, coluna)) {
                    rainhas[linha] = coluna;
                    resolverNQueens(rainhas, linha + 1, solucoes); // Recursão para a próxima linha
                }
            }
        }
    }

    // Verifica se a posição atual é segura (não há ataques)
    private static boolean posicaoSegura(int[] rainhas, int linha, int coluna) {
        for (int i = 0; i < linha; i++) {
            if (rainhas[i] == coluna || Math.abs(rainhas[i] - coluna) == Math.abs(i - linha)) {
                return false; // Há ataque de outra rainha
            }
        }
        return true; // Posição segura
    }

    // Cria a interface gráfica para exibir as soluções
    private static void criarInterfaceGrafica() {
        janela = new JFrame("Solução N Rainhas");
        janela.setSize(n * 50, n * 50 + 50);
        janela.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Adiciona o WindowListener para verificar quando a janela é fechada
        janela.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                fecharJanelaSeExistir();  // Fecha a janela antes de perguntar sobre nova simulação
                perguntarNovaSimulacao();
            }
        });

        painelTabuleiro = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                desenharTabuleiro(g, solucoes.get(indiceAtual)); // Desenha a solução atual
            }
        };

        painelTabuleiro.setPreferredSize(new Dimension(n * 50, n * 50));
        janela.add(painelTabuleiro, BorderLayout.CENTER);

        JPanel painelControle = new JPanel();
        JButton botaoVoltar = new JButton("Voltar");
        JButton botaoProximo = new JButton("Próximo");

        botaoVoltar.addActionListener(e -> mostrarSolucao(indiceAtual - 1));
        botaoProximo.addActionListener(e -> mostrarSolucao(indiceAtual + 1));

        painelControle.add(botaoVoltar);
        painelControle.add(botaoProximo);
        janela.add(painelControle, BorderLayout.SOUTH);

        janela.setVisible(true);
    }

    // Desenha o tabuleiro de xadrez e as rainhas na solução atual
    private static void desenharTabuleiro(Graphics g, int[] rainhas) {
        for (int linha = 0; linha < n; linha++) {
            for (int coluna = 0; coluna < n; coluna++) {
                if ((linha + coluna) % 2 == 0) {
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(Color.GRAY);
                }
                g.fillRect(coluna * 50, linha * 50, 50, 50);
            }
        }

        // Desenha as rainhas
        g.setColor(Color.RED);
        for (int linha = 0; linha < n; linha++) {
            int coluna = rainhas[linha];
            g.fillOval(coluna * 50 + 10, linha * 50 + 10, 30, 30);
        }
    }

    // Exibe a solução na posição do índice fornecido
    private static void mostrarSolucao(int indice) {
        if (indice >= 0 && indice < solucoes.size()) {
            indiceAtual = indice;
            painelTabuleiro.repaint();
        }
    }

    // Fecha a janela do tabuleiro se ela existir
    private static void fecharJanelaSeExistir() {
        if (janela != null && janela.isDisplayable()) {
            janela.dispose();
        }
    }

    // Pergunta ao usuário se ele deseja fazer outra simulação
    private static void perguntarNovaSimulacao() {
        fecharJanelaSeExistir();  // Fecha a janela antes de perguntar sobre nova simulação
        int resposta = JOptionPane.showConfirmDialog(null, "Deseja fazer outra simulação?", "Nova Simulação", JOptionPane.YES_NO_OPTION);
        if (resposta == JOptionPane.YES_OPTION) {
            try {
                iniciarSimulacao();
            } catch (IOException | ExcecaoSemSolucao e) {
                fecharJanelaSeExistir();  // Certifica-se de que a janela está fechada antes de exibir o pop-up
                JOptionPane.showMessageDialog(null, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                perguntarNovaSimulacao();
            }
        } else {
            System.exit(0);
        }
    }

    // Classe de exceção personalizada para quando não houver solução possível
    static class ExcecaoSemSolucao extends Exception {
        public ExcecaoSemSolucao(String mensagem) {
            super(mensagem);
        }
    }
}
