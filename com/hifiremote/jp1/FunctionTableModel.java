package com.hifiremote.jp1;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.Vector;

public class FunctionTableModel
  extends AbstractTableModel
{
  private Vector functions = null;
  private Protocol protocol = null;
  private final static int nameCol = 0;
  private final static int efcCol = 1;
  private final static int colOffset = 2;
  private int hexCol = 2;
  private int notesCol = 3;

  public FunctionTableModel( Vector functions )
  {
    this.functions = functions;
  }

  public void functionsUpdated()
  {
    fireTableDataChanged();
  }

  public void setFunctions( Vector functions )
  {
    this.functions = functions;
    fireTableDataChanged();
  }

  public void setProtocol( Protocol protocol )
  {
    this.protocol = protocol;
    hexCol = protocol.getColumnCount() + colOffset;
    notesCol = hexCol + 1;
    fireTableStructureChanged();
  }

  public int getRowCount()
  {
    int rc = 0;
    if ( functions != null )
      rc = functions.size();

    return rc;
  }

  public int getColumnCount()
  {
    int rc = 4;
    if ( protocol != null )
      rc += protocol.getColumnCount() ;
    return rc;
  }

  public Object getValueAt( int row, int col )
  {
    Function function = ( Function )functions.elementAt( row );
    byte[] hex = function.getHex();

    Object rc = "";
    if ( col == nameCol )
      rc = function.getName();
    else if ( col == efcCol )
    {
      if ( hex != null )
        rc = new Integer( protocol.hex2efc( hex ));
      else
        rc = hex;
    }
    else if ( col == notesCol )
      rc = function.getNotes();
    else if ( col == hexCol )
      rc = hex;
    else
    {
      if ( hex == null )
        rc = null;
      else
        rc = protocol.getValueAt( col - colOffset, hex );
    }

    return rc;
  }

  public void checkFunctionAssigned( Function f, Object value )
    throws IllegalArgumentException
  {
    if (( value == null ) && f.assigned() )
    {
      String msg = "Function " + f.getName() + " is assigned to a button, and must not be cleared!";

      KeyMapMaster.showMessage( msg );
      throw new IllegalArgumentException( msg );
    }
  }

  public void setValueAt( Object value, int row, int col )
  {
    Function function = ( Function )functions.elementAt( row );
    if ( col == nameCol )
      function.setName(( String )value );
    else if ( col == efcCol )
    {
      checkFunctionAssigned( function, value );
      if ( value == null )
        function.setHex( null );
      else
      {
        byte[] hex = function.getHex();
        hex = protocol.efc2hex((( Integer )value ).byteValue(), hex );
        function.setHex( hex );
      }
    }
    else if ( col == hexCol )
    {
      checkFunctionAssigned( function, value );

      function.setHex(( byte[] )value );
    }
    else if ( col == notesCol )
      function.setNotes(( String )value );
    else
    {
      checkFunctionAssigned( function, value );
      if ( value == null )
        function.setHex( null );
      else
      {
        byte[] hex = function.getHex();
        if ( hex == null )
        {
          hex = protocol.getDefaultCmd();
          function.setHex( hex );
        }
        protocol.setValueAt( col - colOffset, hex, value );
      }
    }
    fireTableRowsUpdated( row, row );
  }

  public String getColumnName( int col )
  {
    String rc = null;
    if ( col == nameCol )
      rc = "Name";
    else if ( col == efcCol )
      rc = "EFC";
    else if ( col == hexCol )
      rc = "Hex";
    else if ( col == notesCol )
      rc = "Notes";
    else
      rc = protocol.getColumnName( col - colOffset );
    return rc;
  }

  public Class getColumnClass( int col )
  {
    Class rc = null;
    if (( col == nameCol ) || ( col == notesCol ))
      rc = String.class;
    else if ( col == efcCol )
      rc = Integer.class;
    else if ( col == hexCol )
      rc = byte[].class;
    else
      rc = protocol.getColumnClass( col - colOffset );

    return rc;
  }

  public boolean isCellEditable( int row, int col )
  {
    boolean rc = false;
    if (( col == nameCol ) || ( col == efcCol ) || ( col == hexCol ) ||
        ( col == notesCol ))
      rc = true;
    else
      rc = protocol.isEditable( col - colOffset );

    return rc;
  }

  public TableCellEditor getColumnEditor( int col )
  {
    TableCellEditor rc = null;
    if (( col == nameCol ) || ( col == notesCol ))
      rc = null;
    else if ( col == efcCol )
      rc = new ByteEditor();
    else if ( col == hexCol )
      rc = new HexEditor( protocol.getDefaultCmd());
    else
      rc = protocol.getColumnEditor( col - colOffset );
    return rc;
  }

  public TableCellRenderer getColumnRenderer( int col )
  {
    TableCellRenderer rc = null;
    if (( col == nameCol ) || ( col == notesCol ))
      rc = null;
    else if ( col == efcCol )
      rc = new EFCRenderer();
    else if ( col == hexCol )
      rc = new HexRenderer();
    else
      rc = protocol.getColumnRenderer( col - colOffset );
    return rc;
  }
}

