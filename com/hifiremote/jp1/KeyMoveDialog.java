package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.NumberFormatter;

// TODO: Auto-generated Javadoc
/**
 * The Class KeyMoveDialog.
 */
public class KeyMoveDialog extends JDialog implements ActionListener, ItemListener
{

  /**
   * Show dialog.
   * 
   * @param frame
   *          the frame
   * @param keyMove
   *          the key move
   * @param config
   *          the config
   * @return the key move
   */
  public static KeyMove showDialog( JFrame frame, KeyMove keyMove, RemoteConfiguration config )
  {
    if ( dialog == null )
      dialog = new KeyMoveDialog( frame );

    dialog.setRemoteConfiguration( config );
    dialog.setKeyMove( keyMove );
    dialog.pack();
    dialog.setLocationRelativeTo( frame );
    dialog.setVisible( true );

    return dialog.keyMove;
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
   * Instantiates a new key move dialog.
   * 
   * @param frame
   *          the frame
   */
  private KeyMoveDialog( JFrame frame )
  {
    super( frame, "Key Move", true );

    JComponent contentPane = ( JComponent )getContentPane();
    contentPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
    contentPane.setLayout( new BoxLayout( contentPane, BoxLayout.PAGE_AXIS ) );

    // Add the bound device and key controls
    JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 0 ) );
    panel.setAlignmentX( Component.LEFT_ALIGNMENT );
    contentPane.add( panel );
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

    // Add the Function definitions
    Box functionBox = Box.createVerticalBox();
    functionBox.setBorder( BorderFactory.createTitledBorder( "Function to Perform" ) );
    contentPane.add( functionBox );

    Box deviceBox = Box.createVerticalBox();
    functionBox.add( deviceBox );
    deviceBox.setBorder( BorderFactory.createTitledBorder( "Device" ) );
    JLabel label = new JLabel( "Double-click a row to select a type/code, or enter a device below." );
    label.setAlignmentX( Component.LEFT_ALIGNMENT );
    deviceBox.add( label );
    deviceBox.add( Box.createVerticalStrut( 5 ) );

    model.setEditable( false );
    table.setColumnSelectionAllowed( false );
    table.initColumns( model );
    MouseAdapter ma = new MouseAdapter()
    {
      public void mouseClicked( MouseEvent e )
      {
        if ( e.getClickCount() != 2 )
          return;
        int row = table.rowAtPoint( e.getPoint() );
        if ( row != -1 )
        {
          deviceType.setSelectedItem( table.getValueAt( row, 2 ) );
          setupCode.setValue( new Integer( ( ( SetupCode )table.getValueAt( row, 3 ) ).getValue() ) );
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
    deviceBox.add( Box.createVerticalStrut( 10 ) );

    panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 0 ) );
    panel.setAlignmentX( Component.LEFT_ALIGNMENT );
    deviceBox.add( panel );
    label = new JLabel( "Device Type:" );
    panel.add( label );
    panel.add( deviceType );
    label.setLabelFor( deviceType );
    panel.add( Box.createHorizontalStrut( 5 ) );
    label = new JLabel( "Setup Code:" );
    panel.add( label );

    DecimalFormat format = new DecimalFormat( "0000" );
    format.setParseIntegerOnly( true );
    format.setGroupingUsed( false );
    NumberFormatter formatter = new NumberFormatter( format );
    formatter.setValueClass( Integer.class );
    formatter.setMinimum( new Integer( 0 ) );
    formatter.setMaximum( new Integer( 2047 ) );
    setupCode = new JFormattedTextField( formatter );
    setupCode.setColumns( 4 );
    FocusSelector.selectOnFocus( setupCode );
    label.setLabelFor( setupCode );
    panel.add( setupCode );

    // Add the EFC/Hex/Key controls
    panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 0 ) );
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
    FocusSelector.selectOnFocus( efcHexField );
    panel.add( movedKey );

    shiftMovedKey.addActionListener( this );
    panel.add( shiftMovedKey );

    xShiftMovedKey.addActionListener( this );
    panel.add( xShiftMovedKey );

    // Add the notes
    panel = new JPanel( new BorderLayout() );
    panel.setAlignmentX( Component.LEFT_ALIGNMENT );
    contentPane.add( panel, BorderLayout.CENTER );
    panel.setBorder( BorderFactory.createTitledBorder( "Notes" ) );
    notes.setLineWrap( true );
    panel.add( new JScrollPane( notes, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER ) );

    // Add the action buttons
    panel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
    panel.setAlignmentX( Component.LEFT_ALIGNMENT );
    contentPane.add( panel );

    okButton.addActionListener( this );
    panel.add( okButton );

    cancelButton.addActionListener( this );
    panel.add( cancelButton );
  }

  /**
   * Sets the remote configuration.
   * 
   * @param config
   *          the new remote configuration
   */
  private void setRemoteConfiguration( RemoteConfiguration config )
  {
    this.config = config;
    Remote remote = config.getRemote();
    shift.setText( remote.getShiftLabel() );
    xShift.setText( remote.getXShiftLabel() );
    xShift.setVisible( remote.getXShiftEnabled() );
    boundDevice.setModel( new DefaultComboBoxModel( remote.getDeviceButtons() ) );
    boundKey.setModel( new DefaultComboBoxModel( remote.getUpgradeButtons() ) );
    model.set( config );
    table.initColumns( model );
    deviceType.setModel( new DefaultComboBoxModel( remote.getDeviceTypes() ) );

    if ( remote.getEFCDigits() == 5 )
      useEFC.setText( "EFC-5" );
    else
      useEFC.setText( "EFC" );
    useKey.setVisible( ( remote.getAdvCodeBindFormat() == AdvancedCode.BindFormat.LONG ) );
    movedKey.setModel( new DefaultComboBoxModel( remote.getUpgradeButtons() ) );
    shiftMovedKey.setText( remote.getShiftLabel() );
    xShiftMovedKey.setText( remote.getXShiftLabel() );
  }

  /**
   * Sets the key move.
   * 
   * @param keyMove
   *          the new key move
   */
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
    boundDevice.setSelectedIndex( keyMove.getDeviceButtonIndex() );
    shift.setSelected( false );
    xShift.setSelected( false );
    setButton( keyMove.getKeyCode(), boundKey, shift, xShift );

    deviceType.setSelectedIndex( keyMove.getDeviceType() );
    setupCode.setValue( new Integer( keyMove.getSetupCode() ) );

    if ( keyMove.getClass() == KeyMoveKey.class )
    {
      useKey.setSelected( true );
      int code = ( ( KeyMoveKey )keyMove ).getMovedKeyCode();
      setButton( code, movedKey, shiftMovedKey, xShiftMovedKey );
    }
    else
    {
      efcHexField.setText( null );
      useEFC.setSelected( true );
      String text = null;
      if ( config.getRemote().getEFCDigits() == 3 )
      {
        text = keyMove.getEFC().toString();
      }
      else
      {
        text = keyMove.getEFC5().toString();
      }
      efcHexField.setText( text );
    }

    notes.setText( keyMove.getNotes() );
  }

  /**
   * Sets the button.
   * 
   * @param code
   *          the code
   * @param comboBox
   *          the combo box
   * @param shiftBox
   *          the shift box
   * @param xShiftBox
   *          the x shift box
   */
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
        if ( remote.getXShiftEnabled() && ( ( base | remote.getXShiftMask() ) == code ) )
        {
          xShiftBox.setEnabled( remote.getXShiftEnabled() & b.allowsXShiftedMacro() );
          xShiftBox.setSelected( true );
          comboBox.setSelectedItem( b );
          return;
        }
      }
      b = remote.getButton( code & ~remote.getShiftMask() );
      if ( b != null )
        shiftBox.setSelected( true );
      else if ( remote.getXShiftEnabled() )
      {
        b = remote.getButton( code ^ ~remote.getXShiftMask() );
        if ( b != null )
          xShiftBox.setSelected( true );
      }
    }

    shiftBox.setEnabled( b.allowsShiftedKeyMove() );
    xShiftBox.setEnabled( b.allowsXShiftedKeyMove() );

    if ( b.getIsXShifted() )
      xShiftBox.setSelected( true );
    else if ( b.getIsShifted() )
      shiftBox.setSelected( true );

    comboBox.removeActionListener( this );
    comboBox.setSelectedItem( b );
    comboBox.addActionListener( this );
  }

  /**
   * Gets the key code.
   * 
   * @param comboBox
   *          the combo box
   * @param shiftBox
   *          the shift box
   * @param xShiftBox
   *          the x shift box
   * @return the key code
   */
  private int getKeyCode( JComboBox comboBox, JCheckBox shiftBox, JCheckBox xShiftBox )
  {
    int keyCode = ( ( Button )comboBox.getSelectedItem() ).getKeyCode();
    if ( shiftBox.isSelected() )
      keyCode |= config.getRemote().getShiftMask();
    else if ( xShiftBox.isSelected() )
      keyCode |= config.getRemote().getXShiftMask();
    return keyCode;
  }

  /**
   * Show warning.
   * 
   * @param message
   *          the message
   */
  private void showWarning( String message )
  {
    JOptionPane.showMessageDialog( this, message, "Missing Information", JOptionPane.ERROR_MESSAGE );
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
      if ( !setupCode.isEditValid() )
      {
        showWarning( setupCode.getText() + " isn't an integer between 0 and 2047." );
      }
      int setupId = ( ( Integer )setupCode.getValue() ).intValue();

      String notesStr = notes.getText();
      Remote remote = config.getRemote();
      if ( useEFC.isSelected() )
      {
        String text = efcHexField.getText().trim();
        if ( ( text == null ) || text.equals( "" ) )
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
          else if ( ( efc < 0 ) || ( efc > 99999 ) )
          {
            showWarning( "EFC-5s must be between 0 and 99999." );
            return;
          }
          keyMove = remote.createKeyMove( keyCode, deviceIndex, deviceTypeIndex, setupId, efc, notesStr );
        }
        catch ( NumberFormatException ex )
        {
          showWarning( text + " isn't a valid EFC." );
          return;
        }
      }
      else if ( useHex.isSelected() )
      {
        String text = efcHexField.getText().trim();
        if ( ( text == null ) || text.equals( "" ) )
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
        if ( ( remote.getAdvCodeFormat() == AdvancedCode.Format.EFC ) && ( remote.getEFCDigits() == 3 )
            && ( cmd.length() > 1 ) )
        {
          showWarning( "The " + remote.getName() + " doesn't support key moves with multi-byte commands." );
          return;
        }

        keyMove = remote.createKeyMove( keyCode, deviceIndex, deviceTypeIndex, setupId, cmd, notesStr );
      }
      else if ( useKey.isSelected() )
      {
        if ( movedKey.getSelectedItem() == null )
        {
          showWarning( "You must select a key for the function to perform." );
          return;
        }
        int movedKeyCode = getKeyCode( movedKey, shiftMovedKey, xShiftMovedKey );
        keyMove = remote.createKeyMoveKey( keyCode, deviceIndex, deviceTypeIndex, setupId, movedKeyCode, notesStr );
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
      if ( shift.isSelected() )
        xShift.setSelected( false );
    }
    else if ( source == xShift )
    {
      if ( xShift.isSelected() )
        shift.setSelected( false );
    }
  }

  // ItemListener
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  public void itemStateChanged( ItemEvent e )
  {
    Object source = e.getSource();
    if ( e.getStateChange() == ItemEvent.DESELECTED )
    {
      try
      {
        if ( source == useEFC )
        {
          String text = efcHexField.getText().trim();
          if ( !text.equals( "" ) )
          {
            int efc = Integer.parseInt( text );
            if ( config.getRemote().getEFCDigits() == 3 )
            {
              if ( efc > 255 )
              {
                efc &= 0xFF;
              }
              if ( efc >= 0 )
              {
                cmd = EFC.toHex( efc );
              }
              else
                cmd = null;
            }
            else
            {
              if ( ( efc >= 0 ) && ( efc <= 99999 ) )
              {
                cmd = EFC5.toHex( efc );
              }
            }
          }
          else
          {
            cmd = null;
          }
        }
        else if ( source == useHex )
        {
          String text = efcHexField.getText().trim();
          if ( !text.equals( "" ) )
          {
            cmd = new Hex( text );
          }
          else
          {
            cmd = null;
          }
        }
        else
        // source == useKey
        {
          if ( movedKey.getSelectedItem() == null )
          {
            cmd = null;
          }
          else
          {
            int movedKeyCode = getKeyCode( movedKey, shiftMovedKey, xShiftMovedKey );
            cmd = new Hex( 1 );
            cmd.set( ( short )movedKeyCode, 0 );
          }
        }
      }
      catch ( NumberFormatException nfe )
      {
        cmd = null;
      }
      return;
    }
    if ( ( source == useEFC ) || ( source == useHex ) )
    {
      efcHexField.setVisible( true );
      movedKey.setVisible( false );
      shiftMovedKey.setVisible( false );
      xShiftMovedKey.setVisible( false );

      String text = null;
      if ( cmd != null )
      {
        try
        {
          if ( source == useEFC )
          {
            EFC efc = null;
            if ( config.getRemote().getEFCDigits() == 3 )
              efc = new EFC( cmd );
            else
              // 5 digit EFGs
              efc = new EFC5( cmd );

            text = efc.toString();
          }
          else
          // source == useHex
          {
            text = cmd.toString();
          }
        }
        catch ( Exception ex )
        {
          ex.printStackTrace( System.err );
        }
      }
      efcHexField.setText( text );
    }
    else if ( source == useKey )
    {
      efcHexField.setVisible( false );
      efcHexField.setText( null );
      movedKey.setVisible( true );
      movedKey.setSelectedIndex( -1 );
      shiftMovedKey.setVisible( true );
      shiftMovedKey.setSelected( false );
      xShiftMovedKey.setVisible( config.getRemote().getXShiftEnabled() );
      xShiftMovedKey.setSelected( false );
      if ( cmd != null )
      {
        setButton( cmd.getData()[ 0 ], movedKey, shiftMovedKey, xShiftMovedKey );
      }
    }
  }

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

  /** The cancel button. */
  private JButton cancelButton = new JButton( "Cancel" );

  /** The model. */
  private DeviceButtonTableModel model = new DeviceButtonTableModel();

  /** The table. */
  private JP1Table table = new JP1Table( model );

  /** The device type. */
  private JComboBox deviceType = new JComboBox();

  /** The setup code. */
  private JFormattedTextField setupCode = null;

  /** The use efc. */
  private JRadioButton useEFC = new JRadioButton( "EFC" );

  /** The use hex. */
  private JRadioButton useHex = new JRadioButton( "Hex" );

  /** The use key. */
  private JRadioButton useKey = new JRadioButton( "Key" );

  /** The efc hex field. */
  private JTextField efcHexField = new JTextField( 15 );

  /** The moved key. */
  private JComboBox movedKey = new JComboBox();

  /** The shift moved key. */
  private JCheckBox shiftMovedKey = new JCheckBox();

  /** The x shift moved key. */
  private JCheckBox xShiftMovedKey = new JCheckBox();

  /** The notes. */
  private JTextArea notes = new JTextArea( 2, 10 );

  /** The config. */
  private RemoteConfiguration config = null;

  /** The key move. */
  private KeyMove keyMove = null;

  /** The cmd. */
  private Hex cmd = null;

  /** The dialog. */
  private static KeyMoveDialog dialog = null;
}
