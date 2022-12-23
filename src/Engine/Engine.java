package src.Engine;

import java.io.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;

import src.Game.Board;
import src.Game.Move;
import src.Pieces.EmptySquare;
import src.Pieces.Pawn;
import src.Pieces.Piece;

public class Engine {
    private static final int SEARCH_DEPTH = 4;
    private static Move bestMove;
    private static int positions = 0;
    public void playMove(Board b) {
        if (b.getFullMoveCount() < 5) {
            if (playDatabaseMove(b)) {
                return;
            }
        }
        LocalTime start = LocalTime.now();
        alphaBetaMax(b, Integer.MIN_VALUE, Integer.MAX_VALUE, SEARCH_DEPTH);
        b.remoteMove(bestMove);
        LocalTime end = LocalTime.now();
        long ms = start.until(end, ChronoUnit.MILLIS);
        System.out.println(String.format("Time: %dms\t\tPositions evaluated:%d", ms, positions));
        positions = 0;
    }

    public boolean playDatabaseMove(Board b) {
        LocalTime start = LocalTime.now();
        ArrayList<String> possibleMoves = new ArrayList<>();
        try {
            File f = new File("/Users/matthewstroup/Desktop/CS/PROJECTS/Chess/Games/Games.txt");
            FileReader fr = new FileReader(f);
            BufferedReader bfr = new BufferedReader(fr);
            String line = bfr.readLine();
            while (line != null) {
                String checkEqual = line.substring(0, Board.gameLog.length());
                if (checkEqual.equals(Board.gameLog)) {
                    line = line.substring(Board.gameLog.length());
                    if (!line.equals("1-0") && !line.equals("0-1") && !line.equals("1/2-1/2")) {
                        String move = line.substring(0, line.indexOf(" "));
                        possibleMoves.add(move);
                    }
                }
                line = bfr.readLine();
            }
            bfr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (possibleMoves.size() == 0) {
            //couldn't find a move
            return false;
        }
        //pick a random move and play it..
        int index = (int)(Math.random()*possibleMoves.size());
        b.remoteMove(new Move(possibleMoves.get(index)));

        LocalTime end = LocalTime.now();
        long ms = start.until(end, ChronoUnit.MILLIS);
        System.out.println(String.format("Time: %dms", ms));
        //successfully played move
        return true;
    }

    public int evaluate(Board board, int depth) {
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

    public int alphaBetaMax(Board board, int alpha, int beta, int depth) {
        if (depth == 0 || board.turnInCheckMate() || board.turnInStaleMate()) {
            positions++;
            return evaluate(board, depth);
        }
        ArrayList<Move> moveList = board.getAllTurnMoves();
        //move order
        moveOrder(moveList);
        for (Move m : moveList) {
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

    public int alphaBetaMin(Board board, int alpha, int beta, int depth) {
        if (depth == 0 || board.turnInCheckMate() || board.turnInStaleMate()) {
            positions++;
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

    public void moveOrder(ArrayList<Move> moves) {
        int[] moveScores = new int[moves.size()];
        for (int i = 0; i < moves.size(); i++) {
            Move m = moves.get(i);
            Piece startingPiece = m.startingPiece;
            Piece endingPiece = m.endingPiece;
            int score = 0;

            if (!(endingPiece instanceof EmptySquare)) {
                score = 10 * Math.abs(endingPiece.value) - Math.abs(startingPiece.value);
            }

            if (startingPiece instanceof Pawn) {
                switch (m.promCharacter) {
                    case 'q' -> {
                        score += 9;
                    }
                    case 'r' -> {
                        score += 5;
                    }
                    case 'b' -> {
                        score += 3;
                    }
                    case 'n' -> {
                        score += 3;
                    }
                }
            } else {
                int offset = startingPiece.white ? -1 : 1;
                if ((startingPiece.white && startingPiece.rlocation != 0) || (!startingPiece.white && startingPiece.rlocation != 7)) {
                    if (startingPiece.clocation == 0) {
                        if (Board.pieces[startingPiece.rlocation+offset][startingPiece.clocation+1] instanceof Pawn) {
                            score -= 350;
                        }
                    } else if (startingPiece.clocation == 7) {
                        if (Board.pieces[startingPiece.rlocation+offset][startingPiece.clocation-1] instanceof Pawn) {
                            score -= 350;
                        }
                    } else {
                        Piece r = Board.pieces[startingPiece.rlocation+offset][startingPiece.clocation+1];
                        Piece l = Board.pieces[startingPiece.rlocation+offset][startingPiece.clocation-1];
                        if (r instanceof Pawn || l instanceof Pawn) {
                            score -= 350;
                        }
                    }
                }       
            }
            moveScores[i] = score;
        }
        sortMoves(moves, moveScores);
    }

    public void sortMoves(ArrayList<Move> moves, int[] moveScores) {
        for (int i = 0; i < moves.size()-1; i++) {
            for (int j = i + 1; j > 0; j--) {
                int swap = j-1;
                if (moveScores[swap] < moveScores[j]) {
                    Collections.swap(moves, j, swap);
                    int temp = moveScores[j];
                    moveScores[j] = moveScores[swap];
                    moveScores[swap] = temp; 
                }
            }
        }
    }
}