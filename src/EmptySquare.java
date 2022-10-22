package src;
import java.util.*;
public class EmptySquare extends Piece {
    public EmptySquare(int rlocation, int clocation) {
        this.rlocation = rlocation;
        this.clocation = clocation;
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
}
