package com.hifiremote.jp1;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.Vector;

public class FunctionTableModel
  extends KMTableModel
{
  private Vector functions = null;
  private Protocol protocol = null;
  private final static int rowCol = 0;
  private final static int nameCol = rowCol + 1;
  private final static int efcCol = nameCol + 1;
  private final static int colOffset = efcCol + 1;
  private int hexCol = colOffset;
  private int notesCol = hexCol + 1;

  public FunctionTableModel( Vector functions )
  {
    super( functions );
  }

  public void functionsUpdated()
  {
    fireTableDataChanged();
  }

  public void setProtocol( Protocol protocol )
  {
    this.protocol = protocol;
    hexCol = protocol.getColumnCount() + colOffset;
    notesCol = hexCol + 1;
    fireTableStructureChanged();
  }

  public int getColumnCount()
  {
    int rc = 5;
    if ( protocol != null )
      rc += protocol.getColumnCount() ;
    return rc;
  }

  public Object getValueAt( int row, int col )
  {
    Function function = ( Function )data.elementAt( row );
    Hex hex = function.getHex();

    Object rc = "";
    if ( col == rowCol )
      rc = new Integer( row + 1 );
    else if ( col == nameCol )
      rc = function.getName();
    else if ( col == efcCol )
    {
      if ( hex != null )
        rc = protocol.hex2efc( hex );
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
    else
      KeyMapMaster.clearMessage();
  }

  public void setValueAt( Object value, int row, int col )
  {
    Function function = ( Function )data.elementAt( row );
    if ( col == nameCol )
    {
      String text = ( String )value;
      if ( text.length() == 0 )
        text = null;
      checkFunctionAssigned( function, text );
      function.setName( text );
    }
    else if ( col == efcCol )
    {
      checkFunctionAssigned( function, value );
      if ( value == null )
        function.setHex( null );
      else
      {
        Hex hex = function.getHex();
        if ( value.getClass() == String.class )
          value = new EFC(( String )value );
        hex = protocol.efc2hex(( EFC )value, hex );
        function.setHex( hex );
      }
    }
    else if ( col == hexCol )
    {
      checkFunctionAssigned( function, value );
      if (( value != null ) && ( value.getClass() == String.class ))
        value = new Hex(( String )value );
      function.setHex(( Hex )value );
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
        Hex hex = function.getHex();
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
    if ( col == rowCol )
      rc = " # ";
    else if ( col == nameCol )
      rc = "Name";
    else if ( col == efcCol )
      rc = "EFC";
    else if ( col == hexCol )
      rc = " Hex ";
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
    else if ( col == rowCol )
      rc = Integer.class;
    else if ( col == efcCol )
      rc = EFC.class;
    else if ( col == hexCol )
      rc = byte[].class;
    else
      rc = protocol.getColumnClass( col - colOffset );

    return rc;
  }

  public boolean isCellEditable( int row, int col )
  {
    boolean rc = false;
    if ( col == rowCol )
      rc = false;
    else if (( col <= hexCol ) || ( col == notesCol ))
      rc = true;
    else
      rc = protocol.isEditable( col - colOffset );

    return rc;
  }

  public TableCellEditor getColumnEditor( int col )
  {
    TableCellEditor rc = null;
    if (( col == rowCol ) || ( col == nameCol ) || ( col == notesCol ))
      rc = null;
    else if ( col == efcCol )
      rc = new EFCEditor();
    else if ( col == hexCol )
      rc = new HexEditor( protocol.getDefaultCmd());
    else
      rc = protocol.getColumnEditor( col - colOffset );
    return rc;
  }

  public TableCellRenderer getColumnRenderer( int col )
  {
    TableCellRenderer rc = null;
    if ( col == rowCol )
      rc = new RowNumberRenderer();
    else if (( col == nameCol ) || ( col == notesCol ))
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

