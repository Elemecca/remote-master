package com.hifiremote.jp1;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import java.util.Vector;

public class ExternalFunctionTableModel
  extends KMTableModel
{
  private DeviceUpgrade upgrade = null;
  private DefaultCellEditor devTypeEditor = null;
  private final static int rowCol = 0;
  private final static int nameCol = rowCol + 1;
  private final static int devTypeCol = nameCol + 1;
  private final static int setupCodeCol = devTypeCol + 1;
  private final static int typeCol = setupCodeCol + 1;
  private final static int hexCol = typeCol + 1;
  private final static int notesCol = hexCol + 1;

  public ExternalFunctionTableModel( DeviceUpgrade upgrade )
  {
    this.upgrade = upgrade;
  }

  public void update()
  {
    JComboBox cb = ( JComboBox )devTypeEditor.getComponent();
    cb.setModel( new DefaultComboBoxModel( upgrade.getRemote().getDeviceTypes()));
    fireTableDataChanged();
  }

  public int getRowCount()
  {
    int rc = upgrade.getExternalFunctions().size();

    return rc;
  }

  public int getColumnCount()
  {
    return 7;
  }

  public Object getValueAt( int row, int col )
  {
    Vector functions = upgrade.getExternalFunctions();
    ExternalFunction function = ( ExternalFunction )functions.elementAt( row );
    byte[] hex = function.getHex();

    Object rc = null;

    switch ( col )
    {
      case rowCol:
        rc = new Integer( row + 1 );
        break;
      case nameCol:
        rc = function.getName();
        break;
      case devTypeCol:
        rc = function.getDeviceType();
        break;
      case setupCodeCol:
        rc = new Integer( function.getSetupCode());
        break;
      case typeCol:
        rc = new Integer( function.getType());
        break;
      case hexCol:
        rc = function;
        break;
      case notesCol:
        rc = function.getNotes();
        break;
      default:
        break;
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
    System.err.println( "ExternalFunctionTableModel.setValueAt( " + value + ", " + row + ", " + col + " )" );
    Vector functions = upgrade.getExternalFunctions();
    Object o = functions.elementAt( row );
    System.err.println( "function at row " + row + " is " + o );
    ExternalFunction function = ( ExternalFunction )functions.elementAt( row );
    switch ( col )
    {
      case nameCol:
        function.setName(( String )value );
        break;
      case devTypeCol:
        function.setDeviceType(( DeviceType )value );
        break;
      case setupCodeCol:
        function.setSetupCode((( Integer )value ).intValue());
        break;
      case typeCol:
        function.setType(( Integer )value );
        break;
      case hexCol:
        break;
      case notesCol:
        function.setNotes(( String )value );
        break;
      default:
        break;
    }
    fireTableRowsUpdated( row, row );
  }

  public TableCellEditor getEditor( int col )
  {
    TableCellEditor rc = null;
    switch ( col )
    {
      case nameCol:
        break;
      case devTypeCol:
        devTypeEditor = new DefaultCellEditor( new JComboBox());
        rc = devTypeEditor;
        break;
      case setupCodeCol:
        rc = new ByteEditor( 0, 2047 );
        break;
      case typeCol:
        rc = new ChoiceEditor( choices, false );
        break;
      case hexCol:
        rc = new ExternalFunctionEditor();
        break;
      case notesCol:
        break;
      default:
        break;
    }

    return rc;
  }

  public TableCellRenderer getRenderer( int col )
  {
    TableCellRenderer rc = null;
    switch ( col )
    {
      case rowCol:
        rc = new RowNumberRenderer();
        break;
      case nameCol:
        break;
      case devTypeCol:
        break;
      case setupCodeCol:
        rc = new ByteRenderer();
        break;
      case typeCol:
        rc = new ChoiceRenderer( choices );
        break;
      case hexCol:
        rc = new ExternalFunctionRenderer();
        break;
      case notesCol:
        break;
      default:
        break;
    }

    return rc;
  }

  public String getColumnName( int col )
  {
    return names[ col ];
  }

  public Class getColumnClass( int col )
  {
    return classes[ col ];
  }

  public boolean isCellEditable( int row, int col )
  {
    boolean rc = true;
    if ( col == rowCol )
      rc = false;
    return rc;   
  }

  public void removeRow( int row )
  {
    upgrade.getExternalFunctions().removeElementAt( row );
  }

  public void insertRow( int row, Object object )
  {
    upgrade.getExternalFunctions().insertElementAt( object, row );
  }
  
  public void addRow( Object object )
  {
    upgrade.getExternalFunctions().add( object );
  }  

  public Object getRow( int row )
  {
    return upgrade.getExternalFunctions().elementAt( row );
  }

  public void moveRow( int from, int to )
  {
    Vector functions = upgrade.getExternalFunctions();
    Object o = functions.elementAt( from );
    if ( from < to )
      to--;

    functions.insertElementAt( o, to );
  }

  private final static String[] names =
    { " # ", "Name", "Device Type", "Setup Code", "Type", "EFC/Hex", "Notes" };
  private final static Class[] classes =
    { Integer.class, String.class, DeviceType.class, Integer.class, Integer.class, ExternalFunction.class, String.class };

  private final static Choice[] choices =
  {
    new Choice( ExternalFunction.EFCType, "EFC" ),
    new Choice( ExternalFunction.HexType, "Hex" )
  };
}

