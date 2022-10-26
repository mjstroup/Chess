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
    public Gamestate(boolean whiteTurn, HashMap<String, Integer> repeatMap, int halfMoveCount, int fullMoveCount, boolean WKC, boolean WQC, boolean BKC, boolean BQC) {
        this.whiteTurn = whiteTurn;
        this.repeatMap = repeatMap;
        this.halfMoveCount = halfMoveCount;
        this.fullMoveCount = fullMoveCount;
        this.WKC = WKC;
        this.WQC = WQC;
        this.BKC = BKC;
        this.BQC = BQC; 
    }
}
