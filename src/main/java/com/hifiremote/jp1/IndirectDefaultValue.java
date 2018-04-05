package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class IndirectDefaultValue.
 */
public class IndirectDefaultValue
 extends DefaultValue
{
  
  /**
   * Instantiates a new indirect default value.
   * 
   * @param index the index
   * @param ref the ref
   */
  public IndirectDefaultValue( int index, Parameter ref )
  {
    this.index = index;
    this.ref = ref;
  }

  /**
   * Sets the checks if is complement.
   * 
   * @param flag the new checks if is complement
   */
  public void setIsComplement( boolean flag )
  {
    complement = flag;
  }

  /**
   * Gets the checks if is complement.
   * 
   * @return the checks if is complement
   */
  public boolean getIsComplement()
  {
    return complement;
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.DefaultValue#value()
   */
  public Object value()
  {
    Integer rc = ( Integer )ref.getValueOrDefault();
    if ( complement )
      rc = new Integer( 255 - rc.intValue());
      
    return rc;
  }

  /** The ref. */
  private Parameter ref;
  
  /** The index. */
  private int index = 0;
  
  /** The complement. */
  private boolean complement = false;

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString(){ return "[" + ( complement ? "-" : "" ) + index + "]"; }
}