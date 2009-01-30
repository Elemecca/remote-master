package com.hifiremote.jp1;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class HexParmEditorNode.
 */
public abstract class HexParmEditorNode
  extends ProtocolEditorNode
{
  
  /**
   * Instantiates a new hex parm editor node.
   */
  public HexParmEditorNode()
  {
     super( "Parameter", true );
     add( new TranslatorEditorNode());
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorNode#createChild()
   */
  public ProtocolEditorNode createChild()
  {
    return new TranslatorEditorNode();
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorNode#print(java.io.PrintWriter)
   */
  public void print( PrintWriter pw )
  {
    pw.print( getName() + ':' );
    if ( format == HEXADECIMAL )
      pw.print( '$' );
    if ( type == NUMBER )
      pw.print( bits );
    else if ( type == CHOICE )
    {
      boolean first = true;
      for ( Integer choice : choices )
      {
        if ( first )
          first = false;
        else
          pw.print( "|" );
        pw.print( choice );
      }
    }
    else if ( type == FLAG )
      pw.print( "bool" );
    if ( defaultValue != -1 )
      pw.print( "=" + defaultValue );
  }

  /**
   * Sets the default value.
   * 
   * @param value the new default value
   */
  public void setDefaultValue( int value ){ defaultValue = value; }
  
  /**
   * Gets the default value.
   * 
   * @return the default value
   */
  public int getDefaultValue(){ return defaultValue; }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorNode#canDelete()
   */
  public boolean canDelete(){ return true; }

  /** The bits. */
  private int bits = 8;
  
  /**
   * Gets the bits.
   * 
   * @return the bits
   */
  public int getBits()
  {
    if ( type == NUMBER )
      return bits;
    else if ( type == FLAG )
      return 1;
    else // Choice
    {
      int numChoices = choices.size();
      int rc = 0;
      while ( numChoices > 0 )
      {
        numChoices /= 2;
        rc++;
      }
      if ( rc == 0 )
        rc = 1;
      return rc;
    }
  }
  
  /**
   * Sets the bits.
   * 
   * @param newBits the new bits
   */
  public void setBits( int newBits ){ bits = newBits; }

  /** The type. */
  private int type = NUMBER;
  
  /**
   * Sets the type.
   * 
   * @param newType the new type
   */
  public void setType( int newType ){ type = newType; }
  
  /**
   * Gets the type.
   * 
   * @return the type
   */
  public int getType(){ return type; }

  /** The format. */
  private int format = DECIMAL;
  
  /**
   * Sets the format.
   * 
   * @param newFormat the new format
   */
  public void setFormat( int newFormat ){ format = newFormat; }
  
  /**
   * Gets the format.
   * 
   * @return the format
   */
  public int getFormat(){ return format; }

  /** The choices. */
  private List< Integer > choices = new ArrayList< Integer >();
  
  /**
   * Gets the choices.
   * 
   * @return the choices
   */
  public List< Integer > getChoices(){ return choices; }
  
  /**
   * Sets the choices.
   * 
   * @param newChoices the new choices
   */
  public void setChoices( List< Integer > newChoices ){ choices = newChoices; }
  
  /** The default value. */
  private int defaultValue = -1;

  /** The Constant NUMBER. */
  public final static int NUMBER = 0;
  
  /** The Constant CHOICE. */
  public final static int CHOICE = 1;
  
  /** The Constant FLAG. */
  public final static int FLAG = 2;

  /** The Constant DECIMAL. */
  public final static int DECIMAL = 0;
  
  /** The Constant HEXADECIMAL. */
  public final static int HEXADECIMAL = 1;
}
