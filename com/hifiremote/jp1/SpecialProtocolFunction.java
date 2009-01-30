package com.hifiremote.jp1;

import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class SpecialProtocolFunction.
 */
public abstract class SpecialProtocolFunction
  extends KeyMove
{
  
  /**
   * Instantiates a new special protocol function.
   * 
   * @param keyMove the key move
   */
  public SpecialProtocolFunction( KeyMove keyMove )
  {
    super( keyMove );
  }
  
  /**
   * Instantiates a new special protocol function.
   * 
   * @param keyCode the key code
   * @param deviceButtonIndex the device button index
   * @param deviceType the device type
   * @param setupCode the setup code
   * @param cmd the cmd
   * @param notes the notes
   */
  public SpecialProtocolFunction( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
  }    
  
  /**
   * Instantiates a new special protocol function.
   * 
   * @param props the props
   */
  public SpecialProtocolFunction( Properties props )
  {
    super( props );
  }
  
  /**
   * Update.
   * 
   * @param dlg the dlg
   */
  public abstract void update( SpecialFunctionDialog dlg );
  
  /**
   * Gets the type.
   * 
   * @return the type
   */
  public abstract String getType();
  
  /**
   * Gets the display type.
   * 
   * @return the display type
   */
  public abstract String getDisplayType();
}
