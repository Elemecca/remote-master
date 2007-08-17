package com.hifiremote.jp1;

import java.util.*;
import javax.swing.table.*;

public class ParameterTableModel
  extends AbstractTableModel

{
  public ParameterTableModel( ManualProtocol protocol, Type type )
  {
    super();
    this.protocol = protocol;
    this.type = type;
  }
  
  private void getProtocolInfo()
  {
    switch ( type )
    {
      case DEVICE:
        parms = protocol.getDeviceParameters();
        xlators = protocol.getDeviceTranslators();
        break;
      case COMMAND:
        parms = protocol.getCommandParameters();
        xlators = protocol.getCmdTranslators();
        break;
    }
  }

  public int getRowCount()
  {
    getProtocolInfo();
    return parms.length;
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
    return true;
  }

  public Object getValueAt( int row, int col )
  {
    getProtocolInfo();
    Parameter parm = parms[ row ];
    Translator translator = ( Translator )xlators[ row ];
    switch ( col )
    {
      case nameCol:
        return parm.getName();
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
    getProtocolInfo();
    Parameter parm = parms[ row ];
    Translator translator = ( Translator )xlators[ row ];
    switch ( col )
    {
      case nameCol:
        parm.setName(( String )value );
        break;
      case bitsCol:
        int bits = (( Integer )value ).intValue();
        switch ( type )
        {
          case DEVICE:
            (( NumberDeviceParm )parm ).setBits( bits );
            break;
          case COMMAND:
            (( NumberCmdParm )parm ).setBits( bits );
            break;
        }
        translator.setBits( bits );
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

  private Parameter[] parms;
  private Translate[] xlators;

  private ManualProtocol protocol = null;
  
  public enum Type { DEVICE, COMMAND };
  private Type type = Type.DEVICE;
  
  private final static int nameCol = 0;
  private final static int bitsCol = 1;
  private final static int orderCol = 2;
  private final static int compCol = 3;

  private final static String[] colNames =
  {
   "Name", "Bits", "LSB", "Comp"
  };

  private final static Class[] colClasses =
  {
    String.class, Integer.class, Boolean.class, Boolean.class
  };
}
