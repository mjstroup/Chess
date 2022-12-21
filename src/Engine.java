package src;
import java.util.ArrayList;

import java.util.Random;
public class Engine {
    private static Random r = new Random();
    public void playMove(Board b, boolean white) {
        if (white) {
            ArrayList<Move> moves = b.getAllWhiteMoves();
            Move random = moves.get(r.nextInt(moves.size()));
            b.remoteMove(random);
        } else {
            ArrayList<Move> moves = b.getAllBlackMoves();
            Move random = moves.get(r.nextInt(moves.size()));
            b.remoteMove(random);
        }
    }
    public static int evaluation(Board b) {
        if (b.turnInCheckMate()) {
            return Board.whiteTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }
        if (b.turnInStaleMate()) {
            return 0;
        }
        int eval = 0;
        for (int i = 0; i < Board.pieces.length; i++) {
            for (int j = 0; j < Board.pieces.length; j++) {
                Piece p = Board.pieces[i][j];
                eval += p.value;
            }
        }
        return eval;
    }
}
