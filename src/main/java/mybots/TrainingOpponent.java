package mybots;

import robocode.*;
import robocode.util.Utils;

public class TrainingOpponent extends AdvancedRobot {

    private double direction = 1;

    @Override
    public void run() {
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
        while (true) {
            setAhead(150 * direction);
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
        setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()) * 2);
        setTurnGunRightRadians(Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians()));
        if (getGunHeat() == 0) {
            setFire(1.5);
        }
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        direction *= -1;
        setTurnRight(45);
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        direction *= -1;
    }
}