package l2p.gameserver.geodata;

import l2p.gameserver.Config;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.ExShowTrace;
import l2p.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GeoMove
{
	private static List<Location> findPath(final int x, final int y, int z, final Location target, final GameObject obj, final boolean showTrace, final int geoIndex)
	{
		if(Math.abs(z - target.z) > 256)
			return Collections.emptyList();

		z = GeoEngine.getHeight(x, y, z, geoIndex);
		target.z = GeoEngine.getHeight(target, geoIndex);

		final PathFind n = new PathFind(x, y, z, target.x, target.y, target.z, obj, geoIndex);

		if(n.getPath() == null || n.getPath().isEmpty())
			return Collections.emptyList();

		final List<Location> targetRecorder = new ArrayList<>(n.getPath().size() + 2);

		// добавляем первую точку в список (начальная позиция чара)
		targetRecorder.add(new Location(x, y, z));

		targetRecorder.addAll(n.getPath().stream().map(Location::geo2world).collect(Collectors.toList()));

		// добавляем последнюю точку в список (цель)
		targetRecorder.add(target);

		if(Config.PATH_CLEAN)
			pathClean(targetRecorder, geoIndex);

		if(showTrace && obj.isPlayer() && ((Player) obj).getVarB("trace"))
		{
			final Player player = (Player) obj;
			final ExShowTrace trace = new ExShowTrace();
			int i = 0;
			for(final Location loc : targetRecorder)
			{
				i++;
				if(i == 1 || i == targetRecorder.size())
					continue;
				trace.addTrace(loc.x, loc.y, loc.z + 15, 30000);
			}
			player.sendPacket(trace);
		}

		return targetRecorder;
	}

	public static List<List<Location>> findMovePath(final int x, final int y, final int z, final Location target, final GameObject obj, final boolean showTrace, final int geoIndex)
	{
		return getNodePath(findPath(x, y, z, target, obj, showTrace, geoIndex), geoIndex);
	}

	private static List<List<Location>> getNodePath(final List<Location> path, final int geoIndex)
	{
		final int size = path.size();
		if(size <= 1)
			return Collections.emptyList();
		final List<List<Location>> result = new ArrayList<>(size);
		for(int i = 1; i < size; i++)
		{
			final Location p2 = path.get(i);
			final Location p1 = path.get(i - 1);
			final List<Location> moveList = GeoEngine.MoveList(p1.x, p1.y, p1.z, p2.x, p2.y, geoIndex, true); // onlyFullPath = true - проверяем весь путь до конца
			if(moveList == null) // если хотя-бы через один из участков нельзя пройти, забраковываем весь путь
				return Collections.emptyList();
			if(!moveList.isEmpty()) // это может случиться только если 2 одинаковых точки подряд
				result.add(moveList);
		}
		return result;
	}

	public static List<Location> constructMoveList(final Location begin, final Location end)
	{
		begin.world2geo();
		end.world2geo();

		final int diff_x = end.x - begin.x;
        int diff_y = end.y - begin.y;
        final int diff_z = end.z - begin.z;
        final int dx = Math.abs(diff_x);
        int dy = Math.abs(diff_y);
        final int dz = Math.abs(diff_z);
        final float steps = Math.max(Math.max(dx, dy), dz);
		if(steps == 0) // Никуда не идем
			return Collections.emptyList();

		final float step_x = diff_x / steps;
        float step_y = diff_y / steps;
        final float step_z = diff_z / steps;
        float next_x = begin.x, next_y = begin.y, next_z = begin.z;

		final List<Location> result = new ArrayList<>((int) steps + 1);
		result.add(new Location(begin.x, begin.y, begin.z)); // Первая точка

		for(int i = 0; i < steps; i++)
		{
			next_x += step_x;
			next_y += step_y;
			next_z += step_z;

			result.add(new Location((int) (next_x + 0.5f), (int) (next_y + 0.5f), (int) (next_z + 0.5f)));
		}

		return result;
	}

	/**
	 * Очищает путь от ненужных точек.
	 * @param path путь который следует очистить
	 */
	private static void pathClean(final List<Location> path, final int geoIndex)
	{
		int size = path.size();
		if(size > 2)
			for(int i = 2; i < size; i++)
			{
				final Location p3 = path.get(i); // точка конца движения
				final Location p2 = path.get(i - 1); // точка в середине, кандидат на вышибание
				final Location p1 = path.get(i - 2); // точка начала движения
				if(p1.equals(p2) || p3.equals(p2) || IsPointInLine(p1, p2, p3)) // если вторая точка совпадает с первой/третьей или на одной линии с ними - она не нужна
				{
					path.remove(i - 1); // удаляем ее
					size--; // отмечаем это в размере массива
					i = Math.max(2, i - 2); // сдвигаемся назад, FIXME: может я тут не совсем прав
				}
			}

		int current = 0;
		int sub;
		while(current < path.size() - 2)
		{
			final Location one = path.get(current);
			sub = current + 2;
			while(sub < path.size())
			{
				final Location two = path.get(sub);
				if(one.equals(two) || GeoEngine.canMoveWithCollision(one.x, one.y, one.z, two.x, two.y, two.z, geoIndex)) //canMoveWithCollision  /  canMoveToCoord
					while(current + 1 < sub)
					{
						path.remove(current + 1);
						sub--;
					}
				sub++;
			}
			current++;
		}
	}

	private static boolean IsPointInLine(final Location p1, final Location p2, final Location p3)
	{
		// Все 3 точки на одной из осей X или Y.
		if(p1.x == p3.x && p3.x == p2.x || p1.y == p3.y && p3.y == p2.y)
			return true;
		// Условие ниже выполнится если все 3 точки выстроены по диагонали.
		// Это работает потому, что сравниваем мы соседние точки (расстояния между ними равны, важен только знак).
		// Для случая с произвольными точками работать не будет.
		return (p1.x - p2.x) * (p1.y - p2.y) == (p2.x - p3.x) * (p2.y - p3.y);
	}
}