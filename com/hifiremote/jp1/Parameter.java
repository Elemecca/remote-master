package com.hifiremote.jp1;

public abstract class Parameter
{
  public Parameter( String name )
  {
    this( name, null );
  }

  public Parameter( String name, Object defaultValue )
  {
    this.name = name;
    this.defaultValue = defaultValue;
  }

  public String getName(){ return name; }
  public Object getDefaultValue(){ return defaultValue; }
  public void setDefault( Object value ){ defaultValue = value; }
  public abstract Object getValue();

  public Object getValueOrDefault()
  {
    Object rc = getValue();
    if ( rc != null )
      return rc;
    return getDefaultValue();
  }
  public abstract void setValue( Object value );

  private String name;
  private Object defaultValue;
}
