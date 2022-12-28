package src.Engine;

import src.Game.Board;
import src.Game.Move;
import src.Pieces.*;

import java.io.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;

public class Engine {
    public boolean white;
    private int SEARCH_DEPTH;
    private static Move bestMove;
    private static final double ENDGAME_MULTIPLIER = 0.0006;
    private static int positions = 0;
    public Engine(int depth, boolean white) {
        this.SEARCH_DEPTH = depth;
        this.white = white;
    }
    public void playMove(Board b) {
        if (b.getFullMoveCount() < 5) {
            if (playDatabaseMove(b)) {
                return;
            }
        }
        LocalTime start = LocalTime.now();
        int perspective = Board.whiteTurn ? 1 : -1;
        int eval = perspective*alphaBetaMax(b, Integer.MIN_VALUE, Integer.MAX_VALUE, SEARCH_DEPTH);
        b.eval = eval;
        b.remoteMove(bestMove);
        if (b.gameOver != 0) return;
        LocalTime end = LocalTime.now();
        long ms = start.until(end, ChronoUnit.MILLIS);
        String evalS = eval/100. + "";
        System.out.println(eval);
        if (eval >= 900000) {
            evalS = "Mate in " + (1000000-eval);
        } else if (eval <= -900000) {
            evalS = "Mate in " + (1000000+eval);
        }
        System.out.println(String.format("Move: %s\tEval:%s\t\t\tPositions Evaluated: %d\tTime:%dms", bestMove, evalS, positions, ms));
        positions = 0;
        b.canPickUp = true;
    }

    public boolean playDatabaseMove(Board b) {
        LocalTime start = LocalTime.now();
        ArrayList<String> possibleMoves = new ArrayList<>();
        try {
            File f = new File("Games/Games.txt");
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
        if (b.gameOver != 0) return true;
        int eval = evaluate(b,1);
        b.eval = eval;
        String evalS = eval/100. + "";
        LocalTime end = LocalTime.now();
        long ms = start.until(end, ChronoUnit.MILLIS);
        System.out.println(String.format("Move: %s\tEval:%s\t\t\tPositions Evaluated: %d\tTime:%dms", move, evalS, positions, ms));
        //successfully played move
        b.canPickUp = true;
        return true;
    }

    public int evaluate(Board board, int depth) {
        if (board.turnInCheckMate()) {
            return -1000000+depth;
        }
        if (board.turnInStaleMate()) {
            return 0;
        }
        int eval = 0;
        int whiteMaterial = 0;
        int blackMaterial = 0;
        int whiteNoPawns = 0;
        int blackNoPawns = 0;
        int whiteEval = 0;
        int blackEval = 0;
        for (int i = 0; i < Board.pieces.length; i++) {
            for (int j = 0; j < Board.pieces[0].length; j++) {
                Piece p = Board.pieces[i][j];
                if (p instanceof EmptySquare) continue;
                if (p.white) {
                    whiteMaterial += p.value;
                    if (!(p instanceof Pawn))
                        whiteNoPawns += p.value;
                } else {
                    blackMaterial += p.value;
                    if (!(p instanceof Pawn))
                        blackNoPawns += p.value;
                }
            }
        }
        double whiteWeight = endGameWeight(whiteNoPawns);
        double blackWeight = endGameWeight(blackNoPawns);

        whiteEval += whiteMaterial;
        blackEval += blackMaterial;

        whiteEval += pieceTableEval(true, whiteWeight);
        blackEval += pieceTableEval(false, blackWeight);


        whiteEval += endGameEval(true, whiteMaterial, blackMaterial, whiteWeight);
        blackEval += endGameEval(false, blackMaterial, whiteMaterial, blackWeight);

        eval = whiteEval-blackEval;
        int perspective = Board.whiteTurn ? 1 : -1;
        return eval * perspective;
    }

    public double endGameWeight(int material) {
        //from 0 to 1
        return 1 - Math.min(1, material*ENDGAME_MULTIPLIER);
    }

    public int endGameEval(boolean white, int friendlyMaterial, int opponentMaterial, double weight) {
        int endGameEval = 0;
        if (friendlyMaterial > opponentMaterial + 200 && weight > 0) {
            King friendlyKing = white ? Board.whiteKing : Board.blackKing;
            King enemyKing = white ? Board.blackKing : Board.whiteKing;

            //maximize distance of enemy king from center
            int rFromCenter = Math.max(3 - enemyKing.rlocation, enemyKing.rlocation - 4);
            int cFromCenter = Math.max(3 - enemyKing.clocation, enemyKing.clocation - 4);
            int totalFromCenter = rFromCenter + cFromCenter;

            endGameEval += totalFromCenter*10;

            //minimize distance between kings
            int rDist = Math.abs(friendlyKing.getR() - enemyKing.getR());
            int cDist = Math.abs(friendlyKing.getC() - enemyKing.getC());
            int totalDist = rDist + cDist;
            
            endGameEval += (14-totalDist)*5;

            return (int)(endGameEval * weight);
        }
        
        return 0;
    }

    public int pieceTableEval(boolean white, double endgameWeight) {
        int val = 0;
        for (int i = 0; i < Board.pieces.length; i++) {
            for (int j = 0; j < Board.pieces[0].length; j++) {
                Piece p = Board.pieces[i][j];
                if (p.white == white && (p instanceof King)) {
                    int bonus;
                    if (endgameWeight >= 0.4) {
                        bonus = King.endMapping[p.getR()][p.getC()];
                    } else {
                        bonus = p.mapping[p.getR()][p.getC()];
                    }
                    val += bonus;
                } else if (p.white == white && !(p instanceof EmptySquare)) {
                    val += p.mapping[p.getR()][p.getC()];
                }
            }
        }
        return val;
    }

    public int alphaBetaMax(Board board, int alpha, int beta, int depth) {
        if (depth == 0 || board.turnInCheckMate() || board.turnInStaleMate()) {
            int mateIn = (SEARCH_DEPTH-depth)/2;
            positions++;
            return evaluate(board, mateIn);
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
            int mateIn = ((SEARCH_DEPTH-depth)-1)/2;
            return evaluate(board, mateIn) * -1;
        }
        ArrayList<Move> moveList = board.getAllTurnMoves();
        //move order
        moveOrder(moveList);
        for (Move m : moveList) {
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
                if ((startingPiece.white && startingPiece.getR() != 0) || (!startingPiece.white && startingPiece.getR() != 7)) {
                    if (startingPiece.getC() == 0) {
                        if (Board.pieces[startingPiece.getR()+offset][startingPiece.getC()+1] instanceof Pawn) {
                            score -= 3;
                        }
                    } else if (startingPiece.getC() == 7) {
                        if (Board.pieces[startingPiece.getR()+offset][startingPiece.getC()-1] instanceof Pawn) {
                            score -= 3;
                        }
                    } else {
                        Piece r = Board.pieces[startingPiece.getR()+offset][startingPiece.getC()+1];
                        Piece l = Board.pieces[startingPiece.getR()+offset][startingPiece.getC()-1];
                        if (r instanceof Pawn || l instanceof Pawn) {
                            score -= 3;
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