package com.hifiremote.jp1;

public class KeyMoveKey
  extends KeyMove
{
  public KeyMoveKey( int keyCode, int deviceButtonIndex, Hex data, String notes )
  {
    super( keyCode, deviceButtonIndex, data, notes );
  }

  public Object getValue()
  {
    return new Short( data.getData()[ CMD_INDEX ]);
  }

  public void setValue( Object value )
  {
    data.getData()[ CMD_INDEX ] = (( Short )value ).shortValue();
  }
}
