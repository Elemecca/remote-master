package com.hifiremote.jp1;

import javax.swing.table.TableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class DecodeTableModel.
 */
public class DecodeTableModel extends JP1TableModel< LearnedSignalDecode >
{

  /**
   * Instantiates a new decode table model.
   */
  public DecodeTableModel()
  {}

  /**
   * Sets the.
   * 
   * @param learnedSignal
   *          the learned signal
   */
  public void set( LearnedSignal learnedSignal )
  {
    setData( learnedSignal.getDecodes() );
    fireTableDataChanged();
  }

  /** The Constant colNames. */
  private static final String[] colNames =
  {
      "#", "Protocol", "Device", "<html>Sub<br>Device</html>", "OBC", "Hex Cmd", "EFC", "Misc", "Ignore"
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  @Override
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount()
  {
    return colNames.length;
  }

  /** The col prototype names. */
  private static String[] colPrototypeNames =
  {
      " 00 ", "Protocol Name", "Device", "Device", "OBC", "Hex Cmd", "EFC", "Miscellaneous", "Ignore"
  };

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnPrototypeName(int)
   */
  @Override
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ col ];
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#isColumnWidthFixed(int)
   */
  @Override
  public boolean isColumnWidthFixed( int col )
  {
    if ( col == 1 || col == 7 )
    {
      return false;
    }
    return true;
  }

  /** The Constant colClasses. */
  private static final Class< ? >[] colClasses =
  {
      Integer.class, String.class, Integer.class, Integer.class, Integer.class, Integer.class, String.class,
      String.class, Boolean.class
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  @Override
  public Class< ? > getColumnClass( int col )
  {
    return colClasses[ col ];
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt( int row, int column )
  {
    LearnedSignalDecode decode = getRow( row );
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return decode.protocolName;
      case 2:
        if ( decode.device == -1 )
        {
          return null;
        }
        return new Integer( decode.device );
      case 3:
        if ( decode.subDevice == -1 )
        {
          return null;
        }
        return new Integer( decode.subDevice );
      case 4:
        return new Integer( decode.obc );
      case 5:
        return Hex.toString( decode.hex );
      case 6: // EFC
        short[] temp = new short[ decode.hex.length ];
        if ( temp.length == 0 )
        {
          return null;
        }
        for ( int i = 0; i < temp.length; ++i )
        {
          temp[ i ] = ( short )decode.hex[ i ];
        }
        Hex hex = new Hex( temp );
        EFC efc = new EFC( hex );
        return efc.toString();
      case 7: // Misc
        return decode.miscMessage;
      case 8: // Ignore
        return decode.ignore;
      default:
        return null;
    }
  }

  @Override
  public boolean isCellEditable( int row, int col )
  {
    return col == 8;
  }

  @Override
  public void setValueAt( Object value, int row, int col )
  {
    if ( col == 8 )
    {
      getRow( row ).ignore = ( ( Boolean )value ).booleanValue();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnRenderer(int)
   */
  @Override
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
    {
      return new RowNumberRenderer();
    }
    return null;
  }
}
