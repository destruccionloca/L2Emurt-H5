package l2p.gameserver.geodata;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2p.commons.text.StrTable;
import l2p.gameserver.Config;
import l2p.gameserver.utils.Location;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PathFindBuffers
{
	public final static int MIN_MAP_SIZE = 1 << 6;
	public final static int STEP_MAP_SIZE = 1 << 5;
	public final static int MAX_MAP_SIZE = 1 << 9;

	private static final TIntObjectHashMap<PathFindBuffer[]> buffers = new TIntObjectHashMap<>();
	private static int[] sizes = new int[0];
	private static final Lock lock = new ReentrantLock();

	static
	{
		final TIntIntHashMap config = new TIntIntHashMap();
		String[] k;
		for(final String e : Config.PATHFIND_BUFFERS.split(";"))
			if(!e.isEmpty() && (k = e.split("x")).length == 2)
				config.put(Integer.valueOf(k[1]), Integer.valueOf(k[0]));

		final TIntIntIterator itr = config.iterator();

		while(itr.hasNext())
		{
			itr.advance();
			final int size = itr.key();
			final int count = itr.value();

			final PathFindBuffer[] buff = new PathFindBuffer[count];
			for(int i = 0; i < count; i++)
				buff[i] = new PathFindBuffer(size);

			buffers.put(size, buff);
		}

		sizes = config.keys();
		Arrays.sort(sizes);
	}

	private static PathFindBuffer create(final int mapSize)
	{
		lock.lock();
		try
		{
			final PathFindBuffer buffer;
			PathFindBuffer[] buff = buffers.get(mapSize);
			if(buff != null)
				buff = l2p.commons.lang.ArrayUtils.add(buff, buffer = new PathFindBuffer(mapSize));
			else
			{
				buff = new PathFindBuffer[] { buffer = new PathFindBuffer(mapSize) };
				sizes = org.apache.commons.lang3.ArrayUtils.add(sizes, mapSize);
				Arrays.sort(sizes);
			}
			buffers.put(mapSize, buff);
			buffer.inUse = true;
			return buffer;
		}
		finally
		{
			lock.unlock();
		}
	}

	private static PathFindBuffer get(final int mapSize)
	{
		lock.lock();
		try
		{
			final PathFindBuffer[] buff = buffers.get(mapSize);
			for(final PathFindBuffer buffer : buff)
				if(!buffer.inUse)
				{
					buffer.inUse = true;
					return buffer;
				}
			return null;
		}
		finally
		{
			lock.unlock();
		}
	}

	public static PathFindBuffer alloc(int mapSize)
	{
		if(mapSize > MAX_MAP_SIZE)
			return null;
		mapSize += STEP_MAP_SIZE;
		if(mapSize < MIN_MAP_SIZE)
			mapSize = MIN_MAP_SIZE;

		PathFindBuffer buffer = null;
		for (final int size1 : sizes)
			if (size1 >= mapSize) {
				mapSize = size1;
				buffer = get(mapSize);
				break;
			}

		//Не найден свободный буффер, или буфферов под такой размер нет
		if(buffer == null)
			for(int size = MIN_MAP_SIZE; size < MAX_MAP_SIZE; size += STEP_MAP_SIZE)
				if(size >= mapSize)
				{
					mapSize = size;
					buffer = create(mapSize);
					break;
				}

		return buffer;
	}

	public static void recycle(final PathFindBuffer buffer)
	{
		lock.lock();
		try
		{
			buffer.inUse = false;
		}
		finally
		{
			lock.unlock();
		}
	}

	public static StrTable getStats()
	{
		final StrTable table = new StrTable("PathFind Buffers Stats");
		lock.lock();
		try
		{
			long totalUses = 0, totalPlayable = 0, totalTime = 0;
			int index = 0;
			int count;
			long uses;
			long playable;
			long itrs;
			long success;
			long overtime;
			long time;

			for(final int size : sizes)
			{
				index++;
				count = 0;
				uses = 0;
				playable = 0;
				itrs = 0;
				success = 0;
				overtime = 0;
				time = 0;
				for(final PathFindBuffer buff : buffers.get(size))
				{
					count++;
					uses += buff.totalUses;
					playable += buff.playableUses;
					success += buff.successUses;
					overtime += buff.overtimeUses;
					time += buff.totalTime / 1000000;
					itrs += buff.totalItr;
				}

				totalUses += uses;
				totalPlayable += playable;
				totalTime += time;

				table.set(index, "Size", size);
				table.set(index, "Count", count);
				table.set(index, "Uses (success%)", uses + "(" + String.format("%2.2f", uses > 0 ? success * 100. / uses : 0) + "%)");
				table.set(index, "Uses, playble", playable + "(" + String.format("%2.2f", uses > 0 ? playable * 100. / uses : 0) + "%)");
				table.set(index, "Uses, overtime", overtime + "(" + String.format("%2.2f", uses > 0 ? overtime * 100. / uses : 0) + "%)");
				table.set(index, "Iter., avg", uses > 0 ? itrs / uses : 0);
				table.set(index, "Time, avg (ms)", String.format("%1.3f", uses > 0 ? (double) time / uses : 0.));
			}

			table.addTitle("Uses, total / playable  : " + totalUses + " / " + totalPlayable);
			table.addTitle("Uses, total time / avg (ms) : " + totalTime + " / " + String.format("%1.3f", totalUses > 0 ? (double) totalTime / totalUses : 0));
		}
		finally
		{
			lock.unlock();
		}

		return table;
	}

	public static class PathFindBuffer
	{
		final int mapSize;
		final GeoNode[][] nodes;
		final Queue<GeoNode> open;
		int offsetX, offsetY;
		boolean inUse;

		//статистика
		long totalUses;
		long successUses;
		long overtimeUses;
		long playableUses;
		long totalTime;
		long totalItr;

		public PathFindBuffer(final int mapSize)
		{
			open = new PriorityQueue<>(mapSize);
			this.mapSize = mapSize;
			nodes = new GeoNode[mapSize][mapSize];
			for(int i = 0; i < nodes.length; i++)
				for(int j = 0; j < nodes[i].length; j++)
					nodes[i][j] = new GeoNode();
		}

		public void free()
		{
			open.clear();
			for (final GeoNode[] node : nodes)
				for (final GeoNode aNode : node) {
					aNode.free();
				}
		}
	}

	public static class GeoNode implements Comparable<GeoNode>
	{
		public final static int NONE = 0;
		public final static int OPENED = 1;
		public final static int CLOSED = -1;

		public int x, y;
		public short z, nswe;
		public float totalCost, costFromStart, costToEnd;
		public int state;

		public GeoNode parent;

		public GeoNode()
		{
			nswe = -1;
		}

		public GeoNode set(final int x, final int y, final short z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			return this;
		}

		public boolean isSet()
		{
			return nswe != -1;
		}

		public void free()
		{
			nswe = -1;
			costFromStart = 0f;
			totalCost = 0f;
			costToEnd = 0f;
			parent = null;
			state = NONE;
		}

		public Location getLoc()
		{
			return new Location(x, y, z);
		}

		@Override
		public String toString()
		{
			return "[" + x + "," + y + "," + z + "] f: " + totalCost;
		}

		@Override
		public int compareTo(final GeoNode o)
		{
			if(totalCost > o.totalCost)
				return 1;
			if(totalCost < o.totalCost)
				return -1;
			return 0;
		}
	}
}