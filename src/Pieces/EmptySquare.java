package src.Pieces;
import java.util.*;

import src.Game.Move;
public class EmptySquare extends Piece {
    public boolean enPassant = false;
    public EmptySquare(int rlocation, int clocation) {
        this.rlocation = rlocation;
        this.clocation = clocation;
        this.abbreviation = 'E';
        this.value = 0;
    }
    @Override
    public ArrayList<Move> getPossibleMoves() {
        ArrayList<Move> list = new ArrayList<>();
        return list;
    }
    @Override
    public ArrayList<Move> getAttackingMoves() {
        ArrayList<Move> list = new ArrayList<>();
        return list;
    }
    public EmptySquare clonePiece() {
        EmptySquare es = new EmptySquare(this.rlocation, this.clocation);
        es.enPassant = this.enPassant;
        return es;
    }
}
