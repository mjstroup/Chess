package src;

import java.util.*;

public class Gamestate {
    HashMap<String, Integer> repeatMap;
    int halfMoveCount = 0;
    int fullMoveCount = 1;
    boolean whiteTurn;
    boolean WKC;
    boolean WQC;
    boolean BKC;
    boolean BQC;
    EmptySquare EP;
    boolean EPValue;
    boolean moveIsCapture;
    public Gamestate(boolean whiteTurn, HashMap<String, Integer> repeatMap, int halfMoveCount, int fullMoveCount, boolean WKC, boolean WQC, boolean BKC, boolean BQC, EmptySquare EP, boolean EPValue, boolean moveIsCapture) {
        this.whiteTurn = whiteTurn;
        this.repeatMap = repeatMap;
        this.halfMoveCount = halfMoveCount;
        this.fullMoveCount = fullMoveCount;
        this.WKC = WKC;
        this.WQC = WQC;
        this.BKC = BKC;
        this.BQC = BQC; 
        this.EP = EP;
        this.EPValue = EPValue;
        this.moveIsCapture = moveIsCapture;
    }
}
