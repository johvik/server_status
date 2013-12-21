package server.status.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class SortedList<E extends Comparable<E>> implements Collection<E>,
		Iterable<E> {
	private ArrayList<E> list = new ArrayList<E>();

	@Override
	public boolean add(E e) {
		int index = Collections.binarySearch(list, e);
		if (index < 0) {
			list.add((-index) - 1, e);
			return true;
		}
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		int oldSize = list.size();
		for (E e : c) {
			add(e);
		}
		return oldSize != list.size();
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public boolean contains(Object o) {
		@SuppressWarnings("unchecked")
		int index = Collections.binarySearch(list, (E) o);
		return index >= 0;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (!contains(o)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Finds object in the list.
	 * 
	 * @param e
	 *            Object to find.
	 * @return Instance of e in list or null.
	 */
	public E find(E e) {
		int index = Collections.binarySearch(list, e);
		if (index >= 0) {
			return list.get(index);
		}
		return null;
	}

	public E get(int index) {
		return list.get(index);
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	/**
	 * Gets the index of element e.
	 * 
	 * @param e
	 *            Element to find
	 * @return Index of element or -1 if not found.
	 */
	public int indexOf(E e) {
		int index = Collections.binarySearch(list, e);
		if (index >= 0) {
			return index;
		}
		return -1;
	}

	@Override
	public Iterator<E> iterator() {
		return list.iterator();
	}

	@Override
	public boolean remove(Object o) {
		@SuppressWarnings("unchecked")
		int index = Collections.binarySearch(list, (E) o);
		if (index >= 0) {
			list.remove(index);
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		int oldSize = list.size();
		for (Object o : c) {
			remove(o);
		}
		return oldSize != list.size();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] array) {
		return list.toArray(array);
	}

	public boolean update(E e) {
		int index = Collections.binarySearch(list, e);
		if (index >= 0) {
			list.set(index, e);
			return true;
		}
		return false;
	}
}
