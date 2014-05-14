package com.hifiremote.jp1;

import java.util.List;
import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class Function.
 */
public class Function extends GeneralFunction
{

  /**
   * Instantiates a new function.
   */
  public Function()
  {}

  /**
   * Instantiates a new function.
   * 
   * @param name
   *          the name
   * @param hex
   *          the hex
   * @param notes
   *          the notes
   */
  public Function( String name, Hex hex, String notes )
  {
    this.name = name;
    data = hex;
    this.notes = notes;
  }

  /**
   * Instantiates a new function.
   * 
   * @param name
   *          the name
   */
  public Function( String name )
  {
    this.name = name;
  }

  /**
   * Instantiates a new function.
   * 
   * @param base
   *          the base
   */
  public Function( Function base )
  {
    name = base.name;
    gid = base.gid;
    if ( base.data != null )
      data = new Hex( base.data );
    notes = base.notes;
    upgrade = base.upgrade;
    serial = base.serial;
    keyflags = base.keyflags;
    if ( base.icon != null )
//      icon = new RMIcon( base.icon );
      icon = base.icon;  // *** not sure why it needed to be copied
  }

  /**
   * Checks if is external.
   * 
   * @return true, if is external
   */
  public boolean isExternal()
  {
    return false;
  }

  /**
   * Checks if is empty.
   * 
   * @return true, if is empty
   */
  public boolean isEmpty()
  {
    return ( name == null ) && ( data == null ) && ( notes == null );
  }

  /**
   * Store.
   * 
   * @param out
   *          the out
   * @param prefix
   *          the prefix
   */
  public void store( PropertyWriter out, String prefix )
  {
    if ( isEmpty() )
      out.print( prefix + ".name", "" );

    if ( name != null )
      out.print( prefix + ".name", name );
    if ( gid != null && gid != defaultGID )
      out.print( prefix + ".index", Integer.toString( gid ) );
    if ( keyflags != null )
      out.print( prefix + ".keyflags", Integer.toString( keyflags ) );
    if ( serial >= 0 )
      out.print( prefix + ".serial", Integer.toString( serial ) );
    if ( data != null )
      out.print( prefix + ".hex", data.toString() );
    if ( icon != null && icon.ref > 0 )
    {
      out.print( prefix + ".iconref", Integer.toString( icon.ref ) );
    }
    if ( notes != null )
      out.print( prefix + ".notes", notes );
  }
  
  /**
   * Return value is the function macroref, now no longer kept as
   * a function property or written by store() but retained for backward
   * compatibility with earlier .rmir files and used by DeviceUpgrade.load.
   */
  public Integer load( Properties props, String prefix )
  {
    Integer macroref = null;
    String str = props.getProperty( prefix + ".name" );
    if ( str != null )
      setName( str );
    str = props.getProperty( prefix + ".index" );
    if ( str != null )
    {
      setGid( Integer.parseInt( str ) );
    }
    str = props.getProperty( prefix + ".keyflags" );
    if ( str != null )
      setKeyflags( Integer.parseInt( str ) );
    str = props.getProperty( prefix + ".serial" );
    if ( str != null )
      setSerial( Integer.parseInt( str ) );
    str = props.getProperty( prefix + ".hex" );
    if ( str != null )
      setHex( new Hex( str ) );
    str = props.getProperty( prefix + ".macroref" );
    if ( str != null )
      macroref = Integer.parseInt( str );
    str = props.getProperty( prefix + ".notes" );
    if ( str != null )
      setNotes( str );
    return macroref;
  }

  public Function setName( String name )
  {
    this.name = name;
    if ( label != null )
    {
      label.setText( name );
      label.updateToolTipText();
    }
    if ( item != null )
      item.setText( name );
    return this;
  }

  public Integer getGid()
  {
    return gid;
  }

  public void setGid( Integer gid )
  {
    this.gid = gid;
  }

  /**
   * Sets the notes.
   * 
   * @param notes
   *          the notes
   * @return the function
   */
  public Function setNotes( String notes )
  {
    this.notes = notes;
    if ( item != null )
      item.setToolTipText( notes );
    if ( label != null )
      label.updateToolTipText();
    return this;
  }

  /**
   * Sets the data.
   * 
   * @param hex
   *          the hex
   * @return the function
   */
  public Function setHex( Hex hex )
  {
    this.data = hex;
    return this;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return name;
  }

  /**
   * Gets the hex.
   * 
   * @return the hex
   */
  public Hex getHex()
  {
    return data;
  }

  /**
   * Adds the reference.
   * 
   * @param b
   *          the b
   * @param state
   *          the state
   */
  public void addReference( Button b, int state )
  {
    User user = new User( b, state );
    if ( !users.contains( user ) )
    {
      users.add( user );
    }
    if ( label != null )
    {
      label.showAssigned();
      label.updateToolTipText();
    }
  }

  /**
   * Removes the reference.
   * 
   * @param b
   *          the b
   * @param state
   *          the state
   */
  public void removeReference( Button b, int state )
  {
    users.remove( new User( b, state ) );
    if ( label != null )
    {
      if ( users.isEmpty() )
        label.showUnassigned();
      label.updateToolTipText();
    }
  }
  
  /**
   * Returns true if the function has no other assignment than as the base
   * for a macro
   */
  public boolean isMacroBase()
  {
    if ( data != null )
    {
      return false;
    }
    for ( User u : users )
    {
      RemoteConfiguration config = null;
      boolean hasMacro = false;
      if ( u.db == null || u.db == DeviceButton.noButton || u.db.getUpgrade() == null 
          || ( config = u.db.getUpgrade().getRemoteConfig() ) == null )
      {
        return false;
      }
      for ( Macro macro : config.getMacros() )
      {
        for( User mu : macro.getUsers() )
        {
          if ( mu.equals( u ) )
          {
            hasMacro = true;
            break;
          }
        }
        if ( hasMacro )
        {
          break;
        }
      }
      if ( !hasMacro )
      {
        return false;
      }
    }
    return true;
  }

  public Integer getKeyflags()
  {
    return keyflags;
  }

  public void setKeyflags( Integer keyflags )
  {
    this.keyflags = keyflags;
  }
  
  public boolean isEquivalent( Function f )
  {
    // There appear to be functions that differ only in the keygid
    // but as the keygid seems not to be used by the remote, they are
    // treated here as equivalent
    if ( data == null || f.data == null )
    {
      return false;
    }
    return name.equals( f.name )
        && upgrade == f.upgrade
        && data.equals( f.data );
  }
  
  public Function getEquivalent( List< Function > list )
  {
    for ( Function f : list )
    {
      if ( this.isEquivalent( f ) )
      {
        return f;
      }
    }
    return null;
  }
  
  public boolean accept()
  {
    return data != null;
  }
  
//  public void assignSerial()
//  {
//    if ( serial < 0 )
//    {
//      serial = upgrade.getNewFunctionSerial();
//      upgrade.getFunctionMap().put( serial, this );
//    }
//  }
  
//  public Function getIRfunction( DeviceUpgrade du )
//  {
//    if ( serial < 0 )
//    {
//      serial = du.getNewFunctionSerial();
//      du.getFunctionMap().put( serial, this );
//    }
//    return this;
//  }

  public int getRmirIndex()
  {
    return rmirIndex;
  }

  public void setRmirIndex( int rmirIndex )
  {
    this.rmirIndex = rmirIndex;
  }

  /** The EZ-RC GID value corresponding to this function name */
  protected Integer gid = null;
  
  private Integer keyflags = null;
  
  private int rmirIndex = -1;
  
  /** Default value used in upgrade when index==null */
  public static final int defaultGID = 0;

}
