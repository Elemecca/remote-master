package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

// TODO: Auto-generated Javadoc
/**
 * The Class MacroDialog.
 */
public class MacroDialog extends JDialog implements ActionListener, ListSelectionListener
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

    panel.add( new JLabel( "Key:" ) );
    panel.add( boundKey );

    shift.addActionListener( this );
    panel.add( shift );

    xShift.addActionListener( this );
    panel.add( xShift );

    // Add the Macro definition controls
    Box macroBox = Box.createHorizontalBox();
    macroBox.setBorder( BorderFactory.createTitledBorder( "Macro Definition" ) );
    contentPane.add( macroBox, BorderLayout.CENTER );

    JPanel availableBox = new JPanel( new BorderLayout() );
    macroBox.add( availableBox );
    availableBox.add( new JLabel( "Available keys:" ), BorderLayout.NORTH );
    availableButtons.setFixedCellWidth( 100 );
    availableBox.add( new JScrollPane( availableButtons ), BorderLayout.CENTER );
    availableButtons.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    availableButtons.addListSelectionListener( this );

    panel = new JPanel( new GridLayout( 3, 2, 2, 2 ) );
    panel.setBorder( BorderFactory.createEmptyBorder( 2, 0, 0, 0 ) );
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

    macroBox.add( Box.createHorizontalStrut( 20 ) );

    JPanel keysBox = new JPanel( new BorderLayout() );
    macroBox.add( keysBox );
    keysBox.add( new JLabel( "Macro Keys:" ), BorderLayout.NORTH );
    macroButtons.setFixedCellWidth( 100 );
    keysBox.add( new JScrollPane( macroButtons ), BorderLayout.CENTER );
    macroButtons.setModel( macroButtonModel );
    macroButtons.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    macroButtons.setCellRenderer( macroButtonRenderer );
    macroButtons.addListSelectionListener( this );

    JPanel buttonBox = new JPanel( new GridLayout( 3, 2, 2, 2 ) );
    buttonBox.setBorder( BorderFactory.createEmptyBorder( 2, 0, 0, 0 ) );
    keysBox.add( buttonBox, BorderLayout.SOUTH );
    moveUp.addActionListener( this );
    buttonBox.add( moveUp );
    moveDown.addActionListener( this );
    buttonBox.add( moveDown );
    remove.addActionListener( this );
    buttonBox.add( remove );
    clear.addActionListener( this );
    buttonBox.add( clear );

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

    boundKey.setModel( new DefaultComboBoxModel( remote.getUpgradeButtons() ) );

    shift.setText( remote.getShiftLabel() );
    xShift.setText( remote.getXShiftLabel() );
    xShift.setEnabled( remote.getXShiftEnabled() );

    java.util.List< Button > buttons = remote.getButtons();
    DefaultListModel model = new DefaultListModel();
    for ( Button b : buttons )
    {
      if ( b.canAssignToMacro() || b.canAssignShiftedToMacro() || b.canAssignXShiftedToMacro() )
        model.addElement( b );
    }
    availableButtons.setModel( model );

    macroButtonRenderer.setRemote( remote );
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

    availableButtons.setSelectedIndex( -1 );
    macroButtonModel.clear();

    if ( macro == null )
    {
      boundKey.setSelectedIndex( -1 );
      shift.setSelected( false );
      xShift.setSelected( false );
      notes.setText( null );
      enableButtons();
      return;
    }

    setButton( macro.getKeyCode(), boundKey, shift, xShift );
    short[] data = macro.getData().getData();
    for ( int i = 0; i < data.length; ++i )
      macroButtonModel.addElement( new Integer( data[ i ] ) );
    macroButtons.setSelectedIndex( -1 );

    notes.setText( macro.getNotes() );

    enableButtons();
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

  /**
   * Gets the selected key code.
   * 
   * @return the selected key code
   */
  private int getSelectedKeyCode()
  {
    return ( ( Button )availableButtons.getSelectedValue() ).getKeyCode();
  }

  /**
   * Adds the key.
   * 
   * @param mask
   *          the mask
   */
  private void addKey( int mask )
  {
    Integer value = new Integer( getSelectedKeyCode() | mask );
    macroButtonModel.addElement( value );
  }

  /**
   * Insert key.
   * 
   * @param mask
   *          the mask
   */
  private void insertKey( int mask )
  {
    Integer value = new Integer( getSelectedKeyCode() | mask );
    int index = macroButtons.getSelectedIndex();
    if ( index == -1 )
      macroButtonModel.add( 0, value );
    else
      macroButtonModel.add( index, value );
    macroButtons.setSelectedIndex( index + 1 );
    macroButtons.ensureIndexIsVisible( index + 1 );
  }

  /**
   * Swap.
   * 
   * @param index1
   *          the index1
   * @param index2
   *          the index2
   */
  private void swap( int index1, int index2 )
  {
    Object o1 = macroButtonModel.get( index1 );
    Object o2 = macroButtonModel.get( index2 );
    macroButtonModel.set( index1, o2 );
    macroButtonModel.set( index2, o1 );
    macroButtons.setSelectedIndex( index2 );
    macroButtons.ensureIndexIsVisible( index2 );
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
    if ( source == okButton )
    {
      if ( boundKey.getSelectedItem() == null )
      {
        showWarning( "You must select a key for the bound key." );
        return;
      }
      int keyCode = getKeyCode( boundKey, shift, xShift );

      int length = macroButtonModel.getSize();
      if ( length == 0 )
      {
        showWarning( "You haven't included any keys in your macro!" );
        return;
      }

      short[] keyCodes = new short[ length ];
      for ( int i = 0; i < length; ++i )
        keyCodes[ i ] = ( ( Number )macroButtonModel.elementAt( i ) ).shortValue();

      String notesStr = notes.getText();

      macro = new Macro( keyCode, new Hex( keyCodes ), notesStr );
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
        xShift.setSelected( false );
    }
    else if ( source == xShift )
    {
      if ( xShift.isSelected() )
        shift.setSelected( false );
    }
    else if ( source == boundKey )
    {
      Button b = ( Button )boundKey.getSelectedItem();
      if ( b == null )
        return;
      shift.setEnabled( b.allowsShiftedKeyMove() );
      if ( !b.allowsShiftedKeyMove() )
        shift.setSelected( false );
      if ( b.getIsShifted() )
        shift.setSelected( true );

      xShift.setEnabled( remote.getXShiftEnabled() & b.allowsXShiftedKeyMove() );
      if ( !b.allowsXShiftedKeyMove() )
        xShift.setSelected( false );
      if ( b.getIsXShifted() )
        xShift.setSelected( true );
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
      addKey( remote.getShiftMask() );
    }
    else if ( source == insertShift )
    {
      insertKey( remote.getShiftMask() );
    }
    else if ( source == addXShift )
    {
      addKey( remote.getXShiftMask() );
    }
    else if ( source == insertXShift )
    {
      insertKey( remote.getXShiftMask() );
    }
    else if ( source == moveUp )
    {
      int index = macroButtons.getSelectedIndex();
      swap( index, index - 1 );
    }
    else if ( source == moveDown )
    {
      int index = macroButtons.getSelectedIndex();
      swap( index, index + 1 );
    }
    else if ( source == remove )
    {
      int index = macroButtons.getSelectedIndex();
      macroButtonModel.removeElementAt( index );
      int last = macroButtonModel.getSize() - 1;
      if ( index > last )
        index = last;
      macroButtons.setSelectedIndex( index );
    }
    else if ( source == clear )
    {
      macroButtonModel.clear();
    }
    enableButtons();
  }

  /**
   * Enable buttons.
   */
  private void enableButtons()
  {
    int limit = 15;
    if ( config.getRemote().getAdvCodeBindFormat() == AdvancedCode.BindFormat.LONG )
      limit = 255;
    boolean moreRoom = macroButtonModel.getSize() < limit;
    Button b = ( Button )availableButtons.getSelectedValue();
    boolean canAdd = ( b != null ) && moreRoom;

    add.setEnabled( canAdd && b.canAssignToMacro() );
    insert.setEnabled( canAdd && b.canAssignToMacro() );
    addShift.setEnabled( canAdd && b.canAssignShiftedToMacro() );
    insertShift.setEnabled( canAdd && b.canAssignShiftedToMacro() );
    boolean xShiftEnabled = config.getRemote().getXShiftEnabled();
    addXShift.setEnabled( xShiftEnabled && canAdd && b.canAssignXShiftedToMacro() );
    insertXShift.setEnabled( xShiftEnabled && canAdd && b.canAssignXShiftedToMacro() );

    int selected = macroButtons.getSelectedIndex();
    moveUp.setEnabled( selected > 0 );
    moveDown.setEnabled( ( selected != -1 ) && ( selected < ( macroButtonModel.getSize() - 1 ) ) );
    remove.setEnabled( selected != -1 );
    clear.setEnabled( macroButtonModel.getSize() > 0 );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
   */
  public void valueChanged( ListSelectionEvent e )
  {
    if ( e.getValueIsAdjusting() )
      return;

    enableButtons();
  }

  /** The bound key. */
  private JComboBox boundKey = new JComboBox();

  /** The shift. */
  private JCheckBox shift = new JCheckBox();

  /** The x shift. */
  private JCheckBox xShift = new JCheckBox();

  /** The available buttons. */
  private JList availableButtons = new JList();

  /** The add. */
  private JButton add = new JButton( "Add" );

  /** The insert. */
  private JButton insert = new JButton( "Insert" );

  /** The add shift. */
  private JButton addShift = new JButton( "Add Shift" );

  /** The insert shift. */
  private JButton insertShift = new JButton( "Ins Shift" );

  /** The add x shift. */
  private JButton addXShift = new JButton( "Add xShift" );

  /** The insert x shift. */
  private JButton insertXShift = new JButton( "Ins xShift" );

  /** The macro button renderer. */
  private MacroButtonRenderer macroButtonRenderer = new MacroButtonRenderer();

  /** The macro button model. */
  private DefaultListModel macroButtonModel = new DefaultListModel();

  /** The macro buttons. */
  private JList macroButtons = new JList();

  /** The move up. */
  private JButton moveUp = new JButton( "Move up" );

  /** The move down. */
  private JButton moveDown = new JButton( "Move down" );

  /** The remove. */
  private JButton remove = new JButton( "Remove" );

  /** The clear. */
  private JButton clear = new JButton( "Clear" );

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

  /** The dialog. */
  private static MacroDialog dialog = null;
}
