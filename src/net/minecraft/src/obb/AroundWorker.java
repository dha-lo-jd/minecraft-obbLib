package net.minecraft.src.obb;

import java.util.EnumMap;

import net.minecraft.src.obb.EnumSupport.EnumChainMap;
import net.minecraft.src.obb.EnumSupport.EnumWorker;
import net.minecraft.src.obb.EnumSupport.ValueAccessorMap;

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

	public interface DirectionalWorker<T extends Enum<T> & Directionable<T>, V> extends EnumWorker<T, V> {
	}

	public abstract static class Each2SWorker<V, T extends Enum<T> & AroundsDirectionable<T>> implements DirectionalWorker<T, V> {
		private class InternalWorker implements TwoSideWorker<Direction, V> {
			@Override
			public void workNext(Direction dir, V dto) {
				doInternalWorkNext(dir, dto);
			}

			@Override
			public void workPrev(Direction dir, V dto) {
				doInternalWorkPrev(dir, dto);
			}
		}

		private final InternalWorker internalWorker = new InternalWorker();

		@Override
		public void work(T dir, V dto) {
			preWork(dir, dto);

			Direction d = dir.getDirection();
			workTwoSidesDirection(d, internalWorker, dto);

			afterWork(dir, dto);
		}

		protected void afterWork(T dir, V dto) {
		}

		protected abstract void doInternalWorkNext(Direction dir, V dto);

		protected abstract void doInternalWorkPrev(Direction dir, V dto);

		protected void preWork(T dir, V dto) {
		}

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
		void workNext(T dir, V dto);

		void workPrev(T dir, V dto);
	}

	enum Around implements AroundsDirectionable<Around> {
		FORWARD(Offset.FORWARD, AxisType.Y, OffsetType.NEGATIVE), //
		RIGHT(Offset.RIGHT, AxisType.X, OffsetType.POSITIVE), //
		BACK(Offset.BACK, AxisType.Y, OffsetType.POSITIVE), //
		LEFT(Offset.LEFT, AxisType.X, OffsetType.NEGATIVE), //
		;
		public static class AxisMap<V> extends ValueAccessorMap<AxisType, V> {
			private ValueAccessor xValueAccessor;
			private ValueAccessor yValueAccessor;

			public AxisMap(V xValue, V yValue) {
				this.xValueAccessor = new ValueAccessor(xValue);
				this.yValueAccessor = new ValueAccessor(yValue);
			}

			@Override
			protected ValueAccessor getAccessor(AxisType key) {
				switch (key) {
				case X:
					return xValueAccessor;
				case Y:
					return yValueAccessor;
				}
				return null;
			}
		}

		public interface AxisTypeWorker<OUT, IN> {
			OUT work(Around around, IN in, AxisType axisType);
		}

		public static class OffsetTypeMap<V> extends ValueAccessorMap<OffsetType, V> {
			private ValueAccessor prevValueAccessor;
			private ValueAccessor nextValueAccessor;

			public OffsetTypeMap(V prevValue, V nextValue) {
				this.prevValueAccessor = new ValueAccessor(prevValue);
				this.nextValueAccessor = new ValueAccessor(nextValue);
			}

			@Override
			protected ValueAccessor getAccessor(OffsetType key) {
				switch (key) {
				case NEGATIVE:
					return prevValueAccessor;
				case POSITIVE:
					return nextValueAccessor;
				}
				return null;
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
			POSITIVE, NEGATIVE,
		}

		public static <V> void foreach(DirectionalWorker<Around, V> worker, V dto) {
			EnumSupport.foreach(worker, dto, values());
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

		private static final EnumChainMap<Around> MAP;

		static {
			EnumChainMap<Around> map = new EnumChainMap<AroundWorker.Around>(Around.values());
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
		FORWARDRIGHT(Offset.FORWARDRIGHT), BACKRIGHT(Offset.BACKRIGHT), BACKLEFT(Offset.BACKLEFT), FORWARDLEFT(Offset.FORWARDLEFT), //
		;
		private final Offset offset;
		private static final EnumChainMap<Diagonal> MAP;
		static {
			EnumChainMap<Diagonal> map = new EnumChainMap<AroundWorker.Diagonal>(Diagonal.values());
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
		private static final EnumChainMap<Direction> MAP;
		static {
			EnumChainMap<Direction> map = new EnumChainMap<AroundWorker.Direction>(Direction.values());
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

	public static <V, T extends Enum<T> & Directionable<T>> void workTwoSidesDirection(T dir, TwoSideWorker<T, V> worker, V dto) {
		{
			T d = dir.prev();
			worker.workPrev(d, dto);
		}
		{
			T d = dir.next();
			worker.workNext(d, dto);
		}
	}
}
