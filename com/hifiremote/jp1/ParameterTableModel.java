package com.hifiremote.jp1;

import javax.swing.table.AbstractTableModel;

import com.hifiremote.jp1.translate.Translate;
import com.hifiremote.jp1.translate.Translator;

// TODO: Auto-generated Javadoc
/**
 * The Class ParameterTableModel.
 */
public class ParameterTableModel extends AbstractTableModel

{

  /**
   * Instantiates a new parameter table model.
   * 
   * @param protocol
   *          the protocol
   * @param type
   *          the type
   */
  public ParameterTableModel( ManualProtocol protocol, Type type )
  {
    super();
    this.protocol = protocol;
    this.type = type;
  }

  /**
   * Gets the protocol info.
   * 
   * @return the protocol info
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getRowCount()
   */
  public int getRowCount()
  {
    getProtocolInfo();
    return parms.length;
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

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  @Override
  public String getColumnName( int col )
  {
    if ( col < colNames.length )
    {
      return colNames[ col ];
    }
    else
    {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  @Override
  public Class< ? > getColumnClass( int col )
  {
    if ( col < colClasses.length )
    {
      return colClasses[ col ];
    }
    else
    {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
   */
  @Override
  public boolean isCellEditable( int row, int col )
  {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
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
        return new Integer( translator.getBits() );
      case orderCol:
        return Boolean.valueOf( translator.getLSB() );
      case compCol:
        return Boolean.valueOf( translator.getComp() );
      default:
        return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
   */
  @Override
  public void setValueAt( Object value, int row, int col )
  {
    getProtocolInfo();
    Parameter parm = parms[ row ];
    Translator translator = ( Translator )xlators[ row ];
    switch ( col )
    {
      case nameCol:
        parm.setName( ( String )value );
        break;
      case bitsCol:
        int bits = ( ( Integer )value ).intValue();
        switch ( type )
        {
          case DEVICE:
            ( ( NumberDeviceParm )parm ).setBits( bits );
            break;
          case COMMAND:
            ( ( NumberCmdParm )parm ).setBits( bits );
            break;
        }
        translator.setBits( bits );
        break;
      case orderCol:
        translator.setLSB( ( ( Boolean )value ).booleanValue() );
        break;
      case compCol:
        translator.setComp( ( ( Boolean )value ).booleanValue() );
        break;
      default:
    }
  }

  /** The parms. */
  private Parameter[] parms;

  /** The xlators. */
  private Translate[] xlators;

  /** The protocol. */
  private ManualProtocol protocol = null;

  /**
   * The Enum Type.
   */
  public enum Type
  {
    /** The DEVICE. */
    DEVICE, /** The COMMAND. */
    COMMAND
  };

  /** The type. */
  private Type type = Type.DEVICE;

  /** The Constant nameCol. */
  private final static int nameCol = 0;

  /** The Constant bitsCol. */
  private final static int bitsCol = 1;

  /** The Constant orderCol. */
  private final static int orderCol = 2;

  /** The Constant compCol. */
  private final static int compCol = 3;

  /** The Constant colNames. */
  private final static String[] colNames =
  {
      "Name", "Bits", "LSB", "Comp"
  };

  /** The Constant colClasses. */
  private final static Class< ? >[] colClasses =
  {
      String.class, Integer.class, Boolean.class, Boolean.class
  };
}
