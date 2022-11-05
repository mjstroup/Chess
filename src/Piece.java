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
    public ArrayList<Move> getPinnedMoves(){return null;}
    public ArrayList<Move> getAttackingMoves(){return null;}
    public Piece clonePiece(){return null;}
    public static final String defaultFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    public int value;

    public int getR() {
        return this.rlocation;
    }

    public int getC() {
        return this.clocation;
    }

    public void setLocation(int x, int y) {
        this.rlocation = x;
        this.clocation = y;
    }

    public ArrayList<Move> getLegalMoves() {
        ArrayList<Move> list = new ArrayList<>();
        boolean singleCheck;
        boolean doubleCheck;
        ArrayList<Piece> piecesAttackingOurKing;
        if (this.white)
            piecesAttackingOurKing = Board.whiteKing.attackedByBlackList();
        else
            piecesAttackingOurKing = Board.blackKing.attackedByWhiteList();
        int attackers = piecesAttackingOurKing.size();
        switch (attackers) {
            case 0 -> {
                singleCheck = false;
                doubleCheck = false;
            }
            case 1 -> {
                singleCheck = true;
                doubleCheck = false;
            }
            default -> {
                singleCheck = false;
                doubleCheck = true;
            }
        }

        if (singleCheck) {
            Piece attacker = piecesAttackingOurKing.get(0);
            
            /**
             * We are in check, only way to get out is by
             * 1. Capture checked piece with a non-pinned piece
             * 2. Block the check (if check is a sliding piece, i.e., not a knight or pawn) by a non-pinned piece
             * 3. King moves to non-attacked square
             * 
             * NOTE: King cannot castle out of this position, so we don't need to calculate for it
             */
            
            //3. move king
            if (this instanceof King) {
                list = this.getPossibleMoves();
                return list;
            }
            //1. take piece
            if (this.isPinned() == null) {
                //EP take
                if ((this.clocation != 0 && Board.pieces[this.rlocation][this.clocation-1] == attacker || this.clocation != 7 && Board.pieces[this.rlocation][this.clocation+1] == attacker) && this.white && this instanceof Pawn && !attacker.white && attacker.rlocation != 0 && Board.pieces[attacker.rlocation-1][attacker.clocation] == Board.enPassantPiece && Board.enPassantPiece.enPassant) {
                    list.add(new Move(this, Board.enPassantPiece));
                }
                if ((this.clocation != 0 && Board.pieces[this.rlocation][this.clocation-1] == attacker || this.clocation != 7 && Board.pieces[this.rlocation][this.clocation+1] == attacker) && !this.white && this instanceof Pawn && attacker.white && attacker.rlocation != 7 && Board.pieces[attacker.rlocation+1][attacker.clocation] == Board.enPassantPiece && Board.enPassantPiece.enPassant) {

                    list.add(new Move(this, Board.enPassantPiece));
                }
                if (attacker.white && attacker.attackedByBlackList().contains(this)) {
                    for (Move m : this.getPossibleMoves()) {
                        if (m.endingPiece == attacker) {
                            list.add(m);
                            if (!(this instanceof Pawn))
                                break;
                        }
                    }
                } else if (!attacker.white && attacker.attackedByWhiteList().contains(this)) {
                    for (Move m : this.getPossibleMoves()) {
                        if (m.endingPiece == attacker) {
                            list.add(m);
                            if (!(this instanceof Pawn))
                                break;
                        }
                    }
                }
                //2. block the check
                if (!(attacker instanceof Knight || attacker instanceof Pawn)) {
                    ArrayList<Piece> blockSquares = new ArrayList<>();
                    King k = this.white ? Board.whiteKing : Board.blackKing;
                    int rdif = attacker.rlocation - k.rlocation;
                    int cdif = attacker.clocation - k.clocation;
                    if (rdif * cdif != 0 && Math.abs(rdif) != Math.abs(cdif))
                        return null;
                    int rdirection, cdirection;
                    if (rdif > 0) {
                        rdirection = 1;
                    } else if (rdif < 0) {
                        rdirection = -1;
                    } else {
                        rdirection = 0;
                    }
                    if (cdif > 0) {
                        cdirection = 1;
                    } else if (cdif < 0) {
                        cdirection = -1;
                    } else {
                        cdirection = 0;
                    }
                    for (int i = 1; i < 9; i++) {
                        Piece p = Board.pieces[attacker.rlocation+(rdirection*i*-1)][attacker.clocation+(cdirection*i*-1)];
                        if (p == k) {
                            break;
                        }
                        blockSquares.add(p);
                    }
                    for (Move m : this.getPossibleMoves()) {
                        if (blockSquares.contains(m.endingPiece))
                            list.add(m);
                    }
                }
            }
        } else if (doubleCheck) {
            /*
             * We are double checked, only way to get out is
             * 1. Move the king to non-attacking square.
             */
            if (this instanceof King) {
                list.addAll(this.getPossibleMoves());
            }
        } else {
            /*
             * We are not in check, we generate all possible moves
             */
            list.addAll(this.getPossibleMoves());
        }
        return list;
    }

    public boolean isAttackedByWhite() {
        return attackedBy(true);
    }

    public boolean isAttackedByBlack() {
        return attackedBy(false);
    }

    public ArrayList<Piece> attackedByWhiteList() {
        return attackedByList(true);
    }

    public ArrayList<Piece> attackedByBlackList() {
        return attackedByList(false);
    }

    public ArrayList<Piece> attackedByList(boolean white) {
        ArrayList<Piece> list = new ArrayList<>();
        //look at knight squraes
        int r = rlocation-1;
        int c = clocation+2;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7 && Board.pieces[r][c] instanceof Knight && Board.pieces[r][c].white == white) {
            list.add(Board.pieces[r][c]);
        }
        r = rlocation-2;
        c = clocation+1;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7 && Board.pieces[r][c] instanceof Knight && Board.pieces[r][c].white == white)
            list.add(Board.pieces[r][c]);
        r = rlocation-2;
        c = clocation-1;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7 && Board.pieces[r][c] instanceof Knight && Board.pieces[r][c].white == white)
            list.add(Board.pieces[r][c]);
        r = rlocation-1;
        c = clocation-2;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7 && Board.pieces[r][c] instanceof Knight && Board.pieces[r][c].white == white)
            list.add(Board.pieces[r][c]);
        r = rlocation+1;
        c = clocation-2;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7 && Board.pieces[r][c] instanceof Knight && Board.pieces[r][c].white == white)
            list.add(Board.pieces[r][c]);
        r = rlocation+2;
        c = clocation-1;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7 && Board.pieces[r][c] instanceof Knight && Board.pieces[r][c].white == white)
            list.add(Board.pieces[r][c]);
        r = rlocation+2;
        c = clocation+1;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7 && Board.pieces[r][c] instanceof Knight && Board.pieces[r][c].white == white)
            list.add(Board.pieces[r][c]);
        r = rlocation+1;
        c = clocation+2;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7 && Board.pieces[r][c] instanceof Knight && Board.pieces[r][c].white == white)
            list.add(Board.pieces[r][c]);
        
        //check pawn squares
        int offset = white ? 1 : -1;
        if (rlocation+offset >= 0 && rlocation+offset <= 7 && clocation-1 >= 0 && Board.pieces[rlocation+offset][clocation-1] instanceof Pawn && Board.pieces[rlocation+offset][clocation-1].white == white) {
            list.add(Board.pieces[rlocation+offset][clocation-1]);
        }
        if (rlocation+offset >= 0 && rlocation+offset <= 7 && clocation+1 <= 7 && Board.pieces[rlocation+offset][clocation+1] instanceof Pawn && Board.pieces[rlocation+offset][clocation+1].white == white) {
            list.add(Board.pieces[rlocation+offset][clocation+1]);
        }
        
        //check king squares
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                int r2 = rlocation + i;
                int c2 = clocation + j;
                if (r2 <= 7 && r2 >= 0 && c2 <= 7 && c2 >= 0 && Board.pieces[r2][c2] instanceof King && Board.pieces[r2][c2].white == white)
                    list.add(Board.pieces[r2][c2]);
            }
        }

        //check sliding pieces
        boolean left = true, up = true, right = true, down = true, topLeft = true, topRight = true, bottomLeft = true, bottomRight = true;
        for (int i = 1; i < 8; i++) {
            if (!(left || up || right || down || topLeft || topRight || bottomLeft || bottomRight)) break;
            if (left) {
                Piece p = null;
                if (clocation-i <= 7 && clocation-i >= 0) 
                    p = Board.pieces[rlocation][clocation-i];
                if (!(clocation-i <= 7 && clocation-i >= 0)) {
                    left = false;
                } else if ((p instanceof Rook || p instanceof Queen) && p.white == white) {
                    list.add(p);
                    left = false;
                } else if (!(p instanceof EmptySquare) && !(p instanceof King && p.white != white)) {
                    left = false;
                }
            }
            if (up) {
                Piece p = null;
                if (rlocation-i <= 7 && rlocation-i >= 0) 
                    p = Board.pieces[rlocation-i][clocation];
                if (!(rlocation-i <= 7 && rlocation-i >= 0)) {
                    up = false;
                } else if ((p instanceof Rook || p instanceof Queen) && p.white == white) {
                    list.add(p);
                    up = false;
                } else if (!(p instanceof EmptySquare) && !(p instanceof King && p.white != white)) {
                    up = false;
                }
            }
            if (right) {
                Piece p = null;
                if (clocation+i <= 7 && clocation+i >= 0) 
                    p = Board.pieces[rlocation][clocation+i];
                if (!(clocation+i <= 7 && clocation+i >= 0)) {
                    right = false;
                } else if ((p instanceof Rook || p instanceof Queen) && p.white == white) {
                    list.add(p);
                    right = false;
                } else if (!(p instanceof EmptySquare) && !(p instanceof King && p.white != white)) {
                    right = false;
                }
            }
            if (down) {
                Piece p = null;
                if (rlocation+i <= 7 && rlocation+i >= 0) 
                    p = Board.pieces[rlocation+i][clocation];
                if (!(rlocation+i <= 7 && rlocation+i >= 0)) {
                    down = false;
                } else if ((p instanceof Rook || p instanceof Queen) && p.white == white) {
                    list.add(p);
                    down = false;
                } else if (!(p instanceof EmptySquare) && !(p instanceof King && p.white != white)) {
                    down = false;
                }
            }
            if (topLeft) {
                Piece p = null;
                if (rlocation-i >= 0 && rlocation-i <= 7 && clocation-i >= 0 && clocation-i <= 7)
                    p = Board.pieces[rlocation-i][clocation-i];
                if (!(rlocation-i >= 0 && rlocation-i <= 7 && clocation-i >= 0 && clocation-i <= 7)) {
                    topLeft = false;
                } else if ((p instanceof Bishop || p instanceof Queen) && p.white == white) {
                    list.add(p);
                    topLeft = false;
                } else if (!(p instanceof EmptySquare) && !(p instanceof King && p.white != white)) {
                    topLeft = false;
                }
            }
            if (topRight) {
                Piece p = null;
                if (rlocation-i >= 0 && rlocation-i <= 7 && clocation+i >= 0 && clocation+i <= 7)
                    p = Board.pieces[rlocation-i][clocation+i];
                if (!(rlocation-i >= 0 && rlocation-i <= 7 && clocation+i >= 0 && clocation+i <= 7)) {
                    topRight = false;
                } else if ((p instanceof Bishop || p instanceof Queen) && p.white == white) {
                    list.add(p);
                    topRight = false;
                } else if (!(p instanceof EmptySquare) && !(p instanceof King && p.white != white)) {
                    topRight = false;
                }
            }
            if (bottomLeft) {
                Piece p = null;
                if (rlocation+i >= 0 && rlocation+i <= 7 && clocation-i >= 0 && clocation-i <= 7)
                    p = Board.pieces[rlocation+i][clocation-i];
                if (!(rlocation+i >= 0 && rlocation+i <= 7 && clocation-i >= 0 && clocation-i <= 7)) {
                    bottomLeft = false;
                } else if ((p instanceof Bishop || p instanceof Queen) && p.white == white) {
                    list.add(p);
                    bottomLeft = false;
                } else if (!(p instanceof EmptySquare) && !(p instanceof King && p.white != white)) {
                    bottomLeft = false;
                }
            }
            if (bottomRight) {
                Piece p = null;
                if (rlocation+i >= 0 && rlocation+i <= 7 && clocation+i >= 0 && clocation+i <= 7)
                    p = Board.pieces[rlocation+i][clocation+i];
                if (!(rlocation+i >= 0 && rlocation+i <= 7 && clocation+i >= 0 && clocation+i <= 7)) {
                    bottomRight = false;
                } else if ((p instanceof Bishop || p instanceof Queen) && p.white == white) {
                    list.add(p);
                    bottomRight = false;
                } else if (!(p instanceof EmptySquare) && !(p instanceof King && p.white != white)) {
                    bottomRight = false;
                }
            }
        }
        return list;
    }

    public boolean attackedBy(boolean white) {
        //look at knight squraes
        int r = rlocation-1;
        int c = clocation+2;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7 && Board.pieces[r][c] instanceof Knight && Board.pieces[r][c].white == white)
            return true;
        r = rlocation-2;
        c = clocation+1;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7 && Board.pieces[r][c] instanceof Knight && Board.pieces[r][c].white == white)
            return true;
        r = rlocation-2;
        c = clocation-1;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7 && Board.pieces[r][c] instanceof Knight && Board.pieces[r][c].white == white)
            return true;
        r = rlocation-1;
        c = clocation-2;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7 && Board.pieces[r][c] instanceof Knight && Board.pieces[r][c].white == white)
            return true;
        r = rlocation+1;
        c = clocation-2;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7 && Board.pieces[r][c] instanceof Knight && Board.pieces[r][c].white == white)
            return true;
        r = rlocation+2;
        c = clocation-1;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7 && Board.pieces[r][c] instanceof Knight && Board.pieces[r][c].white == white)
            return true;
        r = rlocation+2;
        c = clocation+1;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7 && Board.pieces[r][c] instanceof Knight && Board.pieces[r][c].white == white)
            return true;
        r = rlocation+1;
        c = clocation+2;
        if (r >= 0 && r <= 7 && c >= 0 && c <= 7 && Board.pieces[r][c] instanceof Knight && Board.pieces[r][c].white == white)
            return true;
        
        //check pawn squares
        int offset = white ? 1 : -1;
        if (rlocation+offset >= 0 && rlocation+offset <= 7 && clocation-1 >= 0 && Board.pieces[rlocation+offset][clocation-1] instanceof Pawn && Board.pieces[rlocation+offset][clocation-1].white == white) {
            return true;
        }
        if (rlocation+offset >= 0 && rlocation+offset <= 7 && clocation+1 <= 7 && Board.pieces[rlocation+offset][clocation+1] instanceof Pawn && Board.pieces[rlocation+offset][clocation+1].white == white) {
            return true;
        }
        
        //check king squares
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                int r2 = rlocation + i;
                int c2 = clocation + j;
                if (r2 <= 7 && r2 >= 0 && c2 <= 7 && c2 >= 0 && Board.pieces[r2][c2] instanceof King && Board.pieces[r2][c2].white == white)
                    return true;
            }
        }

        //check sliding pieces
        boolean left = true, up = true, right = true, down = true, topLeft = true, topRight = true, bottomLeft = true, bottomRight = true;
        for (int i = 1; i < 8; i++) {
            if (!(left || up || right || down || topLeft || topRight || bottomLeft || bottomRight)) break;
            if (left) {
                Piece p = null;
                if (clocation-i <= 7 && clocation-i >= 0) 
                    p = Board.pieces[rlocation][clocation-i];
                if (!(clocation-i <= 7 && clocation-i >= 0)) {
                    left = false;
                } else if ((p instanceof Rook || p instanceof Queen) && p.white == white) {
                    return true;
                } else if (!(p instanceof EmptySquare) && !(p instanceof King && p.white != white)) {
                    left = false;
                }
            }
            if (up) {
                Piece p = null;
                if (rlocation-i <= 7 && rlocation-i >= 0) 
                    p = Board.pieces[rlocation-i][clocation];
                if (!(rlocation-i <= 7 && rlocation-i >= 0)) {
                    up = false;
                } else if ((p instanceof Rook || p instanceof Queen) && p.white == white) {
                    return true;
                } else if (!(p instanceof EmptySquare) && !(p instanceof King && p.white != white)) {
                    up = false;
                }
            }
            if (right) {
                Piece p = null;
                if (clocation+i <= 7 && clocation+i >= 0) 
                    p = Board.pieces[rlocation][clocation+i];
                if (!(clocation+i <= 7 && clocation+i >= 0)) {
                    right = false;
                } else if ((p instanceof Rook || p instanceof Queen) && p.white == white) {
                    return true;
                } else if (!(p instanceof EmptySquare) && !(p instanceof King && p.white != white)) {
                    right = false;
                }
            }
            if (down) {
                Piece p = null;
                if (rlocation+i <= 7 && rlocation+i >= 0) 
                    p = Board.pieces[rlocation+i][clocation];
                if (!(rlocation+i <= 7 && rlocation+i >= 0)) {
                    down = false;
                } else if ((p instanceof Rook || p instanceof Queen) && p.white == white) {
                    return true;
                } else if (!(p instanceof EmptySquare) && !(p instanceof King && p.white != white)) {
                    down = false;
                }
            }
            if (topLeft) {
                Piece p = null;
                if (rlocation-i >= 0 && rlocation-i <= 7 && clocation-i >= 0 && clocation-i <= 7)
                    p = Board.pieces[rlocation-i][clocation-i];
                if (!(rlocation-i >= 0 && rlocation-i <= 7 && clocation-i >= 0 && clocation-i <= 7)) {
                    topLeft = false;
                } else if ((p instanceof Bishop || p instanceof Queen) && p.white == white) {
                    return true;
                } else if (!(p instanceof EmptySquare) && !(p instanceof King && p.white != white)) {
                    topLeft = false;
                }
            }
            if (topRight) {
                Piece p = null;
                if (rlocation-i >= 0 && rlocation-i <= 7 && clocation+i >= 0 && clocation+i <= 7)
                    p = Board.pieces[rlocation-i][clocation+i];
                if (!(rlocation-i >= 0 && rlocation-i <= 7 && clocation+i >= 0 && clocation+i <= 7)) {
                    topRight = false;
                } else if ((p instanceof Bishop || p instanceof Queen) && p.white == white) {
                    return true;
                } else if (!(p instanceof EmptySquare) && !(p instanceof King && p.white != white)) {
                    topRight = false;
                }
            }
            if (bottomLeft) {
                Piece p = null;
                if (rlocation+i >= 0 && rlocation+i <= 7 && clocation-i >= 0 && clocation-i <= 7)
                    p = Board.pieces[rlocation+i][clocation-i];
                if (!(rlocation+i >= 0 && rlocation+i <= 7 && clocation-i >= 0 && clocation-i <= 7)) {
                    bottomLeft = false;
                } else if ((p instanceof Bishop || p instanceof Queen) && p.white == white) {
                    return true;
                } else if (!(p instanceof EmptySquare) && !(p instanceof King && p.white != white)) {
                    bottomLeft = false;
                }
            }
            if (bottomRight) {
                Piece p = null;
                if (rlocation+i >= 0 && rlocation+i <= 7 && clocation+i >= 0 && clocation+i <= 7)
                    p = Board.pieces[rlocation+i][clocation+i];
                if (!(rlocation+i >= 0 && rlocation+i <= 7 && clocation+i >= 0 && clocation+i <= 7)) {
                    bottomRight = false;
                } else if ((p instanceof Bishop || p instanceof Queen) && p.white == white) {
                    return true;
                } else if (!(p instanceof EmptySquare) && !(p instanceof King && p.white != white)) {
                    bottomRight = false;
                }
            }
        }
        return false;
    }

    public Piece isPinned() {
        if (this == Board.blackKing || this == Board.whiteKing)
            return null;
        //get direction from piece to its king
        int rdif, cdif;
        if (this.white) {
            rdif = this.rlocation - Board.whiteKing.rlocation;
            cdif = this.clocation - Board.whiteKing.clocation;
        } else {
            rdif = this.rlocation - Board.blackKing.rlocation;
            cdif = this.clocation - Board.blackKing.clocation;
        }
        if (rdif * cdif != 0 && Math.abs(rdif) != Math.abs(cdif))
            return null;
        int rdirection, cdirection;
        if (rdif > 0) {
            rdirection = 1;
        } else if (rdif < 0) {
            rdirection = -1;
        } else {
            rdirection = 0;
        }
        if (cdif > 0) {
            cdirection = 1;
        } else if (cdif < 0) {
            cdirection = -1;
        } else {
            cdirection = 0;
        }
        //if theres a piece inbetween us and the king... we are not pinned
        for (int i = 1; i < 9; i++) {
            Piece p = Board.pieces[this.rlocation+(rdirection*i*-1)][this.clocation+(cdirection*i*-1)];
            if (p instanceof King && p.white == this.white) {
                break;
            }
            if (!(p instanceof EmptySquare)) {
                return null;
            }
        }
        //look for piece that will pin us
        for (int i = 1; i < 9; i++) {
            if (this.rlocation+(rdirection*i) < 0 || this.rlocation+(rdirection*i) > 7 || this.clocation+(cdirection*i) < 0 || this.clocation+(cdirection*i) > 7) {
                break;
            }
            Piece p = Board.pieces[this.rlocation+(rdirection*i)][this.clocation+(cdirection*i)];
            if (p instanceof Queen && p.white != this.white) {
                return p;
            }
            if (p instanceof Bishop && p.white != this.white && rdirection*cdirection != 0) {
                //ensure we are going diagonally
                return p;
            }
            if (p instanceof Rook && p.white != this.white && rdirection*cdirection == 0) {
                //ensure we are not going diagonally
                return p;
            }
            if (!(p instanceof EmptySquare))
                return null;
        }
        return null;
    }

    public ArrayList<Move> getSlidingAttackingMoves() {
        ArrayList<Move> list = new ArrayList<>();
        boolean topLeft = true, topRight = true, bottomLeft = true, bottomRight = true, up = true, left = true, right = true, down = true;
        if (this instanceof Rook) {
            topRight = false;
            topLeft = false;
            bottomLeft = false;
            bottomRight = false;
        }
        if (this instanceof Bishop) {
            left = false;
            right = false;
            down = false;
            up = false;
        }
        for (int i = 1; i < 8; i++) {
            if (!(left || up || right || down || topLeft || topRight || bottomLeft || bottomRight)) break;
            if (topLeft) {
                Piece p = null;
                if (rlocation-i >= 0 && rlocation-i <= 7 && clocation-i >= 0 && clocation-i <= 7)
                    p = Board.pieces[rlocation-i][clocation-i];
                if (!(rlocation-i >= 0 && rlocation-i <= 7 && clocation-i >= 0 && clocation-i <= 7)) {
                    topLeft = false;
                } else if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else if (p instanceof King && p.white != this.white) {
                    list.add(new Move(this, p));
                } else {
                    list.add(new Move(this, p));
                    topLeft = false;
                }
            }
            if (topRight) {
                Piece p = null;
                if (rlocation-i >= 0 && rlocation-i <= 7 && clocation+i >= 0 && clocation+i <= 7)
                    p = Board.pieces[rlocation-i][clocation+i];
                if (!(rlocation-i >= 0 && rlocation-i <= 7 && clocation+i >= 0 && clocation+i <= 7)) {
                    topRight = false;
                } else if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else if (p instanceof King && p.white != this.white) {
                    list.add(new Move(this, p));
                } else {
                    list.add(new Move(this, p));
                    topRight = false;
                }
            }
            if (bottomLeft) {
                Piece p = null;
                if (rlocation+i >= 0 && rlocation+i <= 7 && clocation-i >= 0 && clocation-i <= 7)
                    p = Board.pieces[rlocation+i][clocation-i];
                if (!(rlocation+i >= 0 && rlocation+i <= 7 && clocation-i >= 0 && clocation-i <= 7)) {
                    bottomLeft = false;
                } else if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else if (p instanceof King && p.white != this.white) {
                    list.add(new Move(this, p));
                } else {
                    list.add(new Move(this, p));
                    bottomLeft = false;
                }
            }
            if (bottomRight) {
                Piece p = null;
                if (rlocation+i >= 0 && rlocation+i <= 7 && clocation+i >= 0 && clocation+i <= 7)
                    p = Board.pieces[rlocation+i][clocation+i];
                if (!(rlocation+i >= 0 && rlocation+i <= 7 && clocation+i >= 0 && clocation+i <= 7)) {
                    bottomRight = false;
                } else if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else if (p instanceof King && p.white != this.white) {
                    list.add(new Move(this, p));
                } else {
                    list.add(new Move(this, p));
                    bottomRight = false;
                }
            }
            if (left) {
                Piece p = null;
                if (clocation-i <= 7 && clocation-i >= 0) 
                    p = Board.pieces[rlocation][clocation-i];
                if (!(clocation-i <= 7 && clocation-i >= 0)) {
                    left = false;
                } else if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else if (p instanceof King && p.white != this.white) {
                    list.add(new Move(this, p));
                } else {
                    list.add(new Move(this, p));
                    left = false;
                }
            }
            if (up) {
                Piece p = null;
                if (rlocation-i <= 7 && rlocation-i >= 0) 
                    p = Board.pieces[rlocation-i][clocation];
                if (!(rlocation-i <= 7 && rlocation-i >= 0)) {
                    up = false;
                } else if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else if (p instanceof King && p.white != this.white) {
                    list.add(new Move(this, p));
                } else {
                    list.add(new Move(this, p));
                    up = false;
                }
            }
            if (right) {
                Piece p = null;
                if (clocation+i <= 7 && clocation+i >= 0) 
                    p = Board.pieces[rlocation][clocation+i];
                if (!(clocation+i <= 7 && clocation+i >= 0)) {
                    right = false;
                } else if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else if (p instanceof King && p.white != this.white) {
                    list.add(new Move(this, p));
                } else {
                    list.add(new Move(this, p));
                    right = false;
                }
            }
            if (down) {
                Piece p = null;
                if (rlocation+i <= 7 && rlocation+i >= 0) 
                    p = Board.pieces[rlocation+i][clocation];
                if (!(rlocation+i <= 7 && rlocation+i >= 0)) {
                    down = false;
                } else if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else if (p instanceof King && p.white != this.white) {
                    list.add(new Move(this, p));
                } else {
                    list.add(new Move(this, p));
                    down = false;
                }
            }
        }   
        return list;
    }
    public ArrayList<Move> getSlidingPinnedMoves() {
        ArrayList<Move> list = new ArrayList<>();
        Piece attacker = this.isPinned();
        int rdif = attacker.rlocation - this.rlocation;
        int cdif = attacker.clocation - this.clocation;
        boolean topLeft = true, topRight = true, bottomLeft = true, bottomRight = true, left = true, up = true, right = true, down = true;
        if (this instanceof Rook) {
            topRight = false;
            topLeft = false;
            bottomLeft = false;
            bottomRight = false;
        }
        if (this instanceof Bishop) {
            left = false;
            right = false;
            down = false;
            up = false;
        }
        if (this instanceof Queen) {
            if (rdif != 0 && cdif != 0) {
                up = false;
                down = false;
                left = false;
                right = false;
            } else {
                topRight = false;
                topLeft = false;
                bottomLeft = false;
                bottomRight = false;
            }
        }
        //rook and queen
        if (rdif == 0) {
            up = false;
            down = false;
        } else {
            right = false;
            left = false;
        }
        if (rdif*cdif > 0) {
            bottomLeft = false;
            topRight = false;
        } else {
            bottomRight = false;
            topLeft = false;
        }
        for (int i = 1; i < 8; i++) {
            if (!(topLeft || topRight || bottomLeft || bottomRight || up || down || right || left)) break;
            if (topLeft) {
                Piece p = Board.pieces[rlocation-i][clocation-i];
                if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else if (p instanceof King && p.white == this.white) {
                    topLeft = false;
                } else if (p == attacker) {
                    list.add(new Move(this, p));
                    topLeft = false;
                }
            }
            if (topRight) {
                Piece p = Board.pieces[rlocation-i][clocation+i];
                if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else if (p instanceof King && p.white == this.white) {
                    topRight = false;
                } else if (p == attacker) {
                    list.add(new Move(this, p));
                    topRight = false;
                }
            }
            if (bottomLeft) {
                Piece p = Board.pieces[rlocation+i][clocation-i];
                if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else if (p instanceof King && p.white == this.white) {
                    bottomLeft = false;
                } else if (p == attacker) {
                    list.add(new Move(this, p));
                    bottomLeft = false;
                }
            }
            if (bottomRight) {
                Piece p = Board.pieces[rlocation+i][clocation+i];
                if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else if (p instanceof King && p.white == this.white) {
                    bottomRight = false;
                } else if (p == attacker) {
                    list.add(new Move(this, p));
                    bottomRight = false;
                }
            }
            if (left) {
                Piece p = Board.pieces[rlocation][clocation-i];
                if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else if (p instanceof King && p.white == this.white) {
                    left = false;
                } else if (p == attacker) {
                    list.add(new Move(this, p));
                    left = false;
                }
            }
            if (up) {
                Piece p = Board.pieces[rlocation-i][clocation];
                if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else if (p instanceof King && p.white == this.white) {
                    up = false;
                } else if (p == attacker) {
                    list.add(new Move(this, p));
                    up = false;
                }
            }
            if (right) {
                Piece p = Board.pieces[rlocation][clocation+i];
                if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else if (p instanceof King && p.white == this.white) {
                    right = false;
                } else if (p == attacker) {
                    list.add(new Move(this, p));
                    right = false;
                }
            }
            if (down) {
                Piece p = Board.pieces[rlocation+i][clocation];
                if (p instanceof EmptySquare) {
                    list.add(new Move(this, p));
                } else if (p instanceof King && p.white == this.white) {
                    down = false;
                } else if (p == attacker) {
                    list.add(new Move(this, p));
                    down = false;
                }
            }
        }   
        
        return list;
    }
}
