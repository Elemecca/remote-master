package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import com.hifiremote.decodeir.*;

import com.hifiremote.jp1.io.*;

/**
 *  Description of the Class
 *
 *@author     Greg
 *@created    November 30, 2006
 */
public class RemoteMaster
   extends JP1Frame
   implements ActionListener, PropertyChangeListener
{
  private static JFrame frame = null;
  /**
   *  Description of the Field
   */
  public final static String version = "v1.73";
  private File dir = null;
  /**
   *  Description of the Field
   */
  public File file = null;
  private RemoteConfiguration remoteConfig = null;
  private RMFileChooser chooser = null;
  private PropertyFile preferences = null;

  // File menu items
  private JMenuItem newItem = null;
  private JMenuItem openItem = null;
  private JMenuItem saveItem = null;
  private JMenuItem saveAsItem = null;
  private JMenuItem exportIRItem = null;
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
  private SpecialFunctionPanel specialFunctionPanel = null;
  private DeviceUpgradePanel devicePanel = null;
  private ProtocolUpgradePanel protocolPanel = null;
  private LearnedSignalPanel learnedPanel = null;
  private RawDataPanel rawDataPanel = null;

  private JProgressBar advProgressBar = null;
  private JProgressBar upgradeProgressBar = null;
  private JProgressBar learnedProgressBar = null;

  /**
   *  Constructor for the RemoteMaster object
   *
   *@param  workDir        Description of the Parameter
   *@param  prefs          Description of the Parameter
   *@exception  Exception  Description of the Exception
   */
  public RemoteMaster( File workDir, PropertyFile prefs )
    throws Exception
  {
    super( "Java IR" );
    preferences = prefs;

    dir = preferences.getFileProperty( "IRPath", workDir );
    createMenus();

    setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
    setDefaultLookAndFeelDecorated( true );

    addWindowListener(
      new WindowAdapter()
      {
        public void windowClosing( WindowEvent event )
        {
          try
          {
            for ( int i = 0; i < recentFiles.getItemCount(); ++i )
            {
              JMenuItem item = recentFiles.getItem( i );
              preferences.setProperty( "RecentIRs." + i, item.getActionCommand() );
            }
            int state = getExtendedState();
            if ( state != Frame.NORMAL )
              setExtendedState( Frame.NORMAL );
            Rectangle bounds = getBounds();
            preferences.setProperty( "RMBounds", "" + bounds.x + ',' + bounds.y + ',' + bounds.width + ',' + bounds.height );

            preferences.save();
          }
          catch ( Exception exc )
          {
            exc.printStackTrace( System.err );
          }
          System.exit( 0 );
        }
      } );

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

    specialFunctionPanel = new SpecialFunctionPanel();
    tabbedPane.add( "Special Functions", specialFunctionPanel );
    specialFunctionPanel.addPropertyChangeListener( this );

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

    statusBar.add( new JLabel( "Move/Macro:" ) );

    advProgressBar = new JProgressBar();
    advProgressBar.setStringPainted( true );
    advProgressBar.setString( "N/A" );
    statusBar.add( advProgressBar );

    statusBar.add( Box.createHorizontalStrut( 5 ) );
    JSeparator sep = new JSeparator( SwingConstants.VERTICAL );
    Dimension d = sep.getPreferredSize();
    d.height = advProgressBar.getPreferredSize().height;
    sep.setPreferredSize( d );
    statusBar.add( sep );

    statusBar.add( new JLabel( "Upgrade:" ) );

    upgradeProgressBar = new JProgressBar();
    upgradeProgressBar.setStringPainted( true );
    upgradeProgressBar.setString( "N/A" );
    statusBar.add( upgradeProgressBar );

    statusBar.add( Box.createHorizontalStrut( 5 ) );
    sep = new JSeparator( SwingConstants.VERTICAL );
    sep.setPreferredSize( d );
    statusBar.add( sep );

    statusBar.add( new JLabel( "Learned:" ) );

    learnedProgressBar = new JProgressBar();
    learnedProgressBar.setStringPainted( true );
    learnedProgressBar.setString( "N/A" );
    statusBar.add( learnedProgressBar );

    String temp = preferences.getProperty( "RMBounds" );
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
      pack();
    setVisible( true );
  }

  /**
   *  Gets the frame attribute of the RemoteMaster class
   *
   *@return    The frame value
   */
  public static JFrame getFrame()
  {
    return frame;
  }

  /**
   *  Gets the preferences attribute of the RemoteMaster object
   *
   *@return    The preferences value
   */
  public PropertyFile getPreferences()
  {
    return preferences;
  }

  /**
   *  Description of the Method
   */
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

    saveAsItem = new JMenuItem( "Save as...", KeyEvent.VK_A );
    saveAsItem.setDisplayedMnemonicIndex( 5 );
    saveAsItem.setEnabled( false );
    saveAsItem.addActionListener( this );
    menu.add( saveAsItem );

//    revertItem = new JMenuItem( "Revert to saved" );
//    revertItem.setMnemonic( KeyEvent.VK_R );
//    revertItem.addActionListener( this );
//    menu.add( revertItem );

    menu.addSeparator();
    exportIRItem = new JMenuItem( "Export as IR...", KeyEvent.VK_I );
    exportIRItem.setEnabled( false );
    exportIRItem.addActionListener( this );
    menu.add( exportIRItem );

    menu.addSeparator();
    recentFiles = new JMenu( "Recent" );
    menu.add( recentFiles );
    recentFiles.setEnabled( false );
    for ( int i = 0; i < 10; i++ )
    {
      String propName = "RecentIRs." + i;
      String temp = preferences.getProperty( propName );
      if ( temp == null )
        break;
      preferences.remove( propName );
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

  /**
   *  Gets the fileChooser attribute of the RemoteMaster object
   *
   *@return    The fileChooser value
   */
  public RMFileChooser getFileChooser()
  {
    if ( chooser != null )
      return chooser;

    RMFileChooser chooser = new RMFileChooser( dir );
    EndingFileFilter irFilter = new EndingFileFilter( "RM IR files (*.rmir)", rmirEndings );
    chooser.addChoosableFileFilter( irFilter );
    chooser.addChoosableFileFilter( new EndingFileFilter( "IR files (*.ir)", irEndings ) );
    chooser.addChoosableFileFilter( new EndingFileFilter( "RM Device Upgrades (*.rmdu)", rmduEndings ) );
    chooser.addChoosableFileFilter( new EndingFileFilter( "KM Device Upgrades (*.txt)", txtEndings ) );
    chooser.setFileFilter( irFilter );

    return chooser;
  }

  /**
   *  Description of the Method
   *
   *@return                Description of the Return Value
   *@exception  Exception  Description of the Exception
   */
  public File openFile()
    throws Exception
  {
    return openFile( null );
  }

  /**
   *  Description of the Method
   *
   *@param  file           Description of the Parameter
   *@return                Description of the Return Value
   *@exception  Exception  Description of the Exception
   */
  public File openFile( File file )
    throws Exception
  {
    while ( file == null )
    {
      RMFileChooser chooser = getFileChooser();
      int returnVal = chooser.showOpenDialog( this );
      if ( returnVal == RMFileChooser.APPROVE_OPTION )
      {
        file = chooser.getSelectedFile();

        int rc = JOptionPane.YES_OPTION;
        if ( !file.exists() )
          JOptionPane.showMessageDialog( this,
            file.getName() + " doesn't exist.",
            "File doesn't exist.",
            JOptionPane.ERROR_MESSAGE );

        else if ( file.isDirectory() )
          JOptionPane.showMessageDialog( this,
            file.getName() + " is a directory.",
            "File doesn't exist.",
            JOptionPane.ERROR_MESSAGE );

      }
      else
        return null;
    }

    System.err.println( "Opening " + file.getCanonicalPath() + ", last modified " +
      DateFormat.getInstance().format( new Date( file.lastModified() ) ) );
    String ext = file.getName().toLowerCase();
    int dot = ext.lastIndexOf( '.' );
    ext = ext.substring( dot );

    if ( ext.equals( ".rmdu" ) || ext.equals( ".txt" ) )
    {
      KeyMapMaster km = new KeyMapMaster( preferences );
      km.loadUpgrade( file );
      return null;
    }

    if ( ext.equals( ".rmir" ) )
    {
      updateRecentFiles( file );
      saveItem.setEnabled( true );
      saveAsItem.setEnabled( true );
    }
    else
    {
      saveItem.setEnabled( false );
      saveAsItem.setEnabled( true );
    }
    exportIRItem.setEnabled( true );
    uploadItem.setEnabled( true );
    remoteConfig = new RemoteConfiguration( file );
    update();
    setTitleFile( file );
    this.file = file;
    return file;
  }

  /**
   *  Description of the Method
   *
   *@param  file             Description of the Parameter
   *@exception  IOException  Description of the Exception
   */
  private void updateRecentFiles( File file )
    throws IOException
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
      recentFiles.remove( 10 );
    recentFiles.setEnabled( true );
    dir = file.getParentFile();
    preferences.setProperty( "IRPath", dir );
  }

  /**
   *  Description of the Method
   *
   *@exception  IOException  Description of the Exception
   */
  public void saveAs()
    throws IOException
  {
    RMFileChooser chooser = getFileChooser();
    if ( file != null )
    {
      String name = file.getName().toLowerCase();
      if ( name.endsWith( ".ir" ) || name.endsWith( ".txt" ) )
      {
        int dot = name.lastIndexOf( '.' );
        name = name.substring( 0, dot ) + ".rmir";
        file = new File( name );
      }
      chooser.setSelectedFile( file );
    }
    int returnVal = chooser.showSaveDialog( this );
    if ( returnVal == RMFileChooser.APPROVE_OPTION )
    {
      String name = chooser.getSelectedFile().getAbsolutePath();
      if ( !name.toLowerCase().endsWith( ".rmir" ) )
        name = name + ".rmir";
      File newFile = new File( name );
      int rc = JOptionPane.YES_OPTION;
      if ( newFile.exists() )
        rc = JOptionPane.showConfirmDialog( this,
          newFile.getName() + " already exists.  Do you want to replace it?",
          "Replace existing file?",
          JOptionPane.YES_NO_OPTION );

      if ( rc != JOptionPane.YES_OPTION )
        return;

      file = newFile;
      remoteConfig.save( file );
      setTitleFile( file );
      updateRecentFiles( file );
      saveItem.setEnabled( true );
      exportIRItem.setEnabled( true );
      uploadItem.setEnabled( true );
    }
  }

  /**
   *  Description of the Method
   *
   *@exception  IOException  Description of the Exception
   */
  public void exportAsIR()
    throws IOException
  {
    RMFileChooser chooser = getFileChooser();
    String name = file.getName().toLowerCase();
    if ( !name.endsWith( ".ir" ) )
    {
      int dot = name.lastIndexOf( '.' );
      name = name.substring( 0, dot ) + ".IR";
      file = new File( name );
    }
    chooser.setSelectedFile( file );
    int returnVal = chooser.showSaveDialog( this );
    if ( returnVal == RMFileChooser.APPROVE_OPTION )
    {
      name = chooser.getSelectedFile().getAbsolutePath();
      if ( !name.toLowerCase().endsWith( ".ir" ) )
        name = name + ".IR";
      File newFile = new File( name );
      int rc = JOptionPane.YES_OPTION;
      if ( newFile.exists() )
        rc = JOptionPane.showConfirmDialog( this,
          newFile.getName() + " already exists.  Do you want to replace it?",
          "Replace existing file?",
          JOptionPane.YES_NO_OPTION );

      if ( rc != JOptionPane.YES_OPTION )
        return;

      file = newFile;
      remoteConfig.exportIR( file );
    }
  }

  /**
   *  Sets the titleFile attribute of the RemoteMaster object
   *
   *@param  file  The new titleFile value
   */
  private void setTitleFile( File file )
  {
    if ( file == null )
      setTitle( "Java IR" );
    else
      setTitle( "Java IR: " + file.getName() + " - " + remoteConfig.getRemote().getName() );
  }

  /**
   *  Description of the Method
   *
   *@param  e  Description of the Parameter
   */
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
      else if ( source == exportIRItem )
        exportAsIR();
      else if ( source == exitItem )
        dispatchEvent( new WindowEvent( this, WindowEvent.WINDOW_CLOSING ) );
      else if ( source == aboutItem )
      {
        String text = "<html><b>Java IR, " + version + "</b>" +
          "<p>Java version " + System.getProperty( "java.version" ) + " from " + System.getProperty( "java.vendor" ) + "</p>" +
          "<p>RDFs loaded from <b>" + preferences.getProperty( "RDFPath" ) + "</b></p>";
        try
        {
          String v = LearnedSignal.getDecodeIR().getVersion();
          text += "<p>DecodeIR version " + v + "<p>";
        }
        catch ( NoSuchMethodError ex )
        {
        }
        JP12Serial serial = new JP12Serial();
        text += "<p>" + serial.getInterfaceName() + " version " + serial.getInterfaceVersion() + "</p>" +
          "<p>Written primarily by <i>Greg Bush</i>, and now accepting donations " +
          "at <a href=\"http://sourceforge.net/donate/index.php?user_id=735638\">http://sourceforge.net/donate/index.php?user_id=735638</a></p>" +
          "</html>";

        JEditorPane pane = new JEditorPane( "text/html", text );
        pane.setEditable( false );
        pane.setBackground( getContentPane().getBackground() );
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
          remote = remotes[0];
        else
        {// ( remotes.length > 1 )

          String message = "Please pick the best match to your remote from the following list:";
          Object rc = ( Remote )JOptionPane.showInputDialog( null,
            message,
            "Ambiguous Remote",
            JOptionPane.ERROR_MESSAGE,
            null,
            remotes,
            remotes[0] );
          if ( rc == null )
            return;
          else
            remote = ( Remote )rc;
        }
        remote.load();
        remoteConfig = new RemoteConfiguration( remote );
        serial.readRemote( remote.getBaseAddress(), remoteConfig.getData() );
        serial.closeRemote();
        remoteConfig.parseData();
        saveAsItem.setEnabled( true );
        update();
      }
      else if ( source == uploadItem )
      {
        JP12Serial serial = new JP12Serial();
        String port = serial.openRemote( null );
        if ( port == null )
        {
          JOptionPane.showMessageDialog( this, "No response from remote!\n" );
          return;
        }
        String sig = serial.getRemoteSignature();
        if ( !sig.equals( remoteConfig.getRemote().getSignature() ) )
        {
          JOptionPane.showMessageDialog( this, "Signatures don't match!\n" );
          serial.closeRemote();
          return;
        }
        int rc = serial.writeRemote( remoteConfig.getRemote().getBaseAddress(), remoteConfig.getData() );
        serial.closeRemote();

        if ( rc != remoteConfig.getData().length )
          JOptionPane.showMessageDialog( this, "writeRemote returned " + rc );
        else
          JOptionPane.showMessageDialog( this, "Upload complete!" );
      }
      else
      {
        JMenuItem item = ( JMenuItem )source;
        File file = new File( item.getActionCommand() );
        recentFiles.remove( item );
        if ( file.canRead() )
          openFile( file );
      }
    }
    catch ( Exception ex )
    {
      ex.printStackTrace( System.err );
    }
  }

  /**
   *  Description of the Method
   */
  private void update()
  {
    generalPanel.set( remoteConfig );
    keyMovePanel.set( remoteConfig );
    macroPanel.set( remoteConfig );
    if ( remoteConfig.getRemote().getSpecialProtocols().isEmpty() )
    {
      if ( tabbedPane.getComponentAt( 3 ) == specialFunctionPanel )
        tabbedPane.remove( 3 );
    }
    else
      if ( tabbedPane.getComponentAt( 3 ) != specialFunctionPanel )
      tabbedPane.insertTab( "Special Functions", null, specialFunctionPanel, null, 3 );

    specialFunctionPanel.set( remoteConfig );
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
  }

  /**
   *  Description of the Method
   *
   *@param  event  Description of the Parameter
   */
  public void propertyChange( PropertyChangeEvent event )
  {
    Object source = event.getSource();
    if ( ( source == keyMovePanel.getModel() ) || ( source == macroPanel.getModel() ) || ( source == specialFunctionPanel.getModel() ) )
    {
      int used = remoteConfig.updateAdvancedCodes();
      advProgressBar.setValue( used );
      advProgressBar.setString( Integer.toString( advProgressBar.getMaximum() - used ) + " free" );
    }
    else if ( ( source == devicePanel.getModel() ) || ( source == protocolPanel.getModel() ) )
    {
      int used = remoteConfig.updateUpgrades();
      upgradeProgressBar.setValue( used );
      upgradeProgressBar.setString( Integer.toString( upgradeProgressBar.getMaximum() - used ) + " free" );
    }
    else if ( source == learnedPanel.getModel() )
    {
      int used = remoteConfig.updateLearnedSignals();
      learnedProgressBar.setValue( used );
      learnedProgressBar.setString( Integer.toString( learnedProgressBar.getMaximum() - used ) + " free" );
    }
    else
      System.err.println( "propertyChange source is " + source );
    remoteConfig.updateCheckSums();
  }

  /**
   *  Description of the Method
   *
   *@param  args  Description of the Parameter
   */
  private static void createAndShowGUI( String[] args )
  {
    try
    {
      File workDir = new File( System.getProperty( "user.dir" ) );
      File propertiesFile = null;
      File fileToOpen = null;
      boolean launchRM = false;
      for ( int i = 0; i < args.length; ++i )
      {
        String parm = args[i];
        if ( parm.equalsIgnoreCase( "-ir" ) )
          launchRM = true;
        else if ( parm.equalsIgnoreCase( "-h" ) )
        {
          workDir = new File( args[++i] );
          System.setProperty( "user.dir", workDir.getCanonicalPath() );
        }
        else if ( parm.equalsIgnoreCase( "-p" ) )
          propertiesFile = new File( args[++i] );
        else
          fileToOpen = new File( parm );
      }

      try
      {
        System.setErr( new PrintStream( new FileOutputStream( new File( workDir, "rmaster.err" ) ) ) );
      }
      catch ( Exception e )
      {
        e.printStackTrace( System.err );
      }

      ClassPathAdder.addFile( workDir );

      FilenameFilter filter =
        new FilenameFilter()
        {
          public boolean accept( File dir, String name )
          {
            String temp = name.toLowerCase();
            return temp.endsWith( ".jar" ) &&
              !temp.endsWith( "remotemaster.jar" ) &&
              !temp.endsWith( "setup.jar" );
          }
        };

      File[] jarFiles = workDir.listFiles( filter );
      ClassPathAdder.addFiles( jarFiles );

      if ( propertiesFile == null )
        propertiesFile = new File( workDir, "RemoteMaster.properties" );
      PropertyFile properties = new PropertyFile( propertiesFile );

      if ( launchRM )
        UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
      else
      {
        String lookAndFeel = properties.getProperty( "LookAndFeel", UIManager.getSystemLookAndFeelClassName() );
        UIManager.setLookAndFeel( lookAndFeel );
      }

      File rdfDir = properties.getFileProperty( "RDFPath", new File( workDir, "rdf" ) );
      rdfDir = RemoteManager.getRemoteManager().loadRemotes( rdfDir );
      properties.setProperty( "RDFPath", rdfDir );

      ProtocolManager.getProtocolManager().load( new File( workDir, "protocols.ini" ) );

      DigitMaps.load( new File( workDir, "digitmaps.bin" ) );

      if ( launchRM )
      {
        RemoteMaster rm = new RemoteMaster( workDir, properties );
        if ( fileToOpen != null )
          rm.openFile( fileToOpen );
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

  /**
   *  The main program for the RemoteMaster class
   *
   *@param  args  The command line arguments
   */
  public static void main( String[] args )
  {
    JDialog.setDefaultLookAndFeelDecorated( true );
    JFrame.setDefaultLookAndFeelDecorated( true );
    Toolkit.getDefaultToolkit().setDynamicLayout( true );

    parms = args;
    javax.swing.SwingUtilities.invokeLater(
      new Runnable()
      {
        public void run()
        {
          createAndShowGUI( parms );
        }
      } );
  }

  private static String[] parms = null;

  private final static String[] rmirEndings = {".rmir"};
  private final static String[] rmduEndings = {".rmdu"};
  private final static String[] irEndings = {".ir"};
  private final static String[] txtEndings = {".txt"};
}

