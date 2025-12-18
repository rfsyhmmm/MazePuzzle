import java.awt.*;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

public class MazeGame extends JFrame {

    private MazePanel mazePanel;
    private JTextArea logArea;
    private JComboBox<String> algoSelector;

    // --- PALET WARNA DUNGEON THEME ---
    private final Color HEADER_TEXT = new Color(255, 215, 120);
    private final Color TEXT_COLOR = new Color(240, 230, 210);
    private final Color LOG_BG = new Color(20, 15, 10);
    private final Color LOG_TEXT = new Color(160, 210, 160);

    // Font Utama
    private Font titleFont;

    public MazeGame() {
        setTitle("Dungeon Maze Solver - RPG Style");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Setup Font
        titleFont = new Font("Trajan Pro", Font.BOLD, 18);
        if (!titleFont.getFamily().equalsIgnoreCase("Trajan Pro")) {
            titleFont = new Font("Serif", Font.BOLD, 18);
        }

        // --- 1. GRID AREA (CENTER) ---
        mazePanel = new MazePanel(35, 25);

        JPanel mazeContainer = new JPanel(new GridBagLayout());
        mazeContainer.setBackground(new Color(35, 30, 25));

        mazePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(15, 10, 5), 4),
                BorderFactory.createLineBorder(new Color(100, 90, 80), 1)
        ));

        mazeContainer.add(mazePanel);
        add(mazeContainer, BorderLayout.CENTER);

        // --- 2. CONTROL PANEL (SIDEBAR KANAN) ---
        JPanel contentPanel = new WoodPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(20, 0, 20, 20));

        // --- MENGISI KONTEN SIDEBAR ---

        // SECTION 1: WORLD GENERATION
        addHeader(contentPanel, "DUNGEON");
        contentPanel.add(createLabel("Maze Creation Magic:"));

        String[] algorithms = {"Prim's Magic", "Kruskal's Magic"};
        algoSelector = new JComboBox<>(algorithms);
        styleComboBox(algoSelector);
        contentPanel.add(algoSelector);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton btnGen = createRPGButton("Generate Dungeon", new Color(180, 130, 0));
        contentPanel.add(btnGen);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // SECTION 2: PATHFINDING MAGIC
        addHeader(contentPanel, "SPELL BOOK");

        JButton btnBFS = createRPGButton("BFS", new Color(60, 80, 100));
        JButton btnDFS = createRPGButton("DFS", new Color(60, 80, 100));
        JButton btnDijkstra = createRPGButton("Dijkstra", new Color(60, 80, 100));
        JButton btnAStar = createRPGButton("A* Star", new Color(100, 60, 140)); // Tombol Baru

        contentPanel.add(btnBFS);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(btnDFS);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(btnDijkstra);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(btnAStar); // Tambahkan ke panel

        // Jarak setelah tombol terakhir
        contentPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        // SECTION 3: TERRAIN MAP
        addHeader(contentPanel, "TERRAIN MAP");
        addLegend(contentPanel);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // --- SECTION 4: ADVENTURE LOG ---
        addHeader(contentPanel, "ADVENTURE LOG");

        logArea = new JTextArea(12, 20);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.BOLD, 11));
        logArea.setBackground(new Color(25, 20, 15));
        logArea.setForeground(new Color(200, 180, 120));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(null);
        logScrollPane.getViewport().setBackground(new Color(25, 20, 15));
        logScrollPane.getVerticalScrollBar().setBackground(new Color(40, 30, 20));
        logScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));

        JPanel logWrapper = new JPanel(new BorderLayout());
        logWrapper.setOpaque(false);
        logWrapper.setBorder(new EmptyBorder(0, 10, 0, 10));

        JPanel framePanel = new JPanel(new BorderLayout());
        framePanel.setBackground(new Color(25, 20, 15));

        framePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 3, 3, 3, new Color(60, 45, 30)),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(100, 80, 50), 1),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)
                )
        ));

        framePanel.add(logScrollPane, BorderLayout.CENTER);
        logWrapper.add(framePanel, BorderLayout.CENTER);
        contentPanel.add(logWrapper);

        JScrollPane sidebarScroll = new JScrollPane(contentPanel);
        sidebarScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sidebarScroll.getVerticalScrollBar().setUnitIncrement(16);
        sidebarScroll.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, new Color(50, 40, 30)));
        sidebarScroll.setPreferredSize(new Dimension(340, 0));

        mazePanel.setLogArea(logArea);
        add(sidebarScroll, BorderLayout.EAST);

        // --- ACTION LISTENERS ---
        btnGen.addActionListener(e -> {
            logArea.setText("");
            String selectedAlgo = (String) algoSelector.getSelectedItem();
            String code = selectedAlgo.contains("Kruskal") ? "Kruskal's" : "Prim's";
            mazePanel.generateMaze(code);
            log(">> Dungeon Created using " + code + " Magic.");
        });

        btnBFS.addActionListener(e -> mazePanel.solveBFS());
        btnDFS.addActionListener(e -> mazePanel.solveDFS());
        btnDijkstra.addActionListener(e -> mazePanel.solveDijkstra());
        btnAStar.addActionListener(e -> mazePanel.solveAStar()); // Panggil A*

        // --- WINDOW SETTINGS ---
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // --- HELPER UI METHODS ---

    private void log(String msg) {
        logArea.append(msg + "\n");
    }

    private void addHeader(JPanel panel, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(titleFont);
        lbl.setForeground(HEADER_TEXT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,150));
                g2.drawString(text, 2, lbl.getHeight()-7);
                super.paint(g, c);
            }
        });
        panel.add(lbl);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_COLOR);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton createRPGButton(String text, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                Color cTop, cBot;
                if (getModel().isPressed()) {
                    cTop = baseColor.darker().darker();
                    cBot = baseColor;
                } else if (getModel().isRollover()) {
                    cTop = baseColor.brighter();
                    cBot = baseColor.darker();
                } else {
                    cTop = baseColor;
                    cBot = baseColor.darker().darker();
                }

                GradientPaint gp = new GradientPaint(0, 0, cTop, 0, h, cBot);
                g2.setPaint(gp);
                g2.fillRoundRect(2, 2, w-4, h-4, 10, 10);

                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(30, 20, 10));
                g2.drawRoundRect(2, 2, w-5, h-5, 10, 10);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Trajan Pro", Font.BOLD, 13));
        if (!titleFont.getFamily().equalsIgnoreCase("Trajan Pro")) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        }

        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        Dimension size = new Dimension(280, 35);
        btn.setPreferredSize(size);
        btn.setMaximumSize(size);
        btn.setMinimumSize(size);

        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                FontMetrics fm = g.getFontMetrics();
                int textX = (b.getWidth() - fm.stringWidth(b.getText())) / 2;
                int textY = (b.getHeight() + fm.getAscent() - 4) / 2;

                g2.drawString(b.getText(), textX + 1, textY + 1);
                g2.setColor(b.getForeground());
                g2.drawString(b.getText(), textX, textY);
            }
        });

        return btn;
    }

    private void styleComboBox(JComboBox<String> box) {
        Dimension size = new Dimension(280, 35);
        box.setPreferredSize(size);
        box.setMaximumSize(size);
        box.setMinimumSize(size);

        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setFont(new Font("Segoe UI", Font.BOLD, 13));

        box.setBackground(new Color(210, 190, 150));
        box.setForeground(new Color(60, 40, 20));

        box.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (isSelected) {
                    setBackground(new Color(100, 80, 60));
                    setForeground(new Color(255, 215, 120));
                } else {
                    setBackground(new Color(50, 40, 30));
                    setForeground(new Color(200, 200, 200));
                }
                setBorder(new EmptyBorder(5, 5, 5, 5));
                return this;
            }
        });
    }

    private void addLegend(JPanel panel) {
        JPanel legendGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        legendGrid.setOpaque(false);
        legendGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        legendGrid.setMaximumSize(new Dimension(280, 75));

        for (Terrain t : Terrain.values()) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            p.setOpaque(false);

            JPanel colorBox = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int size = getWidth();

                    g2.setColor(t.color);
                    g2.fillRect(0, 0, size, size);

                    if (t == Terrain.GRASS) {
                        g2.setColor(new Color(20, 50, 10, 150)); g2.setStroke(new BasicStroke(1.5f));
                        g2.drawLine(5, size-5, 5, size-12); g2.drawLine(15, size-5, 15, size-10);
                    } else if (t == Terrain.WATER) {
                        g2.setColor(new Color(100, 150, 200, 80)); g2.setStroke(new BasicStroke(1.5f));
                        g2.drawOval(5, 10, 8, 4);
                    } else if (t == Terrain.MUD) {
                        g2.setColor(new Color(30, 20, 10, 100)); g2.fillOval(5, 5, size/2, size/2-5);
                    } else if (t == Terrain.DIRT) {
                        g2.setColor(new Color(60, 50, 40, 120)); g2.fillRect(5, 5, 4, 4);
                    }

                    g2.setColor(new Color(0, 0, 0, 100));
                    g2.drawRect(0, 0, size - 1, size - 1);
                }
            };
            colorBox.setPreferredSize(new Dimension(28, 28));

            // Mengambil nama dan menambahkan nilai cost dalam kurung
            JLabel nameLbl = new JLabel(t.name() + "(" + t.cost + ")");
            nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            nameLbl.setForeground(TEXT_COLOR);

            p.add(colorBox);
            p.add(nameLbl);
            legendGrid.add(p);
        }
        panel.add(legendGrid);
    }

    class WoodPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth();
            int h = getHeight();

            Color woodBase = new Color(110, 90, 60);
            g2.setColor(woodBase);
            g2.fillRect(0, 0, w, h);

            Random rand = new Random(999);

            for (int i = 0; i < h; i += 2) {
                int alpha = rand.nextInt(40);
                if (rand.nextBoolean()) {
                    g2.setColor(new Color(50, 40, 20, alpha));
                } else {
                    g2.setColor(new Color(160, 130, 90, alpha));
                }

                int len = rand.nextInt(w);
                int x = rand.nextInt(w - len);
                g2.drawLine(x, i, x + len, i);
            }

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(0,0,0,100),
                    w/5, 0, new Color(0,0,0,0)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, w/5, h);
        }
    }
}