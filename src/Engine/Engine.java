package src.Engine;

import java.io.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;

import src.Game.Board;
import src.Game.Move;
import src.Pieces.*;

public class Engine {
    //TODO: fix "Mate in #" messages
    //TODO: piece maps
    //TODO: endgame weights
    private static final int[][] kingEndPoints = new int[][]{
        {15,12,12,12,12,12,12,15},
        {12,8,8,8,8,8,8,12},
        {12,8,3,3,3,3,8,12},
        {12,8,3,0,0,3,8,12},
        {12,8,3,0,0,3,8,12},
        {12,8,3,3,3,3,8,12},
        {12,8,8,8,8,8,8,12},
        {15,12,12,12,12,12,12,15}
    };
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
        int eval = -1*alphaBetaMax(b, Integer.MIN_VALUE, Integer.MAX_VALUE, SEARCH_DEPTH);
        b.remoteMove(bestMove);
        System.out.println("Current Eval: " + evaluate(b, 0));
        LocalTime end = LocalTime.now();
        long ms = start.until(end, ChronoUnit.MILLIS);
        String evalS = eval + "";
        if (eval >= 1000000) {
            evalS = "Mate in " + eval%10;
        } else if (eval <= -1000000) {
            evalS = "Mate in " + (eval*-1)%10;
        }
        System.out.println(String.format("Move: %s\tEval:%s\t\t\tPositions Evaluated: %d\tTime:%dms", bestMove, evalS, positions, ms));
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
        Move move = new Move(possibleMoves.get(index));
        b.remoteMove(move);
        int eval = evaluate(b, 0);
        String evalS = eval + "";
        if (eval > 1000000) {
            evalS = "Mate in " + eval%10;
        }
        LocalTime end = LocalTime.now();
        long ms = start.until(end, ChronoUnit.MILLIS);
        System.out.println(String.format("Move: %s\tEval:%s\t\t\tPositions Evaluated: %d\tTime:%dms", move, evalS, positions, ms));
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
        int whiteEval = 0;
        int blackEval = 0;
        for (int i = 0; i < Board.pieces.length; i++) {
            for (int j = 0; j < Board.pieces.length; j++) {
                Piece p = Board.pieces[i][j];
                if (p instanceof EmptySquare) continue;
                if (p.white)
                    whiteEval += p.value;
                else
                    blackEval -= p.value;
            }
        }
        int materialCount = whiteEval-blackEval;
        double whiteWeight = endGameWeight(true);
        double blackWeight = endGameWeight(false);
        // whiteEval += endGameEval(true, whiteWeight, materialCount);
        // blackEval += endGameEval(false, blackWeight, materialCount);
        eval = whiteEval-blackEval;
        int perspective = Board.whiteTurn ? 1 : -1;
        return eval * perspective;
    }

    public double endGameWeight(boolean white) {
        int eval = 0;
        for (int i = 0; i < Board.pieces.length; i++) {
            for (int j = 0; j < Board.pieces.length; j++) {
                Piece p = Board.pieces[i][j];
                if (p instanceof Pawn || p instanceof EmptySquare) continue;
                if (p.white == white)
                    eval += p.value;
            }
        }
        if (!white) eval*=-1;
        return 1-Math.min(1, eval*0.0006);
    }

    public int endGameEval(boolean white, double endGameWeight, int material) {
        double endGameEval = 0;
        //ensure calling team is more than 3 points (enough to checkmate) ahead
        if ((white && material > 2) || (!white && material < -2)) {
            King ourKing = white ? Board.whiteKing : Board.blackKing;
            King enemyKing = white ? Board.blackKing : Board.whiteKing;
            endGameEval+=kingEndPoints[enemyKing.rlocation][enemyKing.clocation]*10;

            int orthogDistance = Math.abs(ourKing.rlocation-enemyKing.rlocation)+Math.abs(ourKing.clocation-enemyKing.clocation);
            endGameEval += (14-orthogDistance)*3;

            return (int)(endGameEval*endGameWeight*0.25);
        }
        return 0;
    }

    public int alphaBetaMax(Board board, int alpha, int beta, int depth) {
        if (depth == 0) {
            positions++;
            return evaluate(board, depth);
        }
        if (board.turnInCheckMate() || board.turnInStaleMate()) {
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
        if (depth == 0) {
            positions++;
            return evaluate(board, depth) * -1;
        }
        if (board.turnInCheckMate() || board.turnInStaleMate()) {
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

    // public void searchCaptures(Board b, int alpha, int beta) {
    //     int eval = evaluate(b, 0);

    // }

    public void moveOrder(ArrayList<Move> moves) {
        double[] moveScores = new double[moves.size()];
        for (int i = 0; i < moves.size(); i++) {
            Move m = moves.get(i);
            Piece startingPiece = m.startingPiece;
            Piece endingPiece = m.endingPiece;
            double score = 0;

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

    public void sortMoves(ArrayList<Move> moves, double[] moveScores) {
        for (int i = 0; i < moves.size()-1; i++) {
            for (int j = i + 1; j > 0; j--) {
                int swap = j-1;
                if (moveScores[swap] < moveScores[j]) {
                    Collections.swap(moves, j, swap);
                    double temp = moveScores[j];
                    moveScores[j] = moveScores[swap];
                    moveScores[swap] = temp; 
                }
            }
        }
    }
}