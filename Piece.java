public class Piece {
    public String fileName;
    public boolean white;
    public static final Piece[][] newBoard = new Piece[][]{{new Rook(false), new Knight(false), new Bishop(false), new Queen(false), new King(false), new Bishop(false), new Knight(false), new Rook(false)},
                                                           {new Pawn(false), new Pawn(false), new Pawn(false), new Pawn(false), new Pawn(false), new Pawn(false), new Pawn(false), new Pawn(false)},
                                                           {null, null, null, null, null, null, null, null},
                                                           {null, null, null, null, null, null, null, null},
                                                           {null, null, null, null, null, null, null, null},
                                                           {null, null, null, null, null, null, null, null},
                                                           {new Pawn(true), new Pawn(true), new Pawn(true), new Pawn(true), new Pawn(true), new Pawn(true), new Pawn(true), new Pawn(true)},
                                                           {new Rook(true), new Knight(true), new Bishop(true), new Queen(true), new King(true), new Bishop(true), new Knight(true), new Rook(true)}};
}
