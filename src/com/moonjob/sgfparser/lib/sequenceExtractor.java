/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moonjob.sgfparser.lib;

import java.lang.String;
import java.lang.Math;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Morgan Laco
 */
public class sequenceExtractor {
    //dir indicates direction file is being read
    //reading file backward is used to reverse sequences

    // Number of times an immediate branching occurs when traversing the sgf tree
    // from the root to the current node
    private int depth;
    // The mover number of the current move
    private int moveNum;
    // Indicates how many sets of square brackets [] in which the current node
    // is contained
    private int tagdepth;
    // The highest move number of all moves
    private int maxMove;
    
    //lastMove and lastOccur specify the requested
    //state of play. They are needed to determine
    //the number of branches from that node.
    //numVari stores that value.
    private int lastMove, lastDepth, lastOccur, numVari, remLen;
    // occur: the number of moves that exist at the depth given by the index
    private int[] occur;
    // branchedMoveNum: stores the common move number of the first moves after
    // a branch point in the game record tree
    private int[] branchedMoveNum;
    private int[] parent;
    //In path, parent ; first index is move number, second index: 1:depth 2:occurence
    // path: array holding instructions on how to reach a move in the game.
    // each entry indicates which branch leads to the move when a branch point is
    // encountered
    private int[][] path;
    private int[][] nextParent;
    private String filename, sequence, remainder;
    private FileReader fstream;
    private int numSibs;

    public sequenceExtractor(int lastMove, int lastOccur) {
        this.maxMove = 50;
        this.lastMove = 1;
        this.lastOccur = lastOccur;
        path = new int[maxMove + 1][2];
        parent = new int[2];
        nextParent = new int[maxMove][2];
        //nextParent indicates what the parent will
        //be, depending on what depth the next move is at.

        depth = 0;
        occur = new int[maxMove];
        branchedMoveNum = new int[maxMove];
        for (int i = 0; i < maxMove; i++) {
            occur[i] = 0;
            branchedMoveNum[i] = 0;
        }
        moveNum = 1;
        tagdepth = 0;
    }

    /**
     * read the sgf file into local memory
     * @param str 
     */
    public void loadSGF(String str) {
        filename = str;
        try {
            fstream = new FileReader(filename);
        } catch (Exception e) {
            System.err.println("Error opening sgf file.");
        }
    }

    public void feed(String stream) {
        int i;
        //Don't forget to prepend the old remainder,baka!
        stream = remainder + stream;
        try {
            i = triage(stream);
            remainder = stream.substring(i);
        } catch (Exception e) {
            System.err.println("Something went wrong in triage");
        }
    }

    /**
     * 
     * @param stream
     * @return 
     */
    private int triage(String stream) {
        int passes = 0;
        int i = 0, q = stream.length() + 1;
        remLen = 0;


        do {

            passes++;
            
			// find position in string of next instance of each grouping symbol
            int iLP = stream.indexOf("(", i);
            int iRP = stream.indexOf(")", i);
            int iLB = stream.indexOf("[", i);
			
            // Can't use a single variable to record the earlier of the two following
			// groupers, because if one is not found, then the whole result will be -1
            int iBLB = stream.indexOf("B[", i);
            int iWLB = stream.indexOf("W[", i);
            
            // If "B[" or "W[" were found, then set iBLB or iWLB to their index
            iBLB = (iBLB == -1) ? q : iBLB;
            iWLB = (iWLB == -1) ? q : iWLB;
            
            // Get the index of the next of either "B[" or "W["
            // In other words, find the index of the next move
            int iCLB = Math.min(iBLB, iWLB);
            
            int iRB = stream.indexOf("]", i);

            // Ensure that no indices are -1;
            iLP = (iLP == -1) ? q : iLP;
            iRP = (iRP == -1) ? q : iRP;
            iLB = (iLB == -1) ? q : iLB;
            iCLB = (iCLB == -1) ? q : iCLB;
            iRB = (iRB == -1) ? q : iRB;

            // now we can triage...
            // Find the position of the next grouping symbol
            int iLGrpr = Math.min(iLP, iLB);
            int iRGrpr = Math.min(iRP, iRB);

            if (iLGrpr < iCLB && iLGrpr < iRGrpr && iLGrpr != q) {
                i = iLGrpr + 1;
                depths(stream.charAt(iLGrpr));
            } else if (iRGrpr < iCLB && iRGrpr != q) {
                i = iRGrpr + 1;
                depths(stream.charAt(iRGrpr));
            } else if (iCLB != q) {
                i = iCLB + 5;
                if (!(stream.length() - iCLB < 5)) {
                    try {
                        // 
                        move(stream.substring(iCLB + 2, iCLB + 4));
                    } catch (Exception e) {
                        System.err.println("Something went wrong in move.");
                    }
                }
            }



            //remLen is the tenative size of the stream remainder,
            //the portion of the string whose processing
            //should be delayed until the buffer size (stream) is increased.
            //This remainder is at most 5 chars long.
            //When stream is no longer a proper superset of
            //the remainder, the loop should break.
            remLen = Math.min(5, (stream.substring(i)).length());

        } while (stream.length() - i > remLen && passes < 50);
        return i;
    }

    /**
     * Determine parameters indicating the position of the current move in the
     * game record tree. This information is necessary for navigating between
     * branches in the tree.
     * 
     * Note: modifies variables depth, occur, branchedMoveNum, and tagdepth
     * 
     * @param c grouping character in sgf record
     */
    private void depths(char c) {

        // TODO: throw error if a valid grouping character is not passed in
        
        // Before changing depth and occur,
        // record those properties of the
        // parent node. This must be repeated
        // after a move is recorded since
        // depths is not encountered every move

        if (c == '[') {
            tagdepth++;
        }
        if (tagdepth == 0) {
            // Note: parenteses do not occur within square brackets in a valid
            // sgf file
            if (c == '(') {
                depth++;
                occur[depth]++;
                branchedMoveNum[depth] = moveNum;
            }
            if (c == ')') {
                depth--;
                moveNum = branchedMoveNum[depth + 1];
            }
        } else if (c == ']') {
            tagdepth--;
        }
    }

    private void move(String node) {
        boolean so;

        //Notice: only the last move needs to be
        //recorded, because the rest of sequence is
        //preserved!
        so = (depth == path[moveNum][0]
                && occur[depth] == path[moveNum][1]
                && moveNum == lastMove);
        if (so) {
            // Add the move coordinates to the current sequence.
            sequence = sequence + node + ";";
        }
        
        
        parent[0] = nextParent[moveNum][0];
        parent[1] = nextParent[moveNum][1];
        moveNum++;
        nextParent[moveNum][0] = depth;
        nextParent[moveNum][1] = occur[depth];

        handleBranches(moveNum-1);
    }

    /**
     * Prepare the sequenceExtractor to load a new sgf file
     */
    public void initialize() {
        depth = 0;
        for (int i = 0; i < maxMove; i++) {
            occur[i] = 0;
        }
        moveNum = 1;
        remLen = 0;
        lastMove = 1;
        lastDepth = 1;
        lastOccur = 1;
        numVari = 0;
        numSibs = 0;
        sequence = "";
        path[moveNum][0] = 1;
        path[moveNum][1] = 1;
        //Setting depth 0 as the parent
        //simply indicates that the empty
        //board is the parent node.
        nextParent[1][0] = 1;
        nextParent[1][1] = 1;

    }

    /**
     * Navigate through the game record.
     * @param amt indicate whether to move forward (positive values) or backward
     * (negative values)
     */
    public void rementLastMove(int amt) {
        //rement... as in DECrement and INCrement
        int temp = 0;
        if (amt != 0) {
            temp = amt / Math.abs(amt);
            if (
                (temp > 0 && numVari  > 0)
                    ||  (temp < 0 && lastMove > 1)) {
                lastMove += temp;
                if (temp == 1 && numVari > 1) {
                    lastDepth++;
                    lastOccur = 1;
                    //sequence remains unchanged.
                    //lastDepth and lastOccur do not change if
                    //the previous node does not branch.

                }
                if (temp == -1) {
                    
                    //the back button should not be
                    //clicked until the forward button
                    //is clicked. Fix this later.
                    
                    // Remove the last move from the sequence
                    sequence = sequence.substring(0, sequence.length() - 6);
                    
                    lastDepth = path[lastMove][0];
                    lastOccur = path[lastMove][1];
                    
                    path[lastMove + 1][0] = 0;
                    path[lastMove + 1][1] = 0;
                }
                resetComb();
            } else {
                System.err.println("Invalid node.");
            }


        }
    }

    public void rementLastOccur(int amt) {
        sequence = sequence.substring(0, sequence.length() - 3);
        int temp;
        lastOccur = (lastOccur + amt - 1) % numSibs + 1;
        path[lastMove][1] = lastOccur;
        resetComb();
    }

    public String getSequence() {

        String seq = String.valueOf(numSibs) + sequence;
        return seq;
    }

    
    public void calcState() {
        String temp;
        try {
            fstream = new FileReader(filename);
        } catch (Exception e) {
            System.err.println("Error reopening stream.");
        }

        BufferedReader in =
                new BufferedReader(fstream);

        try {
            while (in.ready()) {
                temp = in.readLine();
                feed(temp);
            }
        } catch (Exception e) {
            System.err.println("Something went wrong in calcState.");
        }
        temp = sequence;

    }

    private void resetComb() {
        path[lastMove][0] = lastDepth;
        path[lastMove][1] = lastOccur;
        moveNum = 1;
        depth = 0;
        tagdepth = 0;
        for (int i = 0; i < maxMove; i++) {
            occur[i] = 0;
            branchedMoveNum[i] = 0;
            nextParent[i][0] = 0;
            nextParent[i][1] = 0;
        }
        remainder = "";
        numSibs = 0;
        numVari = 0;
    }

    private void handleBranches(int moveNum) {
        if (moveNum == lastMove
                && parent[0] == path[lastMove - 1][0]
                && parent[1] == path[lastMove - 1][1]) {
            numSibs++;
        }
        if (moveNum == lastMove + 1
                && parent[0] == path[lastMove][0]
                && parent[1] == path[lastMove][1]) {
            numVari++;
        }
    }
}
