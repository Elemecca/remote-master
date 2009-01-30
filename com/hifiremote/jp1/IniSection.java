package com.hifiremote.jp1;

import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class IniSection.
 */
public class IniSection
  extends Properties
{
  
  /**
   * Instantiates a new ini section.
   */
  public IniSection()
  {
    super();
  }
  
  /**
   * Instantiates a new ini section.
   * 
   * @param name the name
   */
  public IniSection( String name )
  {
    super();
    this.name = name;
  }
    
  /**
   * Instantiates a new ini section.
   * 
   * @param defaults the defaults
   */
  public IniSection( Properties defaults )
  {
    super( defaults );
  }
  
  /**
   * Instantiates a new ini section.
   * 
   * @param name the name
   * @param defaults the defaults
   */
  public IniSection( String name, Properties defaults )
  {
    super( defaults );
    this.name = name;
  }

  /** The name. */
  private String name = null;
  
  /**
   * Sets the name.
   * 
   * @param name the new name
   */
  public void setName( String name )
  {
    this.name = name;
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName()
  {
    return name;
  }
  
  /**
   * Adds the.
   * 
   * @param property the property
   */
  public void add( Property property )
  {
    setProperty( property.name, property.value );
  }
}
