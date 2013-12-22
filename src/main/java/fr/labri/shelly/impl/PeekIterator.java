package fr.labri.shelly.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PeekIterator<T> implements Iterator<T> {
	final Iterator<T> _backing;
	boolean _eof;
	T _lookahead;

	public PeekIterator(Iterable<T> backing) {
		this(backing.iterator());
	}

	public PeekIterator(Iterator<T> backing) {
		_backing = backing;
		try {
			next();
		} catch (NoSuchElementException e) { }
	}

	public T peek() {
		return _lookahead;
	}
	
	public boolean hasNext() {
		return _eof || _backing.hasNext();
	}
	
	public T next() {
		if(_eof) {
			_eof = false;
			T l = _lookahead;
			_lookahead = null;
			return l;
		}
		T val = _lookahead;
		_lookahead = _backing.next();
		if(!_backing.hasNext())
			_eof = true;
		return val;
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public void replace(T newLookAhead) {
		_lookahead = newLookAhead;
	}
}