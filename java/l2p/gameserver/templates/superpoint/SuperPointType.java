package l2p.gameserver.templates.superpoint;

public enum SuperPointType
{
	CIRCLE, // после окончания маршрута сразу же начинает его с начала
	LOOP, // после окончания маршрута возвращается по нему же в обратную сторону
	DELETE, // после окончания маршрута удаляется и запускается респавн
	FINISH, // после окончания маршрута нпц останавливается
	RANDOM // следующая точка маршрута выбирается случайно
}
