package com.hifiremote.jp1;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.Collection;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * The Class ImportRawUpgradeDialog.
 */
public class ImportRawUpgradeDialog extends JDialog implements ActionListener, DocumentListener, ItemListener
{
  /**
   * Instantiates a new import raw upgrade dialog.
   * 
   * @param owner
   *          the owner
   * @param deviceUpgrade
   *          the device upgrade
   */
  public ImportRawUpgradeDialog( JFrame owner, DeviceUpgrade deviceUpgrade, boolean restrictRemote )
  {
    super( owner, "Import Raw Upgrade", true );
    createGui( owner, deviceUpgrade, restrictRemote );
  }

  /**
   * Instantiates a new import raw upgrade dialog.
   * 
   * @param owner
   *          the owner
   * @param deviceUpgrade
   *          the device upgrade
   */
  public ImportRawUpgradeDialog( JDialog owner, DeviceUpgrade deviceUpgrade, boolean restrictRemote )
  {
    super( owner, "Import Raw Upgrade", true );
    createGui( owner, deviceUpgrade, restrictRemote );
  }

  private void prefillFromClipboard()
  {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable clipData = clipboard.getContents( clipboard );
    if ( clipData != null && clipData.isDataFlavorSupported( DataFlavor.stringFlavor ) )
    {
      try
      {
        String s = ( String )clipData.getTransferData( DataFlavor.stringFlavor );
        loadUpgrade( s );
      }
      catch ( Exception ex )
      {
        ex.printStackTrace( System.err );
      }
    }
  }

  /**
   * Creates the gui.
   * 
   * @param owner
   *          the owner
   */
  private void createGui( Component owner, DeviceUpgrade deviceUpgrade, boolean restrictRemote )
  {
    this.deviceUpgrade = deviceUpgrade;

    setLocationRelativeTo( owner );
    Container contentPane = getContentPane();

    double b = 5; // space between rows and around border
    double c = 10; // space between columns
    double pr = TableLayout.PREFERRED;
    double size[][] =
    {
        {
            b, pr, c, pr, b
        }, // cols
        {
            b, pr, b, pr, b, pr, b, pr, b, pr, pr, b, pr, pr, b, pr, b
        }
    // rows
    };
    TableLayout tl = new TableLayout( size );
    JPanel mainPanel = new JPanel( tl );
    contentPane.add( mainPanel, BorderLayout.CENTER );

    JLabel label = new JLabel( "Remote:" );
    mainPanel.add( label, "1, 1" );

    Remote[] remotes = null;
    if ( restrictRemote )
    {
      remotes = new Remote[ 1 ];
      remotes[ 0 ] = deviceUpgrade.getRemote();
    }
    else
    {
      Collection< Remote > allRemotes = RemoteManager.getRemoteManager().getRemotes();
      remotes = new Remote[ allRemotes.size() ];
      remotes = allRemotes.toArray( remotes );
    }
    remoteList = new JComboBox( remotes );
    label.setLabelFor( remoteList );
    remoteList.setSelectedItem( deviceUpgrade.getRemote() );
    remoteList.addItemListener( this );
    mainPanel.add( remoteList, "3, 1" );

    label = new JLabel( "Setup code:" );
    mainPanel.add( label, "1, 3" );

    setupCode = new JTextField();
    label.setLabelFor( setupCode );
    SetupCodeFilter filter = new SetupCodeFilter( setupCode );
    ( ( AbstractDocument )setupCode.getDocument() ).setDocumentFilter( filter );

    mainPanel.add( setupCode, "3, 3" );

    label = new JLabel( "Device Type:" );
    mainPanel.add( label, "1, 5" );

    String[] aliasNames = deviceUpgrade.getRemote().getDeviceTypeAliasNames();
    String alias = deviceUpgrade.getDeviceTypeAliasName();

    deviceTypeList = new JComboBox( aliasNames );
    label.setLabelFor( deviceTypeList );
    deviceTypeList.setMaximumRowCount( aliasNames.length );
    deviceTypeList.setSelectedItem( alias );
    mainPanel.add( deviceTypeList, "3, 5" );

    protocolGreaterThanFF = new JCheckBox( "Protocol > FF" );
    protocolGreaterThanFF.addItemListener( this );
    mainPanel.add( protocolGreaterThanFF, "3, 7" );
    protocolGreaterThanFF.setVisible( !deviceUpgrade.getRemote().usesTwoBytePID() );

    label = new JLabel( "Upgrade Code:" );
    mainPanel.add( label, "1, 9, 3, 9" );
    upgradeCode = new JTextArea( 10, 50 );
    upgradeCode.getDocument().addDocumentListener( this );
    new TextPopupMenu( upgradeCode );
    label.setLabelFor( upgradeCode );
    mainPanel.add( new JScrollPane( upgradeCode ), "1, 10, 3, 10" );

    protocolLabel = new JLabel( "Protocol Code:" );
    mainPanel.add( protocolLabel, "1, 12, 3, 12" );
    protocolCode = new JTextArea( 10, 50 );
    protocolCode.getDocument().addDocumentListener( this );
    new TextPopupMenu( protocolCode );
    protocolLabel.setLabelFor( protocolCode );
    mainPanel.add( new JScrollPane( protocolCode ), "1, 13, 3, 13" );

    JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );

    ok = new JButton( "OK" );
    ok.setEnabled( false );
    ok.setMnemonic( KeyEvent.VK_O );
    ok.addActionListener( this );
    buttonPanel.add( ok );

    reset = new JButton( "Reset" );
    reset.setMnemonic( KeyEvent.VK_R );
    reset.addActionListener( this );
    buttonPanel.add( reset );

    cancel = new JButton( "Cancel" );
    cancel.setMnemonic( KeyEvent.VK_C );
    cancel.addActionListener( this );
    buttonPanel.add( cancel );

    mainPanel.add( buttonPanel, "1, 15, 3, 15" );

    pack();
    Rectangle rect = getBounds();
    int x = rect.x - rect.width / 2;
    int y = rect.y - rect.height / 2;
    setLocation( x, y );

    prefillFromClipboard();
  }

  public void setRemote( Remote remote )
  {
    remoteList.setSelectedItem( remote );
  }

  private boolean loadUpgrade( String text )
  {
    boolean rc = false;
    try
    {
      BufferedReader rdr = new BufferedReader( new StringReader( text ) );
      String line = "";
      while ( line != null && line.equals( "" ) )
      {
        line = rdr.readLine();
      }

      if ( line.toUpperCase().startsWith( "UPGRADE CODE" ) )
      {
        rc = true;
        String[] tokens = line.split( "[=(/)]" );

        String deviceTypeName = tokens[ 2 ];
        deviceTypeList.setSelectedItem( deviceTypeName );

        setupCode.setText( tokens[ 3 ] );

        StringBuilder sb = new StringBuilder();
        while ( ( ( line = rdr.readLine() ) != null ) && !line.trim().equalsIgnoreCase( "End" ) )
        {
          sb.append( line );
        }

        SwingUtilities.invokeLater( new TextUpdater( upgradeCode, sb.toString() ) );

        line = rdr.readLine();
        while ( line != null && line.equals( "" ) )
        {
          line = rdr.readLine();
        }
      }

      if ( line != null && line.toUpperCase().startsWith( "UPGRADE PROTOCOL" ) )
      {
        rc = true;
        String[] tokens = line.split( "[=()]" );
        Hex pid = new Hex( tokens[ 1 ] );
        protocolGreaterThanFF.setSelected( pid.get( 0 ) > 255 );

        StringBuilder sb = new StringBuilder();
        while ( ( ( line = rdr.readLine() ) != null ) && !line.trim().equalsIgnoreCase( "End" ) )
        {
          if ( sb.length() > 0 )
          {
            sb.append( '\n' );
          }
          sb.append( line );
        }
        SwingUtilities.invokeLater( new TextUpdater( protocolCode, sb.toString() ) );
      }

      return rc;
    }
    catch ( Exception ex )
    {
      JOptionPane.showMessageDialog( this, ex.getMessage(), "Error parsing clipboard data", JOptionPane.ERROR_MESSAGE );
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();
    if ( source == ok )
    {
      setVisible( false );
      try
      {
        deviceUpgrade.importRawUpgrade( uCode, ( Remote )remoteList.getSelectedItem(),
            ( String )deviceTypeList.getSelectedItem(), pid, pCode );
        if ( !setupCode.getText().equals( "" ) )
        {
          try
          {
            deviceUpgrade.setSetupCode( Integer.parseInt( setupCode.getText() ) );
          }
          catch ( NumberFormatException ex )
          {

          }
        }
      }
      catch ( ParseException pe )
      {
        JOptionPane.showMessageDialog( this, pe.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE );
      }
      dispose();
    }
    else if ( source == cancel )
    {
      setVisible( false );
      dispose();
    }
    else if ( source == reset )
    {
      remoteList.setSelectedItem( deviceUpgrade.getRemote() );
      setupCode.setText( "" );
      protocolGreaterThanFF.setSelected( false );
      upgradeCode.setText( "" );
      protocolCode.setText( "" );
      validateInput();

    }
  }

  /**
   * Validate input.
   */
  private void validateInput()
  {
    if ( uCode == null )
    {
      ok.setEnabled( false );
      protocolCode.setEnabled( false );
      protocolLabel.setText( "Protocol Code:" );
      protocolLabel.setEnabled( false );
      return;
    }

    Remote remote = ( Remote )remoteList.getSelectedItem();
    if ( remote.usesTwoBytePID() )
    {
      pid = new Hex( uCode, 0, 2 );
    }
    else
    {
      short[] temp = new short[ 2 ];
      temp[ 0 ] = ( short )( protocolGreaterThanFF.isSelected() ? 1 : 0 );
      temp[ 1 ] = uCode.getData()[ 0 ];
      pid = new Hex( temp );
    }

    Protocol p = ProtocolManager.getProtocolManager().findProtocolForRemote( remote, pid );
    if ( p != null )
    {
      protocolLabel.setText( "Protocol Code:" );
      ok.setEnabled( true );
      return;
    }
    else
    {
      protocolLabel.setText( "Protocol Code for Unknown PID " + pid + " ** Required **" );
      protocolLabel.setEnabled( true );
      protocolCode.setEnabled( true );
    }

    if ( pCode == null )
    {
      ok.setEnabled( false );
      return;
    }

    ok.setEnabled( true );
  }

  // DocumentListener methods
  /**
   * Document changed.
   * 
   * @param e
   *          the e
   */
  public void documentChanged( DocumentEvent e )
  {
    Document doc = e.getDocument();
    if ( doc == upgradeCode.getDocument() )
    {
      String text = upgradeCode.getText().trim();
      if ( ( text == null ) || ( text.length() == 0 ) )
      {
        uCode = null;
      }
      else
      {
        if ( !loadUpgrade( text ) )
        {
          try
          {
            uCode = new Hex( text );
          }
          catch ( Exception ex )
          {
            uCode = null;
          }
        }
      }
    }
    else if ( doc == protocolCode.getDocument() )
    {
      String text = protocolCode.getText().trim();
      if ( ( text == null ) || ( text.length() == 0 ) )
      {
        pCode = null;
      }
      else
      {
        if ( !loadUpgrade( text ) )
        {
          try
          {
            pCode = new Hex( text );
            if ( pCode.length() < 3 )
              pCode = null;
          }
          catch ( Exception ex )
          {
            pCode = null;
          }
        }
      }
    }
    validateInput();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
   */
  public void changedUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
   */
  public void insertUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
   */
  public void removeUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }

  // ItemListener methods
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  public void itemStateChanged( ItemEvent e )
  {
    if ( e.getSource() == remoteList )
    {
      try
      {
        if ( e.getStateChange() == ItemEvent.SELECTED )
        {
          Remote remote = ( Remote )e.getItem();
          remote.load();
          String[] aliasNames = remote.getDeviceTypeAliasNames();
          String alias = deviceUpgrade.getDeviceTypeAliasName();
          deviceTypeList.setModel( new DefaultComboBoxModel( aliasNames ) );
          deviceTypeList.setMaximumRowCount( aliasNames.length );
          deviceTypeList.setSelectedItem( alias );
          protocolGreaterThanFF.setVisible( !remote.usesTwoBytePID() );
          validateInput();
        }
      }
      catch ( Exception ex )
      {
        ex.printStackTrace( System.err );
      }

    }
    else
    {
      validateInput();
    }
  }

  /** The device upgrade. */
  private DeviceUpgrade deviceUpgrade = null;

  /** The remote list. */
  private JComboBox remoteList = null;

  private JTextField setupCode = null;

  /** The device type list. */
  private JComboBox deviceTypeList = null;

  /** The protocol greater than ff. */
  private JCheckBox protocolGreaterThanFF = null;

  /** The upgrade code. */
  private JTextArea upgradeCode = null;

  /** The protocol label. */
  private JLabel protocolLabel = null;

  /** The protocol code. */
  private JTextArea protocolCode = null;

  /** The u code. */
  private Hex uCode = null;

  /** The pid. */
  private Hex pid = null;

  /** The p code. */
  private Hex pCode = null;

  /** The ok. */
  private JButton ok = null;

  private JButton reset = null;

  /** The cancel. */
  private JButton cancel = null;

  public class TextUpdater implements Runnable
  {
    private JTextComponent component;
    private String text;

    public TextUpdater( JTextComponent component, String text )
    {
      this.component = component;
      this.text = text;
    }

    public void run()
    {
      component.setText( text );
    }
  }
}
