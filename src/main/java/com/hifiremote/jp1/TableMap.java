/** 
 * In a chain of data manipulators some behaviour is common. TableMap
 * provides most of this behavour and can be subclassed by filters
 * that only need to override a handful of specific methods. TableMap 
 * implements TableModel by routing all requests to its model, and
 * TableModelListener by routing all events to its listeners. Inserting 
 * a TableMap which has not been subclassed into a chain of table filters 
 * should have no effect.
 *
 * @version 1.4 12/17/97
 * @author Philip Milne */

package com.hifiremote.jp1;

import javax.swing.table.*; 
import javax.swing.event.TableModelListener; 
import javax.swing.event.TableModelEvent; 

// TODO: Auto-generated Javadoc
/**
 * The Class TableMap.
 */
public class TableMap< E > extends JP1TableModel< E > 
                      implements TableModelListener {
    
    /** The model. */
    protected JP1TableModel< E > model; 

    /**
     * Gets the model.
     * 
     * @return the model
     */
    public TableModel getModel() {
        return model;
    }

    /**
     * Sets the model.
     * 
     * @param model the new model
     */
    public void setModel( JP1TableModel< E > model ) {
        this.model = model; 
        model.addTableModelListener( this ); 
    }

    // By default, implement TableModel by forwarding all messages 
    // to the model. 

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int aRow, int aColumn) {
        return model.getValueAt(aRow, aColumn); 
    }
        
    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
    public void setValueAt(Object aValue, int aRow, int aColumn) {
        model.setValueAt(aValue, aRow, aColumn); 
    }

    /* (non-Javadoc)
     * @see com.hifiremote.jp1.JP1TableModel#getRowCount()
     */
    public int getRowCount() {
        return (model == null) ? 0 : model.getRowCount(); 
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return (model == null) ? 0 : model.getColumnCount(); 
    }
        
    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    public String getColumnName(int aColumn) 
    {
        return model.getColumnName(aColumn); 
    }
    
    /* (non-Javadoc)
     * @see com.hifiremote.jp1.JP1TableModel#getColumnPrototypeName(int)
     */
    public String getColumnPrototypeName( int col )
    {
      return model.getColumnPrototypeName( col );
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    public Class<?> getColumnClass(int aColumn) {
        return model.getColumnClass(aColumn); 
    }
        
    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    public boolean isCellEditable(int row, int column) { 
         return model.isCellEditable(row, column); 
    }
    
    /* (non-Javadoc)
     * @see com.hifiremote.jp1.JP1TableModel#isColumnWidthFixed(int)
     */
    public boolean isColumnWidthFixed( int col )
    {
      return model.isColumnWidthFixed( col );
    }

    /* (non-Javadoc)
     * @see com.hifiremote.jp1.JP1TableModel#getColumnRenderer(int)
     */
    public TableCellRenderer getColumnRenderer( int col )
    {
      return model.getColumnRenderer( col );
    }
    
    /* (non-Javadoc)
     * @see com.hifiremote.jp1.JP1TableModel#getColumnEditor(int)
     */
    public TableCellEditor getColumnEditor( int col )
    {
      return model.getColumnEditor( col );
    }

//
// Implementation of the TableModelListener interface, 
//
    // By default forward all events to all the listeners. 
    /* (non-Javadoc)
 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
 */
public void tableChanged(TableModelEvent e) {
        fireTableChanged(e);
    }

    /* (non-Javadoc)
     * @see com.hifiremote.jp1.JP1TableModel#addRow(java.lang.Object)
     */
    public void addRow( E object )
    {
      model.addRow( object );
    }

    /* (non-Javadoc)
     * @see com.hifiremote.jp1.JP1TableModel#insertRow(int, java.lang.Object)
     */
    public void insertRow( int row, E object )
    {
      model.insertRow( row, object );
    }

    /* (non-Javadoc)
     * @see com.hifiremote.jp1.JP1TableModel#removeRow(int)
     */
    public void removeRow( int row )
    {
      model.removeRow( row );
    }

    /* (non-Javadoc)
     * @see com.hifiremote.jp1.JP1TableModel#getRow(int)
     */
    public E getRow( int row )
    {
      return model.getRow( row );
    }
    
    /* (non-Javadoc)
     * @see com.hifiremote.jp1.JP1TableModel#setRow(int, java.lang.Object)
     */
    public void setRow( int row, E value )
    {
      model.setRow( row, value );
    }

    /* (non-Javadoc)
     * @see com.hifiremote.jp1.JP1TableModel#moveRow(int, int)
     */
    public void moveRow( int from, int to )
    {
      model.moveRow( from, to );
    }
}
