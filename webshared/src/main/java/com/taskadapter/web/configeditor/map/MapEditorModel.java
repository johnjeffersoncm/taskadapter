package com.taskadapter.web.configeditor.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Set editor model.
 */
public final class MapEditorModel implements Container,
		Container.ItemSetChangeNotifier, Container.PropertySetChangeNotifier {

	private static final long serialVersionUID = 1L;

	private static final Map<String, Class<?>> PROPTYPES = new HashMap<>();

	static {
		PROPTYPES.put("id", String.class);
		PROPTYPES.put("value", String.class);
	}

	private final List<ItemSetChangeListener> itemListeners = new LinkedList<>();

	/**
	 * Map to edit.
	 */
	private final Map<String, String> map;

	/**
	 * Next item id to use.
	 */
	private int nextId;

	/**
	 * Items list.
	 */
	private final LinkedList<ItemHolder> items = new LinkedList<>();

	public MapEditorModel(Map<String, String> map) {
		super();
		this.map = map;
		items.addAll(map.entrySet()
				.stream()
				.map(entry -> createEntry(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList()));
	}

	/**
	 * Creates a new entry.
	 * 
	 * @param key
	 *            entry key.
	 * @param value
	 *            entry value.
	 * @return entry.
	 */
	private ItemHolder createEntry(String key, String value) {
		KeyProperty keyProp = new KeyProperty(key, this);
		return new ItemHolder(nextId++, keyProp, new ValueProperty(keyProp, this, value));
	}

    @Override
    public void addPropertySetChangeListener(PropertySetChangeListener listener) {
        // not used
    }

    @Deprecated
    @Override
	public void addListener(PropertySetChangeListener listener) {
		// not used
	}

    @Override
    public void removePropertySetChangeListener(PropertySetChangeListener listener) {
        // not used
    }

    @Deprecated
    @Override
	public void removeListener(PropertySetChangeListener listener) {
		// not used
	}

    @Override
    public void addItemSetChangeListener(ItemSetChangeListener listener) {
        itemListeners.add(listener);
    }

    @Deprecated
    @Override
	public void addListener(ItemSetChangeListener listener) {
		itemListeners.add(listener);

	}

    @Override
    public void removeItemSetChangeListener(ItemSetChangeListener listener) {
        itemListeners.remove(listener);
    }

    @Deprecated
    @Override
	public void removeListener(ItemSetChangeListener listener) {
		itemListeners.remove(listener);
	}

	@Override
	public Item getItem(Object itemId) {
		final int id = (Integer) itemId;

		for (ItemHolder holder : items) {
			if (holder.id == id)
				return holder.item;
		}
		return null;
	}

	@Override
	public Collection<?> getContainerPropertyIds() {
		return PROPTYPES.keySet();
	}

	@Override
	public Collection<?> getItemIds() {
		return items.stream()
				.map(holder -> holder.id)
				.collect(Collectors.toList());
	}

	@Override
	public Property getContainerProperty(Object itemId, Object propertyId) {
		final Item item = getItem(itemId);
		if (item == null) {
			return null;
		}
		return item.getItemProperty(propertyId);
	}

	@Override
	public Class<?> getType(Object propertyId) {
		return PROPTYPES.get(propertyId);
	}

	@Override
	public int size() {
		return items.size();
	}

	@Override
	public boolean containsId(Object itemId) {
		final int id = (Integer) itemId;

		for (ItemHolder holder : items) {
			if (holder.id == id) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Item addItem(Object itemId) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object addItem() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeItem(Object itemId)
			throws UnsupportedOperationException {
		final int id = (Integer) itemId;
		final Iterator<ItemHolder> holder = items.iterator();
		while (holder.hasNext()) {
			final ItemHolder item = holder.next();
			if (item.id == id) {
				holder.remove();
				updateBinding(item.key.getValue());
				fireSetChanged();
				return true;
			}
		}

		return false;
	}

	/**
	 * Fires a "set changed" event.
	 */
	private void fireSetChanged() {
		final ItemSetChangeEvent event = new ItemSetChangeEvent() {
			private static final long serialVersionUID = 1L;

			@Override
			public Container getContainer() {
				return MapEditorModel.this;
			}
		};
		for (ItemSetChangeListener listener : itemListeners
				.toArray(new ItemSetChangeListener[itemListeners.size()])) {
			listener.containerItemSetChange(event);
		}
	}

	@Override
	public boolean addContainerProperty(Object propertyId, Class<?> type,
			Object defaultValue) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeContainerProperty(Object propertyId)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAllItems() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Updates binding between two keys.
	 * 
	 * @param oldKey
	 *            old key for a value.
	 * @param newKey
	 *            new key for a value.
	 */
	void updateBinding(String oldKey, String newKey) {
		updateBinding(oldKey);
		updateBinding(newKey);
	}

	/**
	 * Updates a binding.
	 * 
	 * @param value
	 *            key to update.
	 */
	void updateBinding(String value) {
		final ListIterator<ItemHolder> iter = items.listIterator(items.size());

		while (iter.hasPrevious()) {
			final ItemHolder item = iter.previous();
			if (value.equals(item.key.getValue())) {
				map.put(value, item.value.getValue());
				return;
			}
		}
		map.remove(value);
	}

	/**
	 * Appends a value.
	 * 
	 * @param key
	 *            entry key.
	 * @param value
	 *            entry value.
	 */
	public void append(String key, String value) {
		items.add(createEntry(key, value));
		updateBinding(key);
		fireSetChanged();
	}
}
