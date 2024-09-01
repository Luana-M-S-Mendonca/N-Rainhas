import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NQueensSequencial {
    private static int indiceAtual = 0;
    private static List<int[]> solucoes;
    private static JFrame janela;
    private static JPanel painelTabuleiro;
    private static int n;

    public static void main(String[] args) {
        iniciarSimulacao();
    }

    // Método para iniciar a simulação
    private static void iniciarSimulacao() {
        // Solicita ao usuário o número de rainhas
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite o número de rainhas: ");
        n = scanner.nextInt();

        long inicioTempo = System.nanoTime();

        solucoes = new ArrayList<>();
        resolverNQueens(new int[n], 0, solucoes);

        long fimTempo = System.nanoTime();
        long duracao = (fimTempo - inicioTempo) / 1_000; // Tempo em microsegundos

        System.out.println("Número total de soluções: " + solucoes.size());
        System.out.println("Tempo de execução (µs): " + duracao);

        try {
            // Lança exceção se não houverem soluções possíveis
            if (solucoes.isEmpty()) {
                throw new ExcecaoSemSolucao("Nenhuma solução possível encontrada para " + n + " rainhas.");
            }

            // Exibe a interface gráfica com as soluções encontradas
            SwingUtilities.invokeLater(() -> criarInterfaceGrafica());
        } catch (ExcecaoSemSolucao e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            perguntarNovaSimulacao();
        }
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

        janela.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
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

    // Pergunta ao usuário se ele deseja fazer outra simulação
    private static void perguntarNovaSimulacao() {
        int resposta = JOptionPane.showConfirmDialog(null, "Deseja fazer outra simulação?", "Nova Simulação", JOptionPane.YES_NO_OPTION);
        if (resposta == JOptionPane.YES_OPTION) {
            iniciarSimulacao();
        } else {
            System.exit(0);
        }
    }

    // Classe de exceção para quando não houver solução possível
    static class ExcecaoSemSolucao extends Exception {
        public ExcecaoSemSolucao(String mensagem) {
            super(mensagem);
        }
    }
}
