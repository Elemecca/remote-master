package com.hifiremote.jp1;

import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class Macro.
 */
public class Macro extends AdvancedCode
{

  /**
   * Instantiates a new macro.
   * 
   * @param keyCode
   *          the key code
   * @param keyCodes
   *          the key codes
   * @param notes
   *          the notes
   */
  public Macro( int keyCode, Hex keyCodes, String notes )
  {
    super( keyCode, keyCodes, notes );
  }

  /**
   * Instantiates a new macro.
   * 
   * @param props
   *          the props
   */
  public Macro( Properties props )
  {
    super( props );
  }

  /**
   * Gets the value.
   * 
   * @return the value
   */
  public Object getValue()
  {
    return getData();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.AdvancedCode#getValueString(com.hifiremote.jp1.RemoteConfiguration)
   */
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    Remote remote = remoteConfig.getRemote();
    StringBuilder buff = new StringBuilder();
    short[] keys = data.getData();
    for ( int i = 0; i < keys.length; ++i )
    {
      if ( i != 0 )
        buff.append( ';' );
      if ( keys[ i ] == 0 )
      {
        buff.append( "{Pause}" );
        while ( ( i < keys.length ) && ( keys[ i ] == 0 ) )
          ++i;
      }
      else
        buff.append( remote.getButtonName( keys[ i ] ) );
    }
    return buff.toString();
  }

  /**
   * Sets the value.
   * 
   * @param value
   *          the new value
   */
  public void setValue( Object value )
  {
    setData( ( Hex )value );
  }

  public int store( short[] buffer, int offset )
  {
    buffer[ offset++ ] = ( short )getKeyCode();
    if ( bindFormat == BindFormat.NORMAL )
    {
      buffer[ offset ] = 0x10;
    }
    else
    {
      buffer[ offset++ ] = 0x80;
    }
    int dataLength = data.length();
    buffer[ offset++ ] |= ( short )dataLength;
    Hex.put( data, buffer, offset );

    return offset + dataLength;
  }

}
