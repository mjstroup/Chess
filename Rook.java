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
        ArrayList<Piece> list = new ArrayList<>();
        if (Board.whiteTurn != this.white)
            return list;
        return list;
    }
}
