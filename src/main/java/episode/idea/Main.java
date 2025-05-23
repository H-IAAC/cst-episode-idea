package episode.idea;

import episode.idea.Environment.ThingColor;

import episode.idea.utils.IdeaVisualizer;
import org.jetbrains.annotations.Nullable;
import ws3dproxy.CommandExecException;
import ws3dproxy.CommandUtility;
import ws3dproxy.model.Creature;

import java.util.logging.Logger;

class Main {
    static Logger logger = Logger.getLogger(Main.class.getName());

    static Environment env;
    static Creature agent = null;

    public static void main(String... args) {

        String episodeCategoryType = "generic";
        for (int i=0; i<args.length;i++){
            if (args[i].equals("--linear-episode")){
                episodeCategoryType = "linear";
            }
        }

        //----Environment Initialization
        env = initializeEnvironment();
        if (env == null) return;
        sleepFor(500);

        //----Initialize actors
        Creature actor1 = createActor(100, 200);
        if (actor1 == null) return;
        Creature actor2 = createActor(100, 100);
        if (actor2 == null) return;

        //----Initialize main agent that will observe the scene
        AgentMind mind = initializeAgentMind(episodeCategoryType);
        if (mind == null) return;


        IdeaVisualizer visualizer = new IdeaVisualizer(mind);
        //visualizer.addMemoryWatch("Vision");
        visualizer.addMemoryWatch("Walls");
        visualizer.addMemoryWatch("Actors");
        visualizer.addMemoryWatch("PerceptionBuffer");
        visualizer.addMemoryWatch("SimpleEpisodes");
        visualizer.setVisible(true);

        System.out.println("Moving Actors");

        try {

            moveAndWaitArrive(actor1, 700, 200);
            moveAndWaitArrive(actor2, 700, 100);

            actor1.moveto(2, 100, 100);
            sleepFor(1000);
            moveAndWaitArrive(actor2, 100, 200);

            // We expect here 4 Episodes to be detected
            // independent of the Episode Category selected

            //--Refuel actors and main agent so it does not stop moving
            CommandUtility.sendRefuel(actor1.getIndex());
            CommandUtility.sendRefuel(actor2.getIndex());
            CommandUtility.sendRefuel(agent.getIndex());

            moveAndWaitArrive(actor1, 300, 350, true, 50);
            moveAndWaitArrive(actor1, 400, 100, true, 50);
            moveAndWaitArrive(actor1, 500, 350, true, 50);
            moveAndWaitArrive(actor1, 600, 100, true, 50);
            moveAndWaitArrive(actor1, 700, 350, true, 50);


        } catch (CommandExecException e) {
            throw new RuntimeException(e);
        }
    }

    private static void moveInCircle(Creature actor, int sx, int sy) throws CommandExecException {
        moveAndWaitArrive(actor, sx, sy);
        for (int i=0; i < 100; i++){
            actor.move(2,-2,0);
            sleepFor(10);
            actor.move(2,2, 0);
            sleepFor(10);
        }
    }

    private static void moveAndWaitArrive(Creature actor, int x, int y) throws CommandExecException {
        moveAndWaitArrive(actor, x, y, false, 15);
    }

    private static void moveAndWaitArrive(Creature actor, int x, int y, boolean fastTurn, int threshold) throws CommandExecException {
        double velocity = 2;
        if (fastTurn) {
            // Hack to make agent turn fast.
            // Simulation will restrict linear velocity to 2,
            // but keep angular velocity high
            velocity = 50;
        }
        actor.moveto(velocity, x, y);
        double cx = actor.getPosition().getX();
        double cy = actor.getPosition().getY();
        while(Math.abs(cx - x) > threshold || Math.abs(cy - y) > threshold){
            actor.updateState();
            cx = actor.getPosition().getX();
            cy = actor.getPosition().getY();
            sleepFor(50);
        }
    }

    @Nullable
    private static Creature createActor(int x, int y) {
        Creature actor;
        try {
            actor = env.createCreature(x, y, 0, ThingColor.RED);
            actor.start();
            sleepFor(500);
        } catch (CommandExecException e) {
            logger.severe(String.format("Could not create actor creature at (%d, %d)", x, y));
            e.printStackTrace();
            return null;
        }
        return actor;
    }

    @Nullable
    private static AgentMind initializeAgentMind(String episodeType) {
        try {
            //                                             3 * PI / 2
            agent = env.createCreature(400, 450, 3*3.14/2.0);
            agent.start();
            sleepFor(500);
        } catch (CommandExecException e) {
            logger.severe("Could not create main agent");
            e.printStackTrace();
            return null;
        }

        return new AgentMind(agent, episodeType);
    }

    @Nullable
    private static Environment initializeEnvironment() {
        Environment env;
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
