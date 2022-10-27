package src;

import static org.junit.Assert.assertEquals;

import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.junit.Test;

public class GenerationTest {

    @Test
    public void testGenerate1() {
        System.out.println("Starting Test 1");
        Board b = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", null);
        double[] expected = new double[]{20,400,8902,197281,4865609};
        DecimalFormat df = new DecimalFormat("#");
        for (int i = 1; i < 6; i++) {
            LocalTime start = LocalTime.now();
            double positions = moveGeneration(b, i);
            LocalTime end = LocalTime.now();
            long ms = start.until(end, ChronoUnit.MILLIS);
            System.out.println(String.format("Depth: %d ply\tResult: %s positions      Time: %d milliseconds", i, df.format(positions), ms));
            assertEquals(expected[i-1], positions, 0);
        }
        System.out.println("\n");
    }

    @Test
    public void testGenerate2() {
        System.out.println("Starting Test 2");
        Board b = new Board("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", null);
        double[] expected = new double[]{48,2039,97862,4085603,193690690};
        DecimalFormat df = new DecimalFormat("#");
        for (int i = 1; i < 6; i++) {
            LocalTime start = LocalTime.now();
            double positions = moveGeneration(b, i);
            LocalTime end = LocalTime.now();
            long ms = start.until(end, ChronoUnit.MILLIS);
            System.out.println(String.format("Depth: %d ply\tResult: %s positions      Time: %d milliseconds", i, df.format(positions), ms));
            assertEquals(expected[i-1], positions, 0);
        }
        System.out.println("\n");
    }

    @Test
    public void testGenerate3() {
        System.out.println("Starting Test 3");
        Board b = new Board("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1", null);
        double[] expected = new double[]{14,191,2812,43238,674624};
        DecimalFormat df = new DecimalFormat("#");
        for (int i = 1; i < 6; i++) {
            LocalTime start = LocalTime.now();
            double positions = moveGeneration(b, i);
            LocalTime end = LocalTime.now();
            long ms = start.until(end, ChronoUnit.MILLIS);
            System.out.println(String.format("Depth: %d ply\tResult: %s positions      Time: %d milliseconds", i, df.format(positions), ms));
            assertEquals(expected[i-1], positions, 0);
        }
        System.out.println("\n");
    }

    @Test
    public void testGenerate4() {
        System.out.println("Starting Test 4");
        Board b = new Board("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", null);
        double[] expected = new double[]{6,264,9467,422333,15833292};
        DecimalFormat df = new DecimalFormat("#");
        for (int i = 1; i < 6; i++) {
            LocalTime start = LocalTime.now();
            double positions = moveGeneration(b, i);
            LocalTime end = LocalTime.now();
            long ms = start.until(end, ChronoUnit.MILLIS);
            System.out.println(String.format("Depth: %d ply\tResult: %s positions      Time: %d milliseconds", i, df.format(positions), ms));
            assertEquals(expected[i-1], positions, 0);
        }
        System.out.println("\n");
    }

    @Test
    public void testGenerate5() {
        System.out.println("Starting Test 5");
        Board b = new Board("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", null);
        double[] expected = new double[]{44,1486,62379,2103487,89941194};
        DecimalFormat df = new DecimalFormat("#");
        for (int i = 1; i < 6; i++) {
            LocalTime start = LocalTime.now();
            double positions = moveGeneration(b, i);
            LocalTime end = LocalTime.now();
            long ms = start.until(end, ChronoUnit.MILLIS);
            System.out.println(String.format("Depth: %d ply\tResult: %s positions      Time: %d milliseconds", i, df.format(positions), ms));
            assertEquals(expected[i-1], positions, 0);
        }
        System.out.println("\n");
    }

    @Test
    public void testGenerate6() {
        System.out.println("Starting Test 6");
        Board b = new Board("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", null);
        double[] expected = new double[]{46,2079,89890,3894594,164075551};
        DecimalFormat df = new DecimalFormat("#");
        for (int i = 1; i < 6; i++) {
            LocalTime start = LocalTime.now();
            double positions = moveGeneration(b, i);
            LocalTime end = LocalTime.now();
            long ms = start.until(end, ChronoUnit.MILLIS);
            System.out.println(String.format("Depth: %d ply\tResult: %s positions      Time: %d milliseconds", i, df.format(positions), ms));
            assertEquals(expected[i-1], positions, 0);
        }
        System.out.println("\n");
    }

    @Test
    public void testGenerate7() {
        System.out.println("Starting Test 7");
        Board b = new Board("r1bqkb1r/ppp2ppp/2n5/3np1N1/2B5/8/PPPP1PPP/RNBQK2R w KQkq - 0 6", null);
        double[] expected = new double[]{37,1506,55387,2242209,81677277};
        DecimalFormat df = new DecimalFormat("#");
        for (int i = 1; i < 6; i++) {
            LocalTime start = LocalTime.now();
            double positions = moveGeneration(b, i);
            LocalTime end = LocalTime.now();
            long ms = start.until(end, ChronoUnit.MILLIS);
            System.out.println(String.format("Depth: %d ply\tResult: %s positions      Time: %d milliseconds", i, df.format(positions), ms));
            assertEquals(expected[i-1], positions, 0);
        }
        System.out.println("\n");
    }

    public static void runGeneration(Board b, int depth) {
        for (int i = 1; i < depth+1; i++) {
            LocalTime start = LocalTime.now();
            int positions = moveGeneration(b, i);
            LocalTime end = LocalTime.now();
            long ms = start.until(end, ChronoUnit.MILLIS);
            System.out.println(String.format("Depth %d: %d (%d ms)", i, positions, ms));
        }
    }
    
    public static int moveGeneration(Board b, int depth) {
        if (depth == 0) {
            return 1;
        }

        int positions = 0;
        ArrayList<Move> moves = b.getAllTurnMoves();
        for (Move m : moves) {
            Move clone = new Move(m.startingPiece.clonePiece(), m.endingPiece.clonePiece(), m.promCharacter);
            // System.out.println("Before move:\n" + b);
            b.APIMove(m);
            // System.out.println("After move:\n" + b);
            int add = moveGeneration(b, depth-1);
            positions += add;
            b.APIUnMove(m, clone);
            // System.out.println("After unmove:\n" + b);
        }

        return positions;
    }
}
