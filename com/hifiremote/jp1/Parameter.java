package com.hifiremote.jp1;

public abstract class Parameter
{
  public Parameter( String name )
  {
    this( name, null );
  }

  public Parameter( String name, Integer defaultValue )
  {
    this.name = name;
    this.defaultValue = defaultValue;
  }

  public String getName(){ return name; }
  public Integer getDefaultValue(){ return defaultValue; }
  public void setDefault( Integer value ){ defaultValue = value; }
  public abstract Integer getValue();

  public Integer getValueOrDefault()
  {
    Integer rc = getValue();
    if (rc != null)
      return rc;
    return getDefaultValue();
  }
  public abstract void setValue( Integer value );

  private String name;
  private Integer defaultValue;
}
