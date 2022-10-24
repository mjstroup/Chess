package src;
import java.util.*;
public class Rook extends Piece {
    public Rook (Boolean white, int rlocation, int clocation) {
        this.rlocation = rlocation;
        this.clocation = clocation;
        this.white = white;
        this.abbreviation = 'r';
        fileName = this.white ? "./Images/wR.png" : "./Images/bR.png";
    }
    @Override
    public ArrayList<Move> getPossibleMoves() {
        ArrayList<Move> list = new ArrayList<>();
        if (Board.whiteTurn != this.white)
            return list;
        list.addAll(this.getAttackingMoves());
        list.removeIf(p -> (!(p.endingPiece instanceof EmptySquare) && p.endingPiece.white == this.white));
        //check test
        checkTest(list);
        return list;
    }
    @Override
    public ArrayList<Move> getAttackingMoves() {
        ArrayList<Move> list = new ArrayList<>();
        boolean left = true, up = true, right = true, down = true;
        for (int i = 1; i < 8; i++) {
            if (!(left || up || right || down)) break;
            if (left) {
                Piece p = null;
                if (clocation-i <= 7 && clocation-i >= 0) 
                    p = Board.pieces[rlocation][clocation-i];
                if (!(clocation-i <= 7 && clocation-i >= 0)) {
                    left = false;
                } else if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else {
                    list.add(new Move(this, p));
                    left = false;
                }
            }
            if (up) {
                Piece p = null;
                if (rlocation-i <= 7 && rlocation-i >= 0) 
                    p = Board.pieces[rlocation-i][clocation];
                if (!(rlocation-i <= 7 && rlocation-i >= 0)) {
                    up = false;
                } else if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else {
                    list.add(new Move(this, p));
                    up = false;
                }
            }
            if (right) {
                Piece p = null;
                if (clocation+i <= 7 && clocation+i >= 0) 
                    p = Board.pieces[rlocation][clocation+i];
                if (!(clocation+i <= 7 && clocation+i >= 0)) {
                    right = false;
                } else if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else {
                    list.add(new Move(this, p));
                    right = false;
                }
            }
            if (down) {
                Piece p = null;
                if (rlocation+i <= 7 && rlocation+i >= 0) 
                    p = Board.pieces[rlocation+i][clocation];
                if (!(rlocation+i <= 7 && rlocation+i >= 0)) {
                    down = false;
                } else if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else {
                    list.add(new Move(this, p));
                    down = false;
                }
            }
        }
        return list;
    }

    public Rook clonePiece() {
        return new Rook(this.white, this.rlocation, this.clocation);
    }
}
