package com.hifiremote.jp1;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ProgressMonitor;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FilenameFilter;
import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.lang.ClassLoader;

public class KeyMapMaster
 extends JFrame
 implements ActionListener, ChangeListener
{
  private static KeyMapMaster me = null;
  private JMenuItem newItem = null;
  private JMenuItem openItem = null;
  private JMenuItem saveItem = null;
  private JMenuItem saveAsItem = null;
  private JLabel messageLabel = null;
  private JComboBox remoteList = null;
  private JComboBox deviceTypeList = null;
  private Remote[] remotes = null;
  private Vector protocols = new Vector();
  private Remote currentRemote = null;
  private DeviceType currentDeviceType = null;
  private SetupPanel setupPanel = null;
  private FunctionPanel functionPanel = null;
  private ExternalFunctionPanel externalFunctionPanel = null;
  private ButtonPanel buttonPanel = null;
  private OutputPanel outputPanel = null;
  private ProgressMonitor progressMonitor = null;
  private DeviceUpgrade deviceUpgrade = null;

  public KeyMapMaster()
    throws Exception
  {
    super( "KeyMap Master v 0.21" );
    setDefaultLookAndFeelDecorated( true );
    me = this;

    addWindowListener( new WindowAdapter()
    {
      public void windowClosing( WindowEvent event )
      {
        try
        {
          ;
        }
        catch ( Exception e )
        {
          System.err.println( "KeyMapMaster.windowClosing() caught an exception!" );
          e.printStackTrace( System.out );
        }
        System.exit(0);
      }
    });

    deviceUpgrade = new DeviceUpgrade();

    JMenuBar menuBar = new JMenuBar();
    setJMenuBar( menuBar );
    JMenu menu = new JMenu( "File" );
    menuBar.add( menu );
    newItem = new JMenuItem( "New" );
    newItem.addActionListener( this );
    menu.add( newItem );
    openItem = new JMenuItem( "Open..." );
    openItem.addActionListener( this );
    menu.add( openItem );
    saveItem = new JMenuItem( "Save" );
    saveItem.setEnabled( false );
    saveItem.addActionListener( this );
    menu.add( saveItem );
    saveAsItem = new JMenuItem( "Save as..." );
    saveAsItem.addActionListener( this );
    menu.add( saveAsItem );

    Container mainPanel = getContentPane();
    JTabbedPane tabbedPane = new JTabbedPane();
    mainPanel.add( tabbedPane, BorderLayout.CENTER );

    JPanel statusPanel = new JPanel();
    JLabel label = new JLabel( "Remote:" );
    statusPanel.add( label );
    remoteList = new JComboBox();
    remoteList.setMaximumRowCount( 16 );
    remoteList.setPrototypeDisplayValue( "A Really Long Remote Control Name with an Extender and more" );
    remoteList.setToolTipText( "Choose the remote for the upgrade being created." );
    statusPanel.add( remoteList );
    statusPanel.add( Box.createHorizontalStrut( 10 ));

    label = new JLabel( "Device Type:" );
    statusPanel.add( label );
    deviceTypeList = new JComboBox();
    deviceTypeList.setPrototypeDisplayValue( "A Device Type" );
    deviceTypeList.setToolTipText( "Choose the device type for the upgrade being created." );
    statusPanel.add( deviceTypeList );

    mainPanel.add( statusPanel, BorderLayout.NORTH );

    messageLabel = new JLabel( " " );
    messageLabel.setForeground( Color.red );

    mainPanel.add( messageLabel, BorderLayout.SOUTH );

    loadProtocols();

    setupPanel = new SetupPanel( deviceUpgrade, protocols );
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

    outputPanel = new OutputPanel( deviceUpgrade );
    tabbedPane.addTab( "Output", null, outputPanel,
                       "The output to copy-n-paste into IR." );

    pack();
    show();

    loadRemotes();
    setRemotes( remotes );
    setRemote( remotes[ 0 ]);

    remoteList.addActionListener( this );
    deviceTypeList.addActionListener( this );
    tabbedPane.addChangeListener( this );

    currPanel.update();

    clearMessage();
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
    String userDir = System.getProperty( "user.dir" );
    File file = new File( userDir );
    FilenameFilter filter = new FilenameFilter()
    {
      public boolean accept( File dir, String name )
      {
        return name.toLowerCase().endsWith( ".rdf" );
      }
    };

    File[] files = file.listFiles( filter );
    if ( files.length == 0 )
    {
      JOptionPane.showMessageDialog( this, "No remote definitions files were found!",
                                     "Error", JOptionPane.ERROR_MESSAGE );
      System.exit( -1 );
    }

    progressMonitor = new ProgressMonitor( this, "Loading remotes",
                                           "", 0, files.length );
    progressMonitor.setProgress( 0 );
    progressMonitor.setMillisToDecideToPopup( 1000 );

    remotes = new Remote[ files.length ];
    for ( int i = 0; i < files.length; i++ )
    {
      File rdf = files[ i ];
      progressMonitor.setNote( "Loading " + rdf.getName());
      remotes[ i ] = new Remote( rdf );
      progressMonitor.setProgress( i );
    }

    progressMonitor.setNote( "Sorting remotes" );
    Arrays.sort( remotes );
    progressMonitor.setProgress( files.length );
    progressMonitor.close();
  }

  private void loadProtocols()
    throws Exception
  {
    BufferedReader rdr = new BufferedReader( new FileReader( "protocols.ini" ));
    Properties props = null;
    String name = null;
    byte[] id = new byte[ 2 ];
    String type = null;

    while ( true )
    {
      String line = rdr.readLine();
      if ( line == null )
        break;

      if (( line.length() == 0 ) || ( line.charAt( 0 ) == '#' ))
        continue;

      while ( line.endsWith( "\\" ))
        line = line.substring(0, line.length() - 1 ) + rdr.readLine().trim();

      if ( line.charAt( 0 ) == '[' ) // begin new protocol
      {
        if ( name != null  )
        {
          Protocol protocol =
            ProtocolFactory.createProtocol( name, id, type, props );
          if ( protocol != null )
            protocols.add( protocol );
        }
        name = line.substring( 1, line.length() - 1 ).trim();
        props = new Properties();
        id = new byte[ 2 ];
        type = "Protocol";
      }
      else
      {
        StringTokenizer st = new StringTokenizer( line, "=", true );
        String parmName = st.nextToken().trim();
        String parmValue = null;
        st.nextToken(); // skip the =
        if ( !st.hasMoreTokens() )
          continue;
        else
          parmValue = st.nextToken( "" ).trim();

        if ( parmName.equals( "PID" ))
        {
          st = new StringTokenizer( parmValue, " " );
          id[ 0 ] = ( byte )Integer.parseInt( st.nextToken(), 16 );
          id[ 1 ] = ( byte )Integer.parseInt( st.nextToken(), 16 );
        }
        else if ( parmName.equals( "Type" ))
        {
          type = parmValue;
        }
        else
        {
          props.setProperty( parmName, parmValue );
        }
      }
    }
    rdr.close();
    protocols.add( ProtocolFactory.createProtocol( name, id, type, props ));

    if ( protocols.size() == 0 )
    {
      JOptionPane.showMessageDialog( this, "No protocols were loaded!",
                                     "Error", JOptionPane.ERROR_MESSAGE );
      System.exit( -1 );
    }

    clearMessage();
  }

  public void setRemotes( Remote[] remotes )
  {
    if ( remoteList != null )
    {
      remoteList.setModel( new DefaultComboBoxModel( remotes ));
      setRemote( remotes[ 0 ]);
      remoteList.setSelectedIndex( 0 );
    }
  }

  public void setRemote( Remote remote )
  {
    if (( remoteList != null ) && ( remote != currentRemote ))
    {
      currentRemote = remote;
      deviceUpgrade.setRemote( remote );
      setDeviceTypes( remote.getDeviceTypes());
    }
  }

  public void setDeviceTypes( DeviceType[] deviceTypes )
  {
    if ( deviceTypeList != null )
    {
      deviceTypeList.setModel( new DefaultComboBoxModel( deviceTypes ));
      deviceTypeList.setMaximumRowCount( deviceTypes.length );
      setDeviceType( deviceTypes[ 0 ]);
    }
  }

  public void setDeviceType( DeviceType type )
  {
    if (( deviceTypeList != null ) && ( type != currentDeviceType ))
    {
      currentDeviceType = type;
      deviceUpgrade.setDeviceType( type );
      deviceTypeList.setSelectedItem( type );
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
        // add code to try to match the current device type to a
        // type in the new type list.
        setDeviceTypes( remote.getDeviceTypes());
        currPanel.update();
      }
      else if ( source == deviceTypeList )
      {
        DeviceType type = ( DeviceType )deviceTypeList.getSelectedItem();
        setDeviceType( type );
        currPanel.update();
      }
      else if ( source == newItem )
      {
        deviceUpgrade.reset( remotes, protocols );
        remoteList.setSelectedItem( deviceUpgrade.getRemote());
        saveItem.setEnabled( false );
        currPanel.update();
      }
      else if ( source == saveItem )
      {
        deviceUpgrade.store();
      }
      else if ( source == saveAsItem )
      {
        JFileChooser chooser = new JFileChooser( System.getProperty( "user.dir" ));
        chooser.setFileFilter( new KMFileFilter());
        int returnVal = chooser.showSaveDialog( this );
        if ( returnVal == JFileChooser.APPROVE_OPTION )
        {
          String name = chooser.getSelectedFile().getAbsolutePath();
          if ( !name.toLowerCase().endsWith( ".km" ))
            name = name + ".km";
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
          }
        }
      }
      else if ( source == openItem )
      {
        JFileChooser chooser = new JFileChooser( System.getProperty( "user.dir" ));
        chooser.setFileFilter( new KMFileFilter());
        int returnVal = chooser.showOpenDialog( this );
        if ( returnVal == JFileChooser.APPROVE_OPTION )
        {
          String name = chooser.getSelectedFile().getAbsolutePath();
          if ( !name.endsWith( ".km" ))
            name = name + ".km";
          File file = new File( name );
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
            deviceUpgrade.load( file, remotes, protocols );
            saveItem.setEnabled( true );
            remoteList.removeActionListener( this );
            deviceTypeList.removeActionListener( this );
            DeviceType savedType = deviceUpgrade.getDeviceType();
            setRemote( deviceUpgrade.getRemote());
            remoteList.setSelectedItem( deviceUpgrade.getRemote());
            setDeviceType( savedType );
            remoteList.addActionListener( this );
            deviceTypeList.addActionListener( this );
            currPanel.update();
          }
        }
      }
    }
    catch ( Exception ex )
    {
      ex.printStackTrace( System.err );
    }
  }

  // ChangeListener methods
  private KMPanel currPanel = null;
  public void stateChanged( ChangeEvent e )
  {
    currPanel.commit();
    currPanel = ( KMPanel )(( JTabbedPane )e.getSource()).getSelectedComponent();
    currPanel.update();
  }

  public static void main( String[] args )
  {
    try
    {
      UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
      for ( int i = 0; i < info.length; i++ )
      {
        if ( info[ i ].getName().equals( "Windows" ))
        {
          UIManager.setLookAndFeel( info[ i ].getClassName());
          break;
        }
      }
      System.setErr( new PrintStream( new FileOutputStream( "km.err" )));
      KeyMapMaster km = new KeyMapMaster();
    }
    catch ( Exception e )
    {
      System.err.println( "Caught exception in KeyMapMaster.main()!" );
      e.printStackTrace( System.err );
    }
    System.err.flush();
  }

  private class KMFileFilter
    extends FileFilter
  {
    //Accept all directories and all .km files.
    public boolean accept( File f )
    {
      boolean rc = false;
      if ( f.isDirectory())
        rc = true;
      else if ( f.getName().toLowerCase().endsWith( ".km" ))
        rc = true;
      return rc;
    }

    //The description of this filter
    public String getDescription()
    {
      return "KeyMapMaster files";
    }
  }
}
