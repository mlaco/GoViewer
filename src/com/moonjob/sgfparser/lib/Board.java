/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moonjob.sgfparser.lib;

import java.lang.String;
import java.lang.Math;

/**
 *
 * @author Morgan
 */
public class Board {

    int groupCap, numGrp = 0;
    int[] nbrColorAry, nbrGroupAry, adjacentAry, xPoints, yPoints;
    int[][] colors, groups, mock;
    private char numVari;
    //resolveGroups handles same-color liberty interaction
    //reduceLiberties handles opposing-color liberty interactions

    /**
     * Class to represent the state of the Goban (the board on which Go is played)
     */
    public Board() {
        groupCap = 50; // Maximum number of groups permitted on board
        nbrColorAry = new int[4]; // 
        nbrGroupAry = new int[4];
        xPoints = new int[4]; // difference in x position of the neighboring board points
        yPoints = new int[4]; // difference in y position of the neighboring board points
        colors = new int[19][19]; // the color of the stone at each board point
        groups = new int[19][19]; // the group number of the stone at each board point
        mock = new int[19][19]; // second board state, used for counting liberties
        /* adjacentAry simulates an upper triangular matrix
         * such that the position i,j indicates whether
         * group i<j is adjacent to group j. The diagonal entries
         * will be used to keep track of liberties
         *
         * The diagonal entries of this array are used to record the number of 
         * liberties possessed by the group associated with the row (or equivalently
         * by the column)
         */
        adjacentAry = new int[groupCap * (groupCap + 1) / 2];
        xPoints[0] = 0;
        yPoints[0] = -1;
        xPoints[1] = -1;
        yPoints[1] = 0;
        xPoints[2] = 1;
        yPoints[2] = 0;
        xPoints[3] = 0;
        yPoints[3] = 1;

    }

    /**
     * Executes a move on the board
     * @param loc - board location of the move
     * @param color - color of the stone played
     */
    public void move(String loc, int color) {
        
        // Get the coordinates of the move
        int x, y;
        loc = loc.toUpperCase();
        x = convert(loc.charAt(0));
        y = convert(loc.charAt(1));
        
        // Update the state of the board
        setState(x, y, color);
        resolveGroups(x, y, color);
        reduceLibs(color);

    }

    /**
     * Convert the character to a board coordinate
     * @param c character to convert
     * @return board coordinate
     */
    private int convert(char c) {
        int i;
        i = (int) c;
        i = i - 65;
        return i;
    }

    /**
     * Record the presence of a stone of given color at the given coordinates
     * @param x x coordinate of the stone to be placed
     * @param y y coordinate of the stone to be placed
     * @param color color of the stone to be placed
     */
    private void setState(int x, int y, int color) {
        colors[x][y] = color;
        getNbrStates(x, y, color);
    }

    /**
     * Determine and record the state of adjacent points (i.e. empty, black, white)
     * 
     * These states are stored in nbrColorAry
     * 
     * @param xOrig x coordinate of point whose neighbors are checked
     * @param yOrig y coordinate of point whose neighbors are checked
     * @param color color of stone at point whose neighbors are checked
     */
    public void getNbrStates(int xOrig, int yOrig, int color) {
        // Determine the states of orthogonal getNbrStates.
        // A state of -1 indicates no neighbor exists at the point.
        // i.e. edge of the board.
        int x, y;

        for (int i = 0; i < 4; i++) {
            x = xOrig + xPoints[i];
            y = yOrig + yPoints[i];
            if (Math.abs(10 - x) < 10 && Math.abs(10 - y) < 10) {
                nbrColorAry[i] = colors[x][y];
                nbrGroupAry[i] = groups[x][y];
            } else {
                nbrColorAry[i] = -1;
                nbrGroupAry[i] = -1;
            }
        }
    }

    /**
     * Determine the number of liberties of a group of stones
     * @param group group number whose liberties are counted
     * @param color color of group whose liberties are counted
     */
    public void countLiberties(int group, int color) {
        int liberties;
        liberties = 0;
        
        // Initialize the state of the mock board for counting
        clearMock();
        setupMock(group);
        
        for (int row = 0; row < 19; row++) {
            for (int col = 0; col < 19; col++) {
                if (mock[row][col] == color) {
                    liberties += claimLibs(row, col, color);
                }
            }
        }
        try{
        adjacentAry[rowcoltoIndex(group, group)] = liberties;
        }
        catch (Exception e){
            System.err.println("something happened in countLiberties.");
        }
    }

    /**
     * Reduce the liberties of any groups recorded in nbrGroupArray
     * @param color 
     */
    private void reduceLibs(int color) {
        int group;
        // Iterate over orthogonal directions
        for (int i = 0; i < 4; i++) {
            if (nbrColorAry[i] + color == 3) {
                // Decrease the liberties of all adjacent enemy groups.
                
                // Determine which group is adjacent in this direction
                group = nbrGroupAry[i];
                
                /* 
                 * Decrement the liberties of the group
                 */
                adjacentAry[rowcoltoIndex(group, group)] -= 1;
                
                // Check for group capture
                if (adjacentAry[rowcoltoIndex(group, group)] == 0) {
                    
                    //Changing to group 0 is removing the group.
                    changeGroup(group, 0, color);
                    
                    // liberties of groups adjacent to captured groups must
                    // be recalculated.
                    for (int j = 1; j < 51; j++) {
                        
                        // Check whether each group was adjacent to the
                        // now-captured group
                        if (adjacentAry[rowcoltoIndex(j, group)] == 1) {
                            countLiberties(j, color);
                            setAdjacent(group, j, 0);
                        }
                    }
                }
            }
        }
    }

    /**
     * Change the group state of stones in group fromGrp to group toGrp
     * @param fromGrp initial group state of stones
     * @param toGrp final group state of stones
     * @param color color of groups
     */
    private void changeGroup(int fromGrp, int toGrp, int color) {
        for (int row = 0; row < 19; row++) {
            for (int col = 1; col < 19; col++) {

                if (groups[row][col] == fromGrp) {
                    groups[row][col] = toGrp;
                    if (toGrp == 0) {
                        //exception only needs to be made when capturing.
                        //When merging, the colors are already correct.
                        colors[row][col] = 0;
                    }
                }
            }
        }
        /*When using this method to capture a group,
         * it is necessary to recalculate all
         *neighboring groups' liberties
         */
        if (toGrp == 0) {
            for (int i = 1; i < 50; i++) {
                if (i != fromGrp && adjacentAry[rowcoltoIndex(i,fromGrp)]==1) {
                    countLiberties(i, color);
                    setAdjacent(i, fromGrp, 0);
                }
            }
        }
    }

    /**
     * Registers the merging of groups that may occur when a new stone
     * is placed. Ensures that the resultant group has an accurate liberty
     * count, and that groups and adjacentAry are updated
     * 
     * @param x x coordinate of newly played stone
     * @param y y coordinate of newly played stone
     * @param color color of newly played stone
     */
    private void resolveGroups(int x, int y, int color) {
       
        int mbrGrp = 0; // store the group of the new stone
        int aNbr = 0;
        
        // Iterate over orthogonal directions
        for (int i = 0; i < 4; i++) {
            if (mbrGrp == 0) {
                
                // Add the stone to a friendly group if it is adjacent to it
                if (nbrColorAry[i] == color) {
                    mbrGrp = nbrGroupAry[i];
                    groups[x][y] = mbrGrp;

                    // If the stone is added to a group, then we must iterate
                    // over all orthogonal directions again, so that adjacency
                    // information is updated.
                    i = 0;
                }
            } else {
                aNbr = nbrGroupAry[i];
                if (nbrColorAry[i] == color) {
                    changeGroup(aNbr, mbrGrp, color);
                    setAdjacent(aNbr, mbrGrp, 0);//unregister as adjacent
                    consolidateAdjacents(aNbr, mbrGrp);
                } else if (nbrColorAry[i] + color == 3) {
                    setAdjacent(mbrGrp, aNbr, 1);
                    //liberty changes resulting from
                    //enemy interaction are handled
                    //by reduceLiberties
                }
            }
        }
        //If mbrGrp still hasn't been set, then the stone is
        //isolated. Create a new group for it.
        if (mbrGrp == 0) {
            mbrGrp = makeGrp(x, y, color);
        }
        //need only to count the liberties of the group
        //that the newly-placed stone belongs to
        countLiberties(mbrGrp, color);
    }

    /**
     * Record the adjacency of two groups
     * @param grp1 first group
     * @param grp2 second group
     * @param mode value indicating whether first and second group are adjacent.
     * 1 corresponding to true, and 0 to false
     */
    private void setAdjacent(int grp1, int grp2, int mode) {
        if (grp2 < grp1) {
            int temp;
            temp = grp1;
            grp1 = grp2;
            grp2 = temp;
        }
        try{
        adjacentAry[rowcoltoIndex(grp1, grp2)] = mode;
        }
        catch(Exception e){
            System.err.println("Something happenend in setAdjacent.");
        }
    }

    /**
     * Given two groups that are to be merged into one, consolidate all adjacency
     * information by storing it all in the row of adjacentAry associated with
     * the first, and clear the row associated with the second.
     * @param grp1 first group; group that will inherit members from second
     * @param grp2 second group; group whose members will be given to the first
     */
    private void consolidateAdjacents(int grp1, int grp2) {
        int temp1, temp2;
        if (grp2 < grp1) {
            temp1 = grp1;
            grp1 = grp2;
            grp2 = temp1;
        }
        /*The entries relating to a stone k form an "L" shape in the
         * upper triangular matrix adjacentAry. They are the entries (i,k)
         * for i<k, and (k,i) for k<=i.
         * Transferring the liberties "L" associated with grp2 to the "L"
         * associated with grp1 involves 3 phases. These are punctuated by
         * the indexes grp1 and grp2 with grp1<grp2.
         */
        for (int i = 1; i < grp1; i++) {
            temp1 = ithIndexofGrp(grp1, i);
            temp2 = ithIndexofGrp(grp2, i);
            // Record whether merged group is adjacent to group i
            //If a group is a neighbor to either group, then it is a neighbor
            //to their union
            adjacentAry[temp1] =
                    Math.max(adjacentAry[temp1], adjacentAry[temp2]);
            // Clear the row in adjacentAry associated with grp2; all members
            // of grp2 are being placed in grp1
            adjacentAry[temp2] = 0;
        }

    }

    /**
     * Handles the creation of new groups, and ensures that groups and adjacentAry
     * are updated. 'colors' is already set by setStates
     * @param x
     * @param y
     * @param color
     * @return 
     */
    private int makeGrp(int x, int y, int color) {

        int group = 1;
        
        for (int i = 1; i <= numGrp + 1; i++) {
            //check group liberties to see if group ID is in use
            if (adjacentAry[rowcoltoIndex(i, i)] == 0) {
                groups[x][y] = i;
                group = i;
                break;
            }
        }
        //increase numGrp if necessary
        numGrp = Math.max(numGrp, group);
        for (int i = 0; i < 4; i++) {
            if (nbrColorAry[i] + color == 3) {
                setAdjacent(group, nbrGroupAry[i], 1);
            }
        }
        return group;
    }

    /**
     * Returns the adjacentAry index corresponding to the proper position
     * in the upper triangular matrix it simulates, corresponding to two given
     * groups.
     * 
     * @param grp first group id
     * @param i second group id
     * @return the index
     */
    private int ithIndexofGrp(int grp, int i) {
        //This function produces the correct index within
        //the adjacentAry array given the row and column that
        //the entries would have in upper triangular matrix form.

        if (i < grp) {
            return rowcoltoIndex(i, grp);
        } else {
            return rowcoltoIndex(grp, i);
        }
    }

    private void clearMock() {
        for (int row = 0; row < 19; row++) {
            for (int col = 0; col < 19; col++) {
                mock[row][col] = 0;
            }
        }
    }

    /**
     * Adds a given group to the mock board, as well as any group having
     * member stones adjacent to any stone of the given group
     * @param group 
     */
    private void setupMock(int group) {
        // TODO consider a more efficient alternative for liberty counting
        int index;
        addtoMock(group);
        for (int i = 1; i < groupCap; i++) {
            if (i == group) {
                continue;
            }
            index = ithIndexofGrp(group, i);
            if (adjacentAry[index] == 1) {
                addtoMock(i);
            }
        }
    }

    /**
     * Add a given group to the mock board
     * @param group 
     */
    private void addtoMock(int group) {
        for (int row = 0; row < 19; row++) {
            for (int col = 0; col < 19; col++) {
                if (groups[row][col] == group) {
                    //there is no need to keep track of group
                    //number on the mock board
                    mock[row][col] = colors[row][col];
                }
            }
        }
    }

    /**
     * Counts the direct liberties of a stone at the given board point, at the 
     * current state of the mock board
     * 
     * NOTE - changes the state of the mock board to prevent multiple counting
     * of liberties
     * 
     * @param row y coordinate of the point whose liberties are counted
     * @param col x coordinate of the point whose liberties are counted
     * @param color color of the stone at the point whose liberties are counted
     * @return number of direct liberties of a stone at the given board point
     */
    private int claimLibs(int row, int col, int color) {
        int liberties = 0;
        for (int i = 0; i < 4; i++) {
            if (mock[row+xPoints[i]][col+yPoints[i]] == 0) {
                //add a liberty for each empty intersection
                //found, then place an opposite color stone
                //on the intersection to prevent the liberty
                //from being counted again.
                liberties++;
                mock[row+xPoints[i]][col+yPoints[i]] = 3 - color;
            }
        }
        return liberties;
    }

    /**
     * Calculates the index of the element in adjacentAry indicating whether 
     * the given groups are adjacent.
     * 
     * recall adjacentAry simulates an upper triangular matrix
     * 
     * @param row row in the simulated matrix corresponding to the first group
     * @param col col in the simulated matrix corresponding to the first group
     * @return index of the element
     */
    private int rowcoltoIndex(int row, int col) {
        return groupCap * (row - 1) + col - row * (row - 1) / 2;
    }

    /**
     * Generate a string to serve as a graphical representation of the goban
     * @return the string
     */
    public String printGoban() {
        String result = "\n      ";
        for (int row = 0; row < 19; row++) {
            for (int col = 0; col < 19; col++) {
                result = result + getSymbol(colors[col][row]);
            }
            result = result + "\n      ";
        }
        return result;
    }

    /**
     * Get the symbol used to represent stones or board intersections
     * @param color
     * @return 
     */
    private String getSymbol(int color) {
        String val;
        switch (color) {
            case 0:
                val = " +";
                break;
            case 1:
                val = " @";
                break;
            case 2:
                val = " O";
                break;
            default:
                val = " x";
                break;
        }
        return val;
    }

    /**
     * 
     * @param sequence 
     */
    public void loadSeq(String sequence) {
        clearGoban();
        int maxI=0;
        int color=1;
        numVari = sequence.charAt(0);
        sequence = sequence.substring(1);
        maxI = sequence.length()/3-1;
        for (int i=0;i<=1+3*maxI;i+=3){
            move(sequence.substring(i, i+2),color);
            color = color%2+1;
        }
    }

    /**
     * Set the state of the goban to empty and clear all state variables
     */
    private void clearGoban() {
        numGrp=0;
        adjacentAry = new int[groupCap * (groupCap + 1) / 2];
        for (int row=0;row<19;row++){
            for (int col=0;col<19;col++){
                colors[row][col]=0;
                groups[row][col]=0;
            }
        }
    }

    public String getNumVari() {
        return String.valueOf(numVari);
    }
}
