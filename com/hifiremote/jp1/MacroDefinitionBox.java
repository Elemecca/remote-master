package com.hifiremote.jp1;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.NumberFormatter;

import com.hifiremote.jp1.GeneralFunction.User;
import com.hifiremote.jp1.RemoteConfiguration.KeySpec;

public class MacroDefinitionBox extends Box implements ActionListener, ListSelectionListener,
PropertyChangeListener, RMSetter< Object >
{
  public MacroDefinitionBox()
  {
    super( BoxLayout.X_AXIS );
    macroButtons.setModel( macroButtonModel );
    setBorder( BorderFactory.createTitledBorder( "Macro Definition" ) );

    creationPanel = new JPanel( new CardLayout() );
//    add( creationPanel );
    
    JPanel availableBox = new JPanel( new BorderLayout() );
    add( availableBox );
    availableBox.add(  creationPanel, BorderLayout.CENTER );
    JPanel availablePanel = new JPanel( new BorderLayout() );
    availablePanel.add( new JLabel( "Available keys:" ), BorderLayout.NORTH );
    availableButtons.setFixedCellWidth( 100 );
    
    availablePanel.add( new JScrollPane( availableButtons ), BorderLayout.CENTER );

    availableButtons.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    availableButtons.addListSelectionListener( this );
       
    double b = 5; // space between rows and around border
    double c = 10; // space between columns
    double pr = TableLayout.PREFERRED;
    double pf = TableLayout.FILL;
    double size[][] =
    {
        {
            b, pr, c, pf, b
        }, // cols
        {
            b, pr, b, pr, b, pr, b, pr, pr, b
        }  // rows
    };


    JPanel ssdPanel = new JPanel( new BorderLayout() );
    ssdPanel.add( new JLabel( "Specify macro item:"), BorderLayout.PAGE_START );
    itemPanel = new JPanel( new TableLayout( size ) );
    ssdPanel.add( itemPanel, BorderLayout.CENTER );
    itemPanel.add( new JLabel( "Device:"), "1, 1" );
    deviceBox = new JComboBox();
    deviceBox.addActionListener( this );
    itemPanel.add( deviceBox, "3, 1"  );
    itemPanel.add( new JLabel( "Function:"), "1, 3" );
    functionBox = new JComboBox();
    itemPanel.add( functionBox, "3, 3"  );
    
    creationPanel.add( availablePanel, "Normal");
    creationPanel.add( ssdPanel, "SSD");
    
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
    
    JPanel buttonPanel = new JPanel( new BorderLayout() );
    keysBox.add( buttonPanel, BorderLayout.SOUTH );
    
    JPanel buttonBox = new JPanel( new GridLayout( 2, 2, 2, 2 ) );
    buttonPanel.add(  buttonBox, BorderLayout.PAGE_START );
    buttonPanel.setBorder( BorderFactory.createEmptyBorder( 2, 0, 0, 0 ) );
    buttonPanel.add(  Box.createVerticalStrut( ( new JTextField() ).getPreferredSize().height + 4 ), BorderLayout.LINE_START );
    
    moveUp.addActionListener( this );
    buttonBox.add( moveUp );
    moveDown.addActionListener( this );
    buttonBox.add( moveDown );
    remove.addActionListener( this );
    buttonBox.add( remove );
    clear.addActionListener( this );
    buttonBox.add( clear );
    
    formatter = new NumberFormatter( new DecimalFormat( "0.0" ) );
    formatter.setValueClass( Float.class );
    duration = new XFormattedTextField( formatter );
    duration.setColumns( 4 );
    duration.addActionListener( this );
    duration.setToolTipText( "<HTML>To edit the pause after any key, select it, enter the duration (minimum 0.1 sec) and press Return.<br>"
        + "To emulate holding a key, precede it with the special Hold key and add the<br>hold duration to it in the same way.</HTML>" );
    buttonPanel.add( durationPanel, BorderLayout.CENTER );
  }

  private class XFormattedTextField extends JFormattedTextField
  {
    XFormattedTextField( NumberFormatter formatter )
    {
      super( formatter );
      setFocusLostBehavior( JFormattedTextField.COMMIT_OR_REVERT );
    }
    
    @Override
    protected void processFocusEvent( FocusEvent e ) 
    {
      super.processFocusEvent( e );
      if ( e.getID() == FocusEvent.FOCUS_GAINED )
      {  
        selectAll();
      }  
    }
  }

  public void setButtonEnabler( ButtonEnabler buttonEnabler )
  {
    this.buttonEnabler = buttonEnabler;
  }

  @Override
  public void setRemoteConfiguration( RemoteConfiguration config )
  {
    this.config = config;
    Remote remote = config.getRemote();
    macroButtonRenderer.setRemote( remote );
    durationPanel.setVisible( false );
    
    java.util.List< Button > buttons = remote.getButtons();
    for ( Button b : buttons )
    {
      if ( buttonEnabler.isAvailable( b ) )
      {  
        availableButtonModel.addElement( b );
      }
    }
    availableButtons.setModel( availableButtonModel );
    CardLayout cl = ( CardLayout)creationPanel.getLayout();
    cl.show( creationPanel, remote.usesEZRC() ? "SSD" : "Normal" );
    if ( remote.usesEZRC() )
    {
      remote.setDeviceComboBox( deviceBox );
      holdCheck = new JCheckBox( "Hold?" );
      holdCheck.addActionListener( this );
      delay = new XFormattedTextField( formatter );
      delay.addPropertyChangeListener( "value", this );
      itemPanel.add( new JLabel( "Pause after (secs):" ), "1, 5" );
      itemPanel.add( delay, "3, 5" );
      delay.setValue( 0.3f );
      itemPanel.add( holdCheck, "1, 7" );
      durationLabel.setText( "Hold for (secs):" );
      itemPanel.add( durationLabel, "1, 8" );
      itemPanel.add( duration, "3, 8" );
      duration.setValue( 0.0f );
      duration.setEnabled( false );
      durationLabel.setEnabled( false );
//      duration.setFocusLostBehavior( JFormattedTextField.COMMIT_OR_REVERT );
    }
//    else if ( remote.usesEZRC() )
//    {
//      FontMetrics fm = durationLabel.getFontMetrics( durationLabel.getFont() );
//      int width = fm.stringWidth( "Pause after for:  " );
//      durationLabel.setPreferredSize( new Dimension( width, durationLabel.getHeight() ) );
//      durationLabel.setLabelFor( duration );
//      durationLabel.setHorizontalAlignment( SwingConstants.RIGHT );
//      duration.setFocusLostBehavior( JFormattedTextField.PERSIST );
//      durationPanel.setBorder( BorderFactory.createEmptyBorder( 3, 0, 1, 0 ) );
//      durationPanel.add( durationLabel, BorderLayout.LINE_START );
//      durationPanel.add( duration, BorderLayout.CENTER );
//      durationPanel.add( durationSuffix, BorderLayout.LINE_END );
//      durationPanel.setVisible( true );
//    }
    
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
    else if ( source == duration )
    {
      // minimum duration is 0 for hold buttons but 0.1 for others
      float f = ( Float )duration.getValue();
      if ( !remote.usesEZRC() )
      {
        int selected = macroButtons.getSelectedIndex();
        int val = ( ( Number )macroButtonModel.elementAt( selected ) ).intValue();
        val &= 0xFF;
        Button btn = remote.getButton( val );
        int pdVal = Math.max( ( int )( 10.0 * f + 0.5 ), isHold( btn ) ? 0 : 1 );
        val |= pdVal << 8;
        macroButtonModel.set( selected, val ); 
      }
    }
    else if ( source == deviceBox )
    {
      DeviceButton db = ( DeviceButton )deviceBox.getSelectedItem();
      DefaultComboBoxModel model = new DefaultComboBoxModel( db.getUpgrade().getGeneralFunctionList().toArray() );
      functionBox.setModel( model );
    }
    else if ( source == holdCheck )
    {
      duration.setEnabled( holdCheck.isSelected() );
      durationLabel.setEnabled( holdCheck.isSelected() );
      if ( !holdCheck.isSelected() )
      {
        duration.setValue( 0.0f );
      }
    }
    enableButtons();
  }
  
  private KeySpec getKeySpec()
  {
    DeviceButton db = ( DeviceButton )deviceBox.getSelectedItem();
    GeneralFunction f = ( GeneralFunction )functionBox.getSelectedItem();
    KeySpec ks = null;
    
    if ( f instanceof Function )
    {
      ks = new KeySpec( db, f );
    }
    else if ( !f.getUsers().isEmpty() )
    {
      // For XSight Touch, this is only learns and Selector keys (Home etc)
      User u = f.getUsers().get( 0 );
      ks = new KeySpec( u.db, u.button );
    }
    else
    {
      // This case should not occur
      return null;
    }
    
    Float fv = ( Float )delay.getValue();
    ks.delay = fv == null ? 0 : ( int )( 10.0 * fv + 0.5 );
    if ( holdCheck.isSelected() )
    {
      fv = ( Float )duration.getValue();
      ks.duration = fv == null ? 0 : ( int )( 10.0 * fv + 0.5 );
    }
    else
    {
      ks.duration = -1;
    }
    return ks;
  }
  
  /**
   * Adds the key.
   * 
   * @param mask
   *          the mask
   */
  private void addKey( int mask )
  {
    Remote remote = config.getRemote();
    if ( remote.usesEZRC() )
    {
      macroButtonModel.addElement( getKeySpec() );
      return;
    }
//    if ( remote.usesEZRC() )
//    {
//      // minimum duration is 0 for hold buttons but 0.1 for others
//      Button btn = ( Button )availableButtons.getSelectedValue();
//      mask |= isHold( btn ) ? 0 : 0x100;
//    }
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
    int index = macroButtons.getSelectedIndex();
    if ( config.getRemote().usesEZRC() )
    {
      KeySpec value = getKeySpec();
      macroButtonModel.add( index, value );
    }
    else
    {
      Integer value = new Integer( getSelectedKeyCode() | mask );
      if ( index == -1 )
        macroButtonModel.add( 0, value );
      else
        macroButtonModel.add( index, value );
    }
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
  
  public boolean isMoreRoom( int limit )
  {
    return macroButtonModel.getSize() < limit;
  }

  public boolean isEmpty()
  {
    return macroButtonModel.getSize() == 0;
  }
  
  private boolean isHold( Button btn )
  {
    Remote remote = config.getRemote();
    if ( remote.usesEZRC() )
    {
      LinkedHashMap< String, List< Button >> groups = remote.getButtonGroups();
      List< Button > holdList = groups != null ? groups.get( "Hold" ) : null;
      return holdList != null && holdList.contains( btn );
    }
    return false;
  }
  
  @Override
  public Object getValue()
  {
    int length = macroButtonModel.getSize();
    if ( config.getRemote().usesEZRC() )
    {
      List< KeySpec > items = new ArrayList< KeySpec >();
      for ( int i = 0; i < length; ++i )
      {  
        items.add( ( ( KeySpec )macroButtonModel.elementAt( i ) ) );
      }  
      return items;
    }
    else
    {
      short[] keyCodes = new short[ length ];
      for ( int i = 0; i < length; ++i )
      {  
        keyCodes[ i ] = ( ( Number )macroButtonModel.elementAt( i ) ).shortValue();
      }  
      return new Hex( keyCodes );
    }
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
    if ( config.getRemote().usesEZRC() )
    {
      KeySpec ks = ( KeySpec)macroButtons.getSelectedValue();
      if ( ks == null )
      {
        return;
      }
      deviceBox.setSelectedItem( ks.db );
      
      Button b = ks.fn == null ? ks.btn : ks.fn.getUsers().isEmpty() ? null : ks.fn.getUsers().get( 0 ).button;
      GeneralFunction gf = null;
      if ( b != null )
      {
        gf = ks.db.getUpgrade().getGeneralFunction( b.getKeyCode() );
      }
      else if ( ks.fn != null )
      {
        gf = ks.fn;
      }
      functionBox.getModel().setSelectedItem( null );
      functionBox.setSelectedItem( gf );
      delay.setValue( ks.delay / 10.0f );
      boolean showDuration = ks.duration >= 0;
      holdCheck.setSelected( showDuration );
      duration.setEnabled( showDuration );
      durationLabel.setEnabled( showDuration );
      duration.setValue( showDuration ? ks.duration/ 10.0f : 0f );
    }
  }
  
  @Override
  public void setValue( Object value )
  {
    macroButtonModel.clear();
    if ( value == null )
    {
      return;
    }
    if ( config.getRemote().usesEZRC() )
    {
      @SuppressWarnings( "unchecked" )
      List< KeySpec > list = ( List< KeySpec > )value;
      for ( KeySpec ks : list )
      {
        macroButtonModel.addElement( ks );
      }
    }
    else
    {
      Hex hex = ( Hex )value;
      availableButtons.setSelectedIndex( -1 );
      short[] data = hex.getData();
      for ( int i = 0; i < data.length; ++i )
        macroButtonModel.addElement( new Integer( data[ i ] ) );
      macroButtons.setSelectedIndex( -1 );
    }
  }

  /**
   * Enable buttons.
   */
  public void enableButtons()
  {
    Remote remote = config.getRemote();
    int selected = macroButtons.getSelectedIndex();
    moveUp.setEnabled( selected > 0 );
    moveDown.setEnabled( ( selected != -1 ) && ( selected < ( macroButtonModel.getSize() - 1 ) ) );
    remove.setEnabled( selected != -1 );
    clear.setEnabled( macroButtonModel.getSize() > 0 );
    if ( durationPanel.isVisible() )
    {
      List< Button > holdButtons = remote.getButtonGroups() != null ? 
          remote.getButtonGroups().get( "Hold" ) : null;
      duration.setEnabled( selected != -1 );
      durationLabel.setEnabled( selected != -1 );
      durationSuffix.setEnabled( selected != -1 );

      if ( selected >= 0 )
      {
        int val = ( ( Hex )getValue() ).getData()[ selected ];
        Button btn = remote.getButton( val & 0xFF );
        duration.setValue( new Float( ( ( val >> 8 ) & 0xFF ) / 10.0 ) );
        durationLabel.setText( holdButtons != null && holdButtons.contains( btn ) ? 
            "Hold next for:  " : "Pause after for:  " );
      }
      else
      {
        durationLabel.setText( "Duration:  " );
        duration.setText( null );
      }
    }

    Button baseButton = ( Button )availableButtons.getSelectedValue();
    buttonEnabler.enableButtons( baseButton, this );
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
  
  private JComboBox deviceBox = null;
  private JComboBox functionBox = null;
  
  private JPanel durationPanel = new JPanel( new BorderLayout() );
  
  private JPanel creationPanel = null;
  private JPanel itemPanel = null;
  
  private JLabel durationLabel = new JLabel( "Duration:  " );
  
  private JLabel durationSuffix = new JLabel( " secs" );
  
  private XFormattedTextField duration = null;
  private XFormattedTextField delay = null;
  private NumberFormatter formatter = null;
  private JCheckBox holdCheck = null;

  /** The config. */
  private RemoteConfiguration config = null;

  /** The available buttons. */
  private JList availableButtons = new JList();
  
  private DefaultListModel availableButtonModel = new DefaultListModel();
  
  /** The macro buttons. */
  private JList macroButtons = new JList();
  
  private JPanel panel = null;
  
  private ButtonEnabler buttonEnabler = null;
  
  /** The macro button model. */
  private DefaultListModel macroButtonModel = new DefaultListModel();
  
  /** The macro button renderer. */
  private MacroButtonRenderer macroButtonRenderer = new MacroButtonRenderer();

  @Override
  public void propertyChange( PropertyChangeEvent e )
  {
    Object source = e.getSource();
    if ( source == delay ) 
    {
      Float f = ( Float )delay.getValue();
      if ( f < 0.1 )
      {
        delay.setValue( 0.1f );
      }
    }
  }
}
