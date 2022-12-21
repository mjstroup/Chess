package src;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.io.*;

import javax.sound.sampled.*;

public class Board extends JFrame  implements MouseListener, MouseMotionListener{
    final Color light = new Color(240,217,181);
    final Color dark = new Color(181,136,99);
    final Color lightCover = new Color(174,177,136);
    final Color darkCover = new Color(133,120,78);
    final Color previousMoveOriginalColor = new Color(206,175,104);
    final Color previousMoveDestColor = new Color(215,205,118);
    
    public static King whiteKing;
    public static King blackKing;
    public static EmptySquare enPassantPiece;
    public static boolean whiteTurn = true;
    public static Piece[][] pieces;
    private static Engine engine;
    private static int halfMoveCount = 0;
    private static int fullMoveCount = 1;
    private static HashMap<String, Integer> repeatMap;
    private Piece currentPiece;
    private ArrayList<Move> currentMoves;
    private JLayeredPane layeredPane;
    private JPanel chessBoard;
    private JPanel originalPanel;
    private JPanel currentPanel;
    private Color currentPanelColor;
    private Color originalPanelColor;
    private JPanel previousMoveOriginalPanel;
    private JPanel previousMoveCurrentPanel;
    private JLabel piece;
    private boolean moveIsCapture = false;
    private Stack<Gamestate> previousGamestates;
    
    public Board(String FENString) {
        this(FENString, null);
    }
    public Board(String FENString, Engine e) {
        engine = e;
        repeatMap = new HashMap<>();
        previousGamestates = new Stack<>();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pieces = fenStringToPieces(FENString);
        Dimension size = new Dimension(800,800);
        layeredPane = new JLayeredPane();
        getContentPane().add(layeredPane);
        layeredPane.setPreferredSize(size);
        layeredPane.addMouseListener(this);
        layeredPane.addMouseMotionListener(this);
        chessBoard = new JPanel(new GridLayout(8,8));
        chessBoard.setBorder(new LineBorder(Color.BLACK));

        layeredPane.add(chessBoard, JLayeredPane.DEFAULT_LAYER);
        chessBoard.setPreferredSize(size);
        chessBoard.setBounds(0,0,size.width,size.height);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                JPanel square = new JPanel(new BorderLayout());
                chessBoard.add(square);
                if ((r + c) % 2 == 0)
                    square.setBackground(light);
                else 
                    square.setBackground(dark);
                if (!(pieces[r][c] instanceof EmptySquare)) {
                    JLabel label = new JLabel();
                    Image image = (new ImageIcon(pieces[r][c].fileName)).getImage();
                    image = image.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
                    label.setIcon(new ImageIcon(image));
                    square.add(label);
                }
            }
        }
    }

    public void mousePressed(MouseEvent me) {
        if (!SwingUtilities.isLeftMouseButton(me)) return;

        Component c = chessBoard.findComponentAt(me.getX(), me.getY());
        if (c instanceof JPanel) return;

        piece = (JLabel)c;
        piece.setLocation(me.getX() - 49, me.getY() - 45);
        piece.setSize(piece.getWidth(), piece.getHeight());
        layeredPane.add(piece, JLayeredPane.DRAG_LAYER);
        currentPanel = (JPanel)chessBoard.findComponentAt(me.getX(), me.getY());
        currentPanelColor = currentPanel.getBackground();
        originalPanelColor = currentPanel.getBackground();
        originalPanel = (JPanel)chessBoard.findComponentAt(me.getX(), me.getY());
        if (isWhitePanel(originalPanel))
            originalPanel.setBackground(lightCover);
        else
            originalPanel.setBackground(darkCover);
        currentPiece = componentToPiece(currentPanel);
        currentMoves = currentPiece.getLegalMoves();
        System.out.println(currentPiece);
        System.out.println(currentMoves);
    }

    public void mouseDragged(MouseEvent me) {
        if (!SwingUtilities.isLeftMouseButton(me)) return;
        if (piece == null) return;
        piece.setLocation(me.getX() - 49, me.getY() - 45);
        Component c = chessBoard.findComponentAt(me.getX(), me.getY());
        //in different panel now... change old panel to original color and mark new color
        if (!currentPanel.equals(c)) {
            currentPanel.setBackground(currentPanelColor);
            if (c instanceof JLabel) {
                currentPanelColor = ((JPanel)c.getParent()).getBackground();
            } else {
                currentPanelColor = c.getBackground();
            }
        }
        //ensure that the original panel stays lit up for show
        if (isWhitePanel(originalPanel))
            originalPanel.setBackground(lightCover);
        else
            originalPanel.setBackground(darkCover);
        Piece currentPanelPiece = componentToPiece(c);
        if (c instanceof JLabel) {
            currentPanel = (JPanel)c.getParent();
            currentPanelPiece = componentToPiece(c.getParent());
        } else {
            currentPanel = (JPanel)c;
        }
        //light up current panel
        boolean light = false;
        for (Move m : currentMoves) {
            if (m.endingPiece == currentPanelPiece) {
                light = true;
            }
        }
        if (currentPanel instanceof JPanel)
            if (currentPanelPiece != null && light)
                if (isWhitePanel(currentPanel)) 
                    currentPanel.setBackground(lightCover);
                else 
                    currentPanel.setBackground(darkCover);
    }

    public void mouseReleased(MouseEvent me) {
        if (!SwingUtilities.isLeftMouseButton(me)) return;
        if (piece == null) return;
        Component c = chessBoard.findComponentAt(me.getX(), me.getY());
        Piece movingPiece = currentPiece;
        Piece destination = componentToPiece(c);
        boolean returned = false;
        if (c instanceof JLabel) {
            destination = componentToPiece(c.getParent());
        }
        //if this move is not possible... *poof*
        boolean validMove = false;
        Move validMoveMove = null;
        for (Move m : currentMoves) {
            if (m.endingPiece == destination) {
                validMove = true;
                validMoveMove = m;
                break;
            }
        }
        if(!validMove) {
            currentPanel = originalPanel;
            c = originalPanel;
            destination = componentToPiece(c);
            validMoveMove = new Move(movingPiece, destination);
            returned = true;
        }
        piece.setVisible(false);
        
        if (c instanceof JLabel) {
            Container parent = c.getParent();
            c = c.getParent();
            while (parent.getComponentCount() != 0)
                parent.remove(0);
            parent.add(piece);
        } else {
            Container parent = (Container)c;
            parent.add(piece);
        }
        piece.setVisible(true);
        movePiece(validMoveMove);

        currentPanel.setBackground(currentPanelColor);
        originalPanel.setBackground(originalPanelColor);
        //did the move really happen..
        if (!returned) {
            postMove(movingPiece);
        }
        piece = null;
        currentPiece = null;
        currentPanel = null;
        currentPanelColor = null;
        currentMoves = null;
        originalPanel = null;
        moveIsCapture = false;
        if (!returned && engine != null) {
            engine.playMove(this, Board.whiteTurn);
        }
    }

    public void movePiece(Move move) {
        Piece movingPiece = move.startingPiece;
        Piece destination = move.endingPiece;
        if (!(destination instanceof EmptySquare))
            moveIsCapture = true;
        if (movingPiece.equals(destination)) return;
        //en passant move
        if (movingPiece instanceof Pawn && destination instanceof EmptySquare && movingPiece.getC() != destination.getC()) {
            //mark move as capture because movePiece() will not do it for us
            moveIsCapture = true;
            //pawn movement
            pieces[destination.getR()][destination.getC()] = pieces[movingPiece.getR()][movingPiece.getC()];
            pieces[movingPiece.getR()][movingPiece.getC()] = new EmptySquare(movingPiece.getR(), movingPiece.getC());
            movingPiece.setLocation(destination.getR(), destination.getC());
            Pawn passantPiece;
            if (whiteTurn) {
                passantPiece = (Pawn)pieces[destination.getR()+1][destination.getC()];
            } else {
                passantPiece = (Pawn)pieces[destination.getR()-1][destination.getC()];
            }
            //remove EP pawn label
            pieceToComponent(pieces[passantPiece.getR()][passantPiece.getC()]).getParent().remove(0);
            //set EP piece to empty square
            Board.pieces[passantPiece.getR()][passantPiece.getC()] = new EmptySquare(passantPiece.getR(), passantPiece.getC());
            ((EmptySquare)destination).enPassant = false;
            return;
        }
        //reset passant
        if (enPassantPiece != null) {
            enPassantPiece.enPassant = false;
            enPassantPiece = null;
        }
        //double pawn push passant flag
        if (movingPiece instanceof Pawn && Math.abs(movingPiece.getR() - destination.getR()) == 2) {
            if (whiteTurn) {
                enPassantPiece = ((EmptySquare)pieces[destination.getR()+1][destination.getC()]);
            } else {
                enPassantPiece = ((EmptySquare)pieces[destination.getR()-1][destination.getC()]);
            }
            enPassantPiece.enPassant = true;
        }
        //remove castle rights
        if (movingPiece instanceof King) {
            ((King)movingPiece).removeKingCastleRights();
            ((King)movingPiece).removeQueenCastleRights();
        }
        if (movingPiece instanceof Rook && (movingPiece == pieces[7][0] || movingPiece == pieces[7][7]) && movingPiece.white) {
            if (pieces[7][4] instanceof King) {
                if (movingPiece == pieces[7][0])
                    ((King)pieces[7][4]).removeQueenCastleRights();
                else
                    ((King)pieces[7][4]).removeKingCastleRights(); 
            }
        } else if (movingPiece instanceof Rook && (movingPiece == pieces[0][0] || movingPiece == pieces[0][7]) && !movingPiece.white) {
            if (pieces[0][4] instanceof King) {
                if (movingPiece == pieces[0][0])
                    ((King)pieces[0][4]).removeQueenCastleRights();
                else
                    ((King)pieces[0][4]).removeKingCastleRights(); 
            }
        }
        //castle
        if (movingPiece instanceof King && Math.abs(destination.getC() - movingPiece.getC()) > 1) {
            int row = movingPiece.white ? 7 : 0;
            if (destination.getC() == 6) {
                //king side
                //rook
                pieces[row][5] = pieces[row][7];
                pieces[row][5].setLocation(row, 5);
                //king
                pieces[row][6] = pieces[row][4];
                pieces[row][6].setLocation(row, 6);
                //empty king and rook orig squares
                pieces[row][7] = new EmptySquare(row, 7);
                pieces[row][4] = new EmptySquare(row, 4);

                pieceToComponent(pieces[row][7]).getParent().remove(0);
                JLabel label = new JLabel();
                Image image = (new ImageIcon(pieces[row][5].fileName)).getImage();
                image = image.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(image));
                ((JPanel)pieceToComponent(pieces[row][5])).add(label);
                repaint();
                return;
            } else if (destination.getC() == 2) {
                //queen side
                //rook
                pieces[row][3] = pieces[row][0];
                pieces[row][3].setLocation(row, 3);
                //king
                pieces[row][2] = pieces[row][4];
                pieces[row][2].setLocation(row, 2);
                //empty king and rook orig squares
                pieces[row][0] = new EmptySquare(row, 0);
                pieces[row][4] = new EmptySquare(row, 4);

                pieceToComponent(pieces[row][0]).getParent().remove(0);
                JLabel label = new JLabel();
                Image image = (new ImageIcon(pieces[row][3].fileName)).getImage();
                image = image.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(image));
                ((JPanel)pieceToComponent(pieces[row][3])).add(label);
                repaint();
                return;
            }
        }
        //pawn promote
        if (movingPiece instanceof Pawn && (destination.getR() == 0 || destination.getR() == 7)) {
            char promoteCharacter = move.promCharacter;
            switch (promoteCharacter) {
                case 'q' -> {
                    pieces[destination.getR()][destination.getC()] = new Queen(movingPiece.white, destination.getR(), destination.getC());
                }
                case 'r' -> {
                    pieces[destination.getR()][destination.getC()] = new Rook(movingPiece.white, destination.getR(), destination.getC());
                }
                case 'b' -> {
                    pieces[destination.getR()][destination.getC()] = new Bishop(movingPiece.white, destination.getR(), destination.getC());
                }
                case 'n' -> {
                    pieces[destination.getR()][destination.getC()] = new Knight(movingPiece.white, destination.getR(), destination.getC());
                }
            }
            currentPiece = pieces[destination.getR()][destination.getC()];
            Image image = (new ImageIcon(currentPiece.fileName)).getImage();
            image = image.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
            
            piece.setIcon(new ImageIcon(image));
            pieces[movingPiece.getR()][movingPiece.getC()] = new EmptySquare(movingPiece.getR(), movingPiece.getC());
            movingPiece.setLocation(destination.getR(), destination.getC());
            return;
        }
        //now move
        pieces[destination.getR()][destination.getC()] = pieces[movingPiece.getR()][movingPiece.getC()];
        pieces[movingPiece.getR()][movingPiece.getC()] = new EmptySquare(movingPiece.getR(), movingPiece.getC());
        movingPiece.setLocation(destination.getR(), destination.getC());
    }

    public void APImovePiece(Move move) {
        Piece movingPiece = move.startingPiece;
        Piece destination = move.endingPiece;
        if (!(destination instanceof EmptySquare))
            moveIsCapture = true;
        if (movingPiece.equals(destination)) return;
        //en passant move
        if (movingPiece instanceof Pawn && destination instanceof EmptySquare && movingPiece.getC() != destination.getC()) {
            //mark move as capture because movePiece() will not do it for us
            moveIsCapture = true;
            Pawn passantPiece;
            if (whiteTurn) {
                passantPiece = (Pawn)pieces[destination.getR()+1][destination.getC()];
            } else {
                passantPiece = (Pawn)pieces[destination.getR()-1][destination.getC()];
            }
            //pawn movement
            pieces[destination.getR()][destination.getC()] = pieces[movingPiece.getR()][movingPiece.getC()];
            pieces[movingPiece.getR()][movingPiece.getC()] = new EmptySquare(movingPiece.getR(), movingPiece.getC());
            movingPiece.setLocation(destination.getR(), destination.getC());
            //set EP piece to empty square
            Board.pieces[passantPiece.getR()][passantPiece.getC()] = new EmptySquare(passantPiece.getR(), passantPiece.getC());
            ((EmptySquare)destination).enPassant = false;
            return;
        }
        //reset passant
        if (enPassantPiece != null) {
            enPassantPiece.enPassant = false;
            enPassantPiece = null;
        }
        //double pawn push passant flag
        if (movingPiece instanceof Pawn && Math.abs(movingPiece.getR() - destination.getR()) == 2) {
            if (whiteTurn) {
                enPassantPiece = ((EmptySquare)pieces[destination.getR()+1][destination.getC()]);
            } else {
                enPassantPiece = ((EmptySquare)pieces[destination.getR()-1][destination.getC()]);
            }
            enPassantPiece.enPassant = true;
        }
        //remove castle rights
        if (movingPiece instanceof King) {
            ((King)pieces[movingPiece.getR()][movingPiece.getC()]).removeKingCastleRights();
            ((King)pieces[movingPiece.getR()][movingPiece.getC()]).removeQueenCastleRights();
        }
        if (movingPiece instanceof Rook && (movingPiece == pieces[7][0] || movingPiece == pieces[7][7]) && movingPiece.white) {
            if (pieces[7][4] instanceof King) {
                if (movingPiece == pieces[7][0])
                    ((King)pieces[7][4]).removeQueenCastleRights();
                else
                    ((King)pieces[7][4]).removeKingCastleRights(); 
            }
        } else if (movingPiece instanceof Rook && (movingPiece == pieces[0][0] || movingPiece == pieces[0][7]) && !movingPiece.white) {
            if (pieces[0][4] instanceof King) {
                if (movingPiece == pieces[0][0])
                    ((King)pieces[0][4]).removeQueenCastleRights();
                else
                    ((King)pieces[0][4]).removeKingCastleRights(); 
            }
        }
        //castle
        if (movingPiece instanceof King && Math.abs(destination.getC() - movingPiece.getC()) > 1) {
            int row = movingPiece.white ? 7 : 0;
            if (destination.getC() == 6) {
                //king side
                //rook
                pieces[row][5] = pieces[row][7];
                pieces[row][5].setLocation(row, 5);
                //king
                pieces[row][6] = pieces[row][4];
                pieces[row][6].setLocation(row, 6);
                //empty king and rook orig squares
                pieces[row][7] = new EmptySquare(row, 7);
                pieces[row][4] = new EmptySquare(row, 4);
                return;
            } else if (destination.getC() == 2) {
                //queen side
                //rook
                pieces[row][3] = pieces[row][0];
                pieces[row][3].setLocation(row, 3);
                //king
                pieces[row][2] = pieces[row][4];
                pieces[row][2].setLocation(row, 2);
                //empty king and rook orig squares
                pieces[row][0] = new EmptySquare(row, 0);
                pieces[row][4] = new EmptySquare(row, 4);
                return;
            }
        }
        //pawn promote
        if (movingPiece instanceof Pawn && (destination.getR() == 0 || destination.getR() == 7)) {
            char promoteCharacter = move.promCharacter;
            switch (promoteCharacter) {
                case 'q' -> {
                    pieces[destination.getR()][destination.getC()] = new Queen(movingPiece.white, destination.getR(), destination.getC());
                }
                case 'r' -> {
                    pieces[destination.getR()][destination.getC()] = new Rook(movingPiece.white, destination.getR(), destination.getC());
                }
                case 'b' -> {
                    pieces[destination.getR()][destination.getC()] = new Bishop(movingPiece.white, destination.getR(), destination.getC());
                }
                case 'n' -> {
                    pieces[destination.getR()][destination.getC()] = new Knight(movingPiece.white, destination.getR(), destination.getC());
                }
            }
            currentPiece = pieces[destination.getR()][destination.getC()];
            pieces[movingPiece.getR()][movingPiece.getC()] = new EmptySquare(movingPiece.getR(), movingPiece.getC());
            movingPiece.setLocation(destination.getR(), destination.getC());
            return;
        }
        //now move
        pieces[destination.getR()][destination.getC()] = pieces[movingPiece.getR()][movingPiece.getC()];
        pieces[movingPiece.getR()][movingPiece.getC()] = new EmptySquare(movingPiece.getR(), movingPiece.getC());
        movingPiece.setLocation(destination.getR(), destination.getC());
    }

    public void postMove(Piece movingPiece) {
        //play sound
        if (moveIsCapture)
            playSound("../Sounds/Capture.wav");
        else
            playSound("../Sounds/Move.wav");
        //update half move count
        if (moveIsCapture || movingPiece instanceof Pawn)
            halfMoveCount = 0;
        else
            halfMoveCount++;
        
        //update the colors for previous move
        originalPanel.setBackground(previousMoveOriginalColor);
        currentPanel.setBackground(previousMoveDestColor);
        //set previous (previous) move panels to original color unless its equal to one of the above
        if (previousMoveOriginalPanel != null && previousMoveOriginalPanel != currentPanel && previousMoveOriginalPanel != originalPanel) {
            if (((previousMoveOriginalPanel.getX()+previousMoveOriginalPanel.getY())/99)%2 == 0) {
                previousMoveOriginalPanel.setBackground(light);
            } else {
                previousMoveOriginalPanel.setBackground(dark);
            }
        }
        if (previousMoveCurrentPanel != null && previousMoveCurrentPanel != currentPanel && previousMoveCurrentPanel != originalPanel) {
            if (((previousMoveCurrentPanel.getX()+previousMoveCurrentPanel.getY())/99)%2 == 0) {
                previousMoveCurrentPanel.setBackground(light);
            } else {
                previousMoveCurrentPanel.setBackground(dark);
            }
        }
        //re-mark these variables
        previousMoveOriginalPanel = originalPanel;
        previousMoveCurrentPanel = currentPanel;
        
        whiteTurn = !whiteTurn;
        //3 move stalemate
        String fen = this.getFEN();
        fen = fen.substring(0, Board.ordinalIndexOf(fen, " ", 4));
        if (repeatMap.get(fen) != null) {
            repeatMap.put(fen, repeatMap.get(fen)+1);
        } else {
            repeatMap.put(fen, 1);
        }
        if (repeatMap.get(fen) == 3) {
            throwStaleMate();
        }
        //stalemate checks
        //50 move rule
        if (halfMoveCount == 100) {
            throwStaleMate();
        }
        //update full move
        if (whiteTurn) {
            fullMoveCount++;
        }
        // insufficient material
        ArrayList<Piece> whitePieces = new ArrayList<>();
        ArrayList<Piece> blackPieces = new ArrayList<>();
        for (int i = 0; i < Board.pieces.length; i++) {
            for (int j = 0; j < Board.pieces[0].length; j++) {
                Piece p = pieces[i][j];
                if (p instanceof EmptySquare) continue;
                if (p.white) {
                    whitePieces.add(p);
                } else {
                    blackPieces.add(p);
                }
            }
        }
        //2 kings
        if (whitePieces.size() == 1 && blackPieces.size() == 1) {
            throwStaleMate();
        }
        //2 kings + bishop or knight
        if ((whitePieces.size() == 1 || blackPieces.size() == 1) && (whitePieces.size() == 2 || blackPieces.size() == 2)) {
            if (blackPieces.size() == 2) {
                if (blackPieces.get(0) instanceof Bishop || blackPieces.get(1) instanceof Bishop) {
                    throwStaleMate();
                }
                if (blackPieces.get(0) instanceof Knight || blackPieces.get(1) instanceof Knight) {
                    throwStaleMate();
                }
            }
            if (whitePieces.size() == 2) {
                if (whitePieces.get(0) instanceof Bishop || whitePieces.get(1) instanceof Bishop) {
                    throwStaleMate();
                }
                if (whitePieces.get(0) instanceof Knight || whitePieces.get(1) instanceof Knight) {
                    throwStaleMate();
                }
            }
        }
        //2 kings + 2 bishop same color
        if (whitePieces.size() == 2 && blackPieces.size() == 2) {
            if ((whitePieces.get(0) instanceof Bishop || whitePieces.get(1) instanceof Bishop) && (blackPieces.get(0) instanceof Bishop || blackPieces.get(1) instanceof Bishop)) {
                Bishop white = whitePieces.get(0) instanceof King ? (Bishop)whitePieces.get(1) : (Bishop)whitePieces.get(0);
                Bishop black = blackPieces.get(0) instanceof King ? (Bishop)blackPieces.get(1) : (Bishop)blackPieces.get(0);
                if ((white.getR() + white.getC()) % 2 == (black.getR() + black.getC()) % 2) {
                    throwStaleMate();
                }
            }
        }
        //checkmate check
        if (turnInCheckMate()) {
            throwCheckMate();
        } else if (turnInStaleMate()) {
            throwStaleMate();
        }
    }

    /*
     * assumes move is valid
     */
    public void remoteMove(Move move) {
        Piece movingPiece = move.startingPiece;
        Piece destination = move.endingPiece;
        JPanel startingPanel;
        JPanel endingPanel;
        Component originalComponent = pieceToComponent(movingPiece); 
        startingPanel = (JPanel)originalComponent.getParent();
        JLabel pieceLabel = (JLabel)originalComponent;

        Component destinationComponent = pieceToComponent(destination);
        if (destinationComponent instanceof JLabel) {
            endingPanel = (JPanel)destinationComponent.getParent();
        } else {
            endingPanel = (JPanel)destinationComponent;
        }

        pieceLabel.setVisible(false);
        startingPanel.remove(0);
        if (destinationComponent instanceof JLabel) {
            endingPanel.remove(0);
        }
        endingPanel.add(pieceLabel);
        pieceLabel.setVisible(true);

        piece = pieceLabel;

        movePiece(move);
        
        this.originalPanel = startingPanel;
        this.currentPanel = endingPanel;

        postMove(movingPiece);
        
        originalPanel = null;
        currentPanel = null;
        moveIsCapture = false;
        piece = null;
    }

    public void APIMove(Move move) {
        boolean value = false;
        if (enPassantPiece != null) {
            value = enPassantPiece.enPassant;
        }
        previousGamestates.push(new Gamestate(whiteTurn, repeatMap, halfMoveCount, fullMoveCount, whiteKing.kingCastleRights, whiteKing.queenCastleRights, blackKing.kingCastleRights, blackKing.queenCastleRights, enPassantPiece, value, moveIsCapture));
        APImovePiece(move);
        whiteTurn = !whiteTurn;
    }

    public void APIUnMove(Move move, Move clone) {
        Gamestate g = previousGamestates.pop();
        Piece movingPiece = move.startingPiece;
        Piece destination = move.endingPiece;
        if (clone.startingPiece instanceof King && Math.abs(clone.startingPiece.getC() - clone.endingPiece.getC()) > 1) {
            //last move was a castle
            int row = movingPiece.rlocation;;
            if (clone.endingPiece.getC() == 6) {
                //king side
                //rook
                pieces[row][7] = pieces[row][5];
                pieces[row][7].setLocation(row, 7);
                // king
                pieces[row][4] = pieces[row][6];
                pieces[row][4].setLocation(row, 4);
                //empty king and rook orig squares
                pieces[row][5] = new EmptySquare(row, 5);
                pieces[row][6] = new EmptySquare(row, 6);
            } else if (destination.getC() == 2) {
                //queen side
                //rook
                pieces[row][0] = pieces[row][3];
                pieces[row][0].setLocation(row, 0);
                //king
                pieces[row][4] = pieces[row][2];
                pieces[row][4].setLocation(row, 4);
                //empty king and rook orig squares
                pieces[row][2] = new EmptySquare(row, 2);
                pieces[row][3] = new EmptySquare(row, 3);
            }
        } else if (clone.startingPiece instanceof Pawn && clone.endingPiece instanceof EmptySquare && Math.abs(clone.startingPiece.getC()-clone.endingPiece.getC()) != 0) {
            movingPiece.rlocation = clone.startingPiece.rlocation;
            movingPiece.clocation = clone.startingPiece.clocation;
            destination.rlocation = clone.endingPiece.rlocation;
            destination.clocation = clone.endingPiece.clocation;
            pieces[movingPiece.rlocation][movingPiece.clocation] = movingPiece;
            pieces[destination.rlocation][destination.clocation] = destination;
            if (movingPiece.white) {
                pieces[destination.rlocation+1][destination.clocation] = new Pawn(false, destination.rlocation+1, destination.clocation);
            } else {
                pieces[destination.rlocation-1][destination.clocation] = new Pawn(true, destination.rlocation-1, destination.clocation);
            }
        } else {
            movingPiece.rlocation = clone.startingPiece.rlocation;
            movingPiece.clocation = clone.startingPiece.clocation;
            destination.rlocation = clone.endingPiece.rlocation;
            destination.clocation = clone.endingPiece.clocation;
            pieces[movingPiece.rlocation][movingPiece.clocation] = movingPiece;
            pieces[destination.rlocation][destination.clocation] = destination;
        }

        whiteTurn = g.whiteTurn;
        repeatMap = g.repeatMap;
        halfMoveCount = g.halfMoveCount;
        fullMoveCount = g.fullMoveCount;
        whiteKing.kingCastleRights = g.WKC;
        whiteKing.queenCastleRights = g.WQC;
        blackKing.kingCastleRights = g.BKC;
        blackKing.queenCastleRights = g.BQC;
        enPassantPiece = g.EP;
        moveIsCapture = g.moveIsCapture;
        if (enPassantPiece != null) {
            enPassantPiece.enPassant = g.EPValue;
        }
    }

    public static String pieceToCoords(Piece p) {
        int x = p.getR();
        int y = p.getC();
        String s = "";
        s += (char)(y+97);
        s += (8-x) + "";
        return s;
    }
    
    public Piece coordsToPiece(String s, Piece[][] pieces) {
        char letter = s.charAt(0);
        char number = s.charAt(1);
        int x = (8-(number-48));
        int y = letter-97;
        return pieces[x][y];
    }

    public void mouseClicked(MouseEvent me) {}
    public void mouseMoved(MouseEvent me) {}
    public void mouseEntered(MouseEvent me) {}
    public void mouseExited(MouseEvent e) {}

    public Piece componentToPiece(Component c) {
        return pieces[c.getY()/99][c.getX()/99];
    }

    public Component pieceToComponent(Piece p) {
        return chessBoard.findComponentAt(p.getC()*99+50, p.getR()*99+50);
    }
    public boolean isWhitePanel(Component c) {
        return (c.getX()+50/99 + c.getY()+50/99) % 2 == 0;
    }

    /*
     * https://stackoverflow.com/a/26318
     */
    public void playSound(String fileName) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                    Board.class.getResourceAsStream(fileName));
                    clip.open(inputStream);
                    clip.start();
                } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                    System.err.println(e.getMessage());
                }
            }
        }).start();
    }

    public Piece[][] fenStringToPieces(String FENString) {
        Piece[][] pieces = new Piece[8][8];
        int rcounter = 0;
        int ccounter = 0;
        
        //fill out table
        for (int i = 0; i < FENString.indexOf(" "); i++) {
            char c = FENString.charAt(i);
            if (c == '/') continue;
            switch(c) {
                case 'R' -> {
                    pieces[rcounter][ccounter] = new Rook(true, rcounter, ccounter);
                }
                case 'N' -> {
                    pieces[rcounter][ccounter] = new Knight(true, rcounter, ccounter);
                }
                case 'B' -> {
                    pieces[rcounter][ccounter] = new Bishop(true, rcounter, ccounter);
                }
                case 'Q' -> {
                    pieces[rcounter][ccounter] = new Queen(true, rcounter, ccounter);
                }
                case 'K' -> {
                    pieces[rcounter][ccounter] = new King(true, rcounter, ccounter);
                    whiteKing = (King)pieces[rcounter][ccounter];
                }
                case 'P' -> {
                    pieces[rcounter][ccounter] = new Pawn(true, rcounter, ccounter);
                }
                case 'r' -> {
                    pieces[rcounter][ccounter] = new Rook(false, rcounter, ccounter);
                }
                case 'n' -> {
                    pieces[rcounter][ccounter] = new Knight(false, rcounter, ccounter);
                }
                case 'b' -> {
                    pieces[rcounter][ccounter] = new Bishop(false, rcounter, ccounter);
                }
                case 'q' -> {
                    pieces[rcounter][ccounter] = new Queen(false, rcounter, ccounter);
                }
                case 'k' -> {
                    pieces[rcounter][ccounter] = new King(false, rcounter, ccounter);
                    blackKing = (King)pieces[rcounter][ccounter];
                }
                case 'p' -> {
                    pieces[rcounter][ccounter] = new Pawn(false, rcounter, ccounter);
                }
                //empty squares
                default -> {
                    int num = c-48;
                    for (int j = 0; j < num; j++) {
                        pieces[rcounter][ccounter] = new EmptySquare(rcounter, ccounter);
                        if (ccounter == 7) {
                            rcounter++;
                            ccounter = 0;
                        } else {
                            ccounter++;
                        }
                    }
                    //just decrement once so we can increment after switch
                    if (ccounter == 0) {
                        rcounter--;
                        ccounter = 7;
                    } else {
                        ccounter--;
                    }
                }
            }
            //increment and wrap if needed
            if (ccounter == 7) {
                rcounter++;
                ccounter = 0;
            } else {
                ccounter++;
            }
        }
        //whose move is it?
        FENString = FENString.substring(FENString.indexOf(" ") + 1);
        if (FENString.charAt(0) == 'w') {
            Board.whiteTurn = true;
        } else {
            Board.whiteTurn = false;
        }
        //castling rights
        FENString = FENString.substring(2);
        for (int i = 0; i < FENString.indexOf(" "); i++) {
            char c = FENString.charAt(i);
            switch (c) {
                case 'K' -> {
                    whiteKing.kingCastleRights = true;
                }
                case 'Q' -> {
                    whiteKing.queenCastleRights = true;
                }
                case 'k' -> {
                    blackKing.kingCastleRights = true;
                }
                case 'q' -> {
                    blackKing.queenCastleRights = true;
                }
            }
        }
        FENString = FENString.substring(FENString.indexOf(" ")+1);
        //now EP
        String EPSquare = FENString.substring(0,2);
        if (!EPSquare.equals("- ")) {
            EmptySquare epPiece = (EmptySquare)coordsToPiece(EPSquare, pieces);
            epPiece.enPassant = true;
            enPassantPiece = epPiece;
        }
        FENString = FENString.substring(FENString.indexOf(" ")+1);
        Board.halfMoveCount = Integer.parseInt(FENString.substring(0, FENString.indexOf(" ")));
        FENString = FENString.substring(FENString.indexOf(" ")+1);
        Board.fullMoveCount = Integer.parseInt(FENString);
        return pieces;
    }

    public ArrayList<Move> getAllWhiteMoves() {
        return getAllMoves(true);
    }

    public ArrayList<Move> getAllBlackMoves() {
        return getAllMoves(false);
    }

    public ArrayList<Move> getAllTurnMoves() {
        return getAllMoves(whiteTurn);
    }

    public boolean turnInCheck() {
        if (whiteTurn) {
            return whiteKing.isAttackedByBlack();
        } else {
            return blackKing.isAttackedByWhite();
        }
    }

    public boolean turnInCheckMate() {
        return turnInCheck() && getAllTurnMoves().size() == 0;
    }

    public boolean turnInStaleMate() {
        return !turnInCheck() && getAllTurnMoves().size() == 0;
    }

    public void throwCheckMate() {
        //checkmate
        dispose();
        String winner = whiteTurn ? "black" : "white";
        System.out.println("Checkmate for " + winner + ".");
        System.exit(0);
    }

    public void throwStaleMate() {
        //stalemate
        dispose();
        System.out.println("Stalemate.");
        System.exit(0);
    }

    public ArrayList<Move> getAllMoves(boolean white) {
        ArrayList<Move> totalMoves = new ArrayList<>();
        for (int i = 0; i < pieces.length; i++) {
            for (int j = 0; j < pieces[0].length; j++) {
                Piece p = pieces[i][j];
                if (!(p instanceof EmptySquare) && (p.white == white)) {
                    totalMoves.addAll(p.getLegalMoves());
                }
            }
        }
        return totalMoves;
    }

    public String getCastlingRights() {
        String rights = "";
        if (whiteKing.kingCastleRights) {
            rights += "K";
        }
        if (whiteKing.queenCastleRights) {
            rights += "Q";
        }
        if (blackKing.kingCastleRights) {
            rights += 'k';
        }
        if (blackKing.queenCastleRights) {
            rights += 'q';
        }
        return rights;
    }

    public String getFEN() {
        String fen = "";
        for (int i = 0; i < Board.pieces.length; i++) {
            for (int j = 0; j < Board.pieces[0].length; j++) {
                char c = Board.pieces[i][j].abbreviation;
                if (Board.pieces[i][j] instanceof EmptySquare) {
                    if (fen.length() == 0) {
                        fen += 1;
                        continue;
                    }
                    char previous = fen.charAt(fen.length()-1);
                    if (previous >= 49 && previous <= 56) {
                        //its a num
                        fen = fen.substring(0, fen.length()-1);
                        fen += (char)(previous+1);
                        continue;
                    } else {
                        fen += 1;
                        continue;
                    }
                }
                if (Board.pieces[i][j].white) {
                    c -= 32;
                }
                fen += c;
            }
            fen += '/';
        }
        //remove extra /
        fen = fen.substring(0, fen.length()-1);

        if (whiteTurn) {
            fen += " w ";
        } else {
            fen += " b ";
        }

        if (getCastlingRights().length() != 0) {
            fen += getCastlingRights() + " ";
        } else {
            fen += "- ";
        }
        
        EmptySquare e = enPassantPiece;
        if (e == null) {
            fen += "- ";
        } else {
            fen += pieceToCoords(e) + " ";
        }
        fen += halfMoveCount + " ";
        fen += fullMoveCount;

        return fen;
    }

    public String toString() {
        String s = "";
        for (int i = 0; i < pieces.length; i++) {
            for (int j = 0; j < pieces[0].length; j++) {
                s += String.format("%c(%d,%d) ", pieces[i][j].abbreviation, pieces[i][j].getR(), pieces[i][j].getC());
            }
            s += "\n";
        }
        return s;
    }
    
    /*
     * https://stackoverflow.com/a/3976656
     */
    public static int ordinalIndexOf(String str, String substr, int n) {
        int pos = str.indexOf(substr);
        while (--n > 0 && pos != -1)
            pos = str.indexOf(substr, pos + 1);
        return pos;
    }
}