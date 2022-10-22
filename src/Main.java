package src;

public class Main {
    public static void main(String[] args) {
        Engine e = new Engine();
        Board b = new Board(Piece.defaultFEN, e);
        // Board b = new Board("rnbqkbnr/ppp1ppp1/7p/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3");
        b.pack();
        b.setResizable(false);
        b.setLocationRelativeTo(null);
        b.setVisible(true);
    }
}