package com.fpetrola.cap.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public interface BidirectionalBinder<T, K> extends Binder {

	List<T> pull(K source);

	default List<Supplier> getSuppliers(Supplier<K> valueSupplier) {
		List<T> pull = pull(valueSupplier.get());

		List<Supplier> suppliers = new ArrayList<>();

		for (int i = 0; i < pull.size(); i++) {
			final int index = i;
			Supplier<Object> supplier = new Supplier<Object>() {

				public Object get() {
					List object = pull(valueSupplier.get());
					return object.get(index);
				}

				public String toString() {
					return get().toString();
				}
			};

			suppliers.add(supplier);
		}
		return suppliers;
	}

}
