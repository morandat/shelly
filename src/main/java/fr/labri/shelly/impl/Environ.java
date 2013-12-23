package fr.labri.shelly.impl;

import java.lang.reflect.Member;
import java.util.ArrayList;

import fr.labri.shelly.Composite;
import fr.labri.shelly.Group;
import fr.labri.shelly.Terminal;

public class Environ {
	ArrayList<Entry> map = new ArrayList<>();
	
	public void push(Composite<Class<?>, Member> composite, Object object) {
		map.add(new Entry(composite, object));
	}
	
	public Object get(Composite<Class<?>, Member> composite) {
		int i = size();
		while(i-- > 0) {
			Entry e = map.get(i);
			if(e.key.equals(composite))
				return e.val;
		}
		return null;
	}

	public Composite<Class<?>, Member> pop() {
		return map.remove(last()).key;
	}

	public Group<Class<?>, Member> drop() {
		Composite<Class<?>, Member> key;
		do 
			key = map.remove(last()).key;
		while(!(key instanceof Group));
		return (Group<Class<?>, Member>) key;
	}
	
	public Object getLast() {
		if (map.isEmpty())
			return null;
		return map.get(last()).val;
	}

	public Object get(Terminal<Class<?>, Member> terminal) {
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