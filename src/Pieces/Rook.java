package src.Pieces;
import java.util.*;

import src.Game.Board;
import src.Game.Move;
public class Rook extends Piece {
    public Rook (Boolean white, int rlocation, int clocation) {
        this.rlocation = rlocation;
        this.clocation = clocation;
        this.white = white;
        this.abbreviation = 'r';
        this.value = 500;
        this.fileName = this.white ? "Images/wR.png" : "Images/bR.png";
        if (this.white) {
            this.mapping = new int[][]{
                { 0,  0,  0,  0,  0,  0,  0,  0},
                { 5, 10, 10, 10, 10, 10, 10,  5},
                {-5,  0,  0,  0,  0,  0,  0, -5},
                {-5,  0,  0,  0,  0,  0,  0, -5},
                {-5,  0,  0,  0,  0,  0,  0, -5},
                {-5,  0,  0,  0,  0,  0,  0, -5},
                {-5,  0,  0,  0,  0,  0,  0, -5},
                { 0,  0,  0,  5,  5,  0,  0,  0}
            };
        } else {
            this.mapping = new int[][]{
                { 0,  0,  0,  5,  5,  0,  0,  0},
                {-5,  0,  0,  0,  0,  0,  0, -5},
                {-5,  0,  0,  0,  0,  0,  0, -5},
                {-5,  0,  0,  0,  0,  0,  0, -5},
                {-5,  0,  0,  0,  0,  0,  0, -5},
                {-5,  0,  0,  0,  0,  0,  0, -5},
                { 5, 10, 10, 10, 10, 10, 10,  5},
                { 0,  0,  0,  0,  0,  0,  0,  0},
            };
        }
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

    public Rook clonePiece() {
        return new Rook(this.white, this.rlocation, this.clocation);
    }
}
