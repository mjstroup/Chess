package src;
import java.util.*;
public class Piece {
    public char abbreviation;
    public String fileName;
    public boolean white;
    public int rlocation;
    public int clocation;
    public char promotion;
    public ArrayList<Move> getPossibleMoves(){return null;}
    public ArrayList<Move> getPinnedMoves(){return null;}
    public ArrayList<Move> getAttackingMoves(){return null;}
    public Piece clonePiece(){return null;}
    public static final String defaultFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public int getR() {
        return this.rlocation;
    }

    public int getC() {
        return this.clocation;
    }

    public void setLocation(int x, int y) {
        this.rlocation = x;
        this.clocation = y;
    }

    public boolean isAttackedByWhite() {
        for (int i = 0; i < Board.pieces.length; i++) {
            for (int j = 0; j < Board.pieces[0].length; j++) {
                if (!Board.pieces[i][j].white) continue;
                for (Move m : Board.pieces[i][j].getAttackingMoves()) {
                    if (m.endingPiece == this) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ArrayList<Piece> attackedByWhiteList() {
        ArrayList<Piece> list = new ArrayList<>();
        for (int i = 0; i < Board.pieces.length; i++) {
            for (int j = 0; j < Board.pieces[0].length; j++) {
                if (!Board.pieces[i][j].white) continue;
                for (Move m : Board.pieces[i][j].getAttackingMoves()) {
                    if (m.endingPiece == this) {
                        list.add(m.startingPiece);
                    }
                }
            }
        }
        return list;
    }

    public boolean isAttackedByBlack() {
        for (int i = 0; i < Board.pieces.length; i++) {
            for (int j = 0; j < Board.pieces[0].length; j++) {
                if (Board.pieces[i][j].white) continue;
                for (Move m : Board.pieces[i][j].getAttackingMoves()) {
                    if (m.endingPiece == this) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ArrayList<Piece> attackedByBlackList() {
        ArrayList<Piece> list = new ArrayList<>();
        for (int i = 0; i < Board.pieces.length; i++) {
            for (int j = 0; j < Board.pieces[0].length; j++) {
                if (Board.pieces[i][j].white) continue;
                for (Move m : Board.pieces[i][j].getAttackingMoves()) {
                    if (m.endingPiece == this) {
                        list.add(m.startingPiece);
                    }
                }
            }
        }
        return list;
    }

    public ArrayList<Move> getLegalMoves() {
        ArrayList<Move> list = new ArrayList<>();
        boolean singleCheck;
        boolean doubleCheck;
        ArrayList<Piece> piecesAttackingOurKing;
        if (this.white)
            piecesAttackingOurKing = Board.whiteKing.attackedByBlackList();
        else
            piecesAttackingOurKing = Board.blackKing.attackedByWhiteList();

        int attackers = piecesAttackingOurKing.size();
        switch (attackers) {
            case 0 -> {
                singleCheck = false;
                doubleCheck = false;
            }
            case 1 -> {
                singleCheck = true;
                doubleCheck = false;
            }
            default -> {
                singleCheck = false;
                doubleCheck = true;
            }
        }

        if (singleCheck) {
            Piece attacker = piecesAttackingOurKing.get(0);
            
            /**
             * We are in check, only way to get out is by
             * 1. Capture checked piece with a non-pinned piece
             * 2. Block the check (if check is a sliding piece, i.e., not a knight or pawn) by a non-pinned piece
             * 3. King moves to non-attacked square
             * 
             * NOTE: King cannot castle out of this position, so we don't need to calculate for it
             */
            
            //3. move king
            if (this instanceof King) {
                list = this.getPossibleMoves();
                return list;
            }
            //1. take piece
            if (this.isPinned() == null) {
                //EP take
                if ((this.clocation != 0 && Board.pieces[this.rlocation][this.clocation-1] == attacker || this.clocation != 7 && Board.pieces[this.rlocation][this.clocation+1] == attacker) && this.white && this instanceof Pawn && !attacker.white && attacker.rlocation != 0 && Board.pieces[attacker.rlocation-1][attacker.clocation] == Board.enPassantPiece && Board.enPassantPiece.enPassant) {
                    list.add(new Move(this, Board.enPassantPiece));
                }
                if ((this.clocation != 0 && Board.pieces[this.rlocation][this.clocation-1] == attacker || this.clocation != 7 && Board.pieces[this.rlocation][this.clocation+1] == attacker) && !this.white && this instanceof Pawn && attacker.white && attacker.rlocation != 7 && Board.pieces[attacker.rlocation+1][attacker.clocation] == Board.enPassantPiece && Board.enPassantPiece.enPassant) {

                    list.add(new Move(this, Board.enPassantPiece));
                }
                if (attacker.white && attacker.attackedByBlackList().contains(this)) {
                    for (Move m : this.getPossibleMoves()) {
                        if (m.endingPiece == attacker) {
                            list.add(m);
                            if (!(this instanceof Pawn))
                                break;
                        }
                    }
                } else if (!attacker.white && attacker.attackedByWhiteList().contains(this)) {
                    for (Move m : this.getPossibleMoves()) {
                        if (m.endingPiece == attacker) {
                            list.add(m);
                            if (!(this instanceof Pawn))
                                break;
                        }
                    }
                }
                //2. block the check
                if (!(attacker instanceof Knight || attacker instanceof Pawn)) {
                    ArrayList<Piece> blockSquares = new ArrayList<>();
                    King k = this.white ? Board.whiteKing : Board.blackKing;
                    int rdif = attacker.rlocation - k.rlocation;
                    int cdif = attacker.clocation - k.clocation;
                    if (rdif * cdif != 0 && Math.abs(rdif) != Math.abs(cdif))
                        return null;
                    int rdirection, cdirection;
                    if (rdif > 0) {
                        rdirection = 1;
                    } else if (rdif < 0) {
                        rdirection = -1;
                    } else {
                        rdirection = 0;
                    }
                    if (cdif > 0) {
                        cdirection = 1;
                    } else if (cdif < 0) {
                        cdirection = -1;
                    } else {
                        cdirection = 0;
                    }
                    for (int i = 1; i < 9; i++) {
                        Piece p = Board.pieces[attacker.rlocation+(rdirection*i*-1)][attacker.clocation+(cdirection*i*-1)];
                        if (p == k) {
                            break;
                        }
                        blockSquares.add(p);
                    }
                    for (Move m : this.getPossibleMoves()) {
                        if (blockSquares.contains(m.endingPiece))
                            list.add(m);
                    }
                }
            }
        } else if (doubleCheck) {
            /*
             * We are double checked, only way to get out is
             * 1. Move the king to non-attacking square.
             */
            if (this instanceof King) {
                list.addAll(this.getPossibleMoves());
            }
        } else {
            /*
             * We are not in check, we generate all possible moves
             */
            list.addAll(this.getPossibleMoves());
        }
        return list;
    }

    public Piece isPinned() {
        if (this == Board.blackKing || this == Board.whiteKing)
            return null;
        //get direction from piece to its king
        int rdif, cdif;
        if (this.white) {
            rdif = this.rlocation - Board.whiteKing.rlocation;
            cdif = this.clocation - Board.whiteKing.clocation;
        } else {
            rdif = this.rlocation - Board.blackKing.rlocation;
            cdif = this.clocation - Board.blackKing.clocation;
        }
        if (rdif * cdif != 0 && Math.abs(rdif) != Math.abs(cdif))
            return null;
        int rdirection, cdirection;
        if (rdif > 0) {
            rdirection = 1;
        } else if (rdif < 0) {
            rdirection = -1;
        } else {
            rdirection = 0;
        }
        if (cdif > 0) {
            cdirection = 1;
        } else if (cdif < 0) {
            cdirection = -1;
        } else {
            cdirection = 0;
        }
        //if theres a piece inbetween us and the king... we are not pinned
        for (int i = 1; i < 9; i++) {
            Piece p = Board.pieces[this.rlocation+(rdirection*i*-1)][this.clocation+(cdirection*i*-1)];
            if (p instanceof King && p.white == this.white) {
                break;
            }
            if (!(p instanceof EmptySquare)) {
                return null;
            }
        }
        //look for piece that will pin us
        for (int i = 1; i < 9; i++) {
            if (this.rlocation+(rdirection*i) < 0 || this.rlocation+(rdirection*i) > 7 || this.clocation+(cdirection*i) < 0 || this.clocation+(cdirection*i) > 7) {
                break;
            }
            Piece p = Board.pieces[this.rlocation+(rdirection*i)][this.clocation+(cdirection*i)];
            if (p instanceof Queen && p.white != this.white) {
                return p;
            }
            if (p instanceof Bishop && p.white != this.white && rdirection*cdirection != 0) {
                //ensure we are going diagonally
                return p;
            }
            if (p instanceof Rook && p.white != this.white && rdirection*cdirection == 0) {
                //ensure we are not going diagonally
                return p;
            }
            if (!(p instanceof EmptySquare))
                return null;
        }
        return null;
    }
}
