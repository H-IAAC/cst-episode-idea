package episode.idea;

import episode.idea.Environment.ThingColor;

import org.jetbrains.annotations.Nullable;
import ws3dproxy.CommandExecException;
import ws3dproxy.model.Creature;

import java.util.logging.Logger;

class Main {
    static Logger logger = Logger.getLogger(Main.class.getName());

    static Environment env;

    public static void main(String[] args) {

        //----Environment Initialization
        env = initializeEnvironment();
        if (env == null) return;

        //----Initialize main agent that will observe the scene
        AgentMind mind = initializeAgentMind();
        if (mind == null) return;

        //----Initialize actors
        Creature actor1 = createActor(100, 200);
        if (actor1 == null) return;
        Creature actor2 = createActor(100, 100);
        if (actor2 == null) return;

        sleepFor(1000);
        System.out.printf("Moving Actors");

        try {
            for (int i = 0; i<2; i++){
                moveAndWaitArrive(actor1, 700, 200);
                moveAndWaitArrive(actor2, 700, 100);

                actor1.moveto(2, 100, 200);
                moveAndWaitArrive(actor2, 100, 100);

                actor1.moveto(2, 700, 100);
                sleepFor(1000);
                moveAndWaitArrive(actor2, 700, 200);

                actor1.moveto(2, 100, 200);
                sleepFor(1000);
                moveAndWaitArrive(actor2, 100, 100);

                sleepFor(500);
            }
        } catch (CommandExecException e) {
            throw new RuntimeException(e);
        }
    }

    private static void moveAndWaitArrive(Creature actor, int x, int y) throws CommandExecException {
        actor.moveto(2, x, y);
        double cx = actor.getPosition().getX();
        double cy = actor.getPosition().getY();
        while(Math.abs(cx - x) > 10 || Math.abs(cy - y) > 10){
            actor.updateState();
            cx = actor.getPosition().getX();
            cy = actor.getPosition().getY();
            sleepFor(100);
        }
    }

    @Nullable
    private static Creature createActor(int x, int y) {
        Creature actor;
        try {
            actor = env.createCreature(x, y, 0, ThingColor.RED);
            actor.start();
        } catch (CommandExecException e) {
            logger.severe(String.format("Could not create actor creature at (%d, %d)", x, y));
            e.printStackTrace();
            return null;
        }
        return actor;
    }

    @Nullable
    private static AgentMind initializeAgentMind() {
        Creature agent = null;
        try {
            //                                             3 * PI / 2
            agent = env.createCreature(400, 250, 3*3.14/2.0);
            agent.start();
        } catch (CommandExecException e) {
            logger.severe("Could not create main agent");
            e.printStackTrace();
            return null;
        }

        AgentMind mind = new AgentMind(agent);
        return mind;
    }

    @Nullable
    private static Environment initializeEnvironment() {
        Environment env = null;
        try {
            env = new Environment(100, 100);
            if (!env.initializeEnv()){
                logger.severe("Could not initialize Environment Walls");
            }
        } catch (CommandExecException e) {
            logger.severe("Could not create Environment");
            e.printStackTrace();
            return null;
        }
        return env;
    }
    
    private static void sleepFor(long len){
        try {
            Thread.sleep(len);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
