package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class LDKPSpecialProtocol.
 */
public class LDKPSpecialProtocol extends SpecialProtocol
{
  
  /**
   * Instantiates a new lDKP special protocol.
   * 
   * @param name the name
   * @param pid the pid
   */
  public LDKPSpecialProtocol( String name, Hex pid )
  {
    super( name, pid );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.SpecialProtocol#createFunction(com.hifiremote.jp1.KeyMove)
   */
  public SpecialProtocolFunction createFunction( KeyMove keyMove )
  {
    return new LDKPFunction( keyMove );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.SpecialProtocol#createHex(com.hifiremote.jp1.SpecialFunctionDialog)
   */
  public Hex createHex( SpecialFunctionDialog dlg )
  {
    return LDKPFunction.createHex( dlg );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.SpecialProtocol#getFunctions()
   */
  public String[] getFunctions(){ return functions; }
  
  /** The Constant functions. */
  private static final String[] functions = { "DKP", "LKP" };
}
