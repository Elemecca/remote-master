package com.hifiremote.jp1;

import java.io.*;
import javax.swing.*;

public class RemoteMaster
{
  public static void main( String[] args )
  {
    try
    {
      KeyMapMaster km = new KeyMapMaster( args );
    }
    catch ( Exception e )
    {
      System.err.println( "Caught exception in RemoteMaster.main()!" );
      e.printStackTrace( System.err );
    }
    System.err.flush();
  }

}
