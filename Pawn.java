import java.util.*;
public class Pawn extends Piece {
    public Pawn (Boolean white, int rlocation, int clocation) {
        this.rlocation = rlocation;
        this.clocation = clocation;
        this.white = white;
        this.abbreviation = 'P';
        fileName = this.white ? "./Images/wP.png" : "./Images/bP.png";
    }
    @Override
    public ArrayList<Piece> getPossibleMoves() {
        ArrayList<Piece> list = new ArrayList<>();
        if (Board.whiteTurn != this.white)
            return list;
        //TODO: en passant 
        if (this.white) {
            //captures
            Piece topLeft = null;
            if (clocation != 0) 
                topLeft = Board.pieces[rlocation-1][clocation-1];
            Piece topRight = null;
            if (clocation != 7) 
                topRight = Board.pieces[rlocation-1][clocation+1];
            if (topLeft != null && !(topLeft instanceof EmptySquare) && !topLeft.white)
                list.add(topLeft);
            if (topRight != null && !(topRight instanceof EmptySquare) && !topRight.white) 
                list.add(topRight);
            Piece aheadByOne = Board.pieces[rlocation-1][clocation];
            if (this.rlocation == 6 && aheadByOne instanceof EmptySquare && Board.pieces[rlocation-2][clocation] instanceof EmptySquare)
                list.add(Board.pieces[rlocation-2][clocation]);
            if (aheadByOne instanceof EmptySquare)
                list.add(aheadByOne);
        } else {
            //captures
            Piece bottomLeft = null;
            if (clocation != 0)
                bottomLeft = Board.pieces[rlocation+1][clocation-1];
            Piece bottomRight = null;
            if (clocation != 7)
                bottomRight = Board.pieces[rlocation+1][clocation+1];
            if (bottomLeft != null && !(bottomLeft instanceof EmptySquare) && bottomLeft.white) 
                list.add(bottomLeft);
            if (bottomRight != null && !(bottomRight instanceof EmptySquare) && bottomRight.white)
                list.add(bottomRight);
            Piece aheadByOne = Board.pieces[rlocation+1][clocation];
            if (this.rlocation == 1 && aheadByOne instanceof EmptySquare & Board.pieces[rlocation+2][clocation] instanceof EmptySquare)
                list.add(Board.pieces[rlocation+2][clocation]);
            if (aheadByOne instanceof EmptySquare)
                list.add(aheadByOne);
        }
        return list;
    }
}
