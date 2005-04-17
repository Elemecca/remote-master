package com.hifiremote.jp1;

import java.beans.*;
import java.io.PrintWriter;
import java.util.Enumeration;

public abstract class HexEditorNode
  extends ProtocolEditorNode
{
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

  private Hex hex = null;
  public Hex getHex(){ return hex; }
  public void setHex( Hex newHex )
  { 
    if ( newHex == null )
      newHex = nullHex;
    Hex oldHex = hex;
    hex = newHex; 
    firePropertyChange( "Hex", oldHex, newHex );
  }

  public void print( PrintWriter pw )
  {
    if ( hex != nullHex )
      pw.println( hexName + hex.toString());

    // Print the parameters
    Enumeration children = children();
    if ( !children.hasMoreElements() )
      return;

    pw.print( parmsName );
    while( children.hasMoreElements() )
    {
      HexParmEditorNode node = ( HexParmEditorNode )children.nextElement();
      node.print( pw );
      if ( children.hasMoreElements())
        pw.print( ',' );
    } 
    pw.println();

    // Print the translators
    pw.print( translatorName );
    children = children();
    int parmNumber = 0;
    while( children.hasMoreElements() )
    {
      HexParmEditorNode devNode = ( HexParmEditorNode )children.nextElement();
      Enumeration xlators = devNode.children();
      while ( xlators.hasMoreElements())
      {
        TranslatorEditorNode xlator = ( TranslatorEditorNode )xlators.nextElement();
        pw.print( "Translator(" + parmNumber + ',' );
        xlator.print( pw );
        pw.print( ')' );
        if ( xlators.hasMoreElements())
          pw.print( ' ' );
      }
      if ( children.hasMoreElements())
        pw.print( ' ' );  
      parmNumber++;
    } 
    pw.println();
  }

  public boolean canAddChildren()
  {
    if (( hex != nullHex ) && ( hex.length() > 0 ))
      return true;
   return false; 
  }

  private static Hex nullHex = null;
  private String hexName = null;
  private String parmsName = null;
  private String translatorName = null;
}
