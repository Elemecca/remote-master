package com.hifiremote.jp1;

import java.io.*;
import javax.swing.*;

public class RemoteMaster
{
  private static void createAndShowGUI( String[] args )
  {
    try
    {
      KeyMapMaster km = new KeyMapMaster( args );
    }
    catch ( Exception e )
    {
      System.err.println( "Caught exception in RemoteMaster.main()!" );
      e.printStackTrace( System.err );
      System.err.flush();
      System.exit( 0 );
    }
    System.err.flush();
  }

  public static void main(String[] args)
  {
    //Schedule a job for the event-dispatching thread:
    //creating and showing this application's GUI.
    parms = args;
    javax.swing.SwingUtilities.invokeLater( new Runnable() 
    {
      public void run() 
      {
        createAndShowGUI( parms );
      }
    });
  }
  private static String[] parms = null;
}
