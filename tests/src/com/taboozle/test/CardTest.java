/**
 * 
 */
package com.taboozle.test;

import java.util.ArrayList;

import android.test.AndroidTestCase;

import com.taboozle.Card;


/**
 * @author Taboozle Team
 *
 */
public class CardTest extends AndroidTestCase
{

  /**
   * Members
   */
  Card empty;
  Card standard;
  
  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception
  {
    super.setUp();
    int id = 1;
    int rws = 0;
    String title = "Title";
    ArrayList<String> badWords = new ArrayList<String>();
    badWords.add( "w1" );
    badWords.add( "w2" );
    badWords.add( "w3" );
    badWords.add( "w4" );
    badWords.add( "w5" );
    int time = 1;
    this.empty = new Card();
    this.standard = new Card( id, rws, title, badWords, time );
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown() throws Exception
  {
    super.tearDown();
  }

  /**
   * Test method for {@link com.taboozle.Card#BustString(java.lang.String)}.
   */
  public void testBustString()
  {
    String sequence = "10,20,egg";
    ArrayList<String> truth = new ArrayList<String>();
    truth.add( "10" );
    truth.add( "20" );
    truth.add( "EGG" );
    ArrayList<String> al = Card.BustString( sequence );
    assertEquals( truth, al );
  }

  /**
   * Test method for {@link com.taboozle.Card#Card()}.
   */
  public void testCard()
  {
    Card e = new Card();
    assertEquals( this.empty, e );
  }

  /**
   * Test method for {@link com.taboozle.Card#Card(com.taboozle.Card)}.
   */
  public void testCardCard()
  {
    Card copy = new Card(this.standard);
    assertEquals( this.standard, copy );
  }

  /**
   * Test method for {@link com.taboozle.Card#getId()}.
   */
  public void testGetId()
  {
    assertEquals( this.standard.getId(), 1 );
  }

  /**
   * Test method for {@link com.taboozle.Card#setId(long)}.
   */
  public void testSetId()
  {
    this.standard.setId( 7 );
    assertEquals( this.standard.getId(), 7 );
  }

  /**
   * Test method for {@link com.taboozle.Card#getRws()}.
   */
  public void testGetRws()
  {
    assertEquals( this.standard.getRws(), 0 );
  }

  /**
   * Test method for {@link com.taboozle.Card#setRws(int)}.
   */
  public void testSetRws()
  {
    this.standard.setRws( 2 );
    assertEquals( this.standard.getRws(), 2 );
  }

  /**
   * Test method for {@link com.taboozle.Card#getTitle()}.
   */
  public void testGetTitle()
  {
    assertEquals( this.standard.getTitle(), "Title" );
  }

  /**
   * Test method for {@link com.taboozle.Card#setTitle(java.lang.String)}.
   */
  public void testSetTitle()
  {
    this.standard.setTitle( "foo" );
    assertEquals( this.standard.getTitle(), "foo" );
  }

  /**
   * Test method for {@link com.taboozle.Card#getBadWords()}.
   */
  public void testGetBadWords()
  {
    ArrayList<String> badWords = new ArrayList<String>();
    badWords.add( "w1" );
    badWords.add( "w2" );
    badWords.add( "w3" );
    badWords.add( "w4" );
    badWords.add( "w5" );
    assertEquals( this.standard.getBadWords(), badWords );
  }

  /**
   * Test method for {@link com.taboozle.Card#setBadWords(java.util.ArrayList)}.
   */
  public void testSetBadWordsArrayListOfString()
  {
    ArrayList<String> truth = new ArrayList<String>();
    truth.add( "10" );
    truth.add( "20" );
    truth.add( "EGG" );
    this.standard.setBadWords( truth );
    assertEquals( this.standard.getBadWords(), truth );
  }

  /**
   * Test method for {@link com.taboozle.Card#setBadWords(java.lang.String)}.
   */
  public void testSetBadWordsString()
  {
    String sequence = "10,20,egg";
    ArrayList<String> truth = new ArrayList<String>();
    truth.add( "10" );
    truth.add( "20" );
    truth.add( "EGG" );
    this.standard.setBadWords( sequence );
    assertEquals( this.standard.getBadWords(), truth );
  }

  /**
   * Test method for {@link com.taboozle.Card#getDrawableId()}.
   */
  public void testGetDrawableId()
  {
    assertEquals( this.standard.getDrawableId(), com.taboozle.R.drawable.right );
    this.standard.setRws( 1 );
    assertEquals( this.standard.getDrawableId(), com.taboozle.R.drawable.wrong );
    this.standard.setRws( 2 );
    assertEquals( this.standard.getDrawableId(), com.taboozle.R.drawable.skip );
  }
  
  /**
   * Test method for {@link com.taboozle.Card#getDrawableId()}.
   */
  public void testGetDrawableIdForBack()
  {
    assertEquals( this.standard.getDrawableIdForBack(), com.taboozle.R.drawable.controls_right );
    this.standard.setRws( 1 );
    assertEquals( this.standard.getDrawableIdForBack(), com.taboozle.R.drawable.controls_wrong );
    this.standard.setRws( 2 );
    assertEquals( this.standard.getDrawableIdForBack(), com.taboozle.R.drawable.controls_skip );
  }

  /**
   * Test method for {@link com.taboozle.Card#cycleRws()}.
   */
  public void testCycleRws()
  {
    int r = this.standard.getRws();
    this.standard.cycleRws();
    int w = this.standard.getRws();
    this.standard.cycleRws();
    int s = this.standard.getRws();
    this.standard.cycleRws();
    int r2 = this.standard.getRws();
    this.standard.cycleRws();
    assertEquals( r, 0 );
    assertEquals( w, 1 );
    assertEquals( s, 2 );
    assertEquals( r2, 0 );
    
  }

  /**
   * Test method for {@link com.taboozle.Card#setTime(int)}.
   */
  public void testSetTime()
  {
    this.standard.setTime( 5 );
    assertEquals( this.standard.getTime(), 5 );
  }

  /**
   * Test method for {@link com.taboozle.Card#getTime()}.
   */
  public void testGetTime()
  {
    assertEquals( this.standard.getTime(), 1 );
  }

}
