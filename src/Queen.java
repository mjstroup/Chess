package src;
import java.util.*;
public class Queen extends Piece {
    public Queen (Boolean white, int rlocation, int clocation) {
        this.rlocation = rlocation;
        this.clocation = clocation;
        this.white = white;
        this.abbreviation = 'q';
        fileName = this.white ? "./Images/wQ.png" : "./Images/bQ.png";
    }

    @Override
    public ArrayList<Move> getPossibleMoves() {
        ArrayList<Move> list = new ArrayList<>();
        if (Board.whiteTurn != this.white)
            return list;
        if (this.isPinned() != null) {
            return this.getPinnedMoves();
        } else {
            list.addAll(this.getAttackingMoves());
            list.removeIf(p -> (!(p.endingPiece instanceof EmptySquare) && p.endingPiece.white == this.white));
        }
        return list;
    }

    @Override
    public ArrayList<Move> getPinnedMoves() {
        ArrayList<Move> list = new Rook(this.white, this.rlocation, this.clocation).getPinnedMoves();
        list.addAll(new Bishop(this.white, this.rlocation, this.clocation).getPinnedMoves());
        for (Move m : list) {
            m.startingPiece = this;
        }
        return list;
    }

    @Override
    public ArrayList<Move> getAttackingMoves() {
        ArrayList<Move> list = new Rook(this.white, this.rlocation, this.clocation).getAttackingMoves();
        list.addAll(new Bishop(this.white, this.rlocation, this.clocation).getAttackingMoves());
        for (Move m : list) {
            m.startingPiece = this;
        }
        return list;
    }

    public Queen clonePiece() {
        return new Queen(this.white, this.rlocation, this.clocation);
    }
}
