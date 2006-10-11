package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.text.*;

public class LearnedSignalDialog
  extends JDialog
  implements ActionListener
{
  public static LearnedSignal showDialog( Component locationComp,
                                          LearnedSignal learnedSignal )
  {
    if ( dialog == null )
      dialog = new LearnedSignalDialog( locationComp );

    dialog.setLearnedSignal( learnedSignal );

    dialog.pack();
    dialog.setLocationRelativeTo( locationComp );
    dialog.setVisible( true );
    return dialog.learnedSignal;
  }

  private static String toString( int[] data )
  {
    int[] charPos = new int[ data.length ];

    StringBuilder str = new StringBuilder();
    if ( data != null && data.length != 0)
    {
      for (int i = 0; i < data.length; i++)
      {
        if (( i > 0 ) && (( i & 1 ) == 0 ))
          str.append(" ");
        charPos[i] = str.length();
        str.append((( i & 1 ) == 0 ? +1 : -1 ) * data[ i ]);
      }
    /*
      for (int i = 0; i < positions.size(); i++)
      {
        int[] pos = positions.get(i);
        System.out.println("pos[] = " + pos[0] + ", " + pos[1] );
        pos[2] = charPos[ 2 * pos[0] ];
        pos[3] = charPos[ 2 * pos[1] - 1 ] - 1;
      }
      */
    }
    if (str.length() == 0)
      return "** No signal **";
    return str.toString();
  }

  private LearnedSignalDialog( Component c )
  {
    super(( JFrame )SwingUtilities.getRoot( c ));
    setTitle( "Learned Signal Details" );
    setModal( true );

    JComponent contentPane = ( JComponent )getContentPane();
    contentPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));

    JP1Table table = new JP1Table( model );
    table.setCellSelectionEnabled( false );
    table.setRowSelectionAllowed( true );
    Dimension d = table.getPreferredScrollableViewportSize();
    d.width = table.getPreferredSize().width;
    d.height = 8 * table.getRowHeight();
    table.setPreferredScrollableViewportSize( d );
    table.initColumns( model );
    JScrollPane scrollPane = new JScrollPane( table );
    scrollPane.setBorder(
      BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder( "Decodes" ),
        scrollPane.getBorder()));
    contentPane.add( scrollPane, BorderLayout.CENTER );

    Box bottomPanel = Box.createVerticalBox();
    contentPane.add( bottomPanel, BorderLayout.SOUTH );
    bottomPanel.setBorder( BorderFactory.createTitledBorder( "Advanced Details" ));

    burstTextArea.setEditable( false );
    burstTextArea.setLineWrap( true );
    burstTextArea.setWrapStyleWord( true );
    scrollPane = new JScrollPane( burstTextArea );
    scrollPane.setBorder(
      BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder( "Bursts" ),
        scrollPane.getBorder()));
    bottomPanel.add( scrollPane );

    durationTextArea.setEditable( false );
    durationTextArea.setLineWrap( true );
    durationTextArea.setWrapStyleWord( true );
    scrollPane = new JScrollPane( durationTextArea );
    scrollPane.setBorder(
      BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder( "Durations" ),
        scrollPane.getBorder()));
    bottomPanel.add( scrollPane );

    // Add the action buttons
    JPanel panel = new JPanel( new FlowLayout( FlowLayout.RIGHT ));
    bottomPanel.add( panel );

    okButton.addActionListener( this );
    panel.add( okButton );
  }

  private void setLearnedSignal( LearnedSignal learnedSignal )
  {
    this.learnedSignal = null;

    if ( learnedSignal == null )
    {
      enableButtons();
      return;
    }

    model.set( learnedSignal );
    UnpackLearned ul = learnedSignal.getUnpackLearned();
    burstTextArea.setText( toString( ul.bursts ));
    durationTextArea.setText( toString( ul.durations ));

    enableButtons();
  }

  public void actionPerformed( ActionEvent event )
  {
    Object source = event.getSource();
    if ( source == okButton )
    {
      setVisible( false );
    }
    enableButtons();
  }

  private void enableButtons()
  {
  }

  private JButton okButton = new JButton( "OK" );
  private JTextArea burstTextArea = new JTextArea( 4, 70 );
  private JTextArea durationTextArea = new JTextArea( 8, 70 );

  private LearnedSignal learnedSignal = null;
  private DecodeTableModel model = new DecodeTableModel();
  private static LearnedSignalDialog dialog = null;
}
