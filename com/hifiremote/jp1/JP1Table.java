package com.hifiremote.jp1;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

// TODO: Auto-generated Javadoc
/**
 * The Class JP1Table.
 */
public class JP1Table extends JTableX
{

  /**
   * Instantiates a new j p1 table.
   * 
   * @param model
   *          the model
   */
  public JP1Table( TableModel model )
  {
    super( model );
    setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    // getSelectionModel().addListSelectionListener( this );
    setCellSelectionEnabled( true );
    setSurrendersFocusOnKeystroke( true );
    setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN );
    tableHeader.setReorderingAllowed( false );
    DefaultCellEditor e = ( DefaultCellEditor )getDefaultEditor( String.class );
    new TextPopupMenu( ( JTextComponent )e.getComponent() );
  }

  public void initColumns()
  {
    JP1TableModel< ? > model = ( JP1TableModel< ? > )getModel();
    if ( model != null )
      initColumns( model );
  }

  /**
   * Inits the columns.
   * 
   * @param model
   *          the model
   */
  public void initColumns( JP1TableModel< ? > model )
  {
    TableColumnModel columnModel = getColumnModel();
    TableColumn column;

    int cols = columnModel.getColumnCount();
    for ( int i = 0; i < cols; i++ )
    {
      setColumnWidth( i, model.getColumnPrototypeName( i ), model.isColumnWidthFixed( i ), 0 );
      column = columnModel.getColumn( i );

      TableCellEditor editor = model.getColumnEditor( i );
      if ( editor != null )
        column.setCellEditor( editor );

      TableCellRenderer renderer = model.getColumnRenderer( i );
      if ( renderer != null )
        column.setCellRenderer( renderer );

      if ( model.getColumnName( i ).startsWith( "<html>" ) )
      {
        column.setHeaderRenderer( getTableHeader().getDefaultRenderer() );
      }
    }
    validate();
  }
}
