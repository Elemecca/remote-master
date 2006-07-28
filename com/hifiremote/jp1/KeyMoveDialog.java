package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.text.*;

public class KeyMoveDialog
  extends JDialog
  implements ActionListener, FocusListener, Runnable, ItemListener
{
  public static KeyMove showDialog( Component locationComp,
                                    KeyMove keyMove, RemoteConfiguration config )
  {
    if ( dialog == null )
      dialog = new KeyMoveDialog( locationComp );
    
    dialog.setRemoteConfiguration( config );
    dialog.setKeyMove( keyMove );
    dialog.setLocationRelativeTo( locationComp );

    dialog.pack();
    if ( locationComp instanceof JPanel )
    {
      Rectangle rect = dialog.getBounds();
      int x = rect.x - rect.width / 2;
      if ( x < 0 ) x = 10;
      int y = rect.y - rect.height / 2;
      if ( y < 0 ) y = 10;
      dialog.setLocation( x, y );
    }
    dialog.setVisible( true );
    return dialog.keyMove;
  }

  private KeyMoveDialog( Component c ) 
  {
    super(( JFrame )SwingUtilities.getRoot( c ));
    setTitle( "Key Move" );
    setModal( true );
    
    this.config = config;
    
    JComponent contentPane = ( JComponent )getContentPane();
    contentPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
    contentPane.setLayout( new BoxLayout( contentPane, BoxLayout.PAGE_AXIS ));
    
    // Add the bound device and key controls
    JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT ));
    panel.setAlignmentX( Component.LEFT_ALIGNMENT );
    contentPane.add( panel );
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
    
    // Add the Function definitions
    Box functionBox = Box.createVerticalBox();
    functionBox.setBorder( BorderFactory.createTitledBorder( "Function to Perform" ));
    contentPane.add( functionBox );
    
    Box deviceBox = Box.createVerticalBox();
    functionBox.add( deviceBox );
    deviceBox.setBorder( BorderFactory.createTitledBorder( "Device" ));
    JLabel label = new JLabel( "Double-click a row to select a type/code, or enter a device below." );
    label.setAlignmentX( Component.LEFT_ALIGNMENT );
    deviceBox.add( label );
    deviceBox.add( Box.createVerticalStrut( 5 ));
    
    model.setEditable( false );
    table.setColumnSelectionAllowed( false );
    table.initColumns( model );
    MouseAdapter ma = new MouseAdapter()
    {
      public void mouseClicked( MouseEvent e )
      {
        if ( e.getClickCount() != 2 )
          return;
        int row = table.rowAtPoint( e.getPoint());
        if ( row != -1 )
        {
          deviceType.setSelectedItem( table.getValueAt( row, 2 ));
          setupCode.setValue( new Integer((( SetupCode )table.getValueAt( row, 3 )).getValue()));
        }
      }
    };
    table.addMouseListener( ma );
    Dimension d = table.getPreferredScrollableViewportSize();
    d.width = table.getPreferredSize().width;
    d.height = 8 * table.getRowHeight();
    table.setPreferredScrollableViewportSize( d );
    JScrollPane scroll = new JScrollPane( table );
    scroll.setAlignmentX( Component.LEFT_ALIGNMENT );
    deviceBox.add( scroll );
    deviceBox.add( Box.createVerticalStrut( 10 ));
    
    panel = new JPanel( new FlowLayout( FlowLayout.LEFT ));
    panel.setAlignmentX( Component.LEFT_ALIGNMENT );
    deviceBox.add( panel );
    label = new JLabel( "Device Type:" ); 
    panel.add( label );
    panel.add( deviceType );
    label.setLabelFor( deviceType );
    panel.add( Box.createHorizontalStrut( 5 ));
    label = new JLabel( "Setup Code:" );
    panel.add( label );

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
    label.setLabelFor( setupCode );
    panel.add( setupCode );
    
    // Add the EFC/Hex/Key controls
    panel = new JPanel( new FlowLayout( FlowLayout.LEFT ));
    panel.setAlignmentX( Component.LEFT_ALIGNMENT );
    functionBox.add( panel );
    
    ButtonGroup group = new ButtonGroup();
    
    useEFC.addItemListener( this );
    panel.add( useEFC );
    group.add( useEFC );
    
    useHex.addItemListener( this );
    panel.add( useHex );
    group.add( useHex );
    
    useKey.addItemListener( this );
    panel.add( useKey );
    group.add( useKey );
    
    panel.add( efcHexField );
    efcHexField.addFocusListener( this );
    panel.add( movedKey );
    
    shiftMovedKey.addActionListener( this );
    panel.add( shiftMovedKey );
    
    xShiftMovedKey.addActionListener( this );
    panel.add( xShiftMovedKey );
    
    // Add the notes
    panel = new JPanel( new BorderLayout());
    panel.setAlignmentX( Component.LEFT_ALIGNMENT );
    contentPane.add( panel, BorderLayout.CENTER );
    panel.setBorder( BorderFactory.createTitledBorder( "Notes" ));
    notes.setLineWrap( true );
    panel.add( new JScrollPane( notes, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
                                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER ));
    
    // Add the action buttons
    panel = new JPanel( new FlowLayout( FlowLayout.RIGHT ));
    panel.setAlignmentX( Component.LEFT_ALIGNMENT );
    contentPane.add( panel );
    
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
    model.set( config );
    deviceType.setModel( new DefaultComboBoxModel( remote.getDeviceTypes()));
    
    if ( remote.getEFCDigits() == 5 )
      useEFC.setText( "EFC-5" );
    else
      useEFC.setText( "EFC" );
    useKey.setVisible( remote.getAdvCodeFormat() != Remote.HEX );
    movedKey.setModel( new DefaultComboBoxModel( remote.getUpgradeButtons()));
    shiftMovedKey.setText( remote.getShiftLabel());
    xShiftMovedKey.setText( remote.getXShiftLabel());
  }
  
  private void setKeyMove( KeyMove keyMove )
  {
    this.keyMove = null;
    if ( keyMove == null )
    {
      cmd = null;
      boundDevice.setSelectedIndex( -1 );
      boundKey.setSelectedIndex( -1 );
      shift.setSelected( false );
      xShift.setSelected( false );
      deviceType.setSelectedIndex( -1 );
      setupCode.setValue( null );
      useEFC.setSelected( true );
      efcHexField.setText( "" );
      movedKey.setSelectedIndex( -1 );
      shiftMovedKey.setSelected( false );
      xShiftMovedKey.setSelected( false );
      
      return;
    }
    
    cmd = keyMove.getCmd();
    if ( cmd != null )
      cmd = new Hex( cmd );
    boundDevice.setSelectedIndex( keyMove.getDeviceButtonIndex());
    shift.setSelected( false );
    xShift.setSelected( false );
    setButton( keyMove.getKeyCode(), boundKey, shift, xShift );
    
    deviceType.setSelectedIndex( keyMove.getDeviceType());
    setupCode.setValue( new Integer( keyMove.getSetupCode()));
    
    if ( keyMove.getClass() == KeyMoveKey.class )
    {
      useKey.setSelected( true );
      int code = (( KeyMoveKey )keyMove ).getMovedKeyCode();
      setButton( code, movedKey, shiftMovedKey, xShiftMovedKey );
    }
    else
    {
      efcHexField.setText( null );
      useEFC.setSelected( true );
      efcHexField.setText( keyMove.getValueString( config.getRemote()));
    }
    
    notes.setText( keyMove.getNotes());
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
    
    if ( source == okButton )
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

      int deviceTypeIndex = deviceType.getSelectedIndex();
      if ( deviceTypeIndex == -1 )
      {
        showWarning( "You must select a device type for the function to perform." );
        return;
      }
      if ( setupCode.getValue() == null )
      {
        showWarning( "You must specify a setup code for the function to perform." );
        return;
      }
      if ( !setupCode.isEditValid())
      {
        showWarning( setupCode.getText() + " isn't an integer between 0 and 2047." );
      }
      int setupId = (( Integer )setupCode.getValue()).intValue();
      
      String notesStr = notes.getText();
      Remote remote = config.getRemote();
      if ( useEFC.isSelected())
      {
        String text = efcHexField.getText().trim();
        if (( text == null ) || text.equals( "" ))
        {
          showWarning( "You must specify an EFC for the function to perform." );
          return;
        }
        try
        {
          int efc = Integer.parseInt( text );
          if ( remote.getEFCDigits() == 3 )
          {
            if ( efc < 0 )
            {
              showWarning( "EFCs must be greater than 0." );
              return;
            }
            if ( efc > 255 )
            {
              int oldEfc = efc;
              efc = oldEfc & 0xFF;
              showWarning( "EFCs repeat after 255.  Standardizing " + oldEfc + " to " + efc + '.' );
            }
          }
          else if (( efc < 0 ) || ( efc > 65535 ))
          {
            showWarning( "EFC-5s must be between 0 and 65535." );
            return;
          }
          keyMove = config.createKeyMove( keyCode, deviceIndex, deviceTypeIndex, setupId, efc, notesStr );
        }
        catch ( NumberFormatException ex )
        {
          showWarning( text + " isn't a valid EFC." );
          return;
        }
      }
      else if ( useHex.isSelected())
      {
        String text = efcHexField.getText().trim();
        if (( text == null ) || text.equals( "" ))
        {
          showWarning( "You must specify an EFC for the function to perform." );
          return;
        }
        cmd = null;
        try
        {
          cmd = new Hex( text );
        }
        catch ( NumberFormatException ex )
        {
          showWarning( text + " isn't a valid hex command." );
          return;
        }
        if (( remote.getAdvCodeFormat() == Remote.EFC ) && ( remote.getEFCDigits() == 3 ) && ( cmd.length() > 1 ))
        {
          showWarning( "The " + remote.getName() + " doesn't support key moves with multi-byte commands." );
          return;
        }
        
        keyMove = config.createKeyMove( keyCode, deviceIndex, deviceTypeIndex, setupId, cmd, notesStr );
      }
      else if ( useKey.isSelected())
      {
        if ( movedKey.getSelectedItem() == null )
        {
          showWarning( "You must select a key for the function to perform." );
          return;
        }
        int movedKeyCode = getKeyCode( movedKey, shiftMovedKey, xShiftMovedKey );
        keyMove = config.createKeyMoveKey( keyCode, deviceIndex, deviceTypeIndex, setupId, movedKeyCode, notesStr );
      }
      setVisible( false );
    }
    else if ( source == cancelButton )
    {
      keyMove = null;
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
    if (( source == useEFC ) || ( source == useHex ))
    {
      efcHexField.setVisible( true );
      movedKey.setVisible( false );
      shiftMovedKey.setVisible( false );
      xShiftMovedKey.setVisible( false );

      String text = efcHexField.getText();
      if (( text != null ) && !text.equals( "" ))
      {
        try
        {
          if ( source == useEFC ) // was useHEx
          {
            cmd = new Hex( text );
            EFC efc = null;
            if ( config.getRemote().getEFCDigits() == 3 )
              efc = new EFC( cmd );
            else // 5 digit EFGs
              efc = new EFC5( cmd );
            
            text = efc.toString();
          }
          else // useHex, was useEFC
          {
            EFC efc = null;
            if ( config.getRemote().getEFCDigits() == 3 )
              efc = new EFC( text );
            else // 5 digit EFCs
              efc = new EFC5( text );
              
            if ( cmd == null )
              cmd = efc.toHex();
            else
              efc.toHex( cmd );
            
            text = cmd.toString();
          }
          efcHexField.setText( text );
        }
        catch ( NumberFormatException ex )
        {
          efcHexField.setText( null );
        }
      }
    }
    else if ( source == useKey )
    {
      efcHexField.setVisible( false );
      efcHexField.setText( null );
      cmd = null;
      movedKey.setVisible( true );
      movedKey.setSelectedIndex( -1 );
      shiftMovedKey.setVisible( true );
      shiftMovedKey.setSelected( false );
      xShiftMovedKey.setVisible( config.getRemote().getXShiftEnabled());
      xShiftMovedKey.setSelected( false );
    }
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
  private JButton okButton = new JButton( "OK" );
  private JButton cancelButton = new JButton( "Cancel" );
  private DeviceButtonTableModel model = new DeviceButtonTableModel();
  private JP1Table table = new JP1Table( model );
  private JComboBox deviceType = new JComboBox();
  private JFormattedTextField setupCode = null;
  private JRadioButton useEFC = new JRadioButton( "EFC" );
  private JRadioButton useHex = new JRadioButton( "Hex" );
  private JRadioButton useKey = new JRadioButton( "Key" );
  private JTextField efcHexField = new JTextField( 15 );
  private JComboBox movedKey = new JComboBox();
  private JCheckBox shiftMovedKey = new JCheckBox();
  private JCheckBox xShiftMovedKey = new JCheckBox();
  private JTextArea notes = new JTextArea( 2, 10 );
  private JTextField focusField = null;
  
  private RemoteConfiguration config = null;
  private KeyMove keyMove = null;
  private Hex cmd = null;
  private static KeyMoveDialog dialog = null;
}
