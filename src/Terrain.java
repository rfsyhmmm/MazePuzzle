import java.awt.Color;

public enum Terrain {
    DIRT(1, new Color(210, 180, 140)),  // Cost +0
    GRASS(2, new Color(84, 167, 7)),   // Cost +1
    MUD(6, new Color(101, 67, 33)),     // Cost +5
    WATER(11, new Color(34, 159, 200));  // Cost +10

    public final int cost;
    public final Color color;

    Terrain(int cost, Color color) {
        this.cost = cost;
        this.color = color;
    }
}