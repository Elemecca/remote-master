package com.hifiremote.jp1;

import java.io.*;
import javax.swing.*;

public class RemoteMaster
{
  public static void main( String[] args )
  {
    try
    {
      System.setErr( new PrintStream( new FileOutputStream( "rmaster.err" )));
      KeyMapMaster km = null;
      if ( args.length > 0 )
        km = new KeyMapMaster( new File( args[ 0 ]));
      else
        km = new KeyMapMaster();
    }
    catch ( Exception e )
    {
      System.err.println( "Caught exception in RemoteMaster.main()!" );
      e.printStackTrace( System.err );
    }
    System.err.flush();
  }

}
