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
    if ( deviceTypeName != null )
    {
      // When the setup code is "builtin"
      return null;
    }
    for ( DeviceUpgrade upgrade : upgrades )
    {
      if ( upgrade.getProtocol().getID().equals( pid ) )
      {
        setupCode = upgrade.getSetupCode();
        deviceType = upgrade.getDeviceType();
        deviceTypeName = deviceType.getName();
        return upgrade;
      }
    }
    return null;
  }

  public boolean isPresent( RemoteConfiguration config )
  {
    if ( assumePresent )
    {
      return true;
    }

    Remote remote = config.getRemote();
    if ( deviceType == null )
    {
      if ( deviceTypeName != null )
      {
        deviceType = remote.getDeviceType( deviceTypeName );
      }
      else
      {
        DeviceUpgrade deviceUpgrade = getDeviceUpgrade( config.getDeviceUpgrades() );
        if ( deviceUpgrade != null )
        {
          return true;
        }
      }
    }

    if ( deviceTypeName == null )
    {
      if ( getDeviceUpgrade( config.getDeviceUpgrades() ) != null )
      {
        return true;
      }
    }
    if ( deviceType == null )
    {
      deviceType = remote.getDeviceType( deviceTypeName );
    }
    return remote.hasSetupCode( deviceType, setupCode );
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
}
