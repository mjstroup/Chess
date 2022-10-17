public class Queen extends Piece {
    public Queen (Boolean white) {
        this.white = white;
        fileName = this.white ? "./Images/wQ.png" : "./Images/bQ.png";
    }
}
