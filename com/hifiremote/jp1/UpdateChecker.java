/**
 * 
 */
package com.hifiremote.jp1;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * @author Greg
 */
public class UpdateChecker
{
  private static Desktop desktop = null;
  
  public static void checkUpdateAvailable( JP1Frame frame ) throws IOException
  {
    URL url = new URL( "http://controlremote.sourceforge.net/version.dat" );
    BufferedReader in = new BufferedReader( new InputStreamReader( url.openStream() ) );
    String latestVersion = in.readLine();
    in.close();
    int latestBuild = 0;
    int pos = latestVersion.indexOf( "build" );
    if ( pos >= 0 )
    {
      String buildStr = latestVersion.substring( pos + 5 ).trim();
      latestVersion = latestVersion.substring( 0, pos ).trim();
      try
      {
        latestBuild = Integer.parseInt( buildStr );
      }
      catch ( NumberFormatException nfe )
      {
        latestBuild = 0;
      }
    }

    int minLen = Math.min( RemoteMaster.version.length(), latestVersion.length() );
    int test = RemoteMaster.version.substring( 0, minLen ).compareTo( latestVersion.substring( 0, minLen ) );
    test = test == 0 ? latestVersion.length() - RemoteMaster.version.length() : test;
    String text = null;
    if ( test > 0 || test == 0 && RemoteMaster.getBuild() >= latestBuild )
    {
      text = "You are using the latest version (" + RemoteMaster.version;
      if ( RemoteMaster.getBuild() > 0 )
      {
        text += " build " + RemoteMaster.getBuild();
      }
      text += ") of RemoteMaster.";
    }
    else
    {
      text = "<html>";
      text += latestBuild > 0 ? "Build " + latestBuild + " of version " : "Version ";
      text += latestVersion + " of RemoteMaster is available, but you are still using ";
      text += RemoteMaster.getBuild() > 0 ? "build " + RemoteMaster.getBuild() + " of " : "";
      text += "version " + RemoteMaster.version + ".";
      text += "<p>The new version is available for download from<br><a href=\"https://sourceforge.net/projects/controlremote/";
      text += "files/RemoteMaster/" + latestVersion;
      text += latestBuild > 0 ? "build" + latestBuild : "";
      text += "\">" + "https://sourceforge.net/projects/controlremote/";
      text += "files/RemoteMaster/" + latestVersion;
      text += ( latestBuild > 0 ? "build" + latestBuild : "" ) + "</a></html>";
    }

    if ( Desktop.isDesktopSupported() )
    {
      desktop = Desktop.getDesktop();
    }
    JEditorPane pane = new JEditorPane( "text/html", text );
    pane.setEditable( false );
    pane.setBackground( frame.getContentPane().getBackground() );
    pane.addHyperlinkListener( new HyperlinkListener()
    {
      @Override
      public void hyperlinkUpdate( HyperlinkEvent event )
      {
        if ( event.getEventType() != HyperlinkEvent.EventType.ACTIVATED )
        {
          return;
        }

        if ( desktop != null )
        {
          try
          {
            desktop.browse( event.getURL().toURI() );
          }
          catch ( IOException e )
          {
            e.printStackTrace( System.err );
          }
          catch ( URISyntaxException e )
          {
            e.printStackTrace( System.err );
          }
        }
      }
    } );
    new TextPopupMenu( pane );
    JOptionPane.showMessageDialog( frame, pane, "RemoteMaster Version Check", JOptionPane.INFORMATION_MESSAGE );
  }
}
