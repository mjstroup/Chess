# Chess Engine

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
  - [3.4. Opening Book](#34-opening-book)
  - [3.5. Piece Square Tables](#35-piece-square-tables)
  - [3.6. Endgame Optimizations](#36-endgame-optimizations)
- [4. Overall Performance](#4-overall-performance)
- [5. Usage](#5-usage)
- [6. Credits](#6-credits)
 
# 1. Overview

![Image](https://github.com/mjstroup/Images/blob/main/Chess/chess.png)

When I started this project, I didn't realize how deep of a rabbit hole chess programming was. There are articles and articles of algorithms, optimizations, and history on chessprogramming.org. Chess has been a passion of mine for over ten years, so it's really nice to pay tribute to it with this project.

While it was easy to find information on these topics, it was a great challenge and pleasure to implement them along with a custom-built object-oriented chess code base.

This project utilizes these algorithms and optimizations to create a chess AI that generates and evaluates positions through depth-based backtracking, and uses tools such as Minimax and Alpha-beta Pruning to perform optimal moves.

# 2. Move Generation

## 2.1. [Pins](https://en.wikipedia.org/wiki/Pin_(chess))

One of the trickiest parts about move generation in chess programming is dealing with pins. There are two types of pins that must be dealt with called absolute pins and partial pins. 

An absolute pin occurs when the pinned piece is unable to move completely without revealing its own king. These are most common when Knights are pinned, but can also occur when Bishops and Rooks are pinned along files/ranks and diagonals respectively. Queens can never be absolutely pinned, but will be restricted to movement in just one direction.

![Image](https://github.com/mjstroup/Images/blob/main/Chess/absolute_pin.png)

*The white Knight is absolutely pinned, completely unable to move.*

A partial pin occurs when a pinned piece is able to move partially without revealing its own king, but not completely. Conversely to absolute pins, partial pins occur most frequently when Queens are pinned, and will occur when Bishops and Rooks are pinned along ranks/files and diagonals respectively. Knights can never be partially pinned.

![Image](https://github.com/mjstroup/Images/blob/main/Chess/partial_pin.png)

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
   1. Capture the checking piece with a non-absolutely pinned piece.
   2. Block the check, if the checking piece is sliding (i.e. Bishop, Rook, or Queen), with a non-pinned piece.
   3. Move the king to a non-attacked square
3. The king is checked by two pieces, in which case the king must move to a non-attacked piece, note that sliding pieces x-ray the king.

The only tricky part about this method is the "non-pinned piece" specification, which required some reworking of how moves were generated as described in [2.1](#21-pins).

It turns out that putting in the extra work to resolve checks the manual way was very much worth it, as it resulted in a more than 10x time save for [perft](https://www.chessprogramming.org/Perft) times.

## 2.3. [En Passant](https://en.wikipedia.org/wiki/En_passant) Tricks

There are a couple of situations that arise involving the move En Passant that can cause possible problems. The most common cause of these problems occurs because En Passant is the only move where the piece being captured is not the piece that the attacker is traveling to.

![Image](https://github.com/mjstroup/Images/blob/main/Chess/EP1.png)

*The white Pawn on d4 checks the black King, and exd4 EP resolves the check.*

For this specific situation, I did not have the Pawn on e4 listed as an attacker for the d4 pawn, as En Passant is a temporary move that comes and goes in one [ply](https://en.wikipedia.org/wiki/Ply_(game_theory)). As a solution, I simply added a couple of lines in the check handling section of the legal move generator to verify if En Passant is possible, and adding it to the list of legal moves if it is.

The second and more interesting case occurs when an En Passant results in a [discovered check](https://en.wikipedia.org/wiki/Discovered_attack). 

![Image](https://github.com/mjstroup/Images/blob/main/Chess/EP2.png)

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

When minimax would like to evaluate a move for white, it will attempt to minimize black's responses. This is done because a position that is good for black is bad for white, therefore minimizing black's evaluation will maximize white's evaluation, even if it is a negative one. This continues down the move tree until it reaches a certain depth, where it statically evaluates the position, and then sends the values up the tree to be minimaxed.
 
Minimax does however waste a bunch of time calculating lines it does not have to. For example, if white wants to evaluate a move to depth 4 where black has two possible responses, and the left tree comes out to be +5 for white, when calculating the right tree, if there comes a point where black has a better evaluation than +5, white will end up picking the left move no matter what the rest of the right tree evaluates out to be, therefore it can be pruned. A technique called Alpha-beta pruning can be used to optimize these time losses.

## 3.2. [Alpha-beta Pruning](https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning)

![Image](https://github.com/mjstroup/Images/blob/main/Chess/a-bp.png)

Alpha-beta pruning is an optimization done to minimax in which two values, alpha and beta, representing the minimum score that the maximizing player is guaranteed and the maximum score that the minimizing player is guaranteed respectively, are stored. Starting out, alpha is negative infinity and beta is positive infinity, as neither player is assured of any score.

As the move tree is filled out, whenever the maximizing player encounters a value that is greater than beta, it is ensured that the optimal move does not belong to this branch, as the minimizing player will never opt to choose a potentially larger-scored branch within the move tree when it can choose its guaranteed worst-case maximum score, beta. Therefore, the rest of the branch does not need to be calculated and can be pruned. 

Similarly, whenever the minimizing player encounters a value that is less than alpha, it is ensured that the optimal move does not belong to this branch. 

The optimizations that occur with Alpha-beta pruning are somewhat luck based, as it is far more beneficial to find an optimal move early on in minimax, as it allows for more pruned branches. By ordering the list of moves by how likely they are to be good, Alpha-beta pruning is heavily optimized.

## 3.3. [Move Ordering](https://www.chessprogramming.org/Move_Ordering)

Move ordering is extremely simple to do for how effective of an optimization it is. Each move gets assigned a "score", the moves are sorted based on these scores, and then they are evaluated by Alpha-beta pruning. 

The way I implemented move ordering is by incentivising a couple of behaviors such as capturing a high valued piece with a low valued piece, promoting pawns, and capturing pieces with pawns. This could be further optimized by incentivising checks as well.

## 3.4. Opening Book

At the start of the game, the depth that minimax searches to is simply not enough to calculate optimal play for the first couple of moves. To counteract this, I downloaded about 4000 Grandmaster-level games from 50 tournaments within the last ten years in which the computer can choose opening sequences from. Up until move five, it looks into its game database, finds all positions in which the current position has occured, and then chooses a random move from those positions. These moves puts the engine in a much better opening position where its evaluations will be much more meaningful.

## 3.5. [Piece Square Tables](https://www.chessprogramming.org/Piece-Square_Tables)

Past the opening book, the engine was getting a little big confused as to what it was supposed to do if it cannot find a guaranteed advantage within the next couple of moves. If the current evaluation is +1 for the computer, and within its search depth, the best evaluation it sees is +1, it will simply play the first move it encounters that gives this evaluation, which resulted in a lot of shuffling back and forth with its rooks. This is obviously an improper way to play Chess, as the engine should look to improve its current position when it cannot find a material advantage. 

To incentivise these positional moves, each piece has a table which quantifies positive or negative bonuses measured in [centipawns](https://www.chessprogramming.org/Centipawns), that are tacked on to each team's evaluation.

![Image](https://github.com/mjstroup/Images/blob/main/Chess/wkmap.png)

*An example of a piece square table for a white Knight*

As the table shows, the white Knight is tempted towards central squares as well as its natural development squares, and detered from edge squares. Note that for a black Knight, the table is simply mirrored along the horizontal axis.

## 3.6. Endgame Optimizations

Once the position reaches an end game, the engine can have trouble seeing far enough into the future to find a forced checkmate, so there are certain optimizations I made to increase the chance of a forced checkmate being found.

The first was to alter the piece square table for the King. In the beginning of the game, the King's table was constructed such that it was encouraged to stay near the corner of the board in castling position behind its pawns. However, in the endgame, this is often the place where Kings are put into checkmate. So, once a certain amount of material is lost, the Kings switch to the endgame table, encouraging them towards the center of the board, and away from the edges and corners. This not only incentivizes the engine to move its King towards the center of the board, but encourages it to push the enemy King towards the edges and corners of the board.

Along with the altered table, I introduced another incentive to push the King as close to the edges and corners board as possible, giving a bonus for every file and rank it is pushed away from the center. This hightens the chance of a checkmate being found, but in situations where the allied King is needed to produce a checkmate, the engine runs into trouble.

To solve this issue, a third bonus is calculated based on how close the allied King is to the enemy King. This promotes [opposition](https://en.wikipedia.org/wiki/Opposition_(chess)), which is an important ingrediant to most checkmate patterns.

With these three optimizations, the engine is able to checkmate much easier with a lower depth.

# 4. Overall Performance

Chess players are evaluated based on an [Elo rating system](https://en.wikipedia.org/wiki/Elo_rating_system), a number that increases and decreases as they play tournament games against other elo-rated players. A chess engine is much harder to quantify by an elo rating, however there are some statistics that can be compared to the average statistics of various elo rated players.

The two statistics I computed is ACPL (average centipawn loss) and CAPS (Computer Aggregated Precision Score). CPL represents the total amount of centipawns lost in the evaluation per move, with ACPL averaging all of these values. CAPS is Chess.com's attempt at quantifying a player's strength at matching a top engine's moves, avoiding major mistakes/blunders, and their own algorithm for measuring patterns of strength. 

To compute these statistics for my engine, I pitted it up against Stockfish 14, one of the strongest open-source chess engines in the world. On average, the engine's ACPL was 40.5, and the engine's CAPS was 0.851.

<details>
<summary>Game Data</summary>

![Image](https://github.com/mjstroup/Images/blob/main/Chess/ACPL_normal.png)

*Normal curve representing the engine's ACPL (src: [University of Iowa](https://homepage.divms.uiowa.edu/~mbognar/applets/normal.html))*

![Image](https://github.com/mjstroup/Images/blob/main/Chess/caps_normal.png)

*Normal curve representing the engine's CAPS*

|    Color    |     ACPL    |       CAPS      | Opening |
| :---        |    :----:   |      :----:     |    :--- |
|    White    |     37      |       0.85      | Scotch Gambit |
|    White    |     45      |       0.80      | Ruy Lopez, Berlin Defense |
|    White    |     35      |       0.88      | Queen's Gambit Declined, Vienna |
|    White    |     39      |       0.89      | English, Anglo Indian Defense |
|    White    |     58      |       0.78      | Nimzo-Larsen Attack |
|    White    |     45      |       0.81      | Advanced Caro-Kann |
|    White    |     33      |       0.89      | Modern Defense |
|    White    |     46      |       0.84      | French Horwitz Attack |
|    White    |     35      |       0.90      | Nimzo-Indian Defense |
|    White    |     38      |       0.85      | Scandanavian Defense |
|    Black    |     48      |       0.78      | Ruy Lopez, Morphy Defense |
|    Black    |     42      |       0.82      | Caro-Kann, Main Line |
|    Black    |     35      |       0.75      | Old Sicilian |
|    Black    |     50      |       0.82      | Giuoco Piano |
|    Black    |     37      |       0.90      | French Exchange |
|    Black    |     39      |       0.90      | Old Benoni |
|    Black    |     32      |       0.91      | Queen's Gambit Declined, Semi-Tarrasch |
|    Black    |     42      |       0.86      | Queen's Gambit Declined, Vienna |
|    Black    |     35      |       0.93      | King's English |
|    Black    |     39      |       0.86      | Semi-Slav Defense |

|    Color    |   Average ACPL   |      Average CAPS     |
|  :---       |      :----:      |         :----:        |
|    White    |       41.1       |         0.849         |
|    Black    |       39.9       |         0.853         |
|   Overall   |       40.5       |         0.851         |

</details>

<br>

To quantify elo based on these scores, we can look at [Chess.com's study](https://www.chess.com/article/view/better-than-ratings-chess-com-s-new-caps-system) of the correlation between CAPS score and elo, suggesting that a player with an average CAPS of 0.851 would fall between the elo range of 1700 and 1800. For ACPL, we can look at a [study](https://kwojcicki.github.io/blog/CHESS-BLUNDERS#f1) done by Krystian Wojcicki, which collected data from around 2500 games, providing around 5000 ACPL/elo samples, suggesting that the elo of a player with an average ACPL of 40.5 would fall between 2700 and 2800.

Elo is incredibly hard to quantify with just these two statistics, especially when the sampled games were taken from games against one of the top engines in the world, in which every game was a loss. To determine a much more accurate elo, this engine would have to be converted to be [UCI](https://en.wikipedia.org/wiki/Universal_Chess_Interface) compatible and submitted to a database of other engines with comparable strengths, with a much large dataset of games against various opponents.

# 5. Usage

Make sure you have a JDK/JRE installed. Clone the repository and run the main method inside src/Main.java. Change the engine's depth and side within the constructor.

# 6. Credits

* [Matthew Stroup](https://github.com/mjstroup)
  * [LinkedIn](https://www.linkedin.com/in/mjstroup)
  * mjstroup@purdue.edu


