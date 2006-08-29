package com.hifiremote.jp1;

import java.util.*;

public abstract class SpecialProtocolFunction
  extends KeyMove
{
  public SpecialProtocolFunction( KeyMove keyMove )
  {
    super( keyMove );
  }
  
  public SpecialProtocolFunction( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
  }    
  
  public SpecialProtocolFunction( Properties props )
  {
    super( props );
  }
  
  public abstract void update( SpecialFunctionDialog dlg );
  
  public abstract String getType();
  public abstract String getDisplayType();
}
