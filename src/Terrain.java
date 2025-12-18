import java.awt.Color;

public enum Terrain {
    DIRT(0, new Color(225, 200, 160)),  // Cost +0
    GRASS(1, new Color(100, 180, 60)),   // Cost +1
    MUD(5, new Color(120, 90, 60)),     // Cost +5
    WATER(10, new Color(100, 200, 230));  // Cost +10

    public final int cost;
    public final Color color;

    Terrain(int cost, Color color) {
        this.cost = cost;
        this.color = color;
    }
}