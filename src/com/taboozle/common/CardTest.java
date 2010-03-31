/**
 * 
 */
package com.taboozle.common;

import junit.framework.TestCase;

/**
 * @author cpatrick
 *
 */
public class CardTest extends TestCase {

	public Card perfidious;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		perfidious = new Card("perfidious",5);
		String[] badWords = {"trust","betray","gossip","liar","backstab"};
		perfidious.AddBadWords(badWords);
		super.setUp();
	}

	/**
	 * Test method for {@link com.taboozle.common.Card#GetName()}.
	 */
	public void testGetName() {
		if( perfidious.GetName() != "perfidious" )
		{
			fail("Name not set properly.");	
		}

	}

	/**
	 * Test method for {@link com.taboozle.common.Card#GetBadWords()}.
	 */
	public void testGetBadWords() {
		String[] badWords = this.perfidious.GetBadWords();
		if( !( badWords[0] == "trust" && badWords[1] == "betray" && badWords[2] == "gossip" &&
				badWords[3] == "liar" && badWords[4] == "backstab" ))
		{
			fail("Bad Words not set properly.");	
		}	
	}
}
