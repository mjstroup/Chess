package src.Game;

import src.Pieces.*;
import java.util.*;

public class Move {
    public Piece startingPiece;
    public Piece endingPiece;
    public char promCharacter;
    public boolean checkMate;
    public boolean check;

    public Move(Piece startingPiece, Piece endingPiece) {
        this.startingPiece = startingPiece;
        this.endingPiece = endingPiece;
    }

    public Move(Piece startingPiece, Piece endingPiece, char promCharacter) {
        this.startingPiece = startingPiece;
        this.endingPiece = endingPiece;
        this.promCharacter = promCharacter;
    }

    public Move(String notation) {
        boolean whiteTurn = Board.whiteTurn;
        if (notation.contains("+") || notation.contains("#")) {
            notation = notation.substring(0, notation.length()-1);
        }

        //short castle
        if (notation.equals("O-O")) {
            int row = whiteTurn ? 7 : 0;
            this.startingPiece = whiteTurn ? Board.whiteKing : Board.blackKing;
            this.endingPiece = Board.pieces[row][6];
            return;
        }
        //long castle
        if (notation.equals("O-O-O")) {
            int row = whiteTurn ? 7 : 0;
            this.startingPiece = whiteTurn ? Board.whiteKing : Board.blackKing;
            this.endingPiece = Board.pieces[row][2];
            return;
        }

        //pawn moves
        if (notation.charAt(0) > 96) {
            if (notation.contains("=")) {
                this.promCharacter = notation.charAt(notation.length()-1);
                notation = notation.substring(0, notation.length()-2);
            }
            String coords = notation.substring(notation.length()-2);
            notation = notation.substring(0, notation.length()-2);
            this.endingPiece = coordsToPiece(coords);
            //two character move.. (e.g e4)
            if (notation.length() == 0) {
                int offset = whiteTurn ? 1 : -1;
                if (Board.pieces[this.endingPiece.rlocation+offset][this.endingPiece.clocation] instanceof Pawn) {
                    this.startingPiece = Board.pieces[this.endingPiece.rlocation+offset][this.endingPiece.clocation];
                    return;
                } else if (Board.pieces[this.endingPiece.rlocation+offset+offset][this.endingPiece.clocation] instanceof Pawn) {
                    this.startingPiece = Board.pieces[this.endingPiece.rlocation+offset+offset][this.endingPiece.clocation];
                    return;
                }
            }
            //bxc5, all that's left is bx
            int offset = whiteTurn ? 1 : -1;
            int column = notation.charAt(0)-97;
            this.startingPiece = Board.pieces[this.endingPiece.rlocation+offset][column];
            return;
        }

        String coords = notation.substring(notation.length()-2);
        notation = notation.substring(0, notation.length()-2);
        this.endingPiece = coordsToPiece(coords);

        char pieceType = notation.charAt(0);
        notation = notation.substring(1);

        boolean capture = notation.contains("x");
        if (capture) {
            notation = notation.substring(0, notation.indexOf("x"));
        }
        //now all thats left is disambiguation
        String disambiguation = notation;
        ArrayList<Piece> attackedBy = this.endingPiece.attackedByList(whiteTurn);
        if (disambiguation.length() == 0) {
            for (Piece p : attackedBy) {
                if ((p.abbreviation-32) == pieceType) {
                    this.startingPiece = p;
                    break;
                }
            }
        } else if (disambiguation.length() == 2) {
            this.startingPiece = coordsToPiece(disambiguation);
        } else {
            char disChar = disambiguation.charAt(0);
            if (disChar < 58) {
                //rank based
                int row = (8-(disChar-48));
                for (Piece p : attackedBy) {
                    if ((p.abbreviation-32) == pieceType && p.rlocation == row) {
                        this.startingPiece = p;
                    }
                }
            } else {
                int col = disChar-97;
                for (Piece p : attackedBy) {
                    if ((p.abbreviation-32) == pieceType && p.clocation == col) {
                        this.startingPiece = p;
                    }
                }
            }
        }
    }

    public Piece coordsToPiece(String s) {
        char letter = s.charAt(0);
        char number = s.charAt(1);
        int x = (8-(number-48));
        int y = letter-97;
        return Board.pieces[x][y];
    }
    
    public String toString() {
        //castling
        if (startingPiece instanceof King) {
            if (Math.abs(startingPiece.clocation - endingPiece.clocation) == 2) {
                return endingPiece.clocation == 6 ? "O-O" : "O-O-O";
            }
        }

        boolean isCapture = !(endingPiece instanceof EmptySquare);
        if (startingPiece instanceof Pawn && endingPiece instanceof EmptySquare && startingPiece.clocation != endingPiece.clocation) {
            isCapture = true;
        }
        boolean isPromotion = promCharacter != Character.UNASSIGNED;
        /*
         * 0 -> no disambiguation needed
         * 1 -> file based disambiguation needed
         * 2 -> rank based disambiguation needed
         * 3 -> both file and rank disambiguation needed
         */
        int needsDisambiguation = 0;
        if (startingPiece instanceof Pawn) {
            if (!(endingPiece instanceof EmptySquare)) {
                needsDisambiguation = 1;
            }
            if (endingPiece instanceof EmptySquare && startingPiece.clocation != endingPiece.clocation) {
                needsDisambiguation = 1;
            }
        }
        if (needsDisambiguation == 0 && !(startingPiece instanceof Pawn)) {
            for (Piece p : endingPiece.attackedByList(startingPiece.white)) {
                
                if (p.rlocation == startingPiece.rlocation && p.clocation == startingPiece.clocation) continue;
                if (p.abbreviation == startingPiece.abbreviation) {
                    //make sure that the piece is not being xrayed
                    if (p instanceof Bishop || p instanceof Rook || p instanceof Queen) {
                        if (p instanceof Bishop) {
                            int r1 = p.rlocation;
                            int c1 = p.clocation;
                            int r2 = startingPiece.rlocation;
                            int c2 = startingPiece.clocation;
                            //same diag
                            if ((r1 - c1 == r2 - c2) || (r1 + c1 == r2 + c2)) {
                                if (Math.abs(p.rlocation-startingPiece.rlocation) < Math.abs(p.rlocation-endingPiece.rlocation)) {
                                    continue;
                                }
                            }
                        }
                        if (p instanceof Rook) {
                            if ((p.rlocation-startingPiece.rlocation)*(p.clocation-startingPiece.clocation) == 0) {
                                if (Math.abs((p.rlocation-startingPiece.rlocation)+(p.clocation-startingPiece.clocation)) < Math.abs((p.rlocation-endingPiece.rlocation)+(p.clocation-endingPiece.clocation))) {
                                    continue;
                                }
                            }
                        }
                        if (p instanceof Queen) {
                            if ((p.rlocation-startingPiece.rlocation)*(p.clocation-startingPiece.clocation) == 0) {
                                if (Math.abs((p.rlocation-startingPiece.rlocation)+(p.clocation-startingPiece.clocation)) < Math.abs((p.rlocation-endingPiece.rlocation)+(p.clocation-endingPiece.clocation))) {
                                    continue;
                                }
                            }
                            int r1 = p.rlocation;
                            int c1 = p.clocation;
                            int r2 = startingPiece.rlocation;
                            int c2 = startingPiece.clocation;
                            //same diag
                            if ((r1 - c1 == r2 - c2) || (r1 + c1 == r2 + c2)) {
                                if (Math.abs(p.rlocation-startingPiece.rlocation) < Math.abs(p.rlocation-endingPiece.rlocation)) {
                                    continue;
                                }
                            }
                        }
                    }
                    //both file and rank disambiguation needed
                    if ((needsDisambiguation == 1 && p.clocation == startingPiece.clocation) || (needsDisambiguation == 2 && p.rlocation == startingPiece.rlocation)) {
                        needsDisambiguation = 3;
                        break;
                    }
                    //file disambiguation needed
                    if (p.rlocation == startingPiece.rlocation) {
                        needsDisambiguation = 1;
                    }
                    //rank disambiguation needed
                    if (p.clocation == startingPiece.clocation) {
                        needsDisambiguation = 2;
                    }
                    if (p.clocation != startingPiece.clocation && p.rlocation != startingPiece.rlocation) {
                        needsDisambiguation = 1;
                    }
                }
            }
        }
        String move = startingPiece instanceof Pawn ? "" : (char)(startingPiece.abbreviation-32) + "";
        //disambiguation
        switch (needsDisambiguation) {
            case 0 -> {
                break;
            }
            //file based
            case 1 -> {
                move += startingPiece.toString().substring(0,1);
            }
            //rank based
            case 2 -> {
                move += startingPiece.toString().substring(1);
            }
            //both
            case 3 -> {
                move += startingPiece.toString();
            }
        }
        //captures
        move += isCapture ? "x" : "";
        
        //ending piece
        move += endingPiece.toString();

        //promotions
        move += isPromotion ? "=" + (char)(this.promCharacter-32) : "";

        //check or checkmate
        move += check ? "+" : "";
        move += checkMate ? "#" : ""; 

        return move;
    }
}