package src.Pieces;
import java.util.*;

import src.Game.Board;
import src.Game.Move;
public class Knight extends Piece {
    public Knight (Boolean white, int rlocation, int clocation) {
        this.rlocation = rlocation;
        this.clocation = clocation;
        this.white = white;
        this.abbreviation = 'n';
        this.value = 320;
        this.fileName = this.white ? "Images/wN.png" : "Images/bN.png";
        if (this.white) {
            this.mapping = new int[][]{
                {-50,-40,-30,-30,-30,-30,-40,-50},
                {-40,-20,  0,  0,  0,  0,-20,-40},
                {-30,  0, 10, 15, 15, 10,  0,-30},
                {-30,  5, 15, 20, 20, 15,  5,-30},
                {-30,  0, 15, 20, 20, 15,  0,-30},
                {-30,  5, 10, 15, 15, 10,  5,-30},
                {-40,-20,  0,  5,  5,  0,-20,-40},
                {-50,-40,-30,-30,-30,-30,-40,-50}
            };
        } else {
            this.mapping = new int[][]{
                {-50,-40,-30,-30,-30,-30,-40,-50},
                {-40,-20,  0,  5,  5,  0,-20,-40},
                {-30,  5, 10, 15, 15, 10,  5,-30},
                {-30,  0, 15, 20, 20, 15,  0,-30},
                {-30,  5, 15, 20, 20, 15,  5,-30},
                {-30,  0, 10, 15, 15, 10,  0,-30},
                {-40,-20,  0,  0,  0,  0,-20,-40},
                {-50,-40,-30,-30,-30,-30,-40,-50}
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
        ArrayList<Move> list = new ArrayList<>();
        return list;
    }

    @Override
    public ArrayList<Move> getAttackingMoves() {
        ArrayList<Move> list = new ArrayList<>();
        //counter clockwise starting at 0 degrees
        int r = rlocation-1;
        int c = clocation+2;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7)
            list.add(new Move(this, Board.pieces[r][c]));
        r = rlocation-2;
        c = clocation+1;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7)
            list.add(new Move(this, Board.pieces[r][c]));
        r = rlocation-2;
        c = clocation-1;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7)
            list.add(new Move(this, Board.pieces[r][c]));
        r = rlocation-1;
        c = clocation-2;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7)
            list.add(new Move(this, Board.pieces[r][c]));
        r = rlocation+1;
        c = clocation-2;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7)
            list.add(new Move(this, Board.pieces[r][c]));
        r = rlocation+2;
        c = clocation-1;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7)
            list.add(new Move(this, Board.pieces[r][c]));
        r = rlocation+2;
        c = clocation+1;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7)
            list.add(new Move(this, Board.pieces[r][c]));
        r = rlocation+1;
        c = clocation+2;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7)
            list.add(new Move(this, Board.pieces[r][c]));
        return list;
    }

    public Knight clonePiece() {
        return new Knight(this.white, this.rlocation, this.clocation);
    }
}
