package src;
import java.util.*;
public class Piece {
    public char abbreviation;
    public String fileName;
    public boolean white;
    public int rlocation;
    public int clocation;
    public char promotion;
    public ArrayList<Move> getPossibleMoves(){return null;}
    public ArrayList<Move> getAttackingMoves(){return null;}
    public Piece clonePiece(){return null;}
    public static final String defaultFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
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
    public boolean isAttackedByWhite() {
        for (int i = 0; i < Board.pieces.length; i++) {
            for (int j = 0; j < Board.pieces[0].length; j++) {
                if (!Board.pieces[i][j].white) continue;
                for (Move m : Board.pieces[i][j].getAttackingMoves()) {
                    if (m.endingPiece == this) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public boolean isAttackedByBlack() {
        for (int i = 0; i < Board.pieces.length; i++) {
            for (int j = 0; j < Board.pieces[0].length; j++) {
                if (Board.pieces[i][j].white) continue;
                for (Move m : Board.pieces[i][j].getAttackingMoves()) {
                    if (m.endingPiece == this) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public void checkTest(ArrayList<Move> moveList) {
        ArrayList<Piece> list = new ArrayList<>();
        for (Move m : moveList) {
            list.add(m.endingPiece);
        }
        Iterator<Piece> it = list.iterator();
        while (it.hasNext()) {
            Piece p = it.next();
            //set up board for when we move
            Piece tempMoving = Board.pieces[this.getR()][this.getC()];
            Piece tempDestination = Board.pieces[p.getR()][p.getC()];
            Board.pieces[p.getR()][p.getC()] = Board.pieces[this.getR()][this.getC()];
            Board.pieces[this.getR()][this.getC()] = new EmptySquare(this.getR(), this.getC());
            int tempR = this.getR();
            int tempC = this.getC();
            this.setLocation(p.getR(), p.getC());
            //now check if king is in check, if it is, remove from list
            out:
            for (int i = 0; i < Board.pieces.length; i++) {
                for (int j = 0; j < Board.pieces[0].length; j++) {
                    if (Board.pieces[i][j] instanceof King && Board.pieces[i][j].white == this.white) {
                        //this is our king
                        if (Board.pieces[i][j].white && Board.pieces[i][j].isAttackedByBlack()) {
                            it.remove();;
                            break out;
                        } else if (!Board.pieces[i][j].white && Board.pieces[i][j].isAttackedByWhite()) {
                            it.remove();
                            break out;
                        }
                    }
                }
            }
            //restore board
            this.setLocation(tempR, tempC);
            Board.pieces[this.getR()][this.getC()] = tempMoving;
            Board.pieces[p.getR()][p.getC()] = tempDestination;
        }
        Iterator<Move> moveListIterator = moveList.iterator();
        while (moveListIterator.hasNext()) {
            Move m = moveListIterator.next();
            if (!list.contains(m.endingPiece)) {
                moveListIterator.remove();
            }
        }
    }
}
