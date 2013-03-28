package net.minecraft.src.obb;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class AroundWorker {

	public interface AroundsDirectionable<T extends Enum<T> & Directionable<T>> extends Directionable<T> {
		public Direction getDirection();
	}

	public interface Directionable<T extends Enum<T> & Directionable<T>> {
		Offset getOffset();

		T next();

		T prev();

		T reverse();
	}

	public interface DirectionalWorker<T extends Enum<T> & Directionable<T>, V> {
		void work(T dir, Offset offset, V dto);
	}

	public abstract static class Each2SWorker<V, T extends Enum<T> & AroundsDirectionable<T>> implements DirectionalWorker<T, V> {
		private class InternalWorker implements TwoSideWorker<Direction, V> {
			@Override
			public void workNext(Direction dir, Offset offset, V dto) {
				doInternalWorkNext(dir, offset, dto);
			}

			@Override
			public void workPrev(Direction dir, Offset offset, V dto) {
				doInternalWorkPrev(dir, offset, dto);
			}
		}

		private final InternalWorker internalWorker = new InternalWorker();

		@Override
		public void work(T dir, Offset offset, V dto) {
			preWork(dir, offset, dto);

			Direction d = dir.getDirection();
			workTwoSidesDirection(d, internalWorker, dto);

			afterWork(dir, offset, dto);
		}

		protected void afterWork(T dir, Offset offset, V dto) {
		}

		protected abstract void doInternalWorkNext(Direction dir, Offset offset, V dto);

		protected abstract void doInternalWorkPrev(Direction dir, Offset offset, V dto);

		protected void preWork(T dir, Offset offset, V dto) {
		}

	}

	public abstract static class EnumMappingWorker<T extends Enum<T> & Directionable<T>, V> implements DirectionalWorker<T, EnumMap<T, V>> {

		@Override
		public void work(T dir, Offset offset, EnumMap<T, V> dto) {
			dto.put(dir, valueOf(dir));
		}

		protected abstract V valueOf(T dir);
	}

	public enum Offset {
		FORWARD(0, -1, OffsetIndex.KEEP, OffsetIndex.PREV), BACK(0, 1, OffsetIndex.KEEP, OffsetIndex.NEXT), //
		RIGHT(1, 0, OffsetIndex.NEXT, OffsetIndex.KEEP), LEFT(-1, 0, OffsetIndex.PREV, OffsetIndex.KEEP), //
		FORWARDRIGHT(1, -1, OffsetIndex.NEXT, OffsetIndex.PREV), FORWARDLEFT(-1, -1, OffsetIndex.PREV, OffsetIndex.PREV), //
		BACKLEFT(-1, 1, OffsetIndex.PREV, OffsetIndex.NEXT), BACKRIGHT(1, 1, OffsetIndex.NEXT, OffsetIndex.NEXT), //
		;
		public final int xOffset;
		public final int yOffset;
		public final OffsetIndex xIndex;
		public final OffsetIndex yIndex;

		private Offset(int xOffset, int yOffset, OffsetIndex xIndex, OffsetIndex yIndex) {
			this.xOffset = xOffset;
			this.yOffset = yOffset;
			this.xIndex = xIndex;
			this.yIndex = yIndex;
		}
	}

	public enum OffsetIndex {
		NEXT, PREV, KEEP, //
		;
		public static <V> EnumMap<OffsetIndex, V> toMap(V keep, V prev, V next) {
			EnumMap<OffsetIndex, V> map = new EnumMap<AroundWorker.OffsetIndex, V>(OffsetIndex.class);
			map.put(KEEP, keep);
			map.put(PREV, prev);
			map.put(NEXT, next);
			return map;
		}
	}

	public interface TwoSideWorker<T extends Enum<T> & Directionable<T>, V> {
		void workNext(T dir, Offset offset, V dto);

		void workPrev(T dir, Offset offset, V dto);
	}

	enum Around implements AroundsDirectionable<Around> {
		FORWARD(Offset.FORWARD, AxisType.Y, OffsetType.PREV), //
		RIGHT(Offset.RIGHT, AxisType.X, OffsetType.NEXT), //
		BACK(Offset.BACK, AxisType.Y, OffsetType.NEXT), //
		LEFT(Offset.LEFT, AxisType.X, OffsetType.PREV), //
		;
		public static class AxisMap<V> {
			private final EnumMap<AxisType, V> map;

			public AxisMap(V xValue, V yValue) {
				map = new EnumMap<AxisType, V>(AxisType.class);
				map.put(AxisType.X, xValue);
				map.put(AxisType.Y, yValue);
			}

			public V get(AxisType key) {
				return map.get(key);
			}
		}

		public interface AxisTypeWorker<OUT, IN> {
			OUT work(Around around, IN in, AxisType axisType);
		}

		public static class OffsetTypeMap<V> {
			private final EnumMap<OffsetType, V> map;

			public OffsetTypeMap(V prevValue, V nextValue) {
				map = new EnumMap<OffsetType, V>(OffsetType.class);
				map.put(OffsetType.PREV, prevValue);
				map.put(OffsetType.NEXT, nextValue);
			}

			public V get(OffsetType key) {
				return map.get(key);
			}
		}

		public interface OffsetTypeWorker<OUT, IN> {
			OUT work(Around around, IN in, OffsetType offsetType);
		}

		public interface TypeWorker<V, IN> {
			void work(Around around, V dto, IN in);
		}

		enum AxisType {
			X, Y,
		}

		enum OffsetType {
			NEXT, PREV,
		}

		public static <V> void foreach(DirectionalWorker<Around, V> worker, V dto) {
			AroundWorker.foreach(worker, dto, values());
		}

		public static <V, AT, OT> V forEachTypeWork(V dto, AxisTypeWorker<AT, Void> axisTypeWorker, OffsetTypeWorker<OT, AT> offsetTypeWorker,
				TypeWorker<V, OT> typeWorker) {
			for (Around around : Around.values()) {
				typeWorker.work(around, dto, offsetTypeWorker.work(around, axisTypeWorker.work(around, null, around.axisType), around.offsetType));
			}
			return dto;
		}

		public static <V, AT, OT> V forEachTypeWork(V dto, OffsetTypeWorker<OT, Void> offsetTypeWorker, AxisTypeWorker<AT, OT> axisTypeWorker,
				TypeWorker<V, AT> typeWorker) {
			for (Around around : Around.values()) {
				typeWorker.work(around, dto, axisTypeWorker.work(around, offsetTypeWorker.work(around, null, around.offsetType), around.axisType));
			}
			return dto;
		}

		private final Offset offset;
		private final AxisType axisType;
		private final OffsetType offsetType;

		private static final DirectionableChainMap<Around> MAP;

		static {
			DirectionableChainMap<Around> map = new DirectionableChainMap<AroundWorker.Around>(Around.values());
			MAP = map;
		}

		private Around(Offset offset, AxisType axisType, OffsetType offsetType) {
			this.offset = offset;
			this.axisType = axisType;
			this.offsetType = offsetType;
		}

		public AxisType getAxisType() {
			return axisType;
		}

		@Override
		public Direction getDirection() {
			return Direction.valueOf(name());
		}

		@Override
		public Offset getOffset() {
			return offset;
		}

		public OffsetType getOffsetType() {
			return offsetType;
		}

		@Override
		public Around next() {
			return MAP.next(this);
		}

		@Override
		public Around prev() {
			return MAP.prev(this);
		}

		@Override
		public Around reverse() {
			return MAP.reverse(this);
		}
	}

	enum Diagonal implements AroundsDirectionable<Diagonal> {
		FORWARDRIGHT(Offset.FORWARDRIGHT), FORWARDLEFT(Offset.FORWARDLEFT), BACKLEFT(Offset.BACKLEFT), BACKRIGHT(Offset.BACKRIGHT), //
		;
		private final Offset offset;
		private static final DirectionableChainMap<Diagonal> MAP;
		static {
			DirectionableChainMap<Diagonal> map = new DirectionableChainMap<AroundWorker.Diagonal>(Diagonal.values());
			MAP = map;
		}

		private Diagonal(Offset offset) {
			this.offset = offset;
		}

		@Override
		public Direction getDirection() {
			return Direction.valueOf(name());
		}

		@Override
		public Offset getOffset() {
			return offset;
		}

		@Override
		public Diagonal next() {
			return MAP.next(this);
		}

		@Override
		public Diagonal prev() {
			return MAP.prev(this);
		}

		@Override
		public Diagonal reverse() {
			return MAP.reverse(this);
		}
	}

	enum Direction implements AroundsDirectionable<Direction> {
		FORWARD(Offset.FORWARD), //
		FORWARDRIGHT(Offset.FORWARDRIGHT), //
		RIGHT(Offset.RIGHT), //
		BACKRIGHT(Offset.BACKRIGHT), //
		BACK(Offset.BACK), //
		BACKLEFT(Offset.BACKLEFT), //
		LEFT(Offset.LEFT), //
		FORWARDLEFT(Offset.FORWARDLEFT), //
		;
		private final Offset offset;
		private static final DirectionableChainMap<Direction> MAP;
		static {
			DirectionableChainMap<Direction> map = new DirectionableChainMap<AroundWorker.Direction>(Direction.values());
			MAP = map;
		}

		private Direction(Offset offset) {
			this.offset = offset;
		}

		@Override
		public Direction getDirection() {
			return this;
		}

		@Override
		public Offset getOffset() {
			return offset;
		}

		@Override
		public Direction next() {
			return MAP.next(this);
		}

		@Override
		public Direction prev() {
			return MAP.prev(this);
		}

		@Override
		public Direction reverse() {
			return MAP.reverse(this);
		}

	}

	private static class DirectionableChainMap<T extends Enum<T> & Directionable<T>> {
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

		private DirectionableChainMap(T[] ts) {
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

	public static <V, T extends Enum<T> & Directionable<T>> void foreach(DirectionalWorker<T, V> worker, V dto, T[] ts) {
		for (T t : ts) {
			worker.work(t, t.getOffset(), dto);
		}
	}

	public static <T extends Enum<T> & Directionable<T>, V> EnumMap<T, V> mapToEnum(Class<T> type, EnumMappingWorker<T, V> worker, T[] ts) {
		EnumMap<T, V> map = new EnumMap<T, V>(type);
		foreach(worker, map, ts);
		return map;
	}

	public static <V, T extends Enum<T> & Directionable<T>> void workTwoSidesDirection(T dir, TwoSideWorker<T, V> worker, V dto) {
		{
			T d = dir.prev();
			worker.workPrev(d, d.getOffset(), dto);
		}
		{
			T d = dir.next();
			worker.workNext(d, d.getOffset(), dto);
		}
	}
}
