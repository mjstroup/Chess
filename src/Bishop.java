package src;
import java.util.*;
public class Bishop extends Piece {
    public Bishop(Boolean white, int rlocation, int clocation) {
        this.rlocation = rlocation;
        this.clocation = clocation;
        this.white = white;
        this.abbreviation = 'b';
        fileName = this.white ? "./Images/wB.png" : "./Images/bB.png";
    }

    @Override
    public ArrayList<Move> getPossibleMoves() {
        ArrayList<Move> list = new ArrayList<>();
        if (Board.whiteTurn != this.white)
            return list;
        if (this.isPinned() != null) {
            return this.getPinnedMoves();
        } else {
            list.addAll(this.getAttackingMoves());
            list.removeIf(p -> (!(p.endingPiece instanceof EmptySquare) && p.endingPiece.white == this.white));
        }
        return list;
    }

    @Override
    public ArrayList<Move> getPinnedMoves() {
        ArrayList<Move> list = new ArrayList<>();
        Piece attacker = this.isPinned();
        int rdif = attacker.rlocation - this.rlocation;
        int cdif = attacker.clocation - this.clocation;
        if (rdif * cdif != 0) {
            //diagonal
            boolean topLeft = true, topRight = true, bottomLeft = true, bottomRight = true;
            if (rdif *cdif > 0) {
                bottomLeft = false;
                topRight = false;
            } else {
                bottomRight = false;
                topLeft = false;
            }
            for (int i = 1; i < 8; i++) {
                if (!(topLeft || topRight || bottomLeft || bottomRight)) break;
                if (topLeft) {
                    Piece p = Board.pieces[rlocation-i][clocation-i];
                    if (p instanceof EmptySquare) {
                        list.add(new Move(this, p));
                    } else if (p instanceof King && p.white == this.white) {
                        topLeft = false;
                    } else if (p == attacker) {
                        list.add(new Move(this, p));
                        topLeft = false;
                    }
                }
                if (topRight) {
                    Piece p = Board.pieces[rlocation-i][clocation+i];
                    if (p instanceof EmptySquare) {
                        list.add(new Move(this, p));
                    } else if (p instanceof King && p.white == this.white) {
                        topRight = false;
                    } else if (p == attacker) {
                        list.add(new Move(this, p));
                        topRight = false;
                    }
                }
                if (bottomLeft) {
                    Piece p = Board.pieces[rlocation+i][clocation-i];
                    if (p instanceof EmptySquare) {
                        list.add(new Move(this, p));
                    } else if (p instanceof King && p.white == this.white) {
                        bottomLeft = false;
                    } else if (p == attacker) {
                        list.add(new Move(this, p));
                        bottomLeft = false;
                    }
                }
                if (bottomRight) {
                    Piece p = Board.pieces[rlocation+i][clocation+i];
                    if (p instanceof EmptySquare) {
                        list.add(new Move(this, p));
                    } else if (p instanceof King && p.white == this.white) {
                        bottomRight = false;
                    } else if (p == attacker) {
                        list.add(new Move(this, p));
                        bottomRight = false;
                    }
                }
            }   
        }
        return list;
    }

    @Override
    public ArrayList<Move> getAttackingMoves() {
        ArrayList<Move> list = new ArrayList<>();
        boolean topLeft = true, topRight = true, bottomLeft = true, bottomRight = true;
        for (int i = 1; i < 8; i++) {
            if (!(topLeft || topRight || bottomLeft || bottomRight)) break;
            if (topLeft) {
                Piece p = null;
                if (rlocation-i >= 0 && rlocation-i <= 7 && clocation-i >= 0 && clocation-i <= 7)
                    p = Board.pieces[rlocation-i][clocation-i];
                if (!(rlocation-i >= 0 && rlocation-i <= 7 && clocation-i >= 0 && clocation-i <= 7)) {
                    topLeft = false;
                } else if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else if (p instanceof King && p.white != this.white) {
                    list.add(new Move(this, p));
                } else {
                    list.add(new Move(this, p));
                    topLeft = false;
                }
            }
            if (topRight) {
                Piece p = null;
                if (rlocation-i >= 0 && rlocation-i <= 7 && clocation+i >= 0 && clocation+i <= 7)
                    p = Board.pieces[rlocation-i][clocation+i];
                if (!(rlocation-i >= 0 && rlocation-i <= 7 && clocation+i >= 0 && clocation+i <= 7)) {
                    topRight = false;
                } else if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else if (p instanceof King && p.white != this.white) {
                    list.add(new Move(this, p));
                } else {
                    list.add(new Move(this, p));
                    topRight = false;
                }
            }
            if (bottomLeft) {
                Piece p = null;
                if (rlocation+i >= 0 && rlocation+i <= 7 && clocation-i >= 0 && clocation-i <= 7)
                    p = Board.pieces[rlocation+i][clocation-i];
                if (!(rlocation+i >= 0 && rlocation+i <= 7 && clocation-i >= 0 && clocation-i <= 7)) {
                    bottomLeft = false;
                } else if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else if (p instanceof King && p.white != this.white) {
                    list.add(new Move(this, p));
                } else {
                    list.add(new Move(this, p));
                    bottomLeft = false;
                }
            }
            if (bottomRight) {
                Piece p = null;
                if (rlocation+i >= 0 && rlocation+i <= 7 && clocation+i >= 0 && clocation+i <= 7)
                    p = Board.pieces[rlocation+i][clocation+i];
                if (!(rlocation+i >= 0 && rlocation+i <= 7 && clocation+i >= 0 && clocation+i <= 7)) {
                    bottomRight = false;
                } else if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else if (p instanceof King && p.white != this.white) {
                    list.add(new Move(this, p));
                } else {
                    list.add(new Move(this, p));
                    bottomRight = false;
                }
            }
        }   
        return list;
    }
    
    public Bishop clonePiece() {
        return new Bishop(this.white, this.rlocation, this.clocation);
    }
}