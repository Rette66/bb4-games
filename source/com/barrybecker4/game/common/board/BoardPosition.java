/** Copyright by Barry G. Becker, 2000-2011. Licensed under MIT License: http://www.opensource.org/licenses/MIT  */
package com.barrybecker4.game.common.board;

import com.barrybecker4.common.geometry.ByteLocation;
import com.barrybecker4.common.geometry.Location;

/**
 *  The BoardPosition describes the physical marker at a location on the board.
 *  It may be empty if there is no piece there.
 *
 * @see Board
 * @author Barry Becker
 */
public class BoardPosition {

    /** we need to store the location so we can restore captures.  */
    protected Location location;

    /** the piece to display at this position. Null if the position is unoccupied. */
    protected GamePiece piece;

    /**
     * constructor
     * @param row - y position on the board.
     * @param col - x position on the board.
     * @param piece - the piece to put at this position (use null if there is none).
     */
    public BoardPosition( int row, int col, GamePiece piece)  {
        this(new ByteLocation(row, col), piece);
    }

    public BoardPosition(BoardPosition p) {

        location = new ByteLocation(p.getRow(), p.getCol());
        piece = (p.piece != null) ? p.piece.copy() : null;
    }

    /**
     * @return copy of this position.
     */
    public BoardPosition copy() {
        return new BoardPosition(this);
    }

    /**
     * constructor
     * @param loc -  location on the board.
     * @param piece - the piece to put at this position (use null if there is none).
     */
    protected BoardPosition( Location loc, GamePiece piece)  {
        location = loc;
        this.piece = piece;
    }

    /**
     * @return  true if values are equal.
     */
    @Override
    public boolean equals( Object pos )  {
         if ((pos == null) || !(pos.getClass().equals(this.getClass()))) {
             return false;
         }

         BoardPosition comparisonPos = (BoardPosition) pos;
         boolean sameSide;
         if (getPiece() != null && comparisonPos.getPiece() != null)   {
             sameSide = (getPiece().isOwnedByPlayer1() == comparisonPos.getPiece().isOwnedByPlayer1());
         }
         else {
             sameSide = (getPiece() == null && comparisonPos.getPiece() == null);
         }
         return (getRow() == comparisonPos.getRow()) &&
                (getCol() == comparisonPos.getCol()) && (sameSide);
    }

    /**
     * override hashcode if you override equals
     */
    @Override
    public int hashCode() {
        return getRow() * 300 + getCol() ;
    }

    /**
     * @return the piece at this position if there is one.
     */
    public GamePiece getPiece()  {
        return piece;
    }

    /**
     * @param piece the piece to assign to this position.
     */
    public void setPiece(GamePiece piece) {
        this.piece = piece;
    }

    /**
     * @return true if the piece space is currently unoccupied
     */
    public final boolean isUnoccupied() {
        return (piece == null);
    }

    /**
     * @return true if the piece space is currently occupied
     */
    public final boolean isOccupied() {
         return (piece != null);
    }

    public final int getRow() {
        return location.row();
    }

    public final int getCol() {
        return location.col();
    }

    public final void setLocation( Location loc ) {
        location = loc;
    }

    public final Location getLocation() {
        return location;
    }

    /**
     * Get the euclidean distance from another board position
     * @param position to get the distance from
     * @return distance from another position
     */
    public final double getDistanceFrom( BoardPosition position ) {
        return location.getDistanceFrom(position.getLocation());
    }

    /**
     * @param position to check if neighboring
     * @return true if immediate neighbor (nobi neighbor)
     */
    public final boolean isNeighbor( BoardPosition position ) {
        return getDistanceFrom(position) == 1.0;
    }

    /**
     * make it show an empty board position.
     */
    public void clear() {
        setPiece( null );
    }

    /**
     * @return a string representation of the board position
     */
    protected String getDescription() {
        return toString(true);
    }

    /**
     * @return a string representation of the board position
     */
    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * @return string form.
     */
    private String toString(boolean longForm) {
        StringBuilder sb = new StringBuilder( "" );
        if (piece != null)
            sb.append(longForm? piece.getDescription() : " " + piece.toString());
        sb.append(" (").append(location.toString()).append(')');
        return sb.toString();
    }

}

