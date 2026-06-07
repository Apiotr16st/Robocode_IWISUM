package mybots;

import java.awt.Color;
import robocode.*;
import robocode.util.Utils;

public class TrainingOpponent extends AdvancedRobot {

    private double moveDirection = 1;

    @Override
    public void run() {
        setBodyColor(new Color(50, 0, 0));
        setGunColor(new Color(255, 0, 0));
        setRadarColor(new Color(255, 100, 100));
        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);

        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

        while (true) {
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        double absBearing = getHeadingRadians() + e.getBearingRadians();

        double enemyHeading = e.getHeadingRadians();
        double enemyVelocity = e.getVelocity();
        double distance = e.getDistance();

        double firePower = 2.0;
        double bulletSpeed = 20 - 3 * firePower;

        double time = distance / bulletSpeed;
        double predictedX = getX() + distance * Math.sin(absBearing) + enemyVelocity * time * Math.sin(enemyHeading);
        double predictedY = getY() + distance * Math.cos(absBearing) + enemyVelocity * time * Math.cos(enemyHeading);

        double predictedBearing = Math.atan2(predictedX - getX(), predictedY - getY());

        setTurnGunRightRadians(Utils.normalRelativeAngle(predictedBearing - getGunHeadingRadians()));
        setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()) * 2);

        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
            setFire(firePower);
        }

        setTurnRightRadians(Utils.normalRelativeAngle(absBearing - Math.PI / 2 + 0.2 * moveDirection - getHeadingRadians()));
        setAhead(150 * moveDirection);
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        moveDirection = -moveDirection;
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        moveDirection = -moveDirection;
    }
}