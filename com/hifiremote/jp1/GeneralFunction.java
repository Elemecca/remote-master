package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class GeneralFunction
{
  /**
   * The Class User.
   */
  public static class User
  {

    public User( Button b, int state )
    {
      button = b;
      this.state = state;
      db = null;
    }
    
    public User( DeviceButton db, Button button )
    {
      this.db = db;
      this.button = button;
      this.state = Button.NORMAL_STATE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals( Object o )
    {
      User u = ( User )o;
      if ( db != u.db )
        return false;
      if ( button != u.button )
        return false;
      if ( state != u.state )
        return false;
      return true;
    }

    public Button button;
    public int state;
    public DeviceButton db;
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
  
  public String getDisplayName()
  {
    String s = "";
    if ( this instanceof Macro )
    {
      s = "Macro: ";
    }
    else if ( this instanceof KeyMove )
    {
      s = "KM: ";
    }
    else if ( this instanceof LearnedSignal )
    {
      s = "Learn: ";
    }
    return s += name;
  }
  
  public boolean assigned()
  {
    return ( !users.isEmpty() );
  }
  
  public boolean assigned( DeviceButton db )
  {
    // A learned signal hides anything underneath, so treat as unassigned
    // if all assignments are hidden
    for ( User u : users )
    {
      LinkedHashMap< Integer, LearnedSignal > learnedMap = db.getUpgrade().getLearnedMap();
      if ( ( this instanceof Function || u.db == db )
          && ( this instanceof LearnedSignal || learnedMap.get( ( int )u.button.getKeyCode()) == null ) )
      {
        return true;
      }
    }
    return false;
  }


  public List< User > getUsers()
  {
    return users;
  }
  

  public void addReference( DeviceButton db, Button b )
  {
    users.add( new User( db, b ) );
    if ( label != null )
    {
      label.showAssigned();
      label.updateToolTipText();
    }
  }
  

  public void removeReference( DeviceButton db, Button b )
  {
    users.remove( new User( db, b ) );
    if ( label != null )
    {
      label.showAssigned( db );
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
  
  public FunctionItem getItem()
  {
    if ( item == null )
      item = new FunctionItem( this );
    return item;
  }
  
  protected Hex data;
  protected String name;
  protected String notes = null;
  protected List< User > users = new ArrayList< User >();
  protected FunctionLabel label = null;
  protected FunctionItem item = null;

}
