package episode.idea.codelets.episodic;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SituationSequencerCodelet extends Codelet {

    private List<Memory> perceptionInputs;
    private long situationCounter = 0;

    private Memory perceptionBuffer;

    @Override()
    public void accessMemoryObjects() {
        perceptionInputs = getInputs();
        perceptionBuffer = getOutput("PerceptionBuffer");
    }

    @Override()
    public void calculateActivation() {
    }

    @Override()
    public void proc() {
        //TODO: Buffer size is okay?
        // Should we increase codelet time step?
        // We may need to handle situationCounter overflow
        Idea currSituation = new Idea("Situation_"+Long.toHexString(situationCounter++),"","TimeStep",1);
        for (Memory input : perceptionInputs){
            synchronized (input){
                ArrayList<Idea> inputContent = (ArrayList<Idea>) input.getI();
                currSituation.getL().addAll(inputContent);
            }
        }
        synchronized (perceptionBuffer){
            LinkedList<Idea> buffer = (LinkedList<Idea>) perceptionBuffer.getI();
            while (buffer.size() >= 3){
                buffer.poll();
            }
            buffer.add(currSituation);
        }
    }
}
