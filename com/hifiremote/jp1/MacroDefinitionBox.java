package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class MacroDefinitionBox extends Box implements ActionListener, ListSelectionListener
{
  public MacroDefinitionBox( ButtonEnabler buttonEnabler, JList availableButtons, JList macroButtons )
  {
    super( BoxLayout.X_AXIS );
    this.buttonEnabler = buttonEnabler;
    this.availableButtons = availableButtons;
    this.macroButtons = macroButtons;
    macroButtonModel = ( DefaultListModel )macroButtons.getModel();
    setBorder( BorderFactory.createTitledBorder( "Macro Definition" ) );

    JPanel availableBox = new JPanel( new BorderLayout() );
    add( availableBox );
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
    
    add( Box.createHorizontalStrut( 20 ) );

    JPanel keysBox = new JPanel( new BorderLayout() );
    add( keysBox );
    keysBox.add( new JLabel( "Macro Keys:" ), BorderLayout.NORTH );
    macroButtons.setCellRenderer( macroButtonRenderer );    
    macroButtons.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    macroButtons.addListSelectionListener( this );
    macroButtons.setFixedCellWidth( 100 );
    keysBox.add( new JScrollPane( macroButtons ), BorderLayout.CENTER );

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
  }
  
  public void setRemoteConfiguration( RemoteConfiguration config )
  {
    this.config = config;
    Remote remote = config.getRemote();
    macroButtonRenderer.setRemote( remote );
  }  
  
  public void actionPerformed( ActionEvent event )
  {
    Object source = event.getSource();
    Remote remote = config.getRemote();
    if ( source == add )
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
    buttonEnabler.enableButtons();
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
  
  /**
   * Gets the selected key code.
   * 
   * @return the selected key code
   */
  private int getSelectedKeyCode()
  {
    return ( ( Button )availableButtons.getSelectedValue() ).getKeyCode();
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
    buttonEnabler.enableButtons();
  }
  
  /**
   * Enable buttons.
   */
  public void enableButtons()
  {
    int selected = macroButtons.getSelectedIndex();
    moveUp.setEnabled( selected > 0 );
    moveDown.setEnabled( ( selected != -1 ) && ( selected < ( macroButtonModel.getSize() - 1 ) ) );
    remove.setEnabled( selected != -1 );
    clear.setEnabled( macroButtonModel.getSize() > 0 );
  }

  
  /** The add. */
  protected JButton add = new JButton( "Add" );

  /** The insert. */
  protected JButton insert = new JButton( "Insert" );

  /** The add shift. */
  protected JButton addShift = new JButton( "Add Shift" );

  /** The insert shift. */
  protected JButton insertShift = new JButton( "Ins Shift" );

  /** The add x shift. */
  protected JButton addXShift = new JButton( "Add xShift" );

  /** The insert x shift. */
  protected JButton insertXShift = new JButton( "Ins xShift" );
  
  private JButton moveUp = new JButton( "Move up" );

  /** The move down. */
  private JButton moveDown = new JButton( "Move down" );

  /** The remove. */
  private JButton remove = new JButton( "Remove" );

  /** The clear. */
  private JButton clear = new JButton( "Clear" );

  /** The config. */
  private RemoteConfiguration config = null;

  /** The available buttons. */
  private JList availableButtons = new JList();
  
  /** The macro buttons. */
  private JList macroButtons = new JList();
  
  private JPanel panel = null;
  
  private ButtonEnabler buttonEnabler = null;
  
  /** The macro button model. */
  private DefaultListModel macroButtonModel = null;
  
  /** The macro button renderer. */
  private MacroButtonRenderer macroButtonRenderer = new MacroButtonRenderer();
}
