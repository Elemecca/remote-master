package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

// TODO: Auto-generated Javadoc
/**
 * The Class LearnedSignalDialog.
 */
public class LearnedSignalDialog
  extends JDialog
  implements ActionListener
{
  
  /**
   * Show dialog.
   * 
   * @param locationComp the location comp
   * @param learnedSignal the learned signal
   * 
   * @return the learned signal
   */
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

  /**
   * To string.
   * 
   * @param data the data
   * 
   * @return the string
   */
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

  /**
   * Instantiates a new learned signal dialog.
   * 
   * @param c the c
   */
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

  /**
   * Sets the learned signal.
   * 
   * @param learnedSignal the new learned signal
   */
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

  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed( ActionEvent event )
  {
    Object source = event.getSource();
    if ( source == okButton )
    {
      setVisible( false );
    }
    enableButtons();
  }

  /**
   * Enable buttons.
   */
  private void enableButtons()
  {
  }

  /** The ok button. */
  private JButton okButton = new JButton( "OK" );
  
  /** The burst text area. */
  private JTextArea burstTextArea = new JTextArea( 4, 70 );
  
  /** The duration text area. */
  private JTextArea durationTextArea = new JTextArea( 8, 70 );

  /** The learned signal. */
  private LearnedSignal learnedSignal = null;
  
  /** The model. */
  private DecodeTableModel model = new DecodeTableModel();
  
  /** The dialog. */
  private static LearnedSignalDialog dialog = null;
}
