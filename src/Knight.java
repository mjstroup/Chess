package src;
import java.util.*;
public class Knight extends Piece {
    public Knight (Boolean white, int rlocation, int clocation) {
        this.rlocation = rlocation;
        this.clocation = clocation;
        this.white = white;
        this.abbreviation = 'N';
        fileName = this.white ? "./Images/wN.png" : "./Images/bN.png";
    }
    @Override
    public ArrayList<Piece> getPossibleMoves() {
        ArrayList<Piece> list = new ArrayList<>();
        if (Board.whiteTurn != this.white)
            return list;
        list.addAll(this.getAttackingMoves());
        list.removeIf(p -> (!(p instanceof EmptySquare) && p.white == this.white));
        //check test
        checkTest(list);
        return list;
    }
    @Override
    public ArrayList<Piece> getAttackingMoves() {
        ArrayList<Piece> list = new ArrayList<>();
        //counter clockwise starting at 0 degrees
        int r = rlocation-1;
        int c = clocation+2;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7)
            list.add(Board.pieces[r][c]);
        r = rlocation-2;
        c = clocation+1;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7)
            list.add(Board.pieces[r][c]);
        r = rlocation-2;
        c = clocation-1;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7)
            list.add(Board.pieces[r][c]);
        r = rlocation-1;
        c = clocation-2;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7)
            list.add(Board.pieces[r][c]);
        r = rlocation+1;
        c = clocation-2;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7)
            list.add(Board.pieces[r][c]);
        r = rlocation+2;
        c = clocation-1;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7)
            list.add(Board.pieces[r][c]);
        r = rlocation+2;
        c = clocation+1;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7)
            list.add(Board.pieces[r][c]);
        r = rlocation+1;
        c = clocation+2;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7)
            list.add(Board.pieces[r][c]);
        return list;
    }
}
