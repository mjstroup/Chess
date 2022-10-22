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
}