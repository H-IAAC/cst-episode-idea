package episode.idea.codelets;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.representation.idea.Category;
import br.unicamp.cst.representation.idea.Idea;
import ws3dproxy.model.Thing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EntityPerceptionCodelet extends Codelet {

    private Memory vision;
    private final String categoryMemoryName;
    private Memory thingCategory;
    private final String thingMemoryOutput;
    private Memory thingMemory;

    private Category thingCategoryIdea;

    public EntityPerceptionCodelet(String categoryMemoryName, String thingMemoryOutput) {
        this.categoryMemoryName = categoryMemoryName;
        this.thingMemoryOutput = thingMemoryOutput;
    }

    @Override()
    public void accessMemoryObjects() {
        vision = getInput("Vision");
        thingCategory = getInput("ThingCategories");
        HashMap<String, Idea> categoriesMap = (HashMap<String, Idea>) thingCategory.getI();
        thingCategoryIdea = categoriesMap.get(categoryMemoryName);
        thingMemory = getOutput(thingMemoryOutput);
    }

    @Override()
    public void calculateActivation() {
    }

    @Override()
    public void proc() {
        synchronized (vision) {
            synchronized (thingMemory) {
                ArrayList<Idea> thingsList = (ArrayList<Idea>) thingMemory.getI();
                thingsList.clear();
                List<Idea> detectedThings = (List<Idea>) vision.getI();
                for (Idea thingIdea : detectedThings){
                    if (thingCategoryIdea.membership(thingIdea) > 0.5){
                        thingsList.add(constructThingIdea(thingIdea));
                    }
                }
            }
        }
    }

    private Idea constructThingIdea(Idea thingSensorIdea) {
        Thing t = (Thing) thingSensorIdea.getValue();
        String name = t.getName();
        Idea thingIdea = new Idea(name, thingCategoryIdea, "AbstractObject", 1);
        Idea posIdea = new Idea("Position", null, "Property", 1);
        posIdea.add(new Idea("X", t.getCenterPosition().getX(), "QualityDimension", 1));
        posIdea.add(new Idea("Y", t.getCenterPosition().getY(), "QualityDimension", 1));
        Idea sizeIdea = new Idea("Size", null, "Property", 1);
        sizeIdea.add(new Idea("Width", t.getWidth(), "QualityDimension", 1));
        sizeIdea.add(new Idea("Depth", t.getHeight(), "QualityDimension", 1));
        thingIdea.add(sizeIdea);
        Idea color = new Idea("Color", t.getMaterial().getColorName(), "Property", 1);
        color.add(new Idea("R", (t.getMaterial().getColor().getRed()), "QualityDimension", 1));
        color.add(new Idea("G", (t.getMaterial().getColor().getGreen()), "QualityDimension", 1));
        color.add(new Idea("B", (t.getMaterial().getColor().getBlue()), "QualityDimension", 1));
        thingIdea.add(color);
        thingIdea.add(posIdea);
        return thingIdea;
    }
}
