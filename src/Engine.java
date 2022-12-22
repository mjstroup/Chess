package src;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Engine {
    private static final int SEARCH_DEPTH = 4;
    private static Move bestMove;
    public void playMove(Board b, boolean white) {
        LocalTime start = LocalTime.now();
        alphaBetaMax(b, Integer.MIN_VALUE, Integer.MAX_VALUE, SEARCH_DEPTH);
        b.remoteMove(bestMove);
        LocalTime end = LocalTime.now();
        long ms = start.until(end, ChronoUnit.MILLIS);
        System.out.println(String.format("Time: %dms", ms));
    }
    public static int evaluate(Board board, int depth) {
        if (board.turnInCheckMate()) {
            return -1000000-depth;
        }
        if (board.turnInStaleMate()) {
            return 0;
        }
        int eval = 0;
        for (int i = 0; i < Board.pieces.length; i++) {
            for (int j = 0; j < Board.pieces.length; j++) {
                Piece p = Board.pieces[i][j];
                eval += p.value;
            }
        }
        int perspective = Board.whiteTurn ? 1 : -1;
        return eval * perspective;
    }

    public static int alphaBetaMax(Board board, int alpha, int beta, int depth) {
        if (depth == 0 || board.turnInCheckMate() || board.turnInStaleMate()) {
            return evaluate(board, depth);
        }
        for (Move m : board.getAllTurnMoves()) {
            Move clone = new Move(m.startingPiece.clonePiece(), m.endingPiece.clonePiece(), m.promCharacter);
            board.APIMove(m);
            int value = alphaBetaMin(board, alpha, beta, depth-1);
            board.APIUnMove(m, clone);
            if (value >= beta)
                return beta;
            if (value > alpha) {
                alpha = value;
                if (depth == SEARCH_DEPTH) {
                    Engine.bestMove = m;
                }
            }
        }
        return alpha;
    }

    public static int alphaBetaMin(Board board, int alpha, int beta, int depth) {
        if (depth == 0 || board.turnInCheckMate() || board.turnInStaleMate()) {
            return evaluate(board, depth) * -1;
        }
        for (Move m : board.getAllTurnMoves()) {
            Move clone = new Move(m.startingPiece.clonePiece(), m.endingPiece.clonePiece(), m.promCharacter);
            board.APIMove(m);
            int value = alphaBetaMax(board, alpha, beta, depth-1);
            board.APIUnMove(m, clone);
            if (value <= alpha)
                return alpha;
            if (value < beta)
                beta = value;
        }
        return beta;
    }
}