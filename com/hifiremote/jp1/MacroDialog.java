package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

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
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import com.hifiremote.jp1.RemoteConfiguration.KeySpec;

// TODO: Auto-generated Javadoc
/**
 * The Class MacroDialog.
 */
public class MacroDialog extends JDialog implements ActionListener, ButtonEnabler
{

  /**
   * Show dialog.
   * 
   * @param locationComp
   *          the location comp
   * @param macro
   *          the macro
   * @param config
   *          the config
   * @return the macro
   */
  public static Macro showDialog( Component locationComp, Macro macro, RemoteConfiguration config )
  {
    if ( dialog == null )
      dialog = new MacroDialog( locationComp );

    dialog.setRemoteConfiguration( config );
    dialog.setMacro( macro );

    dialog.pack();
    dialog.setLocationRelativeTo( locationComp );
    dialog.setVisible( true );
    return dialog.macro;
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
   * Instantiates a new macro dialog.
   * 
   * @param c
   *          the c
   */
  private MacroDialog( Component c )
  {
    super( ( JFrame )SwingUtilities.getRoot( c ) );
    setTitle( "Macro" );
    setModal( true );

    JComponent contentPane = ( JComponent )getContentPane();
    contentPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
    upperPanel = new JPanel( new BorderLayout() );
    contentPane.add( upperPanel, BorderLayout.NORTH );
    
    // Add the bound device and key controls
    boundPanel = new JPanel( new WrapLayout( FlowLayout.LEFT ) );
    upperPanel.add( boundPanel, BorderLayout.CENTER );
    boundPanel.setBorder( BorderFactory.createTitledBorder( "Bound Key" ) );

    boundKey.addActionListener( this );
    
    // Add the Macro definition controls
    macroBox = new MacroDefinitionBox();
    macroBox.setButtonEnabler( this );
    contentPane.add( macroBox, BorderLayout.CENTER );

    JPanel bottomPanel = new JPanel( new BorderLayout() );
    contentPane.add( bottomPanel, BorderLayout.SOUTH );
    
    // Add the notes
    JPanel panel = new JPanel( new BorderLayout() );
    bottomPanel.add( panel, BorderLayout.NORTH );
    panel.setBorder( BorderFactory.createTitledBorder( "Notes" ) );
    notes.setLineWrap( true );
    panel.add( new JScrollPane( notes, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER ) );

    // Add the action buttons
    panel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
    bottomPanel.add( panel, BorderLayout.SOUTH );

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
    if ( this.config == config )
      return;

    this.config = config;
    Remote remote = config.getRemote();

    boundKey.setModel( new DefaultComboBoxModel( remote.getMacroButtons() ) );
    if ( remote.getMacroButtons().length > 0 )
    {
      boundKey.setSelectedIndex( 0 );
    }
    
    if ( remote.usesEZRC() )
    {
      nameField = new JTextField( 20 );
      JPanel panel = new JPanel( new WrapLayout() );
      panel.add( new JLabel( "Name:") );
      panel.add( nameField );
      upperPanel.add(  panel, BorderLayout.PAGE_START );
      boundDevice = new JComboBox();
      remote.setDeviceComboBox( boundDevice );
      boundPanel.add( new JLabel( "Device:" ) );
      boundPanel.add( boundDevice );
      boundPanel.add( Box.createHorizontalStrut( 5 ) );
      boundPanel.add( new JLabel( "Key:" ) );
      boundPanel.add( boundKey );
    }
    else
    {
      shift.setText( remote.getShiftLabel() );
      shift.setEnabled( remote.getShiftEnabled() );
      xShift.setText( remote.getXShiftLabel() );
      xShift.setEnabled( remote.getXShiftEnabled() );
      boundPanel.add( Box.createHorizontalStrut( 5 ) );
      boundPanel.add( new JLabel( "Key:" ) );
      boundPanel.add( boundKey );
      shift.addActionListener( this );
      boundPanel.add( shift );
      xShift.addActionListener( this );
      boundPanel.add( xShift );
    }
    
    macroBox.setRemoteConfiguration( config );
  }

  /**
   * Sets the macro.
   * 
   * @param macro
   *          the new macro
   */
  @SuppressWarnings( "unchecked" )
  private void setMacro( Macro macro )
  {
    this.macro = macro;

    if ( macro == null )
    {
      boundKey.setSelectedIndex( -1 );
      shift.setSelected( false );
      xShift.setSelected( false );
      macroBox.setValue( null );
//      macroBox.setValue( ( List< KeySpec > )null );
      notes.setText( null );
    }
    else
    {
      setButton( macro.getKeyCode(), boundKey, shift, xShift );
//      macroBox.setValue( macro.getData() );
      Object val = macro.getValue();
      if ( val instanceof Hex )
      {
        macroBox.setValue( ( Hex )val );
      }
      else if ( val instanceof List< ? > )
      {
        macroBox.setValue( ( List< KeySpec > )val );
      }
      if ( config.getRemote().usesEZRC() )
      {
        boundDevice.setSelectedItem( macro.getDeviceButton( config ) );
        nameField.setText( macro.getName() );
      }
      notes.setText( macro.getNotes() );
    }

    macroBox.enableButtons();
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
    shiftBox.setSelected( false );
    xShiftBox.setSelected( false );

    if ( b == null )
    {
      int base = code & 0x3F;
      if ( base != 0 )
      {
        b = remote.getButton( base );
        if ( remote.getShiftEnabled() &&( ( base | remote.getShiftMask() ) == code ) )
        {
          shiftBox.setEnabled( remote.getShiftEnabled() && b.allowsShiftedMacro() );
          shiftBox.setSelected( true );
          comboBox.setSelectedItem( b );
          return;
        }
        if ( remote.getXShiftEnabled() && ( ( base | remote.getXShiftMask() ) == code ) )
        {
          xShiftBox.setEnabled( remote.getXShiftEnabled() && b.allowsXShiftedMacro() );
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

    shiftBox.setEnabled( remote.getShiftEnabled() && b.allowsShiftedMacro() );
    xShiftBox.setEnabled( remote.getXShiftEnabled() && b.allowsXShiftedMacro() );

    if ( remote.getXShiftEnabled() && b.getIsXShifted() )
      xShiftBox.setSelected( true );
    else if ( remote.getShiftEnabled() && b.getIsShifted() )
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
    Remote remote = config.getRemote();
    Button b = ( Button )boundKey.getSelectedItem();

    if ( source == okButton )
    {
      String name = null;
      DeviceButton db = null;
      if ( remote.isSSD() )
      {
        name = nameField.getText();
        if ( name == null || name.isEmpty() )
        {
          showWarning( "You must give a name for this macro." );
          return;
        }        
        if ( boundDevice.getSelectedItem() == null )
        {
          showWarning( "You must select a device for the bound key." );
          return;
        }
        db = ( DeviceButton )boundDevice.getSelectedItem();
      }
      if ( boundKey.getSelectedItem() == null )
      {
        showWarning( "You must select a key for the bound key." );
        return;
      }
      int keyCode = getKeyCode( boundKey, shift, xShift );

      if ( macroBox.isEmpty() )
      {
        showWarning( "You haven't included any keys in your macro!" );
        return;
      }

      String notesStr = notes.getText();
      Object value = macroBox.getValue();
      Macro newMacro = new Macro( keyCode, null, notesStr );
      if ( remote.isSSD() )
      {
        @SuppressWarnings( "unchecked" )
        List< KeySpec >items = ( List< KeySpec > )value;
        newMacro.setItems( items );
        newMacro.setName( name );
        newMacro.setDeviceButtonIndex( db.getButtonIndex() );
        newMacro.setSerial( config.getNewMacroSerial() );
//        DeviceUpgrade du = macro.getUpgrade( remote );
//        du.setFunction( b, newMacro, Button.NORMAL_STATE );
      }
      else
      {
        Hex data = ( Hex )value;
        newMacro.setData( data );
      }
      
      if ( config.hasSegments() && !remote.isSSD() )
      {
        // set default values
        if ( macro == null && remote.usesEZRC() )
        {
          newMacro.setDeviceButtonIndex( remote.getDeviceButtons()[ 0 ].getButtonIndex() );
          newMacro.setSegmentFlags( 0xFF );
          newMacro.setName( "New macro" );
          newMacro.setSerial( config.getNewMacroSerial() );
        }
        else if ( macro == null )
        {
          newMacro.setDeviceButtonIndex( 0 );
          newMacro.setSegmentFlags( 0xFF );
        }
        else
        {
          newMacro.setName( macro.getName() );
          newMacro.setSegmentFlags( macro.getSegmentFlags() );
          newMacro.setDeviceButtonIndex( macro.getDeviceButtonIndex() );
          newMacro.setSerial( macro.getSerial() );
        }
      }
      if ( remote.isSSD() )
      {
        if ( macro != null )
        {
          Button currb = remote.getButton( macro.getKeyCode() );
          DeviceUpgrade currdb = macro.getUpgrade( remote );
          currdb.setFunction( currb, null, Button.NORMAL_STATE );
        }
        DeviceUpgrade du = newMacro.getUpgrade( remote );
        du.setFunction( b, newMacro, Button.NORMAL_STATE );
      }
      macro = newMacro;
      setVisible( false );
    }
    else if ( source == cancelButton )
    {
      macro = null;
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
        xShift.setSelected( b.needsShift( Button.MACRO_BIND ) );
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
        shift.setSelected( b.needsShift( Button.MACRO_BIND ) );
      }
    }    
    else if ( source == boundKey )
    {
      if ( b != null )
      {
        b.setShiftBoxes( Button.MACRO_BIND, shift, xShift );
      }
    } 
  }

  @Override
  public void enableButtons( Button b, MacroDefinitionBox macroBox )
  {
    if ( config.getRemote().usesEZRC() )
    {
      macroBox.add.setEnabled( true );
      macroBox.insert.setEnabled( true );
      macroBox.addShift.setVisible( false );
      macroBox.addXShift.setVisible( false );
      macroBox.insertShift.setVisible( false );
      macroBox.insertXShift.setVisible( false );
      return;
    }
    int limit = 15;
    if ( config.getRemote().getAdvCodeBindFormat() == AdvancedCode.BindFormat.LONG )
      limit = 255;
    boolean canAdd = ( b != null ) && macroBox.isMoreRoom( limit );

    macroBox.add.setEnabled( canAdd && b.canAssignToMacro() );
    macroBox.insert.setEnabled( canAdd && b.canAssignToMacro() );
    macroBox.addShift.setVisible( true );
    macroBox.addXShift.setVisible( true );
    macroBox.insertShift.setVisible( true );
    macroBox.insertXShift.setVisible( true );
    boolean shiftEnabled = config.getRemote().getShiftEnabled();
    macroBox.addShift.setEnabled( shiftEnabled && canAdd && b.canAssignShiftedToMacro() );
    macroBox.insertShift.setEnabled( shiftEnabled && canAdd && b.canAssignShiftedToMacro() );
    boolean xShiftEnabled = config.getRemote().getXShiftEnabled();
    macroBox.addXShift.setEnabled( xShiftEnabled && canAdd && b.canAssignXShiftedToMacro() );
    macroBox.insertXShift.setEnabled( xShiftEnabled && canAdd && b.canAssignXShiftedToMacro() );
  }
  
  @Override
  public boolean isAvailable( Button b )
  {
    return  b.canAssignToMacro() 
    || b.canAssignShiftedToMacro() 
    || b.canAssignXShiftedToMacro();
  }

  private JPanel upperPanel = null;
  private JPanel boundPanel = null;
  private JComboBox boundKey = new JComboBox();
  private JComboBox boundDevice = null;
  private JTextField nameField = null;

  /** The shift. */
  private JCheckBox shift = new JCheckBox();

  /** The x shift. */
  private JCheckBox xShift = new JCheckBox();

  /** The ok button. */
  private JButton okButton = new JButton( "OK" );

  /** The cancel button. */
  private JButton cancelButton = new JButton( "Cancel" );

  /** The notes. */
  private JTextArea notes = new JTextArea( 2, 10 );

  /** The config. */
  private RemoteConfiguration config = null;

  /** The macro. */
  private Macro macro = null;
  
  private MacroDefinitionBox macroBox = null;

  /** The dialog. */
  private static MacroDialog dialog = null;
  
}
