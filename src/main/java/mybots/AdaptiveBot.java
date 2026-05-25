package mybots;

import java.awt.Color;
import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/**
 * A starter bot that keeps its radar locked on an opponent and changes
 * evasive direction when its current movement gets punished by a hit.
 */
public class AdaptiveBot extends AdvancedRobot {
    private static final double MOVE_DISTANCE = 160;

    private int movementDirection = 1;
    private int scanCount;

    @Override
    public void run() {
        setBodyColor(new Color(30, 46, 70));
        setGunColor(new Color(22, 163, 174));
        setRadarColor(new Color(246, 173, 85));
        setBulletColor(new Color(246, 173, 85));

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

        while (true) {
            if (getDistanceRemaining() == 0) {
                setAhead(movementDirection * MOVE_DISTANCE);
            }
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        double absoluteBearing = getHeadingRadians() + event.getBearingRadians();

        double radarTurn = Utils.normalRelativeAngle(
                absoluteBearing - getRadarHeadingRadians());
        setTurnRadarRightRadians(radarTurn * 2);

        double gunTurn = Utils.normalRelativeAngle(
                absoluteBearing - getGunHeadingRadians());
        setTurnGunRightRadians(gunTurn);

        double firePower = Math.min(3, Math.max(1, 500 / event.getDistance()));
        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 8) {
            setFire(firePower);
        }

        setTurnRight(event.getBearing() + 90 - (18 * movementDirection));
        if (++scanCount % 12 == 0) {
            setAhead(movementDirection * MOVE_DISTANCE);
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        reverseMovement();
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        reverseMovement();
    }

    private void reverseMovement() {
        movementDirection = -movementDirection;
        setAhead(movementDirection * MOVE_DISTANCE);
    }
}
