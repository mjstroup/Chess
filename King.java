public class King extends Piece {
    public King (Boolean white) {
        this.white = white;
        fileName = this.white ? "./Images/wK.png" : "./Images/bK.png";
    }
}
