package src;
import java.util.*;

import src.Engine.Engine;
import src.Game.*;
import src.Pieces.*;

import java.time.*;
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args) {
        Engine e = new Engine();
        Board b = new Board(Piece.defaultFEN,e);
        // Board b = new Board("3r4/8/3k4/8/8/3K4/8/8 w - - 0 1",e);
        // Board b = new Board("8/3K4/4P3/8/8/8/6k1/7q w - - 0 1",e);
        // Board b = new Board("Q7/5p1p/5P2/5PPN/6Pk/4N1Rp/7P/6K1 b - - 0 1",e);
        // Board b = new Board("Q6Q/2Q5/2Q3Q1/8/4Q3/1k6/8/4K3 w - - 0 1", e);
        // Board b = new Board("7k/8/3Q4/2Q5/8/4Q3/8/7K w - - 0 1");

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
        int positions = 0;
        ArrayList<Move> moves = b.getAllTurnMoves();

        if (depth == 1) {
            return moves.size();
        }

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