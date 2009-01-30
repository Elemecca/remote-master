package com.hifiremote.jp1;

import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class SpecialProtocol.
 */
public abstract class SpecialProtocol
{
  
  /** The name. */
  private String name = null;
  
  /** The pid. */
  private Hex pid = null;
  
  /** The device type. */
  private DeviceType deviceType = null;
  
  /** The setup code. */
  private int setupCode;
  
  /**
   * Instantiates a new special protocol.
   * 
   * @param name the name
   * @param pid the pid
   */
  protected SpecialProtocol( String name, Hex pid )
  {
    this.name = name;
    this.pid = pid;
  }
  
  /**
   * Creates the.
   * 
   * @param name the name
   * @param pid the pid
   * 
   * @return the special protocol
   */
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
  
  /**
   * Gets the device upgrade.
   * 
   * @param upgrades the upgrades
   * 
   * @return the device upgrade
   */
  public DeviceUpgrade getDeviceUpgrade( List< DeviceUpgrade > upgrades )
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
  
  /**
   * Creates the function.
   * 
   * @param keyMove the key move
   * 
   * @return the special protocol function
   */
  public abstract SpecialProtocolFunction createFunction( KeyMove keyMove );
  
  /**
   * Creates the hex.
   * 
   * @param dlg the dlg
   * 
   * @return the hex
   */
  public abstract Hex createHex( SpecialFunctionDialog dlg );
  
  /**
   * Gets the functions.
   * 
   * @return the functions
   */
  public abstract String[] getFunctions();
  
  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName(){ return name; }
  
  /**
   * Gets the pid.
   * 
   * @return the pid
   */
  public Hex getPid(){ return pid; }
  
  /**
   * Gets the device type.
   * 
   * @return the device type
   */
  public DeviceType getDeviceType(){ return deviceType; }
  
  /**
   * Gets the setup code.
   * 
   * @return the setup code
   */
  public int getSetupCode(){ return setupCode; } 
}
