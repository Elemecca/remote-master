package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

import com.hifiremote.jp1.io.*;

// TODO: Auto-generated Javadoc
/**
 * Description of the Class.
 * 
 * @author     Greg
 * @created    November 30, 2006
 */
public class RemoteMaster
   extends JP1Frame
   implements ActionListener, PropertyChangeListener
{
  
  /** The frame. */
  private static JFrame frame = null;
  
  /** Description of the Field. */
  public final static String version = "v1.87";
  
  /** The dir. */
  private File dir = null;
  
  /** Description of the Field. */
  public File file = null;
  
  /** The remote config. */
  private RemoteConfiguration remoteConfig = null;
  
  /** The chooser. */
  private RMFileChooser chooser = null;

  /** The open item. */
  private JMenuItem openItem = null;
  
  /** The save item. */
  private JMenuItem saveItem = null;
  
  /** The save as item. */
  private JMenuItem saveAsItem = null;
  
  /** The export ir item. */
  private JMenuItem exportIRItem = null;
  
  /** The recent files. */
  private JMenu recentFiles = null;
  
  /** The exit item. */
  private JMenuItem exitItem = null;

  // Remote menu items
  /** The interfaces. */
  private ArrayList< IO > interfaces = new ArrayList< IO >();
  
  /** The download item. */
  private JMenuItem downloadItem = null;
  
  /** The upload item. */
  private JMenuItem uploadItem = null;
  
  /** The upload wav item. */
  private JMenuItem uploadWavItem = null;

  // Help menu items
  /** The about item. */
  private JMenuItem aboutItem = null;

  /** The tabbed pane. */
  private JTabbedPane tabbedPane = null;
  
  /** The general panel. */
  private GeneralPanel generalPanel = null;
  
  /** The key move panel. */
  private KeyMovePanel keyMovePanel = null;
  
  /** The macro panel. */
  private MacroPanel macroPanel = null;
  
  /** The special function panel. */
  private SpecialFunctionPanel specialFunctionPanel = null;
  
  /** The device panel. */
  private DeviceUpgradePanel devicePanel = null;
  
  /** The protocol panel. */
  private ProtocolUpgradePanel protocolPanel = null;
  
  /** The learned panel. */
  private LearnedSignalPanel learnedPanel = null;
  
  /** The raw data panel. */
  private RawDataPanel rawDataPanel = null;

  /** The adv progress bar. */
  private JProgressBar advProgressBar = null;
  
  /** The upgrade progress bar. */
  private JProgressBar upgradeProgressBar = null;
  
  /** The learned progress bar. */
  private JProgressBar learnedProgressBar = null;

  /**
   * Constructor for the RemoteMaster object.
   * 
   * @param workDir the work dir
   * @param prefs the prefs
   * 
   * @throws Exception the exception
   * 
   * @exception  Exception  Description of the Exception
   */
  public RemoteMaster( File workDir, PropertyFile prefs )
    throws Exception
  {
    super( "RM IR", prefs );

    dir = properties.getFileProperty( "IRPath", workDir );
    createMenus();

    setDefaultCloseOperation( EXIT_ON_CLOSE );
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
              properties.setProperty( "RecentIRs." + i, item.getActionCommand() );
            }
            int state = getExtendedState();
            if ( state != Frame.NORMAL )
              setExtendedState( Frame.NORMAL );
            Rectangle bounds = getBounds();
            properties.setProperty( "RMBounds", "" + bounds.x + ',' + bounds.y + ',' + bounds.width + ',' + bounds.height );

            properties.save();
          }
          catch ( Exception exc )
          {
            exc.printStackTrace( System.err );
          }
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

    try
    {
      LearnedSignal.getDecodeIR();
      learnedPanel = new LearnedSignalPanel();
      tabbedPane.addTab( "Learned Signals", learnedPanel );
      learnedPanel.addPropertyChangeListener( this );
    }
    catch ( NoClassDefFoundError ncdfe )
    {
      System.err.println( "DecodeIR class not found!" );
    }
    catch ( NoSuchMethodError nsme )
    {
      System.err.println( "DecodeIR class is wrong version!" );
    }
    catch ( UnsatisfiedLinkError ule )
    {
      System.err.println( "DecodeIR JNI interface not found!" );
    }

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
      pack();
    setVisible( true );
  }

  /**
   * Gets the frame attribute of the RemoteMaster class.
   * 
   * @return    The frame value
   */
  public static JFrame getFrame()
  {
    return frame;
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
      String temp = properties.getProperty( propName );
      if ( temp == null )
        break;
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
      recentFiles.setEnabled( true );
    menu.addSeparator();

    exitItem = new JMenuItem( "Exit", KeyEvent.VK_X );
    exitItem.addActionListener( this );
    menu.add( exitItem );

    menu = new JMenu( "Remote" );
    menu.setMnemonic( KeyEvent.VK_R );
    menuBar.add( menu );

    File userDir = new File( System.getProperty( "user.dir" ));
    try
    {
      interfaces.add( new JP1Parallel( userDir ));
    }
    catch ( LinkageError le )
    {
      System.err.println( "Unable to create JP1Parallel object: " + le.getMessage());
    }

    try
    {
      interfaces.add( new JP12Serial( userDir ));
    }
    catch ( LinkageError le )
    {
      System.err.println( "Unable to create JP12Serial object: " + le.getMessage());
    }

    try
    {
      interfaces.add( new JP1USB( userDir ));
    }
    catch ( LinkageError le )
    {
      System.err.println( "Unable to create JP1USB object: " + le.getMessage());
    }

    ActionListener interfaceListener = new ActionListener()
    {
      public void actionPerformed( ActionEvent event )
      {
        String command = event.getActionCommand();
        if ( command.equals( "autodetect" ))
        {
          properties.remove( "Interface" );
          properties.remove( "Port" );
          return;
        }

        for ( IO io : interfaces )
        {
          if ( io.getInterfaceName().equals( command ))
          {
            String defaultPort = null;
            if ( command.equals( properties.getProperty( "Interface" )))
              defaultPort = properties.getProperty( "Port" );

            String[] availablePorts = io.getPortNames();
            
            PortDialog d = new PortDialog( RemoteMaster.this, availablePorts, defaultPort );
            d.setVisible( true );
            if ( d.getUserAction() == JOptionPane.OK_OPTION )
            {
              String port = d.getPort();
              properties.setProperty( "Interface", io.getInterfaceName());
              if ( port.equals( PortDialog.AUTODETECT ))
                properties.remove( "Port" );
              else
                properties.setProperty( "Port", port );
            }

            break;
          }
        }
      }
    };

    if ( !interfaces.isEmpty())
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

      for ( IO io : interfaces )
      {
        String ioName = io.getInterfaceName();
        item = new JRadioButtonMenuItem( ioName + "..." );
        item.setActionCommand( ioName );
        item.setSelected( ioName.equals( preferredInterface ));
        subMenu.add( item );
        group.add( item );
        item.addActionListener( interfaceListener );
      }
    }

    downloadItem = new JMenuItem( "Download from Remote", KeyEvent.VK_D );
    downloadItem.setEnabled( !interfaces.isEmpty());
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
   * Gets the fileChooser attribute of the RemoteMaster object.
   * 
   * @return    The fileChooser value
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
   * Description of the Method.
   * 
   * @return                Description of the Return Value
   * 
   * @throws Exception the exception
   * 
   * @exception  Exception  Description of the Exception
   */
  public File openFile()
    throws Exception
  {
    return openFile( null );
  }

  /**
   * Description of the Method.
   * 
   * @param file the file
   * 
   * @return                Description of the Return Value
   * 
   * @throws Exception the exception
   * 
   * @exception  Exception  Description of the Exception
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
      KeyMapMaster km = new KeyMapMaster( properties );
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
    uploadItem.setEnabled( !interfaces.isEmpty());
    remoteConfig = new RemoteConfiguration( file );
    update();
    setTitleFile( file );
    this.file = file;
    return file;
  }

  /**
   * Description of the Method.
   * 
   * @param file the file
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   * 
   * @exception  IOException  Description of the Exception
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
    properties.setProperty( "IRPath", dir );
  }

  /**
   * Description of the Method.
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   * 
   * @exception  IOException  Description of the Exception
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
      uploadItem.setEnabled( !interfaces.isEmpty());
    }
  }

  /**
   * Description of the Method.
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   * 
   * @exception  IOException  Description of the Exception
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
   * Sets the titleFile attribute of the RemoteMaster object.
   * 
   * @param file the file
   */
  private void setTitleFile( File file )
  {
    if ( file == null )
      setTitle( "Java IR" );
    else
      setTitle( "Java IR: " + file.getName() + " - " + remoteConfig.getRemote().getName() );
  }

  /**
   * Gets the open interface.
   * 
   * @return the open interface
   */
  private IO getOpenInterface()
  {
    String interfaceName = properties.getProperty( "Interface" );
    String portName = properties.getProperty( "Port" );
    if ( interfaceName != null )
    {
      for ( IO temp : interfaces )
      {
        if ( temp.getInterfaceName().equals( interfaceName ))
        {
          if ( temp.openRemote( portName ) != null )
            return temp;
        }
      }
    }
    else
    {
      for ( IO temp : interfaces )
      {
        portName = temp.openRemote();
        if ( portName != null )
          return temp;
      }
    }
    return null;
  }
  
  /**
   * Description of the Method.
   * 
   * @param e the e
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
          "<p>RDFs loaded from <b>" + properties.getProperty( "RDFPath" ) + "</b></p>";
        try
        {
          String v = LearnedSignal.getDecodeIR().getVersion();
          text += "<p>DecodeIR version " + v + "</p>";
        }
        catch ( LinkageError le )
        {
          text += "<p><b>DecodeIR is not available!</b></p>";
        }

        if ( !interfaces.isEmpty())
        {
          text += "<p>Interfaces:<ul>";
          for ( IO io : interfaces )
            text += "<li>" + io.getInterfaceName() + " version " + io.getInterfaceVersion() + "</li>";
          text += "</ul></p>";
        }

        text += "<p>Written primarily by <i>Greg Bush</i>, and now accepting donations " +
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

        JOptionPane.showMessageDialog( this, scroll, "About Java IR", JOptionPane.INFORMATION_MESSAGE, null );
      }
      else if ( source == downloadItem )
      {
        IO io = getOpenInterface();
        if ( io == null )
        {
          JOptionPane.showMessageDialog( this, "No remotes found!" );
          return;
        }
        String sig = io.getRemoteSignature();
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
        io.readRemote( remote.getBaseAddress(), remoteConfig.getData() );
        io.closeRemote();
        remoteConfig.parseData();
        saveAsItem.setEnabled( true );
        update();
      }
      else if ( source == uploadItem )
      {
        IO io = getOpenInterface();
        if ( io == null )
        {
          JOptionPane.showMessageDialog( this, "No remotes found!" );
          return;
        }
        String sig = io.getRemoteSignature();
        if ( !sig.equals( remoteConfig.getRemote().getSignature() ) )
        {
          JOptionPane.showMessageDialog( this, "Signatures don't match!\n" );
          io.closeRemote();
          return;
        }
        int rc = io.writeRemote( remoteConfig.getRemote().getBaseAddress(), remoteConfig.getData() );
        io.closeRemote();

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
   * Description of the Method.
   */
  private void update()
  {
    if ( remoteConfig != null )
      setTitle( "RMIR - " + remoteConfig.getRemote().getName());
    else
      setTitle( "RMIR" );

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
    AddressRange range = remoteConfig.getRemote().getAdvancedCodeAddress();
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

    if ( learnedPanel != null )
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
   * Description of the Method.
   * 
   * @param event the event
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
    else if (( learnedPanel != null ) && ( source == learnedPanel.getModel()))
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
   * Description of the Method.
   * 
   * @param args the args
   */
  private static void createAndShowGUI( ArrayList< String > args )
  {
    try
    {
      File workDir = new File( System.getProperty( "user.dir" ) );
      File propertiesFile = null;
      File fileToOpen = null;
      boolean launchRM = false;
      for ( int i = 0; i < args.size(); ++i )
      {
        String parm = args.get( i );
        if ( parm.equalsIgnoreCase( "-ir" ) )
          launchRM = true;
        else if ( "-home".startsWith( parm ))
        {
          String dirName = args.get( ++i );
          System.err.println( parm + " applies to \"" + dirName + '"' );
          workDir = new File( dirName );
          System.setProperty( "user.dir", workDir.getCanonicalPath() );
        }
        else if ( "-properties".startsWith( parm ))
        {
          String fileName = args.get( ++i );
          System.err.println( "Properties file name is \"" + fileName + '"' );
          propertiesFile = new File( fileName );
        }
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

      RemoteManager.getRemoteManager().loadRemotes( properties );

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
   * The main program for the RemoteMaster class.
   * 
   * @param args the args
   */
  public static void main( String[] args )
  {
    JDialog.setDefaultLookAndFeelDecorated( true );
    JFrame.setDefaultLookAndFeelDecorated( true );
    Toolkit.getDefaultToolkit().setDynamicLayout( true );

    for ( String arg: args )
    {
      if ( "-version".startsWith( arg ))
      {
        System.out.println( version );
        return;
      }
      else
        parms.add( arg );
    }
    javax.swing.SwingUtilities.invokeLater(
      new Runnable()
      {
        public void run()
        {
          createAndShowGUI( parms );
        }
      } );
  }

  /** The parms. */
  private static ArrayList<String> parms = new ArrayList<String>();

  /** The Constant rmirEndings. */
  private final static String[] rmirEndings = {".rmir"};
  
  /** The Constant rmduEndings. */
  private final static String[] rmduEndings = {".rmdu"};
  
  /** The Constant irEndings. */
  private final static String[] irEndings = {".ir"};
  
  /** The Constant txtEndings. */
  private final static String[] txtEndings = {".txt"};
}

