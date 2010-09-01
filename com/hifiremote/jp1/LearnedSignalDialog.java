package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

// TODO: Auto-generated Javadoc
/**
 * The Class LearnedSignalDialog.
 */
public class LearnedSignalDialog extends JDialog implements ActionListener
{

  /**
   * Show dialog.
   * 
   * @param locationComp
   *          the location comp
   * @param learnedSignal
   *          the learned signal
   * @return the learned signal
   */
  public static LearnedSignal showDialog( Component locationComp, LearnedSignal learnedSignal,
      RemoteConfiguration config )
  {
    if ( dialog == null )
    {
      dialog = new LearnedSignalDialog( locationComp );
    }

    dialog.setRemoteConfiguration( config );
    dialog.setLearnedSignal( learnedSignal );

    dialog.pack();
    dialog.setLocationRelativeTo( locationComp );
    dialog.setVisible( true );
    return dialog.learnedSignal;
  }
  
  public static void reset()
  {
    if ( dialog != null )
    {
      dialog.dispose();
      dialog = null;
    }
  }

  /**
   * To string.
   * 
   * @param data
   *          the data
   * @return the string
   */
  private static String toString( int[] data )
  {
    int[] charPos = new int[ data.length ];

    StringBuilder str = new StringBuilder();
    if ( data != null && data.length != 0 )
    {
      for ( int i = 0; i < data.length; i++ )
      {
        if ( i > 0 && ( i & 1 ) == 0 )
        {
          str.append( " " );
        }
        charPos[ i ] = str.length();
        str.append( ( ( i & 1 ) == 0 ? +1 : -1 ) * data[ i ] );
      }
      /*
       * for (int i = 0; i < positions.size(); i++) { int[] pos = positions.get(i); System.out.println("pos[] = " +
       * pos[0] + ", " + pos[1] ); pos[2] = charPos[ 2 pos[0] ]; pos[3] = charPos[ 2 pos[1] - 1 ] - 1; }
       */
    }
    if ( str.length() == 0 )
    {
      return "** No signal **";
    }
    return str.toString();
  }

  /**
   * Instantiates a new learned signal dialog.
   * 
   * @param c
   *          the c
   */
  private LearnedSignalDialog( Component c )
  {
    super( ( JFrame )SwingUtilities.getRoot( c ) );
    setTitle( "Learned Signal Details" );
    setModal( true );

    JComponent contentPane = ( JComponent )getContentPane();
    contentPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

    // Add the bound device and key controls
    JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 0 ) );
    panel.setAlignmentX( Component.LEFT_ALIGNMENT );
    contentPane.add( panel, BorderLayout.PAGE_START );
    panel.setBorder( BorderFactory.createTitledBorder( "Bound Key" ) );

    panel.add( new JLabel( "Device:" ) );
    panel.add( boundDevice );

    panel.add( Box.createHorizontalStrut( 5 ) );

    panel.add( new JLabel( "Key:" ) );
    panel.add( boundKey );

    shift.addActionListener( this );
    panel.add( shift );

    xShift.addActionListener( this );
    panel.add( xShift );

    table = new JP1Table( model );
    table.setCellSelectionEnabled( false );
    table.setRowSelectionAllowed( true );
    Dimension d = table.getPreferredScrollableViewportSize();
    d.width = table.getPreferredSize().width;
    d.height = 8 * table.getRowHeight();
    table.setPreferredScrollableViewportSize( d );
    table.initColumns( model );
    JScrollPane scrollPane = new JScrollPane( table );
    scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Decodes" ), scrollPane
        .getBorder() ) );
    contentPane.add( scrollPane, BorderLayout.CENTER );

    Box bottomPanel = Box.createVerticalBox();
    contentPane.add( bottomPanel, BorderLayout.SOUTH );
    bottomPanel.setBorder( BorderFactory.createTitledBorder( "Advanced Details" ) );

    burstTextArea.setEditable( false );
    burstTextArea.setLineWrap( true );
    burstTextArea.setWrapStyleWord( true );
    scrollPane = new JScrollPane( burstTextArea );
    scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Bursts" ), scrollPane
        .getBorder() ) );
    bottomPanel.add( scrollPane );

    durationTextArea.setEditable( false );
    durationTextArea.setLineWrap( true );
    durationTextArea.setWrapStyleWord( true );
    scrollPane = new JScrollPane( durationTextArea );
    scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Durations" ),
        scrollPane.getBorder() ) );
    bottomPanel.add( scrollPane );

    // Add the action buttons
    JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
    bottomPanel.add( buttonPanel );

    okButton.addActionListener( this );
    buttonPanel.add( okButton );

    cancelButton.addActionListener( this );
    buttonPanel.add( cancelButton );
  }

  /**
   * Sets the learned signal.
   * 
   * @param learnedSignal
   *          the new learned signal
   */
  private void setLearnedSignal( LearnedSignal learnedSignal )
  {
    this.learnedSignal = learnedSignal;

    boundDevice.setSelectedIndex( learnedSignal.getDeviceButtonIndex() );
    setButton( learnedSignal.getKeyCode(), boundKey, shift, xShift );
    model.set( learnedSignal );
    table.initColumns( model );
    UnpackLearned ul = learnedSignal.getUnpackLearned();
    burstTextArea.setText( toString( ul.bursts ) );
    durationTextArea.setText( toString( ul.durations ) );
  }

  private void setRemoteConfiguration( RemoteConfiguration config )
  {
    this.config = config;
    Remote remote = config.getRemote();
    shift.setText( remote.getShiftLabel() );
    xShift.setText( remote.getXShiftLabel() );
    xShift.setVisible( remote.getXShiftEnabled() );
    boundDevice.setModel( new DefaultComboBoxModel( remote.getDeviceButtons() ) );
    boundKey.setModel( new DefaultComboBoxModel( remote.getUpgradeButtons() ) );
  }

  private void setButton( int code, JComboBox comboBox, JCheckBox shiftBox, JCheckBox xShiftBox )
  {
    Remote remote = config.getRemote();
    Button b = remote.getButton( code );
    if ( b == null )
    {
      int base = code & 0x3F;
      if ( base != 0 )
      {
        b = remote.getButton( base );
        if ( ( base | remote.getShiftMask() ) == code )
        {
          shiftBox.setEnabled( b.allowsShiftedMacro() );
          shiftBox.setSelected( true );
          comboBox.setSelectedItem( b );
          return;
        }
        if ( remote.getXShiftEnabled() && ( base | remote.getXShiftMask() ) == code )
        {
          xShiftBox.setEnabled( remote.getXShiftEnabled() & b.allowsXShiftedMacro() );
          xShiftBox.setSelected( true );
          comboBox.setSelectedItem( b );
          return;
        }
      }
      b = remote.getButton( code & ~remote.getShiftMask() );
      if ( b != null )
      {
        shiftBox.setSelected( true );
      }
      else if ( remote.getXShiftEnabled() )
      {
        b = remote.getButton( code ^ ~remote.getXShiftMask() );
        if ( b != null )
        {
          xShiftBox.setSelected( true );
        }
      }
    }

    shiftBox.setEnabled( b.allowsShiftedKeyMove() );
    xShiftBox.setEnabled( b.allowsXShiftedKeyMove() );

    if ( b.getIsXShifted() )
    {
      xShiftBox.setSelected( true );
    }
    else if ( b.getIsShifted() )
    {
      shiftBox.setSelected( true );
    }

    comboBox.removeActionListener( this );
    comboBox.setSelectedItem( b );
    comboBox.addActionListener( this );
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed( ActionEvent event )
  {
    Object source = event.getSource();
    if ( source == okButton )
    {
      int deviceIndex = boundDevice.getSelectedIndex();
      learnedSignal.setDeviceButtonIndex( deviceIndex );
      int keyCode = getKeyCode( boundKey, shift, xShift );
      learnedSignal.setKeyCode( keyCode );
      setVisible( false );
    }
    else if ( source == cancelButton )
    {
      learnedSignal = null;
      setVisible( false );
    }
  }

  private int getKeyCode( JComboBox comboBox, JCheckBox shiftBox, JCheckBox xShiftBox )
  {
    int keyCode = ( ( Button )comboBox.getSelectedItem() ).getKeyCode();
    if ( shiftBox.isSelected() )
    {
      keyCode |= config.getRemote().getShiftMask();
    }
    else if ( xShiftBox.isSelected() )
    {
      keyCode |= config.getRemote().getXShiftMask();
    }
    return keyCode;
  }

  private RemoteConfiguration config = null;

  /** The bound device. */
  private JComboBox boundDevice = new JComboBox();

  /** The bound key. */
  private JComboBox boundKey = new JComboBox();

  /** The shift. */
  private JCheckBox shift = new JCheckBox();

  /** The x shift. */
  private JCheckBox xShift = new JCheckBox();

  /** The ok button. */
  private JButton okButton = new JButton( "OK" );

  private JButton cancelButton = new JButton( "Cancel" );

  /** The burst text area. */
  private JTextArea burstTextArea = new JTextArea( 4, 70 );

  /** The duration text area. */
  private JTextArea durationTextArea = new JTextArea( 8, 70 );

  /** The learned signal. */
  private LearnedSignal learnedSignal = null;

  /** The model. */
  private JP1Table table = null;
  private DecodeTableModel model = new DecodeTableModel();

  /** The dialog. */
  private static LearnedSignalDialog dialog = null;
}
