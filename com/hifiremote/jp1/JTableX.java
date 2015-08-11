package com.hifiremote.jp1;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

// TODO: Auto-generated Javadoc
/**
 * The Class JTableX.
 */
public class JTableX extends JTable
{

  /** The cell model. */
  protected CellEditorModel cellModel;
  
  public static class ToolTipAdapter extends MouseAdapter
  {
    final int defaultDismissTimeout = ToolTipManager.sharedInstance().getDismissDelay();
    int defaultInitialDelay = 0;
    final int newDismissTimeout = 300000;
    JTableX table = null;

    public ToolTipAdapter( JTableX table )
    {
      super();
      this.table = table;
    }

    @Override
    public void mouseEntered( MouseEvent me ) 
    {
      ToolTipManager tm = ToolTipManager.sharedInstance();
      defaultInitialDelay = tm.getInitialDelay();
      tm.setDismissDelay( newDismissTimeout );
      tm.setInitialDelay( 0 );
    }

    @Override
    public void mouseExited( MouseEvent me ) 
    {
      // If mouse exits below last table row, tooltip does not always get hidden.
      // This code explicitly hides it when table is exited, by reactivating it
      // with a time-out of zero.
      Point p = me.getPoint();
      ToolTipManager tm = ToolTipManager.sharedInstance();
      Rectangle visRect = table.getVisibleRect();
      if ( !visRect.contains( p ) )
      {
        int delta = 0;
        Point pdelta = p;
        while ( delta < 50 )
        {
          pdelta = new Point( p.x, p.y - delta );
          if ( table.rowAtPoint( pdelta ) >= 0 )
          {
            p = pdelta;
            break;
          }
          pdelta = new Point( p.x, p.y + delta );
          if ( table.rowAtPoint( pdelta ) >= 0 )
          {
            p = pdelta;
            break;
          }
          delta += 1;
        }
        if ( delta < 50 && table.columnAtPoint( p ) >= 0 )
        {
          MouseEvent mev = new MouseEvent( table, MouseEvent.MOUSE_CLICKED,
              System.currentTimeMillis(), 0, p.x, p.y, 1, false );
          if ( table.getToolTipText( mev ) != null )
          {
            tm.setDismissDelay( 0 );
            tm.mouseMoved( mev );
          }
        }
      }
      else
      {
        int row = table.rowAtPoint( p );
        int col = table.columnAtPoint( p );
        MouseEvent mev = new MouseEvent( table, MouseEvent.MOUSE_CLICKED,
            System.currentTimeMillis(), 0, p.x, p.y, 1, false );
        int tipHeight = table.getToolTipHeight( mev );
        Rectangle cellRect = table.getCellRect( row, col, true );
        Point bottom = new Point ( p.x, visRect.y + visRect.height );
        if ( table.rowAtPoint( bottom ) >= 0 && tipHeight > 0 
            && cellRect.y + tipHeight + 5 > visRect.y + visRect.height )
        {
          // Tooltip overlaps bottom border of table 
          tm.setDismissDelay( defaultDismissTimeout );
          tm.mouseMoved( mev );
        }
      }
      tm.setDismissDelay( defaultDismissTimeout );
      tm.setInitialDelay( defaultInitialDelay );
    }
  }

  /**
   * Instantiates a new j table x.
   */
  public JTableX()
  {
    super();
    cellModel = null;
  }

  /**
   * Instantiates a new j table x.
   * 
   * @param tm
   *          the tm
   */
  public JTableX( TableModel tm )
  {
    super( tm );
    cellModel = null;
  }

  /**
   * Instantiates a new j table x.
   * 
   * @param tm
   *          the tm
   * @param cm
   *          the cm
   */
  public JTableX( TableModel tm, TableColumnModel cm )
  {
    super( tm, cm );
    cellModel = null;
  }

  /**
   * Instantiates a new j table x.
   * 
   * @param tm
   *          the tm
   * @param cm
   *          the cm
   * @param sm
   *          the sm
   */
  public JTableX( TableModel tm, TableColumnModel cm, ListSelectionModel sm )
  {
    super( tm, cm, sm );
    cellModel = null;
  }

  /**
   * Instantiates a new j table x.
   * 
   * @param rows
   *          the rows
   * @param cols
   *          the cols
   */
  public JTableX( int rows, int cols )
  {
    super( rows, cols );
    cellModel = null;
  }

  /**
   * Instantiates a new j table x.
   * 
   * @param rowData
   *          the row data
   * @param columnNames
   *          the column names
   */
  public JTableX( @SuppressWarnings( "rawtypes" ) final Vector rowData, final Vector< String > columnNames )
  {
    super( rowData, columnNames );
    cellModel = null;
  }

  /**
   * Instantiates a new j table x.
   * 
   * @param rowData
   *          the row data
   * @param colNames
   *          the col names
   */
  public JTableX( final Object[][] rowData, final Object[] colNames )
  {
    super( rowData, colNames );
    cellModel = null;
  }

  // new constructor
  /**
   * Instantiates a new j table x.
   * 
   * @param tm
   *          the tm
   * @param cellModel
   *          the cell model
   */
  public JTableX( TableModel tm, CellEditorModel cellModel )
  {
    super( tm, null, null );
    this.cellModel = cellModel;
  }

  /**
   * Sets the cell editor model.
   * 
   * @param cellModel
   *          the new cell editor model
   */
  public void setCellEditorModel( CellEditorModel cellModel )
  {
    this.cellModel = cellModel;
  }

  /**
   * Gets the cell editor model.
   * 
   * @return the cell editor model
   */
  public CellEditorModel getCellEditorModel()
  {
    return cellModel;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JTable#getCellEditor(int, int)
   */
  public TableCellEditor getCellEditor( int row, int col )
  {
    TableCellEditor tmpEditor = null;
    if ( cellModel != null )
      tmpEditor = cellModel.getCellEditor( row, col );
    if ( tmpEditor != null )
      return tmpEditor;
    return super.getCellEditor( row, col );
  }

  /**
   * Checks if is truncated.
   * 
   * @param row
   *          the row
   * @param col
   *          the col
   * @return true, if is truncated
   */
  private boolean isTruncated( int row, int col )
  {
    Object o = getValueAt( row, col );
    if ( o == null )
      return false;
    Rectangle rect = getCellRect( row, col, true );
    TableCellRenderer r = ( DefaultTableCellRenderer )getCellRenderer( row, col );
    Component c = r.getTableCellRendererComponent( this, o, false, false, row, col );
    Dimension d = c.getPreferredSize();
    if ( d.width < rect.width - 4 )
      return false;
    return true;
  }

  @Override
  public String getToolTipText( MouseEvent e )
  {
    java.awt.Point p = e.getPoint();
    int row = rowAtPoint( p );
    int col = columnAtPoint( p );
    col = convertColumnIndexToModel( col );
    TableModel tm = null;
    TableSorter sorter = null;
    // Check for tooltip overwrite by table model of an RMTablePanel
    if ( dataModel != null && dataModel instanceof TableSorter
        && ( tm = ( ( sorter = ( TableSorter )dataModel ) ).getTableModel() ) != null
        && tm instanceof JP1TableModel< ? > )
    {
      JP1TableModel< ? > model = ( JP1TableModel< ? > )tm;
      String rc = model.getToolTipText( sorter.modelIndex( row ), col );
      if ( rc != null )
      {
        return rc;
      }
    }
    else if ( dataModel != null && dataModel instanceof JP1TableModel< ? > )
    {
      JP1TableModel< ? > model = ( JP1TableModel< ? > )dataModel;
      String rc = model.getToolTipText( row, col );
      if ( rc != null )
      {
        return rc;
      }
    }
    if ( isTruncated( row, col ) )
    {
      DefaultTableCellRenderer r = ( DefaultTableCellRenderer )getCellRenderer( row, col );
      // When getting column width, subtract the width of the static "pad" empty border, namely 6 
      int width = getColumnModel().getColumn( col ).getWidth() - 6;
//      String rc = "<html><div style=\"width: " + width + "px\">" + r.getText() + "</div></html>";
      String rc = "<html><p width=\"" + width + "px\">" + r.getText() + "</p></html>";
      JLabel lbl = new JLabel( rc );
      int jWidth = lbl.getPreferredSize().width;
      // Scale width from units used by Java to units used by html
      width = ( width * width ) / jWidth - 1;
      rc = "<html><p width=\"" + width + "px\">" + r.getText() + "</p></html>";
      return rc;
    }
    return null;
  }
  
  public int getToolTipHeight( MouseEvent e )
  {
    String text = getToolTipText( e );
    if ( text != null )
    {
      Font font = null;
      try
      {
        font = ( Font )UIManager.get( "ToolTip.font" );
      }
      catch ( Exception ex ) {};
      JLabel lbl = new JLabel( text );
      if ( font != null )
      {
        lbl.setFont( font );
      }
      return lbl.getPreferredSize().height;
    }
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#getToolTipLocation(java.awt.event.MouseEvent)
   */
  public Point getToolTipLocation( MouseEvent event )
  {
    int row = rowAtPoint( event.getPoint() );
    int col = columnAtPoint( event.getPoint() );
    col = convertColumnIndexToModel( col );
//    if ( isTruncated( row, col ) )
//    {
      Point rc = getCellRect( row, col, true ).getLocation();
      rc.translate( -1, -1 );
      return rc;
//    }
//    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JTable#prepareRenderer(javax.swing.table.TableCellRenderer, int, int)
   */
  public Component prepareRenderer( TableCellRenderer r, int row, int col )
  {
    JComponent c = ( JComponent )super.prepareRenderer( r, row, col );
    Border b = c.getBorder();
    if ( b != null )
      c.setBorder( pad );
    else
      c.setBorder( BorderFactory.createCompoundBorder( b, pad ) );
    return c;
  }

  /** The pad. */
  private static Border pad = BorderFactory.createEmptyBorder( 0, 3, 0, 3 );

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JTable#setValueAt(java.lang.Object, int, int)
   */
  public void setValueAt( Object value, int row, int col )
  {
    JP1Frame.clearMessage( this );
    try
    {
      super.setValueAt( value, row, col );
    }
    catch ( Exception except )
    {
      JP1Frame.showMessage( except.getMessage(), this );
      except.printStackTrace( System.err );
      changeSelection( row, col, false, false );
    }
  }

  /**
   * Sets the column width.
   * 
   * @param col
   *          the col
   * @param text
   *          the text
   * @param setMax
   *          the set max
   */
  public void setColumnWidth( int col, String text, boolean setMax )
  {
    setColumnWidth( col, text, setMax, 0 );
  }

  /**
   * Sets the column width.
   * 
   * @param col
   *          the col
   * @param text
   *          the text
   * @param setMax
   *          the set max
   * @param limit
   *          the limit
   */
  public void setColumnWidth( int col, String text, boolean setMax, int limit )
  {
    JLabel l = ( JLabel )tableHeader.getDefaultRenderer().getTableCellRendererComponent( this, text, false, false, 0,
        col );
    int width = l.getPreferredSize().width + 4;
    TableColumn column = columnModel.getColumn( col );
    column.setMinWidth( width / 2 );
    column.setPreferredWidth( width );
    if ( setMax )
      column.setMaxWidth( ( width * 3 ) / 2 );
    else if ( limit != 0 )
      column.setMaxWidth( width * limit );
  }
  
  public void setLongToolTipTimeout()
  {
    addMouseListener( new ToolTipAdapter( this ) );
  }
}
