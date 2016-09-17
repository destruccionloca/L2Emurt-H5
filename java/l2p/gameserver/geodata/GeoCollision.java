package l2p.gameserver.geodata;

import l2p.commons.geometry.Shape;

public interface GeoCollision
{
	Shape getShape();
	byte[][] getGeoAround();
	void setGeoAround(byte[][] geo);
	boolean isConcrete();
}