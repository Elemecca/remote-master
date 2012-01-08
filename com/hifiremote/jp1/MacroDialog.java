package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

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

    // Add the bound device and key controls
    JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
    contentPane.add( panel, BorderLayout.NORTH );
    panel.setBorder( BorderFactory.createTitledBorder( "Bound Key" ) );

    panel.add( Box.createHorizontalStrut( 5 ) );

    boundKey.addActionListener( this );
    panel.add( new JLabel( "Key:" ) );
    panel.add( boundKey );

    shift.addActionListener( this );
    panel.add( shift );

    xShift.addActionListener( this );
    panel.add( xShift );

    // Add the Macro definition controls
    macroBox = new MacroDefinitionBox();
    macroBox.setButtonEnabler( this );
    contentPane.add( macroBox, BorderLayout.CENTER );

    JPanel bottomPanel = new JPanel( new BorderLayout() );
    contentPane.add( bottomPanel, BorderLayout.SOUTH );
    
    // Add the notes
    panel = new JPanel( new BorderLayout() );
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

    shift.setText( remote.getShiftLabel() );
    xShift.setText( remote.getXShiftLabel() );
    xShift.setEnabled( remote.getXShiftEnabled() );

    macroBox.setRemoteConfiguration( config );
  }

  /**
   * Sets the macro.
   * 
   * @param macro
   *          the new macro
   */
  private void setMacro( Macro macro )
  {
    this.macro = null;

    if ( macro == null )
    {
      boundKey.setSelectedIndex( -1 );
      shift.setSelected( false );
      xShift.setSelected( false );
      macroBox.setValue( null );
      notes.setText( null );
    }
    else
    {
      setButton( macro.getKeyCode(), boundKey, shift, xShift );
      macroBox.setValue( macro.getData() );
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

    shiftBox.setEnabled( b.allowsShiftedMacro() );
    xShiftBox.setEnabled( remote.getXShiftEnabled() & b.allowsXShiftedMacro() );

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
    Remote remote = config.getRemote();
    Button b = ( Button )boundKey.getSelectedItem();
    if ( source == okButton )
    {
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
      
      Hex data = macroBox.getValue();

      String notesStr = notes.getText();

      macro = new Macro( keyCode, data, notesStr );
      if ( config.hasSegments() )
      {
        // set default values
        macro.setDeviceIndex( 0 );
        macro.setSegmentFlags( 0xFF );
      }
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
    int limit = 15;
    if ( config.getRemote().getAdvCodeBindFormat() == AdvancedCode.BindFormat.LONG )
      limit = 255;
    boolean canAdd = ( b != null ) && macroBox.isMoreRoom( limit );

    macroBox.add.setEnabled( canAdd && b.canAssignToMacro() );
    macroBox.insert.setEnabled( canAdd && b.canAssignToMacro() );
    macroBox.addShift.setEnabled( canAdd && b.canAssignShiftedToMacro() );
    macroBox.insertShift.setEnabled( canAdd && b.canAssignShiftedToMacro() );
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
