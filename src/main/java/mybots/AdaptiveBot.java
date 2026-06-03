package mybots;

import java.awt.Color;
import java.io.*;
import robocode.*;
import robocode.util.Utils;

public class AdaptiveBot extends AdvancedRobot {

    private static final double ALPHA = 0.1;
    private static final double GAMMA = 0.9;
    private static final double EPSILON = 0.1;

    private static double[][][][][] qTable = new double[3][4][2][3][4];

    private int lastStateDist = 0;
    private int lastStateBear = 0;
    private int lastStateWall = 0;
    private int lastStateLat = 0;
    private int lastAction = 0;
    private double currentReward = 0;

    private static double epsilon = 0.3;
    private static int totalEpisodes = 0;

    @Override
    public void run() {
        setBodyColor(new Color(30, 46, 70));
        setGunColor(new Color(22, 163, 174));
        setRadarColor(new Color(246, 173, 85));
        setBulletColor(new Color(246, 173, 85));

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        loadQTable();

        while (true) {
            setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        int sDist = getDistanceState(event.getDistance());
        int sBear = getBearingState(event.getBearing());
        int sWall = getWallState();
        int sLat = getLateralVelocityState(event);

        double maxQ = getMaxQ(sDist, sBear, sWall, sLat);
        qTable[lastStateDist][lastStateBear][lastStateWall][lastStateLat][lastAction] +=
                ALPHA * (currentReward + GAMMA * maxQ - qTable[lastStateDist][lastStateBear][lastStateWall][lastStateLat][lastAction]);

        int action = chooseAction(sDist, sBear, sWall, sLat);

        executeAction(action, event);

        lastStateDist = sDist;
        lastStateBear = sBear;
        lastStateWall = sWall;
        lastStateLat = sLat;
        lastAction = action;
        currentReward = 0;
    }

    private int getLateralVelocityState(ScannedRobotEvent event) {
        double absoluteBearing = getHeadingRadians() + event.getBearingRadians();
        double lateral = event.getVelocity() * Math.sin(event.getHeadingRadians() - absoluteBearing);
        if (lateral < -2) return 0;
        if (lateral > 2) return 2;
        return 1;
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        currentReward -= 15;
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        currentReward += 30;
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        currentReward -= 2;
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        currentReward -= 10;
    }

    @Override
    public void onDeath(DeathEvent event) {
        currentReward -= 100;
        updateFinalState();
        totalEpisodes++;
        epsilon = Math.max(0.05, 0.3 * Math.exp(-0.001 * totalEpisodes));
        saveQTable();
    }

    @Override
    public void onWin(WinEvent event) {
        currentReward += 100;
        updateFinalState();
        totalEpisodes++;
        epsilon = Math.max(0.05, 0.3 * Math.exp(-0.001 * totalEpisodes));
        saveQTable();
    }

    private void updateFinalState() {
        qTable[lastStateDist][lastStateBear][lastStateWall][lastStateLat][lastAction] +=
                ALPHA * (currentReward - qTable[lastStateDist][lastStateBear][lastStateWall][lastStateLat][lastAction]);
    }

    private int getDistanceState(double distance) {
        if (distance < 200) return 0;
        if (distance < 500) return 1;
        return 2;
    }

    private int getBearingState(double bearing) {
        double absBear = Math.abs(bearing);
        if (absBear < 45) return 0;
        if (absBear < 90) return 1;
        if (absBear < 135) return 2;
        return 3;
    }

    private int getWallState() {
        double x = getX();
        double y = getY();
        double w = getBattleFieldWidth();
        double h = getBattleFieldHeight();
        double margin = 60;

        if (x < margin || x > w - margin || y < margin || y > h - margin) {
            return 1;
        }
        return 0;
    }

    private double getMaxQ(int sD, int sB, int sW, int sLat) {
        double max = -Double.MAX_VALUE;
        for (int a = 0; a < 4; a++) {
            if (qTable[sD][sB][sW][sLat][a] > max) {
                max = qTable[sD][sB][sW][sLat][a];
            }
        }
        return max;
    }

    private int chooseAction(int sD, int sB, int sW, int sLat) {
        if (Math.random() < epsilon) {
            return (int) (Math.random() * 4);
        }
        int bestAction = 0;
        double max = -Double.MAX_VALUE;
        for (int a = 0; a < 4; a++) {
            if (qTable[sD][sB][sW][sLat][a] > max) {
                max = qTable[sD][sB][sW][sLat][a];
                bestAction = a;
            }
        }
        return bestAction;
    }

    private void executeAction(int action, ScannedRobotEvent event) {
        double absoluteBearing = getHeadingRadians() + event.getBearingRadians();

        setTurnRadarRightRadians(
                Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()) * 2);
        setTurnGunRightRadians(
                Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians()));

        if (getGunHeat() == 0) {
            setFire(2);
        }

        if (action == 0) {
            setAhead(100);
        } else if (action == 1) {
            setBack(100);
        } else if (action == 2) {
            setTurnRight(45);
            setAhead(100);
        } else if (action == 3) {
            setTurnLeft(45);
            setAhead(100);
        }
    }

    private void saveQTable() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(
                    new RobocodeFileOutputStream(getDataFile("qtable.dat")));
            oos.writeObject(qTable);
            oos.writeDouble(epsilon);
            oos.writeInt(totalEpisodes);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadQTable() {
        try {
            File file = getDataFile("qtable.dat");
            if (file.exists() && file.length() > 0) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                qTable = (double[][][][][]) ois.readObject();
                epsilon = ois.readDouble();
                totalEpisodes = ois.readInt();
                ois.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}