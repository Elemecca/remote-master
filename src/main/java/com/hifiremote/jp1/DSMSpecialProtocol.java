package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class DSMSpecialProtocol.
 */
public class DSMSpecialProtocol extends SpecialProtocol
{
  
  /**
   * Instantiates a new dSM special protocol.
   * 
   * @param name the name
   * @param pid the pid
   */
  public DSMSpecialProtocol( String name, Hex pid )
  {
    super( name, pid );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.SpecialProtocol#createFunction(com.hifiremote.jp1.KeyMove)
   */
  public SpecialProtocolFunction createFunction( KeyMove keyMove )
  {
    return new DSMFunction( keyMove );
  }
  
  public SpecialProtocolFunction createFunction( Macro macro )
  {
    return new DSMFunction( macro );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.SpecialProtocol#createHex(com.hifiremote.jp1.SpecialFunctionDialog)
   */
  public Hex createHex( SpecialFunctionDialog dlg )
  {
    return DSMFunction.createHex( dlg );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.SpecialProtocol#getFunctions()
   */
  public String[] getFunctions(){ return functions; }
  
  /** The Constant functions. */
  private static final String[] functions = { "DSM" };
}
