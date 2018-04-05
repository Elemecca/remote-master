package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class Parameter.
 */
public abstract class Parameter
{
  
  /**
   * Instantiates a new parameter.
   * 
   * @param name the name
   */
  public Parameter( String name )
  {
    this( name, null );
  }

  /**
   * Instantiates a new parameter.
   * 
   * @param name the name
   * @param defaultValue the default value
   */
  public Parameter( String name, DefaultValue defaultValue )
  {
    setName( name );
    this.defaultValue = defaultValue;
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName(){ return name; }
  
  /**
   * Sets the name.
   * 
   * @param aName the new name
   */
  public void setName( String aName )
  {
    int semi = aName.indexOf( ';' );
    if ( semi == -1 )
      name = aName;
    else
    {
      name = aName.substring( 0, semi );
      displayName = aName.substring( semi + 1 );
    }
  }
  
  /**
   * Gets the display name.
   * 
   * @return the display name
   */
  public String getDisplayName()
  {
    if ( displayName != null )
      return displayName;
    return name;
  }

  /**
   * Gets the description.
   * 
   * @return the description
   */
  public abstract String getDescription();

  /**
   * Gets the default value.
   * 
   * @return the default value
   */
  public DefaultValue getDefaultValue()
  {
    return defaultValue;
  }
  
  /**
   * Sets the default.
   * 
   * @param value the new default
   */
  public void setDefault( DefaultValue value ){ defaultValue = value; }
  
  /**
   * Sets the default.
   * 
   * @param value the new default
   */
  public void setDefault( int value )
  {
    setDefault( new DirectDefaultValue( new Integer( value ) ) );
  }

  /**
   * Gets the value.
   * 
   * @return the value
   */
  public abstract Object getValue();

  /**
   * Gets the value or default.
   * 
   * @return the value or default
   */
  public Object getValueOrDefault()
  {
    Object rc = getValue();
    if ( rc != null )
      return rc;
    if ( defaultValue != null )
      return defaultValue.value();
    else
      return null;
  }
  
  /**
   * Sets the value.
   * 
   * @param value the new value
   */
  public abstract void setValue( Object value );

  /** The name. */
  protected String name;
  
  /** The display name. */
  protected String displayName = null;
  
  /** The default value. */
  protected DefaultValue defaultValue;
}
