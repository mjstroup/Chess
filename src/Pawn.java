package src;
import java.util.*;
public class Pawn extends Piece {
    public Pawn (Boolean white, int rlocation, int clocation) {
        this.rlocation = rlocation;
        this.clocation = clocation;
        this.white = white;
        this.abbreviation = 'p';
        fileName = this.white ? "./Images/wP.png" : "./Images/bP.png";
    }
    @Override
    public ArrayList<Move> getPossibleMoves() {
        ArrayList<Move> list = new ArrayList<>();
        if (Board.whiteTurn != this.white)
            return list;
        //en passant left white
        if (this.white && this.clocation != 0 && Board.pieces[rlocation-1][clocation-1] instanceof EmptySquare && this.white == Board.whiteTurn && ((EmptySquare)Board.pieces[rlocation-1][clocation-1]).enPassant) {
            list.add(new Move(this, Board.pieces[rlocation-1][clocation-1]));
        }
        //en passant right white
        if (this.white && this.clocation != 7 && Board.pieces[rlocation-1][clocation+1] instanceof EmptySquare && this.white == Board.whiteTurn && ((EmptySquare)Board.pieces[rlocation-1][clocation+1]).enPassant) {
            list.add(new Move(this, Board.pieces[rlocation-1][clocation+1]));
        }
        //en passant left black
        if (!this.white && this.clocation != 0 && Board.pieces[rlocation+1][clocation-1] instanceof EmptySquare && this.white == Board.whiteTurn && ((EmptySquare)Board.pieces[rlocation+1][clocation-1]).enPassant) {
            list.add(new Move(this, Board.pieces[rlocation+1][clocation-1]));
        }
        //en passant right black
        if (!this.white && this.clocation != 7 && Board.pieces[rlocation+1][clocation+1] instanceof EmptySquare && this.white == Board.whiteTurn && ((EmptySquare)Board.pieces[rlocation+1][clocation+1]).enPassant) {
            list.add(new Move(this, Board.pieces[rlocation+1][clocation+1]));
        }
        if (this.white) {
            //captures
            Piece topLeft = null;
            if (clocation != 0)
                topLeft = Board.pieces[rlocation-1][clocation-1];
            Piece topRight = null;
            if (clocation != 7) 
                topRight = Board.pieces[rlocation-1][clocation+1];
            if (topLeft != null && !(topLeft instanceof EmptySquare) && !topLeft.white) {
                if (topLeft.rlocation != 0) {
                    list.add(new Move(this, topLeft));
                } else {
                    list.add(new Move(this, topLeft, 'q'));
                    list.add(new Move(this, topLeft, 'r'));
                    list.add(new Move(this, topLeft, 'b'));
                    list.add(new Move(this, topLeft, 'n'));
                }
            }
            if (topRight != null && !(topRight instanceof EmptySquare) && !topRight.white) {
                if (topRight.rlocation != 0) {
                    list.add(new Move(this, topRight));
                } else {
                    list.add(new Move(this, topRight, 'q'));
                    list.add(new Move(this, topRight, 'r'));
                    list.add(new Move(this, topRight, 'b'));
                    list.add(new Move(this, topRight, 'n'));
                }
            }
            Piece aheadByOne = Board.pieces[rlocation-1][clocation];
            if (this.rlocation == 6 && aheadByOne instanceof EmptySquare && Board.pieces[rlocation-2][clocation] instanceof EmptySquare)
                list.add(new Move(this, Board.pieces[rlocation-2][clocation]));
            if (aheadByOne instanceof EmptySquare) {
                if (aheadByOne.rlocation != 0) {
                    list.add(new Move(this, aheadByOne));
                } else {
                    list.add(new Move(this, aheadByOne, 'q'));
                    list.add(new Move(this, aheadByOne, 'r'));
                    list.add(new Move(this, aheadByOne, 'b'));
                    list.add(new Move(this, aheadByOne, 'n'));
                }

            }
        } else {
            //captures
            Piece bottomLeft = null;
            if (clocation != 0)
                bottomLeft = Board.pieces[rlocation+1][clocation-1];
            Piece bottomRight = null;
            if (clocation != 7)
                bottomRight = Board.pieces[rlocation+1][clocation+1];
            if (bottomLeft != null && !(bottomLeft instanceof EmptySquare) && bottomLeft.white) {
                if (bottomLeft.rlocation != 7) {
                    list.add(new Move(this, bottomLeft));
                } else {
                    list.add(new Move(this, bottomLeft, 'q'));
                    list.add(new Move(this, bottomLeft, 'r'));
                    list.add(new Move(this, bottomLeft, 'b'));
                    list.add(new Move(this, bottomLeft, 'n'));
                }
            }
            if (bottomRight != null && !(bottomRight instanceof EmptySquare) && bottomRight.white) {
                if (bottomRight.rlocation != 7) {
                    list.add(new Move(this, bottomRight));
                } else {
                    list.add(new Move(this, bottomRight, 'q'));
                    list.add(new Move(this, bottomRight, 'r'));
                    list.add(new Move(this, bottomRight, 'b'));
                    list.add(new Move(this, bottomRight, 'n'));
                }
            }
            Piece aheadByOne = Board.pieces[rlocation+1][clocation];
            if (this.rlocation == 1 && aheadByOne instanceof EmptySquare & Board.pieces[rlocation+2][clocation] instanceof EmptySquare)
                list.add(new Move(this, Board.pieces[rlocation+2][clocation]));
            if (aheadByOne instanceof EmptySquare) {
                if (aheadByOne.rlocation != 7) {
                    list.add(new Move(this, aheadByOne));
                } else {
                    list.add(new Move(this, aheadByOne, 'q'));
                    list.add(new Move(this, aheadByOne, 'r'));
                    list.add(new Move(this, aheadByOne, 'b'));
                    list.add(new Move(this, aheadByOne, 'n'));
                }
            }
        }

        //check test
        checkTest(list);
        return list;
    }
    @Override
    public ArrayList<Move> getAttackingMoves() {
        ArrayList<Move> list = new ArrayList<>();
        int offset = this.white ? -1 : 1;
        if (clocation != 0) {
            list.add(new Move(this, Board.pieces[rlocation + offset][clocation-1]));
        }
        if (clocation != 7) {
            list.add(new Move(this, Board.pieces[rlocation + offset][clocation+1]));
        }
        return list;
    }

    public Pawn clonePiece() {
        return new Pawn(this.white, this.rlocation, this.clocation);
    }
}
