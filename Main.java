public class Main {
    public static void main(String[] args) {
        Board b = new Board(Piece.defaultFEN);
        // Board b = new Board("8/8/8/4p1K1/2k1P3/8/8/8 b - - 0 1");
        b.pack();
        b.setResizable(false);
        b.setLocationRelativeTo(null);
        b.setVisible(true);
    }
}