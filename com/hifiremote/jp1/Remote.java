package com.hifiremote.jp1;

import java.util.*;
import java.io.*;
import javax.swing.*;

public class Remote
  implements Comparable
{
  public Remote( File rdf )
  {
    file = rdf;
    StringTokenizer st = new StringTokenizer( rdf.getName());
    signature = st.nextToken(); // upto the 1st space
    st.nextToken( "()" ); // skip the space
    name = st.nextToken(); // the stuff between the parens
  }

  private void checkLoaded()
  {
    if ( !loaded )
    {
      loaded = true;
      load();
    }
  }

  private void load()
  {
    try
    {
      RDFReader rdr = new RDFReader( file );
      String line = rdr.readLine();
      while ( line != null )
      {
        if ( line.length() == 0 )
        {
          line = rdr.readLine();
        }
        else if ( line.charAt( 0 ) == '[' )
        {
          StringTokenizer st = new StringTokenizer( line, "[]" );
          line = st.nextToken();

          if ( line.equals( "General" ))
            line = parseGeneralSection( rdr );
          else if ( line.equals( "Checksums" ))
            line = parseCheckSums( rdr );
          else if ( line.equals( "Settings" ))
            line = parseSettings( rdr );
          else if ( line.equals( "FixedData" ))
            line = parseFixedData( rdr );
          else if ( line.equals( "DeviceButtons" ))
            line = parseDeviceButtons( rdr );
          else if ( line.equals( "DigitMaps" ))
            line = parseDigitMaps( rdr );
          else if ( line.equals( "DeviceTypes" ))
            line = parseDeviceTypes( rdr );
          else if ( line.equals( "DeviceTypeAliases" ))
            line = parseDeviceTypeAliases( rdr );
          else if ( line.equals( "Buttons" ))
            line = parseButtons( rdr );
          else if ( line.equals( "MultiMacros" ))
            line = parseMultiMacros( rdr );
          else if ( line.equals( "ButtonMaps" ))
            line = parseButtonMaps( rdr );
          else if ( line.equals( "Protocols" ))
            line = parseProtocols( rdr );
          else
            line = rdr.readLine();
        }
        else
          line = rdr.readLine();
      }
      rdr.close();

      if ( buttonMaps.length == 0 )
        System.err.println( "ERROR: " + file.getName() + " does not specify any ButtonMaps!" );
      for ( int i = 0; i < buttonMaps.length; i++ )
        buttonMaps[ i ].setButtons( buttons );

      for ( Enumeration e = deviceTypes.elements(); e.hasMoreElements(); )
      {
        DeviceType type = ( DeviceType )e.nextElement();
        int map = type.getMap();
        if ( map == -1 )
          System.err.println( "ERROR:" + file.getName() + ": DeviceType " + type.getName() + " doesn't have a map." );
        if (( map != -1 ) && ( buttonMaps.length > 0 ))
          type.setButtonMap( buttonMaps[ map ] );
      }

      // Create the upgradeButtons[]
      // and starts off with the buttons in longest button map
      ButtonMap longestMap = null;
      for ( Enumeration e = deviceTypes.elements(); e.hasMoreElements(); )
      {
        DeviceType type = ( DeviceType )e.nextElement();
        ButtonMap thisMap = type.getButtonMap();
        if (( longestMap == null ) || ( longestMap.size() < thisMap.size() ))
          longestMap = thisMap;
      }

      if ( longestMap == null )
        upgradeButtons = buttons;
      else
      {
        upgradeButtons = new Button[ buttons.length ];
        // first copy the buttons from the longest map
        int index = 0;
        while ( index < longestMap.size())
        {
          upgradeButtons[ index ] = longestMap.get( index );
          index++;
        }

        // now copy the rest of the buttons, skipping those in the map
        for ( int i = 0; i < buttons.length; i++ )
        {
          Button b = buttons[ i ];
          if ( !longestMap.isPresent( b ))
            upgradeButtons[ index++ ] = b;
        }
      }
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

  public void print( PrintWriter out )
  {
    checkLoaded();
    out.println( "[General]" );
    out.println( "Name=" + name );
    out.println( "EepromSize=$" + Integer.toHexString( eepromSize ));

    if ( deviceCodeOffset != 0 )
      out.println( "DevCodeOffset=$" + Integer.toHexString( deviceCodeOffset ));

    if ( favKey != null )
    {
      out.println( "FavKey=" + favKey );
    }

    if ( oemDevice != null )
    {
      out.println( "OEMDevice=" + oemDevice );
    }

    if ( oemControl != 0 )
    {
      out.println( "OEMControl=$" + Integer.toHexString( oemControl ));
    }

    if ( upgradeBug )
      out.println( "UpgradeBug=1" );

    if ( advancedCodeAddress != null )
    {
      out.println( "AdvCodeAddr=" + advancedCodeAddress );
    }

    if ( upgradeAddress != null )
    {
      out.println( "UpgradeAddr=" + upgradeAddress );
    }

    if ( deviceUpgradeAddress != null )
    {
      out.println( "DevUpgradeAddr=" + deviceUpgradeAddress );
    }

    if ( timedMacroAddress != null )
    {
      out.println( "TimedMacroAddr=" + timedMacroAddress );
    }

    if ( learnedAddress != null )
    {
      out.println( "LearnedAddr=" + learnedAddress );
    }

    if ( timeAddress != 0 )
    {
      out.println( "TimeAddr=$" + Integer.toHexString( timeAddress ));
    }

    if ( !macroSupport )
      out.println( "MacroSupport=0" );

    if ( timedMacroWarning )
      out.println( "TimedMacroWarning=1" );

    out.println( "Processor=" + processor );

    if ( RAMAddress != 0 )
      out.println( "RAMAddr=$" + Integer.toHexString( RAMAddress ));

    if ( RDFSync != 0 )
      out.println( "RDFSync=" + RDFSync );

    if ( punchThruBase != 0 )
      out.println( "PunchThruBase=$" + Integer.toHexString( punchThruBase ));

    if ( scanBase != 0 )
      out.println( "ScanBase=$" + Integer.toHexString( scanBase ));

    if ( sleepStatusBit != null )
      out.println( "SleepStatusBit=" + sleepStatusBit );

    if ( vptStatusBit != null )
      out.println( "VPTStatusBit=" + vptStatusBit );

    int size = checkSums.length;
    if ( size > 0 )
    {
      out.println();
      out.println( "[CheckSums]" );
      for ( int i = 0; i < size; i++ )
      {
        out.println( checkSums[ i ]);
      }
    }

    size = settings.length;
    if ( size > 0 )
    {
      out.println();
      out.println( "[Settings]" );
      for ( int i = 0; i < size; i++ )
      {
        out.println( settings[ i ]);
      }
    }

    size = fixedData.length;
    if ( size > 0 )
    {
      out.println();
      out.println( "[FixedData]" );
      for ( int i = 0; i < size; i++ )
      {
        out.println( fixedData[ i ]);
      }
    }

    size = deviceButtons.length;
    if ( size > 0 )
    {
      out.println();
      out.println( "[DeviceButtons]" );
      for ( int i = 0; i < size; i++ )
      {
        out.println( deviceButtons[ i ]);
      }
    }

    size = digitMaps.length;
    if ( size > 0 )
    {
      out.println();
      out.println( "[DigitMaps]" );
      for ( int i = 0; i < size; i++ )
      {
        if ( i > 0 )
        {
          if (( i % 16 ) == 0 )
            out.println();
          else
            out.print( ' ' );
        }
        out.print( digitMaps[ i ] );
      }
      out.println();
    }

    size = deviceTypes.size();
    if ( size > 0 )
    {
      out.println();
      out.println( "[DeviceTypes]" );
      int type = 0;
      for ( Enumeration e = deviceTypes.elements(); e.hasMoreElements(); )
      {
        DeviceType devType = ( DeviceType )e.nextElement();
        out.print( devType.getName());
        if ( devType.getMap() != -1 )
        {
          out.print( " = " + devType.getMap());
          if ( devType.getType() != type )
          {
            type = devType.getType();
            out.print( ", $" + Integer.toHexString( type ));
          }
        }
        type += 0x0101;
        out.println();
      }
    }

    boolean hasMultiMacros = false;
    size = buttons.length;
    if ( size > 0 )
    {
      int last = size - 1;
      out.println();
      out.println( "[Buttons]" );
      int code = 1;
      for ( int i = 0; i < size; i++ )
      {
        Button button = buttons[ i ];

        byte newCode = button.getKeyCode();
        if (( i > 0 ) && ( code != newCode ))
            out.println();

        out.print( button.getName() );
        if ( code != newCode )
        {
          code = newCode;
          out.print( "=$" + Integer.toHexString( code ));
        }
        if ( i < last )
          out.print( ", " );
        ++code;

        if ( button.getMultiMacroAddress() != 0 )
          hasMultiMacros = true;
      }
      out.println();

      if ( hasMultiMacros )
      {
        out.println();
        out.println( "[MultiMacros]" );
        for ( int i = 0; i < buttons.length; i++ )
        {
          Button button = buttons[ i ];
          int addr = button.getMultiMacroAddress();
          if ( addr != 0 )
            out.println( button.getName() + "=$" + Integer.toHexString( addr ));
        }
      }
    }

    size = buttonMaps.length;
    if ( size > 0 )
    {
      out.println();
      out.println( "[ButtonMaps]" );
      for ( int i = 0; i < size; i++ )
      {
        ButtonMap map = buttonMaps[ i ];
        out.print( map.getNumber());
        out.print( " = " );
        byte[][] outer = map.getKeyCodeList();
        for ( int j = 0; j < outer.length; j++ )
        {
          if ( j > 0 )
            out.print( ", " );
          byte[] inner = outer[ j ];
          if ( inner.length > 1 )
            out.print( '(' );
          for ( int k = 0; k < inner.length; k++ )
          {
            if ( k > 0 )
              out.print( ", " );
            byte val = inner[ k ];
            if ( val < 0 )
              out.print( "$" + Integer.toHexString( val ).substring( 6 ));
            else
              out.print( inner[ k ]);
          }
          if ( inner.length > 1 )
            out.print( ')' );
        }
        out.println();
      }
    }
  }
  public String toString(){ return name; }
  public String getSignature(){ return signature; }
  public String getName(){ return name; }
  public int getEepromSize()
  {
    checkLoaded();
    return eepromSize;
  }

  public int getDeviceCodeOffset()
  {
    checkLoaded();
    return deviceCodeOffset;
  }

  public Hashtable getDeviceTypes()
  {
    checkLoaded();
    return deviceTypes;
  }

  public DeviceType getDeviceType( String typeName )
  {
    checkLoaded();
    DeviceType devType =( DeviceType )deviceTypes.get( typeName );
    return devType;
  }

  public DeviceType getDeviceTypeByAliasName( String aliasName )
  {
    checkLoaded();
    return ( DeviceType )deviceTypeAliases.get( aliasName );
  }

  public DeviceType getDeviceTypeByIndex( int index )
  {
    for ( Enumeration e = deviceTypes.elements(); e.hasMoreElements(); )
    {
      DeviceType type = ( DeviceType )e.nextElement();
      if ( type.getNumber() == index )
        return type;
    }
    return null;
  }

  public Button[] getButtons()
  {
    checkLoaded();
    return buttons;
  }

  public Button[] getUpgradeButtons()
  {
    checkLoaded();
    return upgradeButtons;
  }

  public String getProcessor()
  {
    checkLoaded();
    return processor;
  }

  public int getRAMAddress()
  {
    checkLoaded();
    return RAMAddress;
  }

  public byte[] getDigitMaps()
  {
    checkLoaded();
    return digitMaps;
  }

  public boolean getOmitDigitMapByte()
  {
    checkLoaded();
    return omitDigitMapByte;
  }

  private String parseGeneralSection( RDFReader rdr )
    throws Exception
  {
    String line = null;
    while ( true )
    {
      line = rdr.readLine();

      if (( line == null ) || ( line.length() == 0 ))
        break;

      StringTokenizer st = new StringTokenizer( line, "=," );

      String parm = st.nextToken();
      if ( parm.equals( "Name" ))
//        name = st.nextToken();
        ;
      else if ( parm.equals( "EepromSize" ))
        eepromSize = rdr.parseNumber( st.nextToken());
      else if ( parm.equals( "DevCodeOffset" ))
        deviceCodeOffset = rdr.parseNumber( st.nextToken());
      else if ( parm.equals( "FavKey" ))
      {
        int keyCode = rdr.parseNumber( st.nextToken( "=, \t" ));
        int deviceButtonAddress = rdr.parseNumber( st.nextToken());
        int maxEntries = rdr.parseNumber( st.nextToken());
        int entrySize = rdr.parseNumber( st.nextToken());
        boolean segregated = false;
        if ( st.hasMoreTokens())
           segregated = ( rdr.parseNumber( st.nextToken()) != 0 );
        favKey = new FavKey( keyCode, deviceButtonAddress, maxEntries, entrySize, segregated );
      }
      else if ( parm.equals( "OEMDevice" ))
      {
        int deviceNumber = rdr.parseNumber( st.nextToken());
        int deviceAddress = rdr.parseNumber( st.nextToken());
        oemDevice = new OEMDevice( deviceNumber, deviceAddress );
      }
      else if ( parm.equals( "OEMControl" ))
        oemControl = rdr.parseNumber( st.nextToken());
      else if ( parm.equals( "UpgradeBug" ))
        upgradeBug = ( rdr.parseNumber( st.nextToken()) != 0 );
      else if ( parm.equals( "AdvCodeAddr" ))
      {
        int start = rdr.parseNumber( st.nextToken( ".=" ));
        int end = rdr.parseNumber( st.nextToken());
        advancedCodeAddress = new AddressRange( start, end );
      }
      else if ( parm.equals( "MacroSupport" ))
        macroSupport = ( rdr.parseNumber( st.nextToken()) != 0 );
      else if ( parm.equals( "UpgradeAddr" ))
      {
        int start = rdr.parseNumber( st.nextToken(".="));
        int end = rdr.parseNumber( st.nextToken());
        upgradeAddress = new AddressRange( start, end );
      }
      else if ( parm.equals( "DevUpgradeAddr" ))
      {
        int start = rdr.parseNumber( st.nextToken( ".=" ));
        int end = rdr.parseNumber( st.nextToken());
        deviceUpgradeAddress = new AddressRange( start, end );
      }
      else if ( parm.equals( "TimedMacroAddr" ))
      {
        int start = rdr.parseNumber( st.nextToken( ".=" ));
        int end = rdr.parseNumber( st.nextToken());
        timedMacroAddress = new AddressRange( start, end );
      }
      else if ( parm.equals( "TimedMacroWarning" ))
        timedMacroWarning = ( rdr.parseNumber( st.nextToken()) != 0 );
      else if ( parm.equals( "LearnedAddr" ))
      {
        int start = rdr.parseNumber( st.nextToken( ".=" ));
        int end = rdr.parseNumber( st.nextToken());
        learnedAddress = new AddressRange( start, end );
      }
      else if ( parm.equals( "Processor" ))
        processor = st.nextToken();
      else if ( parm.equals( "RAMAddr" ))
        RAMAddress = rdr.parseNumber( st.nextToken());
      else if ( parm.equals( "TimeAddr" ))
        timeAddress = rdr.parseNumber( st.nextToken());
      else if ( parm.equals( "RDFSync" ))
        RDFSync = rdr.parseNumber( st.nextToken());
      else if ( parm.equals( "PunchThruBase" ))
        punchThruBase = rdr.parseNumber( st.nextToken());
      else if ( parm.equals( "ScanBase" ))
        scanBase = rdr.parseNumber( st.nextToken());
      else if ( parm.equals( "SleepStatusBit" ))
      {
        int addr = rdr.parseNumber( st.nextToken( ".=" ));
        int bit = rdr.parseNumber( st.nextToken());
        int onVal = 1;
        if ( st.hasMoreTokens())
          onVal = rdr.parseNumber( st.nextToken());
        sleepStatusBit = new StatusBit( addr, bit, onVal );
      }
      else if ( parm.equals( "VPTStatusBit" ))
      {
        int addr = rdr.parseNumber( st.nextToken( ".=" ));
        int bit = rdr.parseNumber( st.nextToken());
        int onVal = 1;
        if ( st.hasMoreTokens())
          onVal = rdr.parseNumber( st.nextToken());
        vptStatusBit = new StatusBit( addr, bit, onVal );
      }
      else if ( parm.equals( "OmitDigitMapByte" ))
        omitDigitMapByte = ( rdr.parseNumber( st.nextToken()) != 0 );
    }
    return line;
  }

  private String parseCheckSums( RDFReader rdr )
    throws Exception
  {
    Vector work = new Vector();
    String line;
    while ( true )
    {
      line = rdr.readLine();
      if (( line == null ) || ( line.length() == 0 ))
        break;

      char ch = line.charAt( 0 );

      line = line.substring( 1 );
      StringTokenizer st = new StringTokenizer( line, ":." );
      int addr = rdr.parseNumber( st.nextToken());
      AddressRange range = new AddressRange( rdr.parseNumber( st.nextToken()),
                                             rdr.parseNumber( st.nextToken()));
      CheckSum sum = null;
      if ( ch == '+' )
        sum = new AddCheckSum( addr, range );
      else
        sum = new XorCheckSum( addr, range );
      work.add( sum );
    }
    checkSums = ( CheckSum[] )work.toArray( checkSums );
    return line;
  }

  private String parseSettings( RDFReader rdr )
    throws Exception
  {
    String line;
    Vector work = new Vector();
    while ( true )
    {
      line = rdr.readLine();

      if (( line == null ) || ( line.length() == 0 ))
        break;

      StringTokenizer st = new StringTokenizer( line, "=" );
      String title = st.nextToken();

      int byteAddress = rdr.parseNumber( st.nextToken( ".= \t" ));
      int bitNumber = rdr.parseNumber( st.nextToken());
      int numberOfBits = rdr.parseNumber( st.nextToken());
      int initialValue = rdr.parseNumber( st.nextToken());
      boolean inverted = ( rdr.parseNumber( st.nextToken()) != 0 );

      Vector options = null;
      String sectionName = null;

      if ( st.hasMoreTokens())
      {
        String token = st.nextToken( ",;)" );
//        while ( token.charAt( 0 ) == ' ' )
//          token = token.substring( 1 );
        if ( token.charAt( 0 ) == '(' )
        {
          options = new Vector();
          options.add( token.substring( 1 ));
          while ( st.hasMoreTokens())
          {
            options.add( st.nextToken());
          }
        }
        else
          sectionName = token;
      }
      String[] optionsList = null;
      if ( options != null )
      {
        optionsList = new String[ options.size()];
        int i = 0;
        for ( Enumeration e = options.elements(); e.hasMoreElements(); i++ )
        {
          optionsList[ i ] = ( String )e.nextElement();
        }
      }
      work.add( new Setting( title, byteAddress, bitNumber,
                                 numberOfBits, initialValue, inverted,
                                 optionsList,
                                 sectionName ));
    }
    settings = ( Setting[] )work.toArray( settings );
    return line;
  }

  private String parseFixedData( RDFReader rdr )
    throws Exception
  {
    Vector work = new Vector();
    Vector temp = new Vector();
    String line;
    int address = -1;
    int value = -1;

    while ( true )
    {
      line = rdr.readLine();

      if (( line == null ) || ( line.length() == 0 ))
        break;

      StringTokenizer st =  new StringTokenizer( line, ",; \t" );
      String token = st.nextToken();
      while( true )
      {
        if ( token.charAt( 0 ) == '=' ) // the last token was an address
        {
          token = token.substring( 1 );
          if ( address != -1 )                       // we've seen some bytes
          {
            byte[] b = new byte[ temp.size()];
            int i = 0;
            for ( Enumeration e = temp.elements();
                  e.hasMoreElements(); ++i )
            {
              b[ i ] = (( Byte )e.nextElement()).byteValue();
            }
            work.add( new FixedData( address, b ));
            temp.clear();
          }
          address = value;
          value = -1;
          if ( token.length() != 0 )
            continue;
        }
        else
        {
          int equal = token.indexOf( '=' );
          String saved = token;
          if ( equal != -1 )
          {
            token = token.substring( 0, equal );
          }
          if ( value != -1 )
          {
            temp.add( new Byte(( byte )value ));
          }
          value = rdr.parseNumber( token );
          if ( equal != -1 )
          {
            token = saved.substring( equal );
            continue;
          }
        }
        if ( !st.hasMoreTokens() )
          break;
        token = st.nextToken();
      }
    }
    temp.add( new Byte(( byte )value ));
    byte[] b = new byte[ temp.size()];
    int j = 0;
    for ( Enumeration en = temp.elements(); en.hasMoreElements(); ++j )
    {
      b[ j ] = (( Byte )en.nextElement()).byteValue();
    }
    work.add( new FixedData( address, b ));
    fixedData = ( FixedData[] )work.toArray( fixedData );
    return line;
  }

  private String parseDeviceButtons( RDFReader rdr )
    throws Exception
  {
    Vector work = new Vector();
    String line;
    while ( true )
    {
      line = rdr.readLine();
      if (( line == null ) || ( line.length() == 0 ))
        break;

      StringTokenizer st = new StringTokenizer( line );
      String name = st.nextToken( "= \t" );

      int hiAddr = rdr.parseNumber( st.nextToken( ",= \t" ));
      int lowAddr = rdr.parseNumber( st.nextToken());
      int typeAddr = 0;
      if ( st.hasMoreTokens())
        typeAddr = rdr.parseNumber( st.nextToken());
      work.add( new DeviceButton( name, hiAddr, lowAddr, typeAddr ));
    }
    deviceButtons = ( DeviceButton[] )work.toArray( deviceButtons );
    return line;
  }

  private String parseDigitMaps( RDFReader rdr )
    throws Exception
  {
    Vector work = new Vector();
    String line;
    while ( true )
    {
      line = rdr.readLine();

      if (( line == null ) || ( line.length() == 0 ))
        break;

      StringTokenizer st = new StringTokenizer( line, ",; \t" );
      while ( st.hasMoreTokens())
      {
        work.add( new Byte(( byte )rdr.parseNumber( st.nextToken())));
      }
    }

    digitMaps = new byte[ work.size()];
    int i = 0;
    for ( Enumeration e = work.elements(); e.hasMoreElements(); ++i )
    {
      digitMaps[ i ] = (( Byte )e.nextElement()).byteValue();
    }
    return line;
  }

  private String parseDeviceTypes( RDFReader rdr )
    throws Exception
  {
    String line;
    int type = 0;
    int count = 0;
    while ( true )
    {
      line = rdr.readLine();
      if (( line == null ) || ( line.length() == 0 ))
        break;

      StringTokenizer st = new StringTokenizer( line, "=, \t" );
      String name = st.nextToken();
      int map = -1;
      if ( st.hasMoreTokens())
      {
        map = rdr.parseNumber( st.nextToken());
        if ( st.hasMoreTokens())
          type = rdr.parseNumber( st.nextToken());
      }
      deviceTypes.put( name, new DeviceType( name, count++, map, type ));
      type += 0x0101;
    }
    return line;
  }

  private String parseDeviceTypeAliases( RDFReader rdr )
    throws Exception
  {
    Vector work = new Vector();
    String line;
    while ( true )
    {
      line = rdr.readLine();
      if (( line == null ) || ( line.length() == 0 ))
        break;

      StringTokenizer st = new StringTokenizer( line, "= \t" );
      String typeName = st.nextToken();
      DeviceType type = getDeviceType( typeName );
      st.nextToken( "=" );
      String rest = st.nextToken().trim();
      st = new StringTokenizer( rest, "," );
      while ( st.hasMoreTokens())
      {
        String aliasName = st.nextToken().trim();
        deviceTypeAliases.put( aliasName, type );
      }
    }
    return line;
  }

  private String parseButtons( RDFReader rdr )
    throws Exception
  {
    Vector work = new Vector();
    String line;
    byte keycode = 1;
    while ( true )
    {
      line = rdr.readLine();
      if ( line == null )
        break;
      if ( line.length() != 0 )
      {
        if (line.charAt( 0 ) == '[')
          break;
        StringTokenizer st = new StringTokenizer( line, "," );
        while ( st.hasMoreTokens())
        {
          String token = st.nextToken().trim();
          int equal = token.indexOf( '=' );
          if ( equal != -1 )
          {
            keycode = ( byte )rdr.parseNumber( token.substring( equal + 1 ));
            token = token.substring( 0, equal );
          }

          int colon = token.indexOf( ':' );
          String name = token;
          if ( colon != -1 )
          {
            name = token.substring( colon + 1 );
            token = token.substring( 0, colon );
          }

          work.add( new Button( token, name, keycode++ ));
        }
      }
    }
    buttons = ( Button[] )work.toArray( buttons );

    buttonsByEfc = new Button[ buttons.length ];
    System.arraycopy( buttons, 0, buttonsByEfc, 0, buttons.length );
    Arrays.sort( buttonsByEfc, efcComparator );

    buttonsByName = new Button[ buttons.length ];
    System.arraycopy( buttons, 0, buttonsByName, 0, buttons.length );
    Arrays.sort( buttonsByName, nameComparator );

    buttonsByStandardName = new Button[ buttons.length ];
    System.arraycopy( buttons, 0, buttonsByStandardName, 0, buttons.length );
    Arrays.sort( buttonsByName, standardNameComparator );

    return line;
  }

  private String parseMultiMacros( RDFReader rdr )
    throws Exception
  {
    String line;
    while ( true )
    {
      line = rdr.readLine();
      if (( line == null ) || ( line.length() == 0 ))
        break;


      StringTokenizer st = new StringTokenizer( line, "=" );
      String name = st.nextToken();

      // Find the matching button
      for ( int i = 0; i < buttons.length; i++ )
      {
        Button button = buttons[ i ];
        if ( button.getName().equals( name ))
        {
          button.setMultiMacroAddress( rdr.parseNumber( st.nextToken()));
          break;
        }
      }
    }
    return line;
  }

  public Button findByEfc( Button b )
  {
    checkLoaded();
    Button rc = null;
    int i = Arrays.binarySearch( buttonsByEfc, b, efcComparator );
    if ( i >= 0 )
      rc = buttonsByEfc[ i ];
    return rc;
  }

  public Button findByName( Button b )
  {
    checkLoaded();
    Button rc = null;
    int i = Arrays.binarySearch( buttonsByName, b, nameComparator );
    if ( i >= 0 )
      rc = buttonsByName[ i ];
    return rc;
  }


  public Button findByStandardName( Button b )
  {
    checkLoaded();
    Button rc = null;
    int i = Arrays.binarySearch( buttonsByStandardName, b, standardNameComparator );
    if ( i >= 0 )
      rc = buttonsByStandardName[ i ];
    return rc;
  }


  private String parseButtonMaps( RDFReader rdr )
    throws Exception
  {
    Vector work = new Vector();
    String line;
    ButtonMap map = null;
    int name = -1;
    Vector outer = new Vector();
    Vector inner = null;
    boolean nested = false;

    while ( true )
    {
      line = rdr.readLine();
      if (( line == null ) || ( line.length() == 0 ))
        break;

      StringTokenizer st = new StringTokenizer( line, "=, \t" );
      if ( line.indexOf( '=' ) != -1 )
      {
        if ( name != -1 )
        {
          byte[][] outerb = new byte[ outer.size()][];
          int o = 0;
          for ( Enumeration oe = outer.elements(); oe.hasMoreElements(); o++ )
          {
            inner = ( Vector )oe.nextElement();
            byte[] innerb = new byte[ inner.size()];
            outerb[ o ] = innerb;
            int i = 0;
            for ( Enumeration ie = inner.elements(); ie.hasMoreElements(); i++ )
            {
              innerb[ i ] = ( byte )(( Byte )ie.nextElement()).byteValue();
            }
            inner.clear();
          }
          outer.clear();
          work.add( new ButtonMap( name, outerb ));
        }
        name = rdr.parseNumber( st.nextToken());
      }

      while ( st.hasMoreTokens())
      {
        String token = st.nextToken();
        if ( token.charAt( 0 ) == '(' ) // it's a list
        {
          nested = true;
          token = token.substring( 1 );
          inner = new Vector();
          outer.add( inner );
        }

        if ( !nested )
        {
          inner = new Vector();
          outer.add( inner );
        }

        int closeParen = token.indexOf( ')' );
        if ( closeParen != -1 )
        {
          nested = false;
          token = token.substring( 0, closeParen );
        }

        inner.add( new Byte(( byte )rdr.parseNumber( token )));
      }
    }
    {
      byte[][] outerb = new byte[ outer.size()][];
      int o = 0;
      for ( Enumeration oe = outer.elements(); oe.hasMoreElements(); o++ )
      {
        inner = ( Vector )oe.nextElement();
        byte[] innerb = new byte[ inner.size()];
        outerb[ o ] = innerb;
        int i = 0;
        for ( Enumeration ie = inner.elements(); ie.hasMoreElements(); i++ )
        {
          innerb[ i ] = ( byte )(( Byte )ie.nextElement()).byteValue();
        }
        inner.clear();
      }
      outer.clear();
      work.add( new ButtonMap( name, outerb ));
    }
    buttonMaps = ( ButtonMap[] )work.toArray( buttonMaps );
    return line;
  }

  private String parseProtocols( RDFReader rdr )
    throws Exception
  {
    String line;
    while ( true )
    {
      line = rdr.readLine();
      if ( line == null )
        break;
      if ( line.length() != 0 )
      {
        if (line.charAt( 0 ) == '[')
          break;
        StringTokenizer st = new StringTokenizer( line, "," );
        while ( st.hasMoreTokens())
        {
          String token = st.nextToken().trim();
          String variantName = "";
          int colon = token.indexOf( ':' );
          String name = token;
          if ( colon != -1 )
          {
            variantName = token.substring( colon + 1 );
            token = token.substring( 0, colon );
          }
          Hex pid = new Hex( token );
          protocolVariantNames.put( pid, variantName );
        }
      }
    }
    return line;
  }

  public String getSupportedVariantName( Hex pid )
  {
    return ( String )protocolVariantNames.get( pid );
  }


  public void clearButtonAssignments()
  {
    checkLoaded();
    for ( int i = 0; i < buttons.length; i++ )
    {
      buttons[ i ].setFunction( null ).setShiftedFunction( null );
    }
  }

  // Interface Comparable
  public int compareTo( Object o )
  {
    return name.compareTo( o.toString());
  }

  private Comparator nameComparator = new Comparator()
  {
    public int compare( Object o1, Object o2 )
    {
      return (( Button )o1 ).getName().compareToIgnoreCase((( Button )o2 ).getName());
    }

    public boolean equals( Object o )
    {
      return ( this == o );
    }
  };

  private Comparator standardNameComparator = new Comparator()
  {
    public int compare( Object o1, Object o2 )
    {
      return (( Button )o1 ).getStandardName().compareToIgnoreCase((( Button )o2 ).getStandardName());
    }

    public boolean equals( Object o )
    {
      return ( this == o );
    }
  };

  private Comparator efcComparator = new Comparator()
  {
    public int compare( Object o1, Object o2 )
    {
      int key1 = (( Button )o1 ).getKeyCode();
      int key2 = (( Button )o2 ).getKeyCode();

      int rc = 0;
      if ( key1 < key2 )
        rc = -1;
      else if ( key1 == key2 )
        rc = 0;
      else
        rc = 1;
      return rc;
    }

    public boolean equals( Object o )
    {
      return ( this == o );
    }
  };

  private File file = null;
  private String signature = null;
  private String name = null;
  private boolean loaded = false;
  private int eepromSize;
  private int deviceCodeOffset;
  private FavKey favKey = null;
  private OEMDevice oemDevice = null;
  private int oemControl = 0;
  private boolean upgradeBug = false;
  private AddressRange advancedCodeAddress = null;
  private boolean macroSupport = true;
  private AddressRange upgradeAddress = null;
  private AddressRange deviceUpgradeAddress = null;
  private AddressRange timedMacroAddress = null;
  private boolean timedMacroWarning = false;
  private AddressRange learnedAddress = null;
  private String processor = null;
  private int RAMAddress;
  private int timeAddress = 0;
  private int RDFSync;
  private int punchThruBase;
  private int scanBase = 0;
  private StatusBit sleepStatusBit = null;
  private StatusBit vptStatusBit = null;
  private CheckSum[] checkSums = new CheckSum[ 0 ];
  private Setting[] settings = new Setting[ 0 ];
  private FixedData[] fixedData = new FixedData[ 0 ];
  private DeviceButton[] deviceButtons = new DeviceButton[ 0 ];
  private Hashtable deviceTypes = new Hashtable();
  private Hashtable deviceTypeAliases = new Hashtable();
  private Button[] buttons = new Button[ 0 ];
  private Button[] buttonsByEfc = null;
  private Button[] buttonsByName = null;
  private Button[] buttonsByStandardName = null;
  private Button[] upgradeButtons = null;
  private byte[] digitMaps = new byte[ 0 ];
  private ButtonMap[] buttonMaps = new ButtonMap[ 0 ];
  private boolean omitDigitMapByte = false;
  private Hashtable protocolVariantNames = new Hashtable();
}
