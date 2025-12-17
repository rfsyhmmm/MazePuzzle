import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;

public class MazeGame extends JFrame {

    private MazePanel mazePanel;
    private JTextArea logArea;
    private JComboBox<String> algoSelector;
    
    // Palette Warna Modern
    private final Color PANEL_BG = new Color(40, 44, 52);       // Dark Grey
    private final Color TEXT_COLOR = new Color(220, 223, 228);  // Off-white
    private final Color BTN_GEN_COLOR = new Color(152, 195, 121); // Pastel Green
    private final Color BTN_SOLVE_COLOR = new Color(97, 175, 239); // Pastel Blue
    private final Color LOG_BG = new Color(30, 33, 39);         // Darker for Log
    private final Color LOG_TEXT = new Color(152, 195, 121);    // Terminal Green

    public MazeGame() {
        setTitle("Java Maze Solver v0.5: UI Overhaul");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- GRID AREA (CENTER) ---
        mazePanel = new MazePanel(30, 20); 
        // Beri sedikit padding di sekitar maze agar tidak nempel pinggir
        JPanel mazeContainer = new JPanel(new GridBagLayout());
        mazeContainer.setBackground(new Color(60, 63, 68)); // Background belakang maze
        mazeContainer.add(mazePanel);
        add(mazeContainer, BorderLayout.CENTER);

        // --- CONTROL PANEL (RIGHT) ---
        JPanel controlPanel = new JPanel();
        controlPanel.setPreferredSize(new Dimension(320, 0));
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(PANEL_BG);
        controlPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Margin dalam

        // 1. SECTION: GENERATOR
        addHeader(controlPanel, "1. MAZE GENERATION");
        
        controlPanel.add(createLabel("Select Algorithm:"));
        
        String[] algorithms = {"Prim's Algorithm", "Kruskal's Algorithm"};
        algoSelector = new JComboBox<>(algorithms);
        styleComboBox(algoSelector);
        controlPanel.add(algoSelector);
        
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JButton btnGen = createStyledButton("Generate Maze", BTN_GEN_COLOR, true);
        controlPanel.add(btnGen);
        
        controlPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Spacer antar section

        // 2. SECTION: SOLVER
        addHeader(controlPanel, "2. PATH FINDING");
        
        JButton btnBFS = createStyledButton("Solve with BFS", BTN_SOLVE_COLOR, false);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        JButton btnDFS = createStyledButton("Solve with DFS", BTN_SOLVE_COLOR, false);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        JButton btnDijkstra = createStyledButton("Solve with Dijkstra", BTN_SOLVE_COLOR, false);
        
        // ... kode sebelumnya (deklarasi tombol) ...

        controlPanel.add(btnBFS);
        // Tambah jarak 10 pixel
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10))); 
        
        controlPanel.add(btnDFS);
        // Tambah jarak 10 pixel
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        controlPanel.add(btnDijkstra);

        // ... kode selanjutnya ...

        controlPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        // 3. SECTION: LEGEND
        addHeader(controlPanel, "TERRAIN LEGEND");
        addLegend(controlPanel); // Method legend yang sudah kita modif sebelumnya
        
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 4. SECTION: LOG
        addHeader(controlPanel, "DECISION LOG");
        
        logArea = new JTextArea(12, 20);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(LOG_BG);
        logArea.setForeground(LOG_TEXT);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Auto-scroll log
        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new LineBorder(new Color(80, 80, 80))); // Border tipis
        controlPanel.add(scrollPane);

        mazePanel.setLogArea(logArea);
        add(controlPanel, BorderLayout.EAST);

        // --- ACTION LISTENERS ---
        btnGen.addActionListener(e -> {
            logArea.setText("");
            String selectedAlgo = (String) algoSelector.getSelectedItem();
            // Mapping nama di combobox ke parameter string
            String code = selectedAlgo.contains("Kruskal") ? "Kruskal" : "Prim";
            mazePanel.generateMaze(code);
            log(">> Maze Generated using " + code);
        });

        btnBFS.addActionListener(e -> mazePanel.solveBFS());
        btnDFS.addActionListener(e -> mazePanel.solveDFS());
        btnDijkstra.addActionListener(e -> mazePanel.solveDijkstra());

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // --- HELPER UI METHODS ---

    private void log(String msg) {
        logArea.append(msg + "\n");
    }

    // Membuat Judul Section (Garis bawah tipis)
    private void addHeader(JPanel panel, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 100, 100)); // Abu-abu pudar
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(TEXT_COLOR);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    // Membuat Tombol Kustom (Flat Design)
    private JButton createStyledButton(String text, Color baseColor, boolean isBold) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Ubah warna jika ditekan
                if (getModel().isPressed()) {
                    g2.setColor(baseColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(baseColor.brighter());
                } else {
                    g2.setColor(baseColor);
                }
                
                // Gambar tombol rounded
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                
                super.paintComponent(g);
            }
        };
        
        btn.setForeground(Color.BLACK); // Teks tombol hitam agar kontras dengan warna pastel
        if (baseColor == BTN_SOLVE_COLOR) btn.setForeground(Color.WHITE); // Teks putih untuk tombol biru

        btn.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); // Penting agar paintComponent custom bekerja
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(280, 40)); // Lebar maksimal
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleComboBox(JComboBox box) {
        box.setMaximumSize(new Dimension(280, 30));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        box.setBackground(Color.WHITE);
        ((JLabel)box.getRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }

    // Method Legend yang sudah kita perbaiki sebelumnya (Updated untuk Dark Mode)
    private void addLegend(JPanel panel) {
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // Grid Layout untuk legend agar rapi (2 kolom)
        JPanel legendGrid = new JPanel(new GridLayout(2, 2, 5, 5));
        legendGrid.setBackground(PANEL_BG);
        legendGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        legendGrid.setMaximumSize(new Dimension(280, 70));

        for (Terrain t : Terrain.values()) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            p.setBackground(PANEL_BG);

            // Kotak Icon Custom (Kode sama dengan sebelumnya)
            JPanel colorBox = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g); 
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int size = getWidth();
                    
                    // Background & Pattern
                    Color baseColor = t.color;
                    GradientPaint gp = new GradientPaint(0, 0, baseColor.brighter(), size, size, baseColor.darker());
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, size, size);

                    // Pola Sederhana (Miniatur dari Cell.java)
                    if (t == Terrain.GRASS) {
                        g2.setColor(new Color(60, 120, 0, 150)); g2.setStroke(new BasicStroke(1.5f));
                        g2.drawLine(5, size-5, 5, size-12); g2.drawLine(15, size-5, 15, size-10);
                    } else if (t == Terrain.WATER) {
                        g2.setColor(new Color(255, 255, 255, 80)); g2.setStroke(new BasicStroke(1.5f));
                        g2.drawLine(5, 10, size-5, 10);
                    } else if (t == Terrain.MUD) {
                        g2.setColor(new Color(60, 40, 10, 100)); g2.fillOval(5, 5, size/2, size/2-5);
                    } else if (t == Terrain.DIRT) {
                        g2.setColor(new Color(150, 120, 80, 180)); g2.fillOval(5, 5, 4, 4);
                    }
                    
                    // Border Putih Tipis (karena background panel gelap)
                    g2.setColor(new Color(200, 200, 200, 100));
                    g2.setStroke(new BasicStroke(1));
                    g2.drawRect(0, 0, size - 1, size - 1);
                }
            };
            colorBox.setPreferredSize(new Dimension(24, 24)); // Icon size

            JLabel nameLbl = new JLabel(t.name());
            nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            nameLbl.setForeground(TEXT_COLOR); // Teks putih

            p.add(colorBox);
            p.add(nameLbl);
            legendGrid.add(p);
        }
        panel.add(legendGrid);
    }
}