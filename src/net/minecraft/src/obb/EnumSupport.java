package net.minecraft.src.obb;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class EnumSupport {

	public static class EnumChainMap<T extends Enum<T>> {
		private class DirectionableChain {
			T prev;
			T next;
			T reverse;

			private DirectionableChain(T prev, T next, T reverse) {
				this.prev = prev;
				this.next = next;
				this.reverse = reverse;
			}

		}

		private final Map<T, DirectionableChain> map;

		public EnumChainMap(T[] ts) {
			Map<T, DirectionableChain> map = new HashMap<T, DirectionableChain>();
			int reverseAmount = ts.length / 2;
			for (int i = 0; i < ts.length; i++) {
				T t = ts[i];

				int nextIdx = i + 1;
				nextIdx = nextIdx < ts.length ? nextIdx : nextIdx - ts.length;

				int prevIdx = i - 1;
				prevIdx = prevIdx >= 0 ? prevIdx : prevIdx + ts.length;

				int reverseIdx = i + reverseAmount;
				reverseIdx = reverseIdx < ts.length ? reverseIdx : reverseIdx - ts.length;

				map.put(t, new DirectionableChain(ts[prevIdx], ts[nextIdx], ts[reverseIdx]));
			}
			this.map = map;
		}

		public T next(T dir) {
			return map.get(dir).next;
		}

		public T prev(T dir) {
			return map.get(dir).prev;
		}

		public T reverse(T dir) {
			return map.get(dir).reverse;
		}
	}

	public abstract static class EnumMappingWorker<T extends Enum<T>, V> implements EnumWorker<T, EnumMap<T, V>> {

		@Override
		public void work(T e, EnumMap<T, V> dto) {
			dto.put(e, valueOf(e));
		}

		protected abstract V valueOf(T e);
	}

	public interface EnumWorker<T extends Enum<T>, V> {
		void work(T e, V dto);
	}

	public static abstract class ValueAccessorMap<T, V> {
		protected class ValueAccessor {
			V value;

			protected ValueAccessor(V value) {
				this.value = value;
			}

			protected V getValue() {
				return value;
			}

			protected void setValue(V value) {
				this.value = value;
			}
		}

		public V get(T key) {
			ValueAccessor accessor = getAccessor(key);
			if (accessor == null) {
				return null;
			}
			return accessor.getValue();
		}

		public void put(T key, V value) {
			ValueAccessor accessor = getAccessor(key);
			if (accessor == null) {
				return;
			}
			accessor.setValue(value);
		}

		protected abstract ValueAccessor getAccessor(T key);
	}

	public static <V, T extends Enum<T>> void foreach(EnumWorker<T, V> worker, V dto, T[] enums) {
		for (T t : enums) {
			worker.work(t, dto);
		}
	}

	public static <T extends Enum<T>, V> EnumMap<T, V> mapToEnum(Class<T> type, EnumMappingWorker<T, V> worker, T[] enums) {
		EnumMap<T, V> map = new EnumMap<T, V>(type);
		foreach(worker, map, enums);
		return map;
	}
}
