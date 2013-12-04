package fr.labri.shelly.impl;

import java.util.Iterator;

public class PeekIterator implements Iterator<String> {
	boolean _eof;
	String _current;
	Iterator<String> _backing;
	
	public PeekIterator(Iterator<String> backing) {
		_backing = backing;
		next();
	}

	public String peek() {
		return _current;
	}
	
	public boolean hasNext() {
		return _eof || _backing.hasNext();
	}
	
	public String next() {
		if(_eof) {
			_eof = false;
			return _current;
		}
		String val = _current;
		_current = _backing.next();
		if(!_backing.hasNext())
			_eof = true;
		return val;
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}
}