package com.hifiremote.jp1;

import info.clearthought.layout.TableLayout;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class KeyMapMaster
 extends JFrame
 implements ActionListener, ChangeListener, DocumentListener
{
  private static KeyMapMaster me = null;
  private static final String version = "v 0.85";
  private JMenuItem newItem = null;
  private JMenuItem openItem = null;
  private JMenuItem saveItem = null;
  private JMenuItem saveAsItem = null;
  private JMenuItem importItem = null;
  private JMenuItem exitItem = null;
  private JMenu recentFileMenu = null;
  private JMenuItem useAllRemotes = null;
  private JMenuItem usePreferredRemotes = null;
  private JMenuItem manualItem = null;
  private JRadioButtonMenuItem[] lookAndFeelItems = null;
  private JRadioButtonMenuItem[] promptButtons = null;
  private JLabel messageLabel = null;
  private JTextField description = null;
  private JComboBox remoteList = null;
  private JComboBox deviceTypeList = null;
  private Remote[] remotes = new Remote[ 0 ];
  private Remote[] preferredRemotes = new Remote[ 0 ];
  private Vector preferredRemoteNames = new Vector( 0 );
  private ProtocolManager protocolManager = new ProtocolManager();
  private Remote currentRemote = null;
  private String currentDeviceTypeName = null;
  private SetupPanel setupPanel = null;
  private FunctionPanel functionPanel = null;
  private ExternalFunctionPanel externalFunctionPanel = null;
  private ButtonPanel buttonPanel = null;
  private LayoutPanel layoutPanel = null;
  private OutputPanel outputPanel = null;
  private KeyMapPanel keyMapPanel = null;
  private ProgressMonitor progressMonitor = null;
  private DeviceUpgrade deviceUpgrade = null;
  private static File homeDirectory = null;
  private File propertiesFile = null;
  private File rdfPath = null;
  private File upgradePath = null;
  private File importPath = null;
  private String lastRemoteName = null;
  private String lastRemoteSignature = null;
  private Rectangle bounds = null;
  private static String upgradeExtension = ".rmdu";
  private static String upgradeDirectory = "Upgrades";
  private int promptFlag = 0;
  private final static String[] promptStrings = { "Always", "On Exit", "Never" };
  private final static int[] promptMnemonics = { KeyEvent.VK_A, KeyEvent.VK_X, KeyEvent.VK_N };
  private final static int PROMPT_NEVER = 2;
  private final static int PROMPT_ALWAYS = 0;
  private final static int ACTION_EXIT = 1;
  private final static int ACTION_LOAD = 2;

  public KeyMapMaster( String[] args )
    throws Exception
  {
    super( "RemoteMaster " + version );
    File fileToOpen = parseArgs( args );

    setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
    setDefaultLookAndFeelDecorated( true );
    JDialog.setDefaultLookAndFeelDecorated( true );
    JFrame.setDefaultLookAndFeelDecorated( true );
    Toolkit.getDefaultToolkit().setDynamicLayout( true );
    me = this;

    addWindowListener( new WindowAdapter()
    {
      public void windowClosing( WindowEvent event )
      {
        try
        {
          if ( !promptToSaveUpgrade( ACTION_EXIT ))
            return;
          savePreferences();
        }
        catch ( Exception e )
        {
          System.err.println( "KeyMapMaster.windowClosing() caught an exception!" );
          e.printStackTrace( System.out );
        }
        System.exit( 0 );
      }
    });

    deviceUpgrade = new DeviceUpgrade();

    createMenus();

    loadPreferences();

    Container mainPanel = getContentPane();
    JTabbedPane tabbedPane = new JTabbedPane();
    mainPanel.add( tabbedPane, BorderLayout.CENTER );

    double b = 10;       // space around border/columns
    double i = 5;        // space between rows
    double f = TableLayout.FILL;
    double p = TableLayout.PREFERRED;
    double size[][] =
    {
      { b, p, b, f, b, p, b, p, b },                     // cols
      { b, p, i, p, b }         // rows
    };
    TableLayout tl = new TableLayout( size );
    JPanel panel = new JPanel( tl );

    JLabel label = new JLabel( "Description:" );
    panel.add( label, "1, 1" );
    description = new JTextField( 50 );
    label.setLabelFor( description );
    description.getDocument().addDocumentListener( this );
    panel.add( description, "3, 1, 7, 1" );

    label = new JLabel( "Remote:" );
    panel.add( label, "1, 3" );
    remoteList = new JComboBox();
    label.setLabelFor( remoteList );
    remoteList.setMaximumRowCount( 16 );
    remoteList.setPrototypeDisplayValue( "A Really Long Remote Control Name with an Extender and more" );
    remoteList.setToolTipText( "Choose the remote for the upgrade being created." );
    panel.add( remoteList, "3, 3" );

    label = new JLabel( "Device Type:" );
    panel.add( label, "5, 3" );
//    String[] aliasNames = deviceUpgrade.getDeviceTypeAliasNames();
    deviceTypeList = new JComboBox();
    label.setLabelFor( deviceTypeList );
    deviceTypeList.setPrototypeDisplayValue( "A Device Type" );
    deviceTypeList.setToolTipText( "Choose the device type for the upgrade being created." );
    panel.add( deviceTypeList, "7, 3" );

    mainPanel.add( panel, BorderLayout.NORTH );

    messageLabel = new JLabel( " " );
    messageLabel.setForeground( Color.red );

    mainPanel.add( messageLabel, BorderLayout.SOUTH );

    protocolManager.load( new File( homeDirectory, "protocols.ini" ));

    setupPanel = new SetupPanel( deviceUpgrade, protocolManager );
    currPanel = setupPanel;
    tabbedPane.addTab( "Setup", null, setupPanel, "Enter general information about the upgrade." );

    functionPanel = new FunctionPanel( deviceUpgrade );
    tabbedPane.addTab( "Functions", null, functionPanel,
                       "Define function names and parameters." );

    externalFunctionPanel = new ExternalFunctionPanel( deviceUpgrade );
    tabbedPane.addTab( "External Functions", null, externalFunctionPanel,
                       "Define functions from other device codes." );

    buttonPanel = new ButtonPanel( deviceUpgrade );
    tabbedPane.addTab( "Buttons", null, buttonPanel,
                       "Assign functions to buttons." );

    layoutPanel = new LayoutPanel( deviceUpgrade );
    tabbedPane.addTab( "Layout", null, layoutPanel ,
                       "Button Layout information." );

    keyMapPanel = new KeyMapPanel( deviceUpgrade );
    tabbedPane.addTab( "Key Map", null, keyMapPanel,
                       "Printable list of buttons and their assigned functions" );

    outputPanel = new OutputPanel( deviceUpgrade );
    tabbedPane.addTab( "Output", null, outputPanel,
                       "The output to copy-n-paste into IR." );

    loadRemotes();
    setRemotes();

    int index = 0;
    if ( lastRemoteName != null )
    {
      index = Arrays.binarySearch( remotes, lastRemoteName );
    }
    if ( index < 0 )
      index = 0;

    Remote temp = remotes[ index ];
    temp.load();
    Protocol protocol = ( Protocol )protocolManager.getProtocolsForRemote( temp ).elementAt( 0 );
    deviceUpgrade.setProtocol( protocol );
    setRemote( temp );
    remoteList.setSelectedItem( temp );

    remoteList.addActionListener( this );
    deviceTypeList.addActionListener( this );
    tabbedPane.addChangeListener( this );

    currPanel.update();

    clearMessage();

    if ( bounds != null )
      setBounds( bounds );
    else
      pack();

    if ( fileToOpen != null )
    {
      openFile( fileToOpen );
    }
    show();
  }

  private void createMenus()
  {
    JMenuBar menuBar = new JMenuBar();
    setJMenuBar( menuBar );
    JMenu menu = new JMenu( "File" );
    menu.setMnemonic( KeyEvent.VK_F );
    menuBar.add( menu );
    newItem = new JMenuItem( "New" );
    newItem.setMnemonic( KeyEvent.VK_N );
    newItem.addActionListener( this );
    menu.add( newItem );
    openItem = new JMenuItem( "Open..." );
    openItem.setMnemonic( KeyEvent.VK_O );
    openItem.addActionListener( this );
    menu.add( openItem );
    saveItem = new JMenuItem( "Save" );
    saveItem.setMnemonic( KeyEvent.VK_S );
    saveItem.setEnabled( false );
    saveItem.addActionListener( this );
    menu.add( saveItem );
    saveAsItem = new JMenuItem( "Save as..." );
    saveAsItem.setMnemonic( KeyEvent.VK_A );
    saveAsItem.setDisplayedMnemonicIndex( 5 );
    saveAsItem.addActionListener( this );
    menu.add( saveAsItem );

    menu.addSeparator();
    importItem = new JMenuItem( "Import KM file..." );
    importItem.setMnemonic( KeyEvent.VK_I );
    importItem.addActionListener( this );
    menu.add( importItem );

    menu.addSeparator();
    recentFileMenu = new JMenu( "Recent" );
    recentFileMenu.setMnemonic( KeyEvent.VK_R );
    recentFileMenu.setEnabled( false );
    menu.add( recentFileMenu );
    menu.addSeparator();

    exitItem = new JMenuItem( "Exit" );
    exitItem.setMnemonic( KeyEvent.VK_X );
    exitItem.addActionListener( this );
    menu.add( exitItem );

    menu = new JMenu( "Options" );
    menu.setMnemonic( KeyEvent.VK_O );
    menuBar.add( menu );

    JMenu submenu = new JMenu( "Look and Feel" );
    submenu.setMnemonic( KeyEvent.VK_L );
    menu.add( submenu );

    ButtonGroup group = new ButtonGroup();
    UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();

    ActionListener al = new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        try
        {
          UIManager.setLookAndFeel((( JRadioButtonMenuItem )e.getSource()).getActionCommand());
          SwingUtilities.updateComponentTreeUI( me );
        }
        catch ( Exception x )
        {}
      }
    };

    lookAndFeelItems = new JRadioButtonMenuItem[ info.length ];
    for ( int i = 0; i < info.length; i++ )
    {
      JRadioButtonMenuItem item = new JRadioButtonMenuItem( info[ i ].getName());
      lookAndFeelItems[ i ] = item;
      item.setMnemonic( item.getText().charAt( 0 ));
      item.setActionCommand( info[ i ].getClassName());
      group.add( item );
      submenu.add( item );
      item.addActionListener( al );
    }

    group = new ButtonGroup();
    submenu = new JMenu( "Prompt to Save" );
    menu.add( submenu );

    al = new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        Object source = e.getSource();
        for ( int i = 0; i < promptButtons.length; i++ )
          if ( promptButtons[ i ] == source )
          {
            promptFlag = i;
            break;
          }
      }
    };

    promptButtons = new JRadioButtonMenuItem[ promptStrings.length ];
    for ( int i = 0; i < promptStrings.length; i++ )
    {
      JRadioButtonMenuItem item = new JRadioButtonMenuItem( promptStrings[ i ] );
      item.setMnemonic( promptMnemonics[ i ]);
      promptButtons[ i ] = item;
      item.addActionListener( al );
      group.add( item );
      submenu.add( item );
    }
    promptButtons[ promptFlag ].setSelected( true );

    submenu = new JMenu( "Remotes" );
    menu.add( submenu );

    group = new ButtonGroup();
    useAllRemotes = new JRadioButtonMenuItem( "All" );
    useAllRemotes.setMnemonic( KeyEvent.VK_A );
    al = new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        Object source = e.getSource();
        if (( source == useAllRemotes ) || ( source == usePreferredRemotes ))
          setRemotes();
        else
          editPreferredRemotes();
      }
    };
    useAllRemotes.setSelected( true );
    useAllRemotes.addActionListener( al );
    group.add( useAllRemotes );
    submenu.add( useAllRemotes );

    usePreferredRemotes = new JRadioButtonMenuItem( "Preferred" );
    usePreferredRemotes.setMnemonic( KeyEvent.VK_P );
    usePreferredRemotes.addActionListener( al );
    group.add( usePreferredRemotes );
    submenu.add( usePreferredRemotes );

    submenu.addSeparator();
    JMenuItem item = new JMenuItem( "Edit preferred..." );
    item.setMnemonic( KeyEvent.VK_E );
    item.addActionListener( al );
    submenu.add( item );

//    menu = new JMenu( "Tools" );
//    menuBar.add( menu );

//    manualItem = new JMenuItem( "Manual settings..." );
//    manualItem.addActionListener( this );
//    menu.add( manualItem );
  }

  private void editPreferredRemotes()
  {
    PreferredRemoteDialog d = new PreferredRemoteDialog( this, remotes, preferredRemotes );
    d.show();
    if ( d.getUserAction() == JOptionPane.OK_OPTION )
    {
      preferredRemotes = d.getPreferredRemotes();
      if ( preferredRemotes.length == 0 )
      {
        usePreferredRemotes.setEnabled( false );
        if  ( !useAllRemotes.isSelected())
        {
          useAllRemotes.setSelected( true );
          Remote r = ( Remote )remoteList.getSelectedItem();
          remoteList.removeActionListener( this );
          remoteList.setModel( new DefaultComboBoxModel( remotes ));
          remoteList.setSelectedItem( r );
          remoteList.addActionListener( this );
        }
      }
      else
        usePreferredRemotes.setEnabled( true );

      if ( usePreferredRemotes.isSelected())
        remoteList.setModel( new DefaultComboBoxModel( preferredRemotes ));
    }
  }

  private void editManualSettings()
  {
    System.err.println( "editManualSettings()");
    ManualSettingsDialog d = new ManualSettingsDialog( this, null );
    d.show();
    if ( d.getUserAction() == JOptionPane.OK_OPTION )
    {
      // ?
    }
  }

  private File parseArgs( String[] args )
  {
    homeDirectory = new File( System.getProperty( "user.dir" ));
    File fileToOpen = null;
    for ( int i = 0; i < args.length; i++ )
    {
      String arg = args[ i ];
      if ( arg.charAt( 0 ) == '-' )
      {
        char flag = arg.charAt( 1 );
        String parm = args[ ++i ];
        if ( flag == 'h' )
        {
          homeDirectory = new File( parm );
        }
        else if ( flag == 'p' )
        {
          propertiesFile = new File( parm );
        }
      }
      else
        fileToOpen = new File( arg );
    }
    try
    {
      System.setErr( new PrintStream( new FileOutputStream( new File ( homeDirectory, "rmaster.err" ))));
    }
    catch ( Exception e )
    {
      e.printStackTrace( System.err );
    }
    if ( propertiesFile == null )
    {
      propertiesFile = new File( homeDirectory, "RemoteMaster.properties" );
    }

    return fileToOpen;
  }

  public static void showMessage( String msg )
  {
    if ( msg.length() == 0 )
      msg = " ";
    me.messageLabel.setText( msg );
    Toolkit.getDefaultToolkit().beep();
  }

  public static void clearMessage()
  {
    me.messageLabel.setText( " " );
  }

  private void loadRemotes()
    throws Exception
  {
    File[] files = new File[ 0 ];
    File dir = rdfPath;
    FilenameFilter filter = new FilenameFilter()
    {
      public boolean accept( File dir, String name )
      {
        return name.toLowerCase().endsWith( ".rdf" );
      }
    };

    while ( files.length == 0 )
    {
      files = dir.listFiles( filter );
      if ( files.length == 0 )
      {
        JOptionPane.showMessageDialog( this, "No RDF files were found!",
                                       "Error", JOptionPane.ERROR_MESSAGE );
        JFileChooser chooser = new JFileChooser( dir );
        chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        chooser.setFileFilter( new KMDirectoryFilter());
        chooser.setDialogTitle( "Choose the directory containing the RDFs" );
        int returnVal = chooser.showOpenDialog( this );
        if ( returnVal != JFileChooser.APPROVE_OPTION )
          System.exit( -1 );
        else
          dir = chooser.getSelectedFile();
      }
    }
    rdfPath = dir;

    progressMonitor = new ProgressMonitor( this, "Loading remotes",
                                           "", 0, files.length );
    progressMonitor.setProgress( 0 );
    progressMonitor.setMillisToDecideToPopup( 1000 );

    Vector work = new Vector();
    for ( int i = 0; i < files.length; i++ )
    {
      File rdf = files[ i ];
      progressMonitor.setNote( "Loading " + rdf.getName());

      Remote r = new Remote( rdf );
      work.add( r );
      for ( int j = 1; j < r.getNameCount(); j++ )
        work.add( new Remote( r, j ));

      progressMonitor.setProgress( i );
    }
    remotes = ( Remote[] )work.toArray( remotes );
    progressMonitor.setNote( "Sorting remotes" );
    Arrays.sort( remotes );
    progressMonitor.setProgress( files.length );
    progressMonitor.close();

    work.clear();
    for ( Enumeration e = preferredRemoteNames.elements(); e.hasMoreElements(); )
    {
      String name = ( String )e.nextElement();
      int index = Arrays.binarySearch( remotes, name );
      System.err.println( "Got index " + index + " searching for preferred remote with name \"" + name + "\"" );
      if ( index >= 0 )
        work.add( remotes[ index ]);
    }
    preferredRemotes = ( Remote[] )work.toArray( preferredRemotes );

    if ( preferredRemotes.length == 0 )
    {
      useAllRemotes.setSelected( true );
      usePreferredRemotes.setEnabled( false );
    }

  } // loadRemotes

  public void setRemotes()
  {
    if ( remoteList != null )
    {
      if ( usePreferredRemotes.isSelected())
        remoteList.setModel( new DefaultComboBoxModel( preferredRemotes ));
      else
        remoteList.setModel( new DefaultComboBoxModel( remotes ));
    }
  }

  public void setRemote( Remote remote )
  {
    if (( remoteList != null ) && ( remote != currentRemote ))
    {
      try
      {
        remote.load();
        String[] aliasNames = remote.getDeviceTypeAliasNames();
        String alias = deviceUpgrade.getDeviceTypeAliasName();
        deviceTypeList.removeActionListener( this );
        deviceTypeList.setModel( new DefaultComboBoxModel( aliasNames ));
        deviceTypeList.setMaximumRowCount( aliasNames.length );
        
        int index = 0;
        for ( index = 0; index < aliasNames.length; index++ )
        {
          if ( aliasNames[ index ].equals( alias ))
            break;
        }
        while (( index == aliasNames.length ))
        {
          String msg = "Remote \"" + remote.getName() + "\" does not support the device type " +
          alias + ".  Please select one of the supported device types below to use instead.\n";
          String rc = ( String )JOptionPane.showInputDialog( null,
                                                             msg,
                                                             "Unsupported Device Type",
                                                             JOptionPane.ERROR_MESSAGE,
                                                             null,
                                                             aliasNames,
                                                             null );
          for ( index = 0; index < aliasNames.length; index++ )
          {
            if ( aliasNames[ index ].equals( rc ))
              break;
          }
        }

        deviceTypeList.setSelectedIndex( index );
        
        currentRemote = remote;
        deviceUpgrade.setRemote( remote );
        deviceUpgrade.setDeviceTypeAliasName( aliasNames[ index ]);
        deviceTypeList.addActionListener( this );
      }
      catch ( Exception e )
      {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw );
        e.printStackTrace( pw );
        pw.flush();
        pw.close();
        JOptionPane.showMessageDialog( null, sw.toString(), "Remote Load Error",
                                       JOptionPane.ERROR_MESSAGE );
        System.err.println( sw.toString());
      }
    }
  }

  public void setDeviceTypeName( String aliasName )
  {
    if (( deviceTypeList != null ) && ( aliasName != currentDeviceTypeName ))
    {
      currentDeviceTypeName = aliasName;
      deviceUpgrade.setDeviceTypeAliasName( aliasName );
      deviceTypeList.setSelectedItem( aliasName );
    }
  }

  // ActionListener Methods
  public void actionPerformed( ActionEvent e )
  {
    try
    {
      Object source = e.getSource();

      if ( source == remoteList )
      {
        Remote remote = ( Remote )remoteList.getSelectedItem();
        setRemote( remote );
        validateUpgrade();
        currPanel.update();
      }
      else if ( source == deviceTypeList )
      {
        String typeName = ( String )deviceTypeList.getSelectedItem();
        setDeviceTypeName( typeName );
        currPanel.update();
      }
      else if ( source == newItem )
      {
        if ( !promptToSaveUpgrade( ACTION_LOAD ))
          return;
        deviceUpgrade.reset( remotes, protocolManager );
        setTitle( "RemoteMapMaster " + version );
        description.setText( null );
        remoteList.setSelectedItem( deviceUpgrade.getRemote());
        deviceTypeList.setSelectedItem( deviceUpgrade.getDeviceTypeAliasName());
        saveItem.setEnabled( false );
        currPanel.update();
      }
      else if ( source == saveItem )
      {
        currPanel.commit();
        deviceUpgrade.store();
      }
      else if ( source == saveAsItem )
      {
        currPanel.commit();
        saveAs();
      }
      else if ( source == openItem )
      {
        if ( !promptToSaveUpgrade( ACTION_LOAD ))
          return;
        JFileChooser chooser = new JFileChooser( upgradePath );
        chooser.setFileFilter( new KMFileFilter());
        int returnVal = chooser.showOpenDialog( this );
        if ( returnVal == JFileChooser.APPROVE_OPTION )
        {
          File file = chooser.getSelectedFile();
          String name = file.getAbsolutePath();
          if ( !name.endsWith( upgradeExtension ) && !name.endsWith( ".km" ))
            file = new File( name + upgradeExtension );

          int rc = JOptionPane.YES_OPTION;
          if ( !file.exists())
          {
            JOptionPane.showMessageDialog( this,
                                           file.getName() + " doesn't exist exists.",
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
          else
          {
            openFile( file );
          }
        }
      }
      else if ( source == importItem )
      {
        if ( !promptToSaveUpgrade( ACTION_LOAD ))
          return;
        JFileChooser chooser = new JFileChooser( importPath );
        chooser.setFileFilter( new TextFileFilter());
        int returnVal = chooser.showOpenDialog( this );
        if ( returnVal == JFileChooser.APPROVE_OPTION )
        {
          File file = chooser.getSelectedFile();
          String name = file.getAbsolutePath();
          if ( !name.endsWith( ".txt" ))
            file = new File( name + ".txt" );

          int rc = JOptionPane.YES_OPTION;
          if ( !file.exists())
          {
            JOptionPane.showMessageDialog( this,
                                           file.getName() + " doesn't exist exists.",
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
          else
          {
            importFile( file );
          }
        }
      }
      else if ( source == exitItem )
      {
        dispatchEvent( new WindowEvent( this, WindowEvent.WINDOW_CLOSING ));
      }
      else if ( source == manualItem )
      {
        editManualSettings();
      }
    }
    catch ( Exception ex )
    {
      ex.printStackTrace( System.err );
    }
  } // actionPerformed

  public void saveAs()
    throws IOException
  {
    JFileChooser chooser = new JFileChooser( upgradePath );
    chooser.setFileFilter( new KMFileFilter());
    File f = deviceUpgrade.getFile();
    if ( f == null )
      f = new File( upgradePath, deviceUpgrade.getDescription() + upgradeExtension );
    chooser.setSelectedFile( f );
    int returnVal = chooser.showSaveDialog( this );
    if ( returnVal == JFileChooser.APPROVE_OPTION )
    {
      String name = chooser.getSelectedFile().getAbsolutePath();
      if ( !name.toLowerCase().endsWith( upgradeExtension ))
        name = name + upgradeExtension;
      File file = new File( name );
      int rc = JOptionPane.YES_OPTION;
      if ( file.exists())
      {
        rc = JOptionPane.showConfirmDialog( this,
                                            file.getName() + " already exists.  Do you want to repalce it?",
                                            "Replace existing file?",
                                            JOptionPane.YES_NO_OPTION );
      }
      if ( rc == JOptionPane.YES_OPTION )
      {
        deviceUpgrade.store( file );
        saveItem.setEnabled( true );
        setTitle( "RemoteMaster " + version + ": " + file.getName());
      }
    }
  }

  public boolean promptToSaveUpgrade( int action )
    throws IOException
  {
    if ( promptFlag == PROMPT_NEVER )
      return true;
    else if ( promptFlag != PROMPT_ALWAYS )
    {
      if ( action != ACTION_EXIT )
        return true;
    }

    int rc = JOptionPane.showConfirmDialog( this,
//                                            "All changes made to the current upgrade will be lost if you proceed.\n\n" +
                                            "Do you want to save the current upgrade before proceeding?",
                                            "Save upgrade?",
                                            JOptionPane.YES_NO_CANCEL_OPTION );
    if (( rc == JOptionPane.CANCEL_OPTION ) || ( rc == JOptionPane.CLOSED_OPTION ))
      return false;
    if ( rc == JOptionPane.NO_OPTION )
      return true;

    currPanel.commit();
    if ( deviceUpgrade.getFile() != null )
      deviceUpgrade.store();
    else
      saveAs();
    return true;
  }

  public void openFile( File file )
    throws Exception
  {
    upgradePath = file.getParentFile();
    deviceUpgrade.reset( remotes, protocolManager );
    deviceUpgrade.load( file, remotes, protocolManager );
    setTitle( "RemoteMaster " + version + ": " + file.getName());
    description.setText( deviceUpgrade.getDescription());
    saveItem.setEnabled( true );
    remoteList.removeActionListener( this );
    deviceTypeList.removeActionListener( this );
    String savedTypeName = deviceUpgrade.getDeviceTypeAliasName();
    Remote r = deviceUpgrade.getRemote();
    setRemote( r );
    remoteList.setSelectedItem( r );
    if ( remoteList.getSelectedItem() != r )
    {
      remoteList.addItem( r );
      remoteList.setSelectedItem( r );
    }
    setDeviceTypeName( savedTypeName );
    remoteList.addActionListener( this );
    deviceTypeList.addActionListener( this );
    currPanel.update();

    int i = recentFileMenu.getItemCount() - 1;
    while ( i >= 0 )
    {
      JMenuItem item = recentFileMenu.getItem( i );
      FileAction action = ( FileAction  )item.getAction();
      File f = action.getFile();
      if ( f.getAbsolutePath().equals( file.getAbsolutePath()))
        recentFileMenu.remove( i );
      --i;
    }
    i = recentFileMenu.getItemCount();
    while ( i > 9 )
      recentFileMenu.remove( --i );
    recentFileMenu.add( new JMenuItem( new FileAction( file )), 0 );
    recentFileMenu.setEnabled( true );

    validateUpgrade();
  }

  public void importFile( File file )
    throws Exception
  {
    importPath = file.getParentFile();
    deviceUpgrade.reset( remotes, protocolManager );
    deviceUpgrade.importFile( file, remotes, protocolManager );
    setTitle( "RemoteMaster " + version );
    description.setText( deviceUpgrade.getDescription());
    remoteList.removeActionListener( this );
    deviceTypeList.removeActionListener( this );
    String savedTypeName = deviceUpgrade.getDeviceTypeAliasName();
    Remote r = deviceUpgrade.getRemote();
    setRemote( r );
    remoteList.setSelectedItem( r );
    if ( remoteList.getSelectedItem() != r )
    {
      remoteList.addItem( r );
      remoteList.setSelectedItem( r );
    }
    setDeviceTypeName( savedTypeName );
    remoteList.addActionListener( this );
    deviceTypeList.addActionListener( this );
    currPanel.update();

    validateUpgrade();
  }

  // ChangeListener methods
  private KMPanel currPanel = null;
  public void stateChanged( ChangeEvent e )
  {
    currPanel.commit();
    currPanel = ( KMPanel )(( JTabbedPane )e.getSource()).getSelectedComponent();
    currPanel.update();
    SwingUtilities.updateComponentTreeUI( currPanel );
    validateUpgrade();
  }

  public static File getHomeDirectory()
  {
    return homeDirectory;
  }

  private void loadPreferences()
    throws Exception
  {
    Properties props = new Properties();

    if ( propertiesFile.canRead())
    {
      FileInputStream in = new FileInputStream( propertiesFile );
      props.load( in );
      in.close();
    }

    String temp = props.getProperty( "RDFPath" );
    if ( temp != null )
      rdfPath = new File( temp );
    else
      rdfPath = new File( homeDirectory, "rdf" );
    while ( !rdfPath.exists())
      rdfPath = rdfPath.getParentFile();

    temp = props.getProperty( "UpgradePath" );
    if ( temp == null )
      temp = props.getProperty( "KMPath" );
    if ( temp != null )
      upgradePath = new File( temp );
    else
      upgradePath = new File( homeDirectory, upgradeDirectory );
    while ( ! upgradePath.exists())
      upgradePath = upgradePath.getParentFile();

    temp = props.getProperty( "ImportPath", upgradePath.getAbsolutePath());
    importPath = new File( temp );

    String defaultLookAndFeel = UIManager.getSystemLookAndFeelClassName();
    temp = props.getProperty( "LookAndFeel", defaultLookAndFeel );
    try
    {
      UIManager.setLookAndFeel( temp );
      SwingUtilities.updateComponentTreeUI( this );
      for ( int i = 0; i < lookAndFeelItems.length; i ++ )
      {
        if ( lookAndFeelItems[ i ].getActionCommand().equals( temp ))
        {
          lookAndFeelItems[ i ].setSelected( true );
          break;
        }
      }
    }
    catch ( Exception e )
    {
      System.err.println( "Exception thrown when setting look and feel to " + temp );
    }

    lastRemoteName = props.getProperty( "Remote.name" );
    lastRemoteSignature = props.getProperty( "Remote.signature" );

    temp = props.getProperty( "PromptToSave", promptStrings[ 0 ] );
    for ( int i = 0; i < promptStrings.length; i++ )
      if ( promptStrings[ i ].equals( temp ))
        promptFlag = i;
    if ( promptFlag > promptStrings.length )
      promptFlag = 0;

    promptButtons[ promptFlag ].setSelected( true );

    for ( int i = 0; true; i++ )
    {
      temp = props.getProperty( "PreferredRemotes." + i );
      if ( temp == null )
        break;
      System.err.println( "Preferred remote name " + temp );
      preferredRemoteNames.add( temp );
    }

    temp = props.getProperty( "ShowRemotes", "All" );
    if ( temp.equals( "All" ))
      useAllRemotes.setSelected( true );
    else
      usePreferredRemotes.setSelected( true );

    for ( int i = 0; i < 10; i++ )
    {
      temp = props.getProperty( "RecentFiles." + i );
      if ( temp == null )
        break;
      recentFileMenu.add( new FileAction( new File( temp )));
    }
    if ( recentFileMenu.getItemCount() > 0 )
      recentFileMenu.setEnabled( true );

    temp = props.getProperty( "Bounds" );
    if ( temp != null )
    {
      bounds = new Rectangle();
      StringTokenizer st = new StringTokenizer( temp, "," );
      bounds.x = Integer.parseInt( st.nextToken());
      bounds.y = Integer.parseInt( st.nextToken());
      bounds.width = Integer.parseInt( st.nextToken());
      bounds.height = Integer.parseInt( st.nextToken());
    }
  }

  private void savePreferences()
    throws Exception
  {
    Properties props = new Properties();
    props.setProperty( "RDFPath", rdfPath.getAbsolutePath());
    props.setProperty( "UpgradePath", upgradePath.getAbsolutePath());
    props.setProperty( "ImportPath", importPath.getAbsolutePath());
    props.setProperty( "LookAndFeel", UIManager.getLookAndFeel().getClass().getName());
    Remote remote = deviceUpgrade.getRemote();
    props.setProperty( "Remote.name", remote.getName());
    props.setProperty( "Remote.signature", remote.getSignature());
    props.setProperty( "PromptToSave", promptStrings[ promptFlag ]);
    if ( useAllRemotes.isSelected())
      props.setProperty( "ShowRemotes", "All" );
    else
      props.setProperty( "ShowRemotes", "Preferred" );

    for ( int i = 0; i < recentFileMenu.getItemCount(); i++ )
    {
      JMenuItem item = recentFileMenu.getItem( i );
      FileAction action = ( FileAction )item.getAction();
      File f = action.getFile();
      props.setProperty( "RecentFiles." + i, f.getAbsolutePath());
    }

    for ( int i = 0; i < preferredRemotes.length; i++ )
    {
      Remote r = preferredRemotes[ i ];
      props.setProperty( "PreferredRemotes." + i, r.getName());
    }

    int state = getExtendedState();
    if ( state != Frame.NORMAL )
      setExtendedState( Frame.NORMAL );
    Rectangle bounds = getBounds();
    props.setProperty( "Bounds", "" + bounds.x + ',' + bounds.y + ',' + bounds.width + ',' + bounds.height );

    FileOutputStream out = new FileOutputStream( propertiesFile );
    props.store( out, null );
    out.flush();
    out.close();
  }

  public void validateUpgrade()
  {
    Remote r = deviceUpgrade.getRemote();
    Protocol p = deviceUpgrade.getProtocol();
    Vector protocols = protocolManager.getProtocolsForRemote( r );
    if ( !protocols.contains( p ) && ( p.getCode( r ) == null ))
    {
      System.err.println( "KeyMapMaster.validateUpgrade(), protocol " + p.getDiagnosticName() +
                          "is not compatible with remote " + r.getName());

      // Find a matching protocol for this remote
      Protocol match = null;
      String name = p.getName();
      for ( Enumeration e = protocols.elements(); e.hasMoreElements(); )
      {
        Protocol p2 = ( Protocol )e.nextElement();
        if ( p2.getName().equals( name ))
        {
          match = p2;
          System.err.println( "\tFound one with the same name: " + p2.getDiagnosticName());
          break;
        }
      }

      if ( match != null )
      {
        System.err.println( "\tChecking for matching dev. parms" );
        DeviceParameter[] parms = p.getDeviceParameters();
        DeviceParameter[] parms2 = match.getDeviceParameters();

        int[] map = new int[ parms.length ];
        boolean parmsMatch = true;
        for ( int i = 0; i < parms.length; i++ )
        {
          name = parms[ i ].getName();
          System.err.print( "\tchecking " + name );
          boolean nameMatch = false;
          for ( int j = 0; j < parms2.length; j++ )
          {
            if ( name.equals( parms2[ j ].getName()))
            {
              map[ i ] = j;
              nameMatch = true;
              System.err.print( " has a match!" );
              break;
            }
          }
          System.err.println();
          parmsMatch = nameMatch;
          if ( !parmsMatch )
            break;
        }
        if ( parmsMatch )
        {
          // copy parameters from p to p2!
          System.err.println( "\tCopying dev. parms" );
          for ( int i = 0; i < map.length; i++ )
          {
            System.err.println( "\tfrom index " + i + " to index " + map[ i ]);
            parms2[ map[ i ]].setValue( parms[ i ].getValue());
          }
          System.err.println();
          System.err.println( "Setting new protocol" );
          p.convertFunctions( deviceUpgrade.getFunctions(), match );
          deviceUpgrade.setProtocol( match );
          return;
        }
      }
      JOptionPane.showMessageDialog( this,
                                     "The selected protocol " + p.getDiagnosticName() +
                                     "\nis not compatible with the selected remote.\n" +
                                     "This upgrade will NOT function correctly.\n" +
                                     "Please choose a different protocol.",
                                     "Error", JOptionPane.ERROR_MESSAGE );

    }
  }

  // DocumentListener methods
  public void changedUpdate( DocumentEvent e )
  {
    deviceUpgrade.setDescription( description.getText());
    currPanel.update();
  }

  public void insertUpdate( DocumentEvent e )
  {
    deviceUpgrade.setDescription( description.getText());
    currPanel.update();
  }

  public void removeUpdate( DocumentEvent e )
  {
    deviceUpgrade.setDescription( description.getText());
    currPanel.update();
  }

  private class KMFileFilter
    extends javax.swing.filechooser.FileFilter
  {
    //Accept all directories and all .km/.rmdu files.
    public boolean accept( File f )
    {
      boolean rc = false;
      if ( f.isDirectory())
        rc = true;
      else
      {
        String lowerName = f.getName().toLowerCase();
        if ( lowerName.endsWith( ".km" ) || lowerName.endsWith( upgradeExtension ))
          rc = true;
      }
      return rc;
    }

    //The description of this filter
    public String getDescription()
    {
      return "RemoteMaster device upgrade files";
    }
  }

  private class KMDirectoryFilter
    extends javax.swing.filechooser.FileFilter
  {
    //Accept all directories
    public boolean accept( File f )
    {
      boolean rc = false;
      if ( f.isDirectory())
        rc = true;
      return rc;
    }

    //The description of this filter
    public String getDescription()
    {
      return "Directories";
    }
  }

  private class FileAction
    extends AbstractAction
  {
    private File file = null;
    public FileAction( File file )
    {
      super( file.getAbsolutePath());
      this.file = file;
    }

    public void actionPerformed( ActionEvent e )
    {
      try
      {
        if ( promptToSaveUpgrade( ACTION_LOAD ))
          openFile( file );
      }
      catch ( Exception ex )
      {
        ex.printStackTrace( System.err );
      }
    }

    public File getFile()
    {
      return file;
    }
  }

  private class TextFileFilter
    extends javax.swing.filechooser.FileFilter
  {
    //Accept all directories and all .km/.rmdu files.
    public boolean accept( File f )
    {
      boolean rc = false;
      if ( f.isDirectory())
        rc = true;
      else
      {
        String lowerName = f.getName().toLowerCase();
        if ( lowerName.endsWith( ".txt" ))
          rc = true;
      }
      return rc;
    }

    //The description of this filter
    public String getDescription()
    {
      return "KeyMapMaster device upgrade files";
    }
  }
}
