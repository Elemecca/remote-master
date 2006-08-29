package com.hifiremote.jp1;

import java.util.*;

public abstract class SpecialProtocol
{
  private String name = null;
  private Hex pid = null;
  private DeviceType deviceType = null;
  private int setupCode;
  
  protected SpecialProtocol( String name, Hex pid )
  {
    this.name = name;
    this.pid = pid;
  }
  
  public static SpecialProtocol create( String name, Hex pid )
  {
    if ( name.equals( "DSM" ))
      return new DSMSpecialProtocol( name, pid );
    if ( name.equals( "UDSM" ))
      return new UDSMSpecialProtocol( name, pid );
    if ( name.equals( "LDKP" ))
      return new LDKPSpecialProtocol( name, pid );
    if ( name.equals( "ULDKP" ))
      return new ULDKPSpecialProtocol( name, pid );
    if ( name.equals( "Multiplex" ))
      return new MultiplexSpecialProtocol( name, pid );
    if ( name.equals( "Pause" ))
      return new PauseSpecialProtocol( name, pid );
    if ( name.equals( "ToadTog" ))
      return new ToadTogSpecialProtocol( name, pid );
    if ( name.equals( "ModeName" ))
      return new ModeNameSpecialProtocol( name, pid );
    return null;
  }
  
  public DeviceUpgrade getDeviceUpgrade( Vector< DeviceUpgrade > upgrades )
  {
    for ( DeviceUpgrade upgrade : upgrades )
    {
      if ( upgrade.getProtocol().getID().equals( pid ))
      {
        setupCode = upgrade.getSetupCode();
        deviceType = upgrade.getDeviceType();
        return upgrade;
      }
    }
    return null;
  }
  
  public abstract SpecialProtocolFunction createFunction( KeyMove keyMove );
  public abstract Hex createHex( SpecialFunctionDialog dlg );
  public abstract String[] getFunctions();
  
  public String getName(){ return name; }
  public Hex getPid(){ return pid; }
  public DeviceType getDeviceType(){ return deviceType; }
  public int getSetupCode(){ return setupCode; } 
}
