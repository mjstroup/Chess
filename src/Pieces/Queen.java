package src.Pieces;
import java.util.*;

import src.Game.Board;
import src.Game.Move;
public class Queen extends Piece {
    public Queen (Boolean white, int rlocation, int clocation) {
        this.rlocation = rlocation;
        this.clocation = clocation;
        this.white = white;
        this.abbreviation = 'q';
        if (white)
            this.value = 9;
        else
            this.value = -9;
        fileName = this.white ? "Images/wQ.png" : "Images/bQ.png";
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
        return getSlidingPinnedMoves();
    }

    @Override
    public ArrayList<Move> getAttackingMoves() {
        return getSlidingAttackingMoves();
    }

    public Queen clonePiece() {
        return new Queen(this.white, this.rlocation, this.clocation);
    }
}
