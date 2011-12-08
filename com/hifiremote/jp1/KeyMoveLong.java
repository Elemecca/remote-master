package com.hifiremote.jp1;

import java.util.Properties;

public class KeyMoveLong extends KeyMove
{
  public KeyMoveLong( int keyCode, int deviceButtonIndex, Hex data, String notes )
  {
    super( keyCode, deviceButtonIndex, data, notes );

  }

  public KeyMoveLong( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
  }
  
  public KeyMoveLong( Properties props )
  {
    super( props );
  }

  @Override
  public Hex getRawHex( int deviceType, int setupCode, Hex cmd )
  {
    Hex hex = new Hex( getCmdIndex() == 2 ? 4 : 6 );
    short[] hexData = hex.getData();
    short[] cmdData = cmd.getData();
    hexData[ getCmdIndex() ] = cmdData[ 0 ];
    if ( getCmdIndex() == 3 && cmdData.length == 3 )
    {
      hexData[ getCmdIndex() + 1 ] = cmdData[ 1 ];
      hexData[ getCmdIndex() + 2 ] = cmdData[ 2 ];
    }
    else if ( cmdData.length == 2 )
    {
      hexData[ getCmdIndex() + 1 ] = cmdData[ 1 ];
    }
    else
    {
      hexData[ getCmdIndex() + 1 ] = EFC.parseHex( cmd );
    }
    update( deviceType, setupCode, hex );
    return hex;
  }
}
