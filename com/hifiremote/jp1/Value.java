package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class Value.
 */
public class Value
{
  
  /**
   * Instantiates a new value.
   * 
   * @param userInt the user int
   */
  public Value( int userInt )
  {
    this( new Integer( userInt ));
  }

  /**
   * Instantiates a new value.
   * 
   * @param userValue the user value
   */
  public Value( Object userValue )
  {
    this( userValue, null );
  }

  /**
   * Instantiates a new value.
   * 
   * @param userValue the user value
   * @param defaultValue the default value
   */
  public Value( Object userValue, DefaultValue defaultValue )
  {
    this.userValue = userValue;
    this.defaultValue = defaultValue;
  }

  /**
   * Gets the user value.
   * 
   * @return the user value
   */
  public Object getUserValue(){ return userValue; }
  
  /**
   * Gets the default value.
   * 
   * @return the default value
   */
  public DefaultValue getDefaultValue(){ return defaultValue; }
  
  /**
   * Checks for user value.
   * 
   * @return true, if successful
   */
  public boolean hasUserValue()
  {
    return userValue != null;
  }

  /**
   * Gets the value.
   * 
   * @return the value
   */
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

  /**
   * Sets the value.
   * 
   * @param value the new value
   */
  public void setValue( Object value )
  {
    userValue = value;
  }

  /**
   * Sets the default value.
   * 
   * @param defaultValue the new default value
   */
  public void setDefaultValue( DefaultValue defaultValue )
  {
    this.defaultValue = defaultValue;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    Object rc = getValue();
    if ( rc == null )
      return null;
    else
      return rc.toString();
  }

  /** The user value. */
  private Object userValue;
  
  /** The default value. */
  private DefaultValue defaultValue;
}
