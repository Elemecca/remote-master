package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;

public class DecodeTableModel
  extends JP1TableModel< LearnedSignalDecode >
{
  public DecodeTableModel(){}

  public void set( LearnedSignal learnedSignal )
  {
    this.learnedSignal = learnedSignal;
    setData( learnedSignal.getDecodes());
    fireTableDataChanged();
  }


  private static final String[] colNames =
  {
    "#", "Protocol", "Device", "<html>Sub<br>Device</html>", "OBC", "Hex Cmd", "EFC", "Misc"
  };
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }
  public int getColumnCount(){ return colNames.length; }

  private static String[] colPrototypeNames =
  {
    "00", "Protocol Name", "Device", "Device", "OBC", "Hex Cmd", "EFC", "Miscellaneous"
  };
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ col ];
  }

  public boolean isColumnWidthFixed( int col )
  {
    if (( col == 1 ) || ( col == 7 ))
      return false;
    return true;
  }

  private static final Class[] colClasses =
  {
    Integer.class, String.class, Integer.class, Integer.class, Integer.class, Integer.class, String.class, String.class
  };
  public Class getColumnClass( int col )
  {
    return colClasses[ col ];
  }

  public Object getValueAt(int row, int column)
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
          return null;
        return new Integer( decode.device );
      case 3:
        if ( decode.subDevice == -1 )
          return null;
        return new Integer( decode.subDevice );
      case 4:
        return new Integer( decode.obc );
      case 5:
        return Hex.toString( decode.hex );
      case 6: // EFC
        short[] temp = new short[ decode.hex.length ];
        for ( int i = 0; i < temp.length; ++i )
          temp[ i ] = ( short )decode.hex[ i ];
        Hex hex = new Hex( temp );
        EFC efc = new EFC( hex );
        return efc.toString();
      case 7: // Misc
        return decode.miscMessage;
      default:
        return null;
    }
  }

  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
      return new RowNumberRenderer();
    return null;
  }

  private LearnedSignal learnedSignal = null;
}
