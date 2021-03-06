/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SGFParser.java
 *
 * Created on Mar 3, 2011, 8:47:22 PM
 */
package com.moonjob.sgfparser.ui;

import javax.swing.JFileChooser;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.moonjob.sgfparser.lib.Board;
import com.moonjob.sgfparser.lib.sequenceExtractor;

/**
 *
 * @author Jocelyn
 */
public class SGFParser extends javax.swing.JFrame
                        implements ActionListener{
    private JButton fileBtn = new JButton();
    private JButton prevBranchBtn = new JButton();
    private JButton nextBranchBtn = new JButton();
    private JButton backBtn = new JButton();
    private JButton nextBtn = new JButton();
    private Board goban;
    private sequenceExtractor Xtract;

    /** Creates new form SGFParser */
    public SGFParser() {
        initComponents();
        goban = new Board();
        Xtract = new sequenceExtractor(1,1);
        button1.addActionListener(this);
        button2.addActionListener(this);
        button3.addActionListener(this);
        button4.addActionListener(this);
        button5.addActionListener(this);

        fileBtn = button1;
        prevBranchBtn = button2;
        nextBranchBtn = button3;
        backBtn = button4;
        nextBtn = button5;
    }

    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel1 = new javax.swing.JPanel();
        SGFLbl = new javax.swing.JLabel();
        SGFText = new javax.swing.JTextField();
        button1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        gobanText = new javax.swing.JTextArea();
        button2 = new javax.swing.JButton();
        button4 = new javax.swing.JButton();
        button5 = new javax.swing.JButton();
        button3 = new javax.swing.JButton();
        varsTxt = new javax.swing.JTextField();
        varsLbl = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        SGFLbl.setText("SGF File");

        button1.setText("Browse...");

        gobanText.setBackground(new java.awt.Color(244, 210, 117));
        gobanText.setColumns(20);
        gobanText.setRows(5);
        gobanText.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        gobanText.setEnabled(false);
        jScrollPane1.setViewportView(gobanText);

        button2.setText("Prev Branch");

        button4.setText("<-");

        button5.setText("->");

        button3.setText("Next Branch");

        varsTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                varsTxtActionPerformed(evt);
            }
        });

        varsLbl.setText("Variations:");
        varsLbl.setToolTipText("The number of alternatives to this move.");

        org.jdesktop.layout.GroupLayout panel1Layout = new org.jdesktop.layout.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(panel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(panel1Layout.createSequentialGroup()
                        .add(SGFLbl)
                        .add(18, 18, 18)
                        .add(SGFText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 135, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(button1))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, panel1Layout.createSequentialGroup()
                        .add(panel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(panel1Layout.createSequentialGroup()
                                .add(varsLbl)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(varsTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(panel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, panel1Layout.createSequentialGroup()
                                    .add(button4, 0, 0, Short.MAX_VALUE)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(button5, 0, 0, Short.MAX_VALUE))
                                .add(button3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(button2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 316, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(13, 13, 13)))
                .addContainerGap())
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(panel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(SGFLbl)
                    .add(SGFText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(button1))
                .add(43, 43, 43)
                .add(panel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(panel1Layout.createSequentialGroup()
                        .add(button2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(button3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(panel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(button4)
                            .add(button5))
                        .add(18, 18, 18)
                        .add(panel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(varsTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(varsLbl)))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(panel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(panel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void varsTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_varsTxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_varsTxtActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new SGFParser().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel SGFLbl;
    private javax.swing.JTextField SGFText;
    private javax.swing.JButton button1;
    private javax.swing.JButton button2;
    private javax.swing.JButton button3;
    private javax.swing.JButton button4;
    private javax.swing.JButton button5;
    private javax.swing.JTextArea gobanText;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel panel1;
    private javax.swing.JLabel varsLbl;
    private javax.swing.JTextField varsTxt;
    // End of variables declaration//GEN-END:variables

    public void actionPerformed(ActionEvent ae) {
        JButton button = (JButton) ae.getSource();
        if (button==fileBtn){
            JFileChooser c = new JFileChooser();
            int rVal = c.showOpenDialog(SGFParser.this);
            if(rVal == JFileChooser.APPROVE_OPTION){
                SGFText.setText(c.getSelectedFile().getName());
                Xtract.initialize();
                Xtract.loadSGF(c.getCurrentDirectory()+"/"+c.getSelectedFile().getName());

            }
        }
        else if(button == prevBranchBtn){
            Xtract.rementLastOccur(-1);
        }
        else if(button == nextBranchBtn){
            Xtract.rementLastOccur(1);
        }
        else if(button == backBtn)
        {
            Xtract.rementLastMove(-1);
            
        }
        else if(button == nextBtn){
            Xtract.rementLastMove(1);
        }
        
        Xtract.calcState();
        String seq = Xtract.getSequence();
        goban.loadSeq(seq);
        String display = goban.printGoban();
        gobanText.setText(display);
        //gobanText.setCaretPosition(gobanText.getDocument().getLength());
        varsTxt.setText(goban.getNumVari());
    }
}
