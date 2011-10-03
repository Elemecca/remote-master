package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class Function.
 */
public class Function
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
    this.hex = hex;
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
    if ( base.hex != null )
      hex = new Hex( base.hex );
    notes = base.notes;
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
    return ( name == null ) && ( hex == null ) && ( notes == null );
  }

  /**
   * Store.
   * 
   * @param props
   *          the props
   * @param prefix
   *          the prefix
   */
  public void store( Properties props, String prefix )
  {
    if ( isEmpty() )
      props.setProperty( prefix + ".name", "" );

    if ( name != null )
      props.setProperty( prefix + ".name", name );
    if ( hex != null )
      props.setProperty( prefix + ".hex", hex.toString() );
    if ( notes != null )
      props.setProperty( prefix + ".notes", notes );
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
    if ( hex != null )
      out.print( prefix + ".hex", hex.toString() );
    if ( notes != null )
      out.print( prefix + ".notes", notes );
  }

  /**
   * Load.
   * 
   * @param props
   *          the props
   * @param prefix
   *          the prefix
   */
  public void load( Properties props, String prefix )
  {
    String str = props.getProperty( prefix + ".name" );
    if ( str != null )
      setName( str );
    str = props.getProperty( prefix + ".hex" );
    if ( str != null )
      setHex( new Hex( str ) );
    str = props.getProperty( prefix + ".notes" );
    if ( str != null )
      setNotes( str );
  }

  /**
   * Sets the name.
   * 
   * @param name
   *          the name
   * @return the function
   */
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
   * Sets the hex.
   * 
   * @param hex
   *          the hex
   * @return the function
   */
  public Function setHex( Hex hex )
  {
    this.hex = hex;
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
   * Gets the name.
   * 
   * @return the name
   */
  public String getName()
  {
    return name;
  }

  /**
   * Gets the notes.
   * 
   * @return the notes
   */
  public String getNotes()
  {
    return notes;
  }

  /**
   * Gets the hex.
   * 
   * @return the hex
   */
  public Hex getHex()
  {
    return hex;
  }

  /**
   * Gets the label.
   * 
   * @return the label
   */
  public FunctionLabel getLabel()
  {
    if ( label == null )
    {
      label = new FunctionLabel( this );
      label.updateToolTipText();
      if ( assigned() )
        label.showAssigned();
    }
    return label;
  }

  /**
   * Gets the item.
   * 
   * @return the item
   */
  public FunctionItem getItem()
  {
    if ( item == null )
      item = new FunctionItem( this );
    return item;
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
    users.add( new User( b, state ) );
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

  public void removeReferences()
  {
    users.clear();
    if ( label != null )
    {
      label.showUnassigned();
      label.updateToolTipText();
    }
  }

  /**
   * Assigned.
   * 
   * @return true, if successful
   */
  public boolean assigned()
  {
    return ( !users.isEmpty() );
  }

  /**
   * Gets the users.
   * 
   * @return the users
   */
  public List< User > getUsers()
  {
    return users;
  }

  /**
   * The Class User.
   */
  public class User
  {

    /**
     * Instantiates a new user.
     * 
     * @param b
     *          the b
     * @param state
     *          the state
     */
    public User( Button b, int state )
    {
      button = b;
      this.state = state;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals( Object o )
    {
      User u = ( User )o;
      if ( button != u.button )
        return false;
      if ( state != u.state )
        return false;
      return true;
    }

    /** The button. */
    public Button button;

    /** The state. */
    public int state;
  }

  /** The name. */
  protected String name = null;

  /** The notes. */
  protected String notes = null;

  /** The hex. */
  protected Hex hex = null;

  /** The label. */
  private FunctionLabel label = null;

  /** The item. */
  private FunctionItem item = null;

  /** The users. */
  private List< User > users = new ArrayList< User >();
}
