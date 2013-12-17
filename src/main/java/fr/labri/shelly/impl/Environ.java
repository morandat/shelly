package fr.labri.shelly.impl;

import java.lang.reflect.Member;
import java.util.ArrayList;

import fr.labri.shelly.Composite;
import fr.labri.shelly.Terminal;

public class Environ {
	ArrayList<Entry> map = new ArrayList<>();
	
	void push(Composite<Class<?>, Member> composite, Object object) {
		map.add(new Entry(composite, object));
	}
	
	public Object get(Composite<Class<?>, Member> composite) {
		int i = last();
		while(i-- > 0) {
			Entry e = map.get(i);
			if(e.key.equals(composite))
				return e.val;
		}
		return null;
	}

	public void pop() {
		map.remove(last());
	}

	public Object getLast() {
		return map.get(last());
	}

	public Object fetch(Terminal<Class<?>, Member> terminal) {
		return get(terminal.getParent());
	}

	
	public int size() {
		return map.size();
	}
	public int last() {
		return map.size() - 1;
	}
	final static class Entry {
		final Composite<Class<?>, Member> key;
		Object val;
		Entry(Composite<Class<?>, Member> key, Object value){
			this.key = key;
			this.val = value;
		}
	}
}