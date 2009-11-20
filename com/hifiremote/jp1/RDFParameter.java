/**
 * 
 */
package com.hifiremote.jp1;

/**
 * @author Greg
 */
public abstract class RDFParameter
{
  public abstract void parse( String text, Remote remote ) throws Exception;

  public void store( short[] data, Remote remote )
  {}
}
