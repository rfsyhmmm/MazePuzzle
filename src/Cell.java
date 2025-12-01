import java.awt.*;

public class Cell {
    public int x, y;
    // Walls: Top, Right, Bottom, Left
    public boolean[] walls = {true, true, true, true};
    public boolean visited = false;
    public Terrain terrain = Terrain.DIRT;

    // Fitur Label (A, B, C)
    public String label = null;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setTerrain(Terrain t) {
        this.terrain = t;
    }

    public void draw(Graphics g, int size) {
        int px = x * size;
        int py = y * size;

        // 1. Gambar Background Terrain (Lantai)
        g.setColor(terrain.color);
        g.fillRect(px, py, size, size);

        // 2. Overlay Visited (Scanning Animation)
        if (visited) {
            g.setColor(new Color(255, 255, 255, 100)); // Putih transparan
            g.fillRect(px, py, size, size);
        }

        // 3. Gambar Dinding Maze (Garis Tegas)
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3)); // Ketebalan garis dinding

        if (walls[0]) g2.drawLine(px, py, px + size, py);           // Top
        if (walls[1]) g2.drawLine(px + size, py, px + size, py + size); // Right
        if (walls[2]) g2.drawLine(px + size, py + size, px, py + size); // Bottom
        if (walls[3]) g2.drawLine(px, py + size, px, py);           // Left

        // 4. Gambar Label (Huruf A, B, C...)
        if (label != null) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 20));

            // Centering text
            FontMetrics fm = g.getFontMetrics();
            int tx = px + (size - fm.stringWidth(label)) / 2;
            int ty = py + (size - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(label, tx, ty);
        }
    }
}