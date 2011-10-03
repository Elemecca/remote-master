package com.hifiremote.jp1;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class FunctionTableModel.
 */
public class FunctionTableModel extends KMTableModel< Function >
{

  /** The protocol. */
  private Protocol protocol = null;

  /** The remote. */
  private Remote remote = null;

  /** The Constant rowCol. */
  private final static int rowCol = 0;

  /** The Constant nameCol. */
  private final static int nameCol = rowCol + 1;

  /** The Constant efcCol. */
  private int efcCol = nameCol + 1;

  /** The efc5col. */
  private int efc5col = -1;

  /** The col offset. */
  private int colOffset = efcCol + 1;

  /** The hex col. */
  private int hexCol = colOffset;

  /** The notes col. */
  private int notesCol = hexCol + 1;

  /**
   * Instantiates a new function table model.
   * 
   * @param deviceUpgrade
   *          the device upgrade
   */
  public FunctionTableModel( DeviceUpgrade deviceUpgrade )
  {
    super();
    if ( deviceUpgrade != null )
      setDeviceUpgrade( deviceUpgrade );
  }

  /**
   * Sets the device upgrade.
   * 
   * @param deviceUpgrade
   *          the new device upgrade
   */
  public void setDeviceUpgrade( DeviceUpgrade deviceUpgrade )
  {
    if ( deviceUpgrade == null )
      return;
    setData( deviceUpgrade.getFunctions() );
    setProtocol( deviceUpgrade.getProtocol(), deviceUpgrade.getRemote() );
    functionsUpdated();
  }

  /**
   * Functions updated.
   */
  public void functionsUpdated()
  {
    fireTableStructureChanged();
  }

  /**
   * Sets the protocol.
   * 
   * @param protocol
   *          the protocol
   * @param remote
   *          the remote
   */
  public void setProtocol( Protocol protocol, Remote remote )
  {
    this.protocol = protocol;
    this.remote = remote;
    efcCol = nameCol + 1;
    colOffset = efcCol + 1;
    if ( ( remote != null ) && ( remote.getEFCDigits() == 5 ) )
    {
      efc5col = efcCol;
      efcCol = colOffset;
      colOffset += 1;
    }
    else
      efc5col = -1;

    hexCol = protocol.getColumnCount() + colOffset;
    notesCol = hexCol + 1;

    fireTableStructureChanged();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount()
  {
    int rc = 5;
    if ( ( remote != null ) && ( remote.getEFCDigits() == 5 ) )
      rc += 1;
    if ( protocol != null )
      rc += protocol.getColumnCount();
    return rc;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt( int row, int col )
  {
    Function function = data.get( row );
    Hex hex = function.getHex();

    Object rc = "";
    if ( col == rowCol )
      rc = new Integer( row + 1 );
    else if ( col == nameCol )
      rc = function.getName();
    else if ( col == efcCol )
    {
      if ( hex == null )
        return null;
      rc = new EFC( hex, protocol.getCmdIndex() );
    }
    else if ( col == efc5col )
    {
      if ( hex == null )
        return null;
      rc = new EFC5( hex );
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

  /**
   * Check function assigned.
   * 
   * @param f
   *          the f
   * @param value
   *          the value
   * @throws IllegalArgumentException
   *           the illegal argument exception
   */
  public void checkFunctionAssigned( Function f, Object value ) throws IllegalArgumentException
  {
    if ( ( value == null ) && f.assigned() )
    {
      String msg = "Function " + f.getName() + " is assigned to a button, and must not be cleared!";

      // KeyMapMaster.showMessage( msg );
      throw new IllegalArgumentException( msg );
    }
    // else
    // KeyMapMaster.clearMessage();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
   */
  public void setValueAt( Object value, int row, int col )
  {
    Function function = data.get( row );
    if ( col == nameCol )
    {
      String text = ( String )value;
      if ( ( text != null ) && ( text.length() == 0 ) )
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
        if ( hex == null )
          hex = protocol.getDefaultCmd();
        if ( value.getClass() == String.class )
          EFC.toHex( Short.parseShort( ( String )value ), hex, protocol.getCmdIndex() );
        else
          ( ( EFC )value ).toHex( hex, protocol.getCmdIndex() );
        function.setHex( hex );
      }
    }
    else if ( col == efc5col )
    {
      checkFunctionAssigned( function, value );
      if ( value == null )
        function.setHex( null );
      else
      {
        Hex hex = function.getHex();
        if ( hex == null )
          hex = protocol.getDefaultCmd();
        if ( value.getClass() == String.class )
          EFC5.toHex( Integer.parseInt( ( String )value ), hex );
        else
          ( ( EFC5 )value ).toHex( hex );
        function.setHex( hex );
      }
    }
    else if ( col == hexCol )
    {
      checkFunctionAssigned( function, value );
      if ( ( value != null ) && ( value.getClass() == String.class ) )
        value = new Hex( ( String )value );
      function.setHex( ( Hex )value );
    }
    else if ( col == notesCol )
      function.setNotes( ( String )value );
    else
    {
      CmdParameter[] cmdParms = protocol.getCommandParameters();
      int parmIndex = col - colOffset;
      DefaultValue defaultValue = cmdParms[ parmIndex ].getDefaultValue();
      System.err.println( "FunctionTableModel.setValueAt(): defaultValue is " + defaultValue );
      if ( defaultValue != null )
        checkFunctionAssigned( function, value );
      if ( value == null && defaultValue != null )
        value = defaultValue.value();
      System.err.println( "FunctionTableModel.setValueAt(): value is " + value );

      if ( ( value == null ) && !cmdParms[ parmIndex ].isOptional() )
        function.setHex( null );
      else
      {
        Hex hex = function.getHex();
        if ( hex == null )
        {
          hex = protocol.getDefaultCmd();
          function.setHex( hex );
        }
        protocol.setValueAt( parmIndex, hex, value );
      }
    }
    fireTableRowsUpdated( row, row );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  public String getColumnName( int col )
  {
    String rc = null;
    if ( col == rowCol )
      rc = "#";
    else if ( col == nameCol )
      rc = "Name";
    else if ( col == efcCol )
      rc = "EFC";
    else if ( col == efc5col )
      rc = "EFC5";
    else if ( col == hexCol )
      rc = "Hex";
    else if ( col == notesCol )
      rc = "Notes";
    else
      rc = protocol.getColumnName( col - colOffset );

    return rc;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnPrototypeName(int)
   */
  public String getColumnPrototypeName( int col )
  {
    String rc = null;
    if ( col == rowCol )
      rc = "199";
    else if ( col == nameCol )
      rc = "Function Name";
    else if ( col == efcCol )
      rc = "CCC";
    else if ( col == efc5col )
      rc = "00000";
    else if ( col == hexCol )
      rc = "CC CC";
    else if ( col == notesCol )
      rc = "A reasonable length function comment";
    else
      rc = protocol.getColumnName( col - colOffset );
    return rc;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  public Class< ? > getColumnClass( int col )
  {
    Class< ? > rc = null;
    if ( ( col == nameCol ) || ( col == notesCol ) )
      rc = String.class;
    else if ( col == rowCol )
      rc = Integer.class;
    else if ( col == efcCol )
      rc = EFC.class;
    else if ( col == efc5col )
      rc = EFC5.class;
    else if ( col == hexCol )
      rc = byte[].class;
    else
      rc = protocol.getColumnClass( col - colOffset );

    return rc;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
   */
  public boolean isCellEditable( int row, int col )
  {
    boolean rc = false;
    if ( col == rowCol )
      rc = false;
    else if ( ( col <= hexCol ) || ( col == notesCol ) )
      rc = true;
    else
      rc = protocol.isEditable( col - colOffset );

    return rc;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnEditor(int)
   */
  public TableCellEditor getColumnEditor( int col )
  {
    if ( ( remote == null ) || ( protocol == null ) || ( col == rowCol ) || ( col == nameCol ) || ( col == notesCol ) )
      return null;
    if ( col == efcCol )
      return new EFCEditor( 3 );
    if ( col == efc5col )
      return new EFCEditor( 5 );
    if ( col == hexCol )
      return new HexEditor( protocol.getDefaultCmd() );
    else
      return protocol.getColumnEditor( col - colOffset );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnRenderer(int)
   */
  public TableCellRenderer getColumnRenderer( int col )
  {
    TableCellRenderer rc = null;
    if ( col == rowCol )
      rc = new RowNumberRenderer();
    else if ( ( col == nameCol ) || ( col == notesCol ) )
      rc = null;
    else if ( col == efcCol )
      rc = new EFCRenderer();
    else if ( col == efc5col )
      rc = new EFCRenderer();
    else if ( col == hexCol )
      rc = new HexRenderer();
    else
      rc = protocol.getColumnRenderer( col - colOffset );
    return rc;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.KMTableModel#isColumnWidthFixed(int)
   */
  public boolean isColumnWidthFixed( int col )
  {
    if ( ( col == rowCol ) || ( col == nameCol ) || ( col == notesCol ) || ( col == efcCol ) || ( col == efc5col )
        || ( col == hexCol ) )
      return super.isColumnWidthFixed( col );
    else
      return protocol.isColumnWidthFixed( col - colOffset );
  }
}
