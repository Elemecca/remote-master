package com.hifiremote.jp1;

public class DirectDefaultValue
 extends DefaultValue
{
  public DirectDefaultValue( Object value )
  {
    this.value = value;
  }
  public Object value()
  {
    return value;
  }

  private Object value;
}