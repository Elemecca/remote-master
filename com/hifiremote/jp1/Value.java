package com.hifiremote.jp1;

public class Value
{
  public Value( Object userValue, Object defaultValue )
  {
    this.userValue = userValue;
    this.defaultValue = defaultValue;
  }

  public Object getUserValue(){ return userValue; }
  public Object getDefaultValue(){ return defaultValue; }
  public boolean hasUserValue()
  {
    return userValue != null;
  }

  public Object getValue()
  {
    Object rc = userValue;
    if ( rc == null )
      rc = defaultValue;

    return rc;
  }

  public void setValue( Object value )
  {
    userValue = value;
  }

  private Object userValue;
  private Object defaultValue;
}
