package com.hifiremote.jp1;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;

public class Preferences
{
  public Preferences( PropertyFile file )
  {
    this.file = file;
  }

  public void load( JMenu recentFileMenu )
  {
    String defaultLookAndFeel = UIManager.getSystemLookAndFeelClassName();
    String temp = file.getProperty( "LookAndFeel", defaultLookAndFeel );
    try
    {
      UIManager.setLookAndFeel( temp );
      SwingUtilities.updateComponentTreeUI( KeyMapMaster.getKeyMapMaster());
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

    /*
    lastRemoteName = props.getProperty( "Remote.name" );
    lastRemoteSignature = props.getProperty( "Remote.signature" );
    */
    temp = file.getProperty( "FontSizeAdjustment" );
    if ( temp != null )
    {
      fontSizeAdjustment = Float.parseFloat( temp );
      adjustFontSize( fontSizeAdjustment );
    }

    temp = file.getProperty( "PromptToSave", promptStrings[ 0 ] );
    for ( int i = 0; i < promptStrings.length; i++ )
      if ( promptStrings[ i ].equals( temp ))
        promptFlag = i;
    if ( promptFlag > promptStrings.length )
      promptFlag = 0;

    promptButtons[ promptFlag ].setSelected( true );

    for ( int i = 0; true; i++ )
    {
      temp = file.getProperty( "PreferredRemotes." + i );
      if ( temp == null )
        break;
      System.err.println( "Preferred remote name " + temp );
      preferredRemoteNames.add( temp );
    }

    temp = file.getProperty( "ShowRemotes", "All" );
    if ( temp.equals( "All" ))
      useAllRemotes.setSelected( true );
    else
      usePreferredRemotes.setSelected( true );

    if ( preferredRemoteNames.size() == 0 )
    {
      useAllRemotes.setSelected( true );
      usePreferredRemotes.setEnabled( false );
    }
    
    file.updateFileMenu( recentFileMenu, "RecentUpgrades." );

    temp = file.getProperty( "CustomNames" );
    if ( temp != null )
    {
      StringTokenizer st = new StringTokenizer( temp, "|" );
      int count = st.countTokens();
      customNames = new String[ count ];
      for ( int i = 0; i < count; i++ )
        customNames[ i ] = st.nextToken();
    }

    temp = file.getProperty( "UseCustomNames" );
    useCustomNames.setSelected( temp != null );
  }

  private void adjustFontSize( float adjustment )
  {
    UIDefaults defaults = UIManager.getDefaults(); // Build of Map of attributes for each component
    for( Enumeration en = defaults.keys(); en.hasMoreElements(); )
    {
      Object o = en.nextElement();
      if ( o.getClass() != String.class )
        continue;
      String key = ( String )o; 
      if ( key.endsWith(".font") && !key.startsWith("class") && !key.startsWith("javax"))
      {
        FontUIResource font = ( FontUIResource )UIManager.get( key );
        FontUIResource newFont = new FontUIResource( font.deriveFont( font.getSize2D() + adjustment ));
        if ( key.indexOf( "Table") != -1 )
        {  
          System.err.println( "key=" + key );
          System.err.println( "got font " + font );
          System.err.println( "set font " + newFont );
        }
        UIManager.put( key, newFont );
      }
    }
    SwingUtilities.updateComponentTreeUI( KeyMapMaster.getKeyMapMaster());
    KeyMapMaster.getKeyMapMaster().pack();
  }

  public void createMenuItems( JMenu menu )
  {
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
          SwingUtilities.updateComponentTreeUI( KeyMapMaster.getKeyMapMaster());
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

    submenu = new JMenu( "Font size" );
    submenu.setMnemonic( KeyEvent.VK_F );
    menu.add( submenu );

    al = new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        JMenuItem button = ( JMenuItem )e.getSource();
        float adjustment = Float.parseFloat( button.getActionCommand());
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
        if (( source == useAllRemotes ) || ( source == usePreferredRemotes ))
          KeyMapMaster.getKeyMapMaster().setRemotes();
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
        if (( source != useDefaultNames ) && ( source != useCustomNames ))
          editCustomNames();
      }
    };
    useDefaultNames.setSelected( true );
    useDefaultNames.addActionListener( al );
    group.add( useDefaultNames );
    submenu.add( useDefaultNames );

    useCustomNames = new JRadioButtonMenuItem( "Custom" );
    useCustomNames.setMnemonic( KeyEvent.VK_C );
    useCustomNames.addActionListener( al );
    group.add( useCustomNames );
    submenu.add( useCustomNames );

    submenu.addSeparator();
    item = new JMenuItem( "Edit custom names..." );
    item.setMnemonic( KeyEvent.VK_E );
    item.addActionListener( al );
    submenu.add( item );
  }

  public boolean getUsePreferredRemotes()
  {
    return usePreferredRemotes.isSelected();
  }

  public Remote[] getPreferredRemotes()
  {
    if ( preferredRemotes.length == 0 )
    {
      RemoteManager rm = RemoteManager.getRemoteManager();
      Vector< Remote > work = new Vector< Remote >();
      for ( String name : preferredRemoteNames )
      {
        Remote r = rm.findRemoteByName( name );
        if ( r != null )
          work.add( r );
      }
      preferredRemotes = work.toArray( preferredRemotes );
      preferredRemoteNames.removeAllElements();
      preferredRemoteNames = null;
    }

    return preferredRemotes;
  }

  public void save( JMenu recentFileMenu )
    throws Exception
  {
    file.setProperty( "LookAndFeel", UIManager.getLookAndFeel().getClass().getName());
    if ( fontSizeAdjustment != 0f )
      file.setProperty( "FontSizeAdjustment", Float.toString( fontSizeAdjustment ));
    Remote remote = KeyMapMaster.getRemote();
    file.setProperty( "Remote.name", remote.getName());
    file.setProperty( "Remote.signature", remote.getSignature());
    file.setProperty( "PromptToSave", promptStrings[ promptFlag ]);
    if ( useAllRemotes.isSelected())
      file.setProperty( "ShowRemotes", "All" );
    else
      file.setProperty( "ShowRemotes", "Preferred" );

    for ( int i = 0; i < recentFileMenu.getItemCount(); i++ )
    {
      JMenuItem item = recentFileMenu.getItem( i );
      FileAction action = ( FileAction )item.getAction();
      File f = action.getFile();
      file.setProperty( "RecentUpgrades." + i, f.getAbsolutePath());
    }

    for ( int i = 0; i < preferredRemotes.length; i++ )
    {
      Remote r = preferredRemotes[ i ];
      file.setProperty( "PreferredRemotes." + i, r.getName());
    }

    KeyMapMaster km = KeyMapMaster.getKeyMapMaster();
    /*
    int state = km.getExtendedState();
    if ( state != Frame.NORMAL )
      km.setExtendedState( Frame.NORMAL );
    */
    Rectangle bounds = km.getBounds();
    setBounds( km.getBounds());

    if ( useCustomNames.isSelected())
      file.setProperty( "UseCustomNames", "yes" );

    if ( customNames != null )
    {
      StringBuffer value = new StringBuffer();
      for ( int i = 0; i < customNames.length; i++ )
      {
        if ( i != 0 )
          value.append( '|' );
        value.append( customNames[ i ]);
      }
      file.setProperty( "CustomNames", value.toString() );
    }
    file.save();
  }

  public File getRDFPath()
  {
    return file.getFileProperty( "RDFPath" );
  }
  
  public File getUpgradePath()
  {
    File path = file.getFileProperty( "UpgradePath" );
    if ( path != null )
      return path;
    path = file.getFileProperty( "KMPath" );
    if ( path != null )
    {
      file.remove( "KMPath" );
      setUpgradePath( path );
      return path;
    }
        
    path = new File( file.getFile().getParentFile(), upgradeDirectory );
    setUpgradePath( path );
    return path;
  }

  public void setUpgradePath( File path )
  {
    file.setProperty( "UpgradePath", path );
  }

  public File getBinaryUpgradePath()
  {
    return file.getFileProperty( "BinaryUpgradePath", getUpgradePath());
  }

  public void setBinaryUpgradePath( File path )
  {
    file.setProperty( "BinaryUpgradePath", path );
  }

  public Rectangle getBounds()
  {
    String temp = file.getProperty( "KMBounds" );
    if ( temp == null )
      return null;
    Rectangle bounds = new Rectangle();
    StringTokenizer st = new StringTokenizer( temp, "," );
    bounds.x = Integer.parseInt( st.nextToken());
    bounds.y = Integer.parseInt( st.nextToken());
    bounds.width = Integer.parseInt( st.nextToken());
    bounds.height = Integer.parseInt( st.nextToken());
    return bounds;
  }
  
  public void setBounds( Rectangle bounds )
  {
    file.setProperty( "KMBounds", "" + bounds.x + ',' + bounds.y + ',' + bounds.width + ',' + bounds.height );
  }

  public String getLastRemoteName()
  {
    return file.getProperty( "Remote.name" );
  }
  
  public void setLastRemoteName( String name )
  {
    file.setProperty( "Remote.name", name );
  }

  public int getPromptFlag()
  {
    return promptFlag;
  }

  private void editPreferredRemotes()
  {
    KeyMapMaster km = KeyMapMaster.getKeyMapMaster();
    PreferredRemoteDialog d = new PreferredRemoteDialog( km, preferredRemotes );
    d.setVisible( true );
    if ( d.getUserAction() == JOptionPane.OK_OPTION )
    {
      preferredRemotes = d.getPreferredRemotes();
      if ( preferredRemotes.length == 0 )
      {
        usePreferredRemotes.setEnabled( false );
        if  ( !useAllRemotes.isSelected())
        {
          useAllRemotes.setSelected( true );

          km.setRemoteList( RemoteManager.getRemoteManager().getRemotes());
        }
      }
      else
        usePreferredRemotes.setEnabled( true );

      if ( usePreferredRemotes.isSelected())
        km.setRemoteList( preferredRemotes );
    }
  }

  public String[] getCustomNames()
  {
    if ( useCustomNames.isSelected())
      return customNames;
    else
      return null;
  }


  private void editCustomNames()
  {
    CustomNameDialog d = new CustomNameDialog( KeyMapMaster.getKeyMapMaster(), customNames );
    d.setVisible( true );
    if ( d.getUserAction() == JOptionPane.OK_OPTION )
    {
      customNames = d.getCustomNames();
      if (( customNames == null ) || customNames.length == 0 )
      {
        useCustomNames.setEnabled( false );
        useDefaultNames.setSelected( true );
      }
      else
        useCustomNames.setEnabled( true );
    }
  }

  private PropertyFile file;
  private JRadioButtonMenuItem[] lookAndFeelItems = null;
  private JRadioButtonMenuItem[] promptButtons = null;
  private float fontSizeAdjustment = 0f;
  private Vector< String > preferredRemoteNames = new Vector< String >();
  private static String[] customNames = null;

  private final static String upgradeDirectory = "Upgrades";
  private final static String[] promptStrings = { "Always", "On Exit", "Never" };
  private final static int[] promptMnemonics = { KeyEvent.VK_A, KeyEvent.VK_X, KeyEvent.VK_N };
  public final static int PROMPT_NEVER = 2;
  public final static int PROMPT_ALWAYS = 0;
  private int promptFlag = 0;
  private JMenuItem useAllRemotes = null;
  private JMenuItem usePreferredRemotes = null;
  private JMenuItem useDefaultNames = null;
  private JMenuItem useCustomNames = null;
  private Rectangle bounds = null;
  private Remote[] preferredRemotes = new Remote[ 0 ];
}
