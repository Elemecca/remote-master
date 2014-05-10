package com.hifiremote.jp1;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;

import com.hifiremote.jp1.io.IO;

public class CodeSelectorDialog extends JDialog implements ActionListener
{
  private CodeSelectorDialog( RemoteMaster rm )
  {
    super( rm );

    owner = rm;
    remoteConfig = rm.getRemoteConfiguration();
    remote = remoteConfig.getRemote();
    setTitle( "Code Selector" );
    
    MouseAdapter mouseAdapter = new MouseAdapter()
    {
      @Override
      public void mouseClicked( MouseEvent event )
      {
        JTextArea textArea = ( JTextArea )event.getSource();
        Point p = event.getPoint();

        int start = textArea.viewToModel( p );
        int end = start + 1;
        try
        {
          while ( start >= 0 && ! textArea.getText( start, 1).equals( " " ) ) start--;
          while ( end < textArea.getText().length() && ! textArea.getText( end, 1).equals( " " ) ) end++;
        }
        catch ( BadLocationException e )
        {
          e.printStackTrace();
        }
        textArea.select( start + 1, end );
        setSelectedCode( textArea.getSelectedText() );
        if ( assignButton.isEnabled() && event.getClickCount() == 2 )
        {
          assignButton.doClick();
        }
      }
    };

    JPanel devicePanel = new JPanel( new FlowLayout( FlowLayout.CENTER ) );
    devicePanel.setBorder( BorderFactory.createTitledBorder
        ( BorderFactory.createCompoundBorder
            ( BorderFactory.createLineBorder( Color.GRAY ), 
                BorderFactory.createEmptyBorder( 0, 15, 5, 15 ))," Device Type: " ) );
    deviceComboBox = new JComboBox( remote.getDeviceTypes() );
    deviceComboBox.addActionListener( this );
    Dimension d = deviceComboBox.getPreferredSize();
    d.width = 100;
    deviceComboBox.setPreferredSize( d );
    devicePanel.add( deviceComboBox );

    Box buttonBox = Box.createHorizontalBox();
    assignButton.addActionListener( this );
    buttonBox.add( assignButton );
    refreshButton.addActionListener( this );
    buttonBox.add( refreshButton );

    JPanel selectedPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
    selectedPanel.add( selectedLabel );

    Box actionBox = Box.createVerticalBox();
    actionBox.add( buttonBox );
    actionBox.add( Box.createVerticalGlue() );
    actionBox.add( selectedPanel );
    int width = assignButton.getPreferredSize().width;
    width += refreshButton.getPreferredSize().width;
    actionBox.add( Box.createHorizontalStrut( width ) );

    JPanel actionPanel = new JPanel( new BorderLayout() );
    actionPanel.add( devicePanel, BorderLayout.LINE_START );
    actionPanel.add( new JLabel(), BorderLayout.CENTER );
    actionPanel.add( actionBox, BorderLayout.LINE_END);

    internalArea = new JTextArea( 10, 40 );
    internalArea.setLineWrap( true );
    internalArea.setWrapStyleWord( true );
    internalArea.setEditable( false );
    internalArea.addMouseListener( mouseAdapter );

    JScrollPane internalPane = new JScrollPane( internalArea  );
    internalPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );

    Box labelBox1 = Box.createHorizontalBox();
    labelBox1.add( new JLabel( "Internal:") );
    labelBox1.add(  Box.createHorizontalGlue() );
    labelBox1.add( new JLabel( "Click on a code to select") );

    Box internalBox = Box.createVerticalBox();
    internalBox.add( labelBox1 );
    internalBox.add( Box.createVerticalStrut( 5 ) );
    internalBox.add( internalPane );

    upgradeArea = new JTextArea( 3, 40 );
    upgradeArea.setLineWrap( true );
    upgradeArea.setWrapStyleWord( true );
    upgradeArea.setEditable( false );
    upgradeArea.addMouseListener( mouseAdapter );

    JScrollPane upgradePane = new JScrollPane( upgradeArea );
    upgradePane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );

    Box labelBox2 = Box.createHorizontalBox();
    labelBox2.add( new JLabel( "Upgrade:" ) );
    labelBox2.add(  Box.createHorizontalGlue() );

    Box upgradeBox = Box.createVerticalBox();
    upgradeBox.add( Box.createVerticalStrut( 5 ) );
    upgradeBox.add( labelBox2 );
    upgradeBox.add( Box.createVerticalStrut( 5 ) );
    upgradeBox.add( upgradePane );

    JPanel codesPanel = new JPanel( new BorderLayout() );
    codesPanel.setBorder( BorderFactory.createTitledBorder
        ( BorderFactory.createCompoundBorder
            ( BorderFactory.createLineBorder( Color.GRAY ), 
                BorderFactory.createEmptyBorder( 5, 5, 5, 5 ))," Valid Device Codes: " ) );
    codesPanel.add(internalBox, BorderLayout.CENTER );
    codesPanel.add( upgradeBox, BorderLayout.PAGE_END );

    JComponent contentPane = ( JComponent )getContentPane();
    contentPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
    contentPane.add( actionPanel, BorderLayout.PAGE_START) ;
    contentPane.add( codesPanel, BorderLayout.CENTER );
    
    if ( remote.usesEZRC() )
    {
      double b = 5; // space between rows and around border
      double c = 10; // space between columns
      double pr = TableLayout.PREFERRED;
      double pf = TableLayout.FILL;
      double size[][] =
      {
          {
              b, pr, c, pf, b
          }, // cols
          {
              b, pr, b, pr, b, pr, b, pr, b
          }  // rows
      };
      assignButton.setVisible( false );
      refreshButton.setVisible( false );
      upgradeBox.setVisible( false );
      bottomPanel = new JPanel( new CardLayout() );
      locationPanel = new JPanel( new TableLayout( size ) );
      locationPanel.setBorder( BorderFactory.createTitledBorder
          ( BorderFactory.createCompoundBorder
              ( BorderFactory.createLineBorder( Color.GRAY ), 
                  BorderFactory.createEmptyBorder( 0, 5, 0, 5 ))," Location: " ) );
      bottomPanel.add( locationPanel, "ON" );
      contentPane.add( bottomPanel, BorderLayout.PAGE_END );
      category = new JTextField();
      brand = new JTextField();
      position = new JTextField();
      JTextArea locationArea = new JTextArea();
      JLabel label = new JLabel();
      locationArea.setFont( label.getFont() );
      locationArea.setBackground( label.getBackground() );
      locationArea.setLineWrap( true );
      locationArea.setWrapStyleWord( true );
      locationArea.setEditable( false );
      String message = "\nUse this location information to add the selected setup code "
          + "using the Settings facility of the remote.  You may use RMIR afterwards to "
          + "change the brand name if this is unsuitable.";
      locationArea.setText( message );
      locationPanel.add( new JLabel( "Category:" ), "1, 1" );
      locationPanel.add( category, "3, 1" );
      locationPanel.add( new JLabel( "Brand:" ), "1, 3" );
      locationPanel.add( brand, "3, 3" );
      locationPanel.add( new JLabel( "Position:" ), "1, 5" );
      locationPanel.add( position, "3, 5" );
      locationPanel.add( locationArea, "1, 7, 3, 7" );
      connectionPanel = new JPanel( new BorderLayout() );
      JTextArea messageArea = new JTextArea();
      messageArea.setFont( label.getFont() );
      messageArea.setBackground( label.getBackground() );
      messageArea.setLineWrap( true );
      messageArea.setWrapStyleWord( true );
      messageArea.setEditable( false );
      message = "\nTo add a new internal setup code, please use the Settings facility "
          + "of the remote.  If you know the code for your device, this Code Selector can "
          + "tell you the brand to select and which setup within that brand to use.  To "
          + "enable this capability, please connect your remote to the PC and press the "
          + "Connect button below.  Once the Location panel appears, you may disconnect "
          + "the remote.\n\nTo add a new setup with a device upgrade, use the Device "
          + "Upgrade tab to add the upgrade.  This will automatically add the upgrade "
          + "as a new Device.";
      messageArea.setText( message );
      connectionPanel.add( messageArea, BorderLayout.CENTER );
      buttonPanel = new JPanel( new CardLayout() );
      connectButton = new JButton( "Connect" );
      connectButton.addActionListener( this );
      JPanel panel = new JPanel();
      panel.add( connectButton );
      buttonPanel.add( panel, "OFF" );
      panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, 5 ) );
      label = new JLabel( "READING DATA...   " );
      Font font = label.getFont();
      Font boldfont = font.deriveFont( Font.BOLD );
      label.setFont( boldfont );
      panel.add( label );
      label = new JLabel( "This may take up to a minute" );
      panel.add( label );
      buttonPanel.add( panel, "ON" );
      label = new JLabel( "CONNECTION FAILED" );
      label.setFont( boldfont );
      label.setForeground( Color.RED );
      panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, 5 ) );
      panel.add( label );
      buttonPanel.add( panel, "FAIL" );
      CardLayout cl = ( CardLayout )buttonPanel.getLayout();
      cl.show( buttonPanel, "OFF" );
      connectionPanel.add( buttonPanel, BorderLayout.PAGE_END );
      bottomPanel.add(  connectionPanel, "OFF" );
      cl = ( CardLayout )bottomPanel.getLayout();
      cl.show( bottomPanel, remoteConfig.getDeviceCategories() == null ? "OFF" : "ON" );
    }

    setSelectedCode( "" );
    setupCodes = remote.getSetupCodes();
    deviceButtonTable = rm.getGeneralPanel().getDeviceButtonTable();
  }
  
  public static CodeSelectorDialog showDialog( RemoteMaster rm )
  {
    if ( selector == null || selector.remoteConfig != rm.getRemoteConfiguration() )
    {
      selector = new CodeSelectorDialog( rm );
    }
    selector.pack();
    // Preferred location is left-hand corner, to keep main window as visible as possible.
    // selector.setLocationRelativeTo( rm );
    selector.setVisible( true );
    selector.refreshButton.doClick();
    return selector;
  }

  
  private class ConnectTask extends SwingWorker< Void, Void >
  {
    @Override
    protected Void doInBackground() throws Exception
    {
      boolean done = false;
      if ( remote.isSSD() )
      {
        done = remoteConfig.setDeviceCategories();
      }
      else
      {
        IO hid = null;
        for ( IO temp : owner.getInterfaces() )
        {
          String tempName = temp.getInterfaceName();
          if ( tempName.equals( "CommHID" ) )
          {
            hid = temp.openRemote( "UPG" ).equals( "UPG" ) ? temp : null;
            if ( hid == null )
            {
              break;
            }
            short[] buffer = new short[ 0x30 ];
            if ( hid.readRemote( 0x020000, buffer, 0x30 ) != 0x30 )
            {
              break;
            }
            // Get address of IR database
            int irAddress = buffer[ 0x18 ] << 24 | buffer[ 0x19 ] << 16 | buffer[ 0x1A ] << 8 | buffer[ 0x1B ];
            // Get address of Text database
            int textAddress = buffer[ 0x24 ] << 24 | buffer[ 0x25 ] << 16 | buffer[ 0x26 ] << 8 | buffer[ 0x27 ];
            if ( hid.readRemote( irAddress + 20, buffer, 0x30 ) != 0x30 )
            {
              break;
            }
            int categoryAddress = ( buffer[ 16 ] << 8 | buffer[ 17 ] ) + irAddress; 
            remoteConfig.setDeviceCategories( hid, textAddress, categoryAddress );
            done = true;
            break;
          }
        }
        if ( hid != null )
        {
          hid.closeRemote();
        }
      }
      if ( done )
      {
        ( ( CardLayout )bottomPanel.getLayout() ).show( bottomPanel, "ON" );
      }
      else
      {
        ( ( CardLayout )buttonPanel.getLayout() ).show( buttonPanel, "FAIL" );
      }
      return null;
    }
  }

  @Override
  public void actionPerformed( ActionEvent event )
  {
    Object source = event.getSource();
    DeviceType deviceType = ( DeviceType )deviceComboBox.getSelectedItem();
    int row = deviceButtonTable.getSelectedRow();

    if ( source == deviceComboBox )
    {
      HashMap< Integer, Integer> typeCodes = setupCodes.get( deviceType.getNumber() );
      ArrayList< Integer > codes = new ArrayList< Integer >();
      if ( typeCodes != null )
      {
        codes.addAll( setupCodes.get( deviceType.getNumber() ).values() );
      }
      internalArea.setText( getCodeText( codes ) );
      codes = new ArrayList< Integer >();
      for ( DeviceUpgrade devUpgrade : remoteConfig.getDeviceUpgrades() )
      {
        if ( deviceType.getNumber() == devUpgrade.getDeviceType().getNumber()
            && ( devUpgrade.getButtonIndependent() 
                || ( row >= 0 && remote.getDeviceButtons()[ row ].getButtonIndex() == 
                devUpgrade.getButtonRestriction().getButtonIndex() ) ) )
        {
          codes.add( devUpgrade.getSetupCode() );
        }
      }
      upgradeArea.setText( getCodeText( codes ) );
      setSelectedCode( "" );
    }
    else if ( source == assignButton )
    {
      if ( deviceType == null || selectedCode.isEmpty() || row == -1 )
      {
        String message = "Nowhere selected for assignment.";
        String title = "Code Selector";
        JOptionPane.showMessageDialog( this, message, title, JOptionPane.ERROR_MESSAGE );
        return;
      }
      
      deviceButtonTable.setValueAt( deviceType, row, 2 );
      deviceButtonTable.setValueAt( selectedCode, row, 3 );
      DeviceButtonTableModel model = ( DeviceButtonTableModel )deviceButtonTable.getModel();
      model.fireTableRowsUpdated( row, row );
    }
    else if ( source == refreshButton )
    {
      if ( row >= 0 )
      {
        DeviceType rowType = ( DeviceType )deviceButtonTable.getValueAt( row, 2 );
        selector.deviceComboBox.setSelectedItem( rowType );
      }
      else if ( selector.deviceComboBox.getItemCount() > 0 )
      {
        selector.deviceComboBox.setSelectedIndex( 0 );
      }
      setSelectedCode( "" );
    }
    else if ( source == connectButton )
    {
      ( ( CardLayout ) buttonPanel.getLayout() ).show( buttonPanel, "ON" );
      ( new ConnectTask() ).execute();
    }
  }
    
  private void setSelectedCode( String code )
  {
    selectedCode = code;
    if ( selectedCode.length() != 4 )
    {
      selectedCode = "";
    }
    selectedLabel.setText( "Selected code:  " + selectedCode );
    assignButton.setEnabled( canAssign && !selectedCode.isEmpty() );
    if ( category != null && remoteConfig.getDeviceCategories() != null )
    {
      if ( code == null || code.isEmpty() )
      {
        category.setText( "" );
        brand.setText( "" );
        position.setText( "" );
        return;
      }
      DeviceType deviceType = ( DeviceType )deviceComboBox.getSelectedItem();
      int type = deviceType.getNumber();
      int setupCode = type << 16 | Integer.parseInt( code );
      Integer location = remoteConfig.getCodeLocations().get( setupCode );
      if ( location == null )
      {
        category.setText( "Not available in " + remoteConfig.getRegion() );
        brand.setText( null );
        position.setText( null );
        return;
      }
      int categoryIndex = location >> 24;
      int brandIndex = ( location >> 16 ) & 0xFF;
      int positionIndex = ( location >> 8) & 0xFF;
      int length = location & 0xFF;
      String categoryValue = remoteConfig.getDeviceCategories().get( categoryIndex );
      String brandValue = remoteConfig.getCategoryBrands().get( categoryIndex ).get( brandIndex );
      String positionValue = String.format( "%02d/%02d", positionIndex, length );
      category.setText( categoryValue );
      brand.setText( brandValue );
      position.setText( positionValue );   
    }
  }
  
  private String getCodeText( ArrayList< Integer > codes)
  {
    Collections.sort( codes );
    StringBuilder sb = new StringBuilder();
    for ( Integer code : codes )
    {
      sb.append( SetupCode.toString( code ) );
      sb.append( " ");
    }
    if ( sb.length() > 0 )
    {
      sb.deleteCharAt( sb.length() - 1 );
    }
    return sb.toString();
  }
  
  public void enableAssign( boolean enable )
  {
    canAssign = enable;
    assignButton.setEnabled( canAssign && !selectedCode.isEmpty() );
  }
  
  private HashMap< Integer, HashMap< Integer, Integer >> setupCodes = null;
  private JComboBox deviceComboBox = null;
  private JTextArea internalArea = null;
  private JTextArea upgradeArea = null;
  private String selectedCode = "";
  private RemoteMaster owner = null;
  private RemoteConfiguration remoteConfig = null;
  private Remote remote = null;
  private boolean canAssign = true;
  
  private JLabel selectedLabel = new JLabel();
  private JButton assignButton = new JButton( "Assign" );
  private JButton refreshButton = new JButton( "Refresh" );
  private JButton connectButton = null;
  
  private static JP1Table  deviceButtonTable = null;
  private static CodeSelectorDialog selector = null;
  
  private JTextField category = null;
  private JTextField brand = null;
  private JTextField position = null;
  private JPanel locationPanel = null;
  private JPanel connectionPanel = null;
  private JPanel buttonPanel = null;
  private JPanel bottomPanel = null;
}
