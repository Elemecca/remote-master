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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.NumberFormatter;

/**
 * The Class KeyMoveDialog.
 */
public class KeyMoveDialog extends JDialog implements ActionListener, PropertyChangeListener, Runnable, ItemListener
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
    KeyMoveDialog dialog = new KeyMoveDialog( frame, config );

    dialog.config = config;
    dialog.setKeyMove( keyMove );
    dialog.pack();
    dialog.setLocationRelativeTo( frame );
    dialog.setVisible( true );

    return dialog.keyMove;
  }

  /**
   * Instantiates a new key move dialog.
   * 
   * @param frame
   *          the frame
   */
  private KeyMoveDialog( JFrame frame, RemoteConfiguration config )
  {
    super( frame, "Key Move", true );
    model.set( config );

    this.config = config;
    Remote remote = config.getRemote();

    JComponent contentPane = ( JComponent )getContentPane();
    contentPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
    contentPane.setLayout( new BoxLayout( contentPane, BoxLayout.PAGE_AXIS ) );

    // Add the bound device and key controls
    JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 0 ) );
    panel.setAlignmentX( Component.LEFT_ALIGNMENT );
    contentPane.add( panel );
    panel.setBorder( BorderFactory.createTitledBorder( "Bound Key" ) );

    panel.add( new JLabel( "Device:" ) );
    boundDevice.setModel( new DefaultComboBoxModel( remote.getDeviceButtons() ) );
    panel.add( boundDevice );

    panel.add( Box.createHorizontalStrut( 5 ) );

    panel.add( new JLabel( "Key:" ) );
    boundKey.setModel( new DefaultComboBoxModel( remote.getUpgradeButtons() ) );
    boundKey.addActionListener( this );
    panel.add( boundKey );

    shift.setText( remote.getShiftLabel() );
    shift.addActionListener( this );
    panel.add( shift );

    if ( remote.getXShiftEnabled() )
    {
      xShift.setText( remote.getXShiftLabel() );
      xShift.addActionListener( this );
      panel.add( xShift );
    }

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
      @Override
      public void mouseClicked( MouseEvent e )
      {
        if ( e.getClickCount() != 2 )
        {
          return;
        }
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

    deviceType.setModel( new DefaultComboBoxModel( remote.getDeviceTypes() ) );
    panel.add( deviceType );
    label.setLabelFor( deviceType );
    deviceType.addActionListener( this );

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
    setupCode.addPropertyChangeListener( this );
    label.setLabelFor( setupCode );
    panel.add( setupCode );

    chooseUpgrade.addActionListener( this );
    panel.add( chooseUpgrade );

    ActionListener al = new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        JMenuItem source = ( JMenuItem )e.getSource();
        int index = Integer.valueOf( source.getActionCommand() );
        DeviceUpgrade upgrade = KeyMoveDialog.this.config.getDeviceUpgrades().get( index );
        KeyMoveDialog.this.deviceType.setSelectedIndex( upgrade.getDeviceType().getNumber() );
        KeyMoveDialog.this.setupCode.setValue( upgrade.getSetupCode() );
      }
    };

    List< DeviceUpgrade > upgrades = config.getDeviceUpgrades();
    for ( int i = 0; i < upgrades.size(); ++i )
    {
      DeviceUpgrade upgrade = upgrades.get( i );
      String desc = upgrade.getDescription();
      if ( getValidFunctions( upgrade ).size() > 0 )
      {
        if ( config.findBoundDeviceButtonIndex( upgrade ) == -1 )
        {
          if ( desc == null || desc.trim().length() == 0 )
          {
            desc = String.format( "%s/%04d", upgrade.getDeviceType().toString(), upgrade.getSetupCode() );
          }
          JMenuItem item = new JMenuItem( desc );
          popup.add( item );
          item.setActionCommand( Integer.toString( i ) );
          item.addActionListener( al );
        }
      }
    }

    if ( popup.getComponentCount() == 0 )
    {
      chooseUpgrade.setVisible( false );
    }

    // Add the EFC/Hex/Key controls
    panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 0 ) );
    panel.setAlignmentX( Component.LEFT_ALIGNMENT );
    functionBox.add( panel );

    ButtonGroup group = new ButtonGroup();

    useEFC.addActionListener( this );
    useEFC.addItemListener( this );
    panel.add( useEFC );
    group.add( useEFC );

    useHex.addActionListener( this );
    useHex.addItemListener( this );
    panel.add( useHex );
    group.add( useHex );

    useFunction.addActionListener( this );
    useFunction.addItemListener( this );
    useFunction.setVisible( false );
    panel.add( useFunction );
    group.add( useFunction );

    boolean showMovedKey = remote.getAdvCodeBindFormat() == AdvancedCode.BindFormat.LONG;

    if ( showMovedKey )
    {
      useKey.addActionListener( this );
      useKey.addItemListener( this );
      panel.add( useKey );
      group.add( useKey );
    }

    use3DigitEFCs = remote.getEFCDigits() == 3;
    if ( use3DigitEFCs )
    {
      useEFC.setText( "EFC" );
      format = new DecimalFormat( "000" );
      format.setParseIntegerOnly( true );
      format.setGroupingUsed( false );
      formatter = new NumberFormatter( format );
      formatter.setValueClass( Integer.class );
      formatter.setMinimum( new Integer( 0 ) );
      formatter.setMaximum( new Integer( 255 ) );
      efcField = new JFormattedTextField( formatter );
      efcField.setColumns( 3 );
    }
    else
    {
      useEFC.setText( "EFC-5" );
      format = new DecimalFormat( "00000" );
      format.setParseIntegerOnly( true );
      format.setGroupingUsed( false );
      formatter = new NumberFormatter( format );
      formatter.setValueClass( Integer.class );
      formatter.setMinimum( new Integer( 0 ) );
      formatter.setMaximum( new Integer( 99999 ) );
      efcField = new JFormattedTextField( formatter );
      efcField.setColumns( 5 );
    }
    FocusSelector.selectOnFocus( efcField );
    efcField.addPropertyChangeListener( this );
    panel.add( efcField );
    efcField.setVisible( false );

    HexFormatter hexFormatter = new HexFormatter( 0 );
    hexField = new JFormattedTextField( hexFormatter );
    hexField.setColumns( 15 );
    FocusSelector.selectOnFocus( hexField );
    hexField.addPropertyChangeListener( this );
    panel.add( hexField );
    FocusSelector.selectOnFocus( hexField );
    hexField.setVisible( false );

    function.addActionListener( this );
    panel.add( function );
    function.setVisible( false );

    if ( showMovedKey )
    {
      movedKey.setVisible( false );
      movedKey.setModel( new DefaultComboBoxModel( remote.getUpgradeButtons() ) );
      movedKey.addActionListener( this );
      panel.add( movedKey );

      shiftMovedKey.setText( remote.getShiftLabel() );
      shiftMovedKey.setVisible( false );
      shiftMovedKey.addActionListener( this );
      panel.add( shiftMovedKey );

      if ( remote.getXShiftEnabled() )
      {
        xShiftMovedKey.setText( remote.getXShiftLabel() );
        xShiftMovedKey.setVisible( false );
        xShiftMovedKey.addActionListener( this );
        panel.add( xShiftMovedKey );
      }
    }

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
   * Sets the key move.
   * 
   * @param keyMove
   *          the new key move
   */
  private void setKeyMove( KeyMove keyMove )
  {
    if ( keyMove == null )
    {
      useEFC.doClick();
      return;
    }

    cmd = new Hex( keyMove.getCmd() );
    DeviceButton devBtn = config.getRemote().getDeviceButton( keyMove.getDeviceButtonIndex() );
    boundDevice.setSelectedItem( devBtn );
    shift.removeActionListener( this );
    xShift.removeActionListener( this );
    setButton( keyMove.getKeyCode(), boundKey, shift, xShift );
    shift.addActionListener( this );
    xShift.addActionListener( this );

    deviceType.removeActionListener( this );
    deviceType.setSelectedIndex( keyMove.getDeviceType() );
    deviceType.addActionListener( this );
    setupCode.removeActionListener( this );
    setupCode.setValue( new Integer( keyMove.getSetupCode() ) );
    setupCode.addActionListener( this );
    initialNotes = keyMove.getNotes();
    notes.setText( keyMove.getNotes() );

    checkForUpgrade();

    if ( keyMove.getClass() == KeyMoveKey.class )
    {
      useKey.doClick();
    }
    else if ( upgrade != null )
    {
      useFunction.doClick();
    }
    else
    {
      useHex.doClick();
    }
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

    comboBox.setSelectedItem( b );
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
    {
      keyCode |= config.getRemote().getShiftMask();
    }
    else if ( xShiftBox.isSelected() )
    {
      keyCode |= config.getRemote().getXShiftMask();
    }
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
    Remote remote = config.getRemote();
    Button b = ( Button )boundKey.getSelectedItem();
    if ( source == okButton )
    {
      int deviceIndex = ( ( DeviceButton )boundDevice.getSelectedItem() ).getButtonIndex();
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
      if ( cmd == null )
      {
        if ( useEFC.isSelected() )
        {
          if ( remote.getEFCDigits() == 3 )
          {
            showWarning( "You must specify an EFC for the function to perform." );
          }
          else
          {
            showWarning( "You must specify an EFC-5 for the function to perform." );
          }
        }
        else if ( useHex.isSelected() )
        {
          showWarning( "You must specify a valid Hex command for the function to perform,\n"
              + "such as 1B or 1B C2.");
        }
        else if ( useKey.isSelected() )
        {
          showWarning( "You must select a key for the function to perform." );
        }
        else if ( useFunction.isSelected() )
        {
          showWarning( "You must select a function to perform." );
        }
        return;
      }
      if ( remote.getAdvCodeFormat() == AdvancedCode.Format.EFC && remote.getEFCDigits() == 3 && cmd.length() > 1 )
      {
        showWarning( "The " + remote.getName() + " doesn't support key moves with multi-byte commands." );
      }
      if ( useKey.isSelected() )
      {
        int movedKeyCode = getKeyCode( movedKey, shiftMovedKey, xShiftMovedKey );
        keyMove = remote.createKeyMoveKey( keyCode, deviceIndex, deviceTypeIndex, setupId, movedKeyCode, notesStr );
      }
      else
      {
        keyMove = remote.createKeyMove( keyCode, deviceIndex, deviceTypeIndex, setupId, cmd, notesStr );
      }
      if ( config.hasSegments() )
      {
        // set default value
        keyMove.setSegmentFlags( 0xFF );
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
      {
        xShift.setSelected( false );
      }
      else if ( b != null && remote.getXShiftEnabled() )
      {       
        xShift.setSelected( b.needsShift( Button.MOVE_BIND ) );
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
        shift.setSelected( b.needsShift( Button.MOVE_BIND ) );
      }
    }    
    else if ( source == boundKey )
    {
      if ( b != null )
      {
        b.setShiftBoxes( Button.MOVE_BIND, shift, xShift );
      }
    }  
    else if ( source == function )
    {
      setFunction();
    }
    else if ( source == movedKey )
    {
      Button button = ( Button )movedKey.getSelectedItem();
      cmd = new Hex( 1 );
      cmd.set( button.getKeyCode(), 0 );
    }
    else if ( source == useEFC )
    {
      if ( cmd != null )
      {
        int value = 0;
        if ( use3DigitEFCs )
        {
          value = new EFC( cmd ).getValue();
        }
        else
        {
          value = new EFC5( cmd ).getValue();
        }

        efcField.removePropertyChangeListener( this );
        efcField.setValue( value );
        efcField.addPropertyChangeListener( this );
      }
    }
    else if ( source == useHex )
    {
      hexField.removePropertyChangeListener( this );
      hexField.setValue( cmd );
      hexField.addPropertyChangeListener( this );
    }
    else if ( source == useKey )
    {
      movedKey.removeActionListener( this );
      shiftMovedKey.removeActionListener( this );
      xShiftMovedKey.removeActionListener( this );

      movedKey.setSelectedIndex( -1 );
      shiftMovedKey.setSelected( false );
      xShiftMovedKey.setSelected( false );
      if ( cmd != null )
      {
        setButton( cmd.getData()[ 0 ], movedKey, shiftMovedKey, xShiftMovedKey );
      }
      movedKey.addActionListener( this );
      shiftMovedKey.addActionListener( this );
      xShiftMovedKey.addActionListener( this );
    }
    else if ( source == useFunction )
    {
      if ( cmd != null )
      {
        if ( upgrade != null )
        {
          Function func = upgrade.getFunction( cmd );
          if ( func != null )
          {
            function.removeActionListener( this );
            function.setSelectedItem( func );
            function.addActionListener( this );
          }
        }
      }
    }
    else if ( source == deviceType )
    {
      checkForUpgrade();
    }
    else if ( source == chooseUpgrade )
    {
      popup.show( chooseUpgrade, 0, chooseUpgrade.getHeight() );
    }
  }
  
  private void setFunction()
  {
    Function func = ( Function )function.getSelectedItem();
    if ( func == null )
    {
      return;
    }
    cmd = func.getHex();
    if ( initialNotes == null || initialNotes.trim().equals( "" ) )
    {
      if ( func != null )
      {
        String text = func.getNotes();
        if ( text == null || text.trim().equals( "" ) )
        {
          text = func.getName();
        }
        notes.setText( text );
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

  private JPopupMenu popup = new JPopupMenu();
  private JButton chooseUpgrade = new JButton( "Device Upgrades" );

  /** The use efc. */
  public boolean use3DigitEFCs = true;
  private JRadioButton useEFC = new JRadioButton( "EFC" );

  /** The use hex. */
  private JRadioButton useHex = new JRadioButton( "Hex" );

  /** The use key. */
  private JRadioButton useKey = new JRadioButton( "Key" );

  private JRadioButton useFunction = new JRadioButton( "Function" );

  private JRadioButton buttonToSelect = null;

  /** The efc field. */
  private JFormattedTextField efcField = null;

  private JFormattedTextField hexField = null;

  private JComboBox function = new JComboBox();

  /** The moved key. */
  private JComboBox movedKey = new JComboBox();

  /** The shift moved key. */
  private JCheckBox shiftMovedKey = new JCheckBox();

  /** The x shift moved key. */
  private JCheckBox xShiftMovedKey = new JCheckBox();

  /** The notes. */
  private JTextArea notes = new JTextArea( 2, 10 );
  
  private String initialNotes = null;

  /** The config. */
  private RemoteConfiguration config = null;

  /** The key move. */
  private KeyMove keyMove = null;

  /** The cmd. */
  private Hex cmd = null;

  private DeviceUpgrade upgrade = null;

  protected List< Function > getValidFunctions( DeviceUpgrade upgrade )
  {
    List< Function > functions = new ArrayList< Function >();
    for ( Function function : upgrade.getFunctions() )
    {
      Hex hex = function.getHex();
      if ( hex != null && hex.length() > 0 )
      {
        functions.add( function );
      }
    }
    return functions;
  }

  protected DeviceUpgrade checkForUpgrade()
  {
    upgrade = null;
    int devTypeIndex = deviceType.getSelectedIndex();
    Integer code = ( Integer )setupCode.getValue();
    if ( devTypeIndex != -1 && code != null )
    {
      upgrade = config.findDeviceUpgrade( devTypeIndex, code );

      Function func = null;
      if ( upgrade != null )
      {
        List< Function > functions = getValidFunctions( upgrade );
        if ( functions.size() > 0 )
        {
          function.removeActionListener( this );
          function.setModel( new DefaultComboBoxModel( functions.toArray() ) );
          useFunction.setVisible( true );
          if ( cmd != null )
          {
            func = upgrade.getFunction( cmd );
            if ( func != null )
            {
              function.setSelectedItem( func );
            }
          }
          function.addActionListener( this );
        }
      }
    }

    return upgrade;
  }

  @Override
  public void propertyChange( PropertyChangeEvent event )
  {
    Object source = event.getSource();
    if ( source == setupCode )
    {
      checkForUpgrade();
    }
    else if ( source == efcField )
    {
      Integer value = ( Integer )efcField.getValue();
      if ( value != null )
      {
        if ( use3DigitEFCs )
        {
          cmd = EFC.toHex( value );
        }
        else
        {
          cmd = EFC5.toHex( value );
        }
      }
    }
    else if ( source == hexField )
    {
      Hex hex = ( Hex )hexField.getValue();
      if ( hex != null )
      {
        cmd = ( Hex )hexField.getValue();
      }
    }
  }

  @Override
  public void run()
  {
    if ( buttonToSelect != null )
    {
      buttonToSelect.setSelected( true );
    }
  }

  @Override
  public void itemStateChanged( ItemEvent event )
  {
    boolean isSelect = event.getStateChange() == ItemEvent.SELECTED;
    JRadioButton source = ( JRadioButton )event.getSource();
    if ( source == useEFC )
    {
      efcField.setVisible( isSelect );
    }
    else if ( source == useHex )
    {
      hexField.setVisible( isSelect );
    }
    else if ( source == useFunction )
    {
      function.setVisible( isSelect );
      setFunction();
    }
    else if ( source == useKey )
    {
      movedKey.setVisible( isSelect );
      shiftMovedKey.setVisible( isSelect );
      xShiftMovedKey.setVisible( isSelect );
    }
    if ( isSelect )
    {
      source.getParent().validate();
    }
  }
}
