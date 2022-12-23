package src.Testing;

import static org.junit.Assert.assertEquals;

import org.junit.*;

import src.Engine.*;
import src.Game.*;
import src.Pieces.*;

import java.io.*;
import java.util.*;


public class MoveNotationTest {
    @Test
    public void testNotation1() {
        Engine engine = new Engine();
        Board b = new Board(Piece.defaultFEN, engine);
        b.pack();
        b.setResizable(false);
        b.setLocationRelativeTo(null);
        b.setVisible(true);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            File f = new File("/Users/matthewstroup/Desktop/CS/PROJECTS/Chess/Games/Games.txt");
            FileReader fr = new FileReader(f);
            BufferedReader bfr = new BufferedReader(fr);
            ArrayList<String> whiteMoves = new ArrayList<>();
            ArrayList<String> blackMoves = new ArrayList<>();
            int randomRepeat = (int)(Math.random()*3000);
            while (randomRepeat-- > 0) {
                bfr.readLine();
            }
            Thread.sleep(2000);
            String game = bfr.readLine();
            String expected = game;
            boolean white = true;
            while (game.length() > 0) {
                if (game.indexOf(" ") == -1) {
                    if (white)
                        whiteMoves.add(game);
                    else
                        blackMoves.add(game);
                    break;
                }
                if (white)
                    whiteMoves.add(game.substring(0, game.indexOf(" ")));
                else
                    blackMoves.add(game.substring(0, game.indexOf(" ")));
                game = game.substring(game.indexOf(" ")+1);
                white = !white;
            }
            for (int j = 0; j < whiteMoves.size(); j++) {
                out:
                try {
                    if (whiteMoves.get(j).equals("1-0")) {
                        b.resignNoDispose(false);
                        break out;
                    } else if (whiteMoves.get(j).equals("0-1")) {
                        b.resignNoDispose(true);
                        break out;
                    } else if (whiteMoves.get(j).equals("1/2-1/2")) {
                        b.drawNoDispose();
                        break out;
                    }
                    b.remoteMove(new Move(whiteMoves.get(j)));
                    Thread.sleep(100);
                    if (blackMoves.get(j).equals("1-0")) {
                        b.resignNoDispose(false);
                        break out;
                    } else if (blackMoves.get(j).equals("0-1")) {
                        b.resignNoDispose(true);
                        break out;
                    } else if (blackMoves.get(j).equals("1/2-1/2")) {
                        b.drawNoDispose();
                        break out;
                    }
                    b.remoteMove(new Move(blackMoves.get(j)));
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                File log = new File("/Users/matthewstroup/Desktop/CS/PROJECTS/Chess/src/Game/log.txt");
                FileReader logfr = new FileReader(log);
                BufferedReader logbfr = new BufferedReader(logfr);
                String actual = logbfr.readLine();
                assertEquals(expected, actual);
                logbfr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bfr.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
