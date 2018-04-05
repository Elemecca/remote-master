package com.hifiremote.jp1;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

// TODO: Auto-generated Javadoc
/**
 * The Class LocalObjectTransferable.
 */
public class LocalObjectTransferable
  implements Transferable
{
  
  /**
   * Instantiates a new local object transferable.
   * 
   * @param o the o
   */
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
  /** The flavor. */
  private static DataFlavor flavor = null;
  
  /** The data flavors. */
  private static DataFlavor[] dataFlavors = null;

  /**
   * Gets the flavor.
   * 
   * @return the flavor
   */
  public static DataFlavor getFlavor(){ return flavor; }
  
  /* (non-Javadoc)
   * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
   */
  public DataFlavor[] getTransferDataFlavors(){ return dataFlavors; }

  /* (non-Javadoc)
   * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
   */
  public boolean isDataFlavorSupported( DataFlavor flavor )
  {
    return ( flavor == LocalObjectTransferable.flavor );
  }

  /* (non-Javadoc)
   * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
   */
  public Object getTransferData( DataFlavor flavor )
    throws UnsupportedFlavorException
  {
    if ( flavor != LocalObjectTransferable.flavor )
      throw new UnsupportedFlavorException( flavor );
    return o;
  }

  /** The o. */
  private Object o;
}
