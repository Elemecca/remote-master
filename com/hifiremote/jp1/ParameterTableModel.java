package com.hifiremote.jp1;

import java.util.*;
import javax.swing.table.*;

public class ParameterTableModel
  extends AbstractTableModel

{
  public ParameterTableModel( List< ? extends Parameter > parms, List< Translate > xlators )
  {
    super();
    this.parms = parms;
    this.xlators = xlators;
  }

  public int getRowCount()
  {
    return parms.size();
  }

  public int getColumnCount()
  {
    return colNames.length;
  }

  public String getColumnName( int col )
  {
    if ( col < colNames.length )
      return colNames[ col ];
    else
      return null;
  }

  public Class getColumnClass( int col )
  {
    if ( col < colClasses.length )
      return colClasses[ col ];
    else
      return null;
  }

  public boolean isCellEditable( int row, int col )
  {
    if ( col == typeCol )
      return false;
    else
      return true;
  }

  public Object getValueAt( int row, int col )
  {
    Parameter parm = parms.get( row );
    Translator translator = ( Translator )xlators.get( row );
    switch ( col )
    {
      case nameCol:
        return parm.getName();
      case typeCol:
        return parm.getDescription();
      case bitsCol:
        return new Integer( translator.getBits());
      case orderCol:
        return Boolean.valueOf( translator.getLSB());
      case compCol:
        return Boolean.valueOf( translator.getComp());
      default:
        return null;
    }
  }

  public void setValueAt( Object value, int row, int col )
  {
    Parameter parm = parms.get( row );
    Translator translator = ( Translator )xlators.get( row );
    switch ( col )
    {
      case nameCol:
        parm.setName(( String )value );
        break;
      case typeCol:
        break;
      case bitsCol:
        translator.setBits((( Integer )value ).intValue());
        break;
      case orderCol:
        translator.setLSB((( Boolean )value ).booleanValue());
        break;
      case compCol:
        translator.setComp((( Boolean )value ).booleanValue());
        break;
      default:
    }
  }

  private List< ? extends Parameter > parms;
  private List< Translate > xlators;

  private final static int nameCol = 0;
  private final static int typeCol = 1;
  private final static int bitsCol = 2;
  private final static int orderCol = 3;
  private final static int compCol = 4;

  private final static String[] colNames =
  {
   "Name", "Type", "Bits", "LSB", "Comp"
  };

  private final static Class[] colClasses =
  {
    String.class, String.class, Integer.class, Boolean.class, Boolean.class
  };
}
