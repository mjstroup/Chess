import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;

public class Board extends JFrame  implements MouseListener, MouseMotionListener{
    final Color light = new Color(240,217,181);
    final Color dark = new Color(181,136,99);
    final Color lightCover = new Color(174,177,136);
    final Color darkCover = new Color(133,120,78);

    Piece[][] pieces;
    JLayeredPane layeredPane;
    JPanel chessBoard;
    JLabel piece;
    JPanel currentPanel;

    public Board(Piece[][] pieces) {
        this.pieces = pieces;
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
                if ((r + c) % 2 == 0) {
                    square.setBackground(light);
                } else {
                    square.setBackground(dark);
                }
                if (pieces[r][c] != null) {
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
        if (isWhitePanel(currentPanel)) {
            currentPanel.setBackground(lightCover);
        } else {
            currentPanel.setBackground(darkCover);
        }
    }

    public void mouseDragged(MouseEvent me) {
        if (!SwingUtilities.isLeftMouseButton(me)) return;
        if (piece == null) return;
        piece.setLocation(me.getX() - 49, me.getY() - 45);
        Component c = chessBoard.findComponentAt(me.getX(), me.getY());
        if (!currentPanel.equals(c)) {
            if (isWhitePanel(currentPanel)) {
                currentPanel.setBackground(light);
            } else {
                currentPanel.setBackground(dark);
            }
        }
        currentPanel = (JPanel)c;
        if (c instanceof JPanel) {
            if (isWhitePanel(c)) {
                c.setBackground(lightCover);
            } else {
                c.setBackground(darkCover);
            }
        }
    }

    public void mouseReleased(MouseEvent me) {
        if (!SwingUtilities.isLeftMouseButton(me)) return;
        if (piece == null) return;
        piece.setVisible(false);
        Component c = chessBoard.findComponentAt(me.getX(), me.getY());
        if (c instanceof JLabel) {
            Container parent = c.getParent();
            parent.remove(0);
            parent.add(piece);
        } else {
            Container parent = (Container)c;
            parent.add(piece);
        }
        piece.setVisible(true);
        if (isWhitePanel(currentPanel)) {
            currentPanel.setBackground(light);
        } else {
            currentPanel.setBackground(dark);
        }
        piece = null;
        currentPanel = null;
    }

    public void mouseClicked(MouseEvent me) {}
    public void mouseMoved(MouseEvent me) {}
    public void mouseEntered(MouseEvent me) {}
    public void mouseExited(MouseEvent e) {}

    public Piece componentToPiece(Component c) {
        return pieces[c.getY()/99][c.getX()/99];
    }
    public boolean isWhitePanel(Component c) {
        return (c.getX()/99 + c.getY()/99) % 2 == 0;
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