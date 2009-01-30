package com.hifiremote.jp1;


// TODO: Auto-generated Javadoc
/**
 * The Class HexFormatter.
 */
public class HexFormatter
  extends RegexFormatter
{
  
  /**
   * Instantiates a new hex formatter.
   * 
   * @param length the length
   */
  HexFormatter( int length )
  {
    super();
    setValueClass( Hex.class );
    setAllowsInvalid( false );
    setOverwriteMode( true );
    setCommitsOnValidEdit( true );
    setLength( length );
  }

  /**
   * Sets the length.
   * 
   * @param length the new length
   */
  public void setLength( int length )
  {
    StringBuilder buff = new StringBuilder();
    if ( length > 0 )
    {
      buff.append( "\\p{XDigit}{2}" );
      if ( length > 1 )
        buff.append( "( +\\p{XDigit}{2}){" + ( length - 1 ) + "}" );
    }
    setPattern( buff.toString());
  }
}
