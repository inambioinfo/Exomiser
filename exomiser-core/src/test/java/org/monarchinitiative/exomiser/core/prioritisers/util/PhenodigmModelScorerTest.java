package org.monarchinitiative.exomiser.core.prioritisers.util;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PhenodigmModelScorerTest {

    private PriorityService priorityService = TestPriorityServiceFactory.TEST_SERVICE;

    private Model makeBestHumanModel(OrganismPhenotypeMatcher referenceOrganismPhenotypeMatcher) {
        List<String> exactHumanPhenotypes = getBestMatchedPhenotypes(referenceOrganismPhenotypeMatcher);
        return new GeneDiseaseModel("DISEASE:1", Organism.HUMAN, 12345, "GENE1", "DISEASE:1", "disease", exactHumanPhenotypes);
    }

    private Model makeBestMouseModel(OrganismPhenotypeMatcher mouseOrganismPhenotypeMatcher) {
        List<String> exactMousePhenotypes = getBestMatchedPhenotypes(mouseOrganismPhenotypeMatcher);
        return new GeneOrthologModel("MOUSE:1", Organism.MOUSE, 12345, "GENE1", "MGI:12345", "gene1", exactMousePhenotypes);
    }

    private Model makeBestFishModel(OrganismPhenotypeMatcher fishOrganismPhenotypeMatcher) {
        List<String> exactFishPhenotypes = getBestMatchedPhenotypes(fishOrganismPhenotypeMatcher);
        return new GeneOrthologModel("FISH:1", Organism.FISH, 12345, "GENE1", "ZFIN:12345", "gene-1", exactFishPhenotypes);
    }

    private List<String> getBestMatchedPhenotypes(OrganismPhenotypeMatcher organismPhenotypeMatcher) {
        return organismPhenotypeMatcher.getBestPhenotypeMatches()
                .stream()
                .map(PhenotypeMatch::getMatchPhenotypeId)
                .collect(toList());
    }

    @Test
    public void testScoreModelNoPhenotypesNoMatches() {
        OrganismPhenotypeMatcher emptyMatches = new OrganismPhenotypeMatcher(Organism.HUMAN, Collections.emptyMap());

        ModelScorer instance = ModelScorer.forSameSpecies(emptyMatches);

        Model model = new GeneDiseaseModel("DISEASE:1", Organism.HUMAN, 12345, "GENE1", "DISEASE:1", "disease", Collections.emptyList());

        ModelPhenotypeMatchScore result = instance.scoreModel(model);

        System.out.println(result);
        assertThat(result.getScore(), equalTo(0.0));
    }

    @Test
    public void testScoreModelNoMatch() {
        List<PhenotypeTerm> queryTerms = ImmutableList.copyOf(priorityService.getHpoTerms());
        OrganismPhenotypeMatcher referenceOrganismPhenotypeMatcher = priorityService.getPhenotypeMatcherForOrganism(queryTerms, Organism.HUMAN);

        ModelScorer instance = ModelScorer.forSameSpecies(referenceOrganismPhenotypeMatcher);

        PhenotypeTerm noMatchTerm = PhenotypeTerm.of("HP:000000", "No term");
        //The model should have no phenotypes in common with the query set.
        assertThat(queryTerms.contains(noMatchTerm), is(false));
        Model model = new GeneDiseaseModel("DISEASE:2", Organism.HUMAN, 12345, "GENE2", "DISEASE:2", "disease 2", Collections.singletonList(noMatchTerm.getId()));
        ModelPhenotypeMatchScore result = instance.scoreModel(model);

        System.out.println(result);
        assertThat(result.getScore(), equalTo(0.0));
        assertThat(result.getBestPhenotypeMatches().isEmpty(), is(true));
    }

    @Test
    public void testScoreModelPerfectMatch() {
        List<PhenotypeTerm> queryTerms = ImmutableList.copyOf(priorityService.getHpoTerms());
        OrganismPhenotypeMatcher referenceOrganismPhenotypeMatcher = priorityService.getPhenotypeMatcherForOrganism(queryTerms, Organism.HUMAN);

        ModelScorer instance = ModelScorer.forSameSpecies(referenceOrganismPhenotypeMatcher);

        Model model = makeBestHumanModel(referenceOrganismPhenotypeMatcher);
        ModelPhenotypeMatchScore result = instance.scoreModel(model);

        System.out.println(result);
        assertThat(result.getScore(), equalTo(1.0));
    }

    @Test
    public void testScoreModelPartialMatch() {

        List<PhenotypeTerm> queryTerms = ImmutableList.copyOf(priorityService.getHpoTerms());
        OrganismPhenotypeMatcher referenceOrganismPhenotypeMatcher = priorityService.getPhenotypeMatcherForOrganism(queryTerms, Organism.HUMAN);

        ModelScorer instance = ModelScorer.forSameSpecies(referenceOrganismPhenotypeMatcher);

        List<String> twoExactPhenotypeMatches = queryTerms.stream().limit(2).map(PhenotypeTerm::getId).collect(toList());

        Model model = new GeneDiseaseModel("DISEASE:1", Organism.HUMAN, 12345, "GENE1", "DISEASE:1", "disease", twoExactPhenotypeMatches);
        ModelPhenotypeMatchScore result = instance.scoreModel(model);

        System.out.println(result);
        assertThat(result.getScore(), equalTo(0.732228059966757));
    }

    @Test
    public void testScoreModelPerfectMatchModelAndUnmatchedQueryPhenotype() {
        List<PhenotypeTerm> queryTerms = new ArrayList<>(priorityService.getHpoTerms());
        queryTerms.add(PhenotypeTerm.of("HP:000000", "No match"));
        OrganismPhenotypeMatcher referenceOrganismPhenotypeMatcher = priorityService.getPhenotypeMatcherForOrganism(queryTerms, Organism.HUMAN);

        ModelScorer instance = ModelScorer.forSameSpecies(referenceOrganismPhenotypeMatcher);

        Model model = makeBestHumanModel(referenceOrganismPhenotypeMatcher);
        ModelPhenotypeMatchScore result = instance.scoreModel(model);

        System.out.println(result);
        assertThat(result.getScore(), equalTo(1.0));
    }

    @Test
    public void testScoreSingleCrossSpecies() {

        List<PhenotypeTerm> queryTerms = ImmutableList.copyOf(priorityService.getHpoTerms());
        OrganismPhenotypeMatcher mouseOrganismPhenotypeMatcher = priorityService.getPhenotypeMatcherForOrganism(queryTerms, Organism.MOUSE);

        ModelScorer mousePhiveModelScorer = ModelScorer.forSingleCrossSpecies(mouseOrganismPhenotypeMatcher);

        Model model = makeBestMouseModel(mouseOrganismPhenotypeMatcher);

        ModelPhenotypeMatchScore result = mousePhiveModelScorer.scoreModel(model);
        System.out.println(result);
        assertThat(result.getScore(), equalTo(1.0));
    }

    @Test
    public void testScoreMultiCrossSpecies() {

        List<PhenotypeTerm> queryTerms = ImmutableList.copyOf(priorityService.getHpoTerms());
        queryTerms.forEach(System.out::println);

        OrganismPhenotypeMatcher referenceOrganismPhenotypeMatcher = priorityService.getPhenotypeMatcherForOrganism(queryTerms, Organism.HUMAN);
        TheoreticalModel bestTheoreticalModel = referenceOrganismPhenotypeMatcher.getBestTheoreticalModel();


        ModelScorer diseaseModelScorer = ModelScorer.forMultiCrossSpecies(bestTheoreticalModel, referenceOrganismPhenotypeMatcher);
        Model disease = makeBestHumanModel(referenceOrganismPhenotypeMatcher);
        ModelPhenotypeMatchScore diseaseResult = diseaseModelScorer.scoreModel(disease);
        System.out.println(diseaseResult);
        assertThat(diseaseResult.getScore(), equalTo(1.0));


        OrganismPhenotypeMatcher mouseOrganismPhenotypeMatcher = priorityService.getPhenotypeMatcherForOrganism(queryTerms, Organism.MOUSE);
        ModelScorer mouseModelScorer = ModelScorer.forMultiCrossSpecies(bestTheoreticalModel, mouseOrganismPhenotypeMatcher);
        Model mouse = makeBestMouseModel(mouseOrganismPhenotypeMatcher);
        ModelPhenotypeMatchScore mouseResult = mouseModelScorer.scoreModel(mouse);
        System.out.println(mouseResult);
        assertThat(mouseResult.getScore(), equalTo(0.9718528996668048));


        OrganismPhenotypeMatcher fishOrganismPhenotypeMatcher = priorityService.getPhenotypeMatcherForOrganism(queryTerms, Organism.FISH);
        ModelScorer fishModelScorer = ModelScorer.forMultiCrossSpecies(bestTheoreticalModel, fishOrganismPhenotypeMatcher);
        Model fish = makeBestFishModel(fishOrganismPhenotypeMatcher);
        ModelPhenotypeMatchScore fishResult = fishModelScorer.scoreModel(fish);
        System.out.println(fishResult);
        assertThat(fishResult.getScore(), equalTo(0.628922135363762));
    }
}