package com.hifiremote.jp1;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

public class LocalObjectTransferable
  implements Transferable
{
  public LocalObjectTransferable( Object o )
  {
    this.o = o;

    if ( dataFlavors == null )
    {
      try
      {
        dataFlavors = new DataFlavor[ 1 ];
        String str = DataFlavor.javaJVMLocalObjectMimeType + "; class=java.lang.Object";
        flavor = new DataFlavor( str, null );
        dataFlavors[ 0 ] = flavor;
      }
      catch ( Exception e )
      {
        e.printStackTrace( System.err );
      }
    }
  }

  // Transferable interface
  private static DataFlavor flavor = null;
  private static DataFlavor[] dataFlavors = null;

  public static DataFlavor getFlavor(){ return flavor; }
  public DataFlavor[] getTransferDataFlavors(){ return dataFlavors; }

  public boolean isDataFlavorSupported( DataFlavor flavor )
  {
    return ( flavor == this.flavor );
  }

  public Object getTransferData( DataFlavor flavor )
    throws UnsupportedFlavorException
  {
    if ( flavor != this.flavor )
      throw new UnsupportedFlavorException( flavor );
    return o;
  }

  private Object o;
}
