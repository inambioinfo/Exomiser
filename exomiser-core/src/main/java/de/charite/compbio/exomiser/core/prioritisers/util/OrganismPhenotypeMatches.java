/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.charite.compbio.exomiser.core.prioritisers.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.charite.compbio.exomiser.core.model.Organism;
import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.*;


/**
 * Stores the PhenotypeMatches for a set of query PhenotypeTerms for an Organism.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OrganismPhenotypeMatches {

    private static final Logger logger = LoggerFactory.getLogger(OrganismPhenotypeMatches.class);

    private final Organism organism;
    private final Map<PhenotypeTerm, Set<PhenotypeMatch>> termPhenotypeMatches;

    private final TheoreticalModel bestTheoreticalModel;

    private final Set<String> matchedOrganismPhenotypeIds;
    private final Set<String> matchedQueryPhenotypeIds;
    //make this private? It's not all that nice and only used here.
    private final Map<String, PhenotypeMatch> mappedTerms;

    /**
     * @param organism             - The organism for which these PhenotypeMatches are associated.
     * @param termPhenotypeMatches - Map of query PhenotypeTerms and their corresponding PhenotypeMatches. If there is no match then an empty Set of PhenotypeMatches is expected.
     */
    public OrganismPhenotypeMatches(Organism organism, Map<PhenotypeTerm, Set<PhenotypeMatch>> termPhenotypeMatches) {
        this.organism = organism;
        this.termPhenotypeMatches = ImmutableMap.copyOf(termPhenotypeMatches);

        this.bestTheoreticalModel = new TheoreticalModel(this.organism, this.termPhenotypeMatches);

        this.matchedOrganismPhenotypeIds = termPhenotypeMatches
                .values().stream()
                .flatMap(set -> set.stream().map(PhenotypeMatch::getMatchPhenotypeId))
                .collect(collectingAndThen(toCollection(TreeSet::new), Collections::unmodifiableSet));

        this.matchedQueryPhenotypeIds = termPhenotypeMatches
                .keySet().stream()
                .map(PhenotypeTerm::getId)
                .collect(collectingAndThen(toCollection(TreeSet::new), Collections::unmodifiableSet));
        logger.info("hpIds with phenotype match={}", matchedQueryPhenotypeIds);

        this.mappedTerms = getCompoundKeyIndexedPhenotypeMatches();
    }

    public Organism getOrganism() {
        return organism;
    }

    public List<PhenotypeTerm> getQueryTerms() {
        return ImmutableList.copyOf(termPhenotypeMatches.keySet());
    }

    public Map<PhenotypeTerm, Set<PhenotypeMatch>> getTermPhenotypeMatches() {
        return termPhenotypeMatches;
    }

    private Map<String, PhenotypeMatch> getCompoundKeyIndexedPhenotypeMatches() {
        //'hpId + mpId' : phenotypeMatch
        return termPhenotypeMatches.values().stream()
                .flatMap(Collection::stream)
                .collect(collectingAndThen(
                        toMap(match -> String.join("", match.getQueryPhenotypeId() + match.getMatchPhenotypeId()), Function.identity()),
                        Collections::unmodifiableMap));
    }

    public Set<String> getMatchedHpIds() {
        return matchedQueryPhenotypeIds;
    }

    public List<PhenotypeMatch> getBestForwardAndReciprocalMatches(List<String> modelPhenotypes) {
        List<String> matchedModelPhenotypeIds = modelPhenotypes.stream()
                .filter(matchedOrganismPhenotypeIds::contains)
                .collect(toList());

        //loop - 191, 206, 211, 293, 260, 221, 229, 247, 203, 204. (226 ms)
        //stream - 1208, 773, 1231, 799, 655, 566, 467, 1037, 792, 722. (825 ms)
        //This takes ~0.7 secs compared to ~0.2 secs using the original loop implementation, although it is now returning
        //the values. Can it be made faster? Do we care?
        List<PhenotypeMatch> forwardMatches = matchedQueryPhenotypeIds.stream()
                .map(hp -> matchedModelPhenotypeIds.stream()
                        .map(mp -> String.join("", hp, mp))
                        .map(mappedTerms::get)
                        .filter(match -> match != null)
                        .max(comparingDouble(PhenotypeMatch::getScore)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());

        //CAUTION!!! This looks very similar to the forward match statement but there are several important differences...
        List<PhenotypeMatch> reciprocalMatches = matchedModelPhenotypeIds.stream()
                .map(mp -> matchedQueryPhenotypeIds.stream()
                        .map(hp -> String.join("", hp, mp))
                        .map(mappedTerms::get)
                        .filter(match -> match != null)
                        .max(comparingDouble(PhenotypeMatch::getScore)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());

        return Stream.concat(forwardMatches.stream(), reciprocalMatches.stream()).collect(collectingAndThen(toList(), ImmutableList::copyOf));
    }

    public TheoreticalModel getBestTheoreticalModel() {
        return bestTheoreticalModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrganismPhenotypeMatches)) return false;
        OrganismPhenotypeMatches that = (OrganismPhenotypeMatches) o;
        return organism == that.organism &&
                Objects.equals(termPhenotypeMatches, that.termPhenotypeMatches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organism, termPhenotypeMatches);
    }


    @Override
    public String toString() {
        return "OrganismPhenotypeMatches{" +
                "organism=" + organism +
                ", termPhenotypeMatches=" + termPhenotypeMatches +
                '}';
    }
}