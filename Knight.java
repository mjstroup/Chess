import java.util.*;
public class Knight extends Piece {
    public Knight (Boolean white, int rlocation, int clocation) {
        this.rlocation = rlocation;
        this.clocation = clocation;
        this.white = white;
        this.abbreviation = 'N';
        fileName = this.white ? "./Images/wN.png" : "./Images/bN.png";
    }
    @Override
    public ArrayList<Piece> getPossibleMoves() {
        ArrayList<Piece> list = new ArrayList<>();
        if (Board.whiteTurn != this.white)
            return list;
        return list;
    }
}
