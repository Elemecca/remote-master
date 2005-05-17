package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class HexParmEditorPanel
  extends ProtocolEditorPanel
  implements ChangeListener, ActionListener, FocusListener, PropertyChangeListener, Runnable
{
  public HexParmEditorPanel( String title )
  {
    super( title );

    Box outerBox = Box.createVerticalBox();
    outerBox.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
    add( outerBox, BorderLayout.CENTER );
    JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEADING ));
    JLabel label = new JLabel( "Name:", SwingConstants.RIGHT );
    outerBox.add( panel );
    panel.add( label );
    name = new JTextField( 20 );
    name.setToolTipText( "Enter the name of the parameter." );
    panel.add( name );
    label.setLabelFor( name );

    Dimension d = panel.getMaximumSize();
    d.height = panel.getPreferredSize().height;
    panel.setMaximumSize( d );

    panel = new JPanel( new FlowLayout( FlowLayout.LEADING ));
    outerBox.add( panel );
//    panel.setBorder( BorderFactory.createTitledBorder( "Parameter Type" ));
    JLabel newLabel = new JLabel( "Parameter Type:" );
    panel.add( newLabel );
    Dimension labelSize = newLabel.getPreferredSize();
    label.setPreferredSize( labelSize );

    ButtonGroup group = new ButtonGroup();

    numberButton = createRadioButton( "Numeric", KeyEvent.VK_N, 
                                      "Select this type if you want the user to enter a numeric value for this parameter.",
                                      panel, group );

    choiceButton = createRadioButton( "List", KeyEvent.VK_L,
                                      "Select this type if you want the user to pick from a list of predefined choices.",
                                      panel, group );

    flagButton = createRadioButton( "Check box", KeyEvent.VK_C,
                                    "Select this type if you want the paremter presented as a check box.",
                                    panel, group );

    d = panel.getMaximumSize();
    d.height = panel.getPreferredSize().height;
    panel.setMaximumSize( d );

    buttons = new JRadioButton[ 3 ];
    buttons[ HexParmEditorNode.NUMBER ] = numberButton;
    buttons[ HexParmEditorNode.CHOICE ] = choiceButton;
    buttons[ HexParmEditorNode.FLAG ] = flagButton;

    for ( int i = 0; i < buttons.length; i++ )
      buttons[ i ].setActionCommand( Integer.toString( i ));

    card = new JPanel( new CardLayout());
    outerBox.add( card );

    panels = new JComponent[ 3 ];
    Box panelBox = Box.createVerticalBox();
    
    panels[ 0 ] = panelBox;
    JPanel boxPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ));
    panelBox.add( boxPanel );
    panels[ 1 ] = new JPanel();
    panels[ 1 ].add( new JLabel( "Panel for choice parameters" ));
    panels[ 2 ] = new JPanel();
    panels[ 2 ].add( new JLabel( "Panel for flag parameters" ));

    for ( int i = 0; i < panels.length; i++ )
      card.add( panels[ i ], Integer.toString( i ));
    
    outerBox.add( Box.createVerticalGlue());

    label = new JLabel( "Bits:", SwingConstants.RIGHT );    
    label.setPreferredSize( labelSize );
    boxPanel.add( label );
    bits = new JSpinner( new SpinnerNumberModel( 8, 0, 16, 1 ));
    boxPanel.add( bits );
    limitHeight( boxPanel );
    label.setLabelFor( bits );
    bits.addChangeListener( this );

    boxPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ));
    panelBox.add( boxPanel );
    label = new JLabel( "Format:", SwingConstants.RIGHT );
    boxPanel.add( label );
    label.setPreferredSize( labelSize );
    group = new ButtonGroup();
    decimal = new JRadioButton( "Decimal" );
    boxPanel.add( decimal );
    group.add( decimal );
    hex = new JRadioButton( "Hexadecimal" );
    boxPanel.add( hex );
    group.add( hex );
    limitHeight( boxPanel );

    boxPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ));
    panelBox.add( boxPanel );
                                   
    label = new JLabel( "Default value:", SwingConstants.RIGHT );
    boxPanel.add( label );
    label.setPreferredSize( labelSize );
    decimalFormatter = new IntegerFormatter( 8 );
    hexFormatter = new HexIntegerFormatter( 8 );    
    defaultValue = new JFormattedTextField();
    defaultValue.setPreferredSize( name.getPreferredSize());
    boxPanel.add( defaultValue );
    limitHeight( boxPanel );

    setText( "Enter the requested information about the parameter.\n\nEach parameter can have one or more translators associated with it." );

    addListeners();
  }

  private static void limitHeight( JComponent c )
  {
    Dimension d = c.getMaximumSize();
    d.height = c.getPreferredSize().height;
    c.setMaximumSize( d );
  }

  private JRadioButton createRadioButton( String name, int mnemonic, String tip, JPanel panel, ButtonGroup group )
  {
    JRadioButton button = new JRadioButton( name );
    button.setMnemonic( mnemonic );
    button.setToolTipText( tip );
    panel.add( button );
    group.add( button );
    return button;
  }

  public void update( ProtocolEditorNode newNode )
  {
    node = ( HexParmEditorNode )newNode;
    removeListeners();

    name.setText( node.getName());
    int type = node.getType();
    buttons[ type ].setSelected( true );
    (( CardLayout )card.getLayout()).show( card, Integer.toString( type ));
    if ( numberButton.isSelected())
    {
      int numBits = node.getBits();
      bits.setValue( new Integer( numBits ));
      decimalFormatter.setBits( numBits );
      hexFormatter.setBits( numBits );
      if ( node.getFormat() == HexParmEditorNode.DECIMAL )
      {
        decimal.setSelected( true );
        defaultValue.setFormatterFactory( new DefaultFormatterFactory( decimalFormatter ));
      }
      else
      {
        hex.setSelected( true );
        defaultValue.setFormatterFactory( new DefaultFormatterFactory( hexFormatter ));
      }
      int val = node.getDefaultValue();
      if ( val == -1 )
        defaultValue.setValue( null );
      else
        defaultValue.setValue( new Integer( val ));
    }

    addListeners();
    
//    bits.setValue( new Integer( node.getBits()));
  }

  private void removeListeners()
  {
    name.removeActionListener( this );
    name.removeFocusListener( this );
    for ( int i = 0; i < buttons.length; i++ )
      buttons[ i ].removeActionListener( this );
    bits.removeChangeListener( this );
    decimal.removeActionListener( this );
    hex.removeActionListener( this );
    defaultValue.removePropertyChangeListener( "value", this );
  }

  private void addListeners()
  {
    name.addActionListener( this );
    name.addFocusListener( this );
    for ( int i = 0; i < buttons.length; i++ )
      buttons[ i ].addActionListener( this );
    bits.addChangeListener( this );
    decimal.addActionListener( this );
    hex.addActionListener( this );
    defaultValue.addPropertyChangeListener( "value", this );
  }

  // ChangeListener methods
  public void stateChanged( ChangeEvent e )
  {
    Object source = e.getSource();
    if ( source == bits )
    {
      int numBits = (( Integer )bits.getValue()).intValue();
      System.err.println( "numBits=" + numBits );
      node.setBits( numBits );
      hexFormatter.setBits( numBits );
      decimalFormatter.setBits( numBits );

      int mask = ( 2 << ( numBits - 1 ))- 1;
      System.err.println( "Mask is " + mask );
      int val = node.getDefaultValue();
      if ( val > mask )
      {
        val &= mask;
        if ( decimal.isSelected())
          defaultValue.setValue( new Integer( val ));
        else
          defaultValue.setValue( new HexInteger( val ));
      }
      
    }
  }

  private void actionOrFocus( AWTEvent e )
  {
    Object source = e.getSource();
    if ( source == name )
    {
      String text = name.getText();
      if ( !text.equals( node.getName()))
        node.setName( text );
    }
  }

  // ActionListener methods
  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();
    String command = e.getActionCommand();
    if ( source == name )
      actionOrFocus( e );
    else if ( source == decimal  )
    {
      removeListeners();
      Object value = defaultValue.getValue();
      System.err.println( "got value " + value );
      defaultValue.setValue( null );
      defaultValue.setFormatterFactory( new DefaultFormatterFactory( decimalFormatter ));
      defaultValue.setValue( value );
      addListeners();
      node.setFormat( HexParmEditorNode.DECIMAL );
    }
    else if ( source == hex )
    {
      removeListeners();
      Object value = defaultValue.getValue();
      System.err.println( "got value " + value );
      defaultValue.setValue( null );
      defaultValue.setFormatterFactory( new DefaultFormatterFactory( hexFormatter ));
      defaultValue.setValue( value );
      addListeners();
      node.setFormat( HexParmEditorNode.HEXADECIMAL );
    }
    else if ( command != null )
    {
      int i = Integer.parseInt( command );
      node.setType( i );
      (( CardLayout )card.getLayout()).show( card, command );
    }
  }

  // FocusLlistener methods
  public void focusGained( FocusEvent e )
  {
    Object source = e.getSource();
    if ( source.getClass() == JFormattedTextField.class )
    {
      controlToSelectAll = ( JFormattedTextField )source;
      SwingUtilities.invokeLater( this );
    }
  }

  public void focusLost( FocusEvent e )
  {
    actionOrFocus( e );
  }

  //
  public void propertyChange( PropertyChangeEvent e )
  {
    Object source = e.getSource();
    String propertyName = e.getPropertyName();
    if ( !propertyName.equals( "value" ))
      return;
    System.err.println( "propertyChange( " + e.getPropertyName() + " )" );
    if ( source == defaultValue )
    {
      Number val = ( Number )e.getNewValue();
      System.err.println( "value=" + val );
      if ( val == null ) 
        node.setDefaultValue( -1 );
      else
        node.setDefaultValue( val.intValue());
    }
  }

  public void run()
  {
    controlToSelectAll.selectAll();
  }

  private HexParmEditorNode node = null;
  private JTextField name = null;
  private JRadioButton numberButton = null;
  private JRadioButton choiceButton = null;
  private JRadioButton flagButton = null;
  private JRadioButton[] buttons = null;
  private JPanel card = null;
  private JComponent[] panels = null;
  private JSpinner bits = null;
  private JRadioButton decimal = null;
  private JRadioButton hex = null;
  private JPanel ftfPanel = null;
  private JFormattedTextField defaultValue = null;
  private HexIntegerFormatter hexFormatter = null;
  private IntegerFormatter decimalFormatter = null;
  private JFormattedTextField controlToSelectAll = null;
}
