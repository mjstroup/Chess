import java.util.*;
public class Rook extends Piece {
    public Rook (Boolean white, int rlocation, int clocation) {
        this.rlocation = rlocation;
        this.clocation = clocation;
        this.white = white;
        this.abbreviation = 'R';
        fileName = this.white ? "./Images/wR.png" : "./Images/bR.png";
    }
    @Override
    public ArrayList<Piece> getPossibleMoves() {
        //TODO: remove castle rights when rook moves
        ArrayList<Piece> list = new ArrayList<>();
        if (Board.whiteTurn != this.white)
            return list;
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
                    list.add(p);
                } else if (p.white != this.white) {
                    list.add(p);
                    left = false;
                } else if (p.white == this.white) {
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
                    list.add(p);
                } else if (p.white != this.white) {
                    list.add(p);
                    up = false;
                } else if (p.white == this.white) {
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
                    list.add(p);
                } else if (p.white != this.white) {
                    list.add(p);
                    right = false;
                } else if (p.white == this.white) {
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
                    list.add(p);
                } else if (p.white != this.white) {
                    list.add(p);
                    down = false;
                } else if (p.white == this.white) {
                    down = false;
                }
            }
        }
        return list;
    }
}
