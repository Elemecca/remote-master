package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

// TODO: Auto-generated Javadoc
/**
 * The Class KeyMapMaster.
 */
public class KeyMapMaster extends JP1Frame implements ActionListener, PropertyChangeListener
{

  /** The me. */
  private static KeyMapMaster me = null;

  /** The preferences. */
  private Preferences preferences = null;

  /** The editor panel. */
  private DeviceEditorPanel editorPanel = null;

  /** The new item. */
  private JMenuItem newItem = null;

  /** The open item. */
  private JMenuItem openItem = null;

  /** The save item. */
  protected JMenuItem saveItem = null;

  /** The save as item. */
  protected JMenuItem saveAsItem = null;
  // private JMenuItem importItem = null;
  /** The import from clipboard item. */
  private JMenuItem importFromClipboardItem = null;

  /** The recent file menu. */
  private JMenu recentFileMenu = null;

  /** The exit item. */
  private JMenuItem exitItem = null;

  private JCheckBoxMenuItem enablePreserveSelection = null;

  private JMenuItem editManualItem = null;

  private JMenuItem newManualItem = null;

  // private JMenuItem editorItem = null;
  /** The raw item. */
  private JMenuItem rawItem = null;

  /** The binary item. */
  private JMenuItem binaryItem = null;

  /** The write binary item. */
  private JMenuItem writeBinaryItem = null;

  /** The update item. */
  private JMenuItem updateItem = null;

  /** For help */
  private Desktop desktop = null;

  private JMenuItem readmeItem = null;

  private JMenuItem tutorialItem = null;

  private JMenuItem homePageItem = null;

  private JMenuItem forumItem = null;

  private JMenuItem wikiItem = null;

  private JMenuItem aboutItem = null;

  /** The ok button. */
  private JButton okButton = null;

  /** The cancel button. */
  private JButton cancelButton = null;

  /** The message label. */
  private JLabel messageLabel = null;

  /** The protocol manager. */
  private ProtocolManager protocolManager = ProtocolManager.getProtocolManager();

  /** The device upgrade. */
  private DeviceUpgrade deviceUpgrade = null;

  /** The home directory. */
  private static File homeDirectory = null;

  /** The upgrade extension. */
  private static String upgradeExtension = ".rmdu";

  private List< AssemblerItem > clipBoardItems = new ArrayList< AssemblerItem >();

  /** The Constant ACTION_EXIT. */
  public final static int ACTION_EXIT = 1;

  /** The Constant ACTION_LOAD. */
  public final static int ACTION_LOAD = 2;

  /**
   * Instantiates a new key map master.
   * 
   * @param prefs
   *          the prefs
   */
  public KeyMapMaster( PropertyFile prefs )
  {
    super( "RemoteMaster", prefs );
    System.err.println( "KeyMapMaster opening" );
    me = this;

    setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );

    preferences = new Preferences( prefs );
    homeDirectory = prefs.getFile().getParentFile();
    
    ProtocolManager.getProtocolManager().loadAltPIDRemoteProperties( properties );

    addWindowListener( new WindowAdapter()
    {
      @Override
      public void windowClosing( WindowEvent event )
      {
        boolean doDispose = true;
        try
        {
          System.err.println( "KeyMapMaster.windowClosing() entered" );
          if ( !promptToSaveUpgrade( ACTION_EXIT ) )
          {
            doDispose = false;
            return;
          }
          System.err.println( "KeyMapMaster.windowClosing() continuing" );
          ProtocolManager.getProtocolManager().setAltPIDRemoteProperties( properties );
          Remote remote = getRemote();
          if ( remote != null )
          {
            preferences.setLastRemoteName( remote.getName() );
            preferences.setLastRemoteSignature( remote.getSignature() );
          }
          properties.removePropertyChangeListener( "enablePreserveSelection", me );
          savePreferences();
          me = null;
        }
        catch ( Exception e )
        {
          System.err.println( "KeyMapMaster.windowClosing() caught an exception!" );
          e.printStackTrace( System.err );
        }
        finally
        {
          if ( doDispose )
          {
            dispose();
          }
        }
      }
    } );

    createMenus();

    preferences.load( recentFileMenu, this );

    deviceUpgrade = new DeviceUpgrade( getCustomNames() );
    Remote r = null;

    String name = preferences.getLastRemoteName();
    RemoteManager rm = RemoteManager.getRemoteManager();
    if ( name != null )
    {
      r = rm.findRemoteByName( name );
    }
    if ( r == null )
    {
      r = getRemotes().iterator().next();
    }
    Protocol protocol = protocolManager.getProtocolsForRemote( r ).get( 0 );
    deviceUpgrade.setProtocol( protocol );
    deviceUpgrade.setRemote( r );
    deviceUpgrade.setBaseline();

    editorPanel = new DeviceEditorPanel( this, deviceUpgrade, getRemotes() );
    add( editorPanel, BorderLayout.CENTER );
    editorPanel.addPropertyChangeListener( this, "remote" );
    editorPanel.setShowRemoteSignature( preferences.getShowRemoteSignature() );
    messageLabel = new JLabel( " " );
    messageLabel.setForeground( Color.RED );
    add( messageLabel, BorderLayout.SOUTH );

    fontSizeAdjustment = preferences.getFontSizeAdjustment();
    adjustFontSize( fontSizeAdjustment );

    pack();
    editorPanel.setAltPIDReason();
    Rectangle bounds = preferences.getBounds();
    if ( bounds != null )
    {
      setBounds( bounds );
    }
    setVisible( true );
  }

  /**
   * Creates the menus.
   */
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

    // importItem = new JMenuItem( "Import KM file..." );
    // importItem.setMnemonic( KeyEvent.VK_K );
    // importItem.addActionListener( this );
    // menu.add( importItem );
    //
    importFromClipboardItem = new JMenuItem( "Import from Clipboard" );
    importFromClipboardItem.setMnemonic( KeyEvent.VK_C );
    importFromClipboardItem.addActionListener( this );
    menu.add( importFromClipboardItem );

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

    ActionListener al = new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        try
        {
          JRadioButtonMenuItem item = ( JRadioButtonMenuItem )e.getSource();
          String lf = item.getActionCommand();
          UIManager.setLookAndFeel( lf );
          preferences.setLookAndFeel( lf );
          SwingUtilities.updateComponentTreeUI( me );
          preferences.setLookAndFeel( lf );
        }
        catch ( Exception x )
        {
          x.printStackTrace( System.err );
        }
      }
    };

    ButtonGroup group = new ButtonGroup();
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
      submenu.add( item );
      if ( item.getActionCommand().equals( lookAndFeel ) )
      {
        item.setSelected( true );
      }
      item.addActionListener( al );
    }

    submenu = new JMenu( "Font size" );
    submenu.setMnemonic( KeyEvent.VK_F );
    menu.add( submenu );

    al = new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        JMenuItem button = ( JMenuItem )e.getSource();
        float adjustment = Float.parseFloat( button.getActionCommand() );
        adjustFontSize( adjustment );
        fontSizeAdjustment += adjustment;
      }
    };

    JMenuItem menuItem = new JMenuItem( "Increase" );
    menuItem.setMnemonic( KeyEvent.VK_I );
    submenu.add( menuItem );
    menuItem.addActionListener( al );
    menuItem.setActionCommand( "1f" );

    menuItem = new JMenuItem( "Decrease" );
    menuItem.setMnemonic( KeyEvent.VK_I );
    submenu.add( menuItem );
    menuItem.addActionListener( al );
    menuItem.setActionCommand( "-1f" );

    group = new ButtonGroup();
    submenu = new JMenu( "Prompt to Save" );
    submenu.setMnemonic( KeyEvent.VK_P );
    menu.add( submenu );

    al = new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        Object source = e.getSource();
        for ( int i = 0; i < promptButtons.length; i++ )
        {
          if ( promptButtons[ i ] == source )
          {
            preferences.setPromptToSave( promptStrings[ i ] );
            break;
          }
        }
      }
    };

    String promptText = preferences.getPromptToSave();
    promptButtons = new JRadioButtonMenuItem[ promptStrings.length ];
    for ( int i = 0; i < promptStrings.length; i++ )
    {
      JRadioButtonMenuItem item = new JRadioButtonMenuItem( promptStrings[ i ] );
      item.setMnemonic( promptMnemonics[ i ] );
      promptButtons[ i ] = item;
      if ( promptStrings[ i ].equals( promptText ) )
      {
        item.setSelected( true );
      }
      item.addActionListener( al );
      group.add( item );
      submenu.add( item );
    }

    submenu = new JMenu( "Remotes" );
    submenu.setMnemonic( KeyEvent.VK_R );
    menu.add( submenu );

    group = new ButtonGroup();
    useAllRemotes = new JRadioButtonMenuItem( "All" );
    useAllRemotes.setMnemonic( KeyEvent.VK_A );
    al = new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        Object source = e.getSource();
        if ( source == useAllRemotes )
        {
          editorPanel.setRemotes( RemoteManager.getRemoteManager().getRemotes() );
          preferences.setShowRemotes( "All" );
        }
        else if ( source == usePreferredRemotes )
        {
          editorPanel.setRemotes( preferences.getPreferredRemotes() );
          preferences.setShowRemotes( "Preferred" );
        }
        else
        {
          editPreferredRemotes();
        }
      }
    };
    useAllRemotes.setSelected( true );
    group.add( useAllRemotes );
    submenu.add( useAllRemotes );

    usePreferredRemotes = new JRadioButtonMenuItem( "Preferred" );
    usePreferredRemotes.setMnemonic( KeyEvent.VK_P );
    group.add( usePreferredRemotes );
    submenu.add( usePreferredRemotes );

    String temp = preferences.getShowRemotes();
    if ( temp.equals( "All" ) )
    {
      useAllRemotes.setSelected( true );
    }
    else
    {
      usePreferredRemotes.setSelected( true );
    }

    if ( preferences.getPreferredRemotes().size() == 0 )
    {
      useAllRemotes.setSelected( true );
      usePreferredRemotes.setEnabled( false );
    }
    useAllRemotes.addActionListener( al );
    usePreferredRemotes.addActionListener( al );

    submenu.addSeparator();
    JMenuItem item = new JMenuItem( "Edit preferred..." );
    item.setMnemonic( KeyEvent.VK_E );
    item.addActionListener( al );
    submenu.add( item );

    submenu = new JMenu( "Function names" );
    submenu.setMnemonic( KeyEvent.VK_F );
    menu.add( submenu );

    group = new ButtonGroup();
    useDefaultNames = new JRadioButtonMenuItem( "Default" );
    useDefaultNames.setMnemonic( KeyEvent.VK_D );
    al = new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        Object source = e.getSource();
        if ( source == useDefaultNames )
        {
          preferences.setUseCustomNames( false );
        }
        else if ( source == useCustomNames )
        {
          preferences.setUseCustomNames( true );
        }
        else
        {
          editCustomNames();
        }
      }
    };
    useDefaultNames.setSelected( true );
    useDefaultNames.addActionListener( al );
    group.add( useDefaultNames );
    submenu.add( useDefaultNames );

    useCustomNames = new JRadioButtonMenuItem( "Custom" );
    useCustomNames.setMnemonic( KeyEvent.VK_C );
    group.add( useCustomNames );
    useCustomNames.setSelected( preferences.getUseCustomNames() );

    useCustomNames.addActionListener( al );
    submenu.add( useCustomNames );

    submenu.addSeparator();
    item = new JMenuItem( "Edit custom names..." );
    item.setMnemonic( KeyEvent.VK_E );
    item.addActionListener( al );
    submenu.add( item );

    al = new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        JCheckBoxMenuItem checkItem = ( JCheckBoxMenuItem )e.getSource();
        boolean state = checkItem.getState();
        preferences.setShowRemoteSignature( state );
        editorPanel.setShowRemoteSignature( state );
      }
    };

    JCheckBoxMenuItem checkItem = new JCheckBoxMenuItem( "Show remote signature" );
    checkItem.setMnemonic( KeyEvent.VK_S );
    checkItem.setState( preferences.getShowRemoteSignature() );
    checkItem.addActionListener( al );
    menu.add( checkItem );

    submenu = new JMenu( "Folders" );
    menu.add( submenu );

    al = new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        String name = e.getActionCommand();
        String extension = ( name == "RDF" ) ? ".rdf" : ".map";
        File path = properties.getFileProperty( name + "Path" );
        RMDirectoryChooser chooser = new RMDirectoryChooser( path, extension, name );
        chooser.setAccessory( new ChoiceArea( chooser ) );
        chooser.setDialogTitle( "Choose the directory containing the " + name + "s" );
        int returnVal = chooser.showDialog( me, "OK" );
        if ( returnVal == RMDirectoryChooser.APPROVE_OPTION )
        {
          File newPath = chooser.getSelectedFile();
          properties.setProperty( name + "Path", newPath );
          RemoteManager mgr = RemoteManager.getRemoteManager();
          mgr.reset();
          mgr.loadRemotes( properties );
          if ( useAllRemotes.isSelected() )
          {
            editorPanel.setRemotes( RemoteManager.getRemoteManager().getRemotes() );
          }
        }
      }
    };

    menuItem = new JMenuItem( "RDF Folder..." );
    menuItem.setMnemonic( KeyEvent.VK_R );
    menuItem.setActionCommand( "RDF" );
    menuItem.addActionListener( al );

    submenu.add( menuItem );

    menuItem = new JMenuItem( "Map Folder..." );
    menuItem.setMnemonic( KeyEvent.VK_R );
    menuItem.setActionCommand( "Image" );
    menuItem.addActionListener( al );

    submenu.add( menuItem );

    enablePreserveSelection = new JCheckBoxMenuItem( "Allow Preserve Control" );
    enablePreserveSelection.setMnemonic( KeyEvent.VK_A );
    enablePreserveSelection.setSelected( Boolean.parseBoolean( properties.getProperty( "enablePreserveSelection",
        "false" ) ) );
    enablePreserveSelection.addActionListener( this );
    enablePreserveSelection
        .setToolTipText( "<html>Allow control of which function data is preserved when changing the protocol used in a device upgrade.<br>Do not use this unless you know what you are doing and why.</html>" );
    menu.add( enablePreserveSelection );

    menu = new JMenu( "Advanced" );
    menu.setMnemonic( KeyEvent.VK_A );
    menuBar.add( menu );

    editManualItem = new JMenuItem( "Edit Protocol..." );
    editManualItem.setMnemonic( KeyEvent.VK_E );
    editManualItem.addActionListener( this );
    menu.add( editManualItem );

    newManualItem = new JMenuItem( "New Manual Protocol..." );
    newManualItem.setMnemonic( KeyEvent.VK_M );
    newManualItem.addActionListener( this );
    menu.add( newManualItem );

    menu.addSeparator();
    /*
     * editorItem = new JMenuItem( "Protocol Editor..." ); editorItem.setMnemonic( KeyEvent.VK_P );
     * editorItem.addActionListener( this ); menu.add( editorItem );
     */
    rawItem = new JMenuItem( "Import Raw Upgrade..." );
    rawItem.setMnemonic( KeyEvent.VK_R );
    rawItem.addActionListener( this );
    menu.add( rawItem );

    binaryItem = new JMenuItem( "Import Binary Upgrade..." );
    binaryItem.setMnemonic( KeyEvent.VK_B );
    binaryItem.addActionListener( this );
    menu.add( binaryItem );

    writeBinaryItem = new JMenuItem( "Export Binary Upgrade..." );
    writeBinaryItem.setEnabled( false );
    writeBinaryItem.setMnemonic( KeyEvent.VK_X );
    writeBinaryItem.addActionListener( this );
    menu.add( writeBinaryItem );

    menu = new JMenu( "Help" );
    menu.setMnemonic( KeyEvent.VK_H );
    menuBar.add( menu );

    if ( Desktop.isDesktopSupported() )
    {
      desktop = Desktop.getDesktop();

      readmeItem = new JMenuItem( "Readme", KeyEvent.VK_R );
      readmeItem.addActionListener( this );
      menu.add( readmeItem );

      tutorialItem = new JMenuItem( "Tutorial", KeyEvent.VK_T );
      tutorialItem.addActionListener( this );
      menu.add( tutorialItem );

      menu.addSeparator();

      homePageItem = new JMenuItem( "Home Page", KeyEvent.VK_H );
      homePageItem.addActionListener( this );
      menu.add( homePageItem );

      forumItem = new JMenuItem( "Forums", KeyEvent.VK_F );
      forumItem.addActionListener( this );
      menu.add( forumItem );

      wikiItem = new JMenuItem( "Wiki", KeyEvent.VK_W );
      wikiItem.addActionListener( this );
      menu.add( wikiItem );

      menu.addSeparator();
    }

    updateItem = new JMenuItem( "Check for updates", KeyEvent.VK_C );
    updateItem.addActionListener( this );
    menu.add( updateItem );

    aboutItem = new JMenuItem( "About..." );
    aboutItem.setMnemonic( KeyEvent.VK_A );
    aboutItem.addActionListener( this );
    menu.add( aboutItem );
  }

  /**
   * Save preferences.
   * 
   * @throws Exception
   *           the exception
   */
  private void savePreferences() throws Exception
  {
    int state = getExtendedState();
    if ( state != Frame.NORMAL )
    {
      setExtendedState( Frame.NORMAL );
    }
    preferences.setBounds( getBounds() );
    preferences.save( recentFileMenu );
  }

  /**
   * Edits the manual settings.
   */
  private void editManualSettings()
  {
    Protocol p = deviceUpgrade.getProtocol();
    Protocol edited = p.editProtocol( getRemote(), this );
    if ( edited == null )
    {
      return;
    }
    if ( p instanceof ManualProtocol && p != edited )
    {
      deviceUpgrade.setProtocol( edited );
    }
  }

  private void newManualSettings()
  {
    ManualProtocol mp = new ManualProtocol( null, null );
    ManualSettingsDialog d = new ManualSettingsDialog( this, mp );
    Remote remote = getRemote();
    if ( remote != null )
    {
      d.setSelectedCode( remote.getProcessor() );
      d.setMessage( 0 );
    }
    d.setVisible( true );
    mp = d.getProtocol();
    if ( mp != null )
    {
      ProtocolManager.getProtocolManager().add( mp );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1Frame#showMessage(java.lang.String)
   */
  @Override
  public void showMessage( String message )
  {
    messageLabel.setText( message );
    Toolkit.getDefaultToolkit().beep();
  }

  /**
   * Show message.
   * 
   * @param message
   *          the message
   * @param c
   *          the c
   */
  public static void showMessage( String message, Component c )
  {
    KeyMapMaster km = ( KeyMapMaster )SwingUtilities.getAncestorOfClass( KeyMapMaster.class, c );
    if ( km != null )
    {
      km.showMessage( message );
      return;
    }

    JP1Frame frame = ( JP1Frame )SwingUtilities.getAncestorOfClass( JP1Frame.class, c );
    if ( frame != null )
    {
      frame.showMessage( message );
      return;
    }
    JOptionPane.showMessageDialog( c, message );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1Frame#clearMessage()
   */
  @Override
  public void clearMessage()
  {
    messageLabel.setText( " " );
  }

  /**
   * Clear message.
   * 
   * @param c
   *          the c
   */
  public static void clearMessage( Component c )
  {
    KeyMapMaster km = ( KeyMapMaster )SwingUtilities.getAncestorOfClass( KeyMapMaster.class, c );
    if ( km != null )
    {
      km.clearMessage();
      return;
    }

    JP1Frame frame = ( JP1Frame )SwingUtilities.getAncestorOfClass( JP1Frame.class, c );
    if ( frame != null )
    {
      frame.clearMessage();
      return;
    }
  }

  // ActionListener Methods
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed( ActionEvent e )
  {
    try
    {
      editorPanel.commit();

      Object source = e.getSource();

      if ( source == newItem )
      {
        if ( !promptToSaveUpgrade( ACTION_LOAD ) )
        {
          return;
        }
        ProtocolManager.getProtocolManager().reset();
        deviceUpgrade.reset( getCustomNames() );
      }
      else if ( source == saveItem )
      {
        deviceUpgrade.store();
      }
      else if ( source == saveAsItem )
      {
        saveAs();
      }
      else if ( source == openItem )
      {
        if ( !promptToSaveUpgrade( ACTION_LOAD ) )
        {
          return;
        }
        File file = getUpgradeFile( preferences.getUpgradePath() );
        loadUpgrade( file );
      }
      else if ( source == importFromClipboardItem )
      {
        if ( !promptToSaveUpgrade( ACTION_LOAD ) )
        {
          return;
        }
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable clipData = clipboard.getContents( clipboard );
        if ( clipData != null )
        {
          try
          {
            if ( clipData.isDataFlavorSupported( DataFlavor.stringFlavor ) )
            {
              String s = ( String )clipData.getTransferData( DataFlavor.stringFlavor );
              BufferedReader in = new BufferedReader( new StringReader( s ) );
              loadUpgrade( in );
            }
          }
          catch ( Exception ex )
          {
            ex.printStackTrace( System.err );
          }
        }
      }
      else if ( source == exitItem )
      {
        dispatchEvent( new WindowEvent( this, WindowEvent.WINDOW_CLOSING ) );
      }
      else if ( source == editManualItem )
      {
        editManualSettings();
      }
      else if ( source == newManualItem )
      {
        newManualSettings();
      }
      /*
       * else if ( source == editorItem ) { ProtocolEditor d = new ProtocolEditor( this ); d.setVisible( true ); }
       */
      else if ( source == rawItem )
      {
        ImportRawUpgradeDialog d = new ImportRawUpgradeDialog( this, deviceUpgrade, false );
        d.setVisible( true );
      }
      else if ( source == binaryItem )
      {
        File file = null;
        RMFileChooser chooser = new RMFileChooser( preferences.getBinaryUpgradePath() );
        try
        {
          chooser.setAcceptAllFileFilterUsed( false );
        }
        catch ( Exception ex )
        {
          ex.printStackTrace( System.err );
        }
        chooser.setFileFilter( new EndingFileFilter( "Binary upgrade files", binaryEndings ) );
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
          else
          {
            preferences.setBinaryUpgradePath( file.getParentFile() );
            BinaryUpgradeReader reader = new BinaryUpgradeReader( file );
            Remote r = reader.getRemote();
            DeviceType devType = r.getDeviceTypeByIndex( reader.getDeviceIndex() );
            String aliasName = null;
            String[] aliasNames = r.getDeviceTypeAliasNames();
            boolean nameMatch = false;
            for ( int i = 0; i < aliasNames.length && !nameMatch; i++ )
            {
              String tryit = aliasNames[ i ];
              if ( devType == r.getDeviceTypeByAliasName( tryit ) )
              {
                nameMatch = devType.getName().equalsIgnoreCase( tryit );
                if ( aliasName == null || nameMatch )
                {
                  aliasName = tryit;
                }
              }
            }
            deviceUpgrade.importRawUpgrade( reader.getCode(), r, aliasName, reader.getPid(), reader.getProtocolCode() );
            deviceUpgrade.setSetupCode( reader.getSetupCode() );
          }
        }
      }
      else if ( source == enablePreserveSelection )
      {
        properties.setProperty( "enablePreserveSelection", Boolean.toString( enablePreserveSelection.isSelected() ) );
      }
      else if ( source == writeBinaryItem )
      {
        File path = BinaryUpgradeWriter.write( deviceUpgrade, preferences.getBinaryUpgradePath() );
        if ( path != null )
        {
          preferences.setBinaryUpgradePath( path );
        }
      }
      else if ( source == updateItem )
      {
        UpdateChecker.checkUpdateAvailable( this );
      }
      else if ( source == readmeItem )
      {
        File readme = new File( "Readme.html" );
        desktop.browse( readme.toURI() );
      }
      else if ( source == tutorialItem )
      {
        URL url = new URL(
            "http://www.hifi-remote.com/wiki/index.php?title=JP1_-_Just_How_Easy_Is_It%3F_-_RM-IR_Version" );
        desktop.browse( url.toURI() );
      }
      else if ( source == homePageItem )
      {
        URL url = new URL( "http://controlremote.sourceforge.net/" );
        desktop.browse( url.toURI() );
      }
      else if ( source == forumItem )
      {
        URL url = new URL( "http://www.hifi-remote.com/forums/" );
        desktop.browse( url.toURI() );
      }
      else if ( source == wikiItem )
      {
        URL url = new URL( "http://www.hifi-remote.com/wiki/index.php?title=Main_Page" );
        desktop.browse( url.toURI() );
      }
      else if ( source == aboutItem )
      {
        String text = "<html><b>RemoteMaster Device Upgrade Editor, "
            + RemoteMaster.version
            + "</b>"
            + "<p>Get the latest version at <a href=\"http://controlremote.sourceforge.net\">http://controlremote.sourceforge.net</a></p>"
            + "<p>Java version "
            + System.getProperty( "java.version" )
            + " from "
            + System.getProperty( "java.vendor" )
            + "</p>"
            + "<p>Home directory is <b>"
            + homeDirectory
            + "</b></p>"
            + "<p>RDFs loaded from <b>"
            + preferences.getRDFPath()
            + "</b></p>"
            + "<p>Written primarily by <i>Greg Bush</i> (and now accepting donations "
            + "at <a href=\"http://sourceforge.net/donate/index.php?user_id=735638\">http://sourceforge.net/donate/index.php?user_id=735638</a>), "
            + "<br>with substantial additions and help from Graham&nbsp;Dixon</p>"
            + "<p>Other contributors include:<blockquote>"
            + "John&nbsp;S&nbsp;Fine, Nils&nbsp;Ekberg, Jon&nbsp;Armstrong, Robert&nbsp;Crowe, "
            + "Mark&nbsp;Pauker, Mark&nbsp;Pierson, Mike&nbsp;England</blockquote></html>";

        JEditorPane pane = new JEditorPane( "text/html", text );
        pane.addHyperlinkListener( this );
        pane.setEditable( false );
        pane.setBackground( getContentPane().getBackground() );
        new TextPopupMenu( pane );
        JScrollPane scroll = new JScrollPane( pane );
        Dimension d = pane.getPreferredSize();
        d.height = d.height * 5 / 4;
        d.width = d.width * 2 / 3;
        scroll.setPreferredSize( d );

        JOptionPane.showMessageDialog( this, scroll, "About RemoteMaster", JOptionPane.INFORMATION_MESSAGE );
      }
      else if ( source == okButton )
      {
        setVisible( false );
      }
      else if ( source == cancelButton )
      {
        deviceUpgrade = null;
        setVisible( false );
      }
      else
      // must be a recent file
      {
        JMenuItem item = ( JMenuItem )source;
        File f = new File( item.getText() );
        loadUpgrade( f );
      }
      refresh();
    }
    catch ( Exception ex )
    {
      ex.printStackTrace( System.err );
    }
  } // actionPerformed

  /**
   * Prompt for upgrade file.
   * 
   * @param path
   *          the path
   * @return the file
   */
  public static File promptForUpgradeFile( File path )
  {
    return me.getUpgradeFile( path );
  }

  /**
   * Gets the upgrade file.
   * 
   * @param path
   *          the path
   * @return the upgrade file
   */
  public File getUpgradeFile( File path )
  {
    if ( path == null )
    {
      path = preferences.getUpgradePath();
    }

    File file = null;
    RMFileChooser chooser = new RMFileChooser( path );
    try
    {
      chooser.setAcceptAllFileFilterUsed( false );
    }
    catch ( Exception e )
    {
      e.printStackTrace( System.err );
    }
    EndingFileFilter filter = new EndingFileFilter( "All device upgrade files", anyEndings );
    chooser.addChoosableFileFilter( filter );
    chooser.addChoosableFileFilter( new EndingFileFilter( "KeyMapMaster device upgrade files", kmEndings ) );
    chooser.addChoosableFileFilter( new EndingFileFilter( "RemoteMaster device upgrade files", rmEndings ) );
    chooser.setFileFilter( filter );

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
      else
      {
        preferences.setUpgradePath( file.getParentFile() );
      }
    }
    return file;
  }

  /**
   * Save as.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void saveAs() throws IOException
  {
    RMFileChooser chooser = new RMFileChooser( preferences.getUpgradePath() );
    chooser.setFileFilter( new EndingFileFilter( "RemoteMaster device upgrade files (*.rmdu)", rmEndings ) );
    File f = deviceUpgrade.getFile();
    if ( f == null )
    {
      String fname = deviceUpgrade.getDescription() + upgradeExtension;
      fname = fname.replace( '/', '-' );
      f = new File( preferences.getUpgradePath(), fname );
    }
    chooser.setSelectedFile( f );
    int returnVal = chooser.showSaveDialog( this );
    if ( returnVal == RMFileChooser.APPROVE_OPTION )
    {
      String name = chooser.getSelectedFile().getAbsolutePath();
      if ( !name.toLowerCase().endsWith( upgradeExtension ) )
      {
        name = name + upgradeExtension;
      }
      File file = new File( name );
      int rc = JOptionPane.YES_OPTION;
      if ( file.exists() )
      {
        rc = JOptionPane.showConfirmDialog( this, file.getName() + " already exists.  Do you want to replace it?",
            "Replace existing file?", JOptionPane.YES_NO_OPTION );
      }
      if ( rc == JOptionPane.YES_OPTION )
      {
        deviceUpgrade.store( file );
        updateRecentFiles( file );
        saveItem.setEnabled( true );
        setTitle( file.getCanonicalPath() + " - RemoteMaster" );
      }
    }
  }

  /**
   * Prompt to save upgrade.
   * 
   * @param action
   *          the action
   * @return true, if successful
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean promptToSaveUpgrade( int action ) throws IOException
  {
    if ( !deviceUpgrade.hasChanged() )
    {
      return true;
    }

    String promptFlag = preferences.getPromptToSave();
    if ( promptFlag.equals( promptStrings[ PROMPT_NEVER ] ) )
    {
      return true;
    }
    else if ( !promptFlag.equals( promptStrings[ PROMPT_ALWAYS ] ) )
    {
      if ( action != ACTION_EXIT )
      {
        return true;
      }
    }

    int rc = JOptionPane
        .showConfirmDialog( this,
            // "All changes made to the current upgrade will be lost if you proceed.\n\n" +
            "Do you want to save the current upgrade before proceeding?", "Save upgrade?",
            JOptionPane.YES_NO_CANCEL_OPTION );
    if ( rc == JOptionPane.CANCEL_OPTION || rc == JOptionPane.CLOSED_OPTION )
    {
      return false;
    }
    if ( rc == JOptionPane.NO_OPTION )
    {
      return true;
    }

    if ( deviceUpgrade.getFile() != null )
    {
      deviceUpgrade.store();
    }
    else
    {
      saveAs();
    }
    return true;
  }

  /**
   * Load upgrade.
   * 
   * @param file
   *          the file
   * @throws Exception
   *           the exception
   */
  public void loadUpgrade( File file ) throws Exception
  {
    if ( file == null || !file.exists() )
    {
      return;
    }

    System.err.println( "Opening " + file.getCanonicalPath() + ", last modified "
        + DateFormat.getInstance().format( new Date( file.lastModified() ) ) );

    deviceUpgrade.reset();
    deviceUpgrade.load( file );

    boolean isRMDU = file.getName().toLowerCase().endsWith( ".rmdu" );

    if ( isRMDU )
    {
      updateRecentFiles( file );
    }
    preferences.setUpgradePath( file.getParentFile() );
    refresh();
  }

  /**
   * Update recent files.
   * 
   * @param file
   *          the file
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private void updateRecentFiles( File file ) throws IOException
  {
    boolean isRMDU = file.getName().toLowerCase().endsWith( ".rmdu" );

    if ( isRMDU )
    {
      int i = recentFileMenu.getItemCount() - 1;
      while ( i >= 0 )
      {
        JMenuItem item = recentFileMenu.getItem( i );
        File f = new File( item.getText() );
        if ( f.getCanonicalPath().equals( file.getCanonicalPath() ) )
        {
          recentFileMenu.remove( i );
        }
        --i;
      }
      i = recentFileMenu.getItemCount();
      while ( i > 9 )
      {
        recentFileMenu.remove( --i );
      }

      JMenuItem item = new JMenuItem( file.getAbsolutePath() );
      item.addActionListener( this );
      recentFileMenu.add( item, 0 );

      recentFileMenu.setEnabled( true );
    }
  }

  /**
   * Load upgrade.
   * 
   * @param reader
   *          the reader
   * @throws Exception
   *           the exception
   */
  public void loadUpgrade( BufferedReader reader ) throws Exception
  {
    deviceUpgrade.reset();
    deviceUpgrade.load( reader );
    refresh();
  }

  /**
   * Refresh.
   */
  private void refresh()
  {
    File file = deviceUpgrade.getFile();
    saveItem.setEnabled( file != null );
    writeBinaryItem.setEnabled( deviceUpgrade.getRemote().getSupportsBinaryUpgrades() );

    validateUpgrade();
    editorPanel.refresh();
  }

  /**
   * Import upgrade.
   * 
   * @param in
   *          the in
   * @throws Exception
   *           the exception
   */
  public void importUpgrade( BufferedReader in ) throws Exception
  {
    deviceUpgrade.reset();
    deviceUpgrade.importUpgrade( in );
    refresh();
  }

  /**
   * Gets the home directory.
   * 
   * @return the home directory
   */
  public static File getHomeDirectory()
  {
    if ( homeDirectory == null )
    {
      String temp = System.getProperty( "user.dir" );
      if ( temp != null )
      {
        homeDirectory = new File( temp );
      }
    }
    return homeDirectory;
  }

  public void validateUpgrade()
  {
    // Call from the editor panel to avoid duplication of error messages
    editorPanel.validateUpgrade();
  }

  /**
   * Validate upgrade.
   */
  // public void validateUpgrade()
  // {
  // Remote r = deviceUpgrade.getRemote();
  // Protocol p = deviceUpgrade.getProtocol();
  //
  // if ( oldRemote == r && oldProtocol == p ) return;
  // oldRemote = r;
  // oldProtocol = p;
  //
  // java.util.List< Protocol > protocols = protocolManager.getProtocolsForRemote( r );
  // if ( !protocols.contains( p ) && !p.hasCode( r ) )
  // {
  // System.err.println( "KeyMapMaster.validateUpgrade(), protocol " + p.getDiagnosticName()
  // + "is not compatible with remote " + r.getName() );
  //
  // // Find a matching protocol for this remote
  // Protocol match = null;
  // String name = p.getName();
  // for ( Protocol p2 : protocols )
  // {
  // if ( p2.getName().equals( name ) )
  // {
  // match = p2;
  // System.err.println( "\tFound one with the same name: " + p2.getDiagnosticName() );
  // break;
  // }
  // }
  // if ( match != null )
  // {
  // deviceUpgrade.setProtocol( match );
  // }
  // else
  // {
  // JOptionPane.showMessageDialog( this, "The selected protocol " + p.getDiagnosticName()
  // + "\nis not compatible with the selected remote.\n" + "This upgrade will NOT function correctly.\n"
  // + "Please choose a different protocol.", "Error", JOptionPane.ERROR_MESSAGE );
  // }
  // }
  // }

  /**
   * Gets the remote.
   * 
   * @return the remote
   */
  public static Remote getRemote()
  {
    return me.deviceUpgrade.getRemote();
  }

  /**
   * Gets the remotes.
   * 
   * @return the remotes
   */
  public Collection< Remote > getRemotes()
  {
    if ( preferences.getShowRemotes().equals( "Preferred" ) )
    {
      return preferences.getPreferredRemotes();
    }
    else
    {
      return RemoteManager.getRemoteManager().getRemotes();
    }
  }

  /**
   * Gets the preferences.
   * 
   * @return the preferences
   */
  public Preferences getPreferences()
  {
    return preferences;
  }

  /**
   * Gets the device upgrade.
   * 
   * @return the device upgrade
   */
  public DeviceUpgrade getDeviceUpgrade()
  {
    return deviceUpgrade;
  }

  /**
   * Edits the preferred remotes.
   */
  private void editPreferredRemotes()
  {
    PreferredRemoteDialog d = new PreferredRemoteDialog( this, preferences.getPreferredRemotes() );
    d.setVisible( true );
    if ( d.getUserAction() == JOptionPane.OK_OPTION )
    {
      Collection< Remote > preferredRemotes = d.getPreferredRemotes();
      preferences.setPreferredRemotes( preferredRemotes );
      if ( preferredRemotes.size() == 0 )
      {
        usePreferredRemotes.setEnabled( false );
        if ( !useAllRemotes.isSelected() )
        {
          useAllRemotes.setSelected( true );

          editorPanel.setRemotes( RemoteManager.getRemoteManager().getRemotes() );
        }
      }
      else
      {
        usePreferredRemotes.setEnabled( true );
      }

      if ( usePreferredRemotes.isSelected() )
      {
        editorPanel.setRemotes( preferredRemotes );
      }
    }
  }

  /**
   * Gets the custom names.
   * 
   * @return the custom names
   */
  public String[] getCustomNames()
  {
    if ( useCustomNames.isSelected() )
    {
      return preferences.getCustomNames();
    }
    else
    {
      return null;
    }
  }

  /**
   * Edits the custom names.
   */
  private void editCustomNames()
  {
    CustomNameDialog d = new CustomNameDialog( this, preferences.getCustomNames() );
    d.setVisible( true );
    if ( d.getUserAction() == JOptionPane.OK_OPTION )
    {
      String[] customNames = d.getCustomNames();
      if ( customNames == null || customNames.length == 0 )
      {
        useCustomNames.setEnabled( false );
        useDefaultNames.setSelected( true );
      }
      else
      {
        useCustomNames.setEnabled( true );
        preferences.setCustomNames( customNames );
      }
    }
  }

  /**
   * Adjust font size.
   * 
   * @param adjustment
   *          the adjustment
   */
  private void adjustFontSize( float adjustment )
  {
    if ( adjustment == 0.0f )
    {
      return;
    }
    UIDefaults defaults = UIManager.getDefaults(); // Build of Map of attributes for each component
    for ( Enumeration< Object > en = defaults.keys(); en.hasMoreElements(); )
    {
      Object o = en.nextElement();
      if ( o.getClass() != String.class )
      {
        continue;
      }
      String key = ( String )o;
      if ( key.endsWith( ".font" ) && !key.startsWith( "class" ) && !key.startsWith( "javax" ) )
      {
        FontUIResource font = ( FontUIResource )UIManager.get( key );
        FontUIResource newFont = new FontUIResource( font.deriveFont( font.getSize2D() + adjustment ) );
        if ( key.indexOf( "Table" ) != -1 )
        {
          System.err.println( "key=" + key );
          System.err.println( "got font " + font );
          System.err.println( "set font " + newFont );
        }
        UIManager.put( key, newFont );
      }
    }
    SwingUtilities.updateComponentTreeUI( this );
    pack();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange( PropertyChangeEvent event )
  {
    refresh();
  }

  public List< AssemblerItem > getClipBoardItems()
  {
    return clipBoardItems;
  }

  /** The look and feel items. */
  private JRadioButtonMenuItem[] lookAndFeelItems = null;

  /** The prompt buttons. */
  private JRadioButtonMenuItem[] promptButtons = null;

  /** The use all remotes. */
  private JMenuItem useAllRemotes = null;

  /** The use preferred remotes. */
  private JMenuItem usePreferredRemotes = null;

  /** The use default names. */
  private JMenuItem useDefaultNames = null;

  /** The use custom names. */
  private JMenuItem useCustomNames = null;

  /** The font size adjustment. */
  private Float fontSizeAdjustment = 0.0f;

  /** The Constant promptStrings. */
  private final static String[] promptStrings =
  {
      "Always", "On Exit", "Never"
  };

  /** The Constant promptMnemonics. */
  private final static int[] promptMnemonics =
  {
      KeyEvent.VK_A, KeyEvent.VK_X, KeyEvent.VK_N
  };

  /** The Constant PROMPT_NEVER. */
  public final static int PROMPT_NEVER = 2;

  /** The Constant PROMPT_ALWAYS. */
  public final static int PROMPT_ALWAYS = 0;

  /** The Constant anyEndings. */
  private final static String[] anyEndings =
  {
      ".txt", ".km", upgradeExtension
  };

  /** The Constant kmEndings. */
  private final static String[] kmEndings =
  {
    ".txt"
  };

  /** The Constant rmEndings. */
  private final static String[] rmEndings =
  {
      ".km", upgradeExtension
  };

  /** The Constant binaryEndings. */
  private final static String[] binaryEndings =
  {
      ".bin", "_obj"
  };
}
