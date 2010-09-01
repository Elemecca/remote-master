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
  private boolean internal = false;

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

  private String[] userFunctions;

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
  public static SpecialProtocol create( String name, String text, Remote remote )
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
    {
      sp = new DSMSpecialProtocol( name, pid );
    }
    else if ( name.equals( "UDSM" ) )
    {
      sp = new UDSMSpecialProtocol( name, pid );
    }
    else if ( name.equals( "LDKP" ) )
    {
      sp = new LDKPSpecialProtocol( name, pid );
    }
    else if ( name.equals( "ULDKP" ) )
    {
      sp = new ULDKPSpecialProtocol( name, pid );
    }
    else if ( name.equals( "Multiplex" ) )
    {
      sp = new MultiplexSpecialProtocol( name, pid );
    }
    else if ( name.equals( "Pause" ) )
    {
      sp = new PauseSpecialProtocol( name, pid );
    }
    else if ( name.equals( "ToadTog" ) )
    {
      sp = new ToadTogSpecialProtocol( name, pid );
    }
    else if ( name.equals( "ModeName" ) )
    {
      sp = new ModeNameSpecialProtocol( name, pid );
    }

    if ( sp != null )
    {
      sp.userFunctions = sp.getFunctions().clone();
      if ( userNames != null )
      {
        for ( int i = 0; i < sp.userFunctions.length; i++ )
        {
          if ( i < userNames.size() )
          {
            sp.userFunctions[ i ] = userNames.get( i );
          }
        }
      }
    }
    
    if ( name.equals( "Pause" ) )
    {
      ( ( PauseSpecialProtocol )sp ).setPauseParameters( remote );
    }

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
        if ( sp.deviceTypeName != null )
        {
          System.err.println( "deviceTypeName=" + sp.deviceTypeName );
          sp.deviceType = remote.getDeviceType( sp.deviceTypeName );
          System.err.println( "deviceType=" + sp.deviceType );
        }
      }
    }

    return sp;
  }

  public void checkSpecialProtocol( Remote remote )
  {
    if ( deviceTypeName != null && deviceType == null )
    {
      deviceType = remote.getDeviceType( deviceTypeName );
    }
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
    /*
     * GD: Commented out lines below as they are based on a misunderstanding of "assumePresent". It is the protocol, not
     * the device upgrade, that is assumed present. It is used when the PID given in the Special Protocol RDF entry does
     * not correspond to a protocol upgrade. If the Special Protocol entry does not give a device type and setup code in
     * a prefix then the PID must correspond to a device upgrade, regardless of whether or not it is marked
     * "assumePresent".
     */
    // if ( assumePresent )
    // {
    // System.err.println( "deviceUpgrade assumed present, returning null" );
    // return null;
    // }
    if ( internal )
    {
      return null;
    }

    for ( DeviceUpgrade upgrade : upgrades )
    {
      if ( upgrade != null )
      {
        System.err.println( "Checking " + upgrade );
        // Allow for 4-byte XPIDs
        if ( upgrade.getProtocol().getID().equals( pid.subHex( 0, 2 ) ) )
        {
          if ( pid.getData().length == 4 )
          {
            // pid is an XPID, so check protocol code for match
            if ( upgrade.getCode().getData()[ pid.getData()[ 2 ] ] != pid.getData()[ 3 ] )
            {
              System.err.println( "PID matched but failed XPID check" );
              continue;
            }
          }
          System.err.println( "It's a match!" );
          return upgrade;
        }
      }
    }
    System.err.println( "No match found!" );
    return null;
  }

  public boolean isInternal()
  {
    return internal;
  }

  public int getInternalSerial()
  {
    return internalSerial;
  }

  public boolean isPresent( RemoteConfiguration config )
  {
    if ( internal )
    {
      System.err.println( "Present because internal" );
      return true;
    }

    if ( deviceTypeName != null )
    {
      System.err.println( "Device upgrade present because built in" );
      // Still need to check protocol
      if ( assumePresent )
      {
        System.err.println( "Protocol assumed built in" );
        return true;
      }
      // Check if present among protocols not used by device upgrades
      int spID = pid.get( 0 );
      System.err.println( "Seeking protocol upgrade " + spID );
      for ( ProtocolUpgrade protocol : config.getProtocolUpgrades() )
      {
        if ( protocol.getPid() == spID )
        {
          if ( pid.getData().length == 4 && protocol.getCode().getData()[ pid.getData()[ 2 ] ] != pid.getData()[ 3 ] )
          {
            // pid is an XPID and the XPID check failed
            return false;
          }
          System.err.println( "Found protocol upgrade" );
          return true;
        }
      }
      return false;
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

  public abstract SpecialProtocolFunction createFunction( Macro macro );

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

  @Override
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

  public String[] getUserFunctions()
  {
    return userFunctions;
  }

}
