package com.hifiremote.jp1;

import java.awt.Color;
import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class SpecialProtocolFunction.
 */
public abstract class SpecialProtocolFunction extends Highlight
{ 
  private KeyMove keyMove = null;
  private Macro macro = null;
  
  /**
   * Instantiates a new special protocol function.
   * 
   * @param keyMove the key move
   */
  public SpecialProtocolFunction( KeyMove keyMove )
  {
    this.keyMove = new KeyMove( keyMove );
  }
  
  public SpecialProtocolFunction( Macro macro )
  {
    this.macro = new Macro( macro );
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
    // GD:  This, and its equivalents in the individual special function classes, appear to be unused.
    keyMove = new KeyMove( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
  }    
  
  /**
   * Instantiates a new special protocol function.
   * 
   * @param props the props
   */
  public SpecialProtocolFunction( Properties props )
  {
    boolean internal = false;
    String temp = props.getProperty( "Internal" );
    if ( temp != null )
    {
      internal = Boolean.parseBoolean( temp );
    }
    if ( internal )
    {
      macro = new Macro( props );
    }
    else
    {  
      keyMove = new KeyMove( props );
    }
  }
  
  public String[] getUserFunctions( RemoteConfiguration remoteConfig )
  {
    Remote remote = remoteConfig.getRemote();
    for ( SpecialProtocol sp : remote.getSpecialProtocols() )
    {
      if ( isInternal() )
      {
        if ( sp.isInternal() && sp.getInternalSerial() == getInternalSerial() )
        {
          return sp.getUserFunctions();
        }
        else
        {
          continue;
        }
      }
      
      if ( sp.isInternal() )
      {
        continue;
      }
 
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
      if ( type == null )
        continue;
      if ( type.getNumber() == getDeviceType()
          && code == getSetupCode() )
        return sp.getUserFunctions();
    }
    return unknownFunctions;
  }
    
  public KeyMove getKeyMove()
  {
    return keyMove;
  }
  
  public Macro getMacro()
  {
    return macro;
  }
  
  public int getKeyCode()
  {
    if ( keyMove != null )
    {
      return keyMove.getKeyCode();
    }
    else
    {
      return macro.getKeyCode();
    }
  }
  
  public void setKeyCode( int keyCode )
  {
    if ( keyMove != null )
    {
      keyMove.setKeyCode( keyCode );
    }
    else
    {
      macro.setKeyCode( keyCode );
    }
  }
  
  public String getValueString( RemoteConfiguration getValueString )
  {
    if ( keyMove != null )
    {
      return keyMove.getValueString( getValueString );
    }
    else
    {
      return macro.getValueString( getValueString );
    }
  }

  public Hex getCmd()
  {
    if ( keyMove != null )
    {
      return keyMove.getCmd();
    }
    else
    {
      return macro.getData();
    }    
  }

  public int getDeviceButtonIndex()
  {
    if ( keyMove != null )
    {
      return keyMove.getDeviceButtonIndex();
    }
    else
    {
      return macro.getDeviceButtonIndex();
    }
  }

  public void setDeviceButtonIndex( int index )
  {
    if ( keyMove != null )
    {
      keyMove.setDeviceButtonIndex( index );
    }
    else
    {
      macro.setDeviceButtonIndex( index );
    }
  }
  
  public int getDeviceType()
  {
    return keyMove.getDeviceType();
  }

  public int getSetupCode()
  {
    return keyMove.getSetupCode();
  }
  
  public int getInternalSerial()
  {
    return macro.getSequenceNumber();
  }
  
  public boolean isInternal()
  {
    return macro != null;
  }
  
  public String getNotes()
  {
    if ( keyMove != null )
    {
      return keyMove.getNotes();
    }
    else
    {
      return macro.getNotes();
    }
  }
  
  public void setNotes( String notes )
  {
    if ( keyMove != null )
    {
      keyMove.setNotes( notes );
    }
    else
    {
      macro.setNotes( notes );
    }
  }

  @Override
  public Color getHighlight()
  {
    if ( keyMove != null )
    {
      return keyMove.getHighlight();
    }
    else
    {
      return macro.getHighlight();
    }
  }
  
  @Override
  public void setHighlight( Color color )
  {
    if ( keyMove != null )
    {
      keyMove.setHighlight( color );
    }
    else
    {
      macro.setHighlight( color );
    }
  }
  
  @Override
  public int getMemoryUsage()
  {
    if ( keyMove != null )
    {
      return keyMove.getMemoryUsage();
    }
    else
    {
      return macro.getMemoryUsage();
    }
  }
  
  private static final String[] unknownFunctions = { "<unknown>", "<unknown>", "<unknown>" };
  
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
  public abstract String getType( RemoteConfiguration remoteConfig );
  
  /**
   * Gets the display type.
   * 
   * @return the display type
   */
  public abstract String getDisplayType( RemoteConfiguration remoteConfig );
}
