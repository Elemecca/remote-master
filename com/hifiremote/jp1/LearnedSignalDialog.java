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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// TODO: Auto-generated Javadoc
/**
 * The Class LearnedSignalDialog.
 */
public class LearnedSignalDialog extends JDialog implements ActionListener, DocumentListener
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
    
    // Set preferred size of advanced button to that for the wider 
    // of the two possible button captions
    dialog.setAdvancedButtonText( false );
    dialog.pack();
    dialog.advancedButton.setPreferredSize( dialog.advancedButton.getSize() );
    dialog.setAdvancedButtonText( dialog.advancedArea.isVisible() );
    dialog.applyButton.setEnabled( false );
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
        // Format changed to allow pasting to IRScope as timing list
        if ( i > 0 /* && ( i & 1 ) == 0 */ )
        {
          str.append( " " );
        }
        charPos[ i ] = str.length();
        // str.append( ( ( i & 1 ) == 0 ? +1 : -1 ) * data[ i ] ); 
        str.append( ( i & 1 ) == 0 ? "+" : "-" );
        str.append( data[ i ] );
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

    JPanel topPanel = new JPanel( new BorderLayout() );
    contentPane.add( topPanel, BorderLayout.PAGE_START );
    
    // Add the bound device and key controls
    JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 0 ) );
    panel.setAlignmentX( Component.LEFT_ALIGNMENT );
    panel.setBorder( BorderFactory.createTitledBorder( "Bound Key" ) );
    panel.add( new JLabel( "Device:" ) );
    panel.add( boundDevice );
    panel.add( Box.createHorizontalStrut( 5 ) );
    panel.add( new JLabel( "Key:" ) );
    panel.add( boundKey );
    panel.add( shift );
    panel.add( xShift );    
    topPanel.add( panel, BorderLayout.LINE_START );
    boundKey.addActionListener( this );
    shift.addActionListener( this );
    xShift.addActionListener( this );
    
    topPanel.add( advancedButton, BorderLayout.LINE_END );
    advancedButton.setToolTipText( "Shows or hides the signal timing details" );
    advancedButton.addActionListener( this );
        
    signalTextArea.setEditable( true );
    signalTextArea.setLineWrap( true );
    signalTextArea.setWrapStyleWord( true );
    signalTextArea.getDocument().addDocumentListener( this );
    signalTextArea.setToolTipText( "Edits to Signal Data do not take effect until you press Apply or OK" );
    JScrollPane scrollPane = new JScrollPane( signalTextArea );
    scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Signal Data" ), scrollPane
        .getBorder() ) );
    topPanel.add( scrollPane, BorderLayout.PAGE_END );
    
    table = new JP1Table( model );
    table.setCellSelectionEnabled( false );
    table.setRowSelectionAllowed( true );
    Dimension d = table.getPreferredScrollableViewportSize();
    d.width = table.getPreferredSize().width;
    d.height = 8 * table.getRowHeight();
    table.setPreferredScrollableViewportSize( d );
    table.initColumns( model );
    scrollPane = new JScrollPane( table );
    scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Decodes" ), scrollPane
        .getBorder() ) );
    contentPane.add( scrollPane, BorderLayout.CENTER );
    
    advancedArea = Box.createVerticalBox();
    advancedArea.setBorder( BorderFactory.createTitledBorder( "Advanced Details" ) );

    burstTextArea.setEditable( false );
    burstTextArea.setLineWrap( true );
    burstTextArea.setWrapStyleWord( true );
    scrollPane = new JScrollPane( burstTextArea );
    scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Bursts" ), scrollPane
        .getBorder() ) );
    advancedArea.add( scrollPane );

    durationTextArea.setEditable( false );
    durationTextArea.setLineWrap( true );
    durationTextArea.setWrapStyleWord( true );
    scrollPane = new JScrollPane( durationTextArea );
    scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Durations" ),
        scrollPane.getBorder() ) );
    advancedArea.add( scrollPane );  
    
    Box bottomBox = Box.createVerticalBox();
    contentPane.add( bottomBox, BorderLayout.PAGE_END );
    bottomBox.add( advancedArea );
    
    // Add the action buttons
    JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
    bottomBox.add( buttonPanel );
    
    applyButton.addActionListener( this );
    applyButton.setEnabled( false );
    applyButton.setToolTipText( "Apply edits made in the Signal Data panel without closing dialog" );
    buttonPanel.add( applyButton );

    okButton.addActionListener( this );
    okButton.setToolTipText( "Apply all edits and exit dialog" );
    buttonPanel.add( okButton );

    cancelButton.addActionListener( this );
    cancelButton.setToolTipText( "Abandon all edits and exit dialog" );
    buttonPanel.add( cancelButton );
    
    advancedArea.setVisible( false );
    setAdvancedButtonText( advancedArea.isVisible() );
  }

  /**
   * Sets the learned signal.
   * 
   * @param learnedSignal
   *          the new learned signal
   */
  private void setLearnedSignal( LearnedSignal learnedSignal )
  {
    table.initColumns( model );
    if ( learnedSignal == null )
    {
      this.learnedSignal = new LearnedSignal( 0, 0, new Hex(), null );
      boundKey.setSelectedIndex( 0 );
      shift.setSelected( false );
      xShift.setSelected( false );
      model.set( this.learnedSignal );
      signalTextArea.setText( null );
      burstTextArea.setText( null );
      durationTextArea.setText( null );     
      return;
    }
    this.learnedSignal = learnedSignal;
    Remote remote = config.getRemote();
    boundDevice.setSelectedItem( remote.getDeviceButton( learnedSignal.getDeviceButtonIndex() ) );
    setButton( learnedSignal.getKeyCode(), boundKey, shift, xShift );
    model.set( learnedSignal );
    signalTextArea.setText( learnedSignal.getSignalHex( config.getRemote() ).toString() );
    UnpackLearned ul = learnedSignal.getUnpackLearned();
    if ( ul.ok )
    {
      burstTextArea.setText( toString( ul.bursts ) );
      durationTextArea.setText( toString( ul.durations ) );
    }
  }

  private void setRemoteConfiguration( RemoteConfiguration config )
  {
    this.config = config;
    Remote remote = config.getRemote();
    shift.setText( remote.getShiftLabel() );
    xShift.setText( remote.getXShiftLabel() );
    xShift.setVisible( remote.getXShiftEnabled() );
    boundDevice.setModel( new DefaultComboBoxModel( remote.getDeviceButtons() ) );
    boundKey.setModel( new DefaultComboBoxModel( remote.getLearnButtons() ) );
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
    Remote remote = config.getRemote();
    Button b = ( Button )boundKey.getSelectedItem();
    UnpackLearned ul = null;
    
    if ( source == applyButton || source == okButton )
    {
      String notes = learnedSignal.getNotes();
      int deviceIndex = ( ( DeviceButton )boundDevice.getSelectedItem() ).getButtonIndex();
      int keyCode = getKeyCode( boundKey, shift, xShift );
      short[] data = Hex.parseHex( signalTextArea.getText() );
      learnedSignal = new LearnedSignal( keyCode, deviceIndex, ( new Hex( data ) ).subHex( 3 ), notes );
      ul = learnedSignal.getUnpackLearned();
      if ( config.hasSegments() )
      {
        // set default value
        learnedSignal.setSegmentFlags( 0xFF );
      }
      if ( ! ul.ok )
      {
        String message = "Malformed learned signal: " + ul.error;
        String title = "Learned Signal Error";
        JOptionPane.showMessageDialog( this, message, title, JOptionPane.ERROR_MESSAGE );
      }
    }
    
    if ( source == applyButton && ul.ok )
    {
      burstTextArea.setText( toString( ul.bursts ) );
      durationTextArea.setText( toString( ul.durations ) );
      model.set( learnedSignal );
      applyButton.setEnabled( false );
    }
    else if ( source == okButton && ul.ok )
    {
      setVisible( false );
    }
    else if ( source == cancelButton )
    {
      learnedSignal = null;
      setVisible( false );
    }
    else if ( source == advancedButton )
    {
      advancedArea.setVisible( ! advancedArea.isVisible() );
      setAdvancedButtonText( advancedArea.isVisible() );
      pack(); 
    }
    else if ( source == shift )
    {
      if ( shift.isSelected() )
      {
        xShift.setSelected( false );
      }
      else if ( b != null && remote.getXShiftEnabled() )
      {       
        xShift.setSelected( b.needsShift( Button.LEARN_BIND ) );
      }
    }
    else if ( source == xShift )
    {
      if ( xShift.isSelected() )
      {
        shift.setSelected( false );
      }
      else if ( b != null )
      {
        shift.setSelected( b.needsShift( Button.LEARN_BIND ) );
      }
    }    
    else if ( source == boundKey )
    {
      if ( b != null )
      {
        b.setShiftBoxes( Button.LEARN_BIND, shift, xShift );
      }
    } 
  }
  
  private void setAdvancedButtonText( boolean hide )
  {
    String text = "<html><center>";
    text += hide ? "Hide " : "Show ";
    text += "Advanced<br>Details</center></html>";
    advancedButton.setText( text );
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
  
  private void documentChanged( DocumentEvent e )
  {
    applyButton.setEnabled( true );
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
  
  private JButton applyButton = new JButton( "Apply" );
  
  private JButton advancedButton = new JButton();
  
  private Box advancedArea = null;

  /** The burst text area. */
  private JTextArea burstTextArea = new JTextArea( 4, 70 );

  /** The duration text area. */
  private JTextArea durationTextArea = new JTextArea( 8, 70 );
  
  private JTextArea signalTextArea = new JTextArea( 4, 70 );

  /** The learned signal. */
  private LearnedSignal learnedSignal = null;

  /** The model. */
  private JP1Table table = null;
  private DecodeTableModel model = new DecodeTableModel();

  /** The dialog. */
  private static LearnedSignalDialog dialog = null;

  @Override
  public void changedUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }

  @Override
  public void insertUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }

  @Override
  public void removeUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }

}
