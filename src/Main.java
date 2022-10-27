package src;
import java.util.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args) {
        // Engine e = new Engine();
        Board b = new Board(Piece.defaultFEN, null);

        b.pack();
        b.setResizable(false);
        b.setLocationRelativeTo(null);
        b.setVisible(true);
        
        
        // runGeneration(b, 5);
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
            Move clone = new Move(m.startingPiece.clonePiece(), m.endingPiece.clonePiece(), m.promCharacter);
            b.APIMove(m);
            int add = moveGeneration(b, depth-1);
            positions += add;
            b.APIUnMove(m, clone);
        }

        return positions;
    }
}