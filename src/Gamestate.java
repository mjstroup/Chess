package src;

import java.util.*;

public class Gamestate {
    Piece[][] pieces;
    HashMap<String, Integer> repeatMap;
    int halfMoveCount = 0;
    int fullMoveCount = 1;
    boolean whiteTurn;
    boolean WKC;
    boolean WQC;
    boolean BKC;
    boolean BQC;
    public Gamestate(Piece[][] pieces, boolean whiteTurn, HashMap<String, Integer> repeatMap, int halfMoveCount, int fullMoveCount, boolean WKC, boolean WQC, boolean BKC, boolean BQC) {
        this.pieces = pieces;
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
