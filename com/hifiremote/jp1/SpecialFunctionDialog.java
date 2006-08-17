package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class SpecialFunctionDialog
  extends JDialog
  implements ActionListener, FocusListener, Runnable, ItemListener
{
  public static SpecialProtocolFunction showDialog( JFrame frame,
                                    SpecialProtocolFunction function, RemoteConfiguration config )
  {
    if ( dialog == null )
      dialog = new SpecialFunctionDialog( frame );
    
    dialog.setRemoteConfiguration( config );
    dialog.setFunction( function );
    dialog.pack();
    dialog.setLocationRelativeTo( frame );
    dialog.setVisible( true );

    return dialog.function;
  }

  private void addToBox( JComponent j, Container c )
  {
    j.setAlignmentX( Component.LEFT_ALIGNMENT );
    c.add( j );
  }
 
  private SpecialFunctionDialog( JFrame frame ) 
  {
    super( frame, "Special Function", true );
    
    this.config = config;
    
    JComponent contentPane = ( JComponent )getContentPane();
    contentPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
    contentPane.setLayout( new BoxLayout( contentPane, BoxLayout.PAGE_AXIS ));
    
    // Add the bound device and key controls
    JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 0 ));
    addToBox( panel, contentPane );
    panel.setBorder( BorderFactory.createTitledBorder( "Bound Key" ));
    
    panel.add( new JLabel( "Device:" ));
    panel.add( boundDevice );
    
    panel.add( Box.createHorizontalStrut( 5 ));
    
    panel.add( new JLabel( "Key:" ));
    panel.add( boundKey );
  
    shift.addActionListener( this );
    panel.add( shift );
    
    xShift.addActionListener( this );
    panel.add( xShift );
    
    // Add the Parameters
    panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, 0 ));
    panel.add( Box.createHorizontalStrut( 5 ));
    addToBox( panel, contentPane );
    panel.setBorder( BorderFactory.createTitledBorder( "Parameters" ));
    Box box = Box.createVerticalBox();
    addToBox( new JLabel( "Type:" ), box );
    addToBox( type, box );
    panel.add( box );
    type.addItem( "ModeName" );
    type.addItem( "Multiplex" );
    type.addItem( "Pause" );
    type.addItem( "ToadTog" );
    type.addItem( "ULKP" );
    type.addItem( "UDKP" );
    type.addItem( "UDSM" );
    type.addActionListener( this );
    
    panel.add( parameterCard );
    
    box = box.createVerticalBox();
    addToBox( new JLabel( "Hex:" ), box );
    addToBox( hex, box );
    box.setBorder( null );
    panel.add( box );
    
    // ModeName parameter panel
    panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 10, 0 ));
    parameterCard.add( panel, "ModeName" );
    box = Box.createVerticalBox();
    addToBox( new JLabel( "Text:" ), box );
    addToBox( modeName, box );
    panel.add( box );
    
    // Multiplex parameter panel
    panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 10, 0 ));
    parameterCard.add( panel, "Multiplex" );
    box = Box.createVerticalBox();
    addToBox( new JLabel( "Device Type:" ), box );
    addToBox( deviceType, box );
    panel.add( box );
    
    DecimalFormat format = new DecimalFormat( "0000" );
    format.setParseIntegerOnly( true );
    format.setGroupingUsed( false );
    NumberFormatter formatter = new NumberFormatter( format );
    formatter.setValueClass( Integer.class );
    formatter.setMinimum( new Integer( 0 ));
    formatter.setMaximum( new Integer( 2047 ));
    setupCode = new JFormattedTextField( formatter );
    setupCode.setColumns( 4 );
    setupCode.addFocusListener( this );

    box = Box.createVerticalBox();
    addToBox( new JLabel( "Setup Code:" ), box );
    addToBox( setupCode, box );
    panel.add( box );
    
    // Pause parameter panel
    panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 10, 0 ));
    parameterCard.add( panel, "Pause" );
    box = Box.createVerticalBox();
    addToBox( new JLabel( "Duration:" ), box );
    addToBox( pauseDuration, box );
    panel.add( box );
    
    // ToadTog parameter panel
    panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 10, 0 ));
    parameterCard.add( panel, "ToadTog" );
    box = Box.createVerticalBox();
    addToBox( new JLabel( "Toggle #:" ), box );
    Integer[] toggles = { 0, 1, 2, 3, 4, 5, 6, 7 };
    toggle = new JComboBox( toggles );
    addToBox( toggle, box );
    panel.add( box );
    
    box = Box.createVerticalBox();
    addToBox( new JLabel( "Condition:" ), box );
    addToBox( condition, box );
    panel.add( box );
    
    // UDSM parameter panel
    panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 10, 0 ));
    parameterCard.add( panel, "UDSM" );
    box = Box.createVerticalBox();
    addToBox( new JLabel( "Key w/ Macro:" ), box );
    addToBox( macroKey, box );
    panel.add( box );
    macroKey.setRenderer( keyCodeRenderer );
    
    // ULDKP parameter panel
    panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 10, 0 ));
    parameterCard.add( panel, "ULDKP" );
    box = Box.createVerticalBox();
    addToBox( new JLabel( "Duration:" ), box );
    Integer[] durations = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
    uldkpDuration = new JComboBox( durations );
    addToBox( uldkpDuration, box );
    panel.add( box );
    box = Box.createVerticalBox();
    addToBox( firstLabel, box );
    addToBox( firstMacro, box );
    panel.add( box );
    firstMacro.setRenderer( keyCodeRenderer );
    box = Box.createVerticalBox();
    addToBox( secondLabel, box );
    addToBox( secondMacro, box );
    panel.add( box );
    secondMacro.setRenderer( keyCodeRenderer );
    
    // Add the Function definitions
    Box functionBox = Box.createVerticalBox();
    functionBox.setBorder( BorderFactory.createTitledBorder( "Function to Perform" ));
    addToBox( functionBox, contentPane );
    
    // Add the notes
    panel = new JPanel( new BorderLayout());
    addToBox( panel, contentPane );
    panel.setBorder( BorderFactory.createTitledBorder( "Notes" ));
    notes.setLineWrap( true );
    panel.add( new JScrollPane( notes, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
                                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER ));
    
    // Add the action buttons
    panel = new JPanel( new FlowLayout( FlowLayout.RIGHT ));
    addToBox( panel, contentPane );
    
    okButton.addActionListener( this );
    panel.add( okButton );

    cancelButton.addActionListener( this );
    panel.add( cancelButton );
  }
  
  private void setRemoteConfiguration( RemoteConfiguration config )
  {
    this.config = config;
    Remote remote = config.getRemote();
    shift.setText( remote.getShiftLabel());
    xShift.setText( remote.getXShiftLabel());
    xShift.setVisible( remote.getXShiftEnabled());
    boundDevice.setModel( new DefaultComboBoxModel( remote.getDeviceButtons()));
    boundKey.setModel( new DefaultComboBoxModel( remote.getUpgradeButtons()));
    deviceType.setModel( new DefaultComboBoxModel( remote.getDeviceTypes()));
    keyCodeRenderer.setRemote( remote );
    Vector< Integer >macroKeys = new Vector< Integer >();
    for ( Macro macro : config.getMacros())
      macroKeys.add( new Integer( macro.getKeyCode()));
    DefaultComboBoxModel model = new DefaultComboBoxModel( macroKeys ); 
    macroKey.setModel( model );
    firstMacro.setModel( model );
    secondMacro.setModel( model );
  }
  
  private void setFunction( SpecialProtocolFunction function )
  {
    this.function = null;
    if ( function == null )
    {
      cmd = null;
      boundDevice.setSelectedIndex( -1 );
      boundKey.setSelectedIndex( -1 );
      shift.setSelected( false );
      xShift.setSelected( false );
      
      return;
    }
    
    cmd = function.getCmd();
    if ( cmd != null )
      cmd = new Hex( cmd );
    boundDevice.setSelectedIndex( function.getDeviceButtonIndex());
    shift.setSelected( false );
    xShift.setSelected( false );
    setButton( function.getKeyCode(), boundKey, shift, xShift );
    notes.setText( function.getNotes());
  }
  
  private void setButton( int code, JComboBox comboBox, JCheckBox shiftBox, JCheckBox xShiftBox)
  {
    Remote remote = config.getRemote();
    Button b = remote.getButton( code );
    if ( b == null )
    {
      int base = code & 0x3F;
      if ( base != 0 )
      {
        b = remote.getButton( base );
        if (( base | remote.getShiftMask()) == code )
        {
          shiftBox.setEnabled( b.allowsShiftedMacro());
          shiftBox.setSelected( true );
          comboBox.setSelectedItem( b );
          return;
        }
        if ( remote.getXShiftEnabled() && (( base | remote.getXShiftMask()) == code ))
        {
          xShiftBox.setEnabled( remote.getXShiftEnabled() & b.allowsXShiftedMacro());
          xShiftBox.setSelected( true );
          comboBox.setSelectedItem( b );
          return;
        }
      }
      b = remote.getButton( code & ~remote.getShiftMask());
      if ( b != null )
        shiftBox.setSelected( true );
      else if ( remote.getXShiftEnabled())
      {
        b = remote.getButton( code ^ ~remote.getXShiftMask());
        if ( b != null )
          xShiftBox.setSelected( true );
      }
    }
      
    shiftBox.setEnabled( b.allowsShiftedKeyMove());
    xShiftBox.setEnabled( b.allowsXShiftedKeyMove());

    if ( b.getIsXShifted())
      xShiftBox.setSelected( true );      
    else if ( b.getIsShifted())
      shiftBox.setSelected( true );
    
    comboBox.removeActionListener( this );
    comboBox.setSelectedItem( b );  
    comboBox.addActionListener( this );
  }
  
  private int getKeyCode( JComboBox comboBox, JCheckBox shiftBox, JCheckBox xShiftBox)
  {
    int keyCode = (( Button )comboBox.getSelectedItem()).getKeyCode();
    if ( shiftBox.isSelected())
      keyCode |= config.getRemote().getShiftMask();
    else if ( xShiftBox.isSelected())
      keyCode |= config.getRemote().getXShiftMask();
    return keyCode;
  }
  
  private void showWarning( String message )
  {
    JOptionPane.showMessageDialog( this, message, "Missing Information", JOptionPane.ERROR_MESSAGE);
  }
  
  public void actionPerformed( ActionEvent event )
  {
    Object source = event.getSource();
    
    if ( source == type )
    {
      String typeStr = ( String )type.getSelectedItem();
      if ( typeStr.equals( "ULKP" ))
      {
        typeStr = "ULDKP";
        firstLabel.setText( "Short Key:" );
        secondLabel.setText( "Long Key:" );
      }
      else if ( typeStr.equals( "UDKP" ))
      {
        typeStr = "ULDKP";
        firstLabel.setText( "Single Key:" );
        secondLabel.setText( "Double Key:" );
      }
      CardLayout cl = ( CardLayout )parameterCard.getLayout();
      cl.show( parameterCard, typeStr );
    }
    else if ( source == okButton )
    {
      int deviceIndex = boundDevice.getSelectedIndex();
      if ( deviceIndex == -1 )
      {
        showWarning( "You must select a device for the bound key." );
        return;

      }
      if ( boundKey.getSelectedItem() == null )
      {
        showWarning( "You must select a key for the bound key." );
        return;
      }
      int keyCode = getKeyCode( boundKey, shift, xShift );

      String notesStr = notes.getText();
      Remote remote = config.getRemote();
      setVisible( false );
    }
    else if ( source == cancelButton )
    {
      function = null;
      setVisible( false );
    }
    else if ( source == shift )
    {
      if ( shift.isSelected())
        xShift.setSelected( false );
    }
    else if ( source == xShift )
    {
      if ( xShift.isSelected())
        shift.setSelected( false );
    }
  }
  
  // ItemListener
  public void itemStateChanged( ItemEvent e )
  {
    if ( e.getStateChange() != ItemEvent.SELECTED ) 
      return;
    
    Object source = e.getSource();
  }

  // FocusListener
  public void focusGained( FocusEvent e )
  {
    focusField = ( JTextField )e.getSource();
    SwingUtilities.invokeLater( this );
  }

  public void focusLost( FocusEvent e )
  {
    // intentionally left empty
  }

  // Runnable
  public void run()
  {
    focusField.selectAll();
  }  
  
  private JComboBox boundDevice = new JComboBox();
  private JComboBox boundKey = new JComboBox();
  private JCheckBox shift = new JCheckBox();
  private JCheckBox xShift = new JCheckBox();
  private JComboBox type = new JComboBox();
  private JPanel parameterCard = new JPanel( new CardLayout());
  private JTextField hex = new JTextField( 12 );

  // for ModeName
  private JTextField modeName = new JTextField( 10 );
  
  // for Multiplex
  private JComboBox deviceType = new JComboBox();
  private JFormattedTextField setupCode = null;

  // for Pause
  private JSpinner pauseDuration = new JSpinner( new WrappingSpinnerNumberModel( 0, 0, 255, 1 ));

  // for ToadTog
  private JComboBox toggle = null;
  private JComboBox condition = new JComboBox( ToadTogFunction.styleStrings );
  
  // for UDSM
  private JComboBox macroKey = new JComboBox();
  private KeyCodeListRenderer keyCodeRenderer = new KeyCodeListRenderer();
  
  // for ULDKP
  private JComboBox uldkpDuration = null;
  private JLabel firstLabel = new JLabel();
  private JComboBox firstMacro = new JComboBox();
  private JLabel secondLabel = new JLabel();
  private JComboBox secondMacro = new JComboBox();

  private JTextArea notes = new JTextArea( 2, 10 );

  // action Buttons
  private JButton okButton = new JButton( "OK" );
  private JButton cancelButton = new JButton( "Cancel" );
  private JTextField focusField = null;
  
  private RemoteConfiguration config = null;
  private SpecialProtocolFunction function = null;
  private Hex cmd = null;
  private static SpecialFunctionDialog dialog = null;
}
