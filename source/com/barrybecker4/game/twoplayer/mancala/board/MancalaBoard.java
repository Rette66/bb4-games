/** Copyright by Barry G. Becker, 2014. Licensed under MIT License: http://www.opensource.org/licenses/MIT  */
package com.barrybecker4.game.twoplayer.mancala.board;

import com.barrybecker4.common.geometry.Location;
import com.barrybecker4.game.common.Move;
import com.barrybecker4.game.common.board.BoardPosition;
import com.barrybecker4.game.twoplayer.common.TwoPlayerBoard;
import com.barrybecker4.game.twoplayer.mancala.move.Captures;
import com.barrybecker4.game.twoplayer.mancala.move.MancalaMove;
import com.barrybecker4.game.twoplayer.mancala.move.MoveMaker;
import com.barrybecker4.game.twoplayer.mancala.move.MoveUndoer;

import java.util.LinkedList;
import java.util.List;

/**
 * Representation of a Mancala Game Board.
 *
 * @author Barry Becker
 */
public class MancalaBoard extends TwoPlayerBoard<MancalaMove> {

    /** traditionally each bin starts with 3 stones. */
    private static final byte INITIAL_STONES_PER_BIN = 3;

    /** used to navigate around the bins on in the board */
    private BinNavigator navigator;

    /**
     * Constructor
     * A Mancala board always has 2 rows.
     * It typically has 8 columns (six of which are for the ordinary bins)
     * e.g. suppose there are 2 players a and b, then
     * aH   a6  a5  a4  a3  a2  a1   bH
     * aH   b8  b9  b10 b11 b12 b13  bH
     *
     * @param numCols num cols
     */
    public MancalaBoard(int numCols) {
        setSize( 2, numCols );
        navigator = new BinNavigator(numCols);
    }

    /**
     * default constructor
     * The first and last columns are for the home bases.
     */
    public MancalaBoard() {
        this(8);
    }

    protected MancalaBoard(MancalaBoard mb) {
        super(mb);
        navigator = new BinNavigator(mb.getNumCols());
    }

    @Override
    public MancalaBoard copy() {
        return new MancalaBoard(this);
    }

    /**
     * Reset the board to its initial state.
     */
    @Override
    public void reset() {
        super.reset();
        for (int row = 1; row <= getNumRows(); row++) {
            for (int col = 2; col < getNumCols(); col++) {
                getPosition(row, col).setPiece(new MancalaBin(row == 1, INITIAL_STONES_PER_BIN, false));
            }
        }
        getPosition(1, 1).setPiece(new MancalaBin(true, (byte)0, true));
        getPosition(1, getNumCols()).setPiece(new MancalaBin(false, (byte)0, true));
    }

    protected BoardPosition getPositionPrototype() {
        return new BoardPosition(1, 1, null);
    }

    /**
     * This is just a conservative rough guess.
     * My reasoning is that the end of round is inevitable because
     * once a stone enters the home bin, it can never come out.
     * However, a stone does not always go in a home every turn (but they always move closer), and
     * some turns more than one may go in.
     * So my estimate is 3 times the number of columns times the starting number
     * of stones in each bin. Multiplying by 2 instead of 3 would probably be a more
     * accurate estimate, but we want an upper limit.
     */
    @Override
    public int getMaxNumMoves() {
        return getNumCols() * INITIAL_STONES_PER_BIN * 3 + 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean makeInternalMove( MancalaMove move ) {
        return new MoveMaker(this).makeMove(move);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void undoInternalMove( MancalaMove move ) {
        new MoveUndoer(this).undoMove(move);
    }

    public MancalaBin getHomeBin(boolean player1) {
        return getBin(navigator.getHomeLocation(player1));
    }

    public boolean isEmpty() {
        return isSideClear(true) && isSideClear(false);
    }

    public boolean isSideClear(boolean player1) {
        int sum = 0;
        Location currentLoc = navigator.getHomeLocation(!player1);
        for (int i=0; i < getNumCols() - 2; i++) {
            currentLoc = navigator.getNextLocation(currentLoc);
            sum += getBin(currentLoc).getNumStones();
        }
        return sum == 0;
    }

    /**
     * @return true if the player's move will land the last stone in their own home bin.
     */
    public boolean moveAgainAfterMove(Move move) {
        MancalaMove m = (MancalaMove)move;
        Location lastLoc = navigator.getNthLocation(m.getFromLocation(), m.getNumStonesSeeded());
        MancalaBin bin = getBin(lastLoc);
        return bin.isHome() && bin.isOwnedByPlayer1() == m.isPlayer1();
    }

    /**
     * @param player1  the player's who's bins to consider
     * @return a list of all player bin locations that have one stone or more.
     */
    public List<Location> getCandidateStartLocations(boolean player1) {
        List<Location> locations = new LinkedList<>();

        Location currentLoc = getHomeLocation(!player1);
        for (int i=0; i < getNumCols()-2; i++) {
            currentLoc = getNextLocation(currentLoc);
            if (getBin(currentLoc).getNumStones() > 0) {
                locations.add(currentLoc);
            }
        }

        return locations;
    }

    public MancalaBin getBin(Location loc) {
        assert loc != null;
        MancalaBin bin =  (MancalaBin) getPosition(loc).getPiece();
        assert bin != null : " Could not find mancala bin at " + loc;
        return bin;
    }

    /**
     * Clear off the remaining stones on the specified players side and put them in his store.
     * @param player1 player's whose side to clear.
     */
    public void clearSide(boolean player1, Captures captures) {
        MancalaBin homeBin = getHomeBin(player1);

        Location currentLoc = getHomeLocation(!player1);
        for (int i=0; i < getNumCols() - 2; i++) {
            currentLoc = getNextLocation(currentLoc);
            MancalaBin bin = getBin(currentLoc);
            if (bin.getNumStones() > 0) {
                captures.put(currentLoc, bin.getNumStones());
                homeBin.increment(bin.takeStones());
            }
        }
    }

    public Location getHomeLocation(boolean player1) {
        return navigator.getHomeLocation(player1);
    }

    public Location getNextLocation(Location loc) {
        return navigator.getNextLocation(loc);
    }

    public Location getOppositeLocation(Location loc) {
        return navigator.getOppositeLocation(loc);
    }

    /**
     * Num different states.
     * This is used primarily for the Zobrist hash. You do not need to override if you do not use it.
     * For mancala there are a lot of potential states. A bin can be empty, or it can have any number of stones.
     * @return number of different states this position can have.
     */
    @Override
    public int getNumPositionStates() {
        return getNumCols() * INITIAL_STONES_PER_BIN;
    }

    /*
    @Override
    public String toString() {
        StringBuilder bldr = new StringBuilder(1000);
        bldr.append("\n");
        int nRows = getNumRows();
        int nCols = getNumCols();
        TwoPlayerMove lastMove = getMoveList().getLastMove();

        for ( int i = 1; i <= nRows; i++ )   {
            boolean followingLastMove = false;
            for ( int j = 1; j <= nCols; j++ ) {
                BoardPosition pos = this.getPosition(i,j);
                if (pos.isOccupied()) {
                    if (lastMove != null && pos.getLocation().equals(lastMove.getToLocation())) {
                        bldr.append("[").append(pos.getPiece()).append("]");
                        followingLastMove = true;
                    }
                    else  {
                        bldr.append(followingLastMove ? "" : " ").append(pos.getPiece());
                        followingLastMove = false;
                    }
                }
                else {
                    bldr.append(followingLastMove ? "" : " ").append("_");
                    followingLastMove = false;
                }
            }
            bldr.append("\n");
        }
        return bldr.toString();
    }      */
}
