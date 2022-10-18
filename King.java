import java.util.*;
public class King extends Piece {
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
        return list;
    }
}
