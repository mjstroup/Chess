package src;
import java.util.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args) {
        // Engine e = new Engine();
        Board b = new Board(Piece.defaultFEN, null);
        // Board b = new Board("rnbqkbnr/pppppppp/8/8/8/P7/1PPPPPPP/RNBQKBNR b KQkq - 0 1", null);
        // Board b = new Board("8/8/8/4p1K1/2k1P3/8/8/8 b - - 0 1", null);
        // Board b = new Board("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", null);
        // Board b = new Board("rnbqkbnr/pp1ppppp/8/2p5/8/2N5/PPP1PPPP/R1BQKBNR b KQkq - 0 1", null);
        b.pack();
        b.setResizable(false);
        b.setLocationRelativeTo(null);
        b.setVisible(true);
        
        
        runGeneration(b, 5);
    }

    public static void runGeneration(Board b, int depth) {
        for (int i = 1; i < depth+1; i++) {
            LocalTime start = LocalTime.now();
            int positions = moveGeneration(b, i);
            LocalTime end = LocalTime.now();
            long ms = start.until(end, ChronoUnit.MILLIS);
            System.out.println(String.format("Depth %d: %d (%d ms)", i, positions, ms));
        }
    }
    public static int moveGeneration(Board b, int depth) {
        if (depth == 0) {
            return 1;
        }

        int positions = 0;
        ArrayList<Move> moves = b.getAllTurnMoves();
        for (Move m : moves) {
            Move clone = new Move(m.startingPiece.clonePiece(), m.endingPiece.clonePiece());
            // System.out.println("Before move:\n" + b);
            b.APIMove(m);
            // System.out.println("After move:\n" + b);
            // System.out.println("Move: " + m);
            int add = moveGeneration(b, depth-1);
            positions += add;
            b.APIUnMove(m, clone);
            // System.out.println("After unmove:\n" + b);
        }

        return positions;
    }
}