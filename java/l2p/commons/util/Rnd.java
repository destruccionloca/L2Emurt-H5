/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2p.commons.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Hack
 * Date: 06.04.2016 9:31
 */

public class Rnd
{
	private Rnd() {}
	/**
	 * generate keys for login sessions
	 * @return random number greater or equal min value of Integer and lower then max value of Integer
     */
	public static int nextInt() {
		return get(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * get integer random number between 0 and less then max. This method is equal get(int max)
	 * @param max - max number
	 * @return (int) random number equals or more then 0 and less then max
	 */
	@Deprecated
	public static int nextInt(int max) {
		return get(max);
	}

	/**
	 * This method is equal Math.random();
	 * @return random number greater or equal 0 and less then 1
     */
	public static double nextDouble() {
		return ThreadLocalRandom.current().nextDouble();
	}

	/**
	 * This method is equal Math.random(); with format to float
	 * @return random number greater or equal 0 and less then 1 with formant to float
	 */
	public static float get() {
		return (float) ThreadLocalRandom.current().nextDouble();
	}

	/**
	 * random answer with 50% chance
	 * @return true if generated number (more or equal 0 and less then 1) greater then 0.5
     */
	public static boolean nextBoolean() {
		return ThreadLocalRandom.current().nextDouble() > 0.5d;
	}

	public static byte[] nextBytes(byte[] array) {
		ThreadLocalRandom.current().nextBytes(array);
		return array;
	}

	/**
	 * boolean randomizer with custom success chance
	 * @param chance - success percent (from 0 to 100)
	 * @return response of success passage
	 */
	public static boolean chance(int chance) {
		return chance >= 1 && (chance > 99 || get(99) + 1 <= chance);
	}

	/**
	 * boolean randomizer with custom success chance
	 * @param chance - success percent (from 0 to 100)
	 * @return response of success passage
	 */
	public static boolean chance(double chance) {
		return nextDouble() <= chance / 100.;
	}

	/**
	 * get integer random number berween min and max
	 * @param min - min number
	 * @param max - max number
	 * @return (int) random number equals or more then min and less then max
	 */
	public static int get(int min, int max) {
		return (int)(Math.random() * (max - min) + min);
	}

	/**
	 * get integer random number berween min and max
	 * @param min - min number
	 * @param max - max number
	 * @return (long) random number equals or more then min and less then max
	 */
	public static long get(long min, long max) {
		return (long)(Math.random() * (max - min) + min);
	}

	/**
	 * get double random number berween min and max
	 * @param min - min number
	 * @param max - max number
	 * @return (double) random number equals or more then min and less then max
	 */
	public static double get(double min, double max) {
		return Math.random() * (max - min) + min;
	}

	/**
	 * get integer random number between 0 and max
	 * @param max - max number
	 * @return (int) random number equals or more then 0 and less then max
	 */
	public static int get(int max) {
		return get(0, max);
	}

	/**
	 * get double random number between 0 and max
	 * @param max - max number
	 * @return (double) random number equals or more then 0 and less then max
	 */
	public static double get(double max) {
		return get(0, max);
	}

	/**
	 * get double random number between 0 and max
	 * @param max - max number
	 * @return (long) random number equals or more then 0 and less then max
	 */
	public static long get(long max) {
		return get(0, max);
	}

	/**
	 * get random element from List
	 * @param list - list
	 * @param <O> - any list type
	 * @return random element of list
	 */
	public static <O> O get(List<O> list) {
		return list.get(get(list.size()));
	}

	/**
	 * get random element from array
	 * @param array - array with any type
	 * @return random element of array
	 */
	public static <O> O get(O[] array) {
		return array[get(array.length)];
	}


	public static int get(int[] list) {
		return list[get(list.length)];
	}
}
