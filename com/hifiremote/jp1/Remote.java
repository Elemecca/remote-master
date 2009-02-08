package com.hifiremote.jp1;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

// TODO: Auto-generated Javadoc
/**
 * The Class Remote.
 */
public class Remote implements Comparable< Remote >
{

  /**
   * Instantiates a new remote.
   * 
   * @param aRemote
   *          the a remote
   * @param index
   *          the index
   */
  public Remote( Remote aRemote, int index )
  {
    this.file = aRemote.file;
    this.signature = aRemote.signature;
    supportsBinaryUpgrades = aRemote.supportsBinaryUpgrades;
    this.names = aRemote.names;
    nameIndex = index;
  }

  /**
   * Instantiates a new remote.
   * 
   * @param rdf
   *          the rdf
   */
  public Remote( File rdf )
  {
    file = rdf;
    String rdfName = rdf.getName();
    StringTokenizer st = new StringTokenizer( rdfName );
    signature = st.nextToken(); // upto the 1st space
    supportsBinaryUpgrades = signature.startsWith( "BIN" );
    int openParen = rdfName.indexOf( '(' );
    int closeParen = rdfName.lastIndexOf( ')' );
    String name = rdfName.substring( openParen + 1, closeParen );
    st = new StringTokenizer( name, " -", true );
    String prefix = "";
    String postfix = "";
    String[] middles = null;
    boolean foundUnderscore = false;
    while ( st.hasMoreTokens() )
    {
      String token = st.nextToken();
      if ( ( token.length() > 3 ) && ( token.indexOf( '_' ) != -1 ) )
      {
        foundUnderscore = true;
        StringTokenizer st2 = new StringTokenizer( token, "_" );
        middles = new String[ st2.countTokens() ];
        for ( int i = 0; i < middles.length; i++ )
          middles[ i ] = st2.nextToken();
      }
      else
      {
        token = token.replace( '_', '/' );
        if ( foundUnderscore )
          postfix = postfix + token;
        else
          prefix = prefix + token;
      }
    }
    if ( middles == null )
    {
      names[ 0 ] = prefix;
    }
    else
    {
      names = new String[ middles.length ];
      for ( int i = 0; i < middles.length; i++ )
      {
        if ( middles[ i ].length() < middles[ 0 ].length() )
          names[ i ] = middles[ i ] + postfix;
        else
          names[ i ] = prefix + middles[ i ] + postfix;
      }
    }
  }

  /**
   * Gets the file.
   * 
   * @return the file
   */
  public File getFile()
  {
    return file;
  }

  /**
   * Load.
   */
  public void load()
  // throws Exception
  {
    try
    {
      if ( loaded )
        return;
      loaded = true;
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

          if ( line.equals( "General" ) )
            line = parseGeneralSection( rdr );
          else if ( line.equals( "SpecialProtocols" ) )
            line = parseSpecialProtocols( rdr );
          else if ( line.equals( "Checksums" ) )
            line = parseCheckSums( rdr );
          else if ( line.equals( "Settings" ) )
            line = parseSettings( rdr );
          else if ( line.equals( "FixedData" ) )
            line = parseFixedData( rdr );
          else if ( line.equals( "DeviceButtons" ) )
            line = parseDeviceButtons( rdr );
          else if ( line.equals( "DigitMaps" ) )
            line = parseDigitMaps( rdr );
          else if ( line.equals( "DeviceTypes" ) )
            line = parseDeviceTypes( rdr );
          else if ( line.equals( "DeviceAbbreviations" ) )
            line = parseDeviceAbbreviations( rdr );
          else if ( line.equals( "DeviceTypeAliases" ) )
            line = parseDeviceTypeAliases( rdr );
          else if ( line.equals( "DeviceTypeImageMaps" ) )
            line = parseDeviceTypeImageMaps( rdr );
          else if ( line.equals( "Buttons" ) )
            line = parseButtons( rdr );
          else if ( line.equals( "MultiMacros" ) )
            line = parseMultiMacros( rdr );
          else if ( line.equals( "ButtonMaps" ) )
            line = parseButtonMaps( rdr );
          else if ( line.equals( "Protocols" ) )
            line = parseProtocols( rdr );
          else
            line = rdr.readLine();
        }
        else
          line = rdr.readLine();
      }
      rdr.close();

      if ( buttonMaps.length == 0 )
      {
        System.err.println( "ERROR: " + file.getName() + " does not specify any ButtonMaps!" );
        buttonMaps = new ButtonMap[ 1 ];
        buttonMaps[ 0 ] = new ButtonMap( 0, new short[ 0 ][ 0 ] );
      }
      for ( int i = 0; i < buttonMaps.length; i++ )
        buttonMaps[ i ].setButtons( this );

      for ( Enumeration< DeviceType > e = deviceTypes.elements(); e.hasMoreElements(); )
      {
        DeviceType type = e.nextElement();
        int map = type.getMap();
        if ( map == -1 )
          System.err.println( "ERROR:" + file.getName() + ": DeviceType " + type.getName()
              + " doesn't have a map." );
        if ( map >= buttonMaps.length )
        {
          System.err.println( "ERROR:" + file.getName() + ": DeviceType " + type.getName()
              + " uses an undefined map index." );
          map = buttonMaps.length - 1;
        }
        if ( ( map != -1 ) && ( buttonMaps.length > 0 ) )
          type.setButtonMap( buttonMaps[ map ] );
      }

      if ( deviceTypeAliasNames == null )
      {
        java.util.List< String > v = new ArrayList< String >();
        DeviceType vcrType = null;
        boolean hasPVRalias = false;
        for ( Enumeration< DeviceType > e = deviceTypes.elements(); e.hasMoreElements(); )
        {
          DeviceType type = e.nextElement();

          String typeName = type.getName();
          if ( typeName.startsWith( "VCR" ) )
            vcrType = type;
          if ( typeName.equals( "PVR" ) )
            hasPVRalias = true;
          deviceTypeAliases.put( typeName, type );
          v.add( typeName );
        }
        if ( !hasPVRalias )
        {
          v.add( "PVR" );
          deviceTypeAliases.put( "PVR", vcrType );
        }
        deviceTypeAliasNames = new String[ 0 ];
        deviceTypeAliasNames = ( String[] ) v.toArray( deviceTypeAliasNames );
        Arrays.sort( deviceTypeAliasNames );
      }

      // find the longest button map
      ButtonMap longestMap = null;
      for ( Enumeration< DeviceType > e = deviceTypes.elements(); e.hasMoreElements(); )
      {
        DeviceType type = e.nextElement();
        ButtonMap thisMap = type.getButtonMap();
        if ( ( longestMap == null ) || ( longestMap.size() < thisMap.size() ) )
          longestMap = thisMap;
      }

      // Now figure out which buttons are bindable
      java.util.List< Button > bindableButtons = new ArrayList< Button >();

      // first copy the bindable buttons from the longest map
      int index = 0;
      while ( index < longestMap.size() )
      {
        Button b = longestMap.get( index++ );
        if ( b.allowsKeyMove() || b.allowsShiftedKeyMove() || b.allowsXShiftedKeyMove() )
          bindableButtons.add( b );
      }

      // now copy the rest of the bindable buttons, skipping those already added
      for ( Button b : buttons )
      {
        if ( ( b.allowsKeyMove() || b.allowsShiftedKeyMove() || b.allowsXShiftedKeyMove() )
            && !bindableButtons.contains( b ) )
          bindableButtons.add( b );
      }
      upgradeButtons = ( Button[] ) bindableButtons.toArray( upgradeButtons );

      if ( ( imageMaps.length > 0 ) && ( imageMaps[ mapIndex ] != null ) )
        imageMaps[ mapIndex ].parse( this );

      for ( Enumeration< DeviceType > e = deviceTypes.elements(); e.hasMoreElements(); )
      {
        DeviceType type = e.nextElement();
        ImageMap[][] maps = type.getImageMaps();
        if ( maps.length > 0 )
        {
          ImageMap[] a = maps[ mapIndex ];
          for ( int i = 0; i < a.length; ++i )
            a[ i ].parse( this );
        }
      }

      setPhantomShapes();

      loaded = true;
    }
    catch ( Exception e )
    {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter( sw );
      e.printStackTrace( pw );
      pw.flush();
      pw.close();
      JOptionPane.showMessageDialog( RemoteMaster.getFrame(), sw.toString(), "Remote Load Error",
          JOptionPane.ERROR_MESSAGE );
      System.err.println( sw.toString() );
    }
  }

  /**
   * Sets the phantom shapes.
   */
  private void setPhantomShapes()
  {
    double radius = 8;
    double gap = 6;

    double diameter = 2 * radius;
    double x = gap;
    java.util.List< ImageMap > maps = new ArrayList< ImageMap >();
    if ( imageMaps.length > 0 )
      maps.add( imageMaps[ mapIndex ] );
    for ( Enumeration< DeviceType > e = deviceTypes.elements(); e.hasMoreElements(); )
    {
      DeviceType type = e.nextElement();
      if ( type.getImageMaps().length == 0 )
        continue;
      ImageMap[] devMaps = type.getImageMaps()[ mapIndex ];
      for ( int i = 0; i < devMaps.length; ++i )
        maps.add( devMaps[ i ] );
    }

    for ( ImageMap map : maps )
    {
      int h = map.getImage().getIconHeight();
      int w = map.getImage().getIconWidth();
      if ( h > height )
        height = h;
      if ( w > width )
        width = w;
    }
    double y = height + gap;

    for ( int i = 0; i < upgradeButtons.length; i++ )
    {
      Button b = upgradeButtons[ i ];
      if ( !b.getHasShape() && !b.getIsShifted() && !b.getIsXShifted() )
      {
        if ( ( x + diameter + gap ) > width )
        {
          x = gap;
          y += ( gap + diameter );
        }
        Shape shape = new Ellipse2D.Double( x, y, diameter, diameter );
        x += ( diameter + gap );
        ButtonShape buttonShape = new ButtonShape( shape, b );
        phantomShapes.add( buttonShape );
        b.setHasShape( true );
      }
    }
    height = ( int ) ( y + gap + diameter );
    for ( ImageMap map : maps )
    {
      map.getShapes().addAll( phantomShapes );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return names[ nameIndex ];
  }

  /**
   * Gets the signature.
   * 
   * @return the signature
   */
  public String getSignature()
  {
    return signature;
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName()
  {
    return names[ nameIndex ];
  }

  /**
   * Gets the name count.
   * 
   * @return the name count
   */
  public int getNameCount()
  {
    return names.length;
  }

  /**
   * Gets the base address.
   * 
   * @return the base address
   */
  public int getBaseAddress()
  {
    return baseAddress;
  }

  /**
   * Gets the eeprom size.
   * 
   * @return the eeprom size
   */
  public int getEepromSize()
  {
    return eepromSize;
  }

  /**
   * Gets the device code offset.
   * 
   * @return the device code offset
   */
  public int getDeviceCodeOffset()
  {
    return deviceCodeOffset;
  }

  /**
   * Gets the device types.
   * 
   * @return the device types
   */
  public DeviceType[] getDeviceTypes()
  {
    DeviceType[] types = new DeviceType[ deviceTypes.size() ];
    for ( Enumeration< DeviceType > e = deviceTypes.elements(); e.hasMoreElements(); )
    {
      DeviceType type = e.nextElement();
      types[ type.getNumber() ] = type;
    }
    return types;
  }

  /**
   * Gets the device type.
   * 
   * @param typeName
   *          the type name
   * 
   * @return the device type
   */
  public DeviceType getDeviceType( String typeName )
  {
    DeviceType devType = deviceTypes.get( typeName );
    return devType;
  }

  /**
   * Gets the device type by alias name.
   * 
   * @param aliasName
   *          the alias name
   * 
   * @return the device type by alias name
   */
  public DeviceType getDeviceTypeByAliasName( String aliasName )
  {
    DeviceType type = ( DeviceType ) deviceTypeAliases.get( aliasName );
    if ( type != null )
      return type;
    return getDeviceType( aliasName );
  }

  /**
   * Gets the device type by index.
   * 
   * @param index
   *          the index
   * 
   * @return the device type by index
   */
  public DeviceType getDeviceTypeByIndex( int index )
  {
    for ( Enumeration< DeviceType > e = deviceTypes.elements(); e.hasMoreElements(); )
    {
      DeviceType type = e.nextElement();
      if ( type.getNumber() == index )
        return type;
    }
    return null;
  }

  /**
   * Gets the device type alias.
   * 
   * @param type
   *          the type
   * 
   * @return the device type alias
   */
  public String getDeviceTypeAlias( DeviceType type )
  {
    String tentative = null;
    for ( String alias : deviceTypeAliasNames )
    {
      if ( getDeviceTypeByAliasName( alias ) != type )
        continue;
      String typeName = type.getName();
      if ( typeName.equals( alias ) )
        return alias;
      if ( ( typeName.contains( alias ) || alias.contains( typeName ) ) && ( tentative == null ) )
        tentative = alias;
    }
    if ( tentative != null )
      return tentative;
    for ( String alias : deviceTypeAliasNames )
      if ( getDeviceTypeByAliasName( alias ) == type )
      {
        tentative = alias;
        break;
      }
    return tentative;
  }

  /**
   * Gets the device buttons.
   * 
   * @return the device buttons
   */
  public DeviceButton[] getDeviceButtons()
  {
    load();
    return deviceButtons;
  }

  /**
   * Gets the buttons.
   * 
   * @return the buttons
   */
  public java.util.List< Button > getButtons()
  {
    load();
    return buttons;
  }

  /**
   * Gets the upgrade buttons.
   * 
   * @return the upgrade buttons
   */
  public Button[] getUpgradeButtons()
  {
    load();
    return upgradeButtons;
  }

  /**
   * Gets the phantom shapes.
   * 
   * @return the phantom shapes
   */
  public java.util.List< ButtonShape > getPhantomShapes()
  {
    load();
    return phantomShapes;
  }

  /**
   * Gets the processor.
   * 
   * @return the processor
   */
  public Processor getProcessor()
  {
    load();
    return processor;
  }

  // public String getProcessorVersion()
  // {
  // return processorVersion;
  // }

  /**
   * Gets the rAM address.
   * 
   * @return the rAM address
   */
  public int getRAMAddress()
  {
    load();
    return RAMAddress;
  }

  /**
   * Gets the digit maps.
   * 
   * @return the digit maps
   */
  public short[] getDigitMaps()
  {
    load();
    return digitMaps;
  }

  /**
   * Gets the omit digit map byte.
   * 
   * @return the omit digit map byte
   */
  public boolean getOmitDigitMapByte()
  {
    load();
    return omitDigitMapByte;
  }

  /**
   * Gets the image maps.
   * 
   * @param type
   *          the type
   * 
   * @return the image maps
   */
  public ImageMap[] getImageMaps( DeviceType type )
  {
    load();
    ImageMap[][] maps = type.getImageMaps();
    if ( ( maps != null ) && ( maps.length != 0 ) )
      return maps[ mapIndex ];
    else
    {
      ImageMap[] rc = new ImageMap[ 1 ];
      rc[ 0 ] = imageMaps[ mapIndex ];
      return rc;
    }
  }

  /**
   * Gets the adv code format.
   * 
   * @return the adv code format
   */
  public int getAdvCodeFormat()
  {
    load();
    return advCodeFormat;
  }

  /**
   * Gets the adv code bind format.
   * 
   * @return the adv code bind format
   */
  public int getAdvCodeBindFormat()
  {
    load();
    return advCodeBindFormat;
  }

  /**
   * Gets the eFC digits.
   * 
   * @return the eFC digits
   */
  public int getEFCDigits()
  {
    load();
    return efcDigits;
  }

  /**
   * Parses the flag.
   * 
   * @param st
   *          the st
   * 
   * @return true, if successful
   */
  private boolean parseFlag( StringTokenizer st )
  {
    String flag = st.nextToken( " =\t" );
    if ( flag.equalsIgnoreCase( "Y" ) || flag.equalsIgnoreCase( "Yes" )
        || flag.equalsIgnoreCase( "T" ) || flag.equalsIgnoreCase( "True" )
        || flag.equalsIgnoreCase( "1" ) )
    {
      return true;
    }
    return false;
  }

  /**
   * Parses the general section.
   * 
   * @param rdr
   *          the rdr
   * 
   * @return the string
   * 
   * @throws Exception
   *           the exception
   */
  private String parseGeneralSection( RDFReader rdr ) throws Exception
  {
    String processorName = null;
    String processorVersion = null;
    String line = null;
    while ( true )
    {
      line = rdr.readLine();

      if ( ( line == null ) || ( line.length() == 0 ) )
        break;

      StringTokenizer st = new StringTokenizer( line, "=" );

      String parm = st.nextToken();
      if ( parm.equals( "Name" ) )
        ;
      else if ( parm.equals( "BaseAddr" ) )
        baseAddress = rdr.parseNumber( st.nextToken() );
      else if ( parm.equals( "EepromSize" ) )
        eepromSize = rdr.parseNumber( st.nextToken() );
      else if ( parm.equals( "DevCodeOffset" ) )
        deviceCodeOffset = rdr.parseNumber( st.nextToken() );
      else if ( parm.equals( "FavKey" ) )
      {
        int keyCode = rdr.parseNumber( st.nextToken( "=, \t" ) );
        int deviceButtonAddress = rdr.parseNumber( st.nextToken() );
        int maxEntries = rdr.parseNumber( st.nextToken() );
        int entrySize = rdr.parseNumber( st.nextToken() );
        boolean segregated = false;
        if ( st.hasMoreTokens() )
          segregated = rdr.parseNumber( st.nextToken() ) != 0;
        favKey = new FavKey( keyCode, deviceButtonAddress, maxEntries, entrySize, segregated );
      }
      else if ( parm.equals( "OEMDevice" ) )
      {
        int deviceNumber = rdr.parseNumber( st.nextToken( ",=" ) );
        int deviceAddress = rdr.parseNumber( st.nextToken() );
        oemDevice = new OEMDevice( deviceNumber, deviceAddress );
      }
      else if ( parm.equals( "OEMControl" ) )
        oemControl = rdr.parseNumber( st.nextToken() );
      else if ( parm.equals( "UpgradeBug" ) )
        upgradeBug = parseFlag( st );
      else if ( parm.equals( "AdvCodeAddr" ) )
      {
        int start = rdr.parseNumber( st.nextToken( ".=" ) );
        int end = rdr.parseNumber( st.nextToken() );
        advancedCodeAddress = new AddressRange( start, end );
      }
      else if ( parm.equals( "MacroSupport" ) )
        macroSupport = ( rdr.parseNumber( st.nextToken() ) != 0 );
      else if ( parm.equals( "UpgradeAddr" ) )
      {
        int start = rdr.parseNumber( st.nextToken( ".=" ) );
        int end = rdr.parseNumber( st.nextToken() );
        upgradeAddress = new AddressRange( start, end );
      }
      else if ( parm.equals( "DevUpgradeAddr" ) )
      {
        int start = rdr.parseNumber( st.nextToken( ".=" ) );
        int end = rdr.parseNumber( st.nextToken() );
        deviceUpgradeAddress = new AddressRange( start, end );
      }
      else if ( parm.equals( "TimedMacroAddr" ) )
      {
        int start = rdr.parseNumber( st.nextToken( ".=" ) );
        int end = rdr.parseNumber( st.nextToken() );
        timedMacroAddress = new AddressRange( start, end );
      }
      else if ( parm.equals( "TimedMacroWarning" ) )
        timedMacroWarning = parseFlag( st );
      else if ( parm.equals( "LearnedAddr" ) )
      {
        int start = rdr.parseNumber( st.nextToken( ".=" ) );
        int end = rdr.parseNumber( st.nextToken() );
        learnedAddress = new AddressRange( start, end );
      }
      else if ( parm.equals( "Processor" ) )
      {
        processorName = st.nextToken();
        if ( processorName.equals( "6805" ) && ( processorVersion == null ) )
          processorVersion = "C9";
      }
      else if ( parm.equals( "ProcessorVersion" ) )
        processorVersion = st.nextToken();
      else if ( parm.equals( "RAMAddr" ) )
        RAMAddress = rdr.parseNumber( st.nextToken() );
      else if ( parm.equals( "TimeAddr" ) )
        timeAddress = rdr.parseNumber( st.nextToken() );
      else if ( parm.equals( "RDFSync" ) )
        RDFSync = rdr.parseNumber( st.nextToken() );
      else if ( parm.equals( "PunchThruBase" ) )
        punchThruBase = rdr.parseNumber( st.nextToken() );
      else if ( parm.equals( "ScanBase" ) )
        scanBase = rdr.parseNumber( st.nextToken() );
      else if ( parm.equals( "SleepStatusBit" ) )
      {
        int addr = rdr.parseNumber( st.nextToken( ".=" ) );
        int bit = rdr.parseNumber( st.nextToken() );
        int onVal = 1;
        if ( st.hasMoreTokens() )
          onVal = rdr.parseNumber( st.nextToken() );
        sleepStatusBit = new StatusBit( addr, bit, onVal );
      }
      else if ( parm.equals( "VPTStatusBit" ) )
      {
        int addr = rdr.parseNumber( st.nextToken( ".=" ) );
        int bit = rdr.parseNumber( st.nextToken() );
        int onVal = 1;
        if ( st.hasMoreTokens() )
          onVal = rdr.parseNumber( st.nextToken() );
        vptStatusBit = new StatusBit( addr, bit, onVal );
      }
      else if ( parm.equals( "OmitDigitMapByte" ) )
        omitDigitMapByte = parseFlag( st );
      else if ( parm.equals( "ImageMap" ) )
      {
        PropertyFile properties = JP1Frame.getProperties();
        File imageDir = properties.getFileProperty( "ImagePath" );
        if ( imageDir == null )
          imageDir = new File( properties.getFile().getParentFile(), "Images" );

        if ( !imageDir.exists() )
        {
          JOptionPane.showMessageDialog( null, "Images folder not found!", "Error",
              JOptionPane.ERROR_MESSAGE );
          RMFileChooser chooser = new RMFileChooser( imageDir.getParentFile() );
          chooser.setFileSelectionMode( RMFileChooser.DIRECTORIES_ONLY );
          chooser.setDialogTitle( "Choose the directory containing the remote images and maps" );
          if ( chooser.showOpenDialog( null ) != RMFileChooser.APPROVE_OPTION )
            System.exit( -1 );

          imageDir = chooser.getSelectedFile();
          properties.setProperty( "ImagePath", imageDir );
        }

        String mapList = st.nextToken();
        StringTokenizer mapTokenizer = new StringTokenizer( mapList, "," );
        int mapCount = mapTokenizer.countTokens();
        imageMaps = new ImageMap[ mapCount ];
        for ( int m = 0; m < mapCount; ++m )
          imageMaps[ m ] = new ImageMap( new File( imageDir, mapTokenizer.nextToken() ) );

        if ( nameIndex >= mapCount )
          mapIndex = mapCount - 1;
        else
          mapIndex = nameIndex;
      }
      else if ( parm.equals( "DefaultRestrictions" ) )
        defaultRestrictions = parseRestrictions( st.nextToken() );
      else if ( parm.equals( "Shift" ) )
      {
        shiftMask = rdr.parseNumber( st.nextToken( "=," ) );
        if ( st.hasMoreTokens() )
          shiftLabel = st.nextToken().trim();
      }
      else if ( parm.equals( "XShift" ) )
      {
        xShiftEnabled = true;
        xShiftMask = rdr.parseNumber( st.nextToken( "=," ) );
        if ( st.hasMoreTokens() )
          xShiftLabel = st.nextToken().trim();
      }
      else if ( parm.equals( "AdvCodeFormat" ) )
      {
        String value = st.nextToken();
        if ( value.equals( "HEX" ) )
          advCodeFormat = HEX_FORMAT;
        else if ( value.equals( "EFC" ) )
          advCodeFormat = EFC_FORMAT;
      }
      else if ( parm.equals( "AdvCodeBindFormat" ) )
      {
        String value = st.nextToken();
        if ( value.equals( "NORMAL" ) )
          advCodeBindFormat = NORMAL;
        else if ( value.equals( "LONG" ) )
          advCodeBindFormat = LONG;
      }
      else if ( parm.equals( "EFCDigits" ) )
      {
        String value = st.nextToken();
        efcDigits = rdr.parseNumber( value );
      }
      else if ( parm.equals( "DevComb" ) )
      {
        devCombAddress = new int[ 7 ];
        String combParms = st.nextToken();
        StringTokenizer st2 = new StringTokenizer( combParms, ",", true );
        for ( int i = 0; i < 7; i++ )
        {
          if ( st2.hasMoreTokens() )
          {
            String tok = st2.nextToken();
            if ( tok.equals( "," ) )
              devCombAddress[ i ] = -1;
            else
            {
              devCombAddress[ i ] = rdr.parseNumber( tok );
              if ( st2.hasMoreTokens() )
                st2.nextToken(); // skip delimeter
            }
          }
          else
            devCombAddress[ i ] = -1;
        }
      }
      else if ( parm.equals( "ProtocolVectorOffset" ) )
        protocolVectorOffset = rdr.parseNumber( st.nextToken() );
      else if ( parm.equals( "ProtocolDataOffset" ) )
        protocolDataOffset = rdr.parseNumber( st.nextToken() );
      else if ( parm.equals( "EncDec" ) )
      {
        String className = st.nextToken( "=()" );
        String textParm = null;
        if ( st.hasMoreTokens() )
          textParm = st.nextToken();
        try
        {
          if ( className.indexOf( '.' ) == -1 )
            className = "com.hifiremote.jp1." + className;

          Class< ? > cl = Class.forName( className );
          Class< ? extends EncrypterDecrypter > cl2 = cl.asSubclass( EncrypterDecrypter.class );
          Class< ? >[] parmClasses =
          { String.class };
          Constructor< ? extends EncrypterDecrypter > ct = cl2.getConstructor( parmClasses );
          Object[] ctParms =
          { textParm };
          encdec = ct.newInstance( ctParms );
        }
        catch ( Exception e )
        {
          System.err.println( "Error creating an instance of " + className );
          e.printStackTrace( System.err );
        }
      }
      else if ( parm.equals( "MaxUpgradeLength" ) )
        maxUpgradeLength = new Integer( rdr.parseNumber( st.nextToken() ) );
      else if ( parm.equals( "MaxProtocolLength" ) )
        maxProtocolLength = new Integer( rdr.parseNumber( st.nextToken() ) );
      else if ( parm.equals( "MaxCombinedUpgradeLength" ) )
        maxCombinedUpgradeLength = new Integer( rdr.parseNumber( st.nextToken() ) );
      else if ( parm.equals( "SectionTerminator" ) )
        sectionTerminator = ( short ) rdr.parseNumber( st.nextToken() );
      else if ( parm.equalsIgnoreCase( "2BytePid" ) )
        twoBytePID = parseFlag( st );
      else if ( parm.equalsIgnoreCase( "LearnedDevBtnSwapped" ) )
        learnedDevBtnSwapped = parseFlag( st );
    }
    processor = ProcessorManager.getProcessor( processorName, processorVersion );
    return line;
  }

  /**
   * Gets the dev comb addresses.
   * 
   * @return the dev comb addresses
   */
  public int[] getDevCombAddresses()
  {
    load();
    return devCombAddress;
  }

  /**
   * Parses the restrictions.
   * 
   * @param str
   *          the str
   * 
   * @return the int
   */
  private int parseRestrictions( String str )
  {
    int rc = 0;
    if ( restrictionTable == null )
    {
      restrictionTable = new Hashtable< String, Integer >( 46 );
      restrictionTable.put( "MoveBind", new Integer( Button.MOVE_BIND ) );
      restrictionTable.put( "ShiftMoveBind", new Integer( Button.SHIFT_MOVE_BIND ) );
      restrictionTable.put( "XShiftMoveBind", new Integer( Button.XSHIFT_MOVE_BIND ) );
      restrictionTable.put( "AllMoveBind", new Integer( Button.ALL_MOVE_BIND ) );
      restrictionTable.put( "MacroBind", new Integer( Button.MACRO_BIND ) );
      restrictionTable.put( "ShiftMacroBind", new Integer( Button.SHIFT_MACRO_BIND ) );
      restrictionTable.put( "XShiftMacroBind", new Integer( Button.XSHIFT_MACRO_BIND ) );
      restrictionTable.put( "AllMacroBind", new Integer( Button.ALL_MACRO_BIND ) );
      restrictionTable.put( "LearnBind", new Integer( Button.LEARN_BIND ) );
      restrictionTable.put( "ShiftLearnBind", new Integer( Button.SHIFT_LEARN_BIND ) );
      restrictionTable.put( "XShiftLearnBind", new Integer( Button.XSHIFT_LEARN_BIND ) );
      restrictionTable.put( "AllLearnBind", new Integer( Button.ALL_LEARN_BIND ) );
      restrictionTable.put( "MacroData", new Integer( Button.MACRO_DATA ) );
      restrictionTable.put( "ShiftMacroData", new Integer( Button.SHIFT_MACRO_DATA ) );
      restrictionTable.put( "XShiftMacroData", new Integer( Button.XSHIFT_MACRO_DATA ) );
      restrictionTable.put( "AllMacroData", new Integer( Button.ALL_MACRO_DATA ) );
      restrictionTable.put( "TMacroData", new Integer( Button.TMACRO_DATA ) );
      restrictionTable.put( "ShiftTMacroData", new Integer( Button.SHIFT_TMACRO_DATA ) );
      restrictionTable.put( "XShiftMacroData", new Integer( Button.XSHIFT_TMACRO_DATA ) );
      restrictionTable.put( "AllTMacroData", new Integer( Button.ALL_TMACRO_DATA ) );
      restrictionTable.put( "FavData", new Integer( Button.FAV_DATA ) );
      restrictionTable.put( "ShiftFavData", new Integer( Button.SHIFT_FAV_DATA ) );
      restrictionTable.put( "XShiftFavData", new Integer( Button.XSHIFT_FAV_DATA ) );
      restrictionTable.put( "AllFavData", new Integer( Button.ALL_FAV_DATA ) );
      restrictionTable.put( "Bind", new Integer( Button.BIND ) );
      restrictionTable.put( "ShiftBind", new Integer( Button.SHIFT_BIND ) );
      restrictionTable.put( "XShiftBind", new Integer( Button.XSHIFT_BIND ) );
      restrictionTable.put( "Data", new Integer( Button.DATA ) );
      restrictionTable.put( "ShiftData", new Integer( Button.SHIFT_DATA ) );
      restrictionTable.put( "XShiftData", new Integer( Button.XSHIFT_DATA ) );
      restrictionTable.put( "AllBind", new Integer( Button.ALL_BIND ) );
      restrictionTable.put( "AllData", new Integer( Button.ALL_DATA ) );
      restrictionTable.put( "Shift", new Integer( Button.SHIFT ) );
      restrictionTable.put( "XShift", new Integer( Button.XSHIFT ) );
      restrictionTable.put( "All", new Integer( Button.ALL ) );
    }
    StringTokenizer st = new StringTokenizer( str, "+-", true );
    boolean isAdd = true;
    while ( st.hasMoreTokens() )
    {
      String token = st.nextToken();
      if ( token.equals( "+" ) )
        isAdd = true;
      else if ( token.equals( "-" ) )
        isAdd = false;
      else
      {
        Integer value = restrictionTable.get( token );
        if ( value == null )
          continue;
        if ( isAdd )
          rc |= value.intValue();
        else
          rc &= ~value.intValue();
      }
    }
    return rc;
  }

  /**
   * Parses the special protocols.
   * 
   * @param rdr
   *          the rdr
   * 
   * @return the string
   * 
   * @throws Exception
   *           the exception
   */
  private String parseSpecialProtocols( RDFReader rdr ) throws Exception
  {
    java.util.List< CheckSum > work = new ArrayList< CheckSum >();
    String line;
    while ( true )
    {
      line = rdr.readLine();
      if ( ( line == null ) || ( line.length() == 0 ) )
        break;

      StringTokenizer st = new StringTokenizer( line, "=" );
      String name = st.nextToken();
      Hex pid = new Hex( st.nextToken() );
      specialProtocols.add( SpecialProtocol.create( name, pid ) );
    }
    checkSums = ( CheckSum[] ) work.toArray( checkSums );
    return line;
  }

  /**
   * Parses the check sums.
   * 
   * @param rdr
   *          the rdr
   * 
   * @return the string
   * 
   * @throws Exception
   *           the exception
   */
  private String parseCheckSums( RDFReader rdr ) throws Exception
  {
    java.util.List< CheckSum > work = new ArrayList< CheckSum >();
    String line;
    while ( true )
    {
      line = rdr.readLine();
      if ( ( line == null ) || ( line.length() == 0 ) )
        break;

      char ch = line.charAt( 0 );

      line = line.substring( 1 );
      StringTokenizer st = new StringTokenizer( line, ":." );
      int addr = rdr.parseNumber( st.nextToken() );
      AddressRange range = new AddressRange( rdr.parseNumber( st.nextToken() ), rdr.parseNumber( st
          .nextToken() ) );
      CheckSum sum = null;
      if ( ch == '+' )
        sum = new AddCheckSum( addr, range );
      else
        sum = new XorCheckSum( addr, range );
      work.add( sum );
    }
    checkSums = ( CheckSum[] ) work.toArray( checkSums );
    return line;
  }

  /**
   * Gets the check sums.
   * 
   * @return the check sums
   */
  public CheckSum[] getCheckSums()
  {
    return checkSums;
  }

  /**
   * Parses the settings.
   * 
   * @param rdr
   *          the rdr
   * 
   * @return the string
   * 
   * @throws Exception
   *           the exception
   */
  private String parseSettings( RDFReader rdr ) throws Exception
  {
    String line;
    java.util.List< Setting > work = new ArrayList< Setting >();
    while ( true )
    {
      line = rdr.readLine();

      if ( ( line == null ) || ( line.length() == 0 ) )
        break;

      StringTokenizer st = new StringTokenizer( line, "=" );
      String title = st.nextToken();

      int byteAddress = rdr.parseNumber( st.nextToken( ".= \t" ) );
      int bitNumber = rdr.parseNumber( st.nextToken() );
      int numberOfBits = rdr.parseNumber( st.nextToken() );
      int initialValue = rdr.parseNumber( st.nextToken() );
      boolean inverted = ( rdr.parseNumber( st.nextToken() ) != 0 );

      java.util.List< String > options = null;
      String sectionName = null;

      if ( st.hasMoreTokens() )
      {
        String token = st.nextToken( ",;)" ).trim();
        if ( token.charAt( 0 ) == '(' )
        {
          options = new ArrayList< String >();
          options.add( token.substring( 1 ) );
          while ( st.hasMoreTokens() )
            options.add( st.nextToken() );
        }
        else
          sectionName = token.trim();
      }
      String[] optionsList = null;
      if ( options != null )
        optionsList = options.toArray( new String[ 0 ] );
      work.add( new Setting( title, byteAddress, bitNumber, numberOfBits, initialValue, inverted,
          optionsList, sectionName ) );
    }
    settings = ( Setting[] ) work.toArray( settings );
    return line;
  }

  /**
   * Gets the settings.
   * 
   * @return the settings
   */
  public Setting[] getSettings()
  {
    return settings;
  }

  /**
   * Gets the section.
   * 
   * @param name
   *          the name
   * 
   * @return the section
   */
  public Object[] getSection( String name )
  {
    if ( name.equals( "DeviceButtons" ) )
      return getDeviceButtons();
    else if ( name.equals( "DeviceTypes" ) )
      return getDeviceTypes();

    return null;
  }

  /**
   * Parses the fixed data.
   * 
   * @param rdr
   *          the rdr
   * 
   * @return the string
   * 
   * @throws Exception
   *           the exception
   */
  private String parseFixedData( RDFReader rdr ) throws Exception
  {
    java.util.List< FixedData > work = new ArrayList< FixedData >();
    java.util.List< Byte > temp = new ArrayList< Byte >();
    String line;
    int address = -1;
    int value = -1;

    while ( true )
    {
      line = rdr.readLine();

      if ( ( line == null ) || ( line.length() == 0 ) )
        break;

      StringTokenizer st = new StringTokenizer( line, ",; \t" );
      String token = st.nextToken();
      while ( true )
      {
        if ( token.charAt( 0 ) == '=' ) // the last token was an address
        {
          token = token.substring( 1 );
          if ( address != -1 ) // we've seen some bytes
          {
            byte[] b = new byte[ temp.size() ];
            int i = 0;
            for ( Byte val : temp )
            {
              b[ i++ ] = val.byteValue();
            }
            work.add( new FixedData( address, b ) );
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
            temp.add( new Byte( ( byte ) value ) );
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
    temp.add( new Byte( ( byte ) value ) );
    byte[] b = new byte[ temp.size() ];
    int j = 0;
    for ( Byte by : temp )
    {
      b[ j ] = by.byteValue();
    }
    work.add( new FixedData( address, b ) );
    fixedData = work.toArray( fixedData );
    return line;
  }

  /**
   * Parses the device buttons.
   * 
   * @param rdr
   *          the rdr
   * 
   * @return the string
   * 
   * @throws Exception
   *           the exception
   */
  private String parseDeviceButtons( RDFReader rdr ) throws Exception
  {
    java.util.List< DeviceButton > work = new ArrayList< DeviceButton >();
    String line;
    while ( true )
    {
      line = rdr.readLine();
      if ( ( line == null ) || ( line.length() == 0 ) )
        break;

      StringTokenizer st = new StringTokenizer( line );
      String name = st.nextToken( "= \t" );

      int hiAddr = rdr.parseNumber( st.nextToken( ",= \t" ) );
      int lowAddr = rdr.parseNumber( st.nextToken() );
      int typeAddr = 0;
      if ( st.hasMoreTokens() )
        typeAddr = rdr.parseNumber( st.nextToken() );
      work.add( new DeviceButton( name, hiAddr, lowAddr, typeAddr ) );
    }
    deviceButtons = work.toArray( deviceButtons );
    return line;
  }

  /**
   * Parses the device abbreviations.
   * 
   * @param rdr
   *          the rdr
   * 
   * @return the string
   * 
   * @throws Exception
   *           the exception
   */
  private String parseDeviceAbbreviations( RDFReader rdr ) throws Exception
  {
    String line;
    while ( true )
    {
      line = rdr.readLine();
      if ( line == null )
        break;
      if ( ( line.length() == 0 ) || ( line.charAt( 0 ) == '[' ) )
        break;
      StringTokenizer st = new StringTokenizer( line, "," );
      while ( st.hasMoreTokens() )
      {
        String token = st.nextToken().trim();
        int equal = token.indexOf( '=' );
        if ( equal == -1 )
          continue;

        String devName = token.substring( 0, equal );
        String abbreviation = token.substring( equal + 1 );
        DeviceType devType = getDeviceType( devName );
        if ( devType != null )
          devType.setAbbreviation( abbreviation );
      }
    }
    return line;
  }

  /**
   * Parses the digit maps.
   * 
   * @param rdr
   *          the rdr
   * 
   * @return the string
   * 
   * @throws Exception
   *           the exception
   */
  private String parseDigitMaps( RDFReader rdr ) throws Exception
  {
    java.util.List< Integer > work = new ArrayList< Integer >();
    String line;
    while ( true )
    {
      line = rdr.readLine();

      if ( ( line == null ) || ( line.length() == 0 ) )
        break;

      StringTokenizer st = new StringTokenizer( line, ",; \t" );
      while ( st.hasMoreTokens() )
      {
        work.add( new Integer( rdr.parseNumber( st.nextToken() ) ) );
      }
    }

    digitMaps = new short[ work.size() ];
    int i = 0;
    for ( Integer v : work )
    {
      digitMaps[ i++ ] = v.shortValue();
    }
    return line;
  }

  /**
   * Parses the device types.
   * 
   * @param rdr
   *          the rdr
   * 
   * @return the string
   * 
   * @throws Exception
   *           the exception
   */
  private String parseDeviceTypes( RDFReader rdr ) throws Exception
  {
    String line;
    int type = 0;
    while ( true )
    {
      line = rdr.readLine();
      if ( ( line == null ) || ( line.length() == 0 ) )
        break;

      StringTokenizer st = new StringTokenizer( line, "=, \t" );
      String name = st.nextToken();
      int map = 0;
      if ( st.hasMoreTokens() )
      {
        map = rdr.parseNumber( st.nextToken() );
        if ( st.hasMoreTokens() )
          type = rdr.parseNumber( st.nextToken() );
      }
      deviceTypes.put( name, new DeviceType( name, map, type ) );
      type += 0x0101;
    }
    return line;
  }

  /**
   * Parses the device type aliases.
   * 
   * @param rdr
   *          the rdr
   * 
   * @return the string
   * 
   * @throws Exception
   *           the exception
   */
  private String parseDeviceTypeAliases( RDFReader rdr ) throws Exception
  {
    String line;
    java.util.List< String > v = new ArrayList< String >();
    DeviceType vcrType = null;
    boolean hasPVRalias = false;
    while ( true )
    {
      line = rdr.readLine();
      if ( ( line == null ) || ( line.length() == 0 ) )
        break;

      StringTokenizer st = new StringTokenizer( line, "= \t" );
      String typeName = st.nextToken();
      DeviceType type = getDeviceType( typeName );
      st.nextToken( "=" );
      String rest = st.nextToken().trim();
      st = new StringTokenizer( rest, "," );
      while ( st.hasMoreTokens() )
      {
        String aliasName = st.nextToken().trim();
        if ( aliasName.equals( "VCR" ) )
          vcrType = type;
        if ( aliasName.equals( "PVR" ) )
          hasPVRalias = true;
        deviceTypeAliases.put( aliasName, type );
        v.add( aliasName );
      }
    }
    if ( !hasPVRalias && ( vcrType != null ) )
    {
      v.add( "PVR" );
      deviceTypeAliases.put( "PVR", vcrType );
    }
    deviceTypeAliasNames = new String[ 0 ];
    deviceTypeAliasNames = v.toArray( deviceTypeAliasNames );
    Arrays.sort( deviceTypeAliasNames );
    return line;
  }

  /**
   * Gets the device type alias names.
   * 
   * @return the device type alias names
   */
  public String[] getDeviceTypeAliasNames()
  {
    load();
    return deviceTypeAliasNames;
  }

  /**
   * Parses the device type image maps.
   * 
   * @param rdr
   *          the rdr
   * 
   * @return the string
   * 
   * @throws Exception
   *           the exception
   */
  private String parseDeviceTypeImageMaps( RDFReader rdr ) throws Exception
  {
    String line;
    DeviceType type = null;
    java.util.List< java.util.List< ImageMap >> outer = new ArrayList< java.util.List< ImageMap >>();
    java.util.List< ImageMap > inner = null;
    boolean nested = false;
    PropertyFile properties = JP1Frame.getProperties();
    File imageDir = properties.getFileProperty( "ImagePath" );
    if ( imageDir == null )
      imageDir = new File( properties.getFile().getParentFile(), "Images" );

    while ( true )
    {
      line = rdr.readLine();
      if ( ( line == null ) || ( line.length() == 0 ) )
        break;

      StringTokenizer st = new StringTokenizer( line, "=, \t" );
      String typeName = st.nextToken();
      type = getDeviceType( typeName );

      while ( st.hasMoreTokens() )
      {
        String token = st.nextToken();
        if ( token.charAt( 0 ) == '(' ) // it's a list
        {
          nested = true;
          token = token.substring( 1 );
          inner = new ArrayList< ImageMap >();
          outer.add( inner );
        }

        if ( !nested )
        {
          inner = new ArrayList< ImageMap >();
          outer.add( inner );
        }

        int closeParen = token.indexOf( ')' );
        if ( closeParen != -1 )
        {
          nested = false;
          token = token.substring( 0, closeParen );
        }

        inner.add( new ImageMap( new File( imageDir, token ) ) );
      }
      ImageMap[][] outerb = new ImageMap[ outer.size() ][];
      int o = 0;
      for ( java.util.List< ImageMap > maps : outer )
      {
        ImageMap[] innerb = new ImageMap[ maps.size() ];
        outerb[ o++ ] = innerb;
        int i = 0;
        for ( ImageMap map : maps )
        {
          innerb[ i++ ] = map;
        }
        maps.clear();
      }
      outer.clear();
      type.setImageMaps( outerb );
    }
    return line;
  }

  /**
   * Parses the buttons.
   * 
   * @param rdr
   *          the rdr
   * 
   * @return the string
   * 
   * @throws Exception
   *           the exception
   */
  private String parseButtons( RDFReader rdr ) throws Exception
  {
    String line;
    short keycode = 1;
    int restrictions = defaultRestrictions;
    while ( true )
    {
      line = rdr.readLine();
      if ( line == null )
        break;
      if ( ( line.length() == 0 ) || ( line.charAt( 0 ) == '[' ) )
        break;
      StringTokenizer st = new StringTokenizer( line, "," );
      while ( st.hasMoreTokens() )
      {
        String token = st.nextToken().trim();
        int equal = token.indexOf( '=' );
        if ( equal != -1 )
        {
          String keycodeStr = token.substring( equal + 1 );
          token = token.substring( 0, equal );
          int pos = keycodeStr.indexOf( ':' );
          if ( pos != -1 )
          {
            String restrictStr = keycodeStr.substring( pos + 1 );
            restrictions = parseRestrictions( restrictStr );
            keycodeStr = keycodeStr.substring( 0, pos );
          }
          else
            restrictions = defaultRestrictions;
          keycode = ( short ) rdr.parseNumber( keycodeStr );
        }

        int colon = token.indexOf( ':' );
        String name = token;
        if ( colon != -1 )
        {
          name = token.substring( colon + 1 );
          char ch = name.charAt( 0 );
          if ( ( ch == '\'' ) || ch == '"' )
          {
            int end = name.lastIndexOf( ch );
            name = name.substring( 1, end );
          }
          token = token.substring( 0, colon );
          ch = token.charAt( 0 );
          if ( ( ch == '\'' ) || ch == '"' )
          {
            int end = token.lastIndexOf( ch );
            token = token.substring( 1, end );
          }
        }
        Button b = new Button( token, name, keycode, this );
        b.setRestrictions( restrictions );
        keycode++ ;
        addButton( b );
      }
    }

    return line;
  }

  /**
   * Gets the button.
   * 
   * @param keyCode
   *          the key code
   * 
   * @return the button
   */
  public Button getButton( int keyCode )
  {
    load();
    return ( Button ) buttonsByKeyCode.get( new Integer( keyCode ) );
  }

  /**
   * Gets the button name.
   * 
   * @param keyCode
   *          the key code
   * 
   * @return the button name
   */
  public String getButtonName( int keyCode )
  {
    Button b = getButton( keyCode );

    if ( b == null )
    {
      int baseCode = keyCode & 0x3F;
      if ( baseCode != 0 )
      {
        b = getButton( baseCode );
        if ( ( baseCode | shiftMask ) == keyCode )
          return b.getShiftedName();
        if ( xShiftEnabled && ( ( baseCode | xShiftMask ) == keyCode ) )
          return b.getXShiftedName();
      }
      baseCode = keyCode & ~shiftMask;
      b = getButton( baseCode );
      if ( b != null )
        return b.getShiftedName();
      baseCode = keyCode & ~xShiftMask;
      b = getButton( baseCode );
      if ( b != null )
        return b.getXShiftedName();
    }

    if ( b == null )
    {
      System.err.println( "ERROR: Unknown keycode $" + Integer.toHexString( keyCode & 0xFF )
          + ", Creating button!" );
      String name = "button" + Integer.toHexString( keyCode & 0xFF ).toUpperCase();
      b = new Button( name, name, ( short ) keyCode, this );
      if ( b.getIsShifted() )
      {
        Button baseButton = getButton( keyCode & 0x3F );
        if ( baseButton != null )
        {
          b.setBaseButton( baseButton );
          baseButton.setShiftedButton( b );
        }
      }
      else if ( b.getIsXShifted() )
      {
        Button baseButton = getButton( keyCode & 0x3F );
        if ( baseButton != null )
        {
          b.setBaseButton( baseButton );
          baseButton.setXShiftedButton( b );
        }
      }
      addButton( b );
    }

    return b.getName();
  }

  /**
   * Gets the button.
   * 
   * @param name
   *          the name
   * 
   * @return the button
   */
  public Button getButton( String name )
  {
    load();
    return ( Button ) buttonsByName.get( name.toLowerCase() );
  }

  /**
   * Adds the button.
   * 
   * @param b
   *          the b
   */
  public void addButton( Button b )
  {
    int keycode = b.getKeyCode();
    int unshiftedCode = keycode & 0x3f;
    if ( b.getIsShifted() )
    {
      Button c = getButton( unshiftedCode );
      if ( c != null )
      {
        c.setShiftedButton( b );
        b.setBaseButton( c );
        if ( b.getName() == null )
        {
          String name = shiftLabel + '-' + c.getName();
          b.setName( name );
          b.setStandardName( name );
        }
      }
    }
    else if ( b.getIsXShifted() )
    {
      Button c = getButton( unshiftedCode );
      if ( c != null )
      {
        c.setXShiftedButton( b );
        b.setBaseButton( c );
        if ( b.getName() == null )
        {
          String name = xShiftLabel + '-' + c.getName();
          b.setName( name );
          b.setStandardName( name );
        }
      }
    }
    else
    {
      // Look for a shifted button for which this is the base.
      int shiftedCode = keycode + shiftMask;
      Button c = getButton( shiftedCode );
      if ( c != null )
      {
        c.setBaseButton( b );
        b.setShiftedButton( c );
      }
      if ( xShiftEnabled )
      {
        // Look for an xshifted button for which this is the base.
        shiftedCode = keycode + xShiftMask;
        c = getButton( shiftedCode );
        if ( c != null )
        {
          c.setBaseButton( b );
          b.setXShiftedButton( c );
        }
      }
    }
    if ( b.getName() == null )
    {
      String name = "unknown" + Integer.toHexString( keycode );
      b.setName( name );
      b.setStandardName( name );
    }
    buttons.add( b );
    buttonsByName.put( b.getName().toLowerCase(), b );
    buttonsByStandardName.put( b.getStandardName().toLowerCase(), b );
    buttonsByKeyCode.put( new Integer( keycode ), b );
  }

  /**
   * Parses the multi macros.
   * 
   * @param rdr
   *          the rdr
   * 
   * @return the string
   * 
   * @throws Exception
   *           the exception
   */
  private String parseMultiMacros( RDFReader rdr ) throws Exception
  {
    String line;
    while ( true )
    {
      line = rdr.readLine();
      if ( ( line == null ) || ( line.length() == 0 ) )
        break;

      StringTokenizer st = new StringTokenizer( line, "=" );
      String name = st.nextToken();

      // Find the matching button
      Button button = ( Button ) buttonsByName.get( name );
      if ( button != null )
        button.setMultiMacroAddress( rdr.parseNumber( st.nextToken() ) );
    }
    return line;
  }

  /**
   * Find by standard name.
   * 
   * @param b
   *          the b
   * 
   * @return the button
   */
  public Button findByStandardName( Button b )
  {
    load();
    return ( Button ) buttonsByStandardName.get( b.getStandardName().toLowerCase() );
  }

  /**
   * Parses the button maps.
   * 
   * @param rdr
   *          the rdr
   * 
   * @return the string
   * 
   * @throws Exception
   *           the exception
   */
  private String parseButtonMaps( RDFReader rdr ) throws Exception
  {
    java.util.List< ButtonMap > work = new ArrayList< ButtonMap >();
    String line;
    // ButtonMap map = null;
    int name = -1;
    java.util.List< java.util.List< Integer >> outer = new ArrayList< java.util.List< Integer >>();
    java.util.List< Integer > inner = null;
    boolean nested = false;

    while ( true )
    {
      line = rdr.readLine();
      if ( ( line == null ) || ( line.length() == 0 ) )
        break;

      StringTokenizer st = new StringTokenizer( line, "=, \t" );
      if ( line.indexOf( '=' ) != -1 )
      {
        if ( name != -1 )
        {
          short[][] outerb = new short[ outer.size() ][];
          int o = 0;
          for ( java.util.List< Integer > maps : outer )
          {
            short[] innerb = new short[ maps.size() ];
            outerb[ o++ ] = innerb;
            int i = 0;
            for ( Integer v : maps )
            {
              innerb[ i++ ] = v.shortValue();
            }
            maps.clear();
          }
          outer.clear();
          work.add( new ButtonMap( name, outerb ) );
        }
        name = rdr.parseNumber( st.nextToken() );
      }

      while ( st.hasMoreTokens() )
      {
        String token = st.nextToken();
        if ( token.charAt( 0 ) == '(' ) // it's a list
        {
          nested = true;
          token = token.substring( 1 );
          inner = new ArrayList< Integer >();
          outer.add( inner );
        }

        if ( !nested )
        {
          inner = new ArrayList< Integer >();
          outer.add( inner );
        }

        int closeParen = token.indexOf( ')' );
        if ( closeParen != -1 )
        {
          nested = false;
          token = token.substring( 0, closeParen );
        }

        inner.add( new Integer( rdr.parseNumber( token ) ) );
      }
    }
    {
      short[][] outerb = new short[ outer.size() ][];
      int o = 0;
      for ( java.util.List< Integer > maps : outer )
      {
        short[] innerb = new short[ maps.size() ];
        outerb[ o++ ] = innerb;
        int i = 0;
        for ( Integer v : maps )
        {
          innerb[ i++ ] = v.shortValue();
        }
        maps.clear();
      }
      outer.clear();
      work.add( new ButtonMap( name, outerb ) );
    }
    buttonMaps = work.toArray( buttonMaps );
    return line;
  }

  /**
   * Parses the protocols.
   * 
   * @param rdr
   *          the rdr
   * 
   * @return the string
   * 
   * @throws Exception
   *           the exception
   */
  private String parseProtocols( RDFReader rdr ) throws Exception
  {
    String line;
    while ( true )
    {
      line = rdr.readLine();
      if ( line == null )
        break;
      if ( line.length() != 0 )
      {
        if ( line.charAt( 0 ) == '[' )
          break;
        StringTokenizer st = new StringTokenizer( line, "," );
        while ( st.hasMoreTokens() )
        {
          String token = st.nextToken().trim();
          String variantName = "";
          int colon = token.indexOf( ':' );
          // String name = token;
          if ( colon != -1 )
          {
            variantName = token.substring( colon + 1 );
            token = token.substring( 0, colon );
          }
          Hex pid = new Hex( token );
          java.util.List< String > v = protocolVariantNames.get( pid );
          if ( v == null )
          {
            v = new ArrayList< String >();
            protocolVariantNames.put( pid, v );
          }
          v.add( variantName );
        }
      }
    }
    return line;
  }

  /**
   * Gets the height.
   * 
   * @return the height
   */
  public int getHeight()
  {
    load();
    return height;
  }

  /** The height. */
  private int height;

  /**
   * Gets the width.
   * 
   * @return the width
   */
  public int getWidth()
  {
    load();
    return width;
  }

  /** The width. */
  private int width;

  /**
   * Supports variant.
   * 
   * @param pid
   *          the pid
   * @param name
   *          the name
   * 
   * @return true, if successful
   */
  public boolean supportsVariant( Hex pid, String name )
  {
    load();
    java.util.List< String > v = protocolVariantNames.get( pid );
    if ( ( v == null ) || v.isEmpty() )
      return false;

    if ( v.contains( name ) )
      return true;

    return false;
  }

  /**
   * Gets the supported variant names.
   * 
   * @param pid
   *          the pid
   * 
   * @return the supported variant names
   */
  public java.util.List< String > getSupportedVariantNames( Hex pid )
  {
    load();
    return protocolVariantNames.get( pid );
  }

  /*
   * public void clearButtonAssignments() { load(); for ( Enumeration e = buttons.elements();
   * e.hasMoreElements(); ) { (( Button )e.nextElement()).setFunction( null ).setShiftedFunction(
   * null ).setXShiftedFunction( null ); } }
   */

  /**
   * Sets the protocols.
   * 
   * @param protocols
   *          the new protocols
   */
  public void setProtocols( java.util.List< Protocol > protocols )
  {
    load();
    this.protocols = protocols;
  }

  /**
   * Gets the protocols.
   * 
   * @return the protocols
   */
  public java.util.List< Protocol > getProtocols()
  {
    load();
    return protocols;
  }

  /**
   * Gets the encrypter decrypter.
   * 
   * @return the encrypter decrypter
   */
  public EncrypterDecrypter getEncrypterDecrypter()
  {
    load();
    return encdec;
  }

  /**
   * Creates the key move key.
   * 
   * @param keyCode
   *          the key code
   * @param deviceIndex
   *          the device index
   * @param deviceType
   *          the device type
   * @param setupCode
   *          the setup code
   * @param movedKeyCode
   *          the moved key code
   * @param notes
   *          the notes
   * 
   * @return the key move
   */
  public KeyMove createKeyMoveKey( int keyCode, int deviceIndex, int deviceType, int setupCode,
      int movedKeyCode, String notes )
  {
    KeyMove keyMove = null;
    keyMove = new KeyMoveKey( keyCode, deviceIndex, deviceType, setupCode, movedKeyCode, notes );
    return keyMove;
  }

  /**
   * Creates the key move.
   * 
   * @param keyCode
   *          the key code
   * @param deviceIndex
   *          the device index
   * @param deviceType
   *          the device type
   * @param setupCode
   *          the setup code
   * @param cmd
   *          the cmd
   * @param notes
   *          the notes
   * 
   * @return the key move
   */
  public KeyMove createKeyMove( int keyCode, int deviceIndex, int deviceType, int setupCode,
      Hex cmd, String notes )
  {
    KeyMove keyMove = null;
    if ( advCodeFormat == HEX_FORMAT )
      keyMove = new KeyMove( keyCode, deviceIndex, deviceType, setupCode, cmd, notes );
    else if ( efcDigits == 3 )
      keyMove = new KeyMoveEFC( keyCode, deviceIndex, deviceType, setupCode, EFC.parseHex( cmd ),
          notes );
    else
      // EFCDigits == 5
      keyMove = new KeyMoveEFC5( keyCode, deviceIndex, deviceType, setupCode, EFC5.parseHex( cmd ),
          notes );
    return keyMove;
  }

  /**
   * Creates the key move.
   * 
   * @param keyCode
   *          the key code
   * @param deviceIndex
   *          the device index
   * @param deviceType
   *          the device type
   * @param setupCode
   *          the setup code
   * @param efc
   *          the efc
   * @param notes
   *          the notes
   * 
   * @return the key move
   */
  public KeyMove createKeyMove( int keyCode, int deviceIndex, int deviceType, int setupCode,
      int efc, String notes )
  {
    KeyMove keyMove = null;
    if ( advCodeFormat == HEX_FORMAT )
    {
      if ( efcDigits == 3 )
        keyMove = new KeyMove( keyCode, deviceIndex, deviceType, setupCode, EFC.toHex( efc ), notes );
      else
        // EFCDigits == 5
        keyMove = new KeyMove( keyCode, deviceIndex, deviceType, setupCode, EFC5.toHex( efc ),
            notes );
    }
    else if ( efcDigits == 3 )
      keyMove = new KeyMoveEFC( keyCode, deviceIndex, deviceType, setupCode, efc, notes );
    else
      // EFCDigits == 5
      keyMove = new KeyMoveEFC5( keyCode, deviceIndex, deviceType, setupCode, efc, notes );
    return keyMove;
  }

  /**
   * Gets the max upgrade length.
   * 
   * @return the max upgrade length
   */
  public Integer getMaxUpgradeLength()
  {
    return maxUpgradeLength;
  }

  /**
   * Gets the max protocol length.
   * 
   * @return the max protocol length
   */
  public Integer getMaxProtocolLength()
  {
    return maxProtocolLength;
  }

  /**
   * Gets the max combined upgrade length.
   * 
   * @return the max combined upgrade length
   */
  public Integer getMaxCombinedUpgradeLength()
  {
    return maxCombinedUpgradeLength;
  }

  // Interface Comparable
  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo( Remote o )
  {
    return names[ nameIndex ].compareTo( o.names[ o.nameIndex ] );
  }

  /**
   * Gets the shift mask.
   * 
   * @return the shift mask
   */
  public int getShiftMask()
  {
    return shiftMask;
  }

  /**
   * Gets the x shift mask.
   * 
   * @return the x shift mask
   */
  public int getXShiftMask()
  {
    return xShiftMask;
  }

  /**
   * Gets the x shift enabled.
   * 
   * @return the x shift enabled
   */
  public boolean getXShiftEnabled()
  {
    return xShiftEnabled;
  }

  /**
   * Sets the x shift enabled.
   * 
   * @param flag
   *          the new x shift enabled
   */
  public void setXShiftEnabled( boolean flag )
  {
    xShiftEnabled = flag;
  }

  /**
   * Gets the shift label.
   * 
   * @return the shift label
   */
  public String getShiftLabel()
  {
    return shiftLabel;
  }

  /**
   * Gets the x shift label.
   * 
   * @return the x shift label
   */
  public String getXShiftLabel()
  {
    return xShiftLabel;
  }

  /**
   * Gets the protocol vector offset.
   * 
   * @return the protocol vector offset
   */
  public int getProtocolVectorOffset()
  {
    return protocolVectorOffset;
  }

  /**
   * Gets the protocol data offset.
   * 
   * @return the protocol data offset
   */
  public int getProtocolDataOffset()
  {
    return protocolDataOffset;
  }

  /**
   * Gets the supports binary upgrades.
   * 
   * @return the supports binary upgrades
   */
  public boolean getSupportsBinaryUpgrades()
  {
    return supportsBinaryUpgrades;
  }

  /** The file. */
  private File file = null;

  /** The signature. */
  private String signature = null;

  /** The names. */
  private String[] names = new String[ 1 ];

  /** The name index. */
  private int nameIndex = 0;

  /** The loaded. */
  private boolean loaded = false;

  /** The base address. */
  private int baseAddress = 0;

  /** The eeprom size. */
  private int eepromSize;

  /** The device code offset. */
  private int deviceCodeOffset;

  /** The fav key. */
  private FavKey favKey = null;

  /**
   * Gets the fav key.
   * 
   * @return the fav key
   */
  public FavKey getFavKey()
  {
    return favKey;
  }

  /** The oem device. */
  @SuppressWarnings( "unused" )
  private OEMDevice oemDevice = null;

  /** The oem control. */
  @SuppressWarnings( "unused" )
  private int oemControl = 0;

  /** The upgrade bug. */
  @SuppressWarnings( "unused" )
  private boolean upgradeBug = false;

  /** The advanced code address. */
  private AddressRange advancedCodeAddress = null;

  /**
   * Gets the advanced code address.
   * 
   * @return the advanced code address
   */
  public AddressRange getAdvancedCodeAddress()
  {
    return advancedCodeAddress;
  }

  /** The macro support. */
  @SuppressWarnings( "unused" )
  private boolean macroSupport = true;

  /** The upgrade address. */
  private AddressRange upgradeAddress = null;

  /**
   * Gets the upgrade address.
   * 
   * @return the upgrade address
   */
  public AddressRange getUpgradeAddress()
  {
    return upgradeAddress;
  }

  /** The device upgrade address. */
  @SuppressWarnings( "unused" )
  private AddressRange deviceUpgradeAddress = null;

  /** The timed macro address. */
  @SuppressWarnings( "unused" )
  private AddressRange timedMacroAddress = null;

  /** The timed macro warning. */
  @SuppressWarnings( "unused" )
  private boolean timedMacroWarning = false;

  /** The learned address. */
  private AddressRange learnedAddress = null;

  /**
   * Gets the learned address.
   * 
   * @return the learned address
   */
  public AddressRange getLearnedAddress()
  {
    return learnedAddress;
  }

  /** The processor. */
  private Processor processor = null;
  // private String processorVersion = null;
  /** The RAM address. */
  private int RAMAddress;

  /** The time address. */
  @SuppressWarnings( "unused" )
  private int timeAddress = 0;

  /** The RDF sync. */
  @SuppressWarnings( "unused" )
  private int RDFSync;

  /** The punch thru base. */
  @SuppressWarnings( "unused" )
  private int punchThruBase;

  /** The scan base. */
  @SuppressWarnings( "unused" )
  private int scanBase = 0;

  /** The sleep status bit. */
  @SuppressWarnings( "unused" )
  private StatusBit sleepStatusBit = null;

  /** The vpt status bit. */
  @SuppressWarnings( "unused" )
  private StatusBit vptStatusBit = null;

  /** The check sums. */
  private CheckSum[] checkSums = new CheckSum[ 0 ];

  /** The settings. */
  private Setting[] settings = new Setting[ 0 ];

  /** The fixed data. */
  private FixedData[] fixedData = new FixedData[ 0 ];

  /** The device buttons. */
  private DeviceButton[] deviceButtons = new DeviceButton[ 0 ];

  /** The device types. */
  private Hashtable< String, DeviceType > deviceTypes = new Hashtable< String, DeviceType >();

  /** The device type aliases. */
  private Hashtable< String, DeviceType > deviceTypeAliases = new Hashtable< String, DeviceType >();

  /** The device type alias names. */
  private String[] deviceTypeAliasNames = null;

  /** The buttons. */
  private java.util.List< Button > buttons = new ArrayList< Button >();

  /** The buttons by key code. */
  private Hashtable< Integer, Button > buttonsByKeyCode = new Hashtable< Integer, Button >();

  /** The buttons by name. */
  private Hashtable< String, Button > buttonsByName = new Hashtable< String, Button >();

  /** The buttons by standard name. */
  private Hashtable< String, Button > buttonsByStandardName = new Hashtable< String, Button >();

  /** The upgrade buttons. */
  private Button[] upgradeButtons = new Button[ 0 ];

  /** The phantom shapes. */
  private java.util.List< ButtonShape > phantomShapes = new ArrayList< ButtonShape >();

  /** The digit maps. */
  private short[] digitMaps = new short[ 0 ];

  /** The button maps. */
  private ButtonMap[] buttonMaps = new ButtonMap[ 0 ];

  /** The omit digit map byte. */
  private boolean omitDigitMapByte = false;

  /** The protocol variant names. */
  private Hashtable< Hex, java.util.List< String >> protocolVariantNames = new Hashtable< Hex, java.util.List< String >>();

  /** The protocols. */
  private java.util.List< Protocol > protocols = null;

  /** The image maps. */
  private ImageMap[] imageMaps = new ImageMap[ 0 ];

  /** The map index. */
  private int mapIndex = 0;

  /** The shift mask. */
  private int shiftMask = 0x80;

  /** The x shift mask. */
  private int xShiftMask = 0xC0;

  /** The x shift enabled. */
  private boolean xShiftEnabled = false;

  /** The shift label. */
  private String shiftLabel = "Shift";

  /** The x shift label. */
  private String xShiftLabel = "XShift";

  /** The default restrictions. */
  private int defaultRestrictions = 0;

  /** The Constant HEX_FORMAT. */
  public static final int HEX_FORMAT = 0;

  /** The Constant EFC_FORMAT. */
  public static final int EFC_FORMAT = 1;

  /** The Constant NORMAL. */
  public static final int NORMAL = 0;

  /** The Constant LONG. */
  public static final int LONG = 1;

  /** The adv code format. */
  private int advCodeFormat = HEX_FORMAT;

  /** The adv code bind format. */
  private int advCodeBindFormat = NORMAL;

  /** The efc digits. */
  private int efcDigits = 3;

  /** The dev comb address. */
  private int[] devCombAddress = null;

  /** The protocol vector offset. */
  private int protocolVectorOffset = 0;

  /** The protocol data offset. */
  private int protocolDataOffset = 0;

  /** The encdec. */
  private EncrypterDecrypter encdec = null;

  /** The supports binary upgrades. */
  private boolean supportsBinaryUpgrades = false;

  /** The max protocol length. */
  private Integer maxProtocolLength = null;

  /** The max upgrade length. */
  private Integer maxUpgradeLength = null;

  /** The max combined upgrade length. */
  private Integer maxCombinedUpgradeLength = null;

  /** The section terminator. */
  private short sectionTerminator = 0;

  /**
   * Gets the section terminator.
   * 
   * @return the section terminator
   */
  public short getSectionTerminator()
  {
    return sectionTerminator;
  }

  /** The special protocols. */
  public java.util.List< SpecialProtocol > specialProtocols = new ArrayList< SpecialProtocol >();

  /**
   * Gets the special protocols.
   * 
   * @return the special protocols
   */
  public java.util.List< SpecialProtocol > getSpecialProtocols()
  {
    return specialProtocols;
  }

  /** The two byte pid. */
  private boolean twoBytePID = false;

  /**
   * Uses two byte pid.
   * 
   * @return true, if successful
   */
  public boolean usesTwoBytePID()
  {
    return twoBytePID;
  }

  /** The learned dev btn swapped. */
  private boolean learnedDevBtnSwapped = false;

  /**
   * Gets the learned dev btn swapped.
   * 
   * @return the learned dev btn swapped
   */
  public boolean getLearnedDevBtnSwapped()
  {
    return learnedDevBtnSwapped;
  }

  /** The restriction table. */
  private static Hashtable< String, Integer > restrictionTable = null;
}
