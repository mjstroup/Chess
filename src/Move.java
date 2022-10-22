package src;
public class Move {
    public Piece startingPiece;
    public Piece endingPiece;
    public Move(Piece startingPiece, Piece endingPiece) {
        this.startingPiece = startingPiece;
        this.endingPiece = endingPiece;
    }
    public String toString() {
        return startingPiece + "->" + endingPiece;
    }
}
