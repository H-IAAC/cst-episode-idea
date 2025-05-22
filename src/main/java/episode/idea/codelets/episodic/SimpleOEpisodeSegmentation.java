package episode.idea.codelets.episodic;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.representation.idea.Category;
import br.unicamp.cst.representation.idea.Idea;
import org.apache.commons.lang.NotImplementedException;

import java.util.*;

public class SimpleOEpisodeSegmentation extends Codelet {

    private Memory perceptionBuffer;
    private Memory oEpisodes;

    private Idea episodeCategoryIdea;

    public SimpleOEpisodeSegmentation(String type){
        switch (type){
            case "generic":
                episodeCategoryIdea = constructGenericEpisodeCategory();
                break;
            case "linear":
                episodeCategoryIdea = constructLinearEpisodeCategory();
                break;
            default:
                throw new NotImplementedException();
        }
    }

    @Override
    public void accessMemoryObjects() {
        perceptionBuffer = getInput("PerceptionBuffer");
        oEpisodes = getInput("SimpleEpisodes");
    }

    @Override
    public void calculateActivation() {
    }

    @Override
    public void proc() {
        // Process simple o-episodes that are being tracked
        synchronized (oEpisodes){
            ArrayList<Idea> episodesList = (ArrayList<Idea>) oEpisodes.getI();
            List<String> trackedObjectNames = new ArrayList<>();

            // Iterate over all episodes
            for (Idea episode : episodesList) {
                // Check if is not a finished episode
                boolean isEndedEpisode = episode.get("FinalSituation") != null;

                if (!isEndedEpisode) {
                    // Get initial situation from episode
                    Idea initialSituation = episode.get("InitialSituation");

                    //TODO: remove
                    assert initialSituation != null;
                    String trackedObjectName = initialSituation.getL().get(0).getName();
                    trackedObjectNames.add(trackedObjectName);

                    synchronized (perceptionBuffer) {
                        // Get current situation
                        LinkedList<Idea> situationBuffer = (LinkedList<Idea>) perceptionBuffer.getI();
                        Idea situation = situationBuffer.getLast();

                        //Check if tracked object is part of situation
                        for (Idea object : situation.getL()) {
                            if (object.getName().equals(trackedObjectName)) {
                                //Construct test episode from las 3 situations to check against
                                //episode category.
                                LinkedList<Idea> episodeSituations = (LinkedList<Idea>) episode.getL();
                                int episodeLen = episodeSituations.size();
                                Idea mockSituation = new Idea("IntermediateSituation", "");
                                mockSituation.add(object);
                                List<Idea> episodeSeq = Arrays.asList(
                                        episodeSituations.get(episodeLen - 2),
                                        episodeSituations.get(episodeLen - 1),
                                        mockSituation
                                );
                                Idea mockEpisode = new Idea("Episode", "");
                                mockEpisode.setL(episodeSeq);

                                // Check if episode continues
                                if (episodeCategoryIdea.membership(mockEpisode) > 0.5) {
                                    episodeSituations.add(mockSituation);
                                } else {//TODO: should end episode otherwise?
                                    episodeSituations.getLast().setName("FinalSituation");
                                }
                            }
                        }
                    }
                }
            }

            //Process new episodes
            synchronized (perceptionBuffer) {
                // Iterate over objects from first situation on buffer
                LinkedList<Idea> situationBuffer = (LinkedList<Idea>) perceptionBuffer.getI();
                int nSituations = situationBuffer.size();
                int requiredBufferSize = (int) episodeCategoryIdea.get("buffer_size").getValue();
                if (nSituations >= requiredBufferSize) {
                    for (Idea object : situationBuffer.getFirst().getL()) {
                        // If object is not tracked, start tracking it if is an LinearEpisode
                        String newObject = object.getName();
                        if (!trackedObjectNames.contains(newObject)) {
                            LinkedList<Idea> mockSituations = new LinkedList<>();
                            addMockSituationForObject(situationBuffer.get(nSituations - requiredBufferSize), newObject, "InitialSituation", mockSituations);
                            for (int i = requiredBufferSize-1; i > 0; i--){
                                addMockSituationForObject(situationBuffer.get(nSituations - i), newObject, "IntermediarySituation", mockSituations);
                            }
                            if (mockSituations.size() == requiredBufferSize) {
                                Idea candidateEpisode = new Idea("Episode", episodeCategoryIdea, "Episode", 1);
                                candidateEpisode.setL(mockSituations);
                                if (episodeCategoryIdea.membership(candidateEpisode) > 0.5) {
                                    episodesList.add(candidateEpisode);
                                }
                            }
                        }
                    }
                }
            }
            oEpisodes.setI(episodesList);
        }
    }

    private static void addMockSituationForObject(Idea situation, String newObject, String situationName, LinkedList<Idea> mockSituations) {
        Idea objectState = situation.get(newObject);
        if (objectState != null){
            Idea mockSituation = new Idea(situationName, "");
            mockSituation.add(objectState);
            mockSituations.add(mockSituation);
        }
    }

    private Idea constructGenericEpisodeCategory() {
        Category genericEpisodeCategory = new Category() {
            @Override
            public Idea getInstance(Idea idea) {
                //TODO
                return null;
            }

            @Override
            public double membership(Idea idea) {
                // Get object positions from each situation
                List<Idea> objectPositions = idea.getL().stream()
                        .map(situation->situation.getL().get(0))
                        .map(object->object.get("Position"))
                        .toList();

                // Check if positions changed
                double dX = (double) objectPositions.get(0).get("X").getValue()
                          - (double) objectPositions.get(1).get("X").getValue();
                double dY = (double) objectPositions.get(0).get("Y").getValue()
                          - (double) objectPositions.get(1).get("Y").getValue();

                if (dX > 1 || dY > 1) {
                    return 1.0;
                }
                return 0;
            }
        };
        Idea episodeCategory = new Idea("GenericEpisode", genericEpisodeCategory);
        episodeCategory.add(new Idea("buffer_size", 2));
        return episodeCategory;
    }

    private Idea constructLinearEpisodeCategory() {
        Category linearCategoryFunc = new Category() {
            @Override
            public Idea getInstance(Idea idea) {
                //TODO
                return null;
            }

            @Override
            public double membership(Idea idea) {
                // Get object positions from each situation
                List<Idea> objectPositions = idea.getL().stream()
                        .map(situation->situation.getL().get(0))
                        .map(object->object.get("Position"))
                        .toList();

                // Check if positions follow linear path
                double normA = getNorm(objectPositions.get(0), objectPositions.get(1));
                double normB = getNorm(objectPositions.get(1), objectPositions.get(2));
                double cos = (dotProduct(objectPositions)) / (normA * normB);
                double angle = Math.abs(Math.acos(cos));
                //System.out.printf("NormA: %.4f - NormB: %.2f - Angle: %.6f", normA, normB, angle);
                if (normA > 1 && normB > 1 && angle < 0.02) {
                    return 1.0;
                }
                return 0;
            }

            private static double getNorm(Idea pos1, Idea pos2) {
                double dX = (double) pos1.get("X").getValue()
                          - (double) pos2.get("X").getValue();
                double dY = (double) pos1.get("Y").getValue()
                          - (double) pos2.get("Y").getValue();
                return Math.hypot(dX, dY);
            }

            private static double dotProduct(List<Idea> objectPositions){
                double dXA = (double) objectPositions.get(0).get("X").getValue()
                           - (double) objectPositions.get(1).get("X").getValue();
                double dYA = (double) objectPositions.get(0).get("Y").getValue()
                           - (double) objectPositions.get(1).get("Y").getValue();

                double dXB = (double) objectPositions.get(1).get("X").getValue()
                           - (double) objectPositions.get(2).get("X").getValue();
                double dYB = (double) objectPositions.get(1).get("Y").getValue()
                           - (double) objectPositions.get(2).get("Y").getValue();

                return dXA*dXB + dYA*dYB;
            }
        };
        Idea episodeCategory = new Idea("LinearEpisode", linearCategoryFunc);
        episodeCategory.add(new Idea("buffer_size", 3));
        return episodeCategory;
    }
}
