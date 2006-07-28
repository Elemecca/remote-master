package com.hifiremote.jp1;

import java.util.*;

public class IniSection
  extends Properties
{
  public IniSection()
  {
    super();
  }
  
  public IniSection( String name )
  {
    super();
    this.name = name;
  }
  
  public IniSection( Properties defaults )
  {
    super( defaults );
  }
  
  public IniSection( String name, Properties defaults )
  {
    super( defaults );
    this.name = name;
  }

  private String name = null;
  public void setName( String name )
  {
    this.name = name;
  }
  public String getName()
  {
    return name;
  }
}
