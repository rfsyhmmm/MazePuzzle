import java.awt.*;

public class Cell {
    int x, y;
    boolean[] walls = {true, true, true, true}; // Top, Right, Bottom, Left
    boolean visited = false;
    Cell parent;
    Terrain terrain;
    String label = null;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.terrain = Terrain.DIRT; // Default
    }

    public void setTerrain(Terrain t) {
        this.terrain = t;
    }

    // --- METHOD GAMBAR LANTAI (Updated Dark Theme) ---
    public void drawTerrain(Graphics g, int size) {
        int px = x * size;
        int py = y * size;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. LANTAI (FLOOR TILES) - Warna dari Terrain.java yang baru
        g2.setColor(terrain.color);
        g2.fillRect(px, py, size, size);

        // Efek Grid Ubin Samar (Dibuat lebih gelap dan tegas)
        g2.setColor(new Color(0, 0, 0, 60)); // Lebih gelap (alpha 60)
        g2.drawRect(px, py, size, size);

        // 2. DEKORASI TERRAIN (Warna disesuaikan dengan tema gelap)
        if (terrain == Terrain.GRASS) {
            // Rumput gelap
            g2.setColor(new Color(20, 50, 10, 150));
            g2.drawLine(px + 8, py + size - 5, px + 8, py + size - 12);
            g2.drawLine(px + 12, py + size - 8, px + 12, py + size - 15);
            g2.drawLine(px + 22, py + size - 5, px + 22, py + size - 10);
        } else if (terrain == Terrain.WATER) {
            // Gelombang air (biru muda pudar agar terlihat di air gelap)
            g2.setColor(new Color(100, 150, 200, 80));
            g2.drawOval(px + 5, py + 10, 10, 5);
            g2.drawOval(px + 15, py + 18, 8, 4);
        } else if (terrain == Terrain.MUD) {
            // Bercak lumpur pekat
            g2.setColor(new Color(30, 20, 10, 100));
            g2.fillOval(px + 5, py + 5, 12, 12);
            g2.fillOval(px + 18, py + 18, 8, 8);
        } else if (terrain == Terrain.DIRT) {
            // Kerikil batu tua
            g2.setColor(new Color(60, 50, 40, 120));
            g2.fillRect(px + 10, py + 10, 4, 4);
            g2.fillRect(px + 20, py + 22, 3, 3);
        }

        // 3. OVERLAY VISITED (Jejak Hangus/Gelap)
        if (visited) {
            // Warna abu-coklat gelap transparan.
            // Tidak terlalu merah agar terlihat seperti area "mati" atau sudah dieksplorasi.
            g2.setColor(new Color(40, 30, 30, 180));
            // Mengisi penuh satu kotak tanpa celah
            g2.fillRect(px, py, size, size);
        }
    }

    // --- METHOD GAMBAR DINDING & TIANG (Updated Dark Theme) ---
    public void drawWalls(Graphics g, int size) {
        int px = x * size;
        int py = y * size;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 4. WARNA DINDING & TIANG (Lebih Gelap/Batu Arang)
        Color wallColor = new Color(48, 32, 0); // Batu arang tua
        Color postColor = new Color(45, 50, 55); // Tiang sedikit lebih terang dari dinding

        g2.setStroke(new BasicStroke(6.0f));
        g2.setColor(wallColor);

        if (walls[0]) g2.drawLine(px, py, px + size, py);           // Tops
        if (walls[1]) g2.drawLine(px + size, py, px + size, py + size); // Right
        if (walls[2]) g2.drawLine(px + size, py + size, px, py + size); // Bottom
        if (walls[3]) g2.drawLine(px, py + size, px, py);           // Left

        // 5. DEKORASI TIANG (CORNER POSTS)
        int pSize = 8;
        int offset = pSize / 2;

        g2.setColor(postColor);
        if (walls[0] || walls[3]) g2.fillRect(px - offset, py - offset, pSize, pSize);
        if (walls[0] || walls[1]) g2.fillRect(px + size - offset, py - offset, pSize, pSize);
        if (walls[2] || walls[1]) g2.fillRect(px + size - offset, py + size - offset, pSize, pSize);
        if (walls[2] || walls[3]) g2.fillRect(px - offset, py + size - offset, pSize, pSize);

        // Highlight Tiang (Dibuat lebih redup)
        g2.setColor(new Color(200, 100, 0)); // Highlight abu tua
        if (walls[0] || walls[3]) g2.fillRect(px - offset + 1, py - offset + 1, pSize-2, pSize-2);
        if (walls[0] || walls[1]) g2.fillRect(px + size - offset + 1, py - offset + 1, pSize-2, pSize-2);
        if (walls[2] || walls[1]) g2.fillRect(px + size - offset + 1, py + size - offset + 1, pSize-2, pSize-2);
        if (walls[2] || walls[3]) g2.fillRect(px - offset + 1, py + size - offset + 1, pSize-2, pSize-2);

        // 6. LABEL (START / EXIT)
        if (label != null) {
            Font font = new Font("Arial", Font.BOLD, 12);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();

            String text = label;
            int txtW = fm.stringWidth(text);
            int txtH = fm.getHeight();
            int tx = px + (size - txtW) / 2;
            int ty = py + (size - fm.getHeight()) / 2 + fm.getAscent();

            // Background label hitam pekat transparan
            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRoundRect(tx - 4, ty - txtH + 2, txtW + 8, txtH, 6, 6);

            g2.setColor(Color.WHITE);
            // Warna teks sedikit diredam agar tidak terlalu mencolok di tema gelap
            if (label.equals("EXIT")) g2.setColor(new Color(255, 100, 100));
            if (label.equals("Start")) g2.setColor(new Color(100, 255, 100));

            g2.drawString(text, tx, ty);
        }
    }
}