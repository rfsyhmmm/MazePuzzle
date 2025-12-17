import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class MazePanel extends JPanel {
    private final int COLS, ROWS;
    private final int CELL_SIZE = 30; // Ukuran sel tetap 30
    private Cell[][] grid;

    private Cell startCell;
    private Cell endCell;

    private boolean isSolving = false;
    private JTextArea logArea;

    private List<Cell> currentPath = new ArrayList<>();
    private Cell currentProcessing = null;

    public MazePanel(int cols, int rows) {
        this.COLS = cols;
        this.ROWS = rows;
        this.setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE));
        initGrid();
    }

    public void setLogArea(JTextArea logArea) {
        this.logArea = logArea;
    }

    private void log(String msg) {
        if (logArea != null) {
            SwingUtilities.invokeLater(() -> {
                logArea.append(msg + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            });
        }
    }

    private void initGrid() {
        grid = new Cell[ROWS][COLS];
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                grid[y][x] = new Cell(x, y);
            }
        }
    }

    // --- GENERATOR ---
    public void generateMaze(String algorithm) {
        if (isSolving) return;
        initGrid();

        if (algorithm.equals("Kruskal")) {
            generateKruskal();
        } else {
            generatePrim();
        }

        generateTerrainOnly();
        createMultipleWays(new Random());
        setupStartAndExit();

        resetVisited();
        currentPath.clear();
        currentProcessing = null;
        repaint();
    }

    // --- SOLVERS (DIPERBAIKI: CLEAR PATH DI AWAL) ---

    public void solveBFS() {
        if (isSolving || startCell == null) return;
        
        // 1. Bersihkan jalur lama & reset tampilan segera
        currentPath.clear();
        currentProcessing = null;
        repaint(); 
        
        if (logArea != null) logArea.setText("");
        new Thread(this::runBFS).start();
    }

    public void solveDFS() {
        if (isSolving || startCell == null) return;
        
        // 1. Bersihkan jalur lama & reset tampilan segera
        currentPath.clear();
        currentProcessing = null;
        repaint();
        
        if (logArea != null) logArea.setText("");
        new Thread(this::runDFS).start();
    }

    public void solveDijkstra() {
        if (isSolving || startCell == null) return;
        
        // 1. Bersihkan jalur lama & reset tampilan segera
        currentPath.clear();
        currentProcessing = null;
        repaint();
        
        if (logArea != null) logArea.setText("");
        new Thread(this::runDijkstra).start();
    }

    // --- RUNNING ALGORITHMS (UPDATED LOGS) ---

    private void runBFS() {
        isSolving = true;
        resetVisited();
        Queue<Cell> queue = new LinkedList<>();
        Map<Cell, Cell> parentMap = new HashMap<>();

        queue.add(startCell);
        startCell.visited = true;

        log("> The scouting party spreads out...");
        log("Objective: Locate the exit.");
        log("Tactic: Searching every corner equally.");

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            currentProcessing = current;
            
            SwingUtilities.invokeLater(this::repaint);
            sleep(30); 

            if (current == endCell) {
                log("\n> A path through the dark has been revealed.");
                reconstructPath(parentMap, current);
                isSolving = false;
                return;
            }

            for (Cell neighbor : getAccessibleNeighbors(current)) {
                if (!neighbor.visited) {
                    neighbor.visited = true;
                    parentMap.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }
        isSolving = false;
        log("> Hope fades. No passage found.");
        SwingUtilities.invokeLater(this::repaint);
    }

    private void runDFS() {
        isSolving = true;
        resetVisited();
        Stack<Cell> stack = new Stack<>();
        Map<Cell, Cell> parentMap = new HashMap<>();

        stack.push(startCell);
        startCell.visited = true;

        log("> Venturing deep into the unknown...");
        log("Objective: Locate the exit.");
        log("Tactic: Braving the depths blindly.");

        while (!stack.isEmpty()) {
            Cell current = stack.pop();
            currentProcessing = current;
            
            SwingUtilities.invokeLater(this::repaint);
            sleep(30);

            if (current == endCell) {
                log("\n> Destiny met. The exit lies before us.");
                reconstructPath(parentMap, current);
                isSolving = false;
                return;
            }

            List<Cell> neighbors = getAccessibleNeighbors(current);
            Collections.shuffle(neighbors);

            for (Cell neighbor : neighbors) {
                if (!neighbor.visited) {
                    neighbor.visited = true;
                    parentMap.put(neighbor, current);
                    stack.push(neighbor);
                }
            }
        }
        isSolving = false;
        log("> Dead end. The dungeon claims another soul.");
        SwingUtilities.invokeLater(this::repaint);
    }

    private void runDijkstra() {
        isSolving = true;
        resetVisited();

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));
        Map<Cell, Integer> dist = new HashMap<>();
        Map<Cell, Cell> parentMap = new HashMap<>();

        for(int y=0; y<ROWS; y++)
            for(int x=0; x<COLS; x++)
                dist.put(grid[y][x], Integer.MAX_VALUE);

        dist.put(startCell, 0);
        pq.add(new Node(startCell, 0));

        log("> calculating the safest route...");
        log("Objective: Locate the exit.");
        log("Tactic: Avoiding treacherous terrain.");

        while (!pq.isEmpty()) {
            Node node = pq.poll();
            Cell current = node.cell;

            if (node.cost > dist.get(current)) continue;

            currentProcessing = current;
            current.visited = true;
            
            SwingUtilities.invokeLater(this::repaint);
            sleep(30);

            if (current == endCell) {
                log("\n> Optimal path secured.");
                log("Fatigue Cost: " + node.cost);
                reconstructPath(parentMap, current);
                isSolving = false;
                return;
            }

            for (Cell neighbor : getAccessibleNeighbors(current)) {
                int newDist = dist.get(current) + neighbor.terrain.cost;
                if (newDist < dist.get(neighbor)) {
                    dist.put(neighbor, newDist);
                    parentMap.put(neighbor, current);
                    pq.add(new Node(neighbor, newDist));
                }
            }
        }
        isSolving = false;
        log("> No viable path discovered.");
        SwingUtilities.invokeLater(this::repaint);
    }

    // --- UTILITIES (UPDATED LOGS) ---

    private void reconstructPath(Map<Cell, Cell> parentMap, Cell current) {
        List<Cell> path = new ArrayList<>();
        while (current != null) {
            path.add(current);
            current = parentMap.get(current);
        }
        Collections.reverse(path);
        
        SwingUtilities.invokeLater(() -> {
            this.currentPath.clear();
            this.currentPath.addAll(path);
            this.currentProcessing = null;
            repaint();
        });

        // Hitung Statistik
        int dirtCount = 0, grassCount = 0, mudCount = 0, waterCount = 0;
        for (Cell c : path) {
            switch (c.terrain) {
                case DIRT: dirtCount++; break;
                case GRASS: grassCount++; break;
                case MUD: mudCount++; break;
                case WATER: waterCount++; break;
            }
        }

        log("\n[JOURNEY RECORD]");
        log("Distance Traveled: " + path.size() + " paces.");
        log("Terrain Crossed: Dirt(" + dirtCount + ") Grass(" + grassCount + ") Mud(" + mudCount + ") Water(" + waterCount + ")");
        log("-------------------------------------");
    }

    private void generatePrim() {
        List<Wall> walls = new ArrayList<>();
        Random rand = new Random();
        Cell start = grid[0][0];
        start.visited = true;
        addWalls(start, walls);

        while (!walls.isEmpty()) {
            Wall wall = walls.remove(rand.nextInt(walls.size()));
            Cell next = wall.cell2;
            if (!next.visited) {
                next.visited = true;
                removeWall(wall.cell1, next, wall.direction);
                addWalls(next, walls);
            }
        }
    }

    private void generateKruskal() {
        List<Wall> allWalls = new ArrayList<>();
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (x < COLS - 1) allWalls.add(new Wall(grid[y][x], grid[y][x+1], "right"));
                if (y < ROWS - 1) allWalls.add(new Wall(grid[y][x], grid[y+1][x], "bottom"));
            }
        }
        Collections.shuffle(allWalls);
        DisjointSet ds = new DisjointSet(COLS * ROWS);
        for (Wall w : allWalls) {
            int id1 = w.cell1.y * COLS + w.cell1.x;
            int id2 = w.cell2.y * COLS + w.cell2.x;
            if (ds.find(id1) != ds.find(id2)) {
                removeWall(w.cell1, w.cell2, w.direction);
                ds.union(id1, id2);
            }
        }
    }

    class DisjointSet {
        int[] parent;
        public DisjointSet(int n) {
            parent = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;
        }
        int find(int i) {
            if (parent[i] == i) return i;
            return parent[i] = find(parent[i]);
        }
        void union(int i, int j) {
            int rootA = find(i);
            int rootB = find(j);
            if (rootA != rootB) parent[rootA] = rootB;
        }
    }

    private void generateTerrainOnly() {
        Random r = new Random();
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                double chance = r.nextDouble();
                if (chance < 0.50) grid[y][x].setTerrain(Terrain.DIRT);
                else if (chance < 0.75) grid[y][x].setTerrain(Terrain.GRASS);
                else if (chance < 0.90) grid[y][x].setTerrain(Terrain.MUD);
                else grid[y][x].setTerrain(Terrain.WATER);
            }
        }
    }

    private void createMultipleWays(Random rand) {
        int extraPaths = (ROWS * COLS) / 10;
        for (int i = 0; i < extraPaths; i++) {
            Cell c = grid[rand.nextInt(ROWS)][rand.nextInt(COLS)];
            List<Cell> neighbors = getNeighbors(c);
            if (!neighbors.isEmpty()) {
                Cell n = neighbors.get(rand.nextInt(neighbors.size()));
                removeWall(c, n, getDirection(c, n));
            }
        }
    }

    private void setupStartAndExit() {
        Random rand = new Random();
        startCell = grid[0][0];
        startCell.label = "Start";
        int ex = rand.nextInt(COLS / 3) + (COLS * 2 / 3);
        int ey = rand.nextInt(ROWS);
        endCell = grid[ey][ex];
        endCell.label = "EXIT";
        endCell.setTerrain(Terrain.DIRT);
    }

    private List<Cell> getAccessibleNeighbors(Cell c) {
        List<Cell> list = new ArrayList<>();
        if (!c.walls[0] && isValid(c.x, c.y - 1)) list.add(grid[c.y - 1][c.x]);
        if (!c.walls[1] && isValid(c.x + 1, c.y)) list.add(grid[c.y][c.x + 1]);
        if (!c.walls[2] && isValid(c.x, c.y + 1)) list.add(grid[c.y + 1][c.x]);
        if (!c.walls[3] && isValid(c.x - 1, c.y)) list.add(grid[c.y][c.x - 1]);
        return list;
    }

    private void addWalls(Cell c, List<Wall> walls) {
        if (isValid(c.x, c.y-1)) walls.add(new Wall(c, grid[c.y-1][c.x], "top"));
        if (isValid(c.x+1, c.y)) walls.add(new Wall(c, grid[c.y][c.x+1], "right"));
        if (isValid(c.x, c.y+1)) walls.add(new Wall(c, grid[c.y+1][c.x], "bottom"));
        if (isValid(c.x-1, c.y)) walls.add(new Wall(c, grid[c.y][c.x-1], "left"));
    }

    private List<Cell> getNeighbors(Cell c) {
        List<Cell> list = new ArrayList<>();
        if (isValid(c.x, c.y-1)) list.add(grid[c.y-1][c.x]);
        if (isValid(c.x+1, c.y)) list.add(grid[c.y][c.x+1]);
        if (isValid(c.x, c.y+1)) list.add(grid[c.y+1][c.x]);
        if (isValid(c.x-1, c.y)) list.add(grid[c.y][c.x-1]);
        return list;
    }

    private String getDirection(Cell from, Cell to) {
        if (to.x > from.x) return "right";
        if (to.x < from.x) return "left";
        if (to.y > from.y) return "bottom";
        return "top";
    }

    private void removeWall(Cell c, Cell n, String dir) {
        switch (dir) {
            case "top": c.walls[0] = false; n.walls[2] = false; break;
            case "right": c.walls[1] = false; n.walls[3] = false; break;
            case "bottom": c.walls[2] = false; n.walls[0] = false; break;
            case "left": c.walls[3] = false; n.walls[1] = false; break;
        }
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < COLS && y >= 0 && y < ROWS;
    }

    private void resetVisited() {
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                grid[y][x].visited = false;
            }
        }
    }

    private void sleep(int millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) {}
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 1. Gambar Grid (Terrain & Dinding) - 2 Pass Rendering
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                grid[y][x].drawTerrain(g, CELL_SIZE);
            }
        }
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                grid[y][x].drawWalls(g, CELL_SIZE);
            }
        }

        // 2. Gambar Blok Proses (Animasi saat mencari jalan)
        if (currentProcessing != null) {
            g.setColor(new Color(255, 0, 255, 150)); 
            g.fillRect(currentProcessing.x * CELL_SIZE + 5, currentProcessing.y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
            g.setColor(Color.WHITE);
            g.drawRect(currentProcessing.x * CELL_SIZE + 5, currentProcessing.y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
        }

        // 3. GAMBAR JALUR HASIL (PATH) - ORGANIC STYLE
        if (currentPath.size() > 1) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int half = CELL_SIZE / 2;

            // A. LAYER DASAR: GLOW TERANG HALUS
            g2.setColor(new Color(255, 255, 220, 160)); 
            float baseStrokeWidth = CELL_SIZE * 0.85f; // Lebar proporsional (sekitar 25px)
            g2.setStroke(new BasicStroke(baseStrokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            for (int i = 0; i < currentPath.size() - 1; i++) {
                Cell c1 = currentPath.get(i);
                Cell c2 = currentPath.get(i+1);
                g2.drawLine(c1.x * CELL_SIZE + half, c1.y * CELL_SIZE + half,
                        c2.x * CELL_SIZE + half, c2.y * CELL_SIZE + half);
            }

            // B. LAYER TENGAH: NEON MERAH
            g2.setColor(new Color(255, 50, 50, 120)); 
            g2.setStroke(new BasicStroke(CELL_SIZE * 0.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            for (int i = 0; i < currentPath.size() - 1; i++) {
                Cell c1 = currentPath.get(i);
                Cell c2 = currentPath.get(i+1);
                g2.drawLine(c1.x * CELL_SIZE + half, c1.y * CELL_SIZE + half,
                        c2.x * CELL_SIZE + half, c2.y * CELL_SIZE + half);
            }

            // C. LAYER ATAS: GARIS INTI
            g2.setColor(Color.RED); 
            g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            for (int i = 0; i < currentPath.size() - 1; i++) {
                Cell c1 = currentPath.get(i);
                Cell c2 = currentPath.get(i+1);
                g2.drawLine(c1.x * CELL_SIZE + half, c1.y * CELL_SIZE + half,
                        c2.x * CELL_SIZE + half, c2.y * CELL_SIZE + half);
                
                // Breadcrumbs (Titik Kecil)
                g2.fillOval(c1.x * CELL_SIZE + half - 3, c1.y * CELL_SIZE + half - 3, 6, 6);
            }
        }

        // 4. Marker Start (Titik Biru)
        if (startCell != null) {
            g.setColor(Color.BLUE);
            g.fillOval(startCell.x * CELL_SIZE + 8, startCell.y * CELL_SIZE + 8, CELL_SIZE - 16, CELL_SIZE - 16);
            g.setColor(Color.WHITE); 
            ((Graphics2D)g).setStroke(new BasicStroke(2));
            g.drawOval(startCell.x * CELL_SIZE + 8, startCell.y * CELL_SIZE + 8, CELL_SIZE - 16, CELL_SIZE - 16);
        }
    }
}