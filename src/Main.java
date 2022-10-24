package src;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Engine e = new Engine();
        // Board b = new Board(Piece.defaultFEN, null);
        // Board b = new Board("rnbqkbnr/pppppppp/8/8/8/P7/1PPPPPPP/RNBQKBNR b KQkq - 0 1", null);
        // Board b = new Board("8/8/8/4p1K1/2k1P3/8/8/8 b - - 0 1", null);
        Board b = new Board("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", null);
        // Board b = new Board("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPPKNnPP/RNBQ3R b - - 1 8", null);
        b.pack();
        b.setResizable(false);
        b.setLocationRelativeTo(null);
        b.setVisible(true);
        
        for (int i = 1; i < 5; i++) {
            System.out.println("Depth " + i + ": " + moveGeneration(b, i));
        }
    }

    public static int moveGeneration(Board b, int depth) {
        if (depth == 0) {
            return 1;
        }

        int positions = 0;
        ArrayList<Move> moves = b.getAllTurnMoves();
        for (Move m : moves) {
            String fen = b.getFEN();
            b.APIMove(new Move(m.startingPiece.clonePiece(), m.endingPiece.clonePiece(), m.promCharacter));
            Board.pieces = b.fenStringToPieces(b.getFEN());
            int add = moveGeneration(b, depth-1);
            positions += add;
            Board.pieces = b.fenStringToPieces(fen);
        }

        return positions;
    }
}