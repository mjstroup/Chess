import java.util.*;
public class King extends Piece {
    public boolean queenCastleRights = true;
    public boolean kingCastleRights = true;
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

        //white castle
        if (this.white && (this.kingCastleRights || this.queenCastleRights)) {
            //castle king
            //ensure f1 g1 are empty
            if (Board.pieces[7][5] instanceof EmptySquare && Board.pieces[7][6] instanceof EmptySquare && this.kingCastleRights) {
                list.add(Board.pieces[7][6]);
            }
            //castle queen
            //ensure b1 c1 d1 are empty
            if (Board.pieces[7][1] instanceof EmptySquare && Board.pieces[7][2] instanceof EmptySquare && Board.pieces[7][3] instanceof EmptySquare && this.queenCastleRights) {
                list.add(Board.pieces[7][2]);
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
        return list;
    }
}
