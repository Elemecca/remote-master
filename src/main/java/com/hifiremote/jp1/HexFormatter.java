package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class HexFormatter.
 */
public class HexFormatter extends RegexFormatter
{

  /**
   * Instantiates a new hex formatter.
   * 
   * @param length
   *          the length
   */
  HexFormatter( int length )
  {
    super();
    setValueClass( Hex.class );
    setAllowsInvalid( true );
    setOverwriteMode( true );
    setCommitsOnValidEdit( false );
    setLength( length );
  }

  /**
   * Sets the length.
   * 
   * @param length
   *          the new length
   */
  public void setLength( int length )
  {
    if ( length > 0 )
    {
      StringBuilder buff = new StringBuilder();
      buff.append( "\\p{XDigit}{2}" );
      if ( length > 1 )
      {
        buff.append( "( +\\p{XDigit}{2}){" + ( length - 1 ) + "}" );
      }
      setPattern( buff.toString() );
    }
    else
    {
      setPattern( "(\\p{XDigit}{2}( \\p{XDigit}{2})*)?" );
    }
  }
}
