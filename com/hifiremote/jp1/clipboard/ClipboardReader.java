/**
 * 
 */
package com.hifiremote.jp1.clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Greg
 */
public abstract class ClipboardReader
{
  public static ClipboardReader getInstance( Transferable transferable ) throws UnsupportedFlavorException, IOException
  {
    DataFlavor[] flavors = transferable.getTransferDataFlavors();
    ClipboardReader reader = null;
    for ( DataFlavor tentative : flavors )
    {
      if ( tentative.getPrimaryType().equals( "text" ) )
      {
        if ( tentative.getRepresentationClass() == Reader.class )
        {
          String subType = tentative.getSubType();
          if ( subType.equals( "html" ) )
          {
            reader = new HTMLClipboardReader( tentative.getReaderForText( transferable ) );
            break;
          }
          else if ( subType.equals( "plain" ) )
          {
            reader = new PlainTextClipboardReader( tentative.getReaderForText( transferable ) );
          }
        }
      }
    }
    return reader;
  }

  public List< List< String > > getData() throws IOException
  {
    List< List< String > > data = new ArrayList< List< String > >();
    List< String > line = null;
    while ( ( line = readNextLine() ) != null )
    {
      data.add( line );
    }

    if ( data.isEmpty() )
    {
      return null;
    }
    return data;
  }

  public abstract List< String > readNextLine() throws IOException;

  public abstract void close() throws IOException;
}
