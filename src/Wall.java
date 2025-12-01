public class Wall {
    public Cell cell1, cell2;
    public String direction;

    public Wall(Cell c1, Cell c2, String dir) {
        this.cell1 = c1;
        this.cell2 = c2;
        this.direction = dir;
    }
}