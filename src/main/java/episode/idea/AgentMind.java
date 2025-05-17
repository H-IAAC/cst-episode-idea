package episode.idea;

import br.unicamp.cst.representation.idea.Category;
import br.unicamp.cst.representation.idea.Idea;
import episode.idea.codelets.SensoryCodelet;
import episode.idea.codelets.ThingDetector;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.Mind;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

import java.util.HashMap;

public class AgentMind extends Mind {

    AgentMind(Creature agent) {
        super();
       
       
        // Memory Groups Declaration
        createMemoryGroup("Sensory Memory");
        createMemoryGroup("Perceptual Memory");
       
        Memory vision;
        Memory walls;
        Memory actors;
        Memory thingCategories;

        vision = createMemoryObject("Vision");
        registerMemory(vision, "Sensory Memory");
        thingCategories = createMemoryObject("ThingCategories", initializeCategories());
        registerMemory(thingCategories, "Perceptual Memory");
        walls = createMemoryObject("Walls");
        registerMemory(walls, "Perceptual Memory");
        actors = createMemoryObject("Actors");
        registerMemory(actors, "Perceptual Memory");
       
        Codelet sensoryCodelet = new SensoryCodelet(agent);
        sensoryCodelet.addOutput(vision);
        insertCodelet(sensoryCodelet);
       
        Codelet wallDetector = new ThingDetector("Wall", "Walls");
        wallDetector.addInput(vision);
        wallDetector.addInput(thingCategories);
        wallDetector.addOutput(walls);
        insertCodelet(wallDetector);

        Codelet creatureDetector = new ThingDetector("Creature", "Actors");
        creatureDetector.addInput(vision);
        creatureDetector.addInput(thingCategories);
        creatureDetector.addOutput(actors);
        insertCodelet(creatureDetector);

        for (Codelet c : this.getCodeRack().getAllCodelets()) {
            c.setTimeStep(200);
        }
        start();
    }

    private HashMap<String, Idea> initializeCategories() {
        Category wallCategoryFunction = new Category() {
            @Override
            public Idea getInstance(Idea idea) {
                //TODO
                return new Idea();
            }

            @Override
            public double membership(Idea idea) {
                Object value = idea.getValue();
                if (value instanceof Thing) {
                    Thing thing = (Thing) value;
                    if (thing.getCategory() == 1) return 1.0;
                }
                return 0;
            }
        };
        Idea wallCategory = new Idea("WALL", wallCategoryFunction, "AbstractObject", 2);

        Category creatureCategoryFunction = new Category() {
            @Override
            public Idea getInstance(Idea idea) {
                //TODO
                return new Idea();
            }

            @Override
            public double membership(Idea idea) {
                Object value = idea.getValue();
                if (value instanceof Thing) {
                    Thing thing = (Thing) value;
                    if (thing.getCategory() == 0) return 1.0;
                }
                return 0;
            }
        };
        Idea creatureCategory = new Idea("CREATURE", creatureCategoryFunction, "AbstractObject", 2);
        return new HashMap<>(){{
            put("Wall", wallCategory);
            put("Creature", creatureCategory);
        }};
    }
}
