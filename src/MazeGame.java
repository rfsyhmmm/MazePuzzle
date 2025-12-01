import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

public class MazeGame extends JFrame {

    private MazePanel mazePanel;
    private JTextArea logArea;
    private JComboBox<String> algoSelector;

    public MazeGame() {
        setTitle("Java Maze Solver v0.4: Prim's & Kruskal's");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        mazePanel = new MazePanel(30, 20); // Grid Size
        add(mazePanel, BorderLayout.CENTER);

        // --- CONTROL PANEL ---
        JPanel controlPanel = new JPanel();
        controlPanel.setPreferredSize(new Dimension(300, 0));
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // -- New: Algorithm Selector --
        controlPanel.add(new JLabel("Gen Algorithm:"));
        String[] algorithms = {"Prim's", "Kruskal's"};
        algoSelector = new JComboBox<>(algorithms);
        algoSelector.setMaximumSize(new Dimension(280, 30));
        controlPanel.add(algoSelector);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Buttons
        JButton btnGen = new JButton("Generate Maze");
        JButton btnBFS = new JButton("Solve BFS");
        JButton btnDFS = new JButton("Solve DFS");
        JButton btnDijkstra = new JButton("Solve Dijkstra");

        Dimension btnSize = new Dimension(280, 40);
        JButton[] buttons = {btnGen, btnBFS, btnDFS, btnDijkstra};
        for (JButton b : buttons) {
            b.setMaximumSize(btnSize);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            controlPanel.add(b);
            controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        addLegend(controlPanel);

        // Log Area
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        controlPanel.add(new JLabel("Algorithmic Decision Log:"));
        controlPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        logArea = new JTextArea(10, 20);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(logArea);
        controlPanel.add(scrollPane);

        mazePanel.setLogArea(logArea);
        add(controlPanel, BorderLayout.EAST);

        // Action Listeners
        btnGen.addActionListener(e -> {
            logArea.setText("");
            String selectedAlgo = (String) algoSelector.getSelectedItem();
            mazePanel.generateMaze(selectedAlgo.startsWith("Kruskal") ? "Kruskal" : "Prim");
            log("Maze Generated using " + selectedAlgo + " Algorithm.");
        });

        btnBFS.addActionListener(e -> mazePanel.solveBFS());
        btnDFS.addActionListener(e -> mazePanel.solveDFS());
        btnDijkstra.addActionListener(e -> mazePanel.solveDijkstra());

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
    }

    private void addLegend(JPanel panel) {
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(new JLabel("Terrain Legend:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        for (Terrain t : Terrain.values()) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JPanel colorBox = new JPanel();
            colorBox.setPreferredSize(new Dimension(20, 20));
            colorBox.setBackground(t.color);
            colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            p.add(colorBox);
            p.add(new JLabel(t.name() + " (+" + (t.cost - 1) + ")"));
            panel.add(p);
        }
    }
}