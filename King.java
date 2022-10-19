import java.util.*;
public class King extends Piece {
    public boolean queenCastleRights = true;
    public boolean kingCastleRights = true;
    public boolean inCheck = false;
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
        int row = this.white ? 7 : 0;
        if (this.kingCastleRights || this.queenCastleRights) {
            //castle king
            //ensure adjacent are empty
            if (Board.pieces[row][5] instanceof EmptySquare && Board.pieces[row][6] instanceof EmptySquare && this.kingCastleRights) {
                list.add(Board.pieces[row][6]);
            }
            //castle queen
            //ensure adjacent are empty
            if (Board.pieces[row][1] instanceof EmptySquare && Board.pieces[row][2] instanceof EmptySquare && Board.pieces[row][3] instanceof EmptySquare && this.queenCastleRights) {
                list.add(Board.pieces[row][2]);
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
