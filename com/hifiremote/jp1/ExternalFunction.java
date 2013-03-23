package com.hifiremote.jp1;

import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class ExternalFunction.
 */
public class ExternalFunction extends Function
{

  /**
   * Instantiates a new external function.
   */
  public ExternalFunction()
  {
    super();
  }

  /**
   * Instantiates a new external function.
   * 
   * @param base
   *          the base
   */
  public ExternalFunction( ExternalFunction base )
  {
    super( base );
    deviceTypeAliasName = base.deviceTypeAliasName;
    type = base.type;
    setupCode = base.setupCode;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Function#isExternal()
   */
  @Override
  public boolean isExternal()
  {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Function#isEmpty()
   */
  @Override
  public boolean isEmpty()
  {
    return super.isEmpty() && deviceTypeAliasName == null && setupCode == 0 && type == EFCType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Function#store(com.hifiremote.jp1.PropertyWriter, java.lang.String)
   */
  @Override
  public void store( PropertyWriter out, String prefix )
  {
    super.store( out, prefix );
    out.print( prefix + ".type", Integer.toString( type ) );
    if ( deviceTypeAliasName != null )
    {
      out.print( prefix + ".deviceType", deviceTypeAliasName );
    }
    out.print( prefix + ".setupCode", Integer.toString( setupCode ) );
  }

  /**
   * Load.
   * 
   * @param props
   *          the props
   * @param prefix
   *          the prefix
   * @param remote
   *          the remote
   */
  public void load( Properties props, String prefix, Remote remote )
  {
    super.load( props, prefix );
    String str = props.getProperty( prefix + ".type" );
    if ( str != null )
    {
      setType( new Integer( str ) );
    }
    str = props.getProperty( prefix + ".deviceType" );
    if ( str != null )
    {
      for ( String name : remote.getDeviceTypeAliasNames() )
      {
        if ( name.equalsIgnoreCase( str ) )
        {
          setDeviceTypeAliasName( name );
          break;
        }
      }
    }
    str = props.getProperty( prefix + ".setupCode" );
    if ( str != null )
    {
      setSetupCode( Integer.parseInt( str ) );
    }
  }

  /**
   * Gets the device type alias name.
   * 
   * @return the device type alias name
   */
  public String getDeviceTypeAliasName()
  {
    return deviceTypeAliasName;
  }

  /**
   * Sets the device type alias name.
   * 
   * @param name
   *          the new device type alias name
   */
  public void setDeviceTypeAliasName( String name )
  {
    deviceTypeAliasName = name;
  }

  /** The Constant EFCType. */
  public final static int EFCType = 0;

  /** The Constant HexType. */
  public final static int HexType = 1;

  /*
   * public String toString() { String rc = ""; Object o = getValue(); if ( o != null ) rc = o.toString(); return rc; }
   */
  /**
   * Sets the type.
   * 
   * @param type
   *          the new type
   */
  public void setType( int type )
  {
    this.type = type;
  }

  /**
   * Sets the type.
   * 
   * @param type
   *          the new type
   */
  public void setType( Integer type )
  {
    setType( type.intValue() );
  }

  /**
   * Gets the type.
   * 
   * @return the type
   */
  public int getType()
  {
    return type;
  }

  /**
   * Sets the value.
   * 
   * @param value
   *          the new value
   */
  public void setValue( Object value )
  {
    if ( value == null )
    {
      setHex( null );
    }
    else if ( type == EFCType )
    {
      setEFC( ( EFC )value );
    }
    else
    {
      setHex( ( Hex )value );
    }
  }

  /**
   * Gets the value.
   * 
   * @return the value
   */
  public Object getValue()
  {
    if ( type == EFCType )
    {
      return getEFC();
    }
    else
    {
      return getHex();
    }
  }

  /**
   * Sets the setup code.
   * 
   * @param code
   *          the new setup code
   */
  public void setSetupCode( int code )
  {
    setupCode = code;
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

  /**
   * Gets the eFC.
   * 
   * @return the eFC
   */
  public EFC getEFC()
  {
    EFC rc = null;
    if ( data != null )
    {
      rc = new EFC( data, 0 );
    }
    return rc;
  }

  /**
   * Sets the eFC.
   * 
   * @param efc
   *          the new eFC
   */
  public void setEFC( EFC efc )
  {
    if ( efc != null )
    {
      efc.toHex( data, 0 );
    }
    else
    {
      data = null;
    }
  }

  /** The device type alias name. */
  private String deviceTypeAliasName;

  /** The setup code. */
  private int setupCode;

  /** The type. */
  private int type;
}
