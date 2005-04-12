package com.hifiremote.jp1;

import java.io.*;

public class TranslatorEditorNode
  extends ProtocolEditorNode
{
  public TranslatorEditorNode()
  {
    super( "Translator", false );
  }

  public ProtocolEditorPanel getEditingPanel()
  {
    if ( panel == null )
      panel = new TranslatorEditorPanel();
    return panel;
  }

  public void print( PrintWriter pw )
  {
    if ( order == LSB )
      pw.print( "lsb," );
    if ( comp )
      pw.print( "comp," );
    pw.print( getBits());
    pw.print( ',' );
    pw.print( msbOffset );
    if (( lsbOffset != 0 ) || ( adjust != 0 ))
    {
      pw.print( ',' );
      pw.print( lsbOffset );
    }
    if ( adjust != 0 )
    {
      pw.print( ',' );
      pw.print( adjust );
    }      
  }

  public boolean canDelete(){ return true; }
  
  public int getBits()
  {
    if ( bits == -1 )
      return (( HexParmEditorNode )getParent()).getBits();
    return bits;
  }

  public void setBits( int newBits )
  {
    bits = newBits;
  }

  public int getBitOrder(){ return order; }
  public void setBitOrder( int newOrder ){ order = newOrder; }

  public boolean getComp(){ return comp; }
  public void setComp( boolean newComp ){ comp = newComp; }

  public int getMSBOffset(){ return msbOffset; }
  public void setMSBOffset( int newOffset ){ msbOffset = newOffset; }

  public int getLSBOffset(){ return lsbOffset; }
  public void setLSBOffset( int newOffset ){ lsbOffset = newOffset; }

  public int getAdjust(){ return adjust; }
  public void setAdjust( int newAdjust ){ adjust = newAdjust; }

  private static TranslatorEditorPanel panel = null;
  private int bits = -1;
  private int order = MSB;
  private boolean comp = false;
  private int msbOffset = 0;
  private int lsbOffset = 0;
  private int adjust = 0;

  public final static int MSB = 0;
  public final static int LSB = 1;
}
