public class Pawn extends Piece {
    public Pawn(Boolean white) {
        this.white = white;
        fileName = this.white ? "./Images/wP.png" : "./Images/bP.png";
    }
}
