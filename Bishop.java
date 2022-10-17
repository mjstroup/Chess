public class Bishop extends Piece {
    public Bishop(Boolean white) {
        this.white = white;
        fileName = this.white ? "./Images/wB.png" : "./Images/bB.png";
    }
}
