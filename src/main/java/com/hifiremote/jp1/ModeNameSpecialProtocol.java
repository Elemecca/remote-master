package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class ModeNameSpecialProtocol.
 */
public class ModeNameSpecialProtocol extends SpecialProtocol
{
  
  /**
   * Instantiates a new mode name special protocol.
   * 
   * @param name the name
   * @param pid the pid
   */
  public ModeNameSpecialProtocol( String name, Hex pid )
  {
    super( name, pid );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.SpecialProtocol#createFunction(com.hifiremote.jp1.KeyMove)
   */
  public SpecialProtocolFunction createFunction( KeyMove keyMove )
  {
    return new ModeNameFunction( keyMove );
  }
  
  public SpecialProtocolFunction createFunction( Macro macro )
  {
    return new ModeNameFunction( macro );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.SpecialProtocol#createHex(com.hifiremote.jp1.SpecialFunctionDialog)
   */
  public Hex createHex( SpecialFunctionDialog dlg )
  {
    return ModeNameFunction.createHex( dlg );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.SpecialProtocol#getFunctions()
   */
  public String[] getFunctions(){ return functions; }
  
  /** The Constant functions. */
  private static final String[] functions = { "ModeName" };
}
