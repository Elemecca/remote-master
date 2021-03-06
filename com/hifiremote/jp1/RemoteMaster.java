package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkListener;

import com.hifiremote.LibraryLoader;
import com.hifiremote.jp1.FixedData.Location;
import com.hifiremote.jp1.assembler.MAXQ610data;
import com.hifiremote.jp1.extinstall.ExtInstall;
import com.hifiremote.jp1.extinstall.RMExtInstall;
import com.hifiremote.jp1.io.CommHID;
import com.hifiremote.jp1.io.JPS;
import com.hifiremote.jp1.io.IO;
import com.hifiremote.jp1.io.JP12Serial;
import com.hifiremote.jp1.io.JP1Parallel;
import com.hifiremote.jp1.io.JP1USB;
import com.hifiremote.jp1.assembler.MAXQ610data;

/**
 * Description of the Class.
 * 
 * @author Greg
 * @created November 30, 2006
 */

public class RemoteMaster extends JP1Frame implements ActionListener, PropertyChangeListener, HyperlinkListener,
    ChangeListener
{
  public static final int MAX_RDF_SYNC = 5;
  public static final int MIN_RDF_SYNC = 3;

  public static final Color AQUAMARINE = new Color( 127, 255, 212 );

  public static boolean admin = false;
  public static boolean legacyMergeOK = false;
  
  /** The frame. */
  private static JP1Frame frame = null;

  /** Description of the Field. */
  public final static String version = "v2.04";
  public final static int buildVer = 8;
  
  public static int getBuild()
  {
    int buildNumber = buildVer;
    File buildFile = new File( workDir, "buildRef.txt" );
    try
    {
      BufferedReader rdr = new BufferedReader( new FileReader ( buildFile ) );
      String str = rdr.readLine();
      rdr.close();
      buildNumber = Integer.parseInt( str );
      if ( buildNumber < buildVer )
      {
        buildNumber = buildVer;
      }
    }
    catch ( Exception ex ){};
    return buildNumber;
  }
  
  public static String getFullVersion()
  {
    return version.replaceAll("\\s","") + "build" + getBuild();
  }

  public enum Use
  {
    DOWNLOAD, READING, UPLOAD, SAVING, SAVEAS, EXPORT
  }
  
  public static int defaultToolTipTimeout = 5000;
  
  /** The dir. */
  private File dir = null;

  private File mergeDir = null;

  /** Description of the Field. */
  public File file = null;

  /** The remote config. */
  private RemoteConfiguration remoteConfig = null;

  private JToolBar toolBar = null;

  private RMAction newAction = null;
  private RMAction newUpgradeAction = null;

  private RMAction codesAction = null;

  /** The open item. */
  private RMAction openAction = null;

  /** The save item. */
  private RMAction saveAction = null;

  /** The save as item. */
  private RMAction saveAsAction = null;

  private JMenuItem installExtenderItem = null;

  private RMAction openRdfAction = null;

  protected RMAction highlightAction = null;

  private JMenuItem rdfPathItem = null;

  private JMenuItem mapPathItem = null;
  private JMenuItem setBaselineItem = null;
  private JMenuItem clearBaselineItem = null;

  /** The recent files. */
  private JMenu recentFiles = null;

  /** The exit item. */
  private JMenuItem exitItem = null;

  // Remote menu items
  /** The interfaces. */
  private ArrayList< IO > interfaces = new ArrayList< IO >();

  /** The download action. */
  private RMAction downloadAction = null;

  /** The upload action. */
  private RMAction uploadAction = null;

  /** The raw download item */
  private JMenuItem downloadRawItem = null;

  /** The verify upload item */
  private JCheckBoxMenuItem verifyUploadItem = null;

  /** The upload wav item. */
  private JMenuItem uploadWavItem = null;

  // Options menu items
  /** The look and feel items. */
  private JRadioButtonMenuItem[] lookAndFeelItems = null;

  protected JCheckBoxMenuItem highlightItem = null;

  private JCheckBoxMenuItem enablePreserveSelection = null;
  
  private JCheckBoxMenuItem showSlingboxProtocols = null;
  
  private JRadioButtonMenuItem defaultDelayItem = null;
  
  private JRadioButtonMenuItem specifiedDelayItem = null;

  // Advanced menu items
  private JMenuItem cleanUpperMemoryItem = null;

  private JMenuItem clearAltPIDHistory = null;

  private JMenuItem initializeTo00Item = null;

  private JMenuItem initializeToFFItem = null;
  
  private JCheckBoxMenuItem useSavedDataItem = null;
  
  private static JCheckBoxMenuItem getSystemFilesItem = null;
  
  private static JMenuItem putSystemFileItem = null;
  
  private static JMenuItem parseIRDBItem = null;
  
  private static JMenuItem extractSSItem = null;
  
  private static JMenuItem analyzeMAXQprotocols = null;

  // Help menu items
  /** The update item. */
  private JMenuItem updateItem = null;

  private JMenuItem readmeItem = null;

  private JMenuItem tutorialItem = null;

  private JMenuItem homePageItem = null;

  private JMenuItem learnedSignalItem = null;

  private JMenuItem wikiItem = null;

  private JMenuItem forumItem = null;
  
  private int tooltipDelay = 0;
  private int tooltipDefaultDelay = 0;

  /** The about item. */
  private JMenuItem aboutItem = null;

  /** The tabbed pane. */
  private JTabbedPane tabbedPane = null;

  private RMPanel currentPanel = null;

  /** The general panel. */
  private GeneralPanel generalPanel = null;

  /** The key move panel. */
  private KeyMovePanel keyMovePanel = null;

  /** The macro panel. */
  private MacroPanel macroPanel = null;

  /** The special function panel. */
  private SpecialFunctionPanel specialFunctionPanel = null;

  private TimedMacroPanel timedMacroPanel = null;

  private FavScanPanel favScanPanel = null;
  
  private FavoritesPanel favoritesPanel = null;

  /** The device panel. */
  private DeviceUpgradePanel devicePanel = null;

  /** The protocol panel. */
  private ProtocolUpgradePanel protocolPanel = null;
  
  private ActivityPanel activityPanel = null;

  /** The learned panel. */
  private LearnedSignalPanel learnedPanel = null;

  private RawDataPanel rawDataPanel = null;
  private SegmentPanel segmentPanel = null;

  /** The adv progress bar. */
  private JProgressBar advProgressBar = null;
  private JLabel advProgressLabel = null;

  /** The upgrade progress bar. */
  private JProgressBar upgradeProgressBar = null;
  private JProgressBar devUpgradeProgressBar = null;

  private JPanel upgradeProgressPanel = null;

  /** The learned progress bar. */
  private JProgressBar learnedProgressBar = null;

  private JPanel memoryStatus = null;
  private JPanel extraStatus = null;

  private JPanel interfaceStatus = null;
  private JPanel warningStatus = null;
  private String interfaceText = null;
  private DeviceUpgradeEditor duEditor = null;

  private JProgressBar interfaceState = null;
  
  private boolean exitPrompt = false;

  private JPanel statusBar = null;

  private boolean hasInvalidCodes = false;

  private CodeSelectorDialog codeSelectorDialog = null;

  private JDialog colorDialog = null;
  
  private ActionEvent lfEvent = null;

  public JDialog getColorDialog()
  {
    return colorDialog;
  }

  private JColorChooser colorChooser = null;

  public JColorChooser getColorChooser()
  {
    return colorChooser;
  }

  private TextFileViewer rdfViewer = null;

  private List< AssemblerItem > clipBoardItems = new ArrayList< AssemblerItem >();
  
  public static class NegativeDefaultButtonJOptionPane 
  {
    // From http://stackoverflow.com/questions/1395707/how-to-make-joptionpane-showconfirmdialog-have-no-selected-by-default
    public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType,
        int messageType ) 
    {
      List<Object> options = new ArrayList<Object>();
      Object defaultOption;
      switch(optionType){
        case JOptionPane.OK_CANCEL_OPTION:
          options.add(UIManager.getString("OptionPane.okButtonText"));
          options.add(UIManager.getString("OptionPane.cancelButtonText"));
          defaultOption = UIManager.getString("OptionPane.cancelButtonText");
          break;
        case JOptionPane.YES_NO_OPTION:
          options.add(UIManager.getString("OptionPane.yesButtonText"));
          options.add(UIManager.getString("OptionPane.noButtonText"));
          defaultOption = UIManager.getString("OptionPane.noButtonText");
          break;
        case JOptionPane.YES_NO_CANCEL_OPTION:
          options.add(UIManager.getString("OptionPane.yesButtonText"));
          options.add(UIManager.getString("OptionPane.noButtonText"));
          options.add(UIManager.getString("OptionPane.cancelButtonText"));
          defaultOption = UIManager.getString("OptionPane.cancelButtonText");
          break;
        default:
          throw new IllegalArgumentException("Unknown optionType "+optionType);
      }
      return JOptionPane.showOptionDialog(parentComponent, message, title, optionType, messageType, null, options.toArray(), defaultOption);
    }
  }
  
  public class Preview extends JPanel
  {
    Preview()
    {
      super();
      sample.setPreferredSize( new Dimension( 90, 30 ) );
      sample.setBorder( BorderFactory.createLineBorder( Color.GRAY ) );
      JPanel p = new JPanel();
      p.add( sample );
      add( p );
      add( Box.createHorizontalStrut( 20 ) );
      ButtonGroup grp = new ButtonGroup();
      grp.add( devices );
      grp.add( protocols );
      devices.setSelected( true );
      devices.addActionListener( new ActionListener()
      {
        @Override
        public void actionPerformed( ActionEvent e )
        {
          if ( colorCol != 0 )
          {
            Color color = getInitialHighlight( devicePanel.table, 0 );
            colorChooser.setColor( color );
            sample.setBackground( color );
            colorCol = 0;
          }
        }
      } );
      protocols.addActionListener( new ActionListener()
      {
        @Override
        public void actionPerformed( ActionEvent e )
        {
          if ( colorCol != 1 )
          {
            Color color = getInitialHighlight( devicePanel.table, 1 );
            colorChooser.setColor( color );
            sample.setBackground( color );
            colorCol = 1;
          }
        }
      } );
      selectors.add( devices );
      selectors.add( protocols );
      add( selectors );
    }

    public void reset( boolean disableProtocol )
    {
      colorCol = 0;
      devices.setSelected( true );
      protocols.setEnabled( !disableProtocol );
    }

    public Color getColor()
    {
      return result;
    }

    public JPanel getSelectors()
    {
      return selectors;
    }

    private JPanel sample = new JPanel();
    private Color result = null;
    private JPanel selectors = new JPanel( new GridLayout( 2, 1 ) );
    private int colorCol = 0;
    private JRadioButton devices = new JRadioButton( "Device" );
    private JRadioButton protocols = new JRadioButton( "Protocol" );
  }

  private class DownloadTask extends SwingWorker< RemoteConfiguration, Void >
  {
    private File file = null;
    private Use use = null;
    
    public DownloadTask()
    {
      file = null;
      use = Use.DOWNLOAD;
    }
    
    public DownloadTask( File file )
    {
      this.file = file;
      use = Use.READING;
    }
    
    @Override
    protected RemoteConfiguration doInBackground() throws Exception
    {
      clearAllInterfaces();
      IO io = getOpenInterface( file, use );
      if ( io == null )
      {
        setInterfaceState( null );
        JOptionPane.showMessageDialog( RemoteMaster.this, "No remotes found!" );
        return null;
      }
      System.err.println( "Interface opened successfully" );

      // See comment in Hex.getRemoteSignature( short[] ) for why the line below was not safe
      // String sig = io.getRemoteSignature();
      int baseAddress = io.getRemoteEepromAddress();
      System.err.println( "Base address = $" + Integer.toHexString( baseAddress ).toUpperCase() );
      String sigString = getIOsignature( io, baseAddress );
      String sig = null;
      String sig2 = null;
      short[] sigData = null;
      Remote remote = null;
      List< Remote > remotes = null;
      RemoteManager rm = RemoteManager.getRemoteManager();
      byte[] jp2info = new byte[ 0 ];
      if ( sigString.length() > 8 ) // JP1.4/JP2 full signature block
      {
        sig = sigString.substring( 0, 6 );
//      int infoLen = 6 + 6 * remote.getProcessor().getAddressLength();
        int infoLen = 64;
        jp2info = new byte[ infoLen ];
        if ( !io.getJP2info( jp2info, infoLen ) )
        {
          jp2info = null;
        }

        sigData = new short[ sigString.length() + ( jp2info != null ? jp2info.length : 0 ) ];
        int index = 0;
        for ( int i = 0; i < sigString.length(); i++ )
        {
          sigData[ index++ ] = ( short )sigString.charAt( i );
        };
        if ( jp2info != null )
        {
          for ( int i = 0; i < jp2info.length; i++ )
          {
            sigData[ index++ ] = ( short )( jp2info[ i ] & 0xFF );
          }
        }
      }
      else if ( io.getInterfaceName().equals( "JPS" ) )
      {
        sig = sigString;
        int infoLen = 64;
        jp2info = new byte[ infoLen ];
        if ( !io.getJP2info( jp2info, infoLen ) )
        {
          jp2info = null;
        }
        sigData = new short[ 64 ];
        if ( jp2info != null )
        {
          for ( int i = 0; i < jp2info.length; i++ )
          {
            sigData[ i ] = ( short )( jp2info[ i ] & 0xFF );
          }
        }
      }
      else
      {
        sig = sigString;
      }
      
      if ( remoteConfig != null && remoteConfig.getRemote() != null )
      {
        sig2 = remoteConfig.getRemote().getSignature();
        if ( sig2.length() <= sig.length() && sig2.equals( sig.substring( 0, sig2.length() ) ) )
        {
          // Current and download remotes have same signature. Note that if current signature length
          // is less than 8 then we only test the corresponding substring of download signature.
          remotes = rm.findRemoteBySignature( sig2 );
          sig = sig2;
          if ( remotes.size() == 1 )
          {
            // There is only one remote with current signature so this must be the download remote.
            remote = remotes.get( 0 );
          }
        }
      }
      if ( remote == null )
      {
        System.err.println( "Searching for RDF" );
        if ( remotes == null )
        {
          for ( int i = sig.length(); i >= 4; i-- )
          {
            sig2 = sig.substring( 0, i );
            remotes = rm.findRemoteBySignature( sig2 );
            if ( !remotes.isEmpty() )
            {
              break;
            }
          }
          sig = sig2;
          System.err.println( "Final signature sought = " + sig );
        }
        if ( remotes.isEmpty() )
        {
          System.err.println( "No matching RDF found" );
          JOptionPane.showMessageDialog( RemoteMaster.this, "No RDF matches signature starting " + sig );
          io.closeRemote();
          setInterfaceState( null );
          return null;
        }
        else if ( remotes.size() == 1 )
        {
          remote = remotes.get( 0 );
        }
        else
        {// ( remotes.length > 1 )
          int maxFixedData = 0;
          for ( Remote r : remotes )
          {
            r.load();
            for ( FixedData fixedData : r.getRawFixedData() )
            {
              if ( fixedData.getLocation() == Location.E2 )
              {
                maxFixedData = Math.max( maxFixedData, fixedData.getAddress() + fixedData.getData().length );
              }
            }
          }

          int eepromSize = io.getRemoteEepromSize();
          if ( eepromSize > 0 && maxFixedData > eepromSize )
          {
            maxFixedData = eepromSize;
          }
          short[] buffer = new short[ maxFixedData ];
          if ( maxFixedData > 0 )
          {
            io.readRemote( baseAddress, buffer );
          }
          Remote[] choices = FixedData.filter( remotes, buffer, sigData );
          if ( choices.length == 0 )
          {
            // None of the remotes match on fixed data, so offer whole list
            choices = remotes.toArray( choices );
          }
          if ( choices.length == 1 )
          {
            remote = choices[ 0 ];
          }
          else
          {
            String message = "Please pick the best match to your remote from the following list:";
            Object rc = JOptionPane.showInputDialog( null, message, "Ambiguous Remote", JOptionPane.ERROR_MESSAGE,
                null, choices, choices[ 0 ] );
            if ( rc == null )
            {
              io.closeRemote();
              setInterfaceState( null );
              return null;
            }
            else
            {
              remote = ( Remote )rc;
            }
          }
        }
        System.err.println( "Remote identified as: " + remote.getName() );
      }
      Remote newRemote = new Remote( remote, remote.getNameIndex() );
      RemoteManager.getRemoteManager().replaceRemote( remote, newRemote );
      remote = newRemote;
      remote.load();
      RemoteConfiguration rc = new RemoteConfiguration( remote, RemoteMaster.this );
      recreateToolbar();
      int count = io.readRemote( remote.getBaseAddress(), rc.getData() );
      System.err.println( "Number of bytes read  = $" + Integer.toHexString( count ).toUpperCase() );
      io.closeRemote();
      System.err.println( "Ending normal download" );
      if ( sigData != null )
      { 
        if ( !io.getInterfaceName().equals( "JPS" ) )
        {
          sigData = Arrays.copyOf( sigData, 6 + 6 * remote.getProcessor().getAddressLength() );
        }
        rc.setSigData( sigData );
      }
//      try
//      {
        rc.parseData();
        rc.setSavedData();
//      }
//      catch ( IOException e )
//      {
//        e.printStackTrace();
//      }
      rc.updateImage();
      return rc;
    }
    
    @Override
    public void done()
    {
      try
      {
        RemoteConfiguration rc = get();
        if ( rc != null )
        {
          remoteConfig = rc;
        }
      } 
      catch ( InterruptedException ignore ) {}
      catch ( java.util.concurrent.ExecutionException e ) 
      {
        String why = null;
        Throwable cause = e.getCause();
        if ( cause != null ) 
        {
          why = cause.getMessage();
        } 
        else 
        {
          why = e.getMessage();
        }
        System.err.println( "Download error: " + why );
        setInterfaceState( null );
        String message = "<html>Error downloading from remote.<br><br>"
            + "This may well be the result of a bug in the RMIR software.  To help us, please<br>"
            + "do a raw download as follows.  Close RMIR, re-open it and click on &quot;Raw download&quot;<br>"
            + "on the Remote menu.  Click the Download button on the window that then opens.  <br>"
            + "When the dowload finishes, click Save.  Accept the default filename that is offered.<br>"
            + "Then post the resulting file in the JP1 Software forum for the experts to examine.</html><br>";
        JOptionPane.showMessageDialog( RemoteMaster.this, message, "Task error", JOptionPane.ERROR_MESSAGE );
        e.printStackTrace();
      }
      saveAction.setEnabled( file != null );
      saveAsAction.setEnabled( true );
      openRdfAction.setEnabled( true );
      installExtenderItem.setEnabled( file == null || admin );
      cleanUpperMemoryItem.setEnabled( true );
      initializeTo00Item.setEnabled( true );
      initializeToFFItem.setEnabled( true );
      setBaselineItem.setEnabled( true );
      uploadAction.setEnabled( true );
      resetSegmentPanel();
      update();
      changed = remoteConfig != null ? !Hex.equals( remoteConfig.getSavedData(), remoteConfig.getData() ) : false;
      if ( file != null )
      {
        setTitleFile( file );
      }
      setInterfaceState( null );
      return;
    }
  };

  private class UploadSystemFile extends SwingWorker< Void, Void >
  {
    private File file = null;
    
    private UploadSystemFile( File file )
    {
      this.file = file;
    }
    
    @Override
    protected Void doInBackground() throws Exception
    {
      IO io = getOpenInterface( null, Use.UPLOAD );
      if ( io == null )  
      {
        JOptionPane.showMessageDialog( RemoteMaster.this, "No remotes found!" );
        setInterfaceState( null );
        return null;
      }
      System.err.println( "Interface opened successfully" );
      if ( !( io instanceof CommHID ) )
      {
        JOptionPane.showMessageDialog( RemoteMaster.this, "Not an XSight remote!" );
        setInterfaceState( null );
        return null;
      }

      System.err.println( "Uploading system file " + file.getName() );
      ( ( CommHID )io ).writeSystemFile( file );
      if ( verifyUploadItem.isSelected() )
      {
        System.err.println( "Verifying upload." );
        byte[] readBytes = ( ( CommHID )io ).readSystemFile( file.getName() );
        io.closeRemote();
        byte[] fileBytes = RemoteMaster.readBinary( file );

        if ( readBytes.length != fileBytes.length )
        {
          System.err.println( "Upload verify failed: read back " + readBytes.length
              + " bytes, but expected " + fileBytes.length + "." );
          JOptionPane.showMessageDialog( RemoteMaster.this, "Upload verify failed: read back " + readBytes.length
              + " bytes, but expected " + fileBytes.length );
        }
        else if ( !Arrays.equals( readBytes, fileBytes ) )
        {
          System.err.println( "Upload verify failed: data read back doesn't match data written." );
          JOptionPane.showMessageDialog( RemoteMaster.this,
              "Upload verify failed: data read back doesn't match data written." );
        }
        else
        {
          System.err.println( "Upload verification succeeded." );
        }
      }
      else
      {
        io.closeRemote();
        JOptionPane.showMessageDialog( RemoteMaster.this, "Upload complete!" );
      }
      System.err.println( "Ending upload" );
      setInterfaceState( null );
      return null;
    }
  }
  
  private class UploadTask extends WriteTask
  {
    private short[] data;
    private boolean allowClockSet;
    private File file = null;
    private Use use = null;

    private UploadTask( short[] data, boolean allowClockSet )
    {
      this.data = data;
      this.allowClockSet = allowClockSet;
      this.file = null;
      use = Use.UPLOAD;
    }
    
    private UploadTask( File file, short[] data, boolean allowClockSet, boolean forSaveAs )
    {
      this.data = data;
      this.allowClockSet = allowClockSet;
      this.file = file;
      use = forSaveAs ? Use.SAVEAS : Use.SAVING;
    }

    @Override
    protected Void doInBackground() throws Exception
    {
      resetInterfaceState();
      Remote remote = remoteConfig.getRemote();
      IO io = getOpenInterface( file, use );
      if ( io == null )  
      {
        JOptionPane.showMessageDialog( RemoteMaster.this, "No remotes found!" );
        setInterfaceState( null );
        return null;
      }
      System.err.println( "Interface opened successfully" );
      if ( use == Use.UPLOAD )
      {
        int baseAddress = io.getRemoteEepromAddress();
        System.err.println( "Base address = $" + Integer.toHexString( baseAddress ).toUpperCase() );
        String sig = getIOsignature( io, baseAddress );
        if ( sig.length() > 8 ) // JP1.4/JP2 full signature
        {
          sig = sig.substring( 0, 6 );
        }
        String rSig = remote.getSignature();
        String message = null;
        if ( sig.length() < rSig.length() || !rSig.equals( sig.substring( 0, rSig.length() ) ) )
        {
          message = "The signature of the attached remote does not match the signature you are trying to upload.  The image\n"
              + "you are trying to upload may not be compatible with attached remote, and uploading it may damage the\n"
              + "remote.  Copying the contents of one remote to another is only safe when the remotes are identical.\n\n"
              + "This message will be displayed when installing an extender in your remote, which is the only time it is\n"
              + "safe to upload to a remote when the signatures do not match.\n\n"
              + "How would you like to proceed?";
        }
        else
        {
          message = "An upload overwrites the entire memory area for setup data in the remote and cannot\n"
              + "be undone.  Are you sure that you want to do this?";
        }
        Object[] options =
          {
            "Upload to the remote", "Cancel the upload"
          };
        int rc = JOptionPane.showOptionDialog( RemoteMaster.this, message,
            "Upload Confirmation", JOptionPane.DEFAULT_OPTION,
            JOptionPane.WARNING_MESSAGE, null, options, options[ 1 ] );
        if ( rc == 1 || rc == JOptionPane.CLOSED_OPTION )
        {
          io.closeRemote();
          setInterfaceState( null );
          return null;
        }
      }
      AutoClockSet autoClockSet = remote.getAutoClockSet();
      if ( allowClockSet && autoClockSet != null )
      {
        autoClockSet.saveTimeBytes( data );
        autoClockSet.setTimeBytes( data );
        remoteConfig.updateCheckSums();
      }

      int rc = io.writeRemote( remote.getBaseAddress(), data );

      if ( rc != data.length )
      {
        io.closeRemote();
        System.err.println( "Data writing phase failed, bytes written = " + rc + "instead of " + data.length + "." );
        JOptionPane.showMessageDialog( RemoteMaster.this, "writeRemote returned " + rc );
        setInterfaceState( null );
        return null;
      }
      else
      {
        System.err.println( "Data writing phase succeeded, bytes written = " + rc + "." );
      }
      if ( verifyUploadItem.isSelected() )
      {
        System.err.println( "Upload verification phase starting." );
        if ( io.getInterfaceType() == 0x106 )
        {
          io.closeRemote();
          io = getOpenInterface( null, Use.READING );
        }
        short[] readBack = new short[ data.length ];
        rc = io.readRemote( remote.getBaseAddress(), readBack );
        io.closeRemote();
        if ( rc != data.length )
        {
          System.err.println( "Upload verify failed: read back " + rc
              + " bytes, but expected " + data.length + "." );
          JOptionPane.showMessageDialog( RemoteMaster.this, "Upload verify failed: read back " + rc
              + " bytes, but expected " + data.length );

        }
        else if ( !Hex.equals( data, readBack ) )
        {
          System.err.println( "Upload verify failed: data read back doesn't match data written." );
          JOptionPane.showMessageDialog( RemoteMaster.this,
              "Upload verify failed: data read back doesn't match data written." );
        }
        else
        {
          System.err.println( "Upload verification succeeded." );
        }
      }
      else
      {
        io.closeRemote();
        JOptionPane.showMessageDialog( RemoteMaster.this, "Upload complete!" );
      }
      if ( allowClockSet && autoClockSet != null )
      {
        autoClockSet.restoreTimeBytes( data );
        remoteConfig.updateCheckSums();
      }
      System.err.println( "Ending upload" );
      setInterfaceState( null );
      if ( use == Use.SAVEAS )
      {
        RemoteMaster.this.file = file;
        setTitleFile( file );
        updateRecentFiles( file );
        saveAction.setEnabled( true );
      }
      return null;
    }
    
    @Override
    public void done()
    {
      super.done();
      if ( !ok )
      {
        String message = file != null ? "Error saving Simpleset file " + file.getName() : "Error uploading to remote";
        JOptionPane.showMessageDialog( RemoteMaster.this, message, "Task error", JOptionPane.ERROR_MESSAGE );
      }
      if ( exitPrompt )
      {
        exitPrompt = false;
        dispatchEvent( new WindowEvent( RemoteMaster.this, WindowEvent.WINDOW_CLOSING ) );
      }
    }
  }

  public static File getJreExecutable()
  { 
    String jreDirectory = System.getProperty( "java.home" ); 
    File javaExe = null; 
    if ( System.getProperty( "os.name" ).startsWith( "Windows" ) )
    { 
      javaExe = new File( jreDirectory, "bin/javaw.exe" ); 
    }
    else 
    { 
      javaExe = new File( jreDirectory, "bin/javaw" ); 
    } 
    if ( !javaExe.exists() )
    { 
      return null; 
    } 
    return javaExe;
  } 
  
  private static void runKM( String filename )
  {
    File javaExe = getJreExecutable();
    if ( javaExe == null )
    {
      System.err.println( "Unable to find java executable" );
      return;
    }
    try
    {
      Runtime r = Runtime.getRuntime();
      String classPath = System.getProperty( "java.class.path" );
      r.exec( new String[] { javaExe.getCanonicalPath(), "-cp", classPath, "com.hifiremote.jp1.RemoteMaster", "-rm", "-home", workDir.getAbsolutePath(), filename } );
    }
    catch ( IOException e )
    {
      e.printStackTrace();
    }
  }

  protected class RMAction extends AbstractAction
  {
    public RMAction( String text, String action, ImageIcon icon, String description, Integer mnemonic )
    {
      super( text, icon );
      putValue( ACTION_COMMAND_KEY, action );
      putValue( SHORT_DESCRIPTION, description );
      putValue( MNEMONIC_KEY, mnemonic );
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed( ActionEvent event )
    {
      finishEditing();
      try
      {
        String command = event.getActionCommand();
        if ( command.equals( "NEW" ) )
        {
          String title = "File > New";
          String message = "The menu option File > New is primarily intended for creating setups for remotes\n"
                         + "that you do not have, such as for sending to another person to test.  If you have the\n"
                         + "remote, it is strongly recommended that you start a new setup by doing a factory reset\n"
                         + "(981 command) and downloading it.  Although the ideal situation would be for\n"
                         + "File > New to create a factory reset state, for many remotes there are hidden settings\n"
                         + "that are not visible in RMIR whose initial values are not correctly set by File > New.\n"
                         + "These may adversely affect the operation of the remote when a setup created this way\n"
                         + "is uploaded.\n\nDo you wish to continue?";
          if ( JOptionPane.showConfirmDialog( RemoteMaster.this, message, title, JOptionPane.YES_NO_OPTION, 
              JOptionPane.WARNING_MESSAGE ) == JOptionPane.NO_OPTION )
          {
            return;
          }
          if ( !promptToSave() )
          {
            return;
          }
          Remote remote = RMNewDialog.showDialog( RemoteMaster.this );
          if ( remote == null )
          {
            return;
          }
          
          if ( remote.isLoaded() )
          {
            remote.needsLayoutWarning();
          }
          remote.load();
          if ( remote.isSSD() )
          {
            title = "New Remote Image";
            message = "RMIR cannot create a new remote image for this remote.";
            JOptionPane.showMessageDialog( RemoteMaster.this, message, title, JOptionPane.INFORMATION_MESSAGE );
            return;
          }

          ProtocolManager.getProtocolManager().reset();
          clearAllInterfaces();
          remoteConfig = new RemoteConfiguration( remote, RemoteMaster.this );
          recreateToolbar();
          remoteConfig.initializeSetup( 0 );
          remoteConfig.updateImage();
          remoteConfig.setDateIndicator();
          remoteConfig.setSavedData();
          update();
          saveAction.setEnabled( false );
          saveAsAction.setEnabled( true );
          openRdfAction.setEnabled( true );
          installExtenderItem.setEnabled( true );
          cleanUpperMemoryItem.setEnabled( true );
          initializeTo00Item.setEnabled( !interfaces.isEmpty() );
          initializeToFFItem.setEnabled( !interfaces.isEmpty() );
          uploadAction.setEnabled( !interfaces.isEmpty() );
        }
        else if ( command.equals( "NEWDEVICE" ) )
        {
          System.err.println( "RMIR opening new RM instance" );
          
          // Opening KM as a new instance of KeyMapMaster makes it share the same
          // ProtocolManager as RM, which results in crosstalk when one edits the protocol
          // used by the other.  Replaced now by opening KM as a new application.
          
//          new KeyMapMaster( properties );
          runKM( "" );
        }
        else if ( command.equals( "OPEN" ) )
        {
          openFile();
        }
        else if ( command.equals( "SAVE" ) )
        {
          save( file, false );
        }
        else if ( command.equals( "SAVEAS" ) )
        {
          if ( !allowSave( Remote.SetupValidation.WARN ) )
          {
            return;
          }
          saveAs();
        }
        else if ( command.equals( "DOWNLOAD" ) )
        {
          if ( !promptToSave() )
          {
            return;
          }
          System.err.println( "Starting normal download" );
          setInterfaceState( "DOWNLOADING..." );
          ( new DownloadTask() ).execute();
        }
        else if ( command.equals( "UPLOAD" ) )
        {
          boolean validConfiguration = updateUsage();
          if ( !validConfiguration )
          {
            String title = "Invalid Configuration";
            String message = "This configuration is not valid.  It cannot be uploaded as it\n"
                + "could cause the remote to crash.";
            JOptionPane.showMessageDialog( RemoteMaster.this, message, title, JOptionPane.WARNING_MESSAGE );
            return;
          }

          Remote remote = remoteConfig.getRemote();
          if ( !allowSave( remote.getSetupValidation() ) )
          {
            return;
          }
          remoteConfig.saveAltPIDs();
          System.err.println( "Starting upload" );
          setInterfaceState( "UPLOADING..." );
          ( new UploadTask( RemoteMaster.this.useSavedData() ? remoteConfig.getSavedData() : remoteConfig.getData(), true ) ).execute();
        }
        else if ( command == "OPENRDF" )
        {
          String title = "View/Edit RDF";
          rdfViewer = TextFileViewer.showFile( RemoteMaster.this, remoteConfig.getRemote(), title, false );
        }
        else if ( command == "OPENCODES" )
        {
          JP1Table deviceButtonTable = generalPanel.getDeviceButtonTable();
          if ( deviceButtonTable.getCellEditor() != null )
          {
            deviceButtonTable.getCellEditor().stopCellEditing();
          }
          codeSelectorDialog = CodeSelectorDialog.showDialog( RemoteMaster.this );
          codeSelectorDialog.enableAssign( currentPanel == generalPanel );
        }
        else if ( command == "HIGHLIGHT" )
        {
          JP1Table table = null;
          JP1TableModel< ? > model = null;
          TableSorter sorter = null;
          if ( currentPanel instanceof RMTablePanel< ? > )
          {
            RMTablePanel< ? > panel = ( RMTablePanel< ? > )currentPanel;
            table = panel.table;
            model = panel.model;
            sorter = panel.sorter;
          }
          else if ( currentPanel == generalPanel )
          {
            table = generalPanel.getActiveTable();
            model = ( JP1TableModel< ? > )table.getModel();
          }
          else if ( currentPanel == activityPanel )
          {
            table = activityPanel.getActiveTable();
            model = ( JP1TableModel< ? > )table.getModel();
          }
          else if ( currentPanel == favoritesPanel )
          {
            table = favoritesPanel.getActiveTable();
            model = ( JP1TableModel< ? > )table.getModel();
          }
          Color color = getInitialHighlight( table, 0 );
          Preview preview = ( Preview )colorChooser.getPreviewPanel();
          preview.reset( ( currentPanel == devicePanel ) && ( getInitialHighlight( table, 1 ) == null ) );
          preview.selectors.setVisible( currentPanel == devicePanel );
          colorChooser.setColor( color );
          colorDialog.pack();
          colorDialog.setVisible( true );
          color = preview.result;
          if ( table != null && color != null )
          {
            for ( int i : table.getSelectedRows() )
            {
              if ( currentPanel == keyMovePanel )
              {
                // Special case needed to handle attached keymoves
                model.setValueAt( color, sorter.modelIndex( i ), 9 );
              }
              else if ( currentPanel == devicePanel && preview.colorCol == 1 )
              {
                DeviceUpgrade du = devicePanel.getRowObject( i );
                if ( du.needsProtocolCode() )
                {
                  // Special case needed to handle consequential highlights
                  model.setValueAt( color, sorter.modelIndex( i ), model.getColumnCount() - 1 );
                }
              }
              else
              {
                Highlight rowObject = getTableRow( table, i );
                rowObject.setHighlight( color );
              }
            }
            model.fireTableDataChanged();
            model.propertyChangeSupport.firePropertyChange( "data", null, null );
            highlightAction.setEnabled( false );
          }
        }
      }
      catch ( Exception ex )
      {
        ex.printStackTrace( System.err );
      }
    }
  }

  public short[] getInitializationData( int value )
  {
    short[] data = null;
    String title = "Initialize EEPROM Area";
    String message = "This will fill your remote's EEPROM with $" + Hex.asString( value ) + "\n\n"
        + "Doing so will likely cause the remote to stop working until you\n"
        + "perform a hard reset.  Are you sure you want to do this?\n"
        + "(Make sure your current configuration is saved before proceeding.)";
    if ( NegativeDefaultButtonJOptionPane.showConfirmDialog( this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE ) == JOptionPane.YES_OPTION )
    {
      data = new short[ remoteConfig.getRemote().getEepromSize() ];
      Arrays.fill( data, 0, data.length, ( short )value );
    }
    return data;
  }

  private Highlight getTableRow( JP1Table table, int row )
  {
    Object obj;
    if ( row == -1 )
    {
      return null;
    }
    if ( currentPanel instanceof RMTablePanel< ? > )
    {
      obj = ( ( RMTablePanel< ? > )currentPanel ).getRowObject( row );
    }
    else
    {
      obj = ( ( JP1TableModel< ? > )table.getModel() ).getRow( row );
    }
    if ( obj instanceof Highlight )
    {
      return ( Highlight )obj;
    }
    return null;
  }

  private Color getInitialHighlight( JP1Table table, int colorCol )
  {
    Color color = null;
    if ( table != null )
    {
      int[] rows = table.getSelectedRows();
      if ( rows.length > 0 && getTableRow( table, rows[ 0 ] ) != null )
      {
        if ( currentPanel == devicePanel && colorCol == 1 )
        {
          for ( int i : rows )
          {
            DeviceUpgrade du = devicePanel.getRowObject( i );
            if ( !du.needsProtocolCode() )
            {
              continue;
            }
            if ( color == null )
            {
              color = du.getProtocolHighlight();
            }
            else if ( !du.getProtocolHighlight().equals( color ) )
            {
              return Color.WHITE;
            }
          }
        }
        else
        {
          color = getTableRow( table, rows[ 0 ] ).getHighlight();
          for ( int i : rows )
          {
            if ( !getTableRow( table, i ).getHighlight().equals( color ) )
            {
              return Color.WHITE;
            }
          }
        }
      }
    }
    return color;
  }

  /**
   * Constructor for the RemoteMaster object.
   * 
   * @param workDir
   *          the work dir
   * @param prefs
   *          the prefs
   * @throws Exception
   *           the exception
   * @exception Exception
   *              Description of the Exception
   */
  public RemoteMaster( File workDir, PropertyFile prefs ) throws Exception
  {
    super( "RMIR", prefs );
    dir = properties.getFileProperty( "IRPath", workDir );
    defaultToolTipTimeout = ToolTipManager.sharedInstance().getDismissDelay();

    toolBar = new JToolBar();
    toolBar.setFloatable( false );
    createMenus();
    createToolbar();

    setDefaultCloseOperation( DISPOSE_ON_CLOSE );
    setDefaultLookAndFeelDecorated( true );

    ProtocolManager.getProtocolManager().loadAltPIDRemoteProperties( properties );

    final Preview preview = new Preview();
    // If a non-empty border is not set then the preview panel does not appear. This sets
    // an invisible but non-empty border.
    preview.setBorder( BorderFactory.createLineBorder( preview.getBackground() ) );

    colorChooser = new JColorChooser();
    colorChooser.setPreviewPanel( preview );
    colorChooser.getSelectionModel().addChangeListener( new ChangeListener()
    {
      @Override
      public void stateChanged( ChangeEvent evt )
      {
        ColorSelectionModel model = ( ColorSelectionModel )evt.getSource();
        preview.sample.setBackground( model.getSelectedColor() );
      }
    } );

    colorDialog = JColorChooser.createDialog( this, "Highlight Color", true, colorChooser, new ActionListener()
    { // OK button listener
          @Override
          public void actionPerformed( ActionEvent event )
          {
            preview.result = colorChooser.getColor();
          }
        }, new ActionListener()
        { // Cancel button listener
          @Override
          public void actionPerformed( ActionEvent event )
          {
            preview.result = null;
          }
        } );

    setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
    addWindowListener( new WindowAdapter()
    {
      @Override
      public void windowClosing( WindowEvent event )
      {
        boolean doDispose = true;
        try
        {
          System.err.println( "RemoteMaster.windowClosing() entered" );
          boolean quit = false;
          if ( interfaceText != null )
          {
            String title = "Request to exit";
            String message = "A \"" + interfaceText + "\" task is in progress.  You risk corrupting it if you exit\n"
                           + "before it completes.  Are you sure you wish to continue?";
            quit = NegativeDefaultButtonJOptionPane.showConfirmDialog( RemoteMaster.this, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE ) == JOptionPane.NO_OPTION;
          }
          if ( !promptToSave( true ) || exitPrompt || quit )
          {
            System.err.println( "RemoteMaster.windowClosing() exited" );
            doDispose = false;
            return;
          }
          downloadAction = null;
          uploadAction = null;
          for ( int i = 0; i < recentFiles.getItemCount(); ++i )
          {
            JMenuItem item = recentFiles.getItem( i );
            properties.setProperty( "RecentIRs." + i, item.getActionCommand() );
          }
          properties.remove( "Primacy" );
          ProtocolManager.getProtocolManager().setAltPIDRemoteProperties( properties );
          int state = getExtendedState();
          if ( state != Frame.NORMAL )
          {
            setExtendedState( Frame.NORMAL );
          }
          Rectangle bounds = getBounds();
          properties
              .setProperty( "RMBounds", "" + bounds.x + ',' + bounds.y + ',' + bounds.width + ',' + bounds.height );

          properties.save();

          if ( generalPanel.getDeviceUpgradeEditor() != null )
          {
            generalPanel.getDeviceUpgradeEditor().dispose();
          }
          if ( keyMovePanel.getDeviceUpgradeEditor() != null )
          {
            keyMovePanel.getDeviceUpgradeEditor().dispose();
          }
          if ( devicePanel.getDeviceUpgradeEditor() != null )
          {
            devicePanel.getDeviceUpgradeEditor().dispose();
          }
        }
        catch ( Exception exc )
        {
          exc.printStackTrace( System.err );
        }
        finally
        {
          if ( doDispose )
          {
            dispose();
            // For some unknown reason, if map or rdf file folders had to be
            // set on startup, RMIR does not terminate on dispose(), so force
            // it in this case only.  It should not terminate if a RM
            // instance has been launched from it, so cannot force exit in
            // all cases.
            if ( RemoteManager.getRemoteManager().isFilesSet() )
            {
              System.exit( ABORT );
            }
          }
        }
      }
    } );

    addWindowStateListener( new WindowAdapter()
    {
      @Override
      public void windowStateChanged( WindowEvent e )
      {
        DeviceUpgradeEditor editor = devicePanel.getDeviceUpgradeEditor();
        if ( e.getNewState() == JFrame.NORMAL && editor != null && editor.getState() == JFrame.ICONIFIED )
        {
          editor.setExtendedState( NORMAL );
          editor.toFront();
        }
        else if ( e.getNewState() == JFrame.ICONIFIED && editor != null )
        {
          editor.setExtendedState( ICONIFIED );
        }
      }
    } );

    Container mainPanel = getContentPane();

    mainPanel.add( toolBar, BorderLayout.PAGE_START );

    // Set color for text on Progress Bars
    UIManager.put( "ProgressBar.selectionBackground", new javax.swing.plaf.ColorUIResource( Color.BLUE ) );
    UIManager.put( "ProgressBar.selectionForeground", new javax.swing.plaf.ColorUIResource( Color.BLUE ) );
    UIManager.put( "ProgressBar.foreground", new javax.swing.plaf.ColorUIResource( AQUAMARINE ) );

    tabbedPane = new JTabbedPane();
    mainPanel.add( tabbedPane, BorderLayout.CENTER );

    generalPanel = new GeneralPanel();
    tabbedPane.addTab( "General", generalPanel );
    generalPanel.addRMPropertyChangeListener( this );

    keyMovePanel = new KeyMovePanel();
    keyMovePanel.addRMPropertyChangeListener( this );

    macroPanel = new MacroPanel();
    macroPanel.addRMPropertyChangeListener( this );

    specialFunctionPanel = new SpecialFunctionPanel();
    specialFunctionPanel.addRMPropertyChangeListener( this );

    timedMacroPanel = new TimedMacroPanel();
    timedMacroPanel.addRMPropertyChangeListener( this );

    favScanPanel = new FavScanPanel();
    favScanPanel.addRMPropertyChangeListener( this );
    
    favoritesPanel = new FavoritesPanel();
    favoritesPanel.addRMPropertyChangeListener( this );

    devicePanel = new DeviceUpgradePanel();
    devicePanel.addRMPropertyChangeListener( this );

    protocolPanel = new ProtocolUpgradePanel();
    protocolPanel.addRMPropertyChangeListener( this );
    
    activityPanel = new ActivityPanel();
    activityPanel.addRMPropertyChangeListener( this );

    learnedPanel = new LearnedSignalPanel();
    if ( LearnedSignal.hasDecodeIR() )
      learnedPanel.addRMPropertyChangeListener( this );

    rawDataPanel = new RawDataPanel();
    tabbedPane.addTab( "Raw Data", rawDataPanel );
    rawDataPanel.addRMPropertyChangeListener( this );
    
    segmentPanel = new SegmentPanel();
    segmentPanel.addRMPropertyChangeListener( this );

    tabbedPane.addChangeListener( this );

    statusBar = new JPanel( new CardLayout() );
    mainPanel.add( statusBar, BorderLayout.SOUTH );

    memoryStatus = new JPanel();
    extraStatus = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 0 ));
    interfaceStatus = new JPanel();
    warningStatus = new JPanel();
    JLabel warningMessage = new JLabel( "DO NOT EDIT THIS MAIN WINDOW WHILE THE DEVICE EDITOR IS OPEN" );
    Font font = warningMessage.getFont().deriveFont( Font.BOLD );
    warningMessage.setFont( font );
    warningStatus.add( warningMessage );
    
    interfaceState = new JProgressBar();

    interfaceStatus.add( interfaceState );
    interfaceState.setIndeterminate( true );
    interfaceState.setStringPainted( true );
    interfaceState.setString( "" );
    Dimension d = interfaceState.getPreferredSize();
    d.width *= 3;
    interfaceState.setPreferredSize( d );

    statusBar.add( memoryStatus, "MEMORY" );
    statusBar.add( interfaceStatus, "INTERFACE" );
    statusBar.add(  warningStatus, "WARNING" );
    ( ( CardLayout )statusBar.getLayout() ).first( statusBar );

    advProgressLabel = new JLabel( "Move/Macro:" );
    memoryStatus.add( advProgressLabel );

    advProgressBar = new JProgressBar();
    advProgressBar.setStringPainted( true );
    advProgressBar.setString( "N/A" );
    memoryStatus.add( advProgressBar );

    extraStatus.add( Box.createHorizontalStrut( 1 ) );
    JSeparator sep = new JSeparator( SwingConstants.VERTICAL );
    d = sep.getPreferredSize();
    d.height = advProgressBar.getPreferredSize().height;
    sep.setPreferredSize( d );
    extraStatus.add( sep );
    interfaceStatus.add( Box.createVerticalStrut( d.height ) );

    extraStatus.add( new JLabel( "Upgrade:" ) );

    upgradeProgressBar = new JProgressBar();
    upgradeProgressBar.setStringPainted( true );
    upgradeProgressBar.setString( "N/A" );

    upgradeProgressPanel = new JPanel();
    upgradeProgressPanel.setPreferredSize( advProgressBar.getPreferredSize() );
    upgradeProgressPanel.setLayout( new BorderLayout() );

    devUpgradeProgressBar = new JProgressBar();
    devUpgradeProgressBar.setStringPainted( true );
    devUpgradeProgressBar.setString( "N/A" );
    devUpgradeProgressBar.setVisible( false );

    upgradeProgressPanel.add( upgradeProgressBar, BorderLayout.NORTH );
    upgradeProgressPanel.add( devUpgradeProgressBar, BorderLayout.SOUTH );

    extraStatus.add( upgradeProgressPanel );

    extraStatus.add( Box.createHorizontalStrut( 5 ) );
    sep = new JSeparator( SwingConstants.VERTICAL );
    sep.setPreferredSize( d );
    extraStatus.add( sep );

    extraStatus.add( new JLabel( "Learned:" ) );

    learnedProgressBar = new JProgressBar();
    learnedProgressBar.setStringPainted( true );
    learnedProgressBar.setString( "N/A" );
    extraStatus.add( learnedProgressBar );
    memoryStatus.add( extraStatus );

    String temp = properties.getProperty( "RMBounds" );
    if ( temp != null )
    {
      Rectangle bounds = new Rectangle();
      StringTokenizer st = new StringTokenizer( temp, "," );
      bounds.x = Integer.parseInt( st.nextToken() );
      bounds.y = Integer.parseInt( st.nextToken() );
      bounds.width = Integer.parseInt( st.nextToken() );
      bounds.height = Integer.parseInt( st.nextToken() );
      setBounds( bounds );
    }
    else
    {
      pack();
    }
    currentPanel = generalPanel;
    setVisible( true );
  }

  /**
   * Gets the frame attribute of the RemoteMaster class.
   * 
   * @return The frame value
   */
  public static JP1Frame getFrame()
  {
    return frame;
  }
  
  public boolean useSavedData()
  {
    return useSavedDataItem.isSelected();
  }
  
  public static boolean getSystemFiles()
  {
    return getSystemFilesItem.isSelected();
  }
  
  public static void setSystemFilesItems( RemoteMaster rm, Remote remote )
  {
    if ( admin && remote != null && remote.isSSD() )
    {
      getSystemFilesItem.setVisible( true );
      putSystemFileItem.setVisible( true );
      parseIRDBItem.setVisible( true );
      extractSSItem.setVisible( false );
      File file = new File( new File( workDir, "XSight" ), "irdb.bin" );
      parseIRDBItem.setEnabled( file.exists() && file.isFile() );
      return;
    }
    getSystemFilesItem.setVisible( false );
    getSystemFilesItem.setSelected( false );
    putSystemFileItem.setVisible( false );
    parseIRDBItem.setVisible( false );
    extractSSItem.setVisible( admin && ( remote != null && remote.usesSimpleset() || rm.binLoaded() != null ) );
    extractSSItem.setEnabled( admin && rm.binLoaded() != null );
  }
  
  public static byte[] readBinary( File file )
  {
    int totalBytesRead = 0;
    byte[] data = null;
    try
    {
      int length = ( int )file.length();
      if ( length == 0 )
      {
        System.err.println( "File " + file.getAbsolutePath() + " empty or not found" );
        return null;
      }
      data = new byte[ length ];
      InputStream input = null;
      try 
      {
        input = new BufferedInputStream(new FileInputStream( file ) );
        while( totalBytesRead < data.length )
        {
          int bytesRemaining = data.length - totalBytesRead;
          //input.read() returns -1, 0, or more :
          int bytesRead = input.read( data, totalBytesRead, bytesRemaining ); 
          if ( bytesRead > 0 )
          {
            totalBytesRead = totalBytesRead + bytesRead;
          }
        }
        /*
             the while loop usually has a single iteration only.
         */
        if ( totalBytesRead != length )
        {
          System.err.println( "File read error: file length = " + length + ", bytes read = " + totalBytesRead );
          return null;
        }
        System.err.println( "Bytes read from file: " + totalBytesRead );
      }
      catch (FileNotFoundException ex) {
        System.err.println( "File not found" );
        return null;
      }
      finally 
      {
        if ( input != null )
        {
          System.err.println("Closing input stream.");
          input.close();
        }
      }
    }
    catch (IOException ex) {
      System.err.println( ex );
      return null;
    }
    return data;
  }

  public static ImageIcon createIcon( String imageName )
  {
    String imgLocation = "toolbarButtonGraphics/general/" + imageName + ".gif";
    URLClassLoader sysloader = ( URLClassLoader )ClassLoader.getSystemClassLoader();

    java.net.URL imageURL = sysloader.getResource( imgLocation );

    if ( imageURL == null )
    {
      System.err.println( "Resource not found: " + imgLocation );
      return null;
    }
    else
    {
      return new ImageIcon( imageURL );
    }
  }

  /**
   * Description of the Method.
   */
  private void createMenus()
  {
    JMenuBar menuBar = new JMenuBar();
    setJMenuBar( menuBar );

    JMenu menu = new JMenu( "File" );
    menu.setMnemonic( KeyEvent.VK_F );
    menuBar.add( menu );

    JMenu newMenu = new JMenu( "New" );
    newMenu.setMnemonic( KeyEvent.VK_N );
    menu.add( newMenu );

    newAction = new RMAction( "Remote Image...", "NEW", createIcon( "RMNew24" ), "Create new file", KeyEvent.VK_R );
    newMenu.add( newAction ).setIcon( null );

    newUpgradeAction = new RMAction( "Device Upgrade", "NEWDEVICE", null, "Create new Device Upgrade", KeyEvent.VK_D );
    newMenu.add( newUpgradeAction );

    openAction = new RMAction( "Open...", "OPEN", createIcon( "RMOpen24" ), "Open a file", KeyEvent.VK_O );
    menu.add( openAction ).setIcon( null );

    saveAction = new RMAction( "Save", "SAVE", createIcon( "Save24" ), "Save to file", KeyEvent.VK_S );
    saveAction.setEnabled( false );
    menu.add( saveAction ).setIcon( null );

    saveAsAction = new RMAction( "Save as...", "SAVEAS", createIcon( "SaveAs24" ), "Save to a different file",
        KeyEvent.VK_A );
    saveAsAction.setEnabled( false );
    JMenuItem menuItem = menu.add( saveAsAction );
    menuItem.setDisplayedMnemonicIndex( 5 );
    menuItem.setIcon( null );

    // revertItem = new JMenuItem( "Revert to saved" );
    // revertItem.setMnemonic( KeyEvent.VK_R );
    // revertItem.addActionListener( this );
    // menu.add( revertItem );

    menu.addSeparator();

    installExtenderItem = new JMenuItem( "Install Extender..." );
    installExtenderItem.setMnemonic( KeyEvent.VK_I );
    installExtenderItem.addActionListener( this );
    installExtenderItem.setEnabled( false );
    menu.add( installExtenderItem );

    menu.addSeparator();

    JMenu menuSetDirectory = new JMenu( "Set Directory" );
    menuSetDirectory.setMnemonic( KeyEvent.VK_D );
    menu.add( menuSetDirectory );

    rdfPathItem = new JMenuItem( "RDF Path..." );
    rdfPathItem.setMnemonic( KeyEvent.VK_R );
    rdfPathItem.addActionListener( this );
    menuSetDirectory.add( rdfPathItem );

    mapPathItem = new JMenuItem( "Image Path..." );
    mapPathItem.setMnemonic( KeyEvent.VK_I );
    mapPathItem.addActionListener( this );
    menuSetDirectory.add( mapPathItem );

    menu.addSeparator();

    recentFiles = new JMenu( "Recent" );
    menu.add( recentFiles );
    recentFiles.setEnabled( false );
    for ( int i = 0; i < 10; i++ )
    {
      String propName = "RecentIRs." + i;
      String temp = properties.getProperty( propName );
      if ( temp == null )
      {
        break;
      }
      properties.remove( propName );
      File f = new File( temp );
      if ( f.canRead() )
      {
        JMenuItem item = new JMenuItem( temp );
        item.setActionCommand( temp );
        item.addActionListener( this );
        recentFiles.add( item );
      }
    }
    if ( recentFiles.getItemCount() > 0 )
    {
      recentFiles.setEnabled( true );
    }
    menu.addSeparator();

    exitItem = new JMenuItem( "Exit", KeyEvent.VK_X );
    exitItem.addActionListener( this );
    menu.add( exitItem );

    menu = new JMenu( "Remote" );
    menu.setMnemonic( KeyEvent.VK_R );
    menuBar.add( menu );

    File userDir = workDir;
    try
    {
      JP12Serial jp12Serial = new JP12Serial( userDir );
      interfaces.add( jp12Serial );
      System.err.println( "    JP12Serial version " + jp12Serial.getInterfaceVersion() );
    }
    catch ( LinkageError le )
    {
      System.err.println( "Unable to create JP12Serial object: " + le.getMessage() );
    }

    try
    {
      CommHID commHID = new CommHID( userDir );
      interfaces.add( commHID );
      System.err.println( "    CommHID version " + commHID.getInterfaceVersion() );
    }
    catch ( LinkageError le )
    {
      System.err.println( "Unable to create CommHID object: " + le.getMessage() );
    }
    
    try
    {
      JP1USB jp1usb = new JP1USB( userDir );
      interfaces.add( jp1usb );
      System.err.println( "    JP1USB version " + jp1usb.getInterfaceVersion() );
//      System.err.println( "    EEPROM size returns " + jp1usb.getRemoteEepromSize() );
//      System.err.println( "    EEPROM address returns " + jp1usb.getRemoteEepromAddress() );
    }
    catch ( LinkageError le )
    {
      System.err.println( "Unable to create JP1USB object: " + le.getMessage() );
    }
    
    try
    {
      JPS jps = new JPS( userDir );
      interfaces.add( jps );
      System.err.println( "    JPS version " + jps.getInterfaceVersion() );
    }
    catch ( LinkageError le )
    {
      System.err.println( "Unable to create JPS object: " + le.getMessage() );
    }

    try
    {
      JP1Parallel jp1Parallel = new JP1Parallel( userDir );
      interfaces.add( jp1Parallel );
      System.err.println( "    JP1Parallel version " + jp1Parallel.getInterfaceVersion() );
//      System.err.println( "    EEPROM size returns " + jp1Parallel.getRemoteEepromSize() );
//      System.err.println( "    EEPROM address returns " + jp1Parallel.getRemoteEepromAddress() );
    }
    catch ( LinkageError le )
    {
      System.err.println( "Unable to create JP1Parallel object: " + le.getMessage() );
    }

    /*
     * try { JP2Serial jp2Serial = new JP2Serial( userDir ); interfaces.add( jp2Serial ); System.err.println(
     * "    JP12Serial version " + jp2Serial.getInterfaceVersion() ); } catch ( LinkageError le ) { System.err.println(
     * "Unable to create JP12Serial object: " + le.getMessage() ); }
     */

    ActionListener interfaceListener = new ActionListener()
    {
      public void actionPerformed( ActionEvent event )
      {
        String command = event.getActionCommand();
        if ( command.equals( "autodetect" ) )
        {
          properties.remove( "Interface" );
          properties.remove( "Port" );
          return;
        }

        for ( IO io : interfaces )
        {
          if ( io.getInterfaceName().equals( command ) )
          {
            String defaultPort = null;
            if ( command.equals( properties.getProperty( "Interface" ) ) )
            {
              defaultPort = properties.getProperty( "Port" );
            }

            String[] availablePorts = io.getPortNames();

            PortDialog d = new PortDialog( RemoteMaster.this, availablePorts, defaultPort );
            if ( command.equals( "JPS" ) && defaultPort == null && ( ( JPS )io ).isOpen() )
            {
              d.setOtherPort( ( ( JPS )io ).getFilePath() );
            }
            
            d.setVisible( true );
            if ( d.getUserAction() == JOptionPane.OK_OPTION )
            {
              String port = d.getPort();
              properties.setProperty( "Interface", io.getInterfaceName() );
              if ( port == null || port.equals( PortDialog.AUTODETECT ) )
              {
                properties.remove( "Port" );
              }
              else
              {
                properties.setProperty( "Port", port );
              }
            }

            break;
          }
        }
      }
    };

    if ( !interfaces.isEmpty() )
    {
      JMenu subMenu = new JMenu( "Interface" );
      menu.add( subMenu );
      subMenu.setMnemonic( KeyEvent.VK_I );
      ButtonGroup group = new ButtonGroup();
      String preferredInterface = properties.getProperty( "Interface" );
      JRadioButtonMenuItem item = new JRadioButtonMenuItem( "Auto-detect" );
      item.setActionCommand( "autodetect" );
      item.setSelected( preferredInterface == null );
      subMenu.add( item );
      group.add( item );
      item.setMnemonic( KeyEvent.VK_A );
      item.addActionListener( interfaceListener );

      ListIterator< IO > it = interfaces.listIterator();
      while ( it.hasNext() )
      {
        IO io = it.next();
        try
        {
          String ioName = io.getInterfaceName();
          item = new JRadioButtonMenuItem( ioName + "..." );
          item.setActionCommand( ioName );
          item.setSelected( ioName.equals( preferredInterface ) );
          subMenu.add( item );
          group.add( item );
          item.addActionListener( interfaceListener );
        }
        catch ( UnsatisfiedLinkError ule )
        {
          it.remove();
          String className = io.getClass().getName();
          int dot = className.lastIndexOf( '.' );
          if ( dot != -1 )
          {
            className = className.substring( dot + 1 );
          }
          JOptionPane.showMessageDialog( this, "An incompatible version of the " + className
              + " driver was detected.  You will not be able to download or upload using that driver.",
              "Incompatible Driver", JOptionPane.ERROR_MESSAGE );
          ule.printStackTrace( System.err );
        }
      }
    }

    downloadAction = new RMAction( "Download from Remote", "DOWNLOAD", createIcon( "Import24" ),
        "Download from the attached remote", KeyEvent.VK_D );
    downloadAction.setEnabled( !interfaces.isEmpty() );
    menu.add( downloadAction ).setIcon( null );

    uploadAction = new RMAction( "Upload to Remote", "UPLOAD", createIcon( "Export24" ),
        "Upload to the attached remote", KeyEvent.VK_U );
    uploadAction.setEnabled( false );
    menu.add( uploadAction ).setIcon( null );

    openRdfAction = new RMAction( "Open RDF...", "OPENRDF", createIcon( "RMOpenRDF24" ), "Open RDF to view or edit",
        null );
    openRdfAction.setEnabled( false );

    codesAction = new RMAction( "Code Selector...", "OPENCODES", createIcon( "RMCodes24" ), "Open Code Selector", null );
    codesAction.setEnabled( false );

    highlightAction = new RMAction( "Highlight...", "HIGHLIGHT", createIcon( "RMHighlight24" ),
        "Select highlight color", null );
    highlightAction.setEnabled( false );

    uploadWavItem = new JMenuItem( "Create WAV", KeyEvent.VK_W );
    uploadWavItem.setVisible( false );
    uploadWavItem.addActionListener( this );
    menu.add( uploadWavItem );

    menu.addSeparator();
    downloadRawItem = new JMenuItem( "Raw download", KeyEvent.VK_R );
    downloadRawItem.setEnabled( true );
    downloadRawItem.addActionListener( this );
    menu.add( downloadRawItem );

    menu.addSeparator();
    verifyUploadItem = new JCheckBoxMenuItem( "Verify after upload" );
    verifyUploadItem.setMnemonic( KeyEvent.VK_V );
    verifyUploadItem.setSelected( Boolean.parseBoolean( properties.getProperty( "verifyUpload", "true" ) ) );
    verifyUploadItem.addActionListener( this );
    menu.add( verifyUploadItem );

    menu = new JMenu( "Options" );
    menu.setMnemonic( KeyEvent.VK_O );
    menuBar.add( menu );

    JMenu subMenu = new JMenu( "Look and Feel" );
    subMenu.setMnemonic( KeyEvent.VK_L );
    menu.add( subMenu );

    highlightItem = new JCheckBoxMenuItem( "Highlighting" );
    highlightItem.setMnemonic( KeyEvent.VK_H );
    highlightItem.setSelected( Boolean.parseBoolean( properties.getProperty( "highlighting", "false" ) ) );
    highlightItem.addActionListener( this );
    menu.add( highlightItem );

    enablePreserveSelection = new JCheckBoxMenuItem( "Allow Preserve Control" );
    enablePreserveSelection.setMnemonic( KeyEvent.VK_A );
    enablePreserveSelection.setSelected( Boolean.parseBoolean( properties.getProperty( "enablePreserveSelection", "false" ) ) );
    enablePreserveSelection.addActionListener( this );
    enablePreserveSelection.setToolTipText( "<html>Allow control of which function data is preserved when changing the protocol used in a device upgrade.<br>Do not use this unless you know what you are doing and why.</html>" );
    menu.add( enablePreserveSelection );
    
    showSlingboxProtocols = new JCheckBoxMenuItem( "Show Slingbox protocols" );
    showSlingboxProtocols.setMnemonic( KeyEvent.VK_X );
    showSlingboxProtocols.setSelected( Boolean.parseBoolean( properties.getProperty( "ShowSlingboxProtocols", "false" ) ) );
    showSlingboxProtocols.addActionListener( this );
    showSlingboxProtocols.setToolTipText( "<html>Include the no-repeat protocols that are specific to Slingbox usage.<br>"
        + "Note that a change to this option only takes effect when RM or RMIR<br>is next opened.</html>" );
    menu.add( showSlingboxProtocols );
    
    JMenu tooltipSubMenu = new JMenu( "Set Tooltip Delay" );
    tooltipSubMenu.setMnemonic( KeyEvent.VK_T );
    tooltipSubMenu.setToolTipText( "<html>Set the delay between mouse pointer entering an object and the tooltip (help message)<br>"
        + "appearing.  This delay does not apply to cells in tables, where tooltips are primarily used<br>"
        + "to show the full content when it does not fit into the cell concerned.</html>" );
    menu.add( tooltipSubMenu );
    ToolTipManager tm = ToolTipManager.sharedInstance();
    tooltipDefaultDelay = tm.getInitialDelay();
    String temp = properties.getProperty( "TooltipDelay" );
    tooltipDelay = temp != null ? Integer.parseInt( temp ) : tooltipDefaultDelay;
    tm.setInitialDelay( tooltipDelay );
    
    ButtonGroup group = new ButtonGroup();
    defaultDelayItem = new JRadioButtonMenuItem( "Default delay" );
    defaultDelayItem.setSelected( temp == null );
    defaultDelayItem.addActionListener( this );
    tooltipSubMenu.add( defaultDelayItem );
    group.add( defaultDelayItem );
    specifiedDelayItem = new JRadioButtonMenuItem( "Specified delay..." );
    specifiedDelayItem.setSelected( temp != null );
    specifiedDelayItem.addActionListener( this );
    tooltipSubMenu.add( specifiedDelayItem );
    group.add( specifiedDelayItem );

    appendAdvancedOptions( menu );

    ActionListener al = new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        lfEvent = e;       
        SwingUtilities.invokeLater( new Runnable()
        {
          public void run()
          {
            try
            {
              String title = "Look and Feel";
              String message = "Due to a bug in Java, you may find it necessary to close and then re-open RMIR\n"
                  + "for it to work properly after a change of Look and Feel.  Moreover, you may need\n"
                  + "to use the menu item File > Exit to close it.  To abort the change press Cancel,\n"
                  + "otherwise press OK to continue.";
              if ( JOptionPane.showConfirmDialog( RemoteMaster.this, message, title, JOptionPane.OK_CANCEL_OPTION, 
                  JOptionPane.INFORMATION_MESSAGE ) == JOptionPane.CANCEL_OPTION )
              {
                String lf = UIManager.getLookAndFeel().getName();
                for ( JRadioButtonMenuItem item : lookAndFeelItems )
                {
                  if ( item.getText().equals( lf ) )
                  {
                    item.setSelected( true );
                    break;
                  }
                }
                return;
              }
              JRadioButtonMenuItem item = ( JRadioButtonMenuItem )lfEvent.getSource();
              String lf = item.getActionCommand();
              UIManager.setLookAndFeel( lf );
              KeyMoveTableModel.normalSelectedBGColor = UIManager.getColor( "Table.selectionBackground" );
              SwingUtilities.updateComponentTreeUI( RemoteMaster.this );
              RemoteMaster.this.pack();
              properties.setProperty( "LookAndFeel", lf );
            }
            catch ( Exception x )
            {
              x.printStackTrace( System.err );
            }
          }
        } );
      }
    };

    group = new ButtonGroup();
    String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
    lookAndFeelItems = new JRadioButtonMenuItem[ info.length ];
    for ( int i = 0; i < info.length; i++ )
    {
      JRadioButtonMenuItem item = new JRadioButtonMenuItem( info[ i ].getName() );
      lookAndFeelItems[ i ] = item;
      item.setMnemonic( item.getText().charAt( 0 ) );
      item.setActionCommand( info[ i ].getClassName() );
      group.add( item );
      subMenu.add( item );
      if ( item.getActionCommand().equals( lookAndFeel ) )
      {
        item.setSelected( true );
      }
      item.addActionListener( al );
    }

    menu = new JMenu( "Advanced" );
    menu.setMnemonic( KeyEvent.VK_A );
    menuBar.add( menu );

    cleanUpperMemoryItem = new JMenuItem( "Blast Upper Memory...", KeyEvent.VK_B );
    cleanUpperMemoryItem.setEnabled( false );
    cleanUpperMemoryItem.addActionListener( this );
    menu.add( cleanUpperMemoryItem );

    clearAltPIDHistory = new JMenuItem( "Clear Alt PID History...", KeyEvent.VK_H );
    clearAltPIDHistory.addActionListener( this );
    menu.add( clearAltPIDHistory );

    menu.addSeparator();

    initializeTo00Item = new JMenuItem( "Initialize to $00", KeyEvent.VK_0 );
    initializeTo00Item.setEnabled( false );
    initializeTo00Item.addActionListener( this );
    menu.add( initializeTo00Item );

    initializeToFFItem = new JMenuItem( "Initialize to $FF", KeyEvent.VK_F );
    initializeToFFItem.setEnabled( false );
    initializeToFFItem.addActionListener( this );
    menu.add( initializeToFFItem );
    
    menu.addSeparator();
    
    useSavedDataItem = new JCheckBoxMenuItem( "Preserve original data" );
    useSavedDataItem.setSelected( false );
    useSavedDataItem.addActionListener( this );
    useSavedDataItem.setToolTipText( 
        "<html>When selected, the Raw Data tab displays the data as downloaded from the remote<br>"
        + "or loaded from a file, free from any changes made to it by RMIR.  An upload to the<br>"
        + "remote also uploads this unmodified data.</html>");
    menu.add( useSavedDataItem );
    
    setBaselineItem = new JMenuItem( "Set baseline" );
    setBaselineItem.addActionListener( this );
    setBaselineItem.setEnabled( false );
    setBaselineItem.setToolTipText( 
        "<html>Sets baseline to either normal or original data, depending on whether or not<br>"
        + "\"Preserve original data\" is selected.  When a baseline is set, the Raw Data tab<br>"
        + "highlights differences between current and baseline data in RED.</html>" );
    menu.add( setBaselineItem );
    clearBaselineItem = new JMenuItem( "Clear baseline" );
    clearBaselineItem.addActionListener( this );
    clearBaselineItem.setEnabled( false );
    clearBaselineItem.setToolTipText( 
        "Clears the baseline so that the Raw Data tab no longer highlights differences from it" );
    menu.add( clearBaselineItem );
    
    menu.addSeparator();

    getSystemFilesItem = new JCheckBoxMenuItem( "Get system files" );
    getSystemFilesItem.setToolTipText( "<html>When checked, a download from the remote also copies<br>"
        + "the system files of the remote to the XSight subfolder<br>"
        + "of the installation folder, creating it if it does not<br>"
        + "exist.</html>" );
    getSystemFilesItem.setVisible( false );
    getSystemFilesItem.setSelected( false );
    menu.add( getSystemFilesItem );

    putSystemFileItem = new JMenuItem( "Put system file..." );
    putSystemFileItem.setToolTipText( "<html>Uploads a system file to the remote, selected from<br>"
        + "the XSight subfolder of the installation folder.</html>" );
    putSystemFileItem.setVisible( false );
    putSystemFileItem.addActionListener( this );
    menu.add( putSystemFileItem );

    parseIRDBItem = new JMenuItem( "Extract from irdb.bin" );
    parseIRDBItem.setToolTipText( "<html>Extracts data for an RDF from the copy of the irdb.bin<br>"
        + "system file in the XSight subfolder of the installation<br>"
        + "folder.  You must first use \"Get system files\" to copy<br>"
        + "this and other system files from the remote to this folder.</html>" );
    parseIRDBItem.setVisible( false );
    parseIRDBItem.addActionListener( this );
    menu.add( parseIRDBItem );
    
    extractSSItem = new JMenuItem( "Extract from simpleset binary" );
    extractSSItem.setToolTipText( "<html>Extracts data for an RDF from the currently loaded<br>"
        + "simpleset .bin file.</html>" );
    extractSSItem.setVisible( false );
    extractSSItem.addActionListener( this );
    menu.add( extractSSItem );
    
    analyzeMAXQprotocols = new JMenuItem( "Analyze MAXQ protocols" );
    analyzeMAXQprotocols.setVisible( admin );
    analyzeMAXQprotocols.addActionListener( this );
    menu.add( analyzeMAXQprotocols );

    menu = new JMenu( "Help" );
    menu.setMnemonic( KeyEvent.VK_H );
    menuBar.add( menu );

    if ( desktop != null )
    {
      readmeItem = new JMenuItem( "Readme", KeyEvent.VK_R );
      readmeItem.addActionListener( this );
      menu.add( readmeItem );

      tutorialItem = new JMenuItem( "Tutorial", KeyEvent.VK_T );
      tutorialItem.addActionListener( this );
      menu.add( tutorialItem );

      learnedSignalItem = new JMenuItem( "Interpreting Decoded IR Signals", KeyEvent.VK_I );
      learnedSignalItem.addActionListener( this );
      menu.add( learnedSignalItem );

      menu.addSeparator();

      homePageItem = new JMenuItem( "Home Page", KeyEvent.VK_H );
      homePageItem.addActionListener( this );
      menu.add( homePageItem );

      wikiItem = new JMenuItem( "Wiki", KeyEvent.VK_W );
      wikiItem.addActionListener( this );
      menu.add( wikiItem );

      forumItem = new JMenuItem( "Forums", KeyEvent.VK_F );
      forumItem.addActionListener( this );
      menu.add( forumItem );

      menu.addSeparator();
    }

    updateItem = new JMenuItem( "Check for updates", KeyEvent.VK_C );
    updateItem.addActionListener( this );
    menu.add( updateItem );

    aboutItem = new JMenuItem( "About...", KeyEvent.VK_A );
    aboutItem.addActionListener( this );
    menu.add( aboutItem );
  }

  private void appendAdvancedOptions( JMenu menu )
  {
    ActionListener listener = new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        try
        {
          JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
          properties.setProperty( item.getActionCommand(), Boolean.toString( item.isSelected() ) );
          refreshTabbedPanes( item.getActionCommand().equals(  "ShowSegments" ) );
          if ( item.getActionCommand().equals( "NonModalDeviceEditor") )
          {
            setNonModalWarning( Boolean.parseBoolean( properties.getProperty( item.getActionCommand(), "false" ) ), null );
          }
          else if ( item.getActionCommand().equals( "SuppressDeletePrompts") )
          {
            TablePanel.suppressDeletePrompts = item.isSelected();
            RMTablePanel.suppressDeletePrompts = item.isSelected();
          }
        }
        catch ( Exception x )
        {
          x.printStackTrace( System.err );
        }
      }
    };

    // Advanced sub menu
    JMenu advancedSubMenu = new JMenu( "Advanced" );
    advancedSubMenu.setMnemonic( KeyEvent.VK_D );
    menu.addSeparator();
    menu.add( advancedSubMenu );
    JCheckBoxMenuItem item;

    item = new JCheckBoxMenuItem( "Show Segment Editor" );
    item.setActionCommand( "ShowSegments" );
    item.setSelected( Boolean.parseBoolean( properties.getProperty( item.getActionCommand(), "false" ) ) );
    item.addActionListener( listener );
    advancedSubMenu.add( item );
    
    item = new JCheckBoxMenuItem( "Use non-modal Device Editor" );
    item.setActionCommand( "NonModalDeviceEditor" );
    item.setSelected( Boolean.parseBoolean( properties.getProperty( item.getActionCommand(), "false" ) ) );
    item.addActionListener( listener );
    advancedSubMenu.add( item );
    
    advancedSubMenu.addSeparator();
    
    item = new JCheckBoxMenuItem( "Learned Signal Timing Analysis" );
    item.setActionCommand( "LearnedSignalTimingAnalysis" );
    item.setSelected( Boolean.parseBoolean( properties.getProperty( item.getActionCommand(), "false" ) ) );
    item.addActionListener( listener );
    advancedSubMenu.add( item );

    item = new JCheckBoxMenuItem( "Learn to Upgrade Conversion" );
    item.setActionCommand( "LearnUpgradeConversion" );
    item.setSelected( Boolean.parseBoolean( properties.getProperty( item.getActionCommand(), "false" ) ) );
    item.addActionListener( listener );
    advancedSubMenu.add( item );

    // Suppress Messages sub menu
    JMenu suppressSubMenu = new JMenu( "Suppress Messages" );
    suppressSubMenu.setMnemonic( KeyEvent.VK_S );
    menu.add( suppressSubMenu );

    item = new JCheckBoxMenuItem( "Key Move Detach/Delete" );
    item.setActionCommand( "SuppressKeyMovePrompts" );
    item.setSelected( Boolean.parseBoolean( properties.getProperty( item.getActionCommand(), "false" ) ) );
    item.addActionListener( listener );
    suppressSubMenu.add( item );
    
    item = new JCheckBoxMenuItem( "All table deletes" );
    item.setActionCommand( "SuppressDeletePrompts" );
    Boolean bval = Boolean.parseBoolean( properties.getProperty( item.getActionCommand(), "false" ) );
    item.setSelected( bval );
    TablePanel.suppressDeletePrompts = bval;
    RMTablePanel.suppressDeletePrompts = bval;
    item.addActionListener( listener );
    suppressSubMenu.add( item );
  }

  private void createToolbar()
  {
    toolBar.add( newAction );
    toolBar.add( openAction );
    toolBar.add( saveAction );
    toolBar.add( saveAsAction );
    toolBar.addSeparator();
    toolBar.add( downloadAction );
    toolBar.add( uploadAction );
    toolBar.addSeparator();
    toolBar.add( openRdfAction );
    toolBar.add( codesAction );
    if ( remoteConfig != null && remoteConfig.allowHighlighting() )
    {
      toolBar.add( highlightAction );
//      highlightAction.setEnabled( true );
    }
  }
  
  public void recreateToolbar()
  {
    highlightItem.setEnabled( remoteConfig != null && !remoteConfig.getRemote().isSSD() );
    Container mainPanel = getContentPane();
    mainPanel.remove( toolBar );
    toolBar = new JToolBar();
    toolBar.setFloatable( false );
    createToolbar();
    mainPanel.add( toolBar, BorderLayout.PAGE_START );
    mainPanel.validate();
  }

  /**
   * Gets the fileChooser attribute of the RemoteMaster object.
   * 
   * @return The fileChooser value
   */
  public RMFileChooser getFileChooser()
  {
    RMFileChooser chooser = new RMFileChooser( dir );
    EndingFileFilter irFilter = new EndingFileFilter( "All supported files", allEndings );
    chooser.addChoosableFileFilter( irFilter );
    chooser.addChoosableFileFilter( new EndingFileFilter( "RMIR files (*.rmir)", rmirEndings ) );
    chooser.addChoosableFileFilter( new EndingFileFilter( "IR files (*.ir)", irEndings ) );
    chooser.addChoosableFileFilter( new EndingFileFilter( "RM Device Upgrades (*.rmdu)", rmduEndings ) );
    chooser.addChoosableFileFilter( new EndingFileFilter( "KM Device Upgrades (*.txt)", txtEndings ) );
    chooser.addChoosableFileFilter( new EndingFileFilter( "Simpleset files (*.bin)", binEndings ) );
    chooser.addChoosableFileFilter( new EndingFileFilter( "Sling Learned Signals (*.xml)", slingEndings ) );
    chooser.setFileFilter( irFilter );

    return chooser;
  }

  public RMFileChooser getExtenderFileChooser()
  {
    RMFileChooser chooser = new RMFileChooser( mergeDir == null ? dir : mergeDir );
    EndingFileFilter irFilter = new EndingFileFilter( "Extender merge files (*.hex)", extenderEndings );
    chooser.setDialogTitle( "Select the file to merge" );
    chooser.addChoosableFileFilter( irFilter );
    chooser.addChoosableFileFilter( new EndingFileFilter( "Other merge files (*.ir, *.txt)", otherMergeEndings ) );
    chooser.addChoosableFileFilter( new EndingFileFilter( "All merge files", allMergeEndings ) );
    chooser.setFileFilter( irFilter );

    return chooser;
  }

  public RMFileChooser getFileSaveChooser( boolean validConfiguration )
  {
    RMFileChooser chooser = new RMFileChooser( dir );
    chooser.setAcceptAllFileFilterUsed( false );
    EndingFileFilter rmirFilter = new EndingFileFilter( "RM Remote Image (*.rmir)", rmirEndings );
    EndingFileFilter binFilter = new EndingFileFilter( "Simpleset file (*.bin)", binEndings );
    EndingFileFilter useFilter = rmirFilter;
    chooser.addChoosableFileFilter( rmirFilter );
    if ( validConfiguration )
    {
      chooser.addChoosableFileFilter( new EndingFileFilter( "IR file (*.ir)", irEndings ) );
    }
    if ( binLoaded() != null )
    {
      chooser.addChoosableFileFilter( binFilter );
      if ( file != null && file.getName().toLowerCase().endsWith( binEndings[ 0 ] ) )
      {
        useFilter = binFilter;
      }
    }
    chooser.setFileFilter( useFilter );
    return chooser;
  }

  private File getRDFPathChoice()
  {
    File result = null;
    File dir = properties.getFileProperty( "RDFPath" );
    RMDirectoryChooser chooser = new RMDirectoryChooser( dir, ".rdf", "RDF" );
    ChoiceArea area = new ChoiceArea( chooser );
    chooser.setAccessory( area );
    chooser.setDialogTitle( "Select RDF Directory" );
    if ( chooser.showDialog( this, "OK" ) == RMDirectoryChooser.APPROVE_OPTION )
    {
      result = chooser.getSelectedFile();
      if ( result.equals( dir ) )
      {
        result = null; // Not changed
      }
    }
    chooser.removePropertyChangeListener( area );
    return result;
  }

  private File getMapPathChoice()
  {
    File result = null;
    File dir = properties.getFileProperty( "ImagePath" );
    RMDirectoryChooser chooser = new RMDirectoryChooser( dir, ".map", "Map and Image" );
    ChoiceArea area = new ChoiceArea( chooser );
    chooser.setAccessory( area );
    chooser.setDialogTitle( "Select Map and Image Directory" );
    if ( chooser.showDialog( this, "OK" ) == RMDirectoryChooser.APPROVE_OPTION )
    {
      result = chooser.getSelectedFile();
      if ( result.equals( dir ) )
      {
        result = null; // Not changed
      }
    }
    chooser.removePropertyChangeListener( area );
    return result;
  }
  
  public File getSystemFileChoice()
  {
    // Selects an XSight system file for uploading
    File result = null;
    File dir = new File( workDir, "XSight" );
    if ( !dir.exists() )
    {
      System.err.println( "Folder " + dir.getAbsolutePath() + " not found" );
      return null;
    }
    
    while ( result == null )
    {
      RMFileChooser chooser = new RMFileChooser( dir );
      int returnVal = chooser.showOpenDialog( this );
      if ( returnVal == RMFileChooser.APPROVE_OPTION )
      {
        result = chooser.getSelectedFile();

        if ( !result.exists() )
        {
          JOptionPane.showMessageDialog( this, result.getName() + " doesn't exist.", "File doesn't exist.",
              JOptionPane.ERROR_MESSAGE );
        }
        else if ( result.isDirectory() )
        {
          JOptionPane.showMessageDialog( this, result.getName() + " is a directory.", "File doesn't exist.",
              JOptionPane.ERROR_MESSAGE );
        }
      }
      else
      {
        return null;
      }
    }
    return result;
  }
  
  public JPS binLoaded()
  {
    for ( IO io : interfaces )
    {
      if ( io.getInterfaceName().equals( "JPS" ) )
      {
        JPS jps = ( JPS )io;
        if ( jps.isOpen() )
        {
          return jps;
        }
        else
        {
          return null;
        }
      }
    }
    return null;
  }

  /**
   * Description of the Method.
   * 
   * @return Description of the Return Value
   * @throws Exception
   *           the exception
   * @exception Exception
   *              Description of the Exception
   */
  public void openFile() throws Exception
  {
    openFile( null );
  }

  /**
   * Description of the Method.
   * 
   * @param file
   *          the file
   * @return Description of the Return Value
   * @throws Exception
   *           the exception
   * @exception Exception
   *              Description of the Exception
   */
  public void openFile( File file ) throws Exception
  {
    if ( !promptToSave() )
    {
      return;
    }
    while ( file == null )
    {
      RMFileChooser chooser = getFileChooser();
      int returnVal = chooser.showOpenDialog( this );
      if ( returnVal == RMFileChooser.APPROVE_OPTION )
      {
        file = chooser.getSelectedFile();

        if ( !file.exists() )
        {
          JOptionPane.showMessageDialog( this, file.getName() + " doesn't exist.", "File doesn't exist.",
              JOptionPane.ERROR_MESSAGE );
        }
        else if ( file.isDirectory() )
        {
          JOptionPane.showMessageDialog( this, file.getName() + " is a directory.", "File doesn't exist.",
              JOptionPane.ERROR_MESSAGE );
        }
      }
      else
      {
        return;
      }
    }

    System.err.println( "Opening " + file.getCanonicalPath() + ", last modified "
        + DateFormat.getInstance().format( new Date( file.lastModified() ) ) );

    String ext = file.getName().toLowerCase();
    int dot = ext.lastIndexOf( '.' );
    ext = ext.substring( dot );

    dir = file.getParentFile();
    properties.setProperty( "IRPath", dir );

    if ( ext.equals( ".rmir" ) || ext.equals( ".bin" ) || ext.equals( ".ir" ) )
    {
      updateRecentFiles( file );
    }

    if ( ext.equals( ".rmdu" ) || ext.equals( ".txt" ) )
    {
      // Opening KM as a new instance of KeyMapMaster makes it share the same
      // ProtocolManager as RM, which results in crosstalk when one edits the protocol
      // used by the other.  Replaced now by opening KM as a new application.
      
//    KeyMapMaster km = new KeyMapMaster( properties );
//    km.loadUpgrade( file );
      
      runKM( file.getCanonicalPath() );
      return;
    }

    if ( ext.equals( ".xml" ) )
    {
      List< Remote > remotes = RemoteManager.getRemoteManager().findRemoteBySignature( "XMLLEARN" );
      if ( remotes.isEmpty() )
      {
        JOptionPane.showMessageDialog( RemoteMaster.getFrame(),
            "The RDF for XML Slingbox Learns was not found.  Please place it in the RDF folder and try again.",
            "Missing RDF File", JOptionPane.ERROR_MESSAGE );
        return;
      }
      Remote remote = remotes.get( 0 );
      remote.load();
      clearAllInterfaces();
      remoteConfig = new RemoteConfiguration( remote, this );
      recreateToolbar();
      remoteConfig.initializeSetup( 0 );
      remoteConfig.updateImage();
      remoteConfig.setDateIndicator();
      remoteConfig.setSavedData();
      SlingLearnParser.parse( file, remoteConfig );
      remoteConfig.updateImage();
      update();
      saveAction.setEnabled( false );
      saveAsAction.setEnabled( true );
      uploadAction.setEnabled( !interfaces.isEmpty() );
      openRdfAction.setEnabled( true );
      installExtenderItem.setEnabled( true );
      cleanUpperMemoryItem.setEnabled( true );
      initializeTo00Item.setEnabled( !interfaces.isEmpty() );
      initializeToFFItem.setEnabled( !interfaces.isEmpty() );
      setBaselineItem.setEnabled( true );
      return;
    }
    
    if ( ext.equals( ".bin" ) )
    {
      ProtocolManager.getProtocolManager().reset();
      System.err.println( "Starting Simpleset load" );
      setInterfaceState( "LOADING SIMPLESET..." );
      this.file = file;
      ( new DownloadTask( file ) ).execute();
      return;
    }

    // ext.equals( ".rmir" ) || ext.equals( ".ir" )
    ProtocolManager.getProtocolManager().reset();
    saveAction.setEnabled( ext.equals( ".rmir" ) );
    saveAsAction.setEnabled( true );
    openRdfAction.setEnabled( true );
    installExtenderItem.setEnabled( true );
    cleanUpperMemoryItem.setEnabled( true );
    initializeTo00Item.setEnabled( !interfaces.isEmpty() );
    initializeToFFItem.setEnabled( !interfaces.isEmpty() );
    setBaselineItem.setEnabled( true );
    uploadAction.setEnabled( !interfaces.isEmpty() );
    resetSegmentPanel();
    setInterfaceState( "LOADING..." );
    ( new LoadTask( file ) ).execute();
    return;
  }

  private void installExtender() throws Exception
  {
    if ( !promptToSave() )
    {
      return;
    }
    String version = ExtInstall.class.getPackage().getImplementationVersion();
    System.err.print( "Starting Java ExtInstall" );
    if ( version != null )
    {
      System.err.println( ", version " + version );
    }
    else
    {
      System.err.println();
    }

    File file = null;
    while ( file == null )
    {
      RMFileChooser chooser = getExtenderFileChooser();
      int returnVal = chooser.showOpenDialog( this );
      if ( returnVal == RMFileChooser.APPROVE_OPTION )
      {
        file = chooser.getSelectedFile();

        if ( !file.exists() )
        {
          JOptionPane.showMessageDialog( this, file.getName() + " doesn't exist.", "File doesn't exist.",
              JOptionPane.ERROR_MESSAGE );
        }
        else if ( file.isDirectory() )
        {
          JOptionPane.showMessageDialog( this, file.getName() + " is a directory.", "File doesn't exist.",
              JOptionPane.ERROR_MESSAGE );
        }
      }
      else
      {
        System.err.println( "ExtInstall cancelled by user." );
        return;
      }
    }

    System.err.println( "Merge file is " + file.getCanonicalPath() + ", last modified "
        + DateFormat.getInstance().format( new Date( file.lastModified() ) ) );

    mergeDir = file.getParentFile();
    String[] oldDevBtnNotes = remoteConfig.getDeviceButtonNotes();
    List< DeviceUpgrade > oldDevUpgrades = remoteConfig.getDeviceUpgrades();
    List< ProtocolUpgrade > oldProtUpgrades = remoteConfig.getProtocolUpgrades();

    RMExtInstall installer = new RMExtInstall( file.getAbsolutePath(), remoteConfig );
    installer.install();
    if ( binLoaded() != null || RMExtInstall.remoteConfig == null )
    {
      return;
    }
    if ( remoteConfig != RMExtInstall.remoteConfig )
    {
      remoteConfig = RMExtInstall.remoteConfig;
      recreateToolbar();
      String[] newDevBtnNotes = remoteConfig.getDeviceButtonNotes();
      Remote newRemote = remoteConfig.getRemote();
      List< DeviceUpgrade > newDevUpgrades = remoteConfig.getDeviceUpgrades();
      List< ProtocolUpgrade > newProtUpgrades = remoteConfig.getProtocolUpgrades();
      // Copy the old device button notes, as the installer deletes them.
      for ( int i = 0; i < Math.min( oldDevBtnNotes.length, newDevBtnNotes.length ); i++ )
      {
        newDevBtnNotes[ i ] = oldDevBtnNotes[ i ];
      }

      System.err.println( "Restoring .rmir data lost in conversion to .ir format." );

      int devCount = 0;
      int protCount = 0;
      int index = installer.isExtenderMerge() ? installer.getDevUpgradeCodes().size() : 0;
      for ( DeviceUpgrade duOld : oldDevUpgrades )
      {
        int codeOld = duOld.getHexSetupCodeValue();
        if ( installer.getDevUpgradeCodes().contains( Integer.valueOf( codeOld ) ) )
        {
          continue;
        }
        // Upgrade retained in merge, so restore it. The order is preserved by the merge
        // process but those to be restored may not be consecutive.
        boolean found = false;
        for ( ; index < newDevUpgrades.size(); index++ )
        {
          DeviceUpgrade duNew = newDevUpgrades.get( index );
          if ( duNew.getHexSetupCodeValue() == codeOld )
          {
            duOld.protocol = duNew.protocol;
            duOld.setNewRemote( newRemote );
            newDevUpgrades.set( index, duOld );
            devCount++ ;
            found = true;
            break;
          }
        }
        if ( !found )
        {
          System.err.println( "Error restoring device upgrades: failed at setup code = " + codeOld + "." );
        }
      }
      System.err.println( "Restored " + devCount + " device upgrades." );

      index = installer.isExtenderMerge() ? installer.getProtUpgradeIDs().size() : 0;
      for ( ProtocolUpgrade puOld : oldProtUpgrades )
      {
        int pidOld = puOld.getPid();
        if ( installer.getProtUpgradeIDs().contains( Integer.valueOf( pidOld ) ) )
        {
          continue;
        }
        boolean found = false;
        for ( ; index < newProtUpgrades.size(); index++ )
        {
          ProtocolUpgrade puNew = newProtUpgrades.get( index );
          if ( puNew.getPid() == pidOld )
          {
            // Only restore if not used by device upgrade. Restoring a used one would remove
            // the isUsed mark.
            if ( !puNew.isUsed() )
            {
              newProtUpgrades.set( index, puOld );
              protCount++ ;
            }
            found = true;
            break;
          }
        }
        if ( !found )
        {
          System.err.println( "Error restoring protocol upgrades: failed at PID = " + pidOld + "." );
        }
      }
      System.err.println( "Restored " + protCount + " protocol upgrades." );

      for ( Iterator< ProtocolUpgrade > it = newProtUpgrades.iterator(); it.hasNext(); )
      {
        ProtocolUpgrade pu = it.next();
        if ( pu.isUsed() )
        {
          it.remove();
        }
        else if ( installer.getProtUpgradeIDs().contains( Integer.valueOf( pu.getPid() ) ) )
        {
          // Add to ProtocolManager as manual protocol. It is only those in getProtUpgradeIDs()
          // that were removed before the merge, so it is only those that need to be put back and
          // the import process will have handled the ones that are used by device upgrades.
          pu.setManualProtocol( newRemote );
        }
      }

      remoteConfig.setDeviceUpgrades( newDevUpgrades );
      remoteConfig.setProtocolUpgrades( newProtUpgrades );
    }
    remoteConfig.updateImage();
    update();
    saveAction.setEnabled( false );
    System.err.println( "ExtInstall merge completed." );
  }

  /**
   * Description of the Method.
   * 
   * @param file
   *          the file
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @exception IOException
   *              Description of the Exception
   */
  private void updateRecentFiles( File file ) throws IOException
  {
    JMenuItem item = null;
    String path = file.getCanonicalPath();
    for ( int i = 0; i < recentFiles.getItemCount(); ++i )
    {
      File temp = new File( recentFiles.getItem( i ).getText() );

      if ( temp.getCanonicalPath().equals( path ) )
      {
        item = recentFiles.getItem( i );
        recentFiles.remove( i );
        break;
      }
    }
    if ( item == null )
    {
      item = new JMenuItem( path );
      item.setActionCommand( path );
      item.addActionListener( this );
    }
    recentFiles.insert( item, 0 );
    while ( recentFiles.getItemCount() > 10 )
    {
      recentFiles.remove( 10 );
    }
    recentFiles.setEnabled( true );
    dir = file.getParentFile();
    properties.setProperty( "IRPath", dir );
  }

  public void save( File file, boolean forSaveAs ) throws IOException
  {
    if ( file == null )
    {
      saveAs();
      return;
    }
    
    if ( file.exists() && !file.canWrite() )
    {
      String title = "File Save Error";
      String message = "The file\n" + file.getCanonicalPath()
          + "\nis read-only.  Please use \"Save As...\" to save to a different file.";
      JOptionPane.showMessageDialog( this, message, title, JOptionPane.ERROR_MESSAGE );
      return;
    }
    
    String ext = file.getName().toLowerCase();
    int dot = ext.lastIndexOf( '.' );
    ext = ext.substring( dot );
    if ( ext.equals( ".bin" ) )
    {
      boolean validConfiguration = updateUsage();
      if ( !validConfiguration )
      {
        String title = "Invalid Configuration";
        String message = "This configuration is not valid.  It cannot be saved as\n"
            + "copying it to the remote could cause the remote to crash.";
        JOptionPane.showMessageDialog( RemoteMaster.this, message, title, JOptionPane.WARNING_MESSAGE );
        return;
      }

      Remote remote = remoteConfig.getRemote();
      if ( !allowSave( remote.getSetupValidation() ) )
      {
        return;
      }
      remoteConfig.saveAltPIDs();
      System.err.println( "Saving Simpleset" );
      setInterfaceState( "SAVING SIMPLESET..." );
      changed = false;
      ( new UploadTask( file, RemoteMaster.this.useSavedData() ? remoteConfig.getSavedData() : remoteConfig.getData(), false, forSaveAs ) ).execute();
    }
    else
    {
      boolean validConfiguration = updateUsage();
      if ( !allowSave( Remote.SetupValidation.WARN ) )
      {
        return;
      }
      if ( !validConfiguration )
      {
        String title = "Invalid Configuration";
        String message = "This configuration is not valid, but it can be saved and then\n"
            + "re-loaded to give again this same invalid configuration.\n\n" + "Do you wish to continue?";
        if ( NegativeDefaultButtonJOptionPane.showConfirmDialog( RemoteMaster.this, message, title, JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE ) == JOptionPane.NO_OPTION )
        {
          return;
        }
      }
      setInterfaceState( "SAVING..." );
      ( new SaveTask( file, forSaveAs ? Use.SAVEAS : Use.SAVING ) ).execute();
    }
  }

  /**
   * Description of the Method.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @exception IOException
   *              Description of the Exception
   */
  public void saveAs() throws IOException
  {   
    boolean validConfiguration = updateUsage();
    if ( !validConfiguration )
    {
      String title = "Invalid Configuration";
      String message = "This configuration is not valid.  It can be saved as a .rmir file\n"
          + "which can be re-loaded to give again this same invalid configuration,\n"
          + "but it cannot be saved as a .ir file as it could cause the remote\n"
          + "to crash if it were uploaded to it by another application.";
      JOptionPane.showMessageDialog( RemoteMaster.this, message, title, JOptionPane.WARNING_MESSAGE );
    }
    RMFileChooser chooser = getFileSaveChooser( validConfiguration );
    File oldFile = file;
    if ( oldFile != null )
    {
      String name = oldFile.getName();
      if ( name.toLowerCase().endsWith( ".ir" ) || name.toLowerCase().endsWith( ".txt" ) )
      {
        int dot = name.lastIndexOf( '.' );
        name = name.substring( 0, dot ) + ".rmir";
        oldFile = new File( name );
      }
      chooser.setSelectedFile( oldFile );
    }
    int returnVal = chooser.showSaveDialog( this );
    if ( returnVal == RMFileChooser.APPROVE_OPTION )
    {
      String ending = ( ( EndingFileFilter )chooser.getFileFilter() ).getEndings()[ 0 ];
      String name = chooser.getSelectedFile().getAbsolutePath();
      if ( !name.toLowerCase().endsWith( ending ) )
      {
        if ( name.toLowerCase().endsWith( ".rmir" ) || name.toLowerCase().endsWith( ".bin" ) )
        {
          int dot = name.lastIndexOf( '.' );
          name = name.substring( 0, dot );
        }
        name = name + ending;
      }
      File newFile = new File( name );
      int rc = JOptionPane.YES_OPTION;
      if ( newFile.exists() )
      {
        rc = JOptionPane.showConfirmDialog( this, newFile.getName() + " already exists.  Do you want to replace it?",
            "Replace existing file?", JOptionPane.YES_NO_OPTION );
      }

      if ( rc != JOptionPane.YES_OPTION )
      {
        return;
      }

      if ( newFile.exists() && !newFile.canWrite() )
      {
        String title = "File Save Error";
        String message = "The file\n" + newFile.getCanonicalPath()
            + "\nis read-only.  Please save to a different file.";
        JOptionPane.showMessageDialog( this, message, title, JOptionPane.ERROR_MESSAGE );
        return;
      }
      
      dir = newFile.getParentFile();
      properties.setProperty( "IRPath", dir );

      if ( ending.equals( irEndings[ 0 ] ) )
      {
        remoteConfig.saveAltPIDs();
        setInterfaceState( "EXPORTING..." );
        ( new SaveTask( newFile, Use.EXPORT ) ).execute();
      }
      else if ( ending.equals( binEndings[ 0 ] ) )
      {
        save( newFile, true );
      }
      else
      {
        setInterfaceState( "SAVING..." );
        ( new SaveTask( newFile, Use.SAVEAS ) ).execute();
      }
      uploadAction.setEnabled( !interfaces.isEmpty() );
    }
  }
  
  private class LoadTask extends SwingWorker< RemoteConfiguration, Void >
  {
    private File loadFile;
    
    public LoadTask( File file )
    {
      loadFile = file;
    }
    
    @Override
    protected RemoteConfiguration doInBackground() throws Exception
    {
      return new RemoteConfiguration( loadFile, RemoteMaster.this );
    }
    
    @Override
    public void done()
    {
      try
      {
        RemoteConfiguration rc = get();
        if ( rc != null )
        {
          remoteConfig = rc;
        }
      } 
      catch ( InterruptedException ignore ) {}
      catch ( java.util.concurrent.ExecutionException e ) 
      {
        String why = null;
        Throwable cause = e.getCause();
        if ( cause != null ) 
        {
          why = cause.getMessage();
        } 
        else 
        {
          why = e.getMessage();
        }
        System.err.println( "Error setting new RemoteConfiguration: " + why );
        setInterfaceState( null );
        JOptionPane.showMessageDialog( RemoteMaster.this, "Error loading file " + loadFile.getName(), "Task error", JOptionPane.ERROR_MESSAGE );
        e.printStackTrace();
      }
      recreateToolbar();
      update();
      changed = remoteConfig != null ? !Hex.equals( remoteConfig.getSavedData(), remoteConfig.getData() ) : false;
      setTitleFile( loadFile );
      setInterfaceState( null );
      file = loadFile;
    }
  }
  
  private class WriteTask extends SwingWorker< Void, Void >
  {
    private String text = null;
    protected boolean ok = true;
    
    public WriteTask()
    {
      this.text = null;
    }
    
    public WriteTask( String text )
    {
      this.text = text;
    }    
    
    @Override
    protected Void doInBackground() throws Exception
    {
      setInterfaceState( text );
      return null;
    }
    
    @Override
    public void done()
    {
      ok = true;
      try
      {
        get();
      } 
      catch ( InterruptedException ignore ) {}
      catch ( java.util.concurrent.ExecutionException e ) 
      {
        ok = false;
        String why = null;
        Throwable cause = e.getCause();
        if ( cause != null ) 
        {
          why = cause.getMessage();
        } 
        else 
        {
          why = e.getMessage();
        }
        System.err.println( "Error in write task: " + why );
        e.printStackTrace();
      }
    }
    
    public void resetInterfaceState()
    {
      if ( exitPrompt && interfaceText != null )
      {
        ( new WriteTask( interfaceText.substring( 0, interfaceText.length() - 3 ) + " AND EXIT" ) ).execute();
      }
    }
  }
  
  private class SaveTask extends WriteTask
  {
    private File file = null;
    private Use use = null;
    
    public SaveTask( File file, Use use )
    {
      this.file = file;
      this.use = use;
    }
    
    @Override
    protected Void doInBackground() throws Exception
    {
      resetInterfaceState();
      if ( use == Use.EXPORT )
      {
        remoteConfig.exportIR( file );
        if ( exitPrompt )
        {
          changed = false;
        }
        updateRecentFiles( file );
      }
      else
      {
        remoteConfig.save( file );
        changed = false;
        if ( use == Use.SAVEAS && binLoaded() == null )
        {
          RemoteMaster.this.file = file;
          setTitleFile( file );
          updateRecentFiles( file );
          saveAction.setEnabled( true );
        }
      }
      setInterfaceState( null );
      return null;
    }
    
    @Override
    public void done()
    {
      super.done();
      setInterfaceState( null );
      if ( !ok )
      {
        JOptionPane.showMessageDialog( RemoteMaster.this, "Error saving file " + file.getName(), "Task error", JOptionPane.ERROR_MESSAGE );
      }
      if ( exitPrompt )
      {
        exitPrompt = false;
        dispatchEvent( new WindowEvent( RemoteMaster.this, WindowEvent.WINDOW_CLOSING ) );
      }
    }
  }
  
  /**
   * Sets the titleFile attribute of the RemoteMaster object.
   * 
   * @param file
   *          the file
   */
  private void setTitleFile( File file )
  {
    if ( file == null || remoteConfig == null )
    {
      setTitle( "RMIR" );
    }
    else
    {
      setTitle( "RMIR: " + file.getName() + " - " + remoteConfig.getRemote().getName() );
    }
  }

  public ArrayList< IO > getInterfaces()
  {
    return interfaces;
  }
  
  /**
   * Gets the open interface.
   * 
   * @return the open interface
   */
  public IO getOpenInterface( File file, Use use )
  {
    String interfaceName = properties.getProperty( "Interface" );
    System.err.println( "Interface Name = " + ( interfaceName == null ? "NULL" : interfaceName ) );
    String portName = properties.getProperty( "Port" );
    System.err.println( "Port Name = " + ( portName == null ? "NULL" : portName ) );
    if ( interfaceName != null && ( use == Use.DOWNLOAD || use == Use.UPLOAD ) )
    {
      for ( IO temp : interfaces )
      {
        String tempName = temp.getInterfaceName();
        System.err.println( "Testing interface: " + ( tempName == null ? "NULL" : tempName ) );
        if ( tempName.equals( interfaceName ) )
        {
          System.err.println( "Interface matched.  Trying to open remote." );
          IO ioOut = testInterface( temp, portName, file, use );
          if ( ioOut != null )
          {
            return ioOut;
          }
          else
          {
            System.err.println( "Failed to open" );
            break;
          }
        }
      }
    }
    else
    {
      for ( IO temp : interfaces )
      {
        if ( temp instanceof JP1Parallel && System.getProperty( "os.name" ).equals( "Linux" ) )
        {
          String title = "Linux Parallel Port Interface";
          String message = 
                "Auto-detect has not been able to find a remote with any other interface, so is about to try the\n"
              + "Linux parallel port driver, which is unsafe.  It may cause RMIR to crash.  It uses \"bitbanging\"\n"
              + "which is incompatible with modern operating systems and so can only be run with root privileges.\n"
              + "It is not supported.  Use it at your own risk.\n\n"
              + "To use it without getting this message every time, select it explicitly with the Interface item\n"
              + "on the Remote menu rather than using Auto-detect.\n\n"
              + "Do you wish to continue?";
          if ( NegativeDefaultButtonJOptionPane.showConfirmDialog( this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE )
                 != JOptionPane.YES_OPTION )
          {
            continue;
          }
        }
        String tempName = temp.getInterfaceName();
        System.err.println( "Testing interface: " + ( tempName == null ? "NULL" : tempName ) );
        IO ioOut = testInterface( temp, null, file, use );
        if ( ioOut != null )
        {
          System.err.println( "Opened interface type " + Integer.toHexString( ioOut.getInterfaceType() ) );
          return ioOut;
        }
      }
    }
    return null;
  }
  
  private IO testInterface( IO ioIn, String portName, File file, Use use )
  {
    String ioName = ioIn.getInterfaceName();
    String osName = System.getProperty( "os.name" );
    if ( file != null && !ioName.equals( "JPS" ) )
    {
      return null;
    }
    
    if ( ioName.equals( "JPS" ) )
    {
      JPS jps = ( JPS )ioIn;
      if ( jps.isOpen() && ( use == Use.SAVING || use == Use.SAVEAS ) )
      {
        portName = jps.getFilePath();
        System.err.println( "Already open on Port " + portName );
        if ( use == Use.SAVEAS )
        {
          portName = file.getAbsolutePath();
          System.err.println( "Changing Port to " + portName );
          jps.setFilePath( portName );
        }
        return ioIn;
      }
    }
    portName = ioIn.openRemote( portName != null ? portName : file != null ? file.getAbsolutePath() : null );
    if ( portName == null ) portName = "";
    System.err.println( "Port Name = " + ( portName.isEmpty() ? "NULL" : portName ) );
    if ( !portName.isEmpty() )
    {
      System.err.println( "Opened on Port " + portName );
      return ioIn;
    }
    else
    {
      return null;
    }
  }
  
  public boolean usesNonModalDeviceEditor()
  {
    return Boolean.parseBoolean( properties.getProperty( "NonModalDeviceEditor", "false" ) );
  }
  
  public void setNonModalWarning( boolean warn, DeviceUpgradeEditor duEditor )
  {
    if ( duEditor != null )
    {
      // comes from DeviceUpgradeEditor
      this.duEditor = warn ? duEditor : null;
    }
    else
    {
      // comes from menu
      if ( this.duEditor != null && !warn )
      {
        // editor is open in non-modal state, being changed to modal
        if ( this.duEditor.getState() == Frame.ICONIFIED )
        {
          this.duEditor.setExtendedState( Frame.NORMAL );
        }
        this.duEditor.toFront();
        this.duEditor = null;
        setEnabled( false );
      }
      else
      {
        setEnabled( true );
      }
    }
    setInterfaceState( interfaceText );
  }

  private void setInterfaceState( String state )
  {
    interfaceText = state;
    if ( duEditor != null )
    {
      ( ( CardLayout )statusBar.getLayout() ).show( statusBar, "WARNING" );
    }
    else if ( state != null )
    {
      interfaceState.setString( state );
      ( ( CardLayout )statusBar.getLayout() ).show( statusBar, "INTERFACE" );
    }
    else
    {
      ( ( CardLayout )statusBar.getLayout() ).show( statusBar, "MEMORY" );
    }
  }
  
  public void clearAllInterfaces()
  {
    for ( IO io : interfaces )
    {
      io.clear();
    }
  }
  
  public static String getIOsignature( IO io, int baseAddress )
  {
    String sig = null;
    if ( io.getInterfaceType() > 0 ) // JP12Serial versions later than 0.18a
    {
      sig = io.getRemoteSignature();
    }
    else // Other interfaces and earlier JP12Serial versions
    {
      short[] sigData = new short[ 10 ];
      int count = io.readRemote( baseAddress, sigData );
      System.err.println( "Read first " + count + " bytes: " + Hex.toString( sigData ) );
      sig = Hex.getRemoteSignature( sigData );
    }
    if ( sig != null )
    {
      while ( sig.endsWith( "_" ) )
      {
        sig = sig.substring( 0, sig.length() - 1 );
      }
    }
    return sig;
  }
  
  /**
   * Description of the Method.
   * 
   * @param e
   *          the e
   */
  public void actionPerformed( ActionEvent e )
  {
    finishEditing();
    try
    {
      Object source = e.getSource();
      if ( source == installExtenderItem )
      {
        installExtender();
      }
      else if ( source == exitItem )
      {
        dispatchEvent( new WindowEvent( this, WindowEvent.WINDOW_CLOSING ) );
      }
      else if ( source == downloadRawItem )
      {
        if ( !promptToSave() )
        {
          return;
        }
        RawDataDialog dlg = new RawDataDialog( this );
        dlg.setVisible( true );
      }
      else if ( source == verifyUploadItem )
      {
        properties.setProperty( "verifyUpload", Boolean.toString( verifyUploadItem.isSelected() ) );
      }
      else if ( source == highlightItem )
      {
        properties.setProperty( "highlighting", Boolean.toString( highlightItem.isSelected() ) );
        if ( currentPanel instanceof RMTablePanel< ? > )
        {
          RMTablePanel< ? > panel = ( RMTablePanel< ? > )currentPanel;
          panel.getModel().fireTableStructureChanged();
        }
        else if ( currentPanel == generalPanel )
        {
          generalPanel.getDeviceButtonTableModel().fireTableStructureChanged();
          generalPanel.getSettingModel().fireTableStructureChanged();
        }
        currentPanel.set( remoteConfig );
        recreateToolbar();
      }
      else if ( source == enablePreserveSelection )
      {
        properties.setProperty( "enablePreserveSelection", Boolean.toString( enablePreserveSelection.isSelected() ) );
      }
      else if ( source == showSlingboxProtocols )
      {
        if ( !showSlingboxProtocols.isSelected() )
        {
          properties.remove( "ShowSlingboxProtocols" );
        }
        else
        {
          properties.setProperty( "ShowSlingboxProtocols", Boolean.toString( showSlingboxProtocols.isSelected() ) );
        }
      }
      else if ( source == defaultDelayItem )
      {
        properties.remove( "TooltipDelay" );
        tooltipDelay = tooltipDefaultDelay;
        ToolTipManager.sharedInstance().setInitialDelay( tooltipDefaultDelay );
      }
      else if ( source == specifiedDelayItem )
      {
        int val = 0;
        do
        {
          String ret = ( String )JOptionPane.showInputDialog( this, "Specify tooltip delay in milliseconds:", "Tooltip Delay", 
              JOptionPane.PLAIN_MESSAGE, null, null, ""+tooltipDelay );
          if ( ret == null )
          {
            val = -1;
            break;
          }
          try
          {
            val = Integer.parseInt( ret );
          }
          catch ( NumberFormatException nfe )
          {
            val = -1;
          }
          if ( val < 0 )
          {
            JOptionPane.showMessageDialog( this, "You must enter a valid non-negative integer", "Tooltip Delay", JOptionPane.ERROR_MESSAGE );
          }
        }
        while ( val < 0 );
        if ( val >= 0 )
        {
          tooltipDelay = val;
          ToolTipManager.sharedInstance().setInitialDelay( tooltipDelay );
          properties.setProperty( "TooltipDelay", ""+tooltipDelay );
        }
      }
      else if ( source == cleanUpperMemoryItem )
      {
        String title = "Clean Upper Memory";
        String message = "";
        if ( remoteConfig.hasSegments() )
        {
          message += "Please ";
        }
        else
        {
          message += "Do you want to retain all data in the first $100 (i.e. 256) bytes of memory?\n\n"
            + "If you answer No then the memory will be set as if your present setup was\n"
            + "installed on a reset state created in accordance with the RDF alone.  This\n"
            + "is the cleanest option but most RDFs at present do not create a true factory\n" + "reset state.\n\n"
            + "If you answer Yes then any data in the first $100 bytes not set by the RDF\n"
            + "will be retained.  This should include any data set by a factory reset that\n"
            + "is missing from the RDF, but it may also include other data that could be\n" + "usefully cleaned.\n\n"
            + "Please also ";
        }
        message += "be aware that blasting the memory will destroy most extenders, as\n"
            + "they place at least part of their code in the memory that will be cleared.\n"
            + "Press Cancel to exit without blasting the memory.";
        int result = NegativeDefaultButtonJOptionPane.showConfirmDialog( this, message, title, 
            remoteConfig.hasSegments() ? JOptionPane.OK_CANCEL_OPTION : JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE );
        if ( result == JOptionPane.CANCEL_OPTION )
        {
          return;
        }
        
        if ( result == JOptionPane.OK_OPTION )
        {
          // Remote has segments
          Remote remote = remoteConfig.getRemote();
          short[] data = remoteConfig.getData();
          Arrays.fill( data, remote.getUsageRange().getFreeStart(), data.length, ( short )0xFF );
          remoteConfig.updateCheckSums();
          update();
          return;
        }
        
        // Continue for remotes without segments.
        // Save the data that is stored only in the remote image.
        Remote remote = remoteConfig.getRemote();
        DeviceButton[] devBtns = remote.getDeviceButtons();
        int[] devBtnData = new int[ 2 * devBtns.length ];
        DeviceLabels devLabels = remote.getDeviceLabels();
        String[] devLabelText = new String[ 2 * devBtns.length ];
        SoftDevices softDevices = remote.getSoftDevices();
        int[] softSequence = new int[ devBtns.length + 1 ];
        Setting[] settings = remote.getSettings();
        int[] settingValues = new int[ settings.length ];
        short[] data = remoteConfig.getData();
        for ( int i = 0; i < devBtns.length; i++ )
        {
          devBtnData[ 2 * i ] = devBtns[ i ].getDeviceSlot( data );
          devBtnData[ 2 * i + 1 ] = devBtns[ i ].getDeviceGroup( data );
          if ( devLabels != null )
          {
            devLabelText[ 2 * i ] = devLabels.getText( data, i );
            devLabelText[ 2 * i + 1 ] = devLabels.getDefaultText( data, i );
          }
          if ( softDevices != null )
          {
            softSequence[ i ] = softDevices.getSequenceIndex( i, data );
          }
        }
        if ( softDevices != null )
        {
          softSequence[ devBtns.length ] = softDevices.getFilledSlotCount( data );
        }
        for ( int i = 0; i < settings.length; i++ )
        {
          settingValues[ i ] = settings[ i ].getValue();
        }
        remote.setFixedData( remote.getRawFixedData() );

        // Create clean reset state
        remoteConfig.initializeSetup( result == JOptionPane.YES_OPTION ? 0x100 : 0 );

        // Restore the data that is stored only in the remote image
        for ( int i = 0; i < devBtns.length; i++ )
        {
          devBtns[ i ].setDeviceSlot( devBtnData[ 2 * i ], data );
          devBtns[ i ].setDeviceGroup( ( short )devBtnData[ 2 * i + 1 ], data );
          if ( devLabels != null )
          {
            devLabels.setText( devLabelText[ 2 * i ], i, data );
            devLabels.setDefaultText( devLabelText[ 2 * i + 1 ], i, data );
          }
          if ( softDevices != null )
          {
            softDevices.setSequenceIndex( softSequence[ i ], i, data );
          }
        }
        if ( softDevices != null )
        {
          softDevices.setFilledSlotCount( softSequence[ devBtns.length ], data );
        }
        for ( int i = 0; i < settings.length; i++ )
        {
          settings[ i ].setValue( settingValues[ i ] );
        }

        // Update
        if ( result == JOptionPane.NO_OPTION )
        {
          // The state has now been constructed solely from the RDF, so set date indicator
          remoteConfig.setDateIndicator();
        }
        remoteConfig.updateImage();
        update();
      }
      else if ( source == clearAltPIDHistory )
      {
        ProtocolManager pm = ProtocolManager.getProtocolManager();
        int count = pm.countAltPIDRemoteEntries();
        String title = "Clear Alt PID History";
        String message = "The Alt PID History is used only to help a protocol to be recognised when other means\n"
            + "fail, such as in a download from a remote when it has been uploaded with an Alternate\n"
            + "PID instead of the standard one for that protocol.  There is seldom any need to clear\n"
            + "this history unless its size is becoming excessive.\n\n" + "It currently has " + count;
        message += count == 1 ? " entry." : " entries.";
        message += "\n\nAre you sure you want to clear this history?";
        int ans = JOptionPane.showConfirmDialog( this, message, title, JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE );
        if ( ans == JOptionPane.YES_OPTION )
        {
          pm.clearAltPIDRemoteEntries();
          message = "Cleared!";
          JOptionPane.showMessageDialog( this, message, title, JOptionPane.INFORMATION_MESSAGE );
        }
      }
      else if ( source == initializeTo00Item )
      {
        short[] data = getInitializationData( 0 );
        if ( data != null )
        {
          System.err.println( "Starting upload to initialize to FF" );
          setInterfaceState( "INITIALIZING TO 00..." );
          ( new UploadTask( data, false ) ).execute();
        }
      }
      else if ( source == initializeToFFItem )
      {
        short[] data = getInitializationData( 0xFF );
        if ( data != null )
        {
          System.err.println( "Starting upload to initialize to FF" );
          setInterfaceState( "INITIALIZING TO FF..." );
          ( new UploadTask( data, false ) ).execute();
        }
      }
      else if ( source == useSavedDataItem )
      {
        if ( currentPanel == rawDataPanel )
        {
          rawDataPanel.set( remoteConfig );
        }
        else if ( currentPanel == segmentPanel )
        {
          segmentPanel.set( remoteConfig );
        }
      }
      else if ( source == putSystemFileItem )
      {
        File file = getSystemFileChoice();
        if ( file == null )
        {
          return;
        }
        setInterfaceState( "UPLOADING " + file.getName().toUpperCase() + "..." );
        ( new UploadSystemFile( file ) ).execute();
      }
      else if ( source == setBaselineItem )
      {
        remoteConfig.setBaselineData();
        clearBaselineItem.setEnabled( true );
        if ( currentPanel == rawDataPanel )
        {
          rawDataPanel.set( remoteConfig );
        }
      }
      else if ( source == clearBaselineItem )
      {
        remoteConfig.clearBaselineData();
        clearBaselineItem.setEnabled( false );
        if ( currentPanel == rawDataPanel )
        {
          rawDataPanel.set( remoteConfig );
        }
      }
      else if ( source == parseIRDBItem )
      {
        extractIrdb();
        setSystemFilesItems( this, remoteConfig.getRemote() );
      }
      else if ( source == extractSSItem )
      {
        extractSS();
      }
      else if ( source == analyzeMAXQprotocols )
      {
        MAXQ610data maxq = new MAXQ610data();
        maxq.analyze();
      }
      else if ( source == rdfPathItem )
      {
        File path = getRDFPathChoice();
        if ( path == null )
        {
          return;
        }

        int opt = JOptionPane.NO_OPTION;
        if ( remoteConfig != null )
        {
          String message = "Do you want to apply this directory change immediately?\n\n"
              + "Yes = the present setup will be reinterpreted with an RDF from the new directory;\n"
              + "No = the change will take place when you next open a remote, even within this session;\n"
              + "Cancel = the change will be cancelled.\n\n"
              + "Note that if you answer Yes, the setup will still have been loaded with the old RDF.\n"
              + "You can achieve a similar result by answering No, using File/Save As to save the setup\n"
              + "with the old RDF and then opening the saved file, which will open with the new RDF.\n"
              + "The best choice between these two methods can depend on how different the RDFs are,\n"
              + "and what you are trying to achieve.";

          String title = "Change of RDF Directory";
          opt = JOptionPane.showConfirmDialog( this, message, title, JOptionPane.YES_NO_CANCEL_OPTION,
              JOptionPane.QUESTION_MESSAGE );
        }
        if ( opt == JOptionPane.CANCEL_OPTION )
        {
          return;
        }
        properties.setProperty( "RDFPath", path );
        RemoteManager mgr = RemoteManager.getRemoteManager();
        mgr.reset();
        mgr.loadRemotes( properties );
        if ( opt == JOptionPane.NO_OPTION )
        {
          return;
        }
        String rmTitle = getTitle();
        remoteConfig.setSavedData();
        Remote oldRemote = remoteConfig.getRemote();
        // Save the setting values
        Setting[] oldSettings = oldRemote.getSettings();
        int[] settingValues = new int[ oldSettings.length ];
        for ( int i = 0; i < oldSettings.length; i++ )
        {
          settingValues[ i ] = oldSettings[ i ].getValue();
        }
        Remote newRemote = RemoteManager.getRemoteManager().findRemoteByName( oldRemote.getName() );
        remoteConfig.setRemote( newRemote );
        for ( DeviceUpgrade du : remoteConfig.getDeviceUpgrades() )
        {
          du.setRemote( newRemote );
        }
        Setting[] newSettings = newRemote.getSettings();
        for ( int i = 0; i < Math.min( oldSettings.length, newSettings.length ); i++ )
        {
          newSettings[ i ].setValue( settingValues[ i ] );
        }
        SetupCode.setMax( newRemote.getSegmentTypes() == null ? newRemote.usesTwoBytePID() ? 4095 : 2047: 0x7FFF );
        remoteConfig.updateImage();
        RemoteConfiguration.resetDialogs();
        update();
        int index = rmTitle.lastIndexOf( oldRemote.getName() );
        setTitle( rmTitle.substring( 0, index ) + newRemote.getName() );
      }
      else if ( source == mapPathItem )
      {
        File path = getMapPathChoice();
        if ( path == null )
        {
          return;
        }
        int opt = JOptionPane.NO_OPTION;
        if ( remoteConfig != null )
        {
          String message = "Do you want to apply this directory change immediately?\n\n"
              + "Yes = a map and image from the new directory will be used in the present setup;\n"
              + "No = the change will take place when you next open a remote, even within this session;\n"
              + "Cancel = the change will be cancelled.";

          String title = "Change of Map and Image Directory";
          opt = JOptionPane.showConfirmDialog( this, message, title, JOptionPane.YES_NO_CANCEL_OPTION,
              JOptionPane.QUESTION_MESSAGE );
        }
        if ( opt == JOptionPane.CANCEL_OPTION )
        {
          return;
        }
        properties.setProperty( "ImagePath", path );
        if ( remoteConfig == null )
        {
          return;
        }
        Remote remote = remoteConfig.getRemote();
        remote.resetImageMaps( path );
      }
      else if ( source == updateItem )
      {
        UpdateChecker.checkUpdateAvailable( this );
      }
      else if ( source == aboutItem )
      {
        StringBuilder sb = new StringBuilder( 1000 );
        sb.append( "<html><b>RemoteMaster " );
        sb.append( version );     
        sb.append( " build " + getBuild() );
        sb.append( admin ? " (admin mode)" : "" );
        sb.append( "</b>" );
        sb.append( "<p>Written primarily by Greg&nbsp;Bush " );
        sb.append( "with substantial additions and help<br>from Graham&nbsp;Dixon</p>" );
        sb.append( "<p>Additional help was provided by:<blockquote>" );
        sb.append( "John&nbsp;S&nbsp;Fine, Nils&nbsp;Ekberg, Jon&nbsp;Armstrong, Robert&nbsp;Crowe, " );
        sb.append( "Mark&nbsp;Pauker, Mark&nbsp;Pierson, Mike&nbsp;England</blockquote></p>" );

        sb.append( "<p>RDFs loaded from <b>" );
        sb.append( properties.getProperty( "RDFPath" ) );
        sb.append( "</b></p>" );

        sb.append( "</p><p>Images and Maps loaded from <b>" );
        sb.append( properties.getProperty( "ImagePath" ) );
        sb.append( "</b></p>" );
        if ( LearnedSignal.hasDecodeIR() )
        {
          sb.append( "<p>DecodeIR version " );
          sb.append( LearnedSignal.getDecodeIRVersion() );
          sb.append( "</p>" );
        }
        else
        {
          sb.append( "<p><b>DecodeIR is not available!</b></p>" );
        }

        if ( !interfaces.isEmpty() )
        {
          sb.append( "<p>Interfaces:<ul>" );
          for ( IO io : interfaces )
          {
            sb.append( "<li>" );
            sb.append( io.getInterfaceName() );
            sb.append( " version " );
            sb.append( io.getInterfaceVersion() );
            sb.append( "</li>" );
          }
          sb.append( "</ul></p>" );
        }

        String[] propertyNames =
        {
            "java.version", "java.vendor", "os.name", "os.arch"
        };

        sb.append( "<p>System Properties:<ul>" );
        for ( String name : propertyNames )
        {
          sb.append( "<li>" );
          sb.append( name );
          sb.append( " = \"" );
          sb.append( System.getProperty( name ) );
          sb.append( "\"</li>" );
        }
        sb.append( "</ul>" );

        sb.append( "<p>Libraries loaded from " );
        sb.append( LibraryLoader.getLibraryFolder() );
        sb.append( "</p></html>" );

        JEditorPane pane = new JEditorPane( "text/html", sb.toString() );
        pane.addHyperlinkListener( this );
        pane.setEditable( false );
        pane.setBackground( getContentPane().getBackground() );
        new TextPopupMenu( pane );
        JScrollPane scroll = new JScrollPane( pane );
        Dimension d = pane.getPreferredSize();
        d.height = d.height * 5 / 4;
        d.width = d.width * 2 / 3;
        scroll.setPreferredSize( d );

        JOptionPane.showMessageDialog( this, scroll, "About RM/RMIR", JOptionPane.INFORMATION_MESSAGE, null );
      }
      else if ( source == readmeItem )
      {
        File readme = new File( workDir, "Readme.html" );
        desktop.browse( readme.toURI() );
      }
      else if ( source == tutorialItem )
      {
        URL url = new URL(
            "http://www.hifi-remote.com/wiki/index.php?title=JP1_-_Just_How_Easy_Is_It%3F_-_RM-IR_Version" );
        desktop.browse( url.toURI() );
      }
      else if ( source == learnedSignalItem )
      {
        File file = new File( workDir, "DecodeIR.html" );
        desktop.browse( file.toURI() );
      }
      else if ( source == homePageItem )
      {
        URL url = new URL( "http://controlremote.sourceforge.net/" );
        desktop.browse( url.toURI() );
      }
      else if ( source == wikiItem )
      {
        URL url = new URL( "http://www.hifi-remote.com/wiki/index.php?title=Main_Page" );
        desktop.browse( url.toURI() );
      }
      else if ( source == forumItem )
      {
        URL url = new URL( "http://www.hifi-remote.com/forums/" );
        desktop.browse( url.toURI() );
      }
      else
      {
        JMenuItem item = ( JMenuItem )source;
        File file = new File( item.getActionCommand() );
        recentFiles.remove( item );
        if ( file.canRead() )
        {
          openFile( file );
        }
      }
    }
    catch ( Exception ex )
    {
      ex.printStackTrace( System.err );
    }
  }

  /**
   * Description of the Method.
   */
  public void update()
  {
    if ( remoteConfig != null )
    {
      setTitle( "RMIR - " + remoteConfig.getRemote().getName() );
    }
    else
    {
      setTitle( "RMIR" );
      return;
    }

    resetTabbedPanes();
    
    generalPanel.set( remoteConfig );
    keyMovePanel.set( remoteConfig );
    macroPanel.set( remoteConfig );
    specialFunctionPanel.set( remoteConfig );
    timedMacroPanel.set( remoteConfig );
    favScanPanel.set( remoteConfig );
    favoritesPanel.set( remoteConfig );
    devicePanel.set( remoteConfig );
    protocolPanel.set( remoteConfig );
    activityPanel.set( remoteConfig );
    segmentPanel.set( remoteConfig );

    if ( LearnedSignal.hasDecodeIR() )
    {
      learnedPanel.set( remoteConfig );
    }

    Remote remote = remoteConfig.getRemote();
    codesAction.setEnabled( remote.getSetupCodes().size() > 0 );
    if ( codeSelectorDialog != null )
    {
      if ( codeSelectorDialog.isDisplayable() )
      {
        codeSelectorDialog.dispose();
      }
      codeSelectorDialog = null;
    }

    if ( rdfViewer != null )
    {
      if ( rdfViewer.isDisplayable() )
      {
        rdfViewer.dispose();
      }
      rdfViewer = null;
    }

    updateUsage();

    rawDataPanel.set( remoteConfig );
  }
  
  private void resetTabbedPanes()
  {
    Remote remote = remoteConfig.getRemote();
    int index = checkTabbedPane( "Key Moves", keyMovePanel, remote.hasKeyMoveSupport(), 1 );
    index = checkTabbedPane( "Macros", macroPanel, remote.hasMacroSupport(), index );
    index = checkTabbedPane( "Special Functions", specialFunctionPanel, !remote.getSpecialProtocols().isEmpty(), index );
    index = checkTabbedPane( "Timed Macros", timedMacroPanel, remote.hasTimedMacroSupport(), index );
    index = checkTabbedPane( "Fav/Scan", favScanPanel, remote.hasFavKey() && !remote.hasFavorites() && !remote.isSSD(), index );
    index = checkTabbedPane( "Favorites", favoritesPanel, remote.hasFavorites(), index );
    index = checkTabbedPane( "Devices", devicePanel, true, index );
    index = checkTabbedPane( "Protocols", protocolPanel, remote.hasFreeProtocols(), index );
    index = checkTabbedPane( "Activities", activityPanel, remote.hasActivitySupport(), index );
    if ( LearnedSignal.hasDecodeIR() )
      index = checkTabbedPane( "Learned Signals", learnedPanel, remote.hasLearnedSupport() && learnedPanel != null, index );
    else
      index = checkTabbedPane( "Learned Signals", learnedPanel, remote.hasLearnedSupport() && learnedPanel != null, index, "Learned Signals tab disabled due to DecodeIR not being found.", false );
    index = checkTabbedPane( "Segments", segmentPanel, Boolean.parseBoolean( properties.getProperty( "ShowSegments", "false" ) ) &&  remote.getSegmentTypes() != null && !remote.isSSD(), index );
  }
  
  protected void refreshTabbedPanes( boolean reset )
  {
    if ( reset )
    {
      resetTabbedPanes();
    }

    for ( int i = 0; i < tabbedPane.getTabCount(); i++ )
      ((RMPanel) tabbedPane.getComponentAt( i )).refresh();
  }
  private int checkTabbedPane( String name, Component c, boolean test, int index )
  {
    return checkTabbedPane( name, c, test, index, null, true );
  }
  private int checkTabbedPane( String name, Component c, boolean test, int index, String tooltip, boolean enabled )
  {
    if ( c == null )
    {
      return index;
    }
    int tabIndex = getTabIndex( c );
    if ( test )
    {
      if ( tabIndex < 0 )
      {
        tabbedPane.insertTab( name, null, c, tooltip, index );
        tabbedPane.setEnabledAt( index, enabled );
      }
      index++;
    }
    else if ( tabIndex > 0 )
    {
      tabbedPane.remove( index );
    }
    return index;
  }

  private boolean updateUsage( JProgressBar bar, AddressRange range )
  {
    if ( range != null )
    {
      int used = range.getFreeStart() - range.getStart() + range.getEnd() - range.getFreeEnd();
      int available = range.getSize();
      bar.setMinimum( 0 );
      bar.setMaximum( available );
      bar.setValue( used );
      bar.setString( Integer.toString( available - used ) + " free" );
      if ( range == remoteConfig.getRemote().getDeviceUpgradeAddress() )
      {
        // Device Upgrade area is filled from top down, so freeEnd != end in normal use
        bar.setForeground( AQUAMARINE );
      }
      else
      {
        bar.setForeground( ( range.getFreeEnd() == range.getEnd() ) ? AQUAMARINE : Color.YELLOW );
      }

      return available >= used;
    }
    else
    {
      bar.setMinimum( 0 );
      bar.setMaximum( 0 );
      bar.setValue( 0 );
      bar.setString( "N/A" );
      bar.setForeground( AQUAMARINE );

      return true;
    }
  }

  /**
   * Updates the progress bars and returns a boolean specifying whether the configuration is valid, i.e. whether all
   * sections fit in their available space.
   */
  private boolean updateUsage()
  {
    boolean valid = true;
    Remote remote = remoteConfig.getRemote();
    Dimension d = advProgressBar.getPreferredSize();
    Font font = advProgressBar.getFont();
    if ( remoteConfig.hasSegments() )
    {
      extraStatus.setVisible( false );
      advProgressLabel.setText( "Memory usage:" );
    }
    else if ( remote.getDeviceUpgradeAddress() == null )
    {
      upgradeProgressBar.setVisible( false );
      upgradeProgressBar.setPreferredSize( d );
      upgradeProgressBar.setFont( font );
      upgradeProgressBar.setVisible( true );
      devUpgradeProgressBar.setVisible( false );
      extraStatus.setVisible( true );
      advProgressLabel.setText( "Move/Macro:" );
    }
    else
    {
      d.height /= 2;
      Font font2 = font.deriveFont( ( float )font.getSize() * 0.75f );
      upgradeProgressBar.setVisible( false );
      upgradeProgressBar.setPreferredSize( d );
      upgradeProgressBar.setFont( font2 );
      upgradeProgressBar.setVisible( true );
      devUpgradeProgressBar.setVisible( false );
      devUpgradeProgressBar.setPreferredSize( d );
      devUpgradeProgressBar.setFont( font2 );
      devUpgradeProgressBar.setVisible( true );
      extraStatus.setVisible( true );
      advProgressLabel.setText( "Move/Macro:" );
    }

    String title = "Available Space Exceeded";
    String message = "";
    AddressRange range = remoteConfig.hasSegments() ? remote.getUsageRange() : remote.getAdvancedCodeAddress();
    if ( !updateUsage( advProgressBar, range ) )
    {
      valid = false;
      if ( range.getFreeEnd() == range.getEnd() )
      {
        message = "The defined advanced codes (keymoves, macros, special functions etc.) use more space than is available.  Please remove some.";
      }
      else
      {
        message = "There is insufficient space in the advanced codes section for both the defined\n"
            + "advanced codes (keymoves, macros, special functions etc.) and the device\n"
            + "upgrades that have overflowed from their own section.  Please remove some entries.";
      }
      showErrorMessage( message, title );
    }
    if ( !updateUsage( timedMacroPanel.timedMacroProgressBar, remote.getTimedMacroAddress() ) )
    {
      valid = false;
      message = "The defined timed macros use more space than is available.  Please remove some.";
      showErrorMessage( message, title );
    }
    if ( !updateUsage( upgradeProgressBar, remote.getUpgradeAddress() ) )
    {
      // Note that this section can only be full if there are no sections that can take overflow from it.
      // Otherwise, excessive device upgrades cause the overflow section, not this one, to be full.
      valid = false;
      message = "The defined device upgrades use more space than is available. Please remove some.";
      showErrorMessage( message, title );
    }
    if ( !updateUsage( devUpgradeProgressBar, remote.getDeviceUpgradeAddress() ) )
    {
      valid = false;
      message = "The defined button-dependent device upgrades use more space than is available. Please remove some.";
      showErrorMessage( message, title );
    }
    range = remote.getLearnedAddress();
    if ( !updateUsage( learnedProgressBar, range ) )
    {
      valid = false;
      if ( range.getFreeEnd() == range.getEnd() )
      {
        message = "The defined learned signals use more space than is available.  Please remove some.";
      }
      else
      {
        message = "There is insufficient space in the learned signals section for both the defined\n"
            + "learned signals and the device upgrades that have overflowed from their own\n"
            + "section.  Please remove some entries.";
      }
      showErrorMessage( message, title );
    }
    return valid;
  }

  private void showErrorMessage( String message, String title )
  {
    JOptionPane.showMessageDialog( this, message, title, JOptionPane.ERROR_MESSAGE );
  }

  /**
   * Description of the Method.
   * 
   * @param event
   *          the event
   */
  public void propertyChange( PropertyChangeEvent event )
  {
    // No need to check unassigned upgrades as keymoves are saved in .rmir file
    // remoteConfig.checkUnassignedUpgrades();
    if ( currentPanel == keyMovePanel )
    {
      ( ( KeyMoveTableModel )keyMovePanel.getModel() ).resetKeyMoves();
    }
    if ( currentPanel == activityPanel && event.getPropertyName().equals( "tabs" ) )
    {
      activityPanel.set( remoteConfig );
    }
    if ( currentPanel == generalPanel &&  generalPanel.getCreateUpgradesButton().isVisible() )
    {
      generalPanel.getCreateUpgradesButton().setEnabled( remoteConfig.getCreatableMissingCodes() != null );
    }
    remoteConfig.updateImage();
    updateUsage();
    hasInvalidCodes = generalPanel.setWarning();
    if ( !event.getPropertyName().equals( "highlight" ) )
    {
      changed = true;
    }
  }

  private boolean allowSave( Remote.SetupValidation setupValidation )
  {
    if ( !hasInvalidCodes )
    {
      return true;
    }
    Remote remote = remoteConfig.getRemote();
    String title = "Setup configuration";
    if ( setupValidation == Remote.SetupValidation.WARN )
    {
      String message = "The current setup contains invalid device codes";
      if ( remote.usesEZRC() )
      {
        message += "\nor devices without a matching device upgrade";
      }      
      message +=  ".\n\nAre you sure you wish to continue?";
      return NegativeDefaultButtonJOptionPane.showConfirmDialog( this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE ) == JOptionPane.YES_OPTION;
    }
    else if ( setupValidation == Remote.SetupValidation.ENFORCE )
    {
      String message = "The current setup contains invalid device codes\n"
          + "which would cause this remote to malfunction.\n" + "Please correct these codes and try again.";
      JOptionPane.showMessageDialog( this, message, title, JOptionPane.ERROR_MESSAGE );
    }
    return false;
  }

  private static File getWritableFile( File workDir, String fileName )
  {
    File dir = workDir;
    File file = new File( dir, fileName );
    boolean canWrite = true;
    if ( !file.exists() )
    {
      try
      {
        PrintWriter pw = new PrintWriter( new FileWriter( file ) );
        pw.println( "Write Test" );
        pw.flush();
        pw.close();
        file.delete();
      }
      catch ( IOException ioe )
      {
        canWrite = false;
      }
    }
    else
    {
      canWrite = file.canWrite();
    }

    if ( !canWrite )
    {
      String baseFolderName = null;

      if ( System.getProperty( "os.name" ).startsWith( "Windows" )
          && Float.parseFloat( System.getProperty( "os.version" ) ) >= 6.0f )
      {
        baseFolderName = System.getenv( "APPDATA" );
      }
      if ( baseFolderName == null || "".equals( baseFolderName ) )
      {
        baseFolderName = System.getProperty( "user.home" );
      }

      dir = new File( baseFolderName, "RemoteMaster" );
      if ( !dir.exists() )
      {
        dir = new File( baseFolderName, ".RemoteMaster" );
        if ( !dir.exists() )
        {
          dir.mkdirs();
        }
      }
      file = new File( dir, fileName );
    }

    return file;
  }
  
  /**
   * 
   * Taken from http://stackoverflow.com/questions/320542/how-to-get-the-path-of-a-running-jar-file
   * Said there to work with Windows, Linux and Mac.
   */
  public static File getJarContainingFolder( Class<?> aclass ) throws Exception 
  {
    CodeSource codeSource = aclass.getProtectionDomain().getCodeSource();

    File jarFile;

    if ( codeSource.getLocation() != null ) 
    {
      jarFile = new File(codeSource.getLocation().toURI());
    }
    else 
    {
      String path = aclass.getResource(aclass.getSimpleName() + ".class").getPath();
      String jarFilePath = path.substring(path.indexOf(":") + 1, path.indexOf("!"));
      jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
      jarFile = new File(jarFilePath);
    }
    return jarFile.getParentFile();
  }

  /**
   * Description of the Method.
   * 
   * @param args
   *          the args
   */
  private static void createAndShowGUI( ArrayList< String > args )
  {
    try
    {
      workDir = getJarContainingFolder( RemoteMaster.class );
      File propertiesFile = null;
      File errorsFile = null;
      File fileToOpen = null;
      boolean launchRM = true;
      for ( int i = 0; i < args.size(); ++i )
      {
        String parm = args.get( i );
        if ( parm.equalsIgnoreCase( "-ir" ) )
        {
          launchRM = true;
        }
        else if ( parm.equalsIgnoreCase( "-rm" ) )
        {
          launchRM = false;
        }
        else if ( parm.equalsIgnoreCase( "-admin" ) )
        {
          admin = true;
        }
        else if ( "-home".startsWith( parm ) )
        {
          String dirName = args.get( ++i );
          System.err.println( parm + " applies to \"" + dirName + '"' );
          workDir = new File( dirName );
        }
        else if ( "-properties".startsWith( parm ) )
        {
          String fileName = args.get( ++i );
          System.err.println( "Properties file name is \"" + fileName + '"' );
          propertiesFile = new File( fileName );
        }
        else if ( "-errors".startsWith( parm ) )
        {
          String fileName = args.get( ++i );
          System.err.println( "Errors file name is \"" + fileName + '"' );
          errorsFile = new File( fileName );
        }
        else
        {
          fileToOpen = new File( parm );
        }
      }
      
      if ( errorsFile == null )
      {
        errorsFile = getWritableFile( workDir, "rmaster.err" );
      }

      try
      {
        System.setErr( new PrintStream( new FileOutputStream( errorsFile ) ) );
      }
      catch ( Exception e )
      {
        e.printStackTrace( System.err );
      }

      System.err.println( "RemoteMaster " + RemoteMaster.version + " build " + getBuild() );
      System.err.println( "Legacy merge set = " + legacyMergeOK );
      String[] propertyNames =
      {
          "java.version", "java.vendor", "os.name", "os.arch", "java.home", "java.class.path"
      };

      System.err.println( "System Properties:" );
      for ( String name : propertyNames )
      {
        System.err.println( "   " + name + " = " + System.getProperty( name ) );
      }

      ClassPathAdder.addFile( workDir );

      FilenameFilter filter = new FilenameFilter()
      {
        public boolean accept( File dir, String name )
        {
          String temp = name.toLowerCase();
          return temp.endsWith( ".jar" ) && !temp.endsWith( "remotemaster.jar" ) && !temp.endsWith( "setup.jar" );
        }
      };

      File[] jarFiles = workDir.listFiles( filter );
      ClassPathAdder.addFiles( jarFiles );

      if ( propertiesFile == null )
      {
        propertiesFile = getWritableFile( workDir, "RemoteMaster.properties" );
      }
      PropertyFile properties = new PropertyFile( propertiesFile );

      String lookAndFeel = properties.getProperty( "LookAndFeel", UIManager.getSystemLookAndFeelClassName() );
      try
      {
        UIManager.setLookAndFeel( lookAndFeel );
        KeyMoveTableModel.normalSelectedBGColor = UIManager.getColor( "Table.selectionBackground" );
      }
      catch ( Exception ex )
      {
        ex.printStackTrace( System.err );
      }

      RemoteManager.getRemoteManager().loadRemotes( properties );

      ProtocolManager.getProtocolManager().load( new File( workDir, "protocols.ini" ), properties );

      DigitMaps.load( new File( workDir, "digitmaps.bin" ) );

      if ( launchRM )
      {
        RemoteMaster rm = new RemoteMaster( workDir, properties );
        if ( fileToOpen != null )
        {
          rm.openFile( fileToOpen );
        }
        frame = rm;
      }
      else
      {
        KeyMapMaster km = new KeyMapMaster( properties );
        km.loadUpgrade( fileToOpen );
        frame = km;
      }
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

  public List< AssemblerItem > getClipBoardItems()
  {
    return clipBoardItems;
  }

  /**
   * The main program for the RemoteMaster class.
   * 
   * @param args
   *          the args
   */
  public static void main( String[] args )
  {
    java.lang.reflect.Method m;
    try
    {
      m = ClassLoader.class.getDeclaredMethod("findLoadedClass", new Class[] { String.class });
      m.setAccessible(true);
      ClassLoader cl = ClassLoader.getSystemClassLoader();
      Object test1 = m.invoke(cl, "java.util.Arrays");
      legacyMergeOK = test1 == null;
      System.setProperty( "java.util.Arrays.useLegacyMergeSort", "true" );
    }
    catch ( Exception e ) {}
   
    JDialog.setDefaultLookAndFeelDecorated( true );
    JFrame.setDefaultLookAndFeelDecorated( true );
    Toolkit.getDefaultToolkit().setDynamicLayout( true );

    for ( String arg : args )
    {
      if ( "-version".startsWith( arg ) )
      {
        System.out.println( getFullVersion() );
        return;
      }
      else
      {
        parms.add( arg );
      }
    }
    javax.swing.SwingUtilities.invokeLater( new Runnable()
    {
      public void run()
      {
        createAndShowGUI( parms );
      }
    } );
  }

  /** The parms. */
  private static ArrayList< String > parms = new ArrayList< String >();
  
  private static File workDir = null;

  public static File getWorkDir()
  {
    return workDir;
  }

  /** The Constant rmirEndings. */
  private final static String[] rmirEndings =
  {
    ".rmir"
  };

  /** The Constant rmduEndings. */
  private final static String[] rmduEndings =
  {
    ".rmdu"
  };

  /** The Constant irEndings. */
  private final static String[] irEndings =
  {
    ".ir"
  };
  
  private final static String[] binEndings =
    {
      ".bin"
    };

  /** The Constant txtEndings. */
  public final static String[] txtEndings =
  {
    ".txt"
  };

  private final static String[] slingEndings =
  {
    ".xml"
  };

  private final static String[] allEndings =
  {
      ".rmir", ".ir", ".rmdu", ".txt", ".xml", ".bin"
  };

  private final static String[] allMergeEndings =
  {
      ".hex", ".ir", ".txt"
  };

  private final static String[] extenderEndings =
  {
    ".hex"
  };

  private final static String[] otherMergeEndings =
  {
      ".ir", ".txt"
  };

  @Override
  public void stateChanged( ChangeEvent event )
  {
    finishEditing();
    RMPanel newPanel = ( RMPanel )tabbedPane.getSelectedComponent();
    if ( newPanel != currentPanel )
    {
      newPanel.set( remoteConfig );
      currentPanel = newPanel;
      highlightAction.setEnabled( false );
    }
    if ( codeSelectorDialog != null )
    {
      codeSelectorDialog.enableAssign( currentPanel == generalPanel );
    }
  }

  private int getTabIndex( Component c )
  {
    for ( int i = 0; i < tabbedPane.getTabCount(); i++ )
    {
      if ( tabbedPane.getComponentAt( i ).equals( c ) )
      {
        return i;
      }
    }
    return -1;
  }

  public RemoteConfiguration getRemoteConfiguration()
  {
    return remoteConfig;
  }

  public GeneralPanel getGeneralPanel()
  {
    return generalPanel;
  }

  public KeyMovePanel getKeyMovePanel()
  {
    return keyMovePanel;
  }

  public DeviceUpgradePanel getDeviceUpgradePanel()
  {
    return devicePanel;
  }

  private boolean changed = false;

  public boolean isChanged()
  {
    return changed;
  }

  public void setChanged( boolean changed )
  {
    this.changed = changed;
  }

  public boolean promptToSave() throws IOException
  {
    return promptToSave( false );
  }
  
  public boolean promptToSave( boolean doExit ) throws IOException
  {
    if ( !changed )
    {
      return true;
    }
    int rc = JOptionPane.showConfirmDialog( this, "The data has changed.  Do you want to save\n"
        + "the current configuration before proceeding?", "Save upgrade?", JOptionPane.YES_NO_CANCEL_OPTION );
    if ( rc == JOptionPane.CANCEL_OPTION || rc == JOptionPane.CLOSED_OPTION )
    {
      return false;
    }
    if ( rc == JOptionPane.NO_OPTION )
    {
      return true;
    }
    exitPrompt = doExit;
    if ( saveAction.isEnabled() )
    {
      save( file, false );
    }
    else
    {
      saveAs();
    }
    return true;
  }
  
  private void finishEditing()
  {
    if ( currentPanel instanceof RMTablePanel< ? > )
    {
      RMTablePanel< ? > panel = ( RMTablePanel< ? > )currentPanel;
      panel.finishEditing();
    }
    else if ( currentPanel == generalPanel )
    {
      generalPanel.finishEditing();
    }
    else if ( currentPanel == activityPanel )
    {
      activityPanel.finishEditing();
    }
  }
  
  private void extractSS()
  {
    JPS io = binLoaded();
    if ( io == null )
    {
      return;
    }
    Scanner s = io.getScanner();
    if ( s == null )
    {
      String title = "Parsing error";
      String message = "Unable to interpret settings data.  Extraction failed.";
      JOptionPane.showMessageDialog( this, message, title, JOptionPane.INFORMATION_MESSAGE );
      return;
    }
    LinkedHashMap< Integer, List< Integer > > setups = new LinkedHashMap< Integer, List<Integer> >();
    LinkedHashMap< Integer, Integer > pidLenBytes = new LinkedHashMap< Integer, Integer >();
    LinkedHashMap< Integer, Integer > mapNumBytes = new LinkedHashMap< Integer, Integer >();
    List< Integer > prots = new ArrayList< Integer >();
    List< Integer > maps = new ArrayList< Integer >();
    short[] bufSetup = new short[ 4 * s.getSetupCodeCount() ];
    short[] bufType = new short[ 2 * s.getSetupTypeCount() + 2 ];
    short[] bufExec = new short[ 4 * s.getExecutorCount() ];
    short[] bufNum = new short[ 10 * s.getNumberTableSize() ];
    io.readRemote( s.getSetupCodeIndexAddress() + 2, bufSetup );
    io.readRemote( s.getSetupTypeIndexAddress(), bufType );
    io.readRemote( s.getExecutorIndexAddress() + 2, bufExec );
    io.readRemote( s.getNumberTableAddress(), bufNum );
    for ( int i = 0; i < s.getExecutorCount(); i++ )
    {
      int pid = bufExec[ 2 * i ] | bufExec[ 2 * i + 1 ] << 8;
      prots.add( pid );
      int n = 2 * ( s.getExecutorCount() + i );
      int protAddress = ( bufExec[ n ] | bufExec[ n + 1 ] << 8 ) + s.getIndexTablesOffset();
      short[] buf2 = new short[ 2 ];
      if ( io.readRemote( protAddress + 2, buf2 ) != 2 )
      {
        continue;
      }
      pidLenBytes.put( pid, ( int )buf2[ 0 ] );
    }

    int type = -1;
    int typeLimit = ( bufType[ 2 * type + 2 ] | bufType[ 2 * type + 3 ] << 8 ) * 2;
    int mask = s.setupCodeIncludesType() ? 0x0FFF : 0xFFFF;
    for ( int i = 0; i < s.getSetupCodeCount(); i++ )
    {
      int codeAddress = s.getSetupCodeIndexAddress() + 2 + 2 * i;
      if ( codeAddress == typeLimit )
      {
        type++;
        typeLimit = ( bufType[ 2 * type + 2 ] | bufType[ 2 * type + 3 ] << 8 ) * 2;
      }
      int setupCode = bufSetup[ 2 * i ] | bufSetup[ 2 * i + 1 ] << 8;
      List< Integer > codeList = setups.get( type );
      if ( codeList == null )
      {
        codeList = new ArrayList< Integer >();
        setups.put( type, codeList );
      }
      codeList.add( setupCode & mask );
      int n = 2 * ( s.getSetupCodeCount() + i );
      int setupAddress = ( bufSetup[ n ] | bufSetup[ n + 1 ] << 8 ) + s.getIndexTablesOffset();
      short[] buf = new short[ 4 ];
      io.readRemote( setupAddress, buf );
      int pid = buf[ 0 ] << 8 | buf[ 1 ];
      int map = buf[ 2 ];
      if ( map > 0 && pidLenBytes.get( pid ) != null )
      {
        mapNumBytes.put( map - 1, pidLenBytes.get( pid ) & 0x0F );
      }
    }
    int n = 0;
    while ( n < bufNum.length / 10 )
    {
      int numBytes = mapNumBytes.get( n ) != null ? mapNumBytes.get( n ) : 1;
      short[] digitKeyCodes = Arrays.copyOfRange( bufNum, 10 * n, 10 * ( n + numBytes ) );
      int m = DigitMaps.findDigitMapNumber( digitKeyCodes );
      for ( int j = 0; j < numBytes; j++ )
      {
        maps.add( m >= 0 ? m + j : -1 );
      }
      n += numBytes;
    }
    System.err.println();
    System.err.println( "DATA EXTRACT FOR RDF FOR SIGNATURE " + io.getRemoteSignature() + ":"  );
    System.err.println( String.format( "EepromSize=$%04X", io.getRemoteEepromSize() ) );
    System.err.println( String.format( "BaseAddress=$%04X", io.getRemoteEepromAddress() ) );
    printExtract( setups, pidLenBytes, prots, maps );
    System.err.println( "Raw number table data:" );
    n = 0;
    while ( n < bufNum.length / 10 )
    {
      int numBytes = mapNumBytes.get( n ) != null ? mapNumBytes.get( n ) : 1;
      for ( int i = 0; i < 10 * numBytes; i++ )
      {
        System.err.print( String.format( "%02x ", bufNum[ 10 * n + i ] ) );
      }
      System.err.println();
      n += numBytes;
    }
    System.err.println();
    String title = "Extract for RDF";
    String message = 
        "Extract data, including [Protocols] and [SetupCodes] sections\n"
        + "for the RDF, have been output to rmaster.err"; 
    JOptionPane.showMessageDialog( this, message, title, JOptionPane.INFORMATION_MESSAGE );
  }
  
  
  private void extractIrdb()
  {
    int pos = 0;
    File file = new File( new File( workDir, "XSight" ), "irdb.bin" );
    byte[] data = RemoteMaster.readBinary( file );
    LinkedHashMap< Integer, List< Integer > > setups = new LinkedHashMap< Integer, List<Integer> >();
    LinkedHashMap< Integer, Integer > pidLenBytes = new LinkedHashMap< Integer, Integer >();
    List< Integer > prots = new ArrayList< Integer >();
    List< Integer > distinctTags = new ArrayList< Integer >();
    String name = file.getName();
    List< String > tagNames = new ArrayList< String >();
    System.err.println( name + " tags:" );
    int itemsLength = ( data[ pos + 14 ] & 0xFF ) + 0x100 * ( data[ pos + 15 ] & 0xFF );
    pos += 16;
    int itemCount = data[ pos++ ] & 0xFF;
    int itemsEnd = pos + itemsLength;
    char ch;
    for ( int i = 0; i < itemCount; i++ )
    {
      StringBuilder sb = new StringBuilder();
      while ( ( ch = ( char )( data[ pos++ ] & 0xFF ) ) != 0 )
      {
        sb.append( ch );
      }
      String tag = Integer.toHexString( i );
      if ( tag.length() == 1 )
      {
        tag = "0" + tag;
      }
      String tagName = sb.toString();
      tagNames.add( tagName );
      System.err.println( "  " + tag + "  " + tagName );
    }
    if ( pos != itemsEnd )
    {
      System.err.println( "Parsing error in " + name );
      return;
    }

    List< Integer > tags = new ArrayList< Integer >();
    int devLen = 0;
    int cmdLen = 0;
    int pid = -1;
    while ( true )
    {
      int tag = data[ pos++ ] & 0xFF;
      if ( ( tag & 0x80 ) == 0 )
      {
        tags.add( 0, tag );
        if ( !distinctTags.contains( tag ) )
        {
          distinctTags.add( tag );
        }
        if ( tag == 0x0B )
        {
          int type = data[ pos + 1 ];
          char[] chs = new char[ 4 ];
          for ( int i = 2; i < 6; i++ )
          {
            chs[ i - 2 ] = ( char )data[ pos + i ];
          }
          int val = Integer.parseInt( new String( chs ) );
          List< Integer > list = setups.get( type );
          if ( list == null )
          {
            list = new ArrayList< Integer >();
            setups.put( type, list );
          }
          if ( !list.contains( val ) )
          {
            list.add( val );
          }
        }
        else if ( tag == 0x0D )
        {
          devLen = data[ pos ];
        }
        else if ( tag == 0x11 )
        {
          pid = ( data[ pos + 1 ] & 0xFF ) + 0x100 * ( data[ pos + 2 ] & 0xFF );
          if ( !prots.contains( pid ) )
          {
            prots.add( pid );
          }
        }
        else if ( tag == 0x10 )
        {
          if ( cmdLen == 0 )
          {
            cmdLen = data[ pos ] - 1;
          }
          else if ( cmdLen != data[ pos ] - 1 )
          {
            System.err.println( "Inconsistent cmdLen in pid = " + Integer.toHexString( pid ) );
          }
        }
        int len = data[ pos++ ] & 0xFF;
        pos += len;
      }
      else
      {
        int last = tags.remove( 0 );
        if ( tag != ( last | 0x80  ) )
        {
          System.err.println( "XCF file nesting error at " + Integer.toHexString( pos - 1 ) );
          return;
        }
        else if ( tag == 0x8B )
        {
          int val = 0xFFFF;
          if ( devLen < 16 && cmdLen < 16 )
          {
            val = cmdLen | ( devLen << 4 );
          }
          Integer oldVal = pidLenBytes.get( pid );
          if ( oldVal == null )
          {
            pidLenBytes.put( pid, val );
          }
          else if ( oldVal != val )
          {
            System.err.println( "Inconsistent occurrences of pid " + Integer.toHexString( pid ) );
          }
          devLen = 0;
          cmdLen = 0;
          pid = -1;
        }

        if ( tags.isEmpty() )
        {
          System.err.println( "irdb parsing terminating at position " + Integer.toHexString( pos ) );
          break;
        }
      }  
    }

    Collections.sort( distinctTags );
    System.err.println();
    System.err.print( "Distinct tags: " );
    for ( int tag : distinctTags )
    {
      System.err.print( String.format( "%02X ", tag ) );
    }
    System.err.println();
    System.err.println();
    printExtract( setups, pidLenBytes, prots, null );
    String title = "Extract irdb.bin";
    String message = 
        "Extract data, including [Protocols] and [SetupCodes] sections\n"
        + "for the RDF, have been output to rmaster.err"; 
    JOptionPane.showMessageDialog( this, message, title, JOptionPane.INFORMATION_MESSAGE );
  }
  
  private void printExtract( LinkedHashMap< Integer, List< Integer > > setups,
      LinkedHashMap< Integer, Integer > pidLenBytes, List< Integer > prots,
      List< Integer > maps )
  {
    System.err.println();
    System.err.println( "[SetupCodes]");
    List< Integer > types = new ArrayList< Integer >( setups.keySet() );
    Collections.sort( types );
    Collections.sort( prots );
    for ( int type : types )
    {
      List< Integer > list = setups.get( type );
      Collections.sort( list );
      if ( type < 0x10)
      {
        System.err.print( type );
      }
      else
      {
        System.err.print( ( char )type );
      }
      System.err.print( " = " );
      int i = -1;
      for ( int val : list )
      {
        if ( ++i > 0 )
        {
          System.err.print( ", " );

          if ( ( i % 10 ) == 0 )
          {
            System.err.println();
            System.err.print( "    " );
          }
        }
        System.err.print( new SetupCode( val ) );
      }
      System.err.println();
    }
    if ( maps != null )
    {
      System.err.println();
      System.err.println( "[DigitMaps]");
      int i = -1;
      for ( int m : maps )
      {
        if ( ++i > 0 )
        {
          if ( ( i % 16 ) == 0 )
          {
            System.err.println();
          }
        }
        if ( m >= 0 )
        {
          System.err.print( String.format( "%03d ", m ) );
        }
        else
        {
          System.err.print( "??? " );
        }
      }
      System.err.println();
    }
    System.err.println();
    System.err.println( "[Protocols]" );
    int i = -1;
    for ( int p : prots )
    {
      if ( ++i > 0 )
      {
        System.err.print( ", " );

        if ( ( i % 10 ) == 0 )
        {
          System.err.println();
        }
      }
      System.err.print( String.format( "%04X", p ) );
    }
    System.err.println();
    System.err.println();
    i = -1;
    System.err.println( "Dev/Cmd lengths by protocol");
    for ( int p : prots )
    {
      if ( ++i > 0 )
      {
        System.err.print( "; " );

        if ( ( i % 8 ) == 0 )
        {
          System.err.println();
        }
      }
      System.err.print( String.format( "%04X %02X", p, pidLenBytes.get( p ) ) );
    }
    System.err.println();
    System.err.println();
  }
  
  public void resetSegmentPanel()
  {
    if ( segmentPanel != null )
    {
      segmentPanel.resetLastSorted();
    }
  }
}
