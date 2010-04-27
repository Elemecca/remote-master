package com.hifiremote.jp1;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class ExternalFunctionTableModel.
 */
public class ExternalFunctionTableModel
  extends KMTableModel< ExternalFunction >
{
  /** The Constant rowCol. */
  private final static int rowCol = 0;
  
  /** The Constant nameCol. */
  private final static int nameCol = rowCol + 1;
  
  /** The Constant devTypeCol. */
  private final static int devTypeCol = nameCol + 1;
  
  /** The Constant setupCodeCol. */
  private final static int setupCodeCol = devTypeCol + 1;
  
  /** The Constant typeCol. */
  private final static int typeCol = setupCodeCol + 1;
  
  /** The Constant hexCol. */
  private final static int hexCol = typeCol + 1;
  
  /** The Constant notesCol. */
  private final static int notesCol = hexCol + 1;

  /**
   * Instantiates a new external function table model.
   * 
   * @param upgrade the upgrade
   */
  public ExternalFunctionTableModel( DeviceUpgrade upgrade )
  {
    super( upgrade.getExternalFunctions());
  }

  /**
   * Update.
   */
  public void update()
  {
    fireTableDataChanged();
  }

  /* (non-Javadoc)
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount()
  {
    return 7;
  }

  /* (non-Javadoc)
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt( int row, int col )
  {
    ExternalFunction function = getRow( row );
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
        rc = function.getDeviceTypeAliasName();
        break;
      case setupCodeCol:
        rc = new Integer( function.getSetupCode());
        break;
      case typeCol:
        rc = choices[ function.getType() ];
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

  /**
   * Check function assigned.
   * 
   * @param f the f
   * @param value the value
   * 
   * @throws IllegalArgumentException the illegal argument exception
   */
  public void checkFunctionAssigned( Function f, Object value )
    throws IllegalArgumentException
  {
    if (( value == null ) && f.assigned() )
    {
      String msg = "Function " + f.getName() + " is assigned to a button, and must not be cleared!";
      throw new IllegalArgumentException( msg );
    }
  }

  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
   */
  public void setValueAt( Object value, int row, int col )
  {
    ExternalFunction function = getRow( row );
    switch ( col )
    {
      case nameCol:
        function.setName(( String )value );
        break;
      case devTypeCol:
        function.setDeviceTypeAliasName(( String )value );
        break;
      case setupCodeCol:
        if ( value.getClass() == String.class )
          value = new Integer(( String )value );
        function.setSetupCode((( Integer )value ).intValue());
        break;
      case typeCol:
        if ( value.getClass() == String.class )
        {
          String str = ( String )value;
          for ( int i = 0; i < choices.length; i++ )
          {
            if ( str.equals( choices[ i ].getText()))
            {
              value = choices[ i ];
              break;
            }
          }
        }
        function.setType((( Choice )value ).getIndex());
        break;
      case hexCol:
        if ( value.getClass() == String.class )
        {
          String str = ( String )value;
          if ( function.getType() == ExternalFunction.EFCType )
            function.setEFC( new EFC( str ));
          else
            function.setHex( new Hex( str ));
        }
        break;
      case notesCol:
        function.setNotes(( String )value );
        break;
      default:
        break;
    }
    fireTableRowsUpdated( row, row );
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.JP1TableModel#getColumnEditor(int)
   */
  public TableCellEditor getColumnEditor( int col )
  {
    TableCellEditor rc = null;
    switch ( col )
    {
      case nameCol:
        break;
      case devTypeCol:
        rc = new DefaultCellEditor( new JComboBox( DeviceUpgrade.getDeviceTypeAliasNames()));
        break;
      case setupCodeCol:
        rc = new ByteEditor( 0, 2047, null );
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

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.JP1TableModel#getColumnRenderer(int)
   */
  public TableCellRenderer getColumnRenderer( int col )
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

  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  public String getColumnName( int col )
  {
    return names[ col ];
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.JP1TableModel#getColumnPrototypeName(int)
   */
  public String getColumnPrototypeName( int col )
  {
    return prototypeNames[ col ];
  }

  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  public Class<?> getColumnClass( int col )
  {
    return classes[ col ];
  }

  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
   */
  public boolean isCellEditable( int row, int col )
  {
    boolean rc = true;
    if ( col == rowCol )
      rc = false;
    return rc;
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.KMTableModel#isColumnWidthFixed(int)
   */
  public boolean isColumnWidthFixed( int col )
  {
    if (( col == nameCol ) || ( col == hexCol ) || ( col == notesCol ))
      return false;
    return true;
  }

  /** The Constant names. */
  private final static String[] names =
    { "#", "Name", "Device Type", "Setup Code", "Type", "EFC/Hex", "Notes" };
  
  /** The Constant prototypeNames. */
  private final static String[] prototypeNames =
    { " # ", "Function Name", "Device Type", "Setup Code", "Type", "EFC/Hex", "A reasonanble comment" };
  
  /** The Constant classes. */
  private final static Class<?>[] classes =
    { Integer.class, String.class, String.class, Integer.class, Choice.class, ExternalFunction.class, String.class };

  /** The Constant choices. */
  private final static Choice[] choices =
  {
    new Choice( ExternalFunction.EFCType, "EFC" ),
    new Choice( ExternalFunction.HexType, "Hex" )
  };
}

