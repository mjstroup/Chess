public class Knight extends Piece {
    public Knight (Boolean white) {
        this.white = white;
        fileName = this.white ? "./Images/wN.png" : "./Images/bN.png";
    }
}
