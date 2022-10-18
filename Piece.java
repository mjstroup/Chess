import java.util.*;
public class Piece {
    public char abbreviation;
    public String fileName;
    public boolean white;
    public int rlocation;
    public int clocation;
    public ArrayList<Piece> getPossibleMoves(){return null;}
    public static final Piece[][] newBoard = new Piece[][]{{new Rook(false, 0, 0), new Knight(false, 0, 1), new Bishop(false, 0, 2), new Queen(false, 0, 3), new King(false, 0, 4), new Bishop(false, 0, 5), new Knight(false, 0, 6), new Rook(false, 0, 7)},
                                                           {new Pawn(false, 1, 0), new Pawn(false, 1, 1), new Pawn(false, 1, 2), new Pawn(false, 1, 3), new Pawn(false, 1, 4), new Pawn(false, 1, 5), new Pawn(false, 1, 6), new Pawn(false, 1, 7)},
                                                           {new EmptySquare(2, 0), new EmptySquare(2, 1), new EmptySquare(2, 2), new EmptySquare(2, 3), new EmptySquare(2, 4), new EmptySquare(2, 5), new EmptySquare(2, 6), new EmptySquare(2, 7)},
                                                           {new EmptySquare(3, 0), new EmptySquare(3, 1), new EmptySquare(3, 2), new EmptySquare(3, 3), new EmptySquare(3, 4), new EmptySquare(3, 5), new EmptySquare(3, 6), new EmptySquare(3, 7)},
                                                           {new EmptySquare(4, 0), new EmptySquare(4, 1), new EmptySquare(4, 2), new EmptySquare(4, 3), new EmptySquare(4, 4), new EmptySquare(4, 5), new EmptySquare(4, 6), new EmptySquare(4, 7)},
                                                           {new EmptySquare(5, 0), new EmptySquare(5, 1), new EmptySquare(5, 2), new EmptySquare(5, 3), new EmptySquare(5, 4), new EmptySquare(5, 5), new EmptySquare(5, 6), new EmptySquare(5, 7)},
                                                           {new Pawn(true, 6, 0), new Pawn(true, 6, 1), new Pawn(true, 6, 2), new Pawn(true, 6, 3), new Pawn(true, 6, 4), new Pawn(true, 6, 5), new Pawn(true, 6, 6), new Pawn(true, 6, 7)},
                                                           {new Rook(true, 7, 0), new Knight(true, 7, 1), new Bishop(true, 7, 2), new Queen(true, 7, 3), new King(true, 7, 4), new Bishop(true, 7, 5), new Knight(true, 7, 6), new Rook(true, 7, 7)}};
    public void setLocation(int x, int y) {
        this.rlocation = x;
        this.clocation = y;
    }
    public int getR() {
        return rlocation;
    }
    public int getC() {
        return clocation;
    }
}
