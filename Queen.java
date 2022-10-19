import java.util.*;
public class Queen extends Piece {
    public Queen (Boolean white, int rlocation, int clocation) {
        this.rlocation = rlocation;
        this.clocation = clocation;
        this.white = white;
        this.abbreviation = 'Q';
        fileName = this.white ? "./Images/wQ.png" : "./Images/bQ.png";
    }
    @Override
    public ArrayList<Piece> getPossibleMoves() {
        ArrayList<Piece> list = new ArrayList<>();
        if (Board.whiteTurn != this.white)
            return list;
        list.addAll(new Rook(this.white, this.getR(), this.getC()).getPossibleMoves());
        list.addAll(new Bishop(this.white, this.getR(), this.getC()).getPossibleMoves());
        return list;
    }
}
