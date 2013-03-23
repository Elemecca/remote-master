package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.List;

public abstract class GeneralFunction
{
  /**
   * The Class User.
   */
  public static class User
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
  
  public Hex getData()
  {
    return data;
  }

  public void setData( Hex hex )
  {
    data = hex;
  }

  public String getNotes()
  {
    return notes;
  }
  
  public String getName()
  {
    return name;
  }
  
  public boolean assigned()
  {
    return ( !users.isEmpty() );
  }


  public List< User > getUsers()
  {
    return users;
  }
  

  public void addReference( Button b )
  {
    users.add( new User( b, Button.NORMAL_STATE ) );
    if ( label != null )
    {
      label.showAssigned();
      label.updateToolTipText();
    }
  }
  

  public void removeReference( Button b )
  {
    users.remove( new User( b, Button.NORMAL_STATE ) );
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
  
  protected Hex data;
  protected String name;
  protected String notes = null;
  protected List< User > users = new ArrayList< User >();
  protected FunctionLabel label = null;

}
