package fr.labri.shelly.impl;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class TestPeekIterator {
	ArrayList<String> list;
	
	@BeforeMethod
	public void init() {
		list = new ArrayList<>();
	}
	
	@Test(expectedExceptions = {NoSuchElementException.class})
	public void testEmpty() {
		PeekIterator<String> it = new PeekIterator<>(list);
		it.next();
	}
	
	@Test
	public void testHasNextEmpty() {
		PeekIterator<String> it = new PeekIterator<>(list);
		it.hasNext();
	}
	
	@Test
	public void testHasNextEnd() {
		list.add("test");
		PeekIterator<String> it = new PeekIterator<>(list);
		assertTrue(it.hasNext());
		assertEquals("test", it.next());
		assertFalse(it.hasNext());
	}

	@Test
	public void testPeekEnd() {
		list.add("test");
		PeekIterator<String> it = new PeekIterator<>(list);
		assertTrue(it.hasNext());
		assertEquals("test", it.peek());
		assertEquals("test", it.next());
		assertFalse(it.hasNext());
	}
}
