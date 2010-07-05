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
  
  public String[] getUserFunctions( RemoteConfiguration remoteConfig )
  {
    Remote remote = remoteConfig.getRemote();
    for ( SpecialProtocol sp : remote.getSpecialProtocols() )
    {
      DeviceType type = null;
      int code = 0;
      
      DeviceUpgrade upgrade = sp.getDeviceUpgrade( remoteConfig.getDeviceUpgrades() );
      if ( upgrade != null )
      {
        type = upgrade.getDeviceType();
        code = upgrade.getSetupCode();
      }
      else
      {      
        type = sp.getDeviceType();
        code = sp.getSetupCode();
      }
      if ( type == null)
        continue;
      if ( type.getNumber() == getDeviceType()
          && code == getSetupCode() )
        return sp.getUserFunctions();
    }
    return unknownFunctions;
  }
  
  private String[] unknownFunctions = { "<unknown>", "<unknown>", "<unknown>" };
  
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
  public abstract String getDisplayType( RemoteConfiguration remoteConfig );
}
