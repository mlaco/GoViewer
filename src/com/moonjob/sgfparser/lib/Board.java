/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moonjob.sgfparser.lib;

import java.lang.String;
import java.lang.Math;

/**
 *
 * @author Jocelyn
 */
public class Board {

    int groupCap, numGrp = 0;
    int[] nbrColorAry, nbrGroupAry, adjacentAry, xPoints, yPoints;
    int[][] colors, groups, mock;
    private char numVari;
    //resolveGroups handles same-color liberty interaction
    //reduceLiberties handles opposing-color liberty interactions

    public Board() {
        groupCap = 50;
        nbrColorAry = new int[4];
        nbrGroupAry = new int[4];
        xPoints = new int[4];
        yPoints = new int[4];
        colors = new int[19][19];
        groups = new int[19][19];
        mock = new int[19][19];
        /* adjacentAry simulates an upper triangular matrix
         * such that the position i,j indicates whether
         * group i<j is adjacent to group j. The diagonal entries
         * will be used to keep track of liberties
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

    public void move(String loc, int color) {

        int x, y;
        loc = loc.toUpperCase();
        x = convert(loc.charAt(0));
        y = convert(loc.charAt(1));
        setState(x, y, color);
        resolveGroups(x, y, color);
        reduceLibs(color);

    }

    private int convert(char c) {
        int i;
        i = (int) c;
        i = i - 65;
        return i;
    }

    private void setState(int x, int y, int color) {
        colors[x][y] = color;
        getNbrStates(x, y, color);
    }

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

    public void countLiberties(int group, int color) {
        int liberties;
        liberties = 0;
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

    private void reduceLibs(int color) {
        int group;
        for (int i = 0; i < 4; i++) {
            if (nbrColorAry[i] + color == 3) {
                // Decrease the liberties of all adjacent enemy groups.
                group = nbrGroupAry[i];
                /*use the diagonal entries of adjacentAry
                 * to store the number of liberties of a group.
                 */
                adjacentAry[rowcoltoIndex(group, group)] -= 1;
                if (adjacentAry[rowcoltoIndex(group, group)] == 0) {
                    //Changing to group 0 is removing the group.
                    changeGroup(group, 0, color);
                    //liberties of captured groups must be recalculated.
                    for (int j = 1; j < 51; j++) {
                        if (adjacentAry[rowcoltoIndex(j, group)] == 1) {
                            countLiberties(j, color);
                            setAdjacent(group, j, 0);
                        }
                    }
                }
            }
        }
    }

    private void changeGroup(int fromGrp, int toGrp, int color) {
        for (int row = 0; row < 19; row++) {
            for (int col = 1; col < 19; col++) {
                //Change the group state of any stones in group fromGrp
                // to group toGrp.
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

    private void resolveGroups(int x, int y, int color) {
        //This function registers the joining and merging of
        //groups that may occur when a new stone is placed.
        //It will ensure that the resultant group has an
        //accurate liberty count, and that groups and
        //adjacentAry are updated.
        int mbrGrp = 0;
        int aNbr = 0;
        for (int i = 0; i < 4; i++) {
            if (mbrGrp == 0) {
                if (nbrColorAry[i] == color) {
                    mbrGrp = nbrGroupAry[i];
                    groups[x][y] = mbrGrp;
                    //must search over all neighbors
                    //while knowing what group the stone
                    //belongs to. Therefore, reset the index.
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

    private void setAdjacent(int grp1, int grp2, int mode) {
        //should make sure that grp1!=grp2, actually.
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
            //If a group is a neighbor to either group, then it is a neighbor
            //to their union.
            adjacentAry[temp1] =
                    Math.max(adjacentAry[temp1], adjacentAry[temp2]);
            adjacentAry[temp2] = 0;
        }

    }

    private int makeGrp(int x, int y, int color) {
        //set larger than any actual group will be
        //given that the code works
        int group = 1;
        //This function handles the creation of new groups, and ensures
        //that groups and adjacentAry are updated. colors is already set
        //by setStates.
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

    private void setupMock(int group) {
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

    private int rowcoltoIndex(int row, int col) {
        return groupCap * (row - 1) + col - row * (row - 1) / 2;
    }

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
