package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class TranslatorEditorPanel
  extends ProtocolEditorPanel
  implements ActionListener, TableColumnModelListener, AdjustmentListener 
{
  public TranslatorEditorPanel()
  {
    super( "Translator" );
    setLayout( new BoxLayout( this, BoxLayout.PAGE_AXIS ));
    Box box = Box.createVerticalBox();
    add( box );
    box.setBorder( BorderFactory.createTitledBorder( "Bit order" ));
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
    add( Box.createVerticalStrut( 5 ));
    comp = new JCheckBox( "Complement" );
    comp.setAlignmentX( Component.LEFT_ALIGNMENT );
    comp.addActionListener( this );
    add( comp );
    add( Box.createVerticalStrut( 10 ));
    JLabel label = new JLabel( "Device parameter bits to be translated:" );
    label.setAlignmentX( Component.LEFT_ALIGNMENT );
    add( label );
    parmTable = new JTable( new ParmDefaultTableModel( 8 ));
    (( DefaultTableCellRenderer )parmTable.getDefaultRenderer( Integer.class )).setHorizontalAlignment( SwingConstants.CENTER );
    parmTable.setCellSelectionEnabled( false );
    parmTable.setRowSelectionAllowed( false );
    parmTable.setColumnSelectionAllowed( true );
    parmTable.setAlignmentX( Component.LEFT_ALIGNMENT );
    parmTable.getColumnModel().addColumnModelListener( this );
    add( parmTable );

    add( box.createVerticalStrut( 10 ));

    add( new JLabel( "Select which bits in the fixed data will store the translated bits." ));
    dataBar = new MyScrollBar( JScrollBar.HORIZONTAL, 0, 8, 0, 24 );
    dataBar.addAdjustmentListener( this );
    dataBar.setAlignmentX( Component.LEFT_ALIGNMENT );
    dataBar.setAlignmentY( Component.TOP_ALIGNMENT );
    int h = dataBar.getMinimumSize().height;
    add( dataBar );
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
    add( dataBox );
    adjustSizes();
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
    dataBox.setPreferredSize( d );
    dataBox.setMaximumSize( d );
    dataBox.setMinimumSize( d );
    dataBar.setPreferredSize( d );
    dataBar.setMaximumSize( d );
    dataBar.setMinimumSize( d );
  }

  public void update( ProtocolEditorNode aNode )
  {
    node = ( TranslatorEditorNode )aNode;
    if ( node.getBitOrder() == TranslatorEditorNode.MSB )
      msb.setSelected( true );
    else
      lsb.setSelected( true );
    comp.setSelected( node.getComp());
    DevParmEditorNode devParm = ( DevParmEditorNode )node.getParent();
    int parmBits = devParm.getBits();
    parmTable.getColumnModel().removeColumnModelListener( this );
    parmTable.clearSelection();
    (( ParmDefaultTableModel )parmTable.getModel()).setCols( parmBits );
    FixedDataEditorNode fixedData = ( FixedDataEditorNode )devParm.getParent();
    int fixedBits = fixedData.getFixedData().length() * 8;
    (( DataDefaultTableModel )dataTable.getModel()).setCols( fixedBits );
    int lastCol =  parmBits - node.getLSBOffset() - 1;
    int firstCol = lastCol - node.getBits() + 1;
    System.err.println( "Attempting to select cols " + firstCol + "-" + lastCol );
    parmTable.addColumnSelectionInterval( firstCol, lastCol );
    parmTable.getColumnModel().addColumnModelListener( this );
    dataBar.removeAdjustmentListener( this );
    dataBar.setValues( node.getMSBOffset(), node.getBits(), 0, fixedBits );
    dataBar.addAdjustmentListener( this );
    adjustSizes();
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
    System.err.println( "valueChanged!" );
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
    System.err.println( "lastCol=" + lastCol + " and firstCol=" + firstCol + " and bits=" + bits );
    node.setBits( bits );
    node.setLSBOffset( parmTable.getColumnCount() - lastCol - 1 );
//    dataBar.removeAdjustmentListener( this );
    dataBar.setVisibleAmount( bits );
    dataBar.setBlockIncrement( bits );
//    dataBar.addAdjustmentListener( this );
    dataBar.validate();
  }

  // AdjustmentListener
  public void adjustmentValueChanged( AdjustmentEvent e )
  {
    int value = e.getValue();
    System.err.println( "Value is " + value + ", selecting cols " + value + "-" + ( value + node.getBits() - 1 ));
    node.setMSBOffset( value );
    dataTable.clearSelection();
    System.err.println( "dataTable columns:" + dataTable.getColumnCount() );
    dataTable.addColumnSelectionInterval( value, value + node.getBits() - 1 );
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

    public void setPreferredSize( Dimension size )
    {
      preferredSize = size;
    }
      
    public Dimension getPreferredSize()
    {
      if ( preferredSize == null )
        return super.getPreferredSize();
      return preferredSize;
    }

    public void setMaximumSize( Dimension size )
    {
      maximumSize = size;
    }
      
    public Dimension getMaximumSize()
    {
      if ( maximumSize == null )
        return super.getMaximumSize();
      return maximumSize;
    }

    public void seMinimumSize( Dimension size )
    {
      minimumSize = size;
    }

    public Dimension getMinimumSize()
    {
      if ( minimumSize == null )
        return super.getMinimumSize();
      return minimumSize;
    }

    private Dimension preferredSize = null;
    private Dimension minimumSize = null;
    private Dimension maximumSize = null;
  }

  private TranslatorEditorNode node = null;
  private JRadioButton msb = null;
  private JRadioButton lsb = null;
  private JCheckBox comp = null;
  private JSpinner bits = null;
  private JTable parmTable = null;
  private Box dataBox = null;
  private JTable dataTable = null;
  private MyScrollBar dataBar = null;
}
