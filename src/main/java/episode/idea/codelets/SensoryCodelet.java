package episode.idea.codelets;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.representation.idea.Idea;
import ws3dproxy.CommandExecException;
import ws3dproxy.CommandUtility;
import ws3dproxy.model.Creature;
import ws3dproxy.model.CreatureState;
import ws3dproxy.model.Thing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class SensoryCodelet extends Codelet {

    private Memory vision;
    private final Creature agent;
    private List<Long> times = new ArrayList<>();

    public SensoryCodelet(Creature agent) {
        this.agent = agent;
        this.name = "Vision";
    }

    @Override()
    public void accessMemoryObjects() {
        vision = getOutput("Vision");
    }

    @Override()
    public void calculateActivation() {
    }

    @Override()
    public void proc() {
        synchronized (vision){
            try {
                long t = System.currentTimeMillis();
                CreatureState cs = CommandUtility.getCreatureState(agent.getAttributes().getName());
                times.add(System.currentTimeMillis() - t);
                List<Thing> things = cs.getThingsInVision();
                List<Idea> thingIdeas = new ArrayList<>();
                IntStream.range(0, things.size())
                        .forEach(i -> thingIdeas.add(
                                new Idea("Thing" + i, things.get(i))
                        ));
                vision.setI(thingIdeas);
            } catch (CommandExecException e) {
                throw new RuntimeException(e);
            }
            if (times.size() > 20){
                System.out.printf("Mean Time: %.2f\n", 1.0 * times.stream().reduce(0L, Long::sum) / times.size());
                times.clear();
            }
        }
    }
}
