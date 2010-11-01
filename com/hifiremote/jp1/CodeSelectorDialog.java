package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;

public class CodeSelectorDialog extends JDialog implements ActionListener
{
  private CodeSelectorDialog( RemoteMaster rm )
  {
    super( rm );

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
  private RemoteConfiguration remoteConfig = null;
  private Remote remote = null;
  private boolean canAssign = true;
  
  private JLabel selectedLabel = new JLabel();
  private JButton assignButton = new JButton( "Assign" );
  private JButton refreshButton = new JButton( "Refresh" );
  
  private static JP1Table  deviceButtonTable = null;
  private static CodeSelectorDialog selector = null;

}
