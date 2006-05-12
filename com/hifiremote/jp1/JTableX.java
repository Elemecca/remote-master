package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.util.Vector;

public class JTableX extends JTable
{
  protected CellEditorModel cellModel;

  public JTableX()
  {
    super();
    cellModel = null;
  }

  public JTableX( TableModel tm )
  {
    super( tm );
    cellModel = null;
  }

  public JTableX( TableModel tm, TableColumnModel cm )
  {
    super( tm,cm );
    cellModel = null;
  }

  public JTableX( TableModel tm, TableColumnModel cm,
    ListSelectionModel sm )
  {
    super( tm,cm,sm );
    cellModel = null;
  }

  public JTableX( int rows, int cols )
  {
    super( rows,cols );
    cellModel = null;
  }

  public JTableX( final Vector rowData, final Vector columnNames )
  {
    super( rowData, columnNames );
    cellModel = null;
  }

  public JTableX( final Object[][] rowData, final Object[] colNames )
  {
    super( rowData, colNames );
    cellModel = null;
  }

  // new constructor
  public JTableX( TableModel tm, CellEditorModel cellModel )
  {
    super( tm,null,null );
    this.cellModel = cellModel;
  }

  public void setCellEditorModel( CellEditorModel cellModel )
  {
    this.cellModel = cellModel;
  }

  public CellEditorModel getCellEditorModel()
  {
    return cellModel;
  }

  public TableCellEditor getCellEditor( int row, int col )
  {
    TableCellEditor tmpEditor = null;
    if ( cellModel != null )
      tmpEditor = cellModel.getCellEditor( row, col );
    if ( tmpEditor != null )
      return tmpEditor;
    return super.getCellEditor( row,col );
  }

  private boolean isTruncated( int row, int col )
  {
		Object o = getValueAt( row, col );
		if( o == null )
			return false;
    Rectangle rect = getCellRect( row, col, true );
    DefaultTableCellRenderer r = ( DefaultTableCellRenderer )getCellRenderer( row, col );
    r.getTableCellRendererComponent( this, o, false, false, row, col ); 
    Dimension d = r.getPreferredSize();
    if ( d.width < rect.width - 4 )
      return false;
    return true;
  }

  public String getToolTipText( MouseEvent e )
  {
    String tip = null;
    java.awt.Point p = e.getPoint();
    int row = rowAtPoint( p );
    int col = columnAtPoint( p );
    col = convertColumnIndexToModel( col );
    if ( isTruncated( row, col ))
    {
      DefaultTableCellRenderer r = ( DefaultTableCellRenderer )getCellRenderer( row, col );
      return r.getText();
    }
    return null;
  }

	public Point getToolTipLocation( MouseEvent event )
	{
		int row = rowAtPoint( event.getPoint() );
		int col = columnAtPoint( event.getPoint() );
    col = convertColumnIndexToModel( col );
    if ( isTruncated( row, col ))
    {
		  Point rc = getCellRect( row, col, true ).getLocation();
      rc.translate( -1, -1 );
      return rc;
    }
		return null;
	}

  public Component prepareRenderer( TableCellRenderer r, int row, int col )
  {
    JComponent c = ( JComponent )super.prepareRenderer( r, row, col );
    Border b = c.getBorder();
    if ( b != null )
      c.setBorder( pad );
    else
      c.setBorder( BorderFactory.createCompoundBorder( b, pad ));
    return c;
  }
  private static Border pad = BorderFactory.createEmptyBorder( 0, 3, 0, 3 );

  public void setColumnWidth( int col, String text, boolean setMax )
  {  
    setColumnWidth( col, text, setMax, 0 );
  }

  public void setColumnWidth( int col, String text, boolean setMax, int limit )  
  {
    JLabel l = ( JLabel )
      tableHeader.getDefaultRenderer().getTableCellRendererComponent( this, text, false, false, 0, col );
    int width = l.getPreferredSize().width + 2;
    TableColumn column = columnModel.getColumn( col );
    column.setMinWidth( width / 2 );
    column.setPreferredWidth( width );
    if ( setMax )
      column.setMaxWidth( width );
    else if ( limit != 0 )
      column.setMaxWidth( width * limit );
  }
}
