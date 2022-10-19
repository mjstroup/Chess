import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

public class Board extends JFrame  implements MouseListener, MouseMotionListener{
    final Color light = new Color(240,217,181);
    final Color dark = new Color(181,136,99);
    final Color lightCover = new Color(174,177,136);
    final Color darkCover = new Color(133,120,78);

    public static boolean whiteTurn = true;
    public static Piece[][] pieces;
    private Piece currentPiece;
    private ArrayList<Piece> currentMoves;
    private JLayeredPane layeredPane;
    private JPanel chessBoard;
    private JLabel piece;
    private JPanel currentPanel;
    private JPanel originalPanel;

    public Board(Piece[][] pieceList) {
        pieces = pieceList;
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
        originalPanel = (JPanel)chessBoard.findComponentAt(me.getX(), me.getY());
        if (isWhitePanel(originalPanel))
            originalPanel.setBackground(lightCover);
        else
            originalPanel.setBackground(darkCover);
        currentPiece = componentToPiece(currentPanel);
        currentMoves = currentPiece.getPossibleMoves();
        System.out.println(currentPiece);
        System.out.println(currentMoves);
    }

    public void mouseDragged(MouseEvent me) {
        if (!SwingUtilities.isLeftMouseButton(me)) return;
        if (piece == null) return;
        piece.setLocation(me.getX() - 49, me.getY() - 45);
        Component c = chessBoard.findComponentAt(me.getX(), me.getY());
        if (!currentPanel.equals(c) && !currentPanel.equals(originalPanel))
            if (isWhitePanel(currentPanel))
                currentPanel.setBackground(light);
            else 
                currentPanel.setBackground(dark);
        Piece currentPanelPiece = componentToPiece(c);
        if (c instanceof JLabel) {
            currentPanel = (JPanel)c.getParent();
            currentPanelPiece = componentToPiece(c.getParent());
        } else {
            currentPanel = (JPanel)c;
        }
        if (currentPanel instanceof JPanel)
            if (currentPanelPiece != null && currentMoves.contains(currentPanelPiece))
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
        if(!currentMoves.contains(destination)) {
            currentPanel = originalPanel;
            c = originalPanel;
            destination = componentToPiece(c);
            returned = true;
        }
        piece.setVisible(false);
        if (c instanceof JLabel) {
            Container parent = c.getParent();
            c = c.getParent();
            parent.remove(0);
            parent.add(piece);
        } else {
            Container parent = (Container)c;
            parent.add(piece);
        }
        piece.setVisible(true);
        movePiece(movingPiece, destination);

        System.out.println(movingPiece.abbreviation + pieceToCoords(destination));

        if (isWhitePanel(currentPanel))
            currentPanel.setBackground(light);
        else
            currentPanel.setBackground(dark);
        if (isWhitePanel(originalPanel))
            originalPanel.setBackground(light);
        else
            originalPanel.setBackground(dark);
        piece = null;
        currentPiece = null;
        currentPanel = null;
        currentMoves = null;
        originalPanel = null;
        if (!returned) {
            whiteTurn = !whiteTurn;
            // checkmate check
            boolean checkmate = true;
            boolean stalemate = false;
            for (int i = 0; i < pieces.length; i++) {
                for (int j = 0; j < pieces[0].length; j++) {
                    Piece p = pieces[i][j];
                    if (p.white != movingPiece.white && p.getPossibleMoves().size() != 0) {
                        checkmate = false;
                        stalemate = false;
                    }
                    if (p.white != movingPiece.white && p instanceof King)
                        if (p.white && !p.isAttackedByBlack())
                            stalemate = true;
                        else if (!p.isAttackedByWhite())
                            stalemate = true;
                }
            }
            if (checkmate && stalemate) {
                //stalemate
                dispose();
                System.out.println("Stalemate.");
            } else if (checkmate) {
                //checkmate
                dispose();
                String winner = whiteTurn ? "black" : "white";
                System.out.println("Checkmate for " + winner + ".");
            }
        }
    }

    public void movePiece(Piece movingPiece, Piece destination) {
        if (movingPiece.equals(destination)) return;
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
            //TODO: under promotion
            pieces[destination.getR()][destination.getC()] = new Queen(destination.getR() == 0, 0, destination.getC());
            currentPiece = pieces[destination.getR()][destination.getC()];
            Image image = (new ImageIcon(currentPiece.fileName)).getImage();
            image = image.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
            piece.setIcon(new ImageIcon(image));
        } else 
            pieces[destination.getR()][destination.getC()] = pieces[movingPiece.getR()][movingPiece.getC()];
        //now move
        pieces[movingPiece.getR()][movingPiece.getC()] = new EmptySquare(movingPiece.getR(), movingPiece.getC());
        movingPiece.setLocation(destination.getR(), destination.getC());
    }
    public String pieceToCoords(Piece p) {
        int x = p.getR();
        int y = p.getC();
        String s = "";
        switch (y) {
            case 0: s+="A"; break;
            case 1: s+="B"; break;
            case 2: s+="C"; break;
            case 3: s+="D"; break;
            case 4: s+="E"; break;
            case 5: s+="F"; break;
            case 6: s+="G"; break;
            case 7: s+="H"; break;
        }
        s += (8-x) + "";
        return s;
    }
    public String xyToCoords(int x, int y) {
        String s = "";
        switch (y) {
            case 0: s+="A"; break;
            case 1: s+="B"; break;
            case 2: s+="C"; break;
            case 3: s+="D"; break;
            case 4: s+="E"; break;
            case 5: s+="F"; break;
            case 6: s+="G"; break;
            case 7: s+="H"; break;
        }
        s += (8-x) + "";
        return s;
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
    
    public static void main(String[] args) {
        Board b = new Board(Piece.newBoard);
        b.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        b.pack();
        b.setResizable(false);
        b.setLocationRelativeTo(null);
        b.setVisible(true);
    }
}