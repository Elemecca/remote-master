package com.hifiremote.jp1;

public class KeyMoveEFC5
  extends KeyMove
{
  public KeyMoveEFC5( int keyCode, int deviceButtonIndex, Hex data, String notes )
  {
    super( keyCode, deviceButtonIndex, data, notes );
  }

  public Object getValue()
  {
    return new EFC5( data.get( CMD_INDEX ));
  }

  public void setValue( Object value )
  {
    data.put((( EFC5 )value ).getValue(), CMD_INDEX );
  }
}
