package src;
import java.util.*;
public class EmptySquare extends Piece {
    public boolean enPassant = false;
    public EmptySquare(int rlocation, int clocation) {
        this.rlocation = rlocation;
        this.clocation = clocation;
        this.abbreviation = 'E';
    }
    @Override
    public ArrayList<Piece> getPossibleMoves() {
        ArrayList<Piece> list = new ArrayList<>();
        return list;
    }
    @Override
    public ArrayList<Piece> getAttackingMoves() {
        ArrayList<Piece> list = new ArrayList<>();
        return list;
    }
    public EmptySquare clonePiece() {
        return new EmptySquare(this.rlocation, this.clocation);
    }
}
