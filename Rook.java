public class Rook extends Piece {
    public Rook (Boolean white) {
        this.white = white;
        fileName = this.white ? "./Images/wR.png" : "./Images/bR.png";
    }
}
