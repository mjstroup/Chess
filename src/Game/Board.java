package src.Game;

import src.Engine.Engine;
import src.Pieces.Bishop;
import src.Pieces.EmptySquare;
import src.Pieces.King;
import src.Pieces.Knight;
import src.Pieces.Pawn;
import src.Pieces.Piece;
import src.Pieces.Queen;
import src.Pieces.Rook;

import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.border.*;
import javax.sound.sampled.*;

public class Board extends JFrame implements MouseListener, MouseMotionListener {
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
    public static String gameLog = "";
    public int eval;
    public boolean canPickUp = true;
    /*
     * 0 -> game is in session
     * 1 -> white won
     * 2 -> black won
     * 3 -> stalemate
     */
    public int gameOver = 0;
    private static Engine engine;
    private static int halfMoveCount = 0;
    private static int fullMoveCount = 1;
    private static HashMap<String, Integer> repeatMap;
    private Piece currentPiece;
    private ArrayList<Move> currentMoves;
    private JLayeredPane layeredPane;
    private JPanel chessBoard;
    private JPanel whiteEvalPanel;
    private JPanel blackEvalPanel;
    private JPanel originalPanel;
    private JPanel currentPanel;
    private Color currentPanelColor;
    private Color originalPanelColor;
    private JPanel previousMoveOriginalPanel;
    private JPanel previousMoveCurrentPanel;
    private JLabel piece;
    private boolean moveIsCapture = false;
    private Stack<Gamestate> previousGamestates;
    
    public Board(String FENString, Engine e) {
        engine = e;
        eval = 0;
        repeatMap = new HashMap<>();
        repeatMap.put(FENString.substring(0, Board.ordinalIndexOf(FENString, " ", 4)), 1);
        previousGamestates = new Stack<>();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pieces = fenStringToPieces(FENString);

        Dimension layeredSize = new Dimension(830,800);
        Dimension size = new Dimension(800,800);
        layeredPane = new JLayeredPane();
        getContentPane().add(layeredPane);
        layeredPane.setPreferredSize(layeredSize);
        layeredPane.addMouseListener(this);
        layeredPane.addMouseMotionListener(this);

        whiteEvalPanel = new JPanel();
        blackEvalPanel = new JPanel();
        
        whiteEvalPanel.setBackground(Color.WHITE);
        blackEvalPanel.setBackground(Color.BLACK);

        whiteEvalPanel.setBorder(new LineBorder(Color.BLACK));
        blackEvalPanel.setBorder(new LineBorder(Color.BLACK));
        
        updateEvalPanels(eval);

        layeredPane.add(whiteEvalPanel);
        layeredPane.add(blackEvalPanel);

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

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
        Board b = this;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                if (engine != null && engine.white) {
                    engine.playMove(b);
                }
            }
        });
        t.start();
    }

    public void updateEvalPanels(double eval) {
        whiteEvalPanel.removeAll();
        blackEvalPanel.removeAll();
        //checkmate and stalemate
        if (gameOver == 1) {
            eval = 1000000;
        } else if (gameOver == 2) {
            eval = -1000000;
        }
        if (gameOver == 3) {
            Dimension whiteEvalSize = new Dimension(30, 400);
            Dimension blackEvalSize = new Dimension(30, 400);
            whiteEvalPanel.setPreferredSize(whiteEvalSize);
            blackEvalPanel.setPreferredSize(blackEvalSize);

            blackEvalPanel.setBounds(800, 0, blackEvalSize.width, blackEvalSize.height);
            whiteEvalPanel.setBounds(800, blackEvalSize.height, whiteEvalSize.width, whiteEvalSize.height);
            JLabel whiteEvalLabel = new JLabel("1/2");
            JLabel blackEvalLabel = new JLabel("1/2");
            whiteEvalLabel.setForeground(Color.BLACK);
            blackEvalLabel.setForeground(Color.WHITE);
            whiteEvalPanel.add(whiteEvalLabel);
            blackEvalPanel.add(blackEvalLabel);
            
            return;
        }
        if (eval <= -900000) {
            //m black
            Dimension whiteEvalSize = new Dimension(30, 0);
            Dimension blackEvalSize = new Dimension(30, 800);
            whiteEvalPanel.setPreferredSize(whiteEvalSize);
            blackEvalPanel.setPreferredSize(blackEvalSize);

            blackEvalPanel.setBounds(800, 0, blackEvalSize.width, blackEvalSize.height);
            whiteEvalPanel.setBounds(800, blackEvalSize.height, whiteEvalSize.width, whiteEvalSize.height);
            String numTurns = "M" + (int)(1000000+eval);
            if (gameOver == 2) numTurns = "1";
            JLabel blackEvalLabel = new JLabel(numTurns);
            blackEvalLabel.setForeground(Color.WHITE);
            blackEvalPanel.add(blackEvalLabel);
            return;
        }
        if (eval >= 900000) {
            //m white
            Dimension whiteEvalSize = new Dimension(30, 800);
            Dimension blackEvalSize = new Dimension(30, 0);
            whiteEvalPanel.setPreferredSize(whiteEvalSize);
            blackEvalPanel.setPreferredSize(blackEvalSize);

            blackEvalPanel.setBounds(800, 0, blackEvalSize.width, blackEvalSize.height);
            whiteEvalPanel.setBounds(800, blackEvalSize.height, whiteEvalSize.width, whiteEvalSize.height);
            String numTurns = "M" + (int)(1000000-eval);
            if (gameOver == 1) numTurns = "1";
            JLabel whiteEvalLabel = new JLabel(numTurns);
            whiteEvalLabel.setForeground(Color.BLACK);
            whiteEvalPanel.add(whiteEvalLabel);
            return;
        }
        eval/=100;
        //+10 should map to 750, -10 should map to 50
        //400 + (eval*35)
        int whiteSize = (int)Math.max(50, 400+(eval*35));
        whiteSize = Math.min(whiteSize, 750);
        int blackSize = 800-whiteSize;
        Dimension whiteEvalSize = new Dimension(30, whiteSize);
        Dimension blackEvalSize = new Dimension(30, blackSize);

        whiteEvalPanel.setPreferredSize(whiteEvalSize);
        blackEvalPanel.setPreferredSize(blackEvalSize);

        blackEvalPanel.setBounds(800, 0, blackEvalSize.width, blackEvalSize.height);
        whiteEvalPanel.setBounds(800, blackEvalSize.height, whiteEvalSize.width, whiteEvalSize.height);

        String whiteFormat;
        String blackFormat; 
        if (eval >= 10 || eval <= -10) {
            DecimalFormat df = new DecimalFormat("00.0");
            whiteFormat = df.format(eval);
            blackFormat = df.format(-eval);
        } else {
            DecimalFormat df = new DecimalFormat("0.00");
            whiteFormat = df.format(eval);
            blackFormat = df.format(-eval);
        }

        if (eval == 0) {
            JLabel whiteEvalLabel = new JLabel("0.0");
            JLabel blackEvalLabel = new JLabel("0.0");
            whiteEvalLabel.setForeground(Color.BLACK);
            blackEvalLabel.setForeground(Color.WHITE);
            whiteEvalPanel.add(whiteEvalLabel);
            blackEvalPanel.add(blackEvalLabel);
        } else if (whiteSize > blackSize) {
            JLabel whiteEvalLabel = new JLabel(whiteFormat);
            whiteEvalLabel.setForeground(Color.BLACK);
            whiteEvalPanel.add(whiteEvalLabel);
        } else {
            JLabel blackEvalLabel = new JLabel(blackFormat);
            blackEvalLabel.setForeground(Color.WHITE);
            blackEvalPanel.add(blackEvalLabel);
        }
    }

    public void mousePressed(MouseEvent me) {
        if (gameOver != 0) return;
        if (!SwingUtilities.isLeftMouseButton(me)) return;
        if (!canPickUp) return;
        if (me.getX() > 780 || me.getY() > 780 || me.getX() < 20 || me.getY() < 20) return;

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
    }

    public void mouseDragged(MouseEvent me) {
        //out of bounds
        if (gameOver != 0) return;
        if (!SwingUtilities.isLeftMouseButton(me)) return;
        if (!canPickUp) return;
        if (piece == null) return;
        if (me.getX() > 780 || me.getY() > 780 || me.getX() < 20 || me.getY() < 20) return;
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
        if (gameOver != 0) return;
        if (!SwingUtilities.isLeftMouseButton(me)) return;
        if (piece == null) return;
        if (me.getX() > 780 || me.getY() > 780 || me.getX() < 20 || me.getY() < 20) {
            //out of bounds, return to original state
            piece.setVisible(false);
        
            originalPanel.add(piece);

            piece.setVisible(true);
            currentPanel.setBackground(currentPanelColor);
            originalPanel.setBackground(originalPanelColor);
            piece = null;
            currentPiece = null;
            currentPanel = null;
            currentPanelColor = null;
            currentMoves = null;
            originalPanel = null;
            moveIsCapture = false;
            return;
        }
        canPickUp = false;
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
        Move validMoveClone = new Move(validMoveMove.startingPiece.clonePiece(), validMoveMove.endingPiece.clonePiece(), validMoveMove.promCharacter);
        movePiece(validMoveMove);

        currentPanel.setBackground(currentPanelColor);
        originalPanel.setBackground(originalPanelColor);
        //did the move really happen..

        if (!returned) {
            postMove(validMoveClone);
        }
        piece = null;
        currentPiece = null;
        currentPanel = null;
        currentPanelColor = null;
        currentMoves = null;
        originalPanel = null;
        moveIsCapture = false;
        if (!returned) {
            Board b = this;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (engine != null) {
                        if (gameOver == 0)
                            engine.playMove(b);
                        updateEvalPanels(eval);
                        repaint();
                        revalidate();
                    }
                }
            });
            t.start();
        } else {
            canPickUp = true;
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
            Component EPpawn = pieceToComponent(pieces[passantPiece.getR()][passantPiece.getC()]);
            Container parent;
            if (EPpawn instanceof JPanel) {
                parent = (Container)(EPpawn);
            } else {
                parent = EPpawn.getParent();
            }
            parent.remove(0);
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

    public void postMove(Move move) {
        Piece movingPiece = move.startingPiece;
        
        whiteTurn = !whiteTurn;
        //play sound
        if (moveIsCapture)
            playSound("../../Sounds/Capture.wav");
        else
            playSound("../../Sounds/Move.wav");
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
        
        //3 move stalemate
        String fen = this.getFEN();
        fen = fen.substring(0, Board.ordinalIndexOf(fen, " ", 4));
        if (repeatMap.get(fen) != null) {
            repeatMap.put(fen, repeatMap.get(fen)+1);
        } else {
            repeatMap.put(fen, 1);
        }
        //stalemate checks
        //50 move rule
        if (halfMoveCount == 100) {
            throwStaleMate(move);
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
            throwStaleMate(move);
        }
        //2 kings + bishop or knight
        if ((whitePieces.size() == 1 || blackPieces.size() == 1) && (whitePieces.size() == 2 || blackPieces.size() == 2)) {
            if (blackPieces.size() == 2) {
                if (blackPieces.get(0) instanceof Bishop || blackPieces.get(1) instanceof Bishop) {
                    throwStaleMate(move);
                }
                if (blackPieces.get(0) instanceof Knight || blackPieces.get(1) instanceof Knight) {
                    throwStaleMate(move);
                }
            }
            if (whitePieces.size() == 2) {
                if (whitePieces.get(0) instanceof Bishop || whitePieces.get(1) instanceof Bishop) {
                    throwStaleMate(move);
                }
                if (whitePieces.get(0) instanceof Knight || whitePieces.get(1) instanceof Knight) {
                    throwStaleMate(move);
                }
            }
        }
        //2 kings + 2 bishop same color
        if (whitePieces.size() == 2 && blackPieces.size() == 2) {
            if ((whitePieces.get(0) instanceof Bishop || whitePieces.get(1) instanceof Bishop) && (blackPieces.get(0) instanceof Bishop || blackPieces.get(1) instanceof Bishop)) {
                Bishop white = whitePieces.get(0) instanceof King ? (Bishop)whitePieces.get(1) : (Bishop)whitePieces.get(0);
                Bishop black = blackPieces.get(0) instanceof King ? (Bishop)blackPieces.get(1) : (Bishop)blackPieces.get(0);
                if ((white.getR() + white.getC()) % 2 == (black.getR() + black.getC()) % 2) {
                    throwStaleMate(move);
                }
            }
        }
        //checkmate check
        if (turnInCheckMate()) {
            throwCheckMate(move);
        } else if (turnInStaleMate()) {
            throwStaleMate(move);
        } else if (turnInCheck()) {
            move.check = true;
        }
        gameLog += move + " ";
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

        Move moveClone = new Move(move.startingPiece.clonePiece(), move.endingPiece.clonePiece(), move.promCharacter);
        movePiece(move);
        
        this.originalPanel = startingPanel;
        this.currentPanel = endingPanel;

        postMove(moveClone);
        
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
        previousGamestates.push(new Gamestate(whiteTurn, new HashMap<String, Integer>(repeatMap), halfMoveCount, fullMoveCount, whiteKing.kingCastleRights, whiteKing.queenCastleRights, blackKing.kingCastleRights, blackKing.queenCastleRights, enPassantPiece, value, moveIsCapture));
        APImovePiece(move);
        whiteTurn = !whiteTurn;
        String fen = getFEN();
        fen = fen.substring(0, Board.ordinalIndexOf(fen, " ", 4));
        if (repeatMap.get(fen) != null) {
            repeatMap.put(fen, repeatMap.get(fen)+1);
        } else {
            repeatMap.put(fen, 1);
        }
    }

    public void APIUnMove(Move move, Move clone) {
        Gamestate g = previousGamestates.pop();
        Piece movingPiece = move.startingPiece;
        Piece destination = move.endingPiece;
        if (clone.startingPiece instanceof King && Math.abs(clone.startingPiece.getC() - clone.endingPiece.getC()) > 1) {
            //last move was a castle
            int row = movingPiece.rlocation;
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
        String fen = getFEN();
        fen = fen.substring(0, Board.ordinalIndexOf(fen, " ", 4));
        if (repeatMap.get(fen) != null && repeatMap.get(fen) == 3) {
            return true;
        }
        return !turnInCheck() && getAllTurnMoves().size() == 0;
    }

    public void throwCheckMate(Move move) {
        //write to game log
        move.checkMate = true;
        gameLog += move + " ";
        gameLog += whiteTurn ? "0-1" : "1-0";
        writeGameLog();
        //announce in console
        String winner = whiteTurn ? "black" : "white";
        System.out.println("Checkmate for " + winner + ".");
        //dispose
        gameOver = whiteTurn ? 2 : 1;
    }

    public void throwStaleMate(Move move) {
        //write to game log
        gameLog += move + " 1/2-1/2";
        writeGameLog();
        //announce in console
        System.out.println("Stalemate.");
        //dispose
        gameOver = 3;
    }

    public void resign(boolean white) {
        if (white)
            gameLog += "0-1";
        else
            gameLog += "1-0";
        writeGameLog();
        String loser = white ? "White" : "Black";
        System.out.println(loser + " resigned.");
        //dispose
        dispose();
        System.exit(0);
    }

    public void resignNoDispose(boolean white) {
        if (white)
            gameLog += "0-1";
        else
            gameLog += "1-0";
        writeGameLog();
        String loser = white ? "White" : "Black";
        System.out.println(loser + " resigned.");
    }

    public void draw() {
        gameLog += "1/2-1/2";
        writeGameLog();
        System.out.println("Players agreed to a draw.");
        //dispose
        dispose();
        System.exit(0);
    }

    public void drawNoDispose() {
        gameLog += "1/2-1/2";
        writeGameLog();
        System.out.println("Players agreed to a draw.");
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
            fen += e + " ";
        }
        fen += halfMoveCount + " ";
        fen += fullMoveCount;

        return fen;
    }

    public int getFullMoveCount() {
        return Board.fullMoveCount;
    }

    public HashMap<String, Integer> getRepeatMap() {
        return repeatMap;
    }

    public void writeGameLog() {
        try {
            File f = new File("src/Game/log.txt");
            FileWriter fw = new FileWriter(f);
            BufferedWriter bfw = new BufferedWriter(fw);
            bfw.write(gameLog);
            bfw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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