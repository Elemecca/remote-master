package com.hifiremote.jp1;

import java.io.PrintWriter;
import java.util.Enumeration;

// TODO: Auto-generated Javadoc
/**
 * The Class HexEditorNode.
 */
public abstract class HexEditorNode extends ProtocolEditorNode
{

  /**
   * Instantiates a new hex editor node.
   * 
   * @param name
   *          the name
   * @param defaultHex
   *          the default hex
   * @param hexName
   *          the hex name
   * @param parmsName
   *          the parms name
   * @param translatorName
   *          the translator name
   */
  public HexEditorNode( String name, Hex defaultHex, String hexName, String parmsName, String translatorName )
  {
    super( name, true );
    hex = defaultHex;
    if ( nullHex == null )
      nullHex = new Hex( 0 );
    if ( hex == null )
      hex = nullHex;
    this.hexName = hexName;
    this.parmsName = parmsName;
    this.translatorName = translatorName;
  }

  /** The hex. */
  private Hex hex = null;

  /**
   * Gets the hex.
   * 
   * @return the hex
   */
  public Hex getHex()
  {
    return hex;
  }

  /**
   * Sets the hex.
   * 
   * @param newHex
   *          the new hex
   */
  public void setHex( Hex newHex )
  {
    if ( newHex == null )
      newHex = nullHex;
    Hex oldHex = hex;
    hex = newHex;
    firePropertyChange( "Hex", oldHex, newHex );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.ProtocolEditorNode#print(java.io.PrintWriter)
   */
  public void print( PrintWriter pw )
  {
    if ( hex != nullHex )
      pw.println( hexName + hex.toString() );

    // Print the parameters
    @SuppressWarnings( "rawtypes" )
    Enumeration children = children();
    if ( !children.hasMoreElements() )
      return;

    pw.print( parmsName );
    while ( children.hasMoreElements() )
    {
      HexParmEditorNode node = ( HexParmEditorNode )children.nextElement();
      node.print( pw );
      if ( children.hasMoreElements() )
        pw.print( ',' );
    }
    pw.println();

    // Print the translators
    pw.print( translatorName );
    children = children();
    int parmNumber = 0;
    while ( children.hasMoreElements() )
    {
      HexParmEditorNode devNode = ( HexParmEditorNode )children.nextElement();
      @SuppressWarnings( "rawtypes" )
      Enumeration xlators = devNode.children();
      while ( xlators.hasMoreElements() )
      {
        TranslatorEditorNode xlator = ( TranslatorEditorNode )xlators.nextElement();
        pw.print( "Translator(" + parmNumber + ',' );
        xlator.print( pw );
        pw.print( ')' );
        if ( xlators.hasMoreElements() )
          pw.print( ' ' );
      }
      if ( children.hasMoreElements() )
        pw.print( ' ' );
      parmNumber++ ;
    }
    pw.println();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.ProtocolEditorNode#canAddChildren()
   */
  public boolean canAddChildren()
  {
    if ( ( hex != nullHex ) && ( hex.length() > 0 ) )
      return true;
    return false;
  }

  /** The null hex. */
  private static Hex nullHex = null;

  /** The hex name. */
  private String hexName = null;

  /** The parms name. */
  private String parmsName = null;

  /** The translator name. */
  private String translatorName = null;
}
