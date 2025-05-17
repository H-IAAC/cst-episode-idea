package episode.idea;

import ws3dproxy.CommandExecException;
import ws3dproxy.WS3DProxy;
import ws3dproxy.model.Creature;
import ws3dproxy.model.World;

import java.util.logging.Logger;

class Main {
    static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        Environment env = null;
        try {
            env = new Environment(100, 100);
            if (!env.initializeEnv()){
                logger.severe("Could not initialize Environment Walls");
            }
        } catch (CommandExecException e) {
            logger.severe("Could not create Environment");
            e.printStackTrace();
            return;
        }

        Creature c = null;
        try {
            c = env.createCreature(10,10,0);
        } catch (CommandExecException e) {
            logger.severe("Could not create main agent");
            e.printStackTrace();
            return;
        }

        AgentMind mind = new AgentMind(c);
    }

}
