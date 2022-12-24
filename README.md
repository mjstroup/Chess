# Chess Engine

![Image](https://github.com/mjstroup/Images/blob/main/chess.png)

# Table of Contents
- [1. Overview](#1-overview)
- [2. Move Generation](#2-move-generation)
  - [2.1. Pins](#21-pins)
  - [2.2. Checks](#22-checks)
  - [2.3. En Passant Tricks](#23-en-passant-tricks)
  - [2.4. FEN Notation](#24-fen-notation)
  - [2.5. Testing](#25-testing)
- [3. Engine](#3-engine)
  - [3.1. Minimax](#31-minimax)
  - [3.2 Alpha-beta Pruning](#32-alpha-beta-pruning)
  - [3.3. Move Ordering](#33-move-ordering)
  - [3.4. Endgame Optimizations](#34-endgame-optimizations)
- [4. Usage](#4-usage)
- [5. Credits](#5-credits)
  

# 1. Overview

When I started this project, I didn't realize how deep of a rabbit hole chess programming was. There are articles and articles of algorithms, optimizations, and history on chessprogramming.org. Chess has been a passion of mine for over ten years, so it's really nice to pay tribute to it with this project.

While it was easy to find information on these topics, it was a great challenge and pleasure to implement them along with a custom-built object-oriented chess code base.

This project utilizes these algorithms and optimizations to create a chess AI that generates and evaluates positions through depth-based backtracking, and uses tools such as Minimax and Alpha-beta Pruning to perform optimal moves.

# 2. Move Generation

## 2.1. [Pins](https://en.wikipedia.org/wiki/Pin_(chess))

One of the trickiest parts about move generation in chess programming is dealing with pins. There are two types of pins that must be dealt with called absolute pins and partial pins. 

An absolute pin occurs when the pinned piece is unable to move completely without revealing its own king. These are most common when Knights are pinned, but can also occur when Bishops and Rooks are pinned along files/ranks and diagonals respectively. Queens can never be absolutely pinned, but will be restricted to movement in just one direction.

![Image](https://github.com/mjstroup/Images/blob/main/absolute_pin.png)

*The white Knight is absolutely pinned, completely unable to move.*

A partial pin occurs when a pinned piece is able to move partially without revealing its own king, but not completely. Conversely to absolute pins, partial pins occur most frequently when Queens are pinned, and will occur when Bishops and Rooks are pinned along ranks/files and diagonals respectively. Knights can never be partially pinned.

![Image](https://github.com/mjstroup/Images/blob/main/partial_pin.png)

*The white Bishop is partially pinned, able to move to c3 and capture on b4.*

Absolute pins are trivial to generate moves for, as they have no legal moves.

Partial pins are slightly harder, but still fairly simple.

The first step is to identify if a piece is pinned in the first place. The simplest way to do this is to first verify that the piece and its friendly King are on the same file, rank, or diagonal. If they are, verify that there are no friendly or hostile pieces between it and the King (otherwise, a pin is not possible). If they are, search in the opposite direction of the King for an enemy piece that is attacking the (possibly) pinned piece. If the attacking piece is found, the piece is pinned. If no attacking piece is found and the loop runs off the edge of the board, the piece is not pinned.

Once it is verified that the piece is pinned, the type of pin must be identified. For rooks, an absolute pin occurs when it is diagonally pinned, which occurs when (Rook.r - King.r) * (Rook.c - King.c) == 0. The inverse is true for bishops. Knights are always absolutely pinned, and Queens are always partially pinned. Absolutely pinned pieces are ignored, and an empty list is returned. For partially pinned pieces, a similar method is used. For loops run through all attacked directions of the pinned piece to verify where the piece can move, where the pin ends, and if the piece can capture the attacker.


## 2.2. [Checks](https://en.wikipedia.org/wiki/Check_(chess))

One of the original ways that I tried to deal with resolving and preventing checks from being encountered was by generating all [pseudo-legal moves](https://www.chessprogramming.org/Pseudo-Legal_Move), playing them on the board, verifying whether or not the move resulted in a check, then undoing the move. This process would be done for every possible pseudo-legal move for every piece, and proved to be extremely slow. 

From there, I decided to switch to a more manual approach to resolve/prevent checks whilst generating moves, which resulted in three cases.

1. The king is not in check, generate all possible moves and return.
2. The king is checked by one piece and has three ways of resolving the check.
   1. Capture the checking piece with a non-pinned piece.
   2. Block the check, if the checking piece is sliding (i.e. Bishop, Rook, or Queen), with a non-pinned piece.
   3. Move the king to a non-attacked square
3. The king is checked by two pieces, in which case the king must move to a non-attacked piece.

The only tricky part about this method is the "non-pinned piece" specification, which required some reworking of how moves were generated as described in [2.1](#21-pins).

It turns out that putting in the extra work to resolve checks the manual way was very much worth it, as it resulted in a more than 10x time save for [perft](https://www.chessprogramming.org/Perft) times.

## 2.3. [En Passant](https://en.wikipedia.org/wiki/En_passant) Tricks

There are a couple of situations that arise involving the move En Passant that can cause possible problems. The most common cause of these problems occurs because En Passant is the only move where the piece being captured is not the piece that the attacker is traveling to.

![Image](https://github.com/mjstroup/Images/blob/main/EP1.png)

*The white Pawn on d4 checks the black King, and exd4 EP resolves the check.*

For this specific situation, I did not have the Pawn on e4 listed as an attacker for the d4 pawn, as En Passant is a temporary move that comes and goes in one [ply](https://en.wikipedia.org/wiki/Ply_(game_theory)). As a solution, I simply added a couple of lines in the check handling section of the legal move generator to verify if En Passant is possible, and adding it to the list of legal moves if it is.

The second and more interesting case occurs when an En Passant results in a [discovered check](https://en.wikipedia.org/wiki/Discovered_attack). 

![Image](https://github.com/mjstroup/Images/blob/main/EP2.png)

*The white Pawn cannot legally perform cxd5 EP, as it reveals the black Rook's attack on the white King.*

It turns out that there are only eight cases per color where this can occur. With the first letter of the piece's name (lowercase for black, uppercase for white) representing the piece, the eight patterns for white are as follows: KpPq, KpPr, KPpq, KPpr, qpPK, rpPK, qPpK, rPpK. Note that stray pieces on either side of the pattern are not relevant, but pieces inside of the pattern are. The way I solved this case was to generate this type of notation for the specific line, and if it matched one of the eight illegal patterns, the En Passant is not valid. In retrospect, it may have been easier to consider the c5 pawn pinned, but this was a very fun solution to implement and see succeed.

## 2.4. [FEN Notation](https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation)

FEN notation is one of the best ways to represent a chess game's state in a compact way. The notation essentially represents the game board, tokenized by a / for each row, with some extra metadata such as castling rights, En Passant state, and half/full move status.

This notation was a great quality-of-life addition, as it allowed me to quickly load in specific games to debug and test. It was also essential to certain features such as [threefold repetition](https://en.wikipedia.org/wiki/Threefold_repetition).

## 2.5. Testing

To ensure all of the game's niche rules were followed accurately, performance testing was done using [various positions](https://www.chessprogramming.org/Perft_Results) to a depth of five ply.


    int perft(Board b, int depth) {
        int positions = 0;
        List<Move> moves = b.getMoves();

        if (depth == 1) {
            return moves.size();
        }
        for (Move m : moves) {
            b.move(m);
            positions += perft(b, depth-1);
            b.unMove(m);
        }
        return positions;
    }

The perft algorithm utilizes depth-based backtracking to explore all possible positions up to a certain depth. The added if statement is an optimization called bulk-counting, which essentially skips depth 0 searching, as it will always return moves.size().

These tests are available to see in [src/Testing](https://github.com/mjstroup/Chess/blob/master/src/Testing/GenerationTest.java). 

# 3. Engine

## 3.1. [Minimax](https://en.wikipedia.org/wiki/Minimax)

Minimax is a technique used in zero-sum games that utilizes a static evaluation function to attempt to **mini**mize the **max**imum loss that the opponent can inflict. In the case where white moves first, white is the maximizing player and black is the minimizing player. 

When we would like to evaluate a move for white, minimax will attempt to minimize black's responses. This is done because a position that is good for black is bad for white, therefore minimizing black's evaluation will maximize white's evaluation, even if it is a negative one. This continues down the move tree until it reaches a certain depth, where it statically evaluates the position, and then sends the values up the tree to be minimaxed.
 
Minimax does however waste a bunch of time calculating lines it does not have to. For example, if white wants to evaluate a move to depth 4 where black has two possible responses, and the left tree comes out to be +5 for white, when calculating the right tree, if there comes a point where black has a better evaluation than +5, white will end up picking the left move no matter what the rest of the right tree evaluates out to be, therefore it can be pruned. A technique called Alpha-beta pruning can be used to optimize these time losses.

## 3.2. [Alpha-beta Pruning](https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning)

![Image](https://github.com/mjstroup/Images/blob/main/a-bp.png)

Alpha-beta pruning is an optimization done to minimax in which two values, alpha and beta, representing the minimum score that the maximizing player is guaranteed and the maximum score that the minimizing player is guaranteed respectively are stored. Starting out, alpha is negative infinity and beta is positive infinity, as neither player is assured of any score.

As the move tree is filled out, whenever the maximizing player encounters a value that is greater than beta, it is ensured that the optimal move does not belong to this branch, as the minimizing player will never opt to choose a potentially larger-scored branch within the move tree when it can choose its guaranteed worst-case maximum score, beta. Therefore, the rest of the branch does not need to be calculated and can be pruned. 

Similarly, whenever the minimizing player encounters a value that is less than alpha, it is ensured that the optimal move does not belong to this branch. 

The optimizations that occur with Alpha-beta pruning are somewhat luck based, as it is far more beneficial to find an optimal move early on in minimax, as it allows for more pruned branches. By ordering the list of moves by how likely they are to be good, Alpha-beta pruning is heavily optimized.

## 3.3. [Move Ordering](https://www.chessprogramming.org/Move_Ordering)

Move ordering is extremely simple to do for how effective of an optimization it is. Each move gets assigned a "score", and then the moves are sorted based on their score and then evaluated by Alpha-beta pruning. 

The way I implemented move ordering is by incentivising a couple of behaviors such as capturing a high valued piece with a low valued piece, promoting pawns, and capturing pieces with pawns. This could be further optimized by incentivising checks as well.

## 3.4. Endgame Optimizations

# 4. Usage

Clone/download the repository and run the main method inside src/Main.java. Change the engine's depth with the constructor.

For perft testing, uncomment runGeneration() in src/Main.java and change the depth in the parameters.

# 5. Credits

* [Matthew Stroup](https://github.com/mjstroup)
  * [LinkedIn](https://www.linkedin.com/in/mjstroup)
  * mjstroup@purdue.edu


