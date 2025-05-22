package episode.idea;

import br.unicamp.cst.representation.idea.Category;
import br.unicamp.cst.representation.idea.Idea;
import episode.idea.codelets.SensoryCodelet;
import episode.idea.codelets.EntityPerceptionCodelet;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.Mind;
import episode.idea.codelets.episodic.SimpleOEpisodeSegmentation;
import episode.idea.codelets.episodic.SituationSequencerCodelet;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class AgentMind extends Mind {

    AgentMind(Creature agent, String episodeType) {
        super();
       
       
        // Memory Groups Declaration
        createMemoryGroup("Sensory Memory");
        createMemoryGroup("Perceptual Memory");
        createMemoryGroup("Episodic Buffer");

        Memory vision;
        Memory walls;
        Memory actors;
        Memory thingCategories;
        Memory perceptionBuffer;
        Memory episodes;

        vision = createMemoryObject("Vision", new ArrayList<Thing>());
        registerMemory(vision, "Sensory Memory");
        thingCategories = createMemoryObject("ThingCategories", initializeCategories());
        registerMemory(thingCategories, "Perceptual Memory");
        walls = createMemoryObject("Walls", new ArrayList<Idea>());
        registerMemory(walls, "Perceptual Memory");
        actors = createMemoryObject("Actors", new ArrayList<Idea>());
        registerMemory(actors, "Perceptual Memory");
        perceptionBuffer = createMemoryObject("PerceptionBuffer", new LinkedList<Idea>());
        registerMemory(perceptionBuffer, "Perceptual Memory");
        episodes = createMemoryObject("SimpleEpisodes", new ArrayList<Idea>());
        registerMemory(episodes, "Episodic Buffer");
       
        Codelet sensoryCodelet = new SensoryCodelet(agent);
        sensoryCodelet.addOutput(vision);
        insertCodelet(sensoryCodelet);
       
        Codelet wallDetector = new EntityPerceptionCodelet("Wall", "Walls");
        wallDetector.addInput(vision);
        wallDetector.addInput(thingCategories);
        wallDetector.addOutput(walls);
        insertCodelet(wallDetector);

        Codelet creatureDetector = new EntityPerceptionCodelet("Creature", "Actors");
        creatureDetector.addInput(vision);
        creatureDetector.addInput(thingCategories);
        creatureDetector.addOutput(actors);
        insertCodelet(creatureDetector);

        Codelet situationSequencer = new SituationSequencerCodelet();
        situationSequencer.addInput(walls);
        situationSequencer.addInput(actors);
        situationSequencer.addOutput(perceptionBuffer);
        insertCodelet(situationSequencer);

        Codelet simpleOEpisodeSegmentation = new SimpleOEpisodeSegmentation(episodeType);
        simpleOEpisodeSegmentation.addInput(perceptionBuffer);
        simpleOEpisodeSegmentation.addInput(episodes);
        insertCodelet(simpleOEpisodeSegmentation);

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
                    //System.out.printf("TESTING IF ACTOR - CATEGORY: %d - NAME: %s\n", thing.getCategory(), thing.getName());
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
