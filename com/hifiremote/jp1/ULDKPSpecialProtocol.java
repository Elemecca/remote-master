package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class ULDKPSpecialProtocol.
 */
public class ULDKPSpecialProtocol extends SpecialProtocol
{
  
  /**
   * Instantiates a new uLDKP special protocol.
   * 
   * @param name the name
   * @param pid the pid
   */
  public ULDKPSpecialProtocol( String name, Hex pid )
  {
    super( name, pid );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.SpecialProtocol#createFunction(com.hifiremote.jp1.KeyMove)
   */
  public SpecialProtocolFunction createFunction( KeyMove keyMove )
  {
    return new ULDKPFunction( keyMove );
  }
  
  public SpecialProtocolFunction createFunction( Macro macro )
  {
    return new ULDKPFunction( macro );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.SpecialProtocol#createHex(com.hifiremote.jp1.SpecialFunctionDialog)
   */
  public Hex createHex( SpecialFunctionDialog dlg )
  {
    return ULDKPFunction.createHex( dlg );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.SpecialProtocol#getFunctions()
   */
  public String[] getFunctions(){ return functions; }
  
  /** The Constant functions. */
  private static final String[] functions = { "UDSM", "UDKP", "ULKP" };
}
