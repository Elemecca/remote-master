package com.hifiremote.jp1;

public class Value
{
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

  private Object userValue;
  private DefaultValue defaultValue;
}
