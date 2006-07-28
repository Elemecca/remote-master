package com.hifiremote.jp1;

import java.util.Properties;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

public class Function
{
  public Function(){}

  public Function( String name, Hex hex, String notes )
  {
    this.name = name;
    this.hex = hex;
    this.notes = notes;
  }

  public Function( String name )
  {
    this.name = name;
  }
  
  public Function( Function base )
  {
    name = base.name;
    if ( base.hex != null )
      hex = new Hex( base.hex );
    notes = base.notes;
  }

  public boolean isExternal(){ return false; }

  public boolean isEmpty()
  {
    return ( name == null ) && ( hex == null ) && ( notes == null );
  }

  public void store( Properties props, String prefix )
  {
    if ( isEmpty())
      props.setProperty( prefix + ".name", "" );

    if ( name != null )
      props.setProperty( prefix + ".name", name );
    if ( hex != null )
      props.setProperty( prefix + ".hex", hex.toString());
    if ( notes != null )
      props.setProperty( prefix + ".notes", notes );
  }

  public void store( PropertyWriter out, String prefix )
  {
    if ( isEmpty())
      out.print( prefix + ".name", "" );

    if ( name != null )
      out.print( prefix + ".name", name );
    if ( hex != null )
      out.print( prefix + ".hex", hex.toString());
    if ( notes != null )
      out.print( prefix + ".notes", notes );
  }

  public void load( Properties props, String prefix )
  {
    String str = props.getProperty( prefix + ".name" );
    if ( str != null )
      setName( str );
    str = props.getProperty( prefix + ".hex" );
    if ( str != null )
      setHex( new Hex( str ));
    str = props.getProperty( prefix + ".notes" );
    if ( str != null )
      setNotes( str );
  }

  public Function setName( String name )
  {
    this.name = name;
    if ( label != null )
      label.setText( name );
    if ( item != null )
      item.setText( name );
    return this;
  }

  public Function setNotes( String notes )
  {
    this.notes = notes;
    if ( item != null )
      item.setToolTipText( notes );
    if ( label != null )
      label.updateToolTipText();
    return this;
  }

  public Function setHex( Hex hex )
  {
    this.hex = hex;
    return this;
  }

  public String toString()
  {
    return name;
  }
  public String getName(){ return name; }
  public String getNotes(){ return notes; }
  public Hex getHex(){ return hex; }

  public FunctionLabel getLabel()
  {
    if ( label == null )
    {
      label = new FunctionLabel( this );
      label.updateToolTipText();
      if ( assigned())
        label.showAssigned();
    }
    return label;
  }

  public FunctionItem getItem()
  {
    if ( item == null )
      item = new FunctionItem( this );
    return item;
  }

  public void addReference( Button b, int state )
  {
    users.add( new User( b, state ));
    if ( label != null )
    {
      label.showAssigned();
      label.updateToolTipText();
    }
  }

  public void removeReference( Button b, int state )
  {
    users.remove( new User( b, state ));
    if ( label != null )
    {
      if ( users.isEmpty())
        label.showUnassigned();
      label.updateToolTipText();
    }
  }

  public boolean assigned()
  {
    return ( !users.isEmpty() );
  }

  public Enumeration< User > getUsers()
  {
    return users.elements();
  }

  public class User
  {
    public User( Button b, int state )
    {
      button = b;
      this.state = state;
    }

    public boolean equals( Object o )
    {
      User u = ( User )o;
      boolean rc = true;
      if ( button != u.button )
        return false;
      if ( state != u.state )
        return false;
      return true;
    }
    
    public Button button;
    public int state; 
  }

  protected String name = null;
  protected String notes = null;
  protected Hex hex = null;
  private FunctionLabel label = null;
  private FunctionItem item = null;
  private Vector< User > users = new Vector< User >();
}
