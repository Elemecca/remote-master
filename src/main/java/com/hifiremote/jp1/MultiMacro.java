/**
 * 
 */
package com.hifiremote.jp1;

import java.util.StringTokenizer;

/**
 * @author Greg
 */
public class MultiMacro extends RDFParameter
{
  private int address1 = 0;
  private int address2 = 0;
  private int count = 0;

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.RDFParameter#parse(java.lang.String)
   */
  public void parse( String text, Remote remote ) throws Exception
  {
    StringTokenizer st = new StringTokenizer( text, "=," );
    String buttonName = st.nextToken();
    remote.getButton( buttonName ).setMultiMacro( this );
    address1 = RDFReader.parseNumber( st.nextToken() );
    if ( st.hasMoreTokens() )
    {
      address2 = RDFReader.parseNumber( st.nextToken() );
    }
  }

  public void setCount( int count )
  {
    this.count = count;
  }

  public void store( short[] data, Remote remote )
  {
    if ( address2 == 0 )
    {
      data[ address1 ] = ( short )( ( count << 4 | 1 ) & 0xFF );
    }
    else
    {
      data[ address1 ] = ( short )( count & 0xFF );
      data[ address2 ] = 1;
    }
  }
}
