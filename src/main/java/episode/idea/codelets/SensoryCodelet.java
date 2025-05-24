package episode.idea.codelets;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.representation.idea.Idea;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class SensoryCodelet extends Codelet {

    private Memory vision;
    private final Creature agent;

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
            agent.updateState();
            List<Thing> things = agent.getThingsInVision();
            List<Idea> thingIdeas = new ArrayList<>();
            IntStream.range(0, things.size())
                    .forEach(i -> thingIdeas.add(
                            new Idea("Thing" + i, things.get(i))
                    ));
            vision.setI(thingIdeas);
        }
    }
}
