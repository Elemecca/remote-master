package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class PauseSpecialProtocol.
 */
public class PauseSpecialProtocol extends SpecialProtocol
{
  
  /**
   * Instantiates a new pause special protocol.
   * 
   * @param name the name
   * @param pid the pid
   */
  public PauseSpecialProtocol( String name, Hex pid )
  {
    super( name, pid );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.SpecialProtocol#createFunction(com.hifiremote.jp1.KeyMove)
   */
  public SpecialProtocolFunction createFunction( KeyMove keyMove )
  {
    return new PauseFunction( keyMove );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.SpecialProtocol#createHex(com.hifiremote.jp1.SpecialFunctionDialog)
   */
  public Hex createHex( SpecialFunctionDialog dlg )
  {
    return PauseFunction.createHex( dlg );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.SpecialProtocol#getFunctions()
   */
  public String[] getFunctions(){ return functions; }
  
  /** The Constant functions. */
  private static final String[] functions = { "Pause" };
}
