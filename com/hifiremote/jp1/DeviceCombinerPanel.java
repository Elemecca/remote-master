package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class DeviceCombinerPanel
  extends KMPanel
{
  public DeviceCombinerPanel( DeviceUpgrade devUpgrade )
  {
    super( "Device Combiner", devUpgrade );
    setToolTipText( "Combine multiple devices into a single upgrade" );
    setLayout( new BorderLayout());

    model = new AbstractTableModel()
    {
      public String getColumnName( int col )
      {
        return titles[ col ];
      }
      
      public Class getColumnClass( int col )
      {
        return classes[ col ];
      }

      public boolean isCellEditable( int row, int col )
      {
        return false;
      }

      public int getColumnCount(){ return titles.length; }
      public int getRowCount()
      {
        DeviceCombiner deviceCombiner = ( DeviceCombiner )deviceUpgrade.getProtocol();
        return deviceCombiner.getDevices().size();
      }

      public Object getValueAt( int row, int col )
      {
        DeviceCombiner deviceCombiner = ( DeviceCombiner )deviceUpgrade.getProtocol();
        CombinerDevice device = ( CombinerDevice )deviceCombiner.getDevices().elementAt( row );
        if ( device == null )
          return null;
        if ( col == 0 )
          return new Integer( row + 1 );
        else if ( col == 1 )
          return device.getProtocol().getName();
        else if ( col == 2 )
          return device.getProtocol().getID();
        else if ( col == 3 )
        {
          Protocol p = device.getProtocol();
          p.setDeviceParms( device.getValues());
          return p.getFixedData();
        } 
        return null;
      }
    };
    JTable table = new JTable( model );
//    add( table.getTableHeader(), BorderLayout.NORTH );
    add( new JScrollPane( table ), BorderLayout.CENTER );

    ActionListener al = new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        Object source = e.getSource();
        if ( source == importButton )
        {
          File file = KeyMapMaster.promptForUpgradeFile( null );
          if ( file == null )
            return;
          DeviceUpgrade importedUpgrade = new DeviceUpgrade();
          try
          {
            importedUpgrade.load( file );
            importedUpgrade.setRemote( deviceUpgrade.getRemote());

            Protocol importedProtocol = importedUpgrade.getProtocol();
            if ( importedProtocol.getDefaultCmd().length() > 1 )
            {
              JOptionPane.showMessageDialog( null,
                                             "Device Combiner can only combine protocol that use 1-byte commands.  " +
                                             "The device upgrade you tried to import uses the '" +
                                             importedProtocol.getName() + "' protocol, which uses " +
                                             importedProtocol.getDefaultCmd().length() + "-byte commands.",
                                             "Incompatible Upgrade",
                                             JOptionPane.ERROR_MESSAGE );
              return;
            }

            FunctionImportDialog d = new FunctionImportDialog( null, importedUpgrade );
            d.show();
            if ( d.getUserAction() == JOptionPane.OK_OPTION )
            {
              CombinerDevice device = new CombinerDevice( importedProtocol, importedUpgrade.getParmValues());
              DeviceCombiner combiner = ( DeviceCombiner )deviceUpgrade.getProtocol();
              Vector devices = combiner.getDevices();
              int index = devices.size();
              Integer indexInt = new Integer( index );
              devices.add( device );
              
              Vector importedFunctions = d.getSelectedFunctions();
              if ( importedFunctions.size() > 0 )
              {
                Vector functions = deviceUpgrade.getFunctions();
                int firstRow =  functions.size();
                for ( Enumeration enum = importedFunctions.elements(); enum.hasMoreElements(); )
                {
                  Function f = ( Function )enum.nextElement();
                  Function newF = new Function();
                  Hex hex = combiner.getDefaultCmd();
                  combiner.setValueAt( 0, hex, indexInt );
                  EFC efc = importedProtocol.hex2efc( f.getHex());
                  combiner.efc2hex( efc, hex );
                  newF.setHex( hex );
                  newF.setName( f.getName());
                  newF.setNotes( f.getNotes());
                  functions.add( newF );
                }
              }
              model.fireTableRowsInserted( index, index );
            }
          }
          catch ( Exception ex )
          {
            JOptionPane.showMessageDialog( null,
                                           "An error occurred loading the device upgrade from " +
                                           file.getName() + ".  Please see rmaster.err for more details.",
                                           "Device Upgrade Load Error",
                                           JOptionPane.ERROR_MESSAGE );
          }
        }
      }
    };

    JPanel panel = new JPanel( new FlowLayout( FlowLayout.RIGHT ));
    importButton = new JButton( "Import" );
    importButton.addActionListener( al );
    panel.add( importButton );

    removeButton = new JButton( "Remove" );
    removeButton.addActionListener( al );
    panel.add( removeButton );
    
    add( panel, BorderLayout.SOUTH );
    initColumns( table );
  }

  protected void initColumns( JTable table )
  {

    TableColumnModel columnModel = table.getColumnModel();

    JLabel l = new JLabel( model.getColumnName( 0 ));
    l.setBorder( BorderFactory.createEmptyBorder( 0, 4, 0, 4 ));
    columnModel.getColumn( 0 ).setMaxWidth( l.getPreferredSize().width );

    l.setText( model.getColumnName( 2 ));
    columnModel.getColumn( 2 ).setMaxWidth( l.getPreferredSize().width );

    table.doLayout();
  }


  private static String[] titles = { "#", "Protocol", "  PID  ", "Fixed Data" };
  private static Class[] classes = { Integer.class, String.class, Hex.class, Hex.class };

  private AbstractTableModel model = null;
  private JButton importButton = null;
  private JButton removeButton = null;
}
