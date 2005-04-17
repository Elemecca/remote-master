package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class TranslatorEditorPanel
  extends ProtocolEditorPanel
  implements ActionListener, TableColumnModelListener, AdjustmentListener, ChangeListener 
{
  public TranslatorEditorPanel()
  {
    super( "Translator" );
    Box outerBox = Box.createVerticalBox();
    add( outerBox, BorderLayout.CENTER );
    Box box = Box.createVerticalBox();
    outerBox.add( box );
    Border border = BorderFactory.createTitledBorder( "Bit order" ); 
    box.setBorder( border );
    box.setAlignmentX( Component.LEFT_ALIGNMENT );
    msb = new JRadioButton( "MSB (Most Significant Bit first)" );
    msb.addActionListener( this );
    box.add( msb );
    box.add( Box.createVerticalStrut( 5 ));
    lsb = new JRadioButton( "LSB (Least Significant Bit first)" );
    lsb.addActionListener( this );
    box.add( lsb );
    ButtonGroup group = new ButtonGroup();
    group.add( msb );
    group.add( lsb );
    outerBox.add( Box.createVerticalStrut( 5 ));
    Box innerBox = box.createVerticalBox();
    outerBox.add( innerBox );
    Insets insets = border.getBorderInsets( box );
    innerBox.setBorder( BorderFactory.createEmptyBorder( 0, insets.left, 0, 0 )); 
    comp = new JCheckBox( "Complement" );
    comp.setToolTipText( "Select this if the parameter should be complemented during translation" );
    comp.setAlignmentX( Component.LEFT_ALIGNMENT );
    comp.addActionListener( this );
    innerBox.add( comp );
    innerBox.add( Box.createVerticalStrut( 10 ));
    JLabel label = new JLabel( "Parameter bits to be translated:" );
    label.setAlignmentX( Component.LEFT_ALIGNMENT );
    innerBox.add( label );
    parmTable = new JTable( new ParmDefaultTableModel( 8 ));
    (( DefaultTableCellRenderer )parmTable.getDefaultRenderer( Integer.class )).setHorizontalAlignment( SwingConstants.CENTER );
    parmTable.setCellSelectionEnabled( false );
    parmTable.setRowSelectionAllowed( false );
    parmTable.setColumnSelectionAllowed( true );
    parmTable.setAlignmentX( Component.LEFT_ALIGNMENT );
    parmTable.getColumnModel().addColumnModelListener( this );
    innerBox.add( parmTable );

    innerBox.add( box.createVerticalStrut( 10 ));

    innerBox.add( new JLabel( "Bits to receive the translated parameter bits:" ));
    dataBar = new MyScrollBar( JScrollBar.HORIZONTAL, 0, 8, 0, 24 );
    dataBar.addAdjustmentListener( this );
    dataBar.setAlignmentX( Component.LEFT_ALIGNMENT );
    dataBar.setAlignmentY( Component.TOP_ALIGNMENT );
    int h = dataBar.getMinimumSize().height;
    innerBox.add( dataBar );
    dataBox = box.createHorizontalBox();
    dataBox.add( box.createHorizontalStrut( h ));
            
    dataTable = new JTable( new DataDefaultTableModel( 24 ));
    (( DefaultTableCellRenderer )dataTable.getDefaultRenderer( Integer.class )).setHorizontalAlignment( SwingConstants.CENTER );
    dataTable.setCellSelectionEnabled( false );
    dataTable.setRowSelectionAllowed( false );
    dataTable.setColumnSelectionAllowed( true );
    
    dataTable.setAlignmentX( Component.LEFT_ALIGNMENT );
    dataTable.setAlignmentY( Component.TOP_ALIGNMENT );
    dataBox.add( dataTable );
    dataBox.add( box.createHorizontalStrut( h ));
    dataBox.setAlignmentX( Component.LEFT_ALIGNMENT );
    innerBox.add( dataBox );
    adjustSizes();

    innerBox.add( box.createVerticalStrut( 10 ));
    box = box.createHorizontalBox();
    box.setAlignmentX( Component.LEFT_ALIGNMENT );
    box.add( new JLabel( "Adjustment:" ));
    adjustment = new JSpinner( new SpinnerNumberModel( 0, -8, 8, 1 ));
    adjustment.setMaximumSize( adjustment.getPreferredSize());
    adjustment.setToolTipText( "This value is added to the parameter before translation occurs." );
//    (( JSpinner.NumberEditor )adjustment.getEditor()).getTextField().setToolTipText( adjustment.getToolTipText());
    box.add( box.createHorizontalStrut( 5 ));
    box.add( adjustment );
    adjustment.addChangeListener( this );
    box.setMaximumSize( box.getPreferredSize());
    innerBox.add( box );

    setText( "Translators are used to store the value a user enters for a parameter in the appropriate bits of the protocol data." );    
  }

  private void adjustSizes()
  {
    int h = dataBar.getMinimumSize().height;
    JLabel label = new JLabel( "111" );
    int w = label.getPreferredSize().width;
            
    TableColumnModel model = parmTable.getColumnModel();
    for ( int i = 0; i < parmTable.getColumnCount(); i++ )
    {
      TableColumn col = model.getColumn( i );
      col.setPreferredWidth( w );
      col.setMaxWidth( w );
      col.setMinWidth( w );
    }

    Dimension d = parmTable.getPreferredSize();
    parmTable.setMaximumSize( d );
    parmTable.setMinimumSize( d );

    model = dataTable.getColumnModel();
    for ( int i = 0; i < dataTable.getColumnCount(); i++ )
    {
      TableColumn col = model.getColumn( i );
      col.setPreferredWidth( w );
      col.setMaxWidth( w );
      col.setMinWidth( w );
    }
        
    d = dataTable.getPreferredSize();
    dataTable.setMaximumSize( d );
    dataTable.setMinimumSize( d );
    d.width += h;
    d.width += h;
    dataBox.setMaximumSize( d );
    dataBox.setMinimumSize( d );
    Dimension s = dataBar.getPreferredSize();
    s.width = d.width;
    dataBar.setMaximumSize( s );
    dataBar.setMinimumSize( s );
  }

  public void update( ProtocolEditorNode aNode )
  {
    node = ( TranslatorEditorNode )aNode;
    if ( node.getBitOrder() == TranslatorEditorNode.MSB )
      msb.setSelected( true );
    else
      lsb.setSelected( true );
    comp.setSelected( node.getComp());
    HexParmEditorNode parm = ( HexParmEditorNode )node.getParent();
    int parmBits = parm.getBits();
    parmTable.getColumnModel().removeColumnModelListener( this );
    parmTable.clearSelection();
    (( ParmDefaultTableModel )parmTable.getModel()).setCols( parmBits );
    HexEditorNode hex = ( HexEditorNode )parm.getParent();
    int hexBits = hex.getHex().length() * 8;
    (( DataDefaultTableModel )dataTable.getModel()).setCols( hexBits );
    int lastCol =  parmBits - node.getLSBOffset() - 1;
    int firstCol = lastCol - node.getBits() + 1;
    parmTable.addColumnSelectionInterval( firstCol, lastCol );
    parmTable.getColumnModel().addColumnModelListener( this );
    dataBar.removeAdjustmentListener( this );
    dataBar.setValues( node.getMSBOffset(), node.getBits(), 0, hexBits );
    dataBar.addAdjustmentListener( this );
    adjustSizes();
    adjustment.setValue( new Integer( node.getAdjust()));
    validate();
  }

  // ActionListener
  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();
    if ( source == msb ) 
      node.setBitOrder( TranslatorEditorNode.MSB );
    else if ( source == lsb )
      node.setBitOrder( TranslatorEditorNode.LSB );
    else if ( source == comp )
      node.setComp( comp.isSelected());
  }

  // TabelColumnModelListener
  public void columnAdded( TableColumnModelEvent e ){}

  public void columnRemoved( TableColumnModelEvent e ){}

  public void columnMoved( TableColumnModelEvent e ){}

  public void columnMarginChanged( ChangeEvent e ){}

  public void columnSelectionChanged( ListSelectionEvent e )
  {
    if ( e.getValueIsAdjusting())
      return;
    int[] selection = parmTable.getSelectedColumns();
    int bits = selection.length;
    int firstCol = 100; 
    int lastCol = 0;
    for ( int i = 0; i < bits; i++ )
    {
      int col = selection[ i ];
      if ( col < firstCol )
        firstCol = col;
      if ( col > lastCol )
        lastCol = col;
    }
    node.setBits( bits );
    node.setLSBOffset( parmTable.getColumnCount() - lastCol - 1 );
//    dataBar.removeAdjustmentListener( this );
    HexParmEditorNode parm = ( HexParmEditorNode )node.getParent();
    HexEditorNode hex = ( HexEditorNode )parm.getParent();
    int hexBits = hex.getHex().length() * 8;
    if (( dataBar.getValue() + bits ) > hexBits )
      dataBar.setValue( hexBits - bits );
    dataBar.setVisibleAmount( bits );
    dataBar.setBlockIncrement( bits );
//    dataBar.addAdjustmentListener( this );
    dataBar.validate();
  }

  // AdjustmentListener
  public void adjustmentValueChanged( AdjustmentEvent e )
  {
    int value = e.getValue();
    node.setMSBOffset( value );
    dataTable.clearSelection();
    dataTable.addColumnSelectionInterval( value, value + node.getBits() - 1 );
  }

  // ChangeListener
  public void stateChanged( ChangeEvent e )
  {
    Object source = e.getSource();
    if ( source ==  adjustment )
    {
      if ( node != null )
        node.setAdjust((( Integer )adjustment.getValue()).intValue());
    }
  }
 
  private class ParmDefaultTableModel
    extends AbstractTableModel
  {
    public ParmDefaultTableModel( int cols ){ this.cols = cols; }
    public void setCols( int cols )
    {
      this.cols = cols;
      fireTableStructureChanged();
    }
    public int getRowCount(){ return 1; }
    public int getColumnCount(){ return cols; }
    public Class getColumnClass( int col ){ return Integer.class; }
    public Object getValueAt( int row, int col )
    {
      return new Integer( cols - col - 1 );
    }
    
    private int cols;
  }

  private class DataDefaultTableModel
    extends ParmDefaultTableModel
  {
    public DataDefaultTableModel( int cols )
    {
      super( cols );
    }

    public Object getValueAt( int row, int col )
    {
      return new Integer( col );
    }
  }

  private class MyScrollBar
    extends JScrollBar
  {
    public MyScrollBar( int orientation, int value, int extent, int min, int max )
    {
      super( orientation, value, extent, min, max );
    }
    
    public Dimension getMaximumSize()
    {
      if ( max != null )
        return max;
      return super.getMaximumSize();
    }

    public Dimension getMinimumSize()
    {
      if ( min != null )
        return min;
      return super.getMinimumSize();
    }

    public void setMaximumSize( Dimension size )
    {
      max = size;
    }

    public void setMinimumSize( Dimension size )
    {
      min = size;
    }

    public void setSize( int width, int height )
    {
      if ( height < min.height )
        height = min.height;
      if ( width < min.width )
        width = min.width;
      if ( height > max.height )
        height = max.height;
      if ( width > max.width )
        width = max.width;
      super.setSize( width, height );
    }

    public void setBounds( int x, int y, int width, int height )
    {
      if ( height < min.height )
        height = min.height;
      if ( width < min.width )
        width = min.width;
      if ( height > max.height )
        height = max.height;
      if ( width > max.width )
        width = max.width;
      super.setBounds( x, y, width, height );
    }
    
    private Dimension max = null;
    private Dimension min = null;
  }

  private TranslatorEditorNode node = null;
  private JRadioButton msb = null;
  private JRadioButton lsb = null;
  private JCheckBox comp = null;
  private JTable parmTable = null;
  private Box dataBox = null;
  private JTable dataTable = null;
  private MyScrollBar dataBar = null;
  private JSpinner adjustment = null;
}
