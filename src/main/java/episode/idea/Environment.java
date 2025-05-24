package episode.idea;

import ws3dproxy.CommandExecException;
import ws3dproxy.WS3DProxy;
import ws3dproxy.model.Creature;
import ws3dproxy.model.World;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Environment {

    private final static int WALL_WIDTH = 5;

    WS3DProxy proxy;
    World world;
    Logger logger = Logger.getLogger(Environment.class.getName());

    public Environment(int width, int height) throws CommandExecException {
        proxy = new WS3DProxy();
        world = World.getInstance();
        //world.setEnvironmentDimension(width, height);
        world.reset();
    }

    public Creature createCreature(double x, double y, double p) throws CommandExecException {
        return createCreature(x, y, p, ThingColor.RED);
    }

    public Creature createCreature(double x, double y, double p, ThingColor color) throws CommandExecException {
        return proxy.createCreature(x, y, p, color.ordinal());
    }

    public boolean initializeEnv(){
        boolean isAllWallsCreated = true;
        isAllWallsCreated &= insertWall(ThingColor.BLUE, 0,0,800,0);
        isAllWallsCreated &= insertWall(ThingColor.BLUE, 0,0,0,600);
        isAllWallsCreated &= insertWall(ThingColor.BLUE, 800,0,800,600);
        isAllWallsCreated &= insertWall(ThingColor.BLUE, 800,600,0,600);

        isAllWallsCreated &= insertWall(ThingColor.GREEN, 375, 10, 425, 20);
        isAllWallsCreated &= insertWall(ThingColor.MAGENTA, 325, 10, 375, 20);
        isAllWallsCreated &= insertWall(ThingColor.YELLOW, 425, 10, 475, 20);

        return isAllWallsCreated;
    }

    protected boolean insertWall(ThingColor color, double x1, double y1, double x2, double y2){
        try {
            World.createBrick(color.ordinal(), x1 - WALL_WIDTH, y1 - WALL_WIDTH, x2, y2);
            return true;
        } catch (CommandExecException e) {
            logger.severe(String.format("Could not create WALL with color %s at (%.1f, %.1f) (%.1f, %.1f)", color.name(), x1, y1, x2, y2));
            return false;
        }
    }

    public void shutdown() {
        try {
            world.reset();
        } catch (CommandExecException e) {
            Logger.getLogger(this.getClass().getName()).severe("Could not reset world");
        }
    }

    //TODO: Put colors in correct order
    public enum ThingColor{
        RED,
        GREEN,
        BLUE,
        YELLOW,
        MAGENTA,
        WHITE
    }
}
