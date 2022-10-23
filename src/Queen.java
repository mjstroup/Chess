package src;
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
    public ArrayList<Move> getPossibleMoves() {
        ArrayList<Move> list = new ArrayList<>();
        if (Board.whiteTurn != this.white)
            return list;
        list.addAll(this.getAttackingMoves());
        list.removeIf(p -> (!(p.endingPiece instanceof EmptySquare) && p.endingPiece.white == this.white));

        //check test
        checkTest(list);
        return list;
    }
    @Override
    public ArrayList<Move> getAttackingMoves() {
        ArrayList<Move> list = new ArrayList<>();
        ArrayList<Move> tempList = new Rook(this.white, this.getR(), this.getC()).getAttackingMoves();
        tempList.addAll(new Bishop(this.white, this.getR(), this.getC()).getAttackingMoves());
        for (Move m : tempList) {
            m.startingPiece = this;
        }
        list.addAll(tempList);
        return list;
    }

    public Queen clonePiece() {
        return new Queen(this.white, this.rlocation, this.clocation);
    }
}
