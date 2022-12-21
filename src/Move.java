package src;
public class Move {
    public Piece startingPiece;
    public Piece endingPiece;
    public char promCharacter;
    public int eval;
    public Move(Piece startingPiece, Piece endingPiece) {
        this.startingPiece = startingPiece;
        this.endingPiece = endingPiece;
    }
    public Move(Piece startingPiece, Piece endingPiece, char promCharacter) {
        this.startingPiece = startingPiece;
        this.endingPiece = endingPiece;
        this.promCharacter = promCharacter;
    }
    public Move(Piece startingPiece, Piece endingPiece, int eval) {
        this.startingPiece = startingPiece;
        this.endingPiece = endingPiece;
        this.eval = eval;
    }
    public String toString() {
        if (this.promCharacter == Character.UNASSIGNED)
            return Board.pieceToCoords(startingPiece) + Board.pieceToCoords(endingPiece);
        else
        return Board.pieceToCoords(startingPiece) + Board.pieceToCoords(endingPiece) + this.promCharacter;
    }
}
