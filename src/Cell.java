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

    // Di dalam file Cell.java

    public void draw(Graphics g, int size) {
        int px = x * size;
        int py = y * size;
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- 1. BACKGROUND TERRAIN (Gradasi & Pola) ---
        Color baseColor = terrain.color;
        GradientPaint gp = new GradientPaint(px, py, baseColor.brighter(), px + size, py + size, baseColor.darker());
        g2.setPaint(gp);
        g2.fillRect(px, py, size, size);

        // Tekstur Terrain (Sama seperti sebelumnya)
        if (terrain == Terrain.GRASS) {
            g2.setColor(new Color(60, 120, 0, 150)); g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(px + 5, py + size - 5, px + 5, py + size - 12);
            g2.drawLine(px + 5, py + size - 5, px + 8, py + size - 10);
            if ((x + y) % 2 == 0) {
                g2.drawLine(px + 20, py + size - 5, px + 20, py + size - 15);
                g2.drawLine(px + 20, py + size - 5, px + 15, py + size - 10);
            }
        } else if (terrain == Terrain.WATER) {
            g2.setColor(new Color(255, 255, 255, 80)); g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(px + 5, py + 10, px + size - 5, py + 10);
            g2.drawLine(px + 5, py + 20, px + size - 5, py + 20);
        } else if (terrain == Terrain.MUD) {
            g2.setColor(new Color(60, 40, 10, 100)); 
            g2.fillOval(px + 5, py + 5, size/2, size/2 - 5);
            if ((x * y) % 3 == 0) g2.fillOval(px + 15, py + 15, 8, 8);
        } else if (terrain == Terrain.DIRT) {
            g2.setColor(new Color(150, 120, 80, 180));
            g2.fillOval(px + 5, py + 5, 4, 4);
            g2.fillOval(px + 20, py + 10, 3, 3);
            g2.fillOval(px + 10, py + 20, 5, 5);
        }

        // --- 2. OVERLAY VISITED ---
        if (visited) {
            g2.setColor(new Color(255, 255, 200, 120)); 
            g2.fillRect(px, py, size, size);
        }

        // --- 3. DINDING MAZE (3D Style) ---
        // Shadow
        g2.setColor(new Color(0, 0, 0, 60)); 
        g2.setStroke(new BasicStroke(6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); 
        int shOff = 3;
        if (walls[0]) g2.drawLine(px + shOff, py + shOff, px + size + shOff, py + shOff);
        if (walls[1]) g2.drawLine(px + size + shOff, py + shOff, px + size + shOff, py + size + shOff);
        if (walls[2]) g2.drawLine(px + size + shOff, py + size + shOff, px + shOff, py + size + shOff);
        if (walls[3]) g2.drawLine(px + shOff, py + size + shOff, px + shOff, py + shOff);

        // Body
        g2.setColor(new Color(60, 60, 60)); 
        g2.setStroke(new BasicStroke(6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (walls[0]) g2.drawLine(px, py, px + size, py);           
        if (walls[1]) g2.drawLine(px + size, py, px + size, py + size); 
        if (walls[2]) g2.drawLine(px + size, py + size, px, py + size); 
        if (walls[3]) g2.drawLine(px, py + size, px, py);           

        // Highlight
        g2.setColor(new Color(10, 10, 10)); 
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (walls[0]) g2.drawLine(px, py, px + size, py);           
        if (walls[1]) g2.drawLine(px + size, py, px + size, py + size); 
        if (walls[2]) g2.drawLine(px + size, py + size, px, py + size); 
        if (walls[3]) g2.drawLine(px, py + size, px, py);           
        
        // Dekorasi Sudut
        g2.setColor(new Color(50, 50, 50));
        if (walls[0] || walls[3]) g2.fillOval(px - 4, py - 4, 8, 8);

        // --- 4. GAMBAR LABEL (WARNA MERAH) ---
        if (label != null) {
            Font font = new Font("Arial", Font.BOLD, 14); 
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            
            int textWidth = fm.stringWidth(label);
            int textHeight = fm.getAscent();
            int tx = px + (size - textWidth) / 2;
            int ty = py + (size - fm.getHeight()) / 2 + textHeight;

            // 1. Outline Hitam (Supaya kontras)
            g2.setColor(Color.BLACK);
            g2.drawString(label, tx - 1, ty - 1);
            g2.drawString(label, tx + 1, ty - 1);
            g2.drawString(label, tx - 1, ty + 1);
            g2.drawString(label, tx + 1, ty + 1);
            
            // 2. Teks Utama MERAH
            g2.setColor(Color.RED); // <-- Perubahan di sini (sebelumnya WHITE)
            g2.drawString(label, tx, ty);
        }
    }
}