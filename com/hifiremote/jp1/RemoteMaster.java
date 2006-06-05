package com.hifiremote.jp1;

import com.hifiremote.jp1.io.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class RemoteMaster
 extends JFrame
 implements ActionListener, PropertyChangeListener
{
  private static RemoteMaster me = null;
  public static final String version = "v0.04";
  private File dir = null;
  public File file = null;
  private RemoteConfiguration remoteConfig = null;
  private RMFileChooser chooser = null;
  private Properties preferences = null;

  // File menu items
  private JMenuItem newItem = null;
  private JMenuItem openItem = null;
  private JMenuItem saveItem = null;
  private JMenuItem saveAsItem = null;
  private JMenuItem revertItem = null;
  private JMenu recentFiles = null;
  private JMenuItem exitItem = null;

  // Remote menu items
  private JMenuItem downloadItem = null;
  private JMenuItem uploadItem = null;
  private JMenuItem uploadWavItem = null;

  // Interface menu items
  private JMenuItem aboutItem = null;

  private JTabbedPane tabbedPane = null;
  private GeneralPanel generalPanel = null;
  private KeyMovePanel keyMovePanel = null;
  private MacroPanel macroPanel = null;
  private DeviceUpgradePanel devicePanel = null;
  private ProtocolUpgradePanel protocolPanel = null;
  private LearnedSignalPanel learnedPanel = null;
  private RawDataPanel rawDataPanel = null;

  private JProgressBar advProgressBar = null;
  private JProgressBar upgradeProgressBar = null;
  private JProgressBar learnedProgressBar = null;

  public RemoteMaster()
    throws Exception
  {
    super( "Java IR" );
    me = this;
    String workDir = System.getProperty( "user.dir" );
    String homeDir = System.getProperty( "user.home" );
    preferences = new Properties();
    File propertiesFile = new File( homeDir, "ir.properties" );
    if ( propertiesFile.exists() && propertiesFile.canRead())
    {
      FileInputStream in = new FileInputStream( propertiesFile );
      preferences.load( in );
      in.close();
    }

    File rdfDir = new File( preferences.getProperty( "RDFPath", workDir + File.separatorChar + "rdf" ));
    rdfDir = RemoteManager.getRemoteManager().loadRemotes( rdfDir );
    ProtocolManager.getProtocolManager().load( new File( workDir, "protocols.ini" ));

    preferences.put( "RDFPath", rdfDir.getCanonicalPath());

    dir = new File( preferences.getProperty( "fileFolder", workDir ));
    createMenus();

    setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
    setDefaultLookAndFeelDecorated( true );

    addWindowListener( new WindowAdapter()
    {
      public void windowClosing( WindowEvent event )
      {
        try
        {
          for ( int i = 0; i < recentFiles.getItemCount(); ++i )
          {
            JMenuItem item = recentFiles.getItem( i );
            preferences.setProperty( "RecentFiles." + i, item.getActionCommand());
          }
          int state = getExtendedState();
          if ( state != Frame.NORMAL )
            setExtendedState( Frame.NORMAL );
          Rectangle bounds = getBounds();
          preferences.setProperty( "Bounds", "" + bounds.x + ',' + bounds.y + ',' + bounds.width + ',' + bounds.height );

          FileOutputStream out = new FileOutputStream( new File( System.getProperty( "user.home" ), "ir.properties" ));
          preferences.store( out, null );
          out.flush();
          out.close();
        }
        catch ( Exception exc )
        {
          exc.printStackTrace( System.err );
        }
        System.exit( 0 );
      }
    });

    Container mainPanel = getContentPane();
    tabbedPane = new JTabbedPane();
    mainPanel.add( tabbedPane, BorderLayout.CENTER );

    generalPanel = new GeneralPanel();
    tabbedPane.addTab( "General", generalPanel );
    generalPanel.addPropertyChangeListener( this );

    keyMovePanel = new KeyMovePanel();
    tabbedPane.addTab( "Key Moves", keyMovePanel );
    keyMovePanel.addPropertyChangeListener( this );

    macroPanel = new MacroPanel();
    tabbedPane.addTab( "Macros", macroPanel );
    macroPanel.addPropertyChangeListener( this );
    
    // tabbedPane.addTab( "Special Functions", new JPanel());
    // tabbedPane.addTab( "Scan/Fav", new JPanel());

    devicePanel = new DeviceUpgradePanel();
    tabbedPane.addTab( "Devices", devicePanel );
    devicePanel.addPropertyChangeListener( this );

    protocolPanel = new ProtocolUpgradePanel();
    tabbedPane.addTab( "Protocols", protocolPanel );
    protocolPanel.addPropertyChangeListener( this );

    learnedPanel = new LearnedSignalPanel();
    tabbedPane.addTab( "Learned Signals", learnedPanel );
    learnedPanel.addPropertyChangeListener( this );

    rawDataPanel = new RawDataPanel();
    tabbedPane.addTab( "Raw Data", rawDataPanel );
    rawDataPanel.addPropertyChangeListener( this );

    JPanel statusBar = new JPanel();
    mainPanel.add( statusBar, BorderLayout.SOUTH );    

    statusBar.add( new JLabel( "Move/Macro:" ));

    advProgressBar = new JProgressBar();
    advProgressBar.setStringPainted( true );
    advProgressBar.setString( "N/A" );
    statusBar.add( advProgressBar );

    statusBar.add( Box.createHorizontalStrut( 5 ));
    JSeparator sep = new JSeparator( SwingConstants.VERTICAL );
    Dimension d = sep.getPreferredSize();
    d.height = advProgressBar.getPreferredSize().height;
    sep.setPreferredSize( d );
    statusBar.add( sep );

    statusBar.add( new JLabel( "Upgrade:" ));

    upgradeProgressBar = new JProgressBar();
    upgradeProgressBar.setStringPainted( true );
    upgradeProgressBar.setString( "N/A" );
    statusBar.add( upgradeProgressBar );

    statusBar.add( Box.createHorizontalStrut( 5 ));
    sep = new JSeparator( SwingConstants.VERTICAL );
    sep.setPreferredSize( d );
    statusBar.add( sep );

    statusBar.add( new JLabel( "Learned:" ));
    
    learnedProgressBar = new JProgressBar();
    learnedProgressBar.setStringPainted( true );
    learnedProgressBar.setString( "N/A" );
    statusBar.add( learnedProgressBar );

    String temp = preferences.getProperty( "Bounds" );
    if ( temp != null )
    {
      Rectangle bounds = new Rectangle();
      StringTokenizer st = new StringTokenizer( temp, "," );
      bounds.x = Integer.parseInt( st.nextToken());
      bounds.y = Integer.parseInt( st.nextToken());
      bounds.width = Integer.parseInt( st.nextToken());
      bounds.height = Integer.parseInt( st.nextToken());
      setBounds( bounds );
    }
    else
      pack();
    show();
  }

  public static RemoteMaster getRemoteMaster(){ return me;}

  public Properties getPreferences(){ return preferences; }

  private void createMenus()
  {
    JMenuBar menuBar = new JMenuBar();
    setJMenuBar( menuBar );

    JMenu menu = new JMenu( "File" );
    menu.setMnemonic( KeyEvent.VK_F );
    menuBar.add( menu );

//    newItem = new JMenuItem( "New", KeyEvent.VK_N );
//    newItem.addActionListener( this );
//    menu.add( newItem );

    openItem = new JMenuItem( "Open...", KeyEvent.VK_O );
    openItem.addActionListener( this );
    menu.add( openItem );

    saveItem = new JMenuItem( "Save", KeyEvent.VK_S );
    saveItem.setEnabled( false );
    saveItem.addActionListener( this );
    menu.add( saveItem );

    saveAsItem = new JMenuItem( "Save as...",  KeyEvent.VK_A );
    saveAsItem.setDisplayedMnemonicIndex( 5 );
    saveAsItem.setEnabled( false );
    saveAsItem.addActionListener( this );
    menu.add( saveAsItem );

//    revertItem = new JMenuItem( "Revert to saved" );
//    revertItem.setMnemonic( KeyEvent.VK_R );
//    revertItem.addActionListener( this );
//    menu.add( revertItem );

    menu.addSeparator();
    recentFiles = new JMenu( "Recent" );
    menu.add( recentFiles );
    recentFiles.setEnabled( false );
    for ( int i = 0; i < 10; i++ )
    {
      String propName = "RecentFiles." + i;
      String temp = preferences.getProperty( propName );
      if ( temp == null )
        break;
      preferences.remove( propName );
      File f = new File( temp );
      if ( f.canRead())
      {
        JMenuItem item = new JMenuItem( temp );
        item.setActionCommand( temp );
        item.addActionListener( this );
        recentFiles.add( item );
      }
    }
    if ( recentFiles.getItemCount() > 0 )
      recentFiles.setEnabled( true );
    menu.addSeparator();

    exitItem = new JMenuItem( "Exit", KeyEvent.VK_X );
    exitItem.addActionListener( this );
    menu.add( exitItem );

    menu = new JMenu( "Remote" );
    menu.setMnemonic( KeyEvent.VK_R );
    menuBar.add( menu );

    downloadItem = new JMenuItem( "Download from Remote", KeyEvent.VK_D );
    downloadItem.addActionListener( this );
    menu.add( downloadItem );

    uploadItem = new JMenuItem( "Upload to Remote", KeyEvent.VK_U );
    uploadItem.setEnabled( false );
    uploadItem.addActionListener( this );
    menu.add( uploadItem );

    uploadWavItem = new JMenuItem( "Upload using WAV", KeyEvent.VK_W );
    uploadWavItem.setEnabled( false );
    uploadWavItem.addActionListener( this );
    menu.add( uploadWavItem );

    menu = new JMenu( "Help" );
    menu.setMnemonic( KeyEvent.VK_H );
    menuBar.add( menu );

    aboutItem = new JMenuItem( "About...", KeyEvent.VK_A );
    aboutItem.addActionListener( this );
    menu.add( aboutItem );
  }

  public RMFileChooser getFileChooser()
  {
    if ( chooser != null )
      return chooser;

    RMFileChooser chooser = new RMFileChooser( dir );
    EndingFileFilter irFilter = new EndingFileFilter( "RM IR files (*.rmir)", rmEndings );
    chooser.addChoosableFileFilter( irFilter );
    chooser.addChoosableFileFilter( new EndingFileFilter( "IR files (*.ir)", irEndings ));
    chooser.addChoosableFileFilter( new EndingFileFilter( "Text files (*.txt)", txtEndings ));
    chooser.setFileFilter( irFilter );

    return chooser;
  }

  public File openFile()
    throws IOException
  {
    return openFile( null );
  }

  public File openFile( File file )
    throws IOException
  {
    while ( file == null )
    {
      RMFileChooser chooser = getFileChooser();
      int returnVal = chooser.showOpenDialog( this );
      if ( returnVal == RMFileChooser.APPROVE_OPTION )
      {
        file = chooser.getSelectedFile();
  
        int rc = JOptionPane.YES_OPTION;
        if ( !file.exists())
        {
          JOptionPane.showMessageDialog( this,
                                         file.getName() + " doesn't exist.",
                                         "File doesn't exist.",
                                         JOptionPane.ERROR_MESSAGE );
        }
        else if ( file.isDirectory())
        {
          JOptionPane.showMessageDialog( this,
                                         file.getName() + " is a directory.",
                                         "File doesn't exist.",
                                         JOptionPane.ERROR_MESSAGE );
        }
      }
      else
        return null;
    }
    
    JMenuItem item = new JMenuItem( file.getCanonicalPath());
    item.setActionCommand( file.getCanonicalPath());
    item.addActionListener( this );
    recentFiles.insert( item, 0 );
    while ( recentFiles.getItemCount() > 10 )
      recentFiles.remove( 10 ); 
    recentFiles.setEnabled( true );
    dir = file.getParentFile();
    preferences.setProperty( "fileFolder", dir.getCanonicalPath());
    saveItem.setEnabled( true );
    saveAsItem.setEnabled( true );
    remoteConfig = new RemoteConfiguration( file );
    generalPanel.set( remoteConfig );

    keyMovePanel.set( remoteConfig );
    macroPanel.set( remoteConfig );
    AddressRange range = remoteConfig.getRemote().getAdvanceCodeAddress();
    int available = range.getEnd() - range.getStart(); 
    advProgressBar.setMinimum( 0 );
    advProgressBar.setMaximum( available );
    int used = remoteConfig.getAdvancedCodeBytesUsed();
    advProgressBar.setValue( used );
    advProgressBar.setString( Integer.toString( available - used ) + " free" );

    devicePanel.set( remoteConfig );
    protocolPanel.set( remoteConfig );
    range = remoteConfig.getRemote().getUpgradeAddress();
    available = range.getEnd() - range.getStart(); 
    upgradeProgressBar.setMinimum( 0 );
    upgradeProgressBar.setMaximum( available );
    used = remoteConfig.getUpgradeCodeBytesUsed();
    upgradeProgressBar.setValue( used );
    upgradeProgressBar.setString( Integer.toString( available - used ) + " free" );

    learnedPanel.set( remoteConfig );
    range = remoteConfig.getRemote().getLearnedAddress();
    if ( range != null )
    {
      available = range.getEnd() - range.getStart(); 
      learnedProgressBar.setMinimum( 0 );
      learnedProgressBar.setMaximum( available );
      used = remoteConfig.getLearnedSignalBytesUsed();
      learnedProgressBar.setValue( used );
      learnedProgressBar.setString( Integer.toString( available - used ) + " free" );
    }
    else
    {
      learnedProgressBar.setValue( 0 );
      learnedProgressBar.setString( "N/A" );
    }

    rawDataPanel.set( remoteConfig );

    setTitleFile( file );
    this.file = file;
    return file;
  }

  public void saveAs()
    throws IOException
  {
    RMFileChooser chooser = getFileChooser();
    chooser.setSelectedFile( file );
    int returnVal = chooser.showSaveDialog( this );
    if ( returnVal == RMFileChooser.APPROVE_OPTION )
    {
      String name = chooser.getSelectedFile().getAbsolutePath();
      if ( !name.toLowerCase().endsWith( ".rmir" ))
        name = name + ".rmir";
      File newFile = new File( name );
      int rc = JOptionPane.YES_OPTION;
      if ( newFile.exists())
      {
        rc = JOptionPane.showConfirmDialog( this,
                                            newFile.getName() + " already exists.  Do you want to replace it?",
                                            "Replace existing file?",
                                            JOptionPane.YES_NO_OPTION );
      }
      if ( rc == JOptionPane.YES_OPTION )
      {
        file = newFile;
        remoteConfig.save( file );
        setTitleFile( file );
      }
    }
  }

  private void setTitleFile( File file )
  {
    if ( file == null )
      setTitle( "Java IR" );
    else
      setTitle( "Java IR: " + file.getName() + " - " + remoteConfig.getRemote().getName());
  }

  public void actionPerformed( ActionEvent e )
  {
    try 
    {
      Object source = e.getSource();
      if ( source == openItem )
        openFile();
      else if ( source == saveItem )
        remoteConfig.save( file );
      else if ( source == saveAsItem )
        saveAs();
      else if ( source == exitItem )
        dispatchEvent( new WindowEvent( this, WindowEvent.WINDOW_CLOSING ));
      else if ( source == aboutItem )
      {
        String text = "<html><b>Java IR, " + version + "</b>" +
                      "<p>Java version " + System.getProperty( "java.version" ) + " from " + System.getProperty( "java.vendor" ) + "</p>" +
                      "<p>RDFs loaded from <b>" + preferences.getProperty( "RDFPath" ) + "</b></p>" +
                      "<p>Written primarily by <i>Greg Bush</i>, and now accepting donations " +
                      "at <a href=\"http://sourceforge.net/donate/index.php?user_id=735638\">http://sourceforge.net/donate/index.php?user_id=735638</a></p>" +
                      "</html>";

        JEditorPane pane = new JEditorPane( "text/html", text );
        pane.setEditable( false );
        pane.setBackground( getContentPane().getBackground());
        new TextPopupMenu( pane );
        JScrollPane scroll = new JScrollPane( pane );
        Dimension d = pane.getPreferredSize();
        d.height = ( d.height * 5 ) / 4;
        d.width = ( d.width * 2 ) / 3;
        scroll.setPreferredSize( d );

        JOptionPane.showMessageDialog( this, scroll, "About Java IR", JOptionPane.INFORMATION_MESSAGE );
      }
      else if ( source == downloadItem )
      {
        JP12Serial serial = new JP12Serial();
        String port = serial.openRemote( null );
        if ( port == null )
        {
          JOptionPane.showMessageDialog( this, "No response from remote!\n" );
          return;
        }
        String sig = serial.getRemoteSignature();
        Remote[] remotes = RemoteManager.getRemoteManager().findRemoteBySignature( sig );
        Remote remote = null;
        if ( remotes.length == 0 )
        {
          JOptionPane.showMessageDialog( this, "No RDF matches signature " + sig );
          return;
        }
        else if ( remotes.length == 1 )
          remote = remotes[ 0 ];
        else // ( remotes.length > 1 )
        {  
            String message = "Please pick the best match to your remote from the following list:";
            Object rc = ( Remote )JOptionPane.showInputDialog( null,
                                                               message,
                                                               "Ambiguous Remote",
                                                               JOptionPane.ERROR_MESSAGE,
                                                               null,
                                                               remotes,
                                                               remotes[ 0 ]);
            if ( rc == null )
              return;
            else
              remote = ( Remote )rc;
        }
        remote.load();
        remoteConfig = new RemoteConfiguration( remote );
        serial.readRemote( remote.getBaseAddress(), remoteConfig.getData());
        remoteConfig.parseData();
        generalPanel.set( remoteConfig );

        keyMovePanel.set( remoteConfig );
        macroPanel.set( remoteConfig );
        AddressRange range = remoteConfig.getRemote().getAdvanceCodeAddress();
        int available = range.getEnd() - range.getStart(); 
        advProgressBar.setMinimum( 0 );
        advProgressBar.setMaximum( available );
        int used = remoteConfig.getAdvancedCodeBytesUsed();
        advProgressBar.setValue( used );
        advProgressBar.setString( Integer.toString( available - used ) + " free" );
    
        devicePanel.set( remoteConfig );
        protocolPanel.set( remoteConfig );
        range = remoteConfig.getRemote().getUpgradeAddress();
        available = range.getEnd() - range.getStart(); 
        upgradeProgressBar.setMinimum( 0 );
        upgradeProgressBar.setMaximum( available );
        used = remoteConfig.getUpgradeCodeBytesUsed();
        upgradeProgressBar.setValue( used );
        upgradeProgressBar.setString( Integer.toString( available - used ) + " free" );
    
        learnedPanel.set( remoteConfig );
        range = remoteConfig.getRemote().getLearnedAddress();
        available = range.getEnd() - range.getStart(); 
        learnedProgressBar.setMinimum( 0 );
        learnedProgressBar.setMaximum( available );
        used = remoteConfig.getLearnedSignalBytesUsed();
        learnedProgressBar.setValue( used );
        learnedProgressBar.setString( Integer.toString( available - used ) + " free" );
    
        rawDataPanel.set( remoteConfig );

      }
      else
      {
        JMenuItem item = ( JMenuItem )source;
        File file = new File( item.getActionCommand());
        recentFiles.remove( item );
        if ( file.canRead())
          openFile( file );
      }
    }
    catch ( Exception ex )
    {
      ex.printStackTrace( System.err );
    }
  }

  public void propertyChange( PropertyChangeEvent event )
  {
    Object source = event.getSource();
    if (( source == keyMovePanel.getModel()) || ( source == macroPanel.getModel()))
    {
      int used = remoteConfig.updateAdvancedCodes();
      advProgressBar.setValue( used );
      advProgressBar.setString( Integer.toString( advProgressBar.getMaximum() - used ) + " free" );      
    }
    else if (( source == devicePanel.getModel()) || ( source == protocolPanel.getModel()))
    {
      int used = remoteConfig.updateUpgrades();
      upgradeProgressBar.setValue( used );
      upgradeProgressBar.setString( Integer.toString( upgradeProgressBar.getMaximum() - used ) + " free" );
    }
    else if ( source == learnedPanel.getModel())
    {
      int used = remoteConfig.updateLearnedSignals();
      learnedProgressBar.setValue( used );
      learnedProgressBar.setString( Integer.toString( learnedProgressBar.getMaximum() - used ) + " free" );
    }
    System.err.println( "source is " + source );
    remoteConfig.updateCheckSums();
  }
  
  private static void createAndShowGUI( String[] args )
  {
    try
    {
      UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName());
      boolean launchRM = false;
      for ( int i = 0; i < args.length; ++i )
      {
        if ( args[ i ].equals( "-ir" ))
          launchRM = true;
      }
      if ( launchRM )
      {
        System.setErr( new PrintStream( new FileOutputStream( new File ( System.getProperty( "user.dir" ), "javair.err" ))));
        RemoteMaster rm = new RemoteMaster();
      }
      else
      {
        KeyMapMaster km = new KeyMapMaster( args );
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

  public static void main( String[] args )
  {
    JDialog.setDefaultLookAndFeelDecorated( true );
    JFrame.setDefaultLookAndFeelDecorated( true );
    Toolkit.getDefaultToolkit().setDynamicLayout( true );

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

  private final static String[] rmEndings = { ".rmir" };
  private final static String[] irEndings = { ".ir" };
  private final static String[] txtEndings = { ".txt" };
}
