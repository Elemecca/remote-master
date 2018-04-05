package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class PauseSpecialProtocol.
 */
public class PauseSpecialProtocol extends SpecialProtocol
{
  public static PauseParameters getPauseParameters( String userName, Remote remote )
  {
    PauseParameters pauseParameters = remote.getPauseParameters().get( userName );
    if ( pauseParameters == null )
    {
      // set the default value
      pauseParameters = new PauseParameters( userName, remote );
    }
    return pauseParameters;
  }
  
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
  
  public void setPauseParameters( Remote remote )
  {
    pauseParameters = getPauseParameters( getUserFunctions()[ 0 ], remote );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.SpecialProtocol#createFunction(com.hifiremote.jp1.KeyMove)
   */
  public SpecialProtocolFunction createFunction( KeyMove keyMove )
  {
    return new PauseFunction( keyMove );
  }
  
  public SpecialProtocolFunction createFunction( Macro macro )
  {
    return new PauseFunction( macro );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.SpecialProtocol#createHex(com.hifiremote.jp1.SpecialFunctionDialog)
   */
  @Override
  public Hex createHex( SpecialFunctionDialog dlg )
  {
    return PauseFunction.createHex( dlg, pauseParameters );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.SpecialProtocol#getFunctions()
   */
  public String[] getFunctions(){ return functions; }
  
  /** The Constant functions. */
  private static final String[] functions = { "Pause" };
  
  private PauseParameters pauseParameters = null;

}
