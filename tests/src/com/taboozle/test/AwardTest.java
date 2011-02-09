package com.taboozle.test;

import android.test.AndroidTestCase;

import com.taboozle.Award;

/**
 * Test class that tests basic methods of Award class. Nothing fancy here.
 * 
 * @author Taboozle Team
 */
public class AwardTest extends AndroidTestCase 
{

  /**
   * Members
   */
  Award empty;
  Award basicAward;
  
  int aId;
  String aName;
  String aExplanation;
  int aPriority;
  
  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */  
  protected void setUp() throws Exception 
  {
    super.setUp();
    aId = 1;
    aName = "basicAwardTest";
    aExplanation = "just a simple test";
    aPriority = 1;
    this.empty = new Award();
    this.basicAward = new Award(aId, aName, aExplanation, aPriority);
  }

  /**
   * Tests the default Award constructor.
   */
  public void testAward() 
  {
    Award newAward = new Award();
    assertEquals( this.empty, newAward );
  }

  /**
   * Tests the Award constructor that allows an ID, Name, Description, and Priority to be provided.
   */
  public void testAwardIntStringStringInt() 
  {
    assertEquals( this.basicAward.getId(), aId);
    assertEquals( this.basicAward.getName(), aName);
    assertEquals( this.basicAward.getExplanation(), aExplanation);
    assertEquals( this.basicAward.getPriority(), aPriority);
  }

}
