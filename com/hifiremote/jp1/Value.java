package com.hifiremote.jp1;

public class Value
{
  public Value( Integer userValue, Integer defaultValue )
  {
    this.userValue = userValue;
    this.defaultValue = defaultValue;
  }

  public Integer getUserValue(){ return userValue; }
  public Integer getDefaultValue(){ return defaultValue; }
  public boolean hasUserValue()
  {
    return userValue != null;
  }

  public Integer getValue()
  {
    Integer rc = userValue;
    if ( rc == null )
      rc = defaultValue;

    return rc;
  }

  public void setValue( Integer value )
  {
    userValue = value;
  }

  private Integer userValue;
  private Integer defaultValue;
}
