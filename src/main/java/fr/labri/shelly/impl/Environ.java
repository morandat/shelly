package fr.labri.shelly.impl;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

import fr.labri.shelly.Composite;
import fr.labri.shelly.Terminal;

public class Environ {
	List<Composite<Class<?>, Member>> _keys = new ArrayList<>();
	List<Object> _objects = new ArrayList<>();
	
	void push(Composite<Class<?>, Member> composite, Object object) {
		_keys.add(composite);
		_objects.add(object);
	}
	
	Object fetch(Composite<Class<?>, Member> composite) {
		int idx = _keys.lastIndexOf(composite);
		return (idx == -1) ? null : _objects.get(idx);
	}

	public void pop() {
		_keys.remove(_keys.size() - 1);
		_objects.remove(_objects.size() - 1);
	}

	public Object getLast() {
		return _objects.get(_objects.size() - 1);
	}

	public Object fetch(Terminal<Class<?>, Member> terminal) {
		return fetch(terminal.getParent());
	}
}
