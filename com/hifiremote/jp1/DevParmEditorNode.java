package com.hifiremote.jp1;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

public class DevParmEditorNode
  extends ProtocolEditorNode
{
  public DevParmEditorNode()
  {
     super( "Parameter", true );
     add( new TranslatorEditorNode());
  }

  public ProtocolEditorPanel getEditingPanel()
  {
     if ( editorPanel == null )
      editorPanel = new DevParmEditorPanel();
    return editorPanel;
  }

  public ProtocolEditorNode createChild()
  {
    return new TranslatorEditorNode();
  }

  public void print( PrintWriter pw )
  {
    pw.print( getName() + ':' );
    if ( type == NUMBER )
      pw.print( bits );
    else if ( type == CHOICE )
    {
      for ( Enumeration e = choices.elements(); e.hasMoreElements(); )
      {
        pw.print( e.nextElement());
        if ( e.hasMoreElements())
          pw.print( "|" );
      }
    }
    else if ( type == FLAG )
      pw.print( "bool" );
    if ( defaultValue != -1 )
      pw.print( "=" + defaultValue );
  }

  public boolean canDelete(){ return true; }

  public String getName(){ return ( String )getUserObject(); }
  public void setName( String newName ){ setUserObject( newName ); }

  private int bits = 8;
  public int getBits(){ return bits; }
  public void setBits( int newBits ){ bits = newBits; }

  private int type = NUMBER;
  private Vector choices = new Vector();
  private int defaultValue = -1;

  private static DevParmEditorPanel editorPanel = null;

  public final static int NUMBER = 0;
  public final static int CHOICE = 1;
  public final static int FLAG = 2;
}
