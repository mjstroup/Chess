package src.Pieces;
import java.util.*;

import src.Game.Board;
import src.Game.Move;
public class King extends Piece {
    public boolean queenCastleRights = false;
    public boolean kingCastleRights = false;
    public static final int[][] endMapping = new int[][]{
        {-50,-40,-30,-20,-20,-30,-40,-50},
        {-30,-20,-10,  0,  0,-10,-20,-30},
        {-30,-10, 20, 30, 30, 20,-10,-30},
        {-30,-10, 30, 40, 40, 30,-10,-30},
        {-30,-10, 30, 40, 40, 30,-10,-30},
        {-30,-10, 20, 30, 30, 20,-10,-30},
        {-30,-30,  0,  0,  0,  0,-30,-30},
        {-50,-30,-30,-30,-30,-30,-30,-50}
    };
    
    public King (Boolean white, int rlocation, int clocation) {
        this.rlocation = rlocation;
        this.clocation = clocation;
        this.white = white;
        this.abbreviation = 'k';
        this.value = 0;
        this.fileName = this.white ? "Images/wK.png" : "Images/bK.png";
        if (this.white) {
            this.mapping = new int[][]{
                {-30,-40,-40,-50,-50,-40,-40,-30},
                {-30,-40,-40,-50,-50,-40,-40,-30},
                {-30,-40,-40,-50,-50,-40,-40,-30},
                {-30,-40,-40,-50,-50,-40,-40,-30},
                {-20,-30,-30,-40,-40,-30,-30,-20},
                {-10,-20,-20,-20,-20,-20,-20,-10},
                { 20, 20,  0,  0,  0,  0, 20, 20},
                { 20, 30, 10,  0,  0, 10, 30, 20}
            };
        } else {
            this.mapping = new int[][]{
                { 20, 30, 10,  0,  0, 10, 30, 20},
                { 20, 20,  0,  0,  0,  0, 20, 20},
                {-10,-20,-20,-20,-20,-20,-20,-10},
                {-20,-30,-30,-40,-40,-30,-30,-20},
                {-30,-40,-40,-50,-50,-40,-40,-30},
                {-30,-40,-40,-50,-50,-40,-40,-30},
                {-30,-40,-40,-50,-50,-40,-40,-30},
                {-30,-40,-40,-50,-50,-40,-40,-30},
            };
        }
    }

    @Override
    public ArrayList<Move> getPossibleMoves() {
        ArrayList<Move> list = new ArrayList<>();
        if (Board.whiteTurn != this.white)
            return list;

        //castle
        if (this.kingCastleRights || this.queenCastleRights) {
            //castle king
            //ensure adjacent are empty
            if (this.white) {
                Piece a1 = Board.pieces[7][0];
                Piece b1 = Board.pieces[7][1];
                Piece c1 = Board.pieces[7][2];
                Piece d1 = Board.pieces[7][3];
                Piece f1 = Board.pieces[7][5];
                Piece g1 = Board.pieces[7][6];
                Piece h1 = Board.pieces[7][7];
                if (d1 instanceof EmptySquare && c1 instanceof EmptySquare && b1 instanceof EmptySquare && a1 instanceof Rook && a1.white && !this.isAttackedByBlack() && !d1.isAttackedByBlack() && !c1.isAttackedByBlack() && this.queenCastleRights) {
                    list.add(new Move(this, c1));
                }
                if (f1 instanceof EmptySquare && g1 instanceof EmptySquare && h1 instanceof Rook && h1.white && !this.isAttackedByBlack() && !f1.isAttackedByBlack() && !g1.isAttackedByBlack() && this.kingCastleRights) {
                    list.add(new Move(this, g1));
                }
            } else {
                Piece a8 = Board.pieces[0][0];
                Piece b8 = Board.pieces[0][1];
                Piece c8 = Board.pieces[0][2];
                Piece d8 = Board.pieces[0][3];
                Piece f8 = Board.pieces[0][5];
                Piece g8 = Board.pieces[0][6];
                Piece h8 = Board.pieces[0][7];
                if (d8 instanceof EmptySquare && c8 instanceof EmptySquare && b8 instanceof EmptySquare && a8 instanceof Rook && !a8.white && !this.isAttackedByWhite() && !d8.isAttackedByWhite() && !c8.isAttackedByWhite() && this.queenCastleRights) {
                    list.add(new Move(this, c8));
                }
                if (f8 instanceof EmptySquare && g8 instanceof EmptySquare && h8 instanceof Rook && !h8.white && !this.isAttackedByWhite() && !f8.isAttackedByWhite() && !g8.isAttackedByWhite() && this.kingCastleRights) {
                    list.add(new Move(this, g8));
                }
            }
        }
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (i == 0 && j == 0) continue;
                int r = rlocation + i;
                int c = clocation + j;
                if (r <= 7 && r >= 0 && c <= 7 && c >= 0 && (Board.pieces[r][c] instanceof EmptySquare || Board.pieces[r][c].white != this.white) && ((this.white && !Board.pieces[r][c].isAttackedByBlack()) || (!this.white && !Board.pieces[r][c].isAttackedByWhite())))
                    list.add(new Move(this, Board.pieces[r][c])); 
            }
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
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                int r = rlocation + i;
                int c = clocation + j;
                if (r <= 7 && r >= 0 && c <= 7 && c >= 0)
                    list.add(new Move(this, Board.pieces[r][c]));
            }
        }
        return list;
    }

    public void removeQueenCastleRights() {
        this.queenCastleRights = false;
    }
    public void removeKingCastleRights() {
        this.kingCastleRights = false;
    }
    
    public King clonePiece() {
        return new King(this.white, this.rlocation, this.clocation);
    }
}
