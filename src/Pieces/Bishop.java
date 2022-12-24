package src.Pieces;
import java.util.*;

import src.Game.Board;
import src.Game.Move;
public class Bishop extends Piece {
    public Bishop(Boolean white, int rlocation, int clocation) {
        this.rlocation = rlocation;
        this.clocation = clocation;
        this.white = white;
        this.abbreviation = 'b';
        if (white)
            this.value = 3.2;
        else
            this.value = -3.2;
        fileName = this.white ? "/Users/matthewstroup/Desktop/CS/PROJECTS/Chess/Images/wB.png" : "/Users/matthewstroup/Desktop/CS/PROJECTS/Chess/Images/bB.png";
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
    
    public Bishop clonePiece() {
        return new Bishop(this.white, this.rlocation, this.clocation);
    }
}