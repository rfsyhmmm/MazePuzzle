import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MazePanel extends JPanel {
    private final int COLS, ROWS;
    private final int CELL_SIZE = 30;
    private Cell[][] grid;

    // Single Exit Way
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
            logArea.append(msg + "\n");
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

    // --- MAIN GENERATOR SWITCH ---
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

    // --- ALGORITMA 1: PRIM'S ---
    private void generatePrim() {
        List<Wall> walls = new ArrayList<>();
        Random rand = new Random();

        Cell start = grid[0][0];
        start.visited = true;
        addWalls(start, walls);

        while (!walls.isEmpty()) {
            Wall wall = walls.remove(rand.nextInt(walls.size()));
            Cell current = wall.cell1;
            Cell next = wall.cell2;

            if (!next.visited) {
                next.visited = true;
                removeWall(current, next, wall.direction);
                addWalls(next, walls);
            }
        }
    }

    // --- ALGORITMA 2: KRUSKAL'S ---
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

    // --- COMMON GENERATION STEPS ---

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
            int rx = rand.nextInt(COLS);
            int ry = rand.nextInt(ROWS);
            Cell c = grid[ry][rx];
            List<Cell> neighbors = getNeighbors(c);
            if (!neighbors.isEmpty()) {
                Cell n = neighbors.get(rand.nextInt(neighbors.size()));
                String dir = getDirection(c, n);
                removeWall(c, n, dir);
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

    // --- SOLVERS ---

    public void solveBFS() {
        if (isSolving || startCell == null) return;
        if (logArea != null) logArea.setText("");
        new Thread(this::runBFS).start();
    }
    public void solveDFS() {
        if (isSolving || startCell == null) return;
        if (logArea != null) logArea.setText("");
        new Thread(this::runDFS).start();
    }
    public void solveDijkstra() {
        if (isSolving || startCell == null) return;
        if (logArea != null) logArea.setText("");
        new Thread(this::runDijkstra).start();
    }

    private void runBFS() {
        isSolving = true;
        resetVisited();
        Queue<Cell> queue = new LinkedList<>();
        Map<Cell, Cell> parentMap = new HashMap<>();

        queue.add(startCell);
        startCell.visited = true;

        log("=== BFS STARTED ===");
        log("Target: EXIT Point.");
        log("Strategi: Steps terpendek (Mengabaikan Weight).");

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            currentProcessing = current;
            repaint();
            sleep(40);

            if (current == endCell) {
                log("\nSOLUSI DITEMUKAN di EXIT!");
                reconstructPath(parentMap, current);
                isSolving = false;
                return;
            }

            for (Cell neighbor : getAccessibleNeighbors(current)) {
                if (!neighbor.visited) {
                    neighbor.visited = true;
                    parentMap.put(neighbor, current);
                    queue.add(neighbor);
                    log("  + Scan [" + neighbor.x + "," + neighbor.y + "] (" + neighbor.terrain + ")");
                }
            }
        }
        isSolving = false;
        log("Tidak ada jalan ke EXIT.");
    }

    private void runDFS() {
        isSolving = true;
        resetVisited();
        Stack<Cell> stack = new Stack<>();
        Map<Cell, Cell> parentMap = new HashMap<>();

        stack.push(startCell);
        startCell.visited = true;

        log("=== DFS STARTED ===");
        log("Target: EXIT Point.");

        while (!stack.isEmpty()) {
            Cell current = stack.pop();
            currentProcessing = current;
            repaint();
            sleep(40);

            if (current == endCell) {
                log("\nSOLUSI DITEMUKAN di EXIT!");
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
                    log("  + Push [" + neighbor.x + "," + neighbor.y + "]");
                }
            }
        }
        isSolving = false;
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

        log("=== DIJKSTRA STARTED ===");
        log("Target: EXIT Point.");
        log("Strategi: Cost Terendah (Menghindari Terrain Berat).");

        while (!pq.isEmpty()) {
            Node node = pq.poll();
            Cell current = node.cell;

            if (node.cost > dist.get(current)) continue;

            currentProcessing = current;
            current.visited = true;
            repaint();
            sleep(40);

            if (current == endCell) {
                log("\nSOLUSI TERBAIK DITEMUKAN di EXIT!");
                log("Total Cost: " + node.cost);
                reconstructPath(parentMap, current);
                isSolving = false;
                return;
            }

            for (Cell neighbor : getAccessibleNeighbors(current)) {
                int newDist = dist.get(current) + neighbor.terrain.cost;
                if (newDist < dist.get(neighbor)) {
                    log("  -> Update [" + neighbor.x + "," + neighbor.y + "] Cost: " + newDist);
                    dist.put(neighbor, newDist);
                    parentMap.put(neighbor, current);
                    pq.add(new Node(neighbor, newDist));
                }
            }
        }
        isSolving = false;
    }

    // --- UTILITIES ---

    // Fitur v0.4.1: Menghitung statistik Path (Dirt/Grass/Mud/Water)
    private void reconstructPath(Map<Cell, Cell> parentMap, Cell current) {
        currentPath.clear();
        while (current != null) {
            currentPath.add(current);
            current = parentMap.get(current);
        }
        Collections.reverse(currentPath);

        // --- HITUNG STATISTIK TERRAIN ---
        int dirtCount = 0;
        int grassCount = 0;
        int mudCount = 0;
        int waterCount = 0;

        for (Cell c : currentPath) {
            switch (c.terrain) {
                case DIRT: dirtCount++; break;
                case GRASS: grassCount++; break;
                case MUD: mudCount++; break;
                case WATER: waterCount++; break;
            }
        }

        log("\n=== STATISTIK JALUR (PATH RESULT) ===");
        log("Panjang Langkah: " + currentPath.size());
        log("Rincian Terrain yang dilewati:");
        log(" - Dirt  (Normal): " + dirtCount);
        log(" - Grass (Agak Berat): " + grassCount);
        log(" - Mud   (Berat): " + mudCount);
        log(" - Water (Sangat Berat): " + waterCount);
        log("=====================================");
        // ---------------------------------

        currentProcessing = null;
        repaint();
    }

    private List<Cell> getAccessibleNeighbors(Cell c) {
        List<Cell> list = new ArrayList<>();
        checkAndAdd(c, 0, 0, -1, list); // Top
        checkAndAdd(c, 1, 1, 0, list);  // Right
        checkAndAdd(c, 2, 0, 1, list);  // Bottom
        checkAndAdd(c, 3, -1, 0, list); // Left
        return list;
    }

    private void checkAndAdd(Cell c, int wallIdx, int dx, int dy, List<Cell> list) {
        if (!c.walls[wallIdx]) {
            int nx = c.x + dx;
            int ny = c.y + dy;
            if (isValid(nx, ny)) list.add(grid[ny][nx]);
        }
    }

    private void sleep(int millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) {}
    }

    private void addWalls(Cell c, List<Wall> walls) {
        int[][] dirs = {{0,-1,0}, {1,0,1}, {0,1,2}, {-1,0,3}};
        String[] dirNames = {"top", "right", "bottom", "left"};
        for (int i = 0; i < 4; i++) {
            int nx = c.x + dirs[i][0];
            int ny = c.y + dirs[i][1];
            if (isValid(nx, ny)) walls.add(new Wall(c, grid[ny][nx], dirNames[i]));
        }
    }

    private List<Cell> getNeighbors(Cell c) {
        List<Cell> list = new ArrayList<>();
        int[][] dirs = {{0,-1}, {1,0}, {0,1}, {-1,0}};
        for(int[] d : dirs) {
            int nx = c.x + d[0];
            int ny = c.y + d[1];
            if(isValid(nx, ny)) list.add(grid[ny][nx]);
        }
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                grid[y][x].draw(g, CELL_SIZE);
            }
        }
        if (currentProcessing != null) {
            g.setColor(Color.MAGENTA);
            g.fillRect(currentProcessing.x * CELL_SIZE + 5, currentProcessing.y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
        }
        if (currentPath.size() > 1) {
            g.setColor(Color.RED);
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(3));
            for (int i = 0; i < currentPath.size() - 1; i++) {
                Cell c1 = currentPath.get(i);
                Cell c2 = currentPath.get(i+1);
                int half = CELL_SIZE / 2;
                g2.drawLine(c1.x * CELL_SIZE + half, c1.y * CELL_SIZE + half,
                        c2.x * CELL_SIZE + half, c2.y * CELL_SIZE + half);
            }
        }
        if (startCell != null) {
            g.setColor(Color.BLUE);
            g.fillOval(startCell.x*CELL_SIZE+5, startCell.y*CELL_SIZE+5, 20, 20);
        }
    }
}