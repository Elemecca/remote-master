package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

// TODO: Auto-generated Javadoc
/**
 * The Class SpecialProtocol.
 */
public abstract class SpecialProtocol
{
  /** The name. */
  private String name = null;

  /** Is the protocol internal? */
  @SuppressWarnings( "unused" )
  private boolean internal = false;

  @SuppressWarnings( "unused" )
  private int internalSerial = 0;

  private String deviceTypeName = null;

  /** The pid. */
  private Hex pid = null;

  /** The device type. */
  private DeviceType deviceType = null;

  /** The setup code. */
  private int setupCode = -1;

  private boolean assumePresent = false;

  public boolean getAssumePresent()
  {
    return assumePresent;
  }

  @SuppressWarnings( "unused" )
  private List< String > userNames = new ArrayList< String >();

  /**
   * Instantiates a new special protocol.
   * 
   * @param name
   *          the name
   * @param pid
   *          the pid
   */
  protected SpecialProtocol( String name, Hex pid )
  {
    this.name = name;
    this.pid = pid;
  }

  /**
   * Creates the.
   * 
   * @param name
   *          the name
   * @param pid
   *          the pid
   * @return the special protocol
   */
  public static SpecialProtocol create( String name, String text )
  {
    int colon = text.indexOf( ':' );
    String prefix = null;
    if ( colon != -1 )
    {
      prefix = text.substring( 0, colon );
      text = text.substring( colon + 1 );
    }

    int paren = text.indexOf( '(' );
    List< String > userNames = null;
    if ( paren != -1 )
    {
      userNames = new ArrayList< String >();

      StringTokenizer st = new StringTokenizer( text.substring( paren ), "(,)" );

      text = text.substring( 0, paren ).trim();

      while ( st.hasMoreTokens() )
      {
        userNames.add( st.nextToken().trim() );
      }
    }

    boolean hasDash = false;
    if ( text.startsWith( "-" ) )
    {
      text = text.substring( 1 );
      hasDash = true;
    }

    Hex pid = new Hex( text );

    SpecialProtocol sp = null;

    if ( name.equals( "DSM" ) )
      sp = new DSMSpecialProtocol( name, pid );
    else if ( name.equals( "UDSM" ) )
      sp = new UDSMSpecialProtocol( name, pid );
    else if ( name.equals( "LDKP" ) )
      sp = new LDKPSpecialProtocol( name, pid );
    else if ( name.equals( "ULDKP" ) )
      sp = new ULDKPSpecialProtocol( name, pid );
    else if ( name.equals( "Multiplex" ) )
      sp = new MultiplexSpecialProtocol( name, pid );
    else if ( name.equals( "Pause" ) )
      sp = new PauseSpecialProtocol( name, pid );
    else if ( name.equals( "ToadTog" ) )
      sp = new ToadTogSpecialProtocol( name, pid );
    else if ( name.equals( "ModeName" ) )
      sp = new ModeNameSpecialProtocol( name, pid );

    sp.assumePresent = hasDash;

    if ( prefix != null )
    {
      if ( prefix.equalsIgnoreCase( "Internal" ) )
      {
        sp.internal = true;
        sp.internalSerial = pid.getData()[ 0 ];
      }
      else
      {
        int slash = prefix.indexOf( '/' );
        sp.deviceTypeName = prefix.substring( 0, slash );
        sp.setupCode = Integer.parseInt( prefix.substring( slash + 1 ) );
      }
    }

    return sp;
  }

  /**
   * Gets the device upgrade.
   * 
   * @param upgrades
   *          the upgrades
   * @return the device upgrade
   */
  public DeviceUpgrade getDeviceUpgrade( List< DeviceUpgrade > upgrades )
  {
    System.err.println( "in getDeviceUpgrade" );
    if ( assumePresent )
    {
      System.err.println( "deviceUpgrade assumed present, returning null" );
      return null;
    }
    for ( DeviceUpgrade upgrade : upgrades )
    {
      System.err.println( "Checking " + upgrade );
      if ( upgrade.getProtocol().getID().equals( pid ) )
      {
        System.err.println( "It's a match!" );
        return upgrade;
      }
    }
    System.err.println( "No match found!" );
    return null;
  }

  public boolean isPresent( RemoteConfiguration config )
  {
    System.err.println( "in isPresent" );
    if ( assumePresent )
    {
      System.err.println( "Assumed present!" );
      return true;
    }

    Remote remote = config.getRemote();

    if ( deviceTypeName != null )
    {
      System.err.println( "deviceTypeName=" + deviceTypeName );
      DeviceType deviceType = remote.getDeviceType( deviceTypeName );
      System.err.println( "deviceType=" + deviceType );
      if ( deviceType == null )
      {
        System.err.println( "DeviceType not found!" );
        return false;
      }
      System.err.println( "Looking for upgrade" );
      if ( config.findDeviceUpgrade( deviceType.getNumber(), setupCode ) != null )
      {
        System.err.println( "Found upgrade" );
        return true;
      }
      System.err.println( "Checking for builtin setupCode:" + deviceType + '/' + setupCode );
      return remote.hasSetupCode( deviceType, setupCode );
    }
    return getDeviceUpgrade( config.getDeviceUpgrades() ) != null;
  }

  /**
   * Creates the function.
   * 
   * @param keyMove
   *          the key move
   * @return the special protocol function
   */
  public abstract SpecialProtocolFunction createFunction( KeyMove keyMove );

  /**
   * Creates the hex.
   * 
   * @param dlg
   *          the dlg
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
  public String getName()
  {
    return name;
  }

  /**
   * Gets the pid.
   * 
   * @return the pid
   */
  public Hex getPid()
  {
    return pid;
  }

  /**
   * Gets the device type.
   * 
   * @return the device type
   */
  public DeviceType getDeviceType()
  {
    return deviceType;
  }

  /**
   * Gets the setup code.
   * 
   * @return the setup code
   */
  public int getSetupCode()
  {
    return setupCode;
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( name );
    sb.append( ':' );
    if ( pid != null )
    {
      sb.append( pid.toString() );
    }
    else if ( deviceTypeName != null )
    {
      sb.append( deviceTypeName );
      sb.append( '/' );
      sb.append( setupCode );
    }
    return sb.toString();
  }
}
