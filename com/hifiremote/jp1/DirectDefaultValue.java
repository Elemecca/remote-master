package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class DirectDefaultValue.
 */
public class DirectDefaultValue
 extends DefaultValue
{
  
  /**
   * Instantiates a new direct default value.
   * 
   * @param value the value
   */
  public DirectDefaultValue( Object value )
  {
    this.value = value;
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.DefaultValue#value()
   */
  public Object value()
  {
    return value;
  }

  /** The value. */
  private Object value;

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString(){ return value.toString(); }
}
