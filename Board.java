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
    Piece currentPiece;
    ArrayList<Piece> currentMoves;
    JLayeredPane layeredPane;
    JPanel chessBoard;
    JLabel piece;
    JPanel currentPanel;
    JPanel originalPanel;

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

        
        layeredPane.repaint();
        layeredPane.revalidate();
        this.repaint();
        this.revalidate();
        chessBoard.repaint();
        chessBoard.revalidate();
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
        currentPiece = componentToPiece(currentPanel);
        currentMoves = currentPiece.getPossibleMoves();
    }

    public void mouseDragged(MouseEvent me) {
        if (!SwingUtilities.isLeftMouseButton(me)) return;
        if (piece == null) return;
        piece.setLocation(me.getX() - 49, me.getY() - 45);
        Component c = chessBoard.findComponentAt(me.getX(), me.getY());
        if (!currentPanel.equals(c))
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
        // System.out.println(currentMoves);
        // System.out.println(currentPanelPiece);
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
        piece = null;
        currentPiece = null;
        currentPanel = null;
        currentMoves = null;
        originalPanel = null;
        if (!returned)
            whiteTurn = !whiteTurn;
    }

    public void movePiece(Piece movingPiece, Piece destination) {
        if (movingPiece.equals(destination)) return;
        if (movingPiece instanceof Pawn && (destination.getX() == 0 || destination.getX() == 7)) {
            //TODO: under promotion
            pieces[destination.getX()][destination.getY()] = new Queen(destination.getX() == 0, 0, destination.getY());
            currentPiece = pieces[destination.getX()][destination.getY()];
            Image image = (new ImageIcon(currentPiece.fileName)).getImage();
            image = image.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
            piece.setIcon(new ImageIcon(image));
        }
        else 
            pieces[destination.getX()][destination.getY()] = pieces[movingPiece.getX()][movingPiece.getY()];
        pieces[movingPiece.getX()][movingPiece.getY()] = new EmptySquare(movingPiece.getX(), movingPiece.getY());
        movingPiece.setLocation(destination.getX(), destination.getY());
    }
    public String pieceToCoords(Piece p) {
        int x = p.getX();
        int y = p.getY();
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