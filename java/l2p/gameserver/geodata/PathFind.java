package l2p.gameserver.geodata;

import l2p.gameserver.Config;
import l2p.gameserver.geodata.PathFindBuffers.GeoNode;
import l2p.gameserver.geodata.PathFindBuffers.PathFindBuffer;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;

public class PathFind
{
	private int geoIndex = 0;

	private PathFindBuffer buff;

	private List<Location> path;
	private final short[] hNSWE = new short[2];
	private final Location startPoint, endPoint;
	private GeoNode startNode, endNode, currentNode;

	public PathFind(final int x, final int y, final int z, final int destX, final int destY, final int destZ, final GameObject obj, final int geoIndex)
	{
		this.geoIndex = geoIndex;

		startPoint = Config.PATHFIND_BOOST == 0 ? new Location(x, y, z) : GeoEngine.moveCheckWithCollision(x, y, z, destX, destY, true, geoIndex);
		endPoint = Config.PATHFIND_BOOST != 2 || Math.abs(destZ - z) > 200 ? new Location(destX, destY, destZ) : GeoEngine.moveCheckBackwardWithCollision(destX, destY, destZ, startPoint.x, startPoint.y, true, geoIndex);

		startPoint.world2geo();
		endPoint.world2geo();

		startPoint.z = GeoEngine.NgetHeight(startPoint.x, startPoint.y, startPoint.z, geoIndex);
		endPoint.z = GeoEngine.NgetHeight(endPoint.x, endPoint.y, endPoint.z, geoIndex);

		final int xdiff = Math.abs(endPoint.x - startPoint.x);
		final int ydiff = Math.abs(endPoint.y - startPoint.y);

		if(xdiff == 0 && ydiff == 0)
		{
			if(Math.abs(endPoint.z - startPoint.z) < 32)
			{
				path = new ArrayList<>();
				path.add(0, startPoint);
			}
			return;
		}

		final int mapSize = 2 * Math.max(xdiff, ydiff);

		if((buff = PathFindBuffers.alloc(mapSize)) != null)
		{
			buff.offsetX = startPoint.x - buff.mapSize / 2;
			buff.offsetY = startPoint.y - buff.mapSize / 2;

			//статистика
			buff.totalUses++;
			if(obj.isPlayable())
				buff.playableUses++;

			findPath();

			buff.free();

			PathFindBuffers.recycle(buff);
		}
	}

	private List<Location> findPath()
	{
		startNode = buff.nodes[startPoint.x - buff.offsetX][startPoint.y - buff.offsetY].set(startPoint.x, startPoint.y, (short) startPoint.z);

		GeoEngine.NgetHeightAndNSWE(startPoint.x, startPoint.y, (short) startPoint.z, hNSWE, geoIndex);
		startNode.z = hNSWE[0];
		startNode.nswe = hNSWE[1];
		startNode.costFromStart = 0f;
		startNode.state = GeoNode.OPENED;
		startNode.parent = null;

		endNode = buff.nodes[endPoint.x - buff.offsetX][endPoint.y - buff.offsetY].set(endPoint.x, endPoint.y, (short) endPoint.z);

		startNode.costToEnd = pathCostEstimate(startNode);
		startNode.totalCost = startNode.costFromStart + startNode.costToEnd;

		buff.open.add(startNode);

		final long nanos = System.nanoTime();
		long searhTime = 0;
		int itr = 0;

		while((searhTime = System.nanoTime() - nanos) < Config.PATHFIND_MAX_TIME && (currentNode = buff.open.poll()) != null)
		{
			itr++;
			if(currentNode.x == endPoint.x && currentNode.y == endPoint.y && Math.abs(currentNode.z - endPoint.z) < 64)
			{
				path = tracePath(currentNode);
				break;
			}

			handleNode(currentNode);
			currentNode.state = GeoNode.CLOSED;
		}

		buff.totalTime += searhTime;
		buff.totalItr += itr;
		if(path != null)
			buff.successUses++;
		else if(searhTime > Config.PATHFIND_MAX_TIME)
			buff.overtimeUses++;

		return path;
	}

	private List<Location> tracePath(GeoNode f)
	{
		final List<Location> locations = new ArrayList<>();
		do
		{
			locations.add(0, f.getLoc());
			f = f.parent;
		} while(f.parent != null);
		return locations;
	}

	private void handleNode(final GeoNode node)
	{
		final int clX = node.x;
		final int clY = node.y;
		final short clZ = node.z;

		getHeightAndNSWE(clX, clY, clZ);
		final short NSWE = hNSWE[1];

		if(Config.PATHFIND_DIAGONAL)
		{
			// Юго-восток
			if((NSWE & GeoEngine.SOUTH) == GeoEngine.SOUTH && (NSWE & GeoEngine.EAST) == GeoEngine.EAST)
			{
				getHeightAndNSWE(clX + 1, clY, clZ);
				if((hNSWE[1] & GeoEngine.SOUTH) == GeoEngine.SOUTH)
				{
					getHeightAndNSWE(clX, clY + 1, clZ);
					if((hNSWE[1] & GeoEngine.EAST) == GeoEngine.EAST)
						handleNeighbour(clX + 1, clY + 1, node, true);
				}
			}

			// Юго-запад
			if((NSWE & GeoEngine.SOUTH) == GeoEngine.SOUTH && (NSWE & GeoEngine.WEST) == GeoEngine.WEST)
			{
				getHeightAndNSWE(clX - 1, clY, clZ);
				if((hNSWE[1] & GeoEngine.SOUTH) == GeoEngine.SOUTH)
				{
					getHeightAndNSWE(clX, clY + 1, clZ);
					if((hNSWE[1] & GeoEngine.WEST) == GeoEngine.WEST)
						handleNeighbour(clX - 1, clY + 1, node, true);
				}
			}

			// Северо-восток
			if((NSWE & GeoEngine.NORTH) == GeoEngine.NORTH && (NSWE & GeoEngine.EAST) == GeoEngine.EAST)
			{
				getHeightAndNSWE(clX + 1, clY, clZ);
				if((hNSWE[1] & GeoEngine.NORTH) == GeoEngine.NORTH)
				{
					getHeightAndNSWE(clX, clY - 1, clZ);
					if((hNSWE[1] & GeoEngine.EAST) == GeoEngine.EAST)
						handleNeighbour(clX + 1, clY - 1, node, true);
				}
			}

			// Северо-запад
			if((NSWE & GeoEngine.NORTH) == GeoEngine.NORTH && (NSWE & GeoEngine.WEST) == GeoEngine.WEST)
			{
				getHeightAndNSWE(clX - 1, clY, clZ);
				if((hNSWE[1] & GeoEngine.NORTH) == GeoEngine.NORTH)
				{
					getHeightAndNSWE(clX, clY - 1, clZ);
					if((hNSWE[1] & GeoEngine.WEST) == GeoEngine.WEST)
						handleNeighbour(clX - 1, clY - 1, node, true);
				}
			}
		}

		// Восток
		if((NSWE & GeoEngine.EAST) == GeoEngine.EAST)
			handleNeighbour(clX + 1, clY, node, false);

		// Запад
		if((NSWE & GeoEngine.WEST) == GeoEngine.WEST)
			handleNeighbour(clX - 1, clY, node, false);

		// Юг
		if((NSWE & GeoEngine.SOUTH) == GeoEngine.SOUTH)
			handleNeighbour(clX, clY + 1, node, false);

		// Север
		if((NSWE & GeoEngine.NORTH) == GeoEngine.NORTH)
			handleNeighbour(clX, clY - 1, node, false);
	}

	private float pathCostEstimate(final GeoNode n)
	{
		final int diffx = endNode.x - n.x;
		final int diffy = endNode.y - n.y;
		final int diffz = endNode.z - n.z;

		return (float) Math.sqrt(diffx * diffx + diffy * diffy + diffz * diffz / 256);
	}

	private float traverseCost(final GeoNode from, final GeoNode n, final boolean d)
	{
		if(n.nswe != GeoEngine.NSWE_ALL || Math.abs(n.z - from.z) > 16)
			return 3f;
		else
		{
			getHeightAndNSWE(n.x + 1, n.y, n.z);
			if(hNSWE[1] != GeoEngine.NSWE_ALL || Math.abs(n.z - hNSWE[0]) > 16)
				return 2f;

			getHeightAndNSWE(n.x - 1, n.y, n.z);
			if(hNSWE[1] != GeoEngine.NSWE_ALL || Math.abs(n.z - hNSWE[0]) > 16)
				return 2f;

			getHeightAndNSWE(n.x, n.y + 1, n.z);
			if(hNSWE[1] != GeoEngine.NSWE_ALL || Math.abs(n.z - hNSWE[0]) > 16)
				return 2f;

			getHeightAndNSWE(n.x, n.y - 1, n.z);
			if(hNSWE[1] != GeoEngine.NSWE_ALL || Math.abs(n.z - hNSWE[0]) > 16)
				return 2f;
		}

		return d ? 1.414f : 1f;
	}

	private void handleNeighbour(final int x, final int y, final GeoNode from, final boolean d)
	{
		final int nX = x - buff.offsetX;
        final int nY = y - buff.offsetY;
        if(nX >= buff.mapSize || nX < 0 || nY >= buff.mapSize || nY < 0)
			return;

		GeoNode n = buff.nodes[nX][nY];
		final float newCost;

		if(!n.isSet())
		{
			n = n.set(x, y, from.z);
			GeoEngine.NgetHeightAndNSWE(x, y, from.z, hNSWE, geoIndex);
			n.z = hNSWE[0];
			n.nswe = hNSWE[1];
		}

		final int height = Math.abs(n.z - from.z);
		if(height > Config.PATHFIND_MAX_Z_DIFF || n.nswe == GeoEngine.NSWE_NONE)
			return;

		newCost = from.costFromStart + traverseCost(from, n, d);
		if(n.state == GeoNode.OPENED || n.state == GeoNode.CLOSED)
			if(n.costFromStart <= newCost)
				return;

		if(n.state == GeoNode.NONE)
			n.costToEnd = pathCostEstimate(n);

		n.parent = from;
		n.costFromStart = newCost;
		n.totalCost = n.costFromStart + n.costToEnd;

		if(n.state == GeoNode.OPENED)
			return;

		n.state = GeoNode.OPENED;
		buff.open.add(n);
	}

	private void getHeightAndNSWE(final int x, final int y, final short z)
	{
		final int nX = x - buff.offsetX;
        final int nY = y - buff.offsetY;
        if(nX >= buff.mapSize || nX < 0 || nY >= buff.mapSize || nY < 0)
		{
			hNSWE[1] = GeoEngine.NSWE_NONE; // Затычка
			return;
		}

		GeoNode n = buff.nodes[nX][nY];
		if(!n.isSet())
		{
			n = n.set(x, y, z);
			GeoEngine.NgetHeightAndNSWE(x, y, z, hNSWE, geoIndex);
			n.z = hNSWE[0];
			n.nswe = hNSWE[1];
		}
		else
		{
			hNSWE[0] = n.z;
			hNSWE[1] = n.nswe;
		}
	}

	public List<Location> getPath()
	{
		return path;
	}
}