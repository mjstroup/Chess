import java.util.*;
public class King extends Piece {
    public boolean queenCastleRights = false;
    public boolean kingCastleRights = false;
    public King (Boolean white, int rlocation, int clocation) {
        this.rlocation = rlocation;
        this.clocation = clocation;
        this.white = white;
        this.abbreviation = 'K';
        fileName = this.white ? "./Images/wK.png" : "./Images/bK.png";
    }
    @Override
    public ArrayList<Piece> getPossibleMoves() {
        ArrayList<Piece> list = new ArrayList<>();
        if (Board.whiteTurn != this.white)
            return list;

        //castle
        if (this.kingCastleRights || this.queenCastleRights) {
            //castle king
            //ensure adjacent are empty
            if (this.white) {
                Piece a1 = Board.pieces[7][0];
                Piece b1 = Board.pieces[7][1];
                Piece c1 = Board.pieces[7][2];
                Piece d1 = Board.pieces[7][3];
                Piece f1 = Board.pieces[7][5];
                Piece g1 = Board.pieces[7][6];
                Piece h1 = Board.pieces[7][7];
                if (d1 instanceof EmptySquare && c1 instanceof EmptySquare && b1 instanceof EmptySquare && a1 instanceof Rook && a1.white && !this.isAttackedByBlack() && !d1.isAttackedByBlack() && !c1.isAttackedByBlack() && !b1.isAttackedByBlack() && this.queenCastleRights) {
                    list.add(c1);
                }
                if (f1 instanceof EmptySquare && g1 instanceof EmptySquare && h1 instanceof Rook && h1.white && !this.isAttackedByBlack() && !f1.isAttackedByBlack() && !g1.isAttackedByBlack() && this.kingCastleRights) {
                    list.add(g1);
                }
            } else {
                Piece a8 = Board.pieces[0][0];
                Piece b8 = Board.pieces[0][1];
                Piece c8 = Board.pieces[0][2];
                Piece d8 = Board.pieces[0][3];
                Piece f8 = Board.pieces[0][5];
                Piece g8 = Board.pieces[0][6];
                Piece h8 = Board.pieces[0][7];
                if (d8 instanceof EmptySquare && c8 instanceof EmptySquare && b8 instanceof EmptySquare && a8 instanceof Rook && !a8.white && !this.isAttackedByWhite() && !d8.isAttackedByWhite() && !c8.isAttackedByWhite() && !b8.isAttackedByWhite() && this.queenCastleRights) {
                    list.add(c8);
                }
                if (f8 instanceof EmptySquare && g8 instanceof EmptySquare && h8 instanceof Rook && !h8.white && !this.isAttackedByWhite() && !f8.isAttackedByWhite() && !g8.isAttackedByWhite() && this.kingCastleRights) {
                    list.add(g8);
                }
            }
        }
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                int r = rlocation + i;
                int c = clocation + j;
                if (r <= 7 && r >= 0 && c <= 7 && c >= 0 && (Board.pieces[r][c] instanceof EmptySquare || Board.pieces[r][c].white != this.white))
                    list.add(Board.pieces[r][c]);
            }
        }
        
        //check test
        checkTest(list);
        return list;
    }
    public void removeQueenCastleRights() {
        this.queenCastleRights = false;
    }
    public void removeKingCastleRights() {
        this.kingCastleRights = false;
    }
    @Override
    public ArrayList<Piece> getAttackingMoves() {
        ArrayList<Piece> list = new ArrayList<>();
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                int r = rlocation + i;
                int c = clocation + j;
                if (r <= 7 && r >= 0 && c <= 7 && c >= 0)
                    list.add(Board.pieces[r][c]);
            }
        }
        return list;
    }
}
