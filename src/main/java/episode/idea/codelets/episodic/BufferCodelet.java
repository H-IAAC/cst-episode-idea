package episode.idea.codelets.episodic;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;

public class BufferCodelet extends Codelet {

    private Memory Walls;

    private Memory PerceptionBuffer;

    @Override()
    public void accessMemoryObjects() {
        Walls = getInput("Walls");
        PerceptionBuffer = getOutput("PerceptionBuffer");
    }

    @Override()
    public void calculateActivation() {
    }

    @Override()
    public void proc() {
    }
}
