package com.hifiremote.jp1;

public abstract class Parameter
{
  public Parameter( String name )
  {
    this( name, null );
  }

  public Parameter( String name, DefaultValue defaultValue )
  {
    this.name = name;
    this.defaultValue = defaultValue;
  }

  public String getName(){ return name; }
  public Object getDefaultValue()
  {
    return (defaultValue ==null) ? null : defaultValue.value();
  }
  public void setDefault( DefaultValue value ){ defaultValue = value; }
  public void setDefault( int value )
  {
    setDefault( new DirectDefaultValue( new Integer( value ) ) );
  }
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
  private DefaultValue defaultValue;
}
