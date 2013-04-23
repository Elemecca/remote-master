package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class GeneralFunction
{
  public GeneralFunction() {};
  
  public GeneralFunction( String name )
  {
    this.name = name;
  }
  
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

  public int getDeviceButtonIndex()
  {
    return deviceButtonIndex;
  }
  
  public void setDeviceButtonIndex( int deviceButtonIndex )
  {
    this.deviceButtonIndex = deviceButtonIndex;
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
    if ( this instanceof Macro && !( ( ( Macro )this).isSystemMacro() ) )
    {
      s = "Macro: ";
    }
//    else if ( this instanceof KeyMove )
//    {
//      s = "KM: ";
//    }
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
    if ( db != null && db.getUpgrade() != null && db.getUpgrade().getRemote().isSSD() )
    {
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
    else
    {
      return assigned();
    }
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
  
  public FunctionItem getFunctionItem()
  {
    if ( item == null )
      item = new FunctionItem( this );
    return item;
  }
  
  public boolean hasData()
  {
    if ( data != null )
    {
      return true;
    }
    else if ( !(this instanceof AdvancedCode ) )
    {
      return false;
    }
    else
    {
      AdvancedCode a = ( AdvancedCode )this;
      return a.getItems() != null;
    }
  }
  
  public int getSerial()
  {
    return serial;
  }

  public void setSerial( int serial )
  {
    this.serial = serial;
  }

  
  public DeviceUpgrade getUpgrade( Remote remote )
  {
    if ( this instanceof Function )
    {
      return upgrade;
    }
    DeviceButton db = remote.getDeviceButton( deviceButtonIndex );
    if ( db == null )
    {
      return null;
    }
    return db.getUpgrade();
  }

  public void setUpgrade( DeviceUpgrade upgrade )
  {
    this.upgrade = upgrade;
  }
  
  public boolean accept()
  {
    if ( this instanceof Function )
    {
      Function f = ( Function )this;
      return f.accept();
    }
    else if ( this instanceof Macro )
    {
      Macro m = ( Macro )this;
      return !m.isSystemMacro() && m.getActivity() == null;
    }
    else return true;
    
  }

  public Integer getIconref()
  {
    return iconref;
  }

  public void setIconref( Integer iconref )
  {
    this.iconref = iconref;
  }
  
  @Override
  public String toString()
  {
    if ( this instanceof Function )
    {
      return name;
    }
    else if ( this instanceof Macro )
    {
      return "Macro: " + name;
    }
    else if ( this instanceof LearnedSignal )
    {
      return "Learn: " + name;
    }
    else
    {
      // GeneralFunctions for Selector buttons have no subtype
      return "Button: " + name;
    }
  }


  protected Hex data;
  protected String name = null;
  protected int deviceButtonIndex = 0;
  protected int serial = -1;   // signifies unset
  protected String notes = null;
  protected List< User > users = new ArrayList< User >();
  protected FunctionLabel label = null;
  protected FunctionItem item = null;
  protected DeviceUpgrade upgrade = null;
  protected Integer iconref = null;

}
