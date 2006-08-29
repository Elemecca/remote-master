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
  implements ActionListener, FocusListener, Runnable, ItemListener, ListSelectionListener
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
    type.setRenderer( new FunctionTypeRenderer());
    type.addActionListener( this );
    
    panel.add( parameterCard );
    /*
    box = box.createVerticalBox();
    addToBox( new JLabel( "Hex:" ), box );
    addToBox( hex, box );
    box.setBorder( null );
    panel.add( box );
    */
    // Empty panel
    panel = new JPanel();
    parameterCard.add( panel, "Empty" );
    
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
    
    // Duration parameter panel
    panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 10, 0 ));
    parameterCard.add( panel, "Duration" );
    box = Box.createVerticalBox();
    addToBox( new JLabel( "Duration:" ), box );
    addToBox( duration, box );
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
    condition.addActionListener( this );
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
    addToBox( firstKeyLabel, box );
    addToBox( firstMacroKey, box );
    panel.add( box );
    firstMacroKey.setRenderer( keyCodeRenderer );
    box = Box.createVerticalBox();
    addToBox( secondKeyLabel, box );
    addToBox( secondMacroKey, box );
    panel.add( box );
    secondMacroKey.setRenderer( keyCodeRenderer );
    
    // Add the Macro definition panel
    macroBox = Box.createHorizontalBox();
    macroBox.setBorder( BorderFactory.createTitledBorder( "Macro Definition:" ));
    addToBox( macroBox, contentPane );
    
    JPanel availableBox = new JPanel( new BorderLayout());
    macroBox.add( availableBox );
    availableBox.add( availableLabel, BorderLayout.NORTH );
    availableButtons.setFixedCellWidth( 100 );
    availableBox.add( new JScrollPane( availableButtons ), BorderLayout.CENTER );
    availableButtons.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    availableButtons.addListSelectionListener( this );

    activeColor = availableButtons.getBackground();
    availableButtons.setEnabled( false );
    disabledColor = availableButtons.getBackground();
    inactiveColor = new Color( 216, 228, 248 );    
    panel = new JPanel( new GridLayout( 3, 2, 2, 2 ));
    availableBox.add( panel, BorderLayout.SOUTH );
    add.addActionListener( this );
    panel.add( add );
    insert.addActionListener( this );
    panel.add( insert );
    addShift.addActionListener( this );
    panel.add( addShift );
    insertShift.addActionListener( this );
    panel.add( insertShift );
    addXShift.addActionListener( this );
    panel.add( addXShift );
    insertXShift.addActionListener( this );
    panel.add( insertXShift );
    
    macroBox.add( Box.createHorizontalStrut( 10 ));
    
    firstKeysPanel = new JPanel( new BorderLayout());
    macroBox.add( firstKeysPanel );
    firstKeysPanel.add( firstMacroLabel, BorderLayout.NORTH );
    firstMacroButtons.setFixedCellWidth( 100 );
    firstKeysPanel.add( new JScrollPane( firstMacroButtons ), BorderLayout.CENTER );
    firstMacroButtons.setModel( new DefaultListModel());
    firstMacroButtons.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    firstMacroButtons.setCellRenderer( macroButtonRenderer );
    firstMacroButtons.addListSelectionListener( this );
    firstMacroButtons.addFocusListener( this );
    
    JPanel buttonBox = new JPanel( new GridLayout( 3, 2, 2, 2 ));
    firstKeysPanel.add( buttonBox, BorderLayout.SOUTH );
    firstMoveUp.addActionListener( this );
    buttonBox.add( firstMoveUp );
    firstMoveDown.addActionListener( this );
    buttonBox.add( firstMoveDown );
    firstRemove.addActionListener( this );
    buttonBox.add( firstRemove );
    firstClear.addActionListener( this );
    buttonBox.add( firstClear );

    // The second macro
    macroBox.add( Box.createHorizontalStrut( 10 ));
    
    secondKeysPanel = new JPanel( new BorderLayout());
    macroBox.add( secondKeysPanel );
    secondKeysPanel.add( secondMacroLabel, BorderLayout.NORTH );
    secondMacroButtons.setFixedCellWidth( 100 );
    secondKeysPanel.add( new JScrollPane( secondMacroButtons ), BorderLayout.CENTER );
    secondMacroButtons.setModel( new DefaultListModel());
    secondMacroButtons.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    secondMacroButtons.setCellRenderer( macroButtonRenderer );
    secondMacroButtons.addListSelectionListener( this );
    secondMacroButtons.addFocusListener( this );
    
    buttonBox = new JPanel( new GridLayout( 3, 2, 2, 2 ));
    secondKeysPanel.add( buttonBox, BorderLayout.SOUTH );
    secondMoveUp.addActionListener( this );
    buttonBox.add( secondMoveUp );
    secondMoveDown.addActionListener( this );
    buttonBox.add( secondMoveDown );
    secondRemove.addActionListener( this );
    buttonBox.add( secondRemove );
    secondClear.addActionListener( this );
    buttonBox.add( secondClear );

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
    
    Vector< String > specialFunctions = new Vector< String >();
    for ( SpecialProtocol sp : config.getSpecialProtocols())
    {
      for ( String func : sp.getFunctions())
        specialFunctions.add( func );
    }
    type.setModel( new DefaultComboBoxModel( specialFunctions ));
    
    keyCodeRenderer.setRemote( remote );
    Vector< Integer >macroKeys = new Vector< Integer >();
    for ( Macro macro : config.getMacros())
      macroKeys.add( new Integer( macro.getKeyCode()));
    DefaultComboBoxModel model = new DefaultComboBoxModel( macroKeys ); 
    macroKey.setModel( model );
    firstMacroKey.setModel( model );
    secondMacroKey.setModel( model );

    Vector< Button > buttons = remote.getButtons();
    DefaultListModel listModel = new DefaultListModel();
    for ( Button b : buttons )
    {
      if ( b.allowsMacro() || b.allowsShiftedMacro() || b.allowsXShiftedMacro())
        listModel.addElement( b );
    }
    availableButtons.setModel( listModel );
    
    macroButtonRenderer.setRemote( remote );

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
      type.setSelectedIndex( 0 );
      return;
    }
    
    cmd = function.getCmd();
    if ( cmd != null )
      cmd = new Hex( cmd );
    boundDevice.setSelectedIndex( function.getDeviceButtonIndex());
    shift.setSelected( false );
    xShift.setSelected( false );
    setButton( function.getKeyCode(), boundKey, shift, xShift );
    
    type.setSelectedItem( function.getType());
    function.update( this );
    
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
    Remote remote = config.getRemote();
    
    if ( source == type )
    {
      String typeStr = ( String )type.getSelectedItem();
      String cardStr = typeStr;
      if ( typeStr.equals( "DKP" ))
      {
        cardStr = "Duration";
        enableMacros( true );
        firstMacroLabel.setText( "Single keys:" );  
        secondMacroLabel.setText( "Double keys:" );
      }
      else if ( typeStr.equals( "DSM" ))
      {
        cardStr = "Empty";
        enableMacros( true, false );
        setTarget( firstMacroButtons );
        firstMacroLabel.setText( "Keys:" );
      }
      else if ( typeStr.equals( "LKP" ))
      {
        cardStr = "Duration";
        enableMacros( true );
        firstMacroLabel.setText( "Short keys:" );
        secondMacroLabel.setText( "Long keys:" );
      }
      else if ( typeStr.equals( "Pause" ))
      {
        cardStr = "Duration";
        enableMacros( false );
      }
      else if ( typeStr.equals( "ToadTog" ))
      {
        enableMacros( true );
        int i = condition.getSelectedIndex();
        if ( i == -1 )
          i = 0;
        condition.setSelectedIndex( i );
      }
      else if ( typeStr.equals( "ULKP" ))
      {
        cardStr = "ULDKP";
        firstKeyLabel.setText( "Short Key:" );
        secondKeyLabel.setText( "Long Key:" );
        enableMacros( false );
      }
      else if ( typeStr.equals( "UDKP" ))
      {
        cardStr = "ULDKP";
        firstKeyLabel.setText( "Single Key:" );
        secondKeyLabel.setText( "Double Key:" );
        enableMacros( false );
      }
      else
        enableMacros( false );
      CardLayout cl = ( CardLayout )parameterCard.getLayout();
      cl.show( parameterCard, cardStr );
    }
    else if ( source == condition )
    {
      int i = condition.getSelectedIndex();
      firstMacroLabel.setText( ToadTogFunction.onStrings[ i ]);
      secondMacroLabel.setText( ToadTogFunction.offStrings[ i ]);
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

      String typeStr = ( String )type.getSelectedItem();
      SpecialProtocol protocol = null;      
      for ( SpecialProtocol sp : config.getSpecialProtocols())
      {
        for ( String func : sp.getFunctions())
        {
          if ( func.equals( typeStr ))
          {
            protocol = sp;
            break;
          }
        }
        if ( protocol != null )
          break;
      }
      if ( protocol == null )
      {
        showWarning( "No special protocol found for type " + typeStr );
        return;
      }
      
      Hex hex = protocol.createHex( this );
      KeyMove km = new KeyMove( keyCode, deviceIndex, protocol.getDeviceType().getNumber(), 
                                protocol.getSetupCode(), hex, notes.getText());
      function = protocol.createFunction( km );
      if ( function == null )
        return;

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
    else if ( source == add )
    {
      addKey( 0 );
    }
    else if ( source == insert )
    {
      insertKey( 0 );
    }
    else if ( source == addShift )
    {
      addKey( remote.getShiftMask());
    }
    else if ( source == insertShift )
    {
      insertKey( remote.getShiftMask());
    }
    else if ( source == addXShift )
    {
      addKey( remote.getXShiftMask());
    }
    else if ( source == insertXShift )
    {
      insertKey( remote.getXShiftMask());
    }
    else if (( source == firstMoveUp ) || ( source == secondMoveUp ))
    {
      JList list = firstMacroButtons;
      if ( source == secondMoveUp )
        list = secondMacroButtons;
      int index = list.getSelectedIndex();
      swap( list, index, index - 1 );
    }
    else if (( source == firstMoveDown ) || ( source == secondMoveDown ))
    {
      JList list = firstMacroButtons;
      if ( source == secondMoveDown )
        list = secondMacroButtons;
      int index = list.getSelectedIndex();
      swap( list, index, index + 1 );
    }
    else if (( source == firstRemove ) || ( source == secondRemove ))
    {
      JList list = firstMacroButtons;
      if ( source == secondRemove )
        list = secondMacroButtons;
      int index = list.getSelectedIndex();
      DefaultListModel model = ( DefaultListModel )list.getModel();
      model.removeElementAt( index );
      int last = model.getSize() - 1;
      if ( index > last )
        index = last;
      list.setSelectedIndex( index );
    }
    else if ( source == firstClear )
    {
      
      (( DefaultListModel )firstMacroButtons.getModel()).clear();
    }
    else if ( source == secondClear )
    {
      
      (( DefaultListModel )secondMacroButtons.getModel()).clear();
    }
  }
  
  private int getSelectedKeyCode()
  {
    return (( Button )availableButtons.getSelectedValue()).getKeyCode();
  }
  
  private void addKey( int mask )
  {
    Integer value = new Integer( getSelectedKeyCode() | mask );
    (( DefaultListModel )targetList.getModel()).addElement( value );
  }
  
  private void insertKey( int mask )
  {
    Integer value = new Integer( getSelectedKeyCode() | mask );
    int index = targetList.getSelectedIndex();
    DefaultListModel model = ( DefaultListModel )targetList.getModel();
    if ( index == -1 )
      model.add( 0, value );
    else
      model.add( index, value );
    targetList.setSelectedIndex( index + 1 );
    targetList.ensureIndexIsVisible( index + 1 );
  }
  
  private void swap( JList list, int index1, int index2 )
  {
    DefaultListModel model = ( DefaultListModel )list.getModel();
    Object o1 = model.get( index1 );
    Object o2 = model.get( index2 );
    model.set( index1, o2 );
    model.set( index2, o1 );
    list.setSelectedIndex( index2 );
    list.ensureIndexIsVisible( index2 );
  }
  
  private void enableMacros( boolean flag )
  {
    enableMacros( flag, flag );
  }
  
  private void enableMacros( boolean mainFlag, boolean secondFlag )
  {
    if ( !mainFlag )
      secondFlag = false;
    
    macroBox.setEnabled( mainFlag );
    availableLabel.setEnabled( mainFlag );
    availableButtons.setEnabled( mainFlag );
    add.setEnabled( mainFlag );
    insert.setEnabled( mainFlag );
    addShift.setEnabled( mainFlag );
    insertShift.setEnabled( mainFlag );
    addXShift.setEnabled( mainFlag );
    insertXShift.setEnabled( mainFlag );
    
    firstKeysPanel.setEnabled( mainFlag );
    firstMacroLabel.setEnabled( mainFlag );
    firstMacroButtons.setEnabled( mainFlag );
    firstMoveUp.setEnabled( mainFlag );
    firstMoveDown.setEnabled( mainFlag );
    firstRemove.setEnabled( mainFlag );
    firstClear.setEnabled( mainFlag );
  
    secondKeysPanel.setEnabled( secondFlag );
    secondMacroLabel.setEnabled( secondFlag );
    secondMacroButtons.setEnabled( secondFlag );
    secondMoveUp.setEnabled( secondFlag );
    secondMoveDown.setEnabled( secondFlag );
    secondRemove.setEnabled( secondFlag );
    secondClear.setEnabled( secondFlag );

    if ( firstMacroButtons.isEnabled())
    {
      targetList = firstMacroButtons;
      targetList.setBackground( activeColor );
    }
    else
      firstMacroButtons.setBackground( disabledColor );
    
    if ( secondMacroButtons.isEnabled())
      secondMacroButtons.setBackground( inactiveColor );
    else
      secondMacroButtons.setBackground( disabledColor );
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
    Object source = e.getSource();
    if ( source instanceof JTextField )
    {
      focusField = ( JTextField )source;
      SwingUtilities.invokeLater( this );
      return;
    }
    if ( source instanceof JList )
    {
      setTarget(( JList )source );
      return;
    }
  }
  
  private void setTarget( JList list )
  {
    if (( targetList != list ) && targetList.isEnabled())
      targetList.setBackground( inactiveColor );
    targetList = list;
    targetList.setBackground( activeColor );
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

  // ListSelectionListener
  public void valueChanged( ListSelectionEvent e ) 
  {
    if ( e.getValueIsAdjusting())
      return;
    
    enableButtons();
  }
  
  private void enableButtons()
  {
    int limit = 15;
    if ( config.getRemote().getAdvCodeBindFormat() == Remote.LONG )
      limit = 255;
    DefaultListModel listModel = ( DefaultListModel )firstMacroButtons.getModel();
    boolean moreRoom = listModel.getSize() < limit;
    Button b = ( Button )availableButtons.getSelectedValue();
    boolean canAdd = ( b != null ) && moreRoom;
    
    add.setEnabled( canAdd && b.canAssignToMacro());
    insert.setEnabled( canAdd && b.canAssignToMacro());
    addShift.setEnabled( canAdd && b.canAssignShiftedToMacro());
    insertShift.setEnabled( canAdd && b.canAssignShiftedToMacro());
    boolean xShiftEnabled = config.getRemote().getXShiftEnabled();
    addXShift.setEnabled( xShiftEnabled && canAdd && b.canAssignXShiftedToMacro());
    insertXShift.setEnabled( xShiftEnabled && canAdd && b.canAssignXShiftedToMacro());

    int selected = firstMacroButtons.getSelectedIndex();
    firstMoveUp.setEnabled( selected > 0 );
    firstMoveDown.setEnabled(( selected != -1 ) && ( selected < ( listModel.getSize() - 1 )));
    firstRemove.setEnabled( selected != -1 );
    firstClear.setEnabled( listModel.getSize() > 0 );

    listModel = ( DefaultListModel )secondMacroButtons.getModel();
    moreRoom = listModel.getSize() < limit;
    canAdd = ( b != null ) && moreRoom;
    
    boolean isEnabled = secondMacroButtons.isEnabled();
    selected = secondMacroButtons.getSelectedIndex();
    secondMoveUp.setEnabled( isEnabled && ( selected > 0 ));
    secondMoveDown.setEnabled( isEnabled && ( selected != -1 ) && ( selected < ( listModel.getSize() - 1 )));
    secondRemove.setEnabled( isEnabled && ( selected != -1 ));
    secondClear.setEnabled( isEnabled && ( listModel.getSize() > 0 ));
  }

  private void setMacroButtons( Integer[] keyCodes, JList list )
  {
    DefaultListModel model = ( DefaultListModel )list.getModel();
    model.clear();
    for ( Integer keyCode : keyCodes )
      model.addElement( keyCode );
    list.setSelectedIndex( -1 );
  }
  
  private Integer[] getMacroButtons( JList list )
  {
    DefaultListModel model = ( DefaultListModel )list.getModel();
    Integer[] keyCodes = new Integer[ model.size()];
    for ( int i = 0; i < keyCodes.length; ++i )
      keyCodes[ i ] = ( Integer )model.elementAt( i );
    return keyCodes;
  }
  
  public class FunctionTypeRenderer
    extends DefaultListCellRenderer
  {
    public FunctionTypeRenderer(){}
    
    public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
    {
      String text = ( String )value;
      if ( text.charAt( 0 ) == 'U' )
        text = text.substring( 1 ); 
      return super.getListCellRendererComponent( list, text, index, isSelected, cellHasFocus );
    }
  }
  
  private JComboBox boundDevice = new JComboBox();
  private JComboBox boundKey = new JComboBox();
  private JCheckBox shift = new JCheckBox();
  private JCheckBox xShift = new JCheckBox();
  private JComboBox type = new JComboBox();
  public String getType(){ return ( String )type.getSelectedItem(); }
  private JPanel parameterCard = new JPanel( new CardLayout());
//  private JTextField hex = new JTextField( 12 );

  // for ModeName
  private JTextField modeName = new JTextField( 10 );
  public void setModeName( String text )
  {
    modeName.setText( text );
  }
  public String getModeName()
  {
    return modeName.getText();
  }
  
  // for Multiplex
  private JComboBox deviceType = new JComboBox();
  public void setDeviceType( int deviceTypeIndex )
  {
    deviceType.setSelectedIndex( deviceTypeIndex );
  }
  public int getDeviceType()
  {
    return deviceType.getSelectedIndex();
  }
  private JFormattedTextField setupCode = null;
  public void setSetupCode( int code )
  {
    setupCode.setValue( new Integer( code ));
  }
  public int getSetupCode()
  {
    return (( Integer )setupCode.getValue()).intValue();
  }
  

  // for Duration
  private JSpinner duration = new JSpinner( new WrappingSpinnerNumberModel( 0, 0, 255, 1 ));
  public void setDuration( int d )
  {
    duration.setValue( new Integer( d ));
  }
  public int getDuration()
  {
    return (( Integer )duration.getValue()).intValue();
  }

  // for ToadTog
  private JComboBox toggle = null;
  public void setToggle( int t )
  {
    toggle.setSelectedIndex( t );
  }
  public int getToggle()
  {
    return toggle.getSelectedIndex();
  }
  private JComboBox condition = new JComboBox( ToadTogFunction.styleStrings );
  public void setCondition( int c )
  {
    condition.setSelectedIndex( c );
  }
  public int getCondition()
  {
    return condition.getSelectedIndex();
  }
  
  // for UDSM
  private JComboBox macroKey = new JComboBox();
  public void setMacroKey( int keyCode )
  {
    macroKey.setSelectedItem( new Integer( keyCode ));
  }
  public int getMacroKey()
  {
    return (( Integer )macroKey.getSelectedItem()).intValue();
  }
  private KeyCodeListRenderer keyCodeRenderer = new KeyCodeListRenderer();
  
  // for ULDKP
  private JComboBox uldkpDuration = null;
  public void setULDKPDuration( int d )
  {
    uldkpDuration.setSelectedIndex( d );
  }
  public int getULDKPDuration()
  {
    return uldkpDuration.getSelectedIndex();
  }
  private JLabel firstKeyLabel = new JLabel();
  private JComboBox firstMacroKey = new JComboBox();
  public void setFirstMacroKey( int keyCode )
  {
    firstMacroKey.setSelectedItem( new Integer( keyCode ));
  }
  public int getFirstMacroKey()
  {
    return (( Integer )firstMacroKey.getSelectedItem()).intValue();
  }
  private JLabel secondKeyLabel = new JLabel();
  private JComboBox secondMacroKey = new JComboBox();
  public void setSecondMacroKey( int keyCode )
  {
    secondMacroKey.setSelectedItem( new Integer( keyCode ));
  }
  public int getSecondMacroKey()
  {
    return (( Integer )secondMacroKey.getSelectedItem()).intValue();
  }

  // For DSM/LDKP
  
  private Box macroBox = null;
  private JLabel availableLabel = new JLabel( "Available keys:" );
  private JList availableButtons = new JList();
  private JButton add = new JButton( "Add" );
  private JButton insert = new JButton( "Insert" );
  private JButton addShift = new JButton( "Add Shift" );
  private JButton insertShift = new JButton( "Ins Shift" );
  private JButton addXShift = new JButton( "Add xShift" );
  private JButton insertXShift = new JButton( "Ins xShift" );
  
  private MacroButtonRenderer macroButtonRenderer = new MacroButtonRenderer();
  
  private JPanel firstKeysPanel = null;
  private JLabel firstMacroLabel = new JLabel( "Short keys:" );
  private JList firstMacroButtons = new JList();
  private JList targetList = firstMacroButtons;
  public void setFirstMacroButtons( Integer[] keyCodes )
  {
    setMacroButtons( keyCodes, firstMacroButtons );
  }
  public Integer[] getFirstMacroButtons()
  {
    return getMacroButtons( firstMacroButtons );
  }
  private JButton firstMoveUp = new JButton( "Move up" );
  private JButton firstMoveDown = new JButton( "Move down" );
  private JButton firstRemove = new JButton( "Remove" );
  private JButton firstClear = new JButton( "Clear" );

  private JPanel secondKeysPanel = null;
  private JLabel secondMacroLabel = new JLabel( "Long keys:" );
  private JList secondMacroButtons = new JList();
  public void setSecondMacroButtons( Integer[] keyCodes )
  {
    setMacroButtons( keyCodes, secondMacroButtons );
  }
  public Integer[] getSecondMacroButtons()
  {
    return getMacroButtons( firstMacroButtons );
  }
  private JButton secondMoveUp = new JButton( "Move up" );
  private JButton secondMoveDown = new JButton( "Move down" );
  private JButton secondRemove = new JButton( "Remove" );
  private JButton secondClear = new JButton( "Clear" );
  
  private JTextArea notes = new JTextArea( 2, 2 );

  // action Buttons
  private JButton okButton = new JButton( "OK" );
  private JButton cancelButton = new JButton( "Cancel" );
  private JTextField focusField = null;
  
  private RemoteConfiguration config = null;
  private SpecialProtocolFunction function = null;
  private Hex cmd = null;
  private static SpecialFunctionDialog dialog = null;
  private Color activeColor;
  private Color inactiveColor;
  private Color disabledColor;
}
