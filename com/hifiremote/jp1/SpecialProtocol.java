package com.hifiremote.jp1;

import java.util.*;

public class SpecialProtocol
{
  private String name = null;
  private Hex pid = null;
  private DeviceUpgrade upgrade = null;
  
  public SpecialProtocol( String name, Hex pid )
  {
    this.name = name;
    this.pid = pid;
  }
  
  public DeviceUpgrade getDeviceUpgrade( Vector< DeviceUpgrade > upgrades )
  {
    for ( DeviceUpgrade upgrade : upgrades )
    {
      if ( upgrade.getProtocol().getID().equals( pid ))
        return upgrade;
    }
    return null;
  }
  
  public SpecialProtocolFunction createFunction( KeyMove keyMove )
  {
    if ( name.equals( "DSM" ))
      return new DSMFunction( keyMove );
    if ( name.equals( "UDSM" ))
      return new UDSMFunction( keyMove );
    if ( name.equals( "LDKP" ))
      return new LDKPFunction( keyMove );
    if ( name.equals( "ULDKP" ))
      return new ULDKPFunction( keyMove );
    if ( name.equals( "Multiplex" ))
      return new MultiplexFunction( keyMove );
    if ( name.equals( "Pause" ))
      return new PauseFunction( keyMove );
    if ( name.equals( "ToadTog" ))
      return new ToadTogFunction( keyMove );
    if ( name.equals( "ModeName" ))
      return new ModeNameFunction( keyMove );
    return null;
  }
  
  public String getName(){ return name; }
  public Hex getPid(){ return pid; }
}
