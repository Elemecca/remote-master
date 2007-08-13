package com.hifiremote.jp1;

public class Value
{
  public Value( int userInt )
  {
    this( new Integer( userInt ));
  }

  public Value( Object userValue )
  {
    this( userValue, null );
  }

  public Value( Object userValue, DefaultValue defaultValue )
  {
    this.userValue = userValue;
    this.defaultValue = defaultValue;
  }

  public Object getUserValue(){ return userValue; }
  public DefaultValue getDefaultValue(){ return defaultValue; }
  public boolean hasUserValue()
  {
    return userValue != null;
  }

  public Object getValue()
  {
    Object rc = userValue;
    if ( rc == null )
    {
      if ( defaultValue != null )
        rc = defaultValue.value();
    }
    return rc;
  }

  public void setValue( Object value )
  {
    userValue = value;
  }

  public void setDefaultValue( DefaultValue defaultValue )
  {
    this.defaultValue = defaultValue;
  }
  
  public String toString()
  {
    Object rc = getValue();
    if ( rc == null )
      return null;
    else
      return rc.toString();
  }

  private Object userValue;
  private DefaultValue defaultValue;
}
