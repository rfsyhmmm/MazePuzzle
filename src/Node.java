import java.awt.*;

public class Node implements Comparable<Node> {
    public Cell cell;
    public int gCost; // Jarak dari start
    public int hCost; // Jarak estimasi ke end
    public int fCost; // gCost + hCost
    public Node parent; // Untuk melacak jalur balik (opsional, tapi berguna)

    // Constructor 4 Argumen
    public Node(Cell cell, int gCost, int hCost, Node parent) {
        this.cell = cell;
        this.gCost = gCost;
        this.hCost = hCost;
        this.fCost = gCost + hCost;
        this.parent = parent;
    }

    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.fCost, other.fCost);
    }
}