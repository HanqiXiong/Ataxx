/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import java.util.Random;
import java.util.ArrayList;

import static ataxx.PieceColor.*;
import static java.lang.Math.min;
import static java.lang.Math.max;

/** A Player that computes its own moves.
 *  @author Hanqi Xiong
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 4;
    /** A position magnitude indicating a win (for red if positive, blue
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI for GAME that will play MYCOLOR. SEED is used to initialize
     *  a random-number generator for use in move computations.  Identical
     *  seeds produce identical behaviour. */
    AI(Game game, PieceColor myColor, long seed) {
        super(game, myColor);
        _random = new Random(seed);
    }

    @Override
    boolean isAuto() {
        return true;
    }

    @Override
    String getMove() {
        if (!getBoard().canMove(myColor())) {
            game().reportMove(Move.pass(), myColor());
            return "-";
        }
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        game().reportMove(move, myColor());
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(getBoard());
        _lastFoundMove = null;
        if (myColor() == RED) {
            minMax(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            minMax(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to the findMove method
     *  above. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int minMax(Board board, int depth, boolean saveMove, int sense,
                       int alpha, int beta) {
        /* We use WINNING_VALUE + depth as the winning value so as to favor
         * wins that happen sooner rather than later (depth is larger the
         * fewer moves have been made. */
        if (depth == 0 || board.getWinner() != null) {
            return staticScore(board, WINNING_VALUE + depth);
        }

        Move best;
        best = null;

        int bestSoFar = 0;
        ArrayList<Move> allmove = moves(board);
        if (sense == 1) {
            bestSoFar = -INFTY;
            for (Move move : allmove) {
                if (board.legalMove(move)) {
                    board.makeMove(move);
                    int response;
                    response = minMax(board, depth - 1, false, -1, alpha, beta);
                    board.undo();
                    if (response > bestSoFar) {
                        bestSoFar = response;
                        alpha = max(alpha, bestSoFar);
                        best = move;
                        if (alpha >= beta) {
                            break;
                        }
                    }
                }
            }
        } else if (sense == -1) {
            bestSoFar = INFTY;
            for (Move move : allmove) {
                if (board.legalMove(move)) {
                    board.makeMove(move);
                    int response;
                    response = minMax(board, depth - 1, false, 1, alpha, beta);
                    board.undo();
                    if (response < bestSoFar) {
                        bestSoFar = response;
                        beta = min(beta, bestSoFar);
                        best = move;
                        if (alpha >= beta) {
                            break;
                        }
                    }
                }
            }
        }
        if (saveMove) {
            _lastFoundMove = best;
        }
        return bestSoFar;
    }

    /** Return a heuristic value for BOARD.  This value is +- WINNINGVALUE in
     *  won positions, and 0 for ties. */
    private int staticScore(Board board, int winningValue) {
        PieceColor winner = board.getWinner();
        if (winner != null) {
            return switch (winner) {
            case RED -> winningValue;
            case BLUE -> -winningValue;
            default -> 0;
            };
        }

        return board.redPieces() - board.bluePieces();
    }


    private ArrayList<Move> moves(Board board) {
        ArrayList<Move> moves = new ArrayList<>();
        for (int c = 2; c < Board.SIDE + 2; c += 1) {
            for (int r = 2; r < Board.SIDE + 2; r += 1) {
                for (int dc = -2; dc <= 2; dc += 1) {
                    for (int dr = -2; dr <= 2; dr += 1) {
                        char col0 = (char) (c + 'a' - 2);
                        char row0 = (char) (r + '1' - 2);
                        char col1 = (char) (c + dc + 'a' - 2);
                        char row1 = (char) (r + dr + '1' - 2);
                        Move newmove = Move.move(col0, row0, col1, row1);
                        if (board.legalMove(newmove)) {
                            moves.add(newmove);
                        }
                    }
                }
            }
        }
        return moves;
    }

    /** Pseudo-random number generator for move computation. */
    private Random _random = new Random();
}

