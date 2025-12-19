import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class MazePanel extends JPanel {
    private final int COLS, ROWS;
    private final int CELL_SIZE = 30;
    private Cell[][] grid;

    private Cell startCell;
    private Cell endCell;

    // Flag untuk mencegah tabrakan proses (Generating vs Solving)
    private volatile boolean isWorking = false;
    private JTextArea logArea;

    private List<Cell> currentPath = new ArrayList<>();
    private Cell currentProcessing = null; // Digunakan untuk highlight animasi

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

    // --- GENERATOR (ANIMATED) ---

    public void generateMaze(String algorithm) {
        if (isWorking) return; // Cegah double click saat proses berjalan

        // Reset state visual sebelum thread dimulai
        initGrid();
        currentPath.clear();
        currentProcessing = null;
        startCell = null;
        endCell = null;
        repaint();

        // Jalankan generator di Thread terpisah agar bisa di-animasi (sleep)
        new Thread(() -> {
            isWorking = true;
            if (algorithm.contains("Kruskal")) { // Handle string "Kruskal's" dari UI
                runKruskalAnimation();
            } else {
                runPrimAnimation();
            }

            // Setelah struktur maze jadi, generate terrain dan titik start/end
            generateTerrainOnly();
            setupStartAndExit();

            currentProcessing = null; // Hapus highlight proses
            resetVisited(); // Pastikan bersih untuk solver
            isWorking = false;
            SwingUtilities.invokeLater(this::repaint);
            log(">> Dungeon Ready for Adventure!");
        }).start();
    }

    /**
     * Best Practice Randomized Prim's:
     * 1. Mulai dari satu sel acak.
     * 2. Masukkan dinding tetangganya ke dalam daftar (frontier).
     * 3. Pilih dinding acak dari frontier.
     * 4. Jika sel di seberang dinding belum visited, hancurkan dinding dan jadikan sel itu bagian maze.
     */
    private void runPrimAnimation() {
        log("> Casting Prim's Algorithm...");

        // List dinding (Wall) sebagai Frontier
        List<Wall> walls = new ArrayList<>();
        Random rand = new Random();

        // Mulai dari pojok kiri atas (atau acak)
        Cell start = grid[0][0];
        start.visited = true; // Visited di sini artinya "Masuk ke dalam Maze"
        addWalls(start, walls);

        while (!walls.isEmpty()) {
            // Ambil dinding acak dari frontier
            int index = rand.nextInt(walls.size());
            Wall wall = walls.remove(index);

            Cell current = wall.cell1;
            Cell next = wall.cell2;

            if (!next.visited) {
                // Hancurkan dinding
                removeWall(current, next, wall.direction);
                next.visited = true;

                // Tambahkan dinding tetangga baru ke frontier
                addWalls(next, walls);

                // Visualisasi Animasi
                currentProcessing = next;
                SwingUtilities.invokeLater(this::repaint);
                sleep(10); // Kecepatan animasi (makin kecil makin cepat)
            }
        }
    }

    /**
     * Best Practice Randomized Kruskal's:
     * 1. Anggap setiap sel adalah set terpisah.
     * 2. Kumpulkan semua kemungkinan dinding.
     * 3. Acak urutan dinding.
     * 4. Jika dua sel yang dipisahkan dinding berada di set berbeda, hancurkan dinding & gabungkan set.
     */
    private void runKruskalAnimation() {
        log("> Casting Kruskal's Algorithm...");

        List<Wall> allWalls = new ArrayList<>();
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (x < COLS - 1) allWalls.add(new Wall(grid[y][x], grid[y][x+1], "right"));
                if (y < ROWS - 1) allWalls.add(new Wall(grid[y][x], grid[y+1][x], "bottom"));
            }
        }

        // Acak dinding untuk sifat random maze
        Collections.shuffle(allWalls);

        DisjointSet ds = new DisjointSet(COLS * ROWS);

        int count = 0;
        for (Wall w : allWalls) {
            int id1 = w.cell1.y * COLS + w.cell1.x;
            int id2 = w.cell2.y * COLS + w.cell2.x;

            if (ds.find(id1) != ds.find(id2)) {
                removeWall(w.cell1, w.cell2, w.direction);
                ds.union(id1, id2);

                // Visualisasi Animasi (hanya update tiap beberapa langkah agar tidak terlalu lambat)
                currentProcessing = w.cell2;
                if (count++ % 2 == 0) {
                    SwingUtilities.invokeLater(this::repaint);
                    sleep(5);
                }
            }
        }
        SwingUtilities.invokeLater(this::repaint);
    }

    // --- SOLVERS ---

    public void solveBFS() {
        if (isWorking || startCell == null) return;
        prepareSolver();
        new Thread(this::runBFS).start();
    }

    public void solveDFS() {
        if (isWorking || startCell == null) return;
        prepareSolver();
        new Thread(this::runDFS).start();
    }

    public void solveDijkstra() {
        if (isWorking || startCell == null) return;
        prepareSolver();
        new Thread(this::runDijkstra).start();
    }

    public void solveAStar() {
        if (isWorking || startCell == null) return;
        prepareSolver();
        new Thread(this::runAStar).start();
    }

    private void prepareSolver() {
        currentPath.clear();
        currentProcessing = null;
        repaint();
        if (logArea != null) logArea.setText("");
    }

    // --- RUNNING ALGORITHMS (SOLVER) ---
    // (Kode solver tetap sama, hanya menambahkan pengecekan isWorking = true/false)

    private void runBFS() {
        isWorking = true;
        resetVisited();
        Queue<Cell> queue = new LinkedList<>();
        Map<Cell, Cell> parentMap = new HashMap<>();

        queue.add(startCell);
        startCell.visited = true;

        log("> Breadth-First Search (BFS) started.");

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            currentProcessing = current;

            SwingUtilities.invokeLater(this::repaint);
            sleep(15);

            if (current == endCell) {
                log("> Exit found!");
                reconstructPath(parentMap, current);
                isWorking = false;
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
        isWorking = false;
        log("> No path found.");
        SwingUtilities.invokeLater(this::repaint);
    }

    private void runDFS() {
        isWorking = true;
        resetVisited();
        Stack<Cell> stack = new Stack<>();
        Map<Cell, Cell> parentMap = new HashMap<>();

        stack.push(startCell);
        startCell.visited = true;

        log("> Depth-First Search (DFS) started.");

        while (!stack.isEmpty()) {
            Cell current = stack.pop();
            currentProcessing = current;

            SwingUtilities.invokeLater(this::repaint);
            sleep(15);

            if (current == endCell) {
                log("> Exit found!");
                reconstructPath(parentMap, current);
                isWorking = false;
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
        isWorking = false;
        log("> No path found.");
        SwingUtilities.invokeLater(this::repaint);
    }

    private void runDijkstra() {
        isWorking = true;
        resetVisited();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.gCost));
        Map<Cell, Integer> dist = new HashMap<>();
        Map<Cell, Cell> parentMap = new HashMap<>();

        for(int y=0; y<ROWS; y++) for(int x=0; x<COLS; x++) dist.put(grid[y][x], Integer.MAX_VALUE);
        dist.put(startCell, 0);
        pq.add(new Node(startCell, 0, 0, null));

        log("> Dijkstra started.");

        while (!pq.isEmpty()) {
            Node node = pq.poll();
            Cell current = node.cell;

            if (node.gCost > dist.get(current)) continue;

            currentProcessing = current;
            current.visited = true;
            SwingUtilities.invokeLater(this::repaint);
            sleep(15);

            if (current == endCell) {
                log("> Optimal path found (Cost: " + node.gCost + ")");
                reconstructPath(parentMap, current);
                isWorking = false;
                return;
            }

            for (Cell neighbor : getAccessibleNeighbors(current)) {
                int newDist = dist.get(current) + neighbor.terrain.cost;
                if (newDist < dist.get(neighbor)) {
                    dist.put(neighbor, newDist);
                    parentMap.put(neighbor, current);
                    pq.add(new Node(neighbor, newDist, 0, null));
                }
            }
        }
        isWorking = false;
        log("> No path found.");
        SwingUtilities.invokeLater(this::repaint);
    }

    private void runAStar() {
        isWorking = true;
        resetVisited();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.fCost));
        Map<Cell, Integer> gScore = new HashMap<>();
        Map<Cell, Cell> parentMap = new HashMap<>();

        for(int y=0; y<ROWS; y++) for(int x=0; x<COLS; x++) gScore.put(grid[y][x], Integer.MAX_VALUE);
        gScore.put(startCell, 0);
        int hStart = getHeuristic(startCell, endCell);
        pq.add(new Node(startCell, 0, hStart, null));

        log("> A* Search started.");

        while (!pq.isEmpty()) {
            Node node = pq.poll();
            Cell current = node.cell;

            if (node.gCost > gScore.get(current)) continue;

            currentProcessing = current;
            current.visited = true;
            SwingUtilities.invokeLater(this::repaint);
            sleep(15);

            if (current == endCell) {
                log("> Path found!");
                reconstructPath(parentMap, current);
                isWorking = false;
                return;
            }

            for (Cell neighbor : getAccessibleNeighbors(current)) {
                int tentativeG = gScore.get(current) + neighbor.terrain.cost;
                if (tentativeG < gScore.get(neighbor)) {
                    gScore.put(neighbor, tentativeG);
                    parentMap.put(neighbor, current);
                    int hCost = getHeuristic(neighbor, endCell);
                    pq.add(new Node(neighbor, tentativeG, hCost, null));
                }
            }
        }
        isWorking = false;
        log("> No path found.");
        SwingUtilities.invokeLater(this::repaint);
    }

    // --- UTILITIES ---

    private int getHeuristic(Cell a, Cell b) {
        if (b == null) return 0;
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

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

        log("[Finished] Distance: " + path.size());
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

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

        // Highlight sel yang sedang diproses (untuk animasi)
        if (currentProcessing != null) {
            g.setColor(new Color(255, 200, 0, 180)); // Warna emas terang
            g.fillRect(currentProcessing.x * CELL_SIZE + 5, currentProcessing.y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
            g.setColor(Color.WHITE);
            g.drawRect(currentProcessing.x * CELL_SIZE + 5, currentProcessing.y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
        }

        if (currentPath.size() > 1) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int half = CELL_SIZE / 2;

            g2.setColor(new Color(255, 255, 220, 160));
            float baseStrokeWidth = CELL_SIZE * 0.85f;
            g2.setStroke(new BasicStroke(baseStrokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            for (int i = 0; i < currentPath.size() - 1; i++) {
                Cell c1 = currentPath.get(i);
                Cell c2 = currentPath.get(i+1);
                g2.drawLine(c1.x * CELL_SIZE + half, c1.y * CELL_SIZE + half,
                        c2.x * CELL_SIZE + half, c2.y * CELL_SIZE + half);
            }

            g2.setColor(new Color(255, 50, 50, 120));
            g2.setStroke(new BasicStroke(CELL_SIZE * 0.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            for (int i = 0; i < currentPath.size() - 1; i++) {
                Cell c1 = currentPath.get(i);
                Cell c2 = currentPath.get(i+1);
                g2.drawLine(c1.x * CELL_SIZE + half, c1.y * CELL_SIZE + half,
                        c2.x * CELL_SIZE + half, c2.y * CELL_SIZE + half);
            }
        }

        if (startCell != null) {
            g.setColor(Color.BLUE);
            g.fillOval(startCell.x * CELL_SIZE + 8, startCell.y * CELL_SIZE + 8, CELL_SIZE - 16, CELL_SIZE - 16);
            g.setColor(Color.WHITE);
            ((Graphics2D)g).setStroke(new BasicStroke(2));
            g.drawOval(startCell.x * CELL_SIZE + 8, startCell.y * CELL_SIZE + 8, CELL_SIZE - 16, CELL_SIZE - 16);
        }
    }
}