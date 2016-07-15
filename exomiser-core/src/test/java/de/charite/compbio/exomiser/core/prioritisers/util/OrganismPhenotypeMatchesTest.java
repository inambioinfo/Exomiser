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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.charite.compbio.exomiser.core.model.Organism;
import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Created by jules on 03/06/2016.
 */
public class OrganismPhenotypeMatchesTest {

    private OrganismPhenotypeMatches instance;

    //Nose phenotypes
    private final PhenotypeTerm bigNose = new PhenotypeTerm("HP:0000001", "Big nose", 2.0);
    private final PhenotypeTerm nose = new PhenotypeTerm("HP:0000002", "Nose", 1.0);
    private final PhenotypeTerm littleNose = new PhenotypeTerm("HP:0000003", "Little nose", 2.0);

    private final PhenotypeMatch perfectNoseMatch = new PhenotypeMatch(bigNose, bigNose, 1.0, 4.0, bigNose);
    private final PhenotypeMatch noseMatch = new PhenotypeMatch(bigNose, littleNose, 0.5, 1.0, nose);

    //Toe phenotypes
    private final PhenotypeTerm toe = new PhenotypeTerm("HP:0000004", "Toe", 1.0);
    private final PhenotypeTerm bigToe = new PhenotypeTerm("HP:0000005", "Big toe", 2.0);
    private final PhenotypeTerm crookedToe = new PhenotypeTerm("HP:0000006", "Crooked toe", 2.0);
    private final PhenotypeTerm longToe = new PhenotypeTerm("HP:0000007", "Long toe", 2.0);

    private final PhenotypeMatch bestToeMatch = new PhenotypeMatch(bigToe, longToe, 1.0, 2.0, toe);
    private final PhenotypeMatch bigToeCrookedToeMatch = new PhenotypeMatch(bigToe, crookedToe, 1.0, 1.5, toe);

    private final Set<PhenotypeMatch> bestMatches = Sets.newHashSet(perfectNoseMatch, bestToeMatch);

    @Before
    public void setUp() {
        Map<PhenotypeTerm, Set<PhenotypeMatch>> phenotypeMatches = new LinkedHashMap<>();
        phenotypeMatches.put(bigNose, Sets.newHashSet(perfectNoseMatch, noseMatch));
        phenotypeMatches.put(bigToe, Sets.newHashSet(bestToeMatch, bigToeCrookedToeMatch));

        instance = new OrganismPhenotypeMatches(Organism.HUMAN, phenotypeMatches);
    }

    @Test
    public void emptyInputValues() throws Exception {
        OrganismPhenotypeMatches instance = new OrganismPhenotypeMatches(Organism.HUMAN, Collections.emptyMap());

        assertThat(instance.getOrganism(), equalTo(Organism.HUMAN));
        assertThat(instance.getQueryTerms(), equalTo(Collections.emptyList()));
        assertThat(instance.getTermPhenotypeMatches(), equalTo(Collections.emptyMap()));
    }

    @Test
    public void testEquals() {
        OrganismPhenotypeMatches emptyHumanOne = new OrganismPhenotypeMatches(Organism.HUMAN, Collections.emptyMap());
        OrganismPhenotypeMatches emptyMouseOne = new OrganismPhenotypeMatches(Organism.MOUSE, Collections.emptyMap());
        OrganismPhenotypeMatches emptyHumanTwo = new OrganismPhenotypeMatches(Organism.HUMAN, Collections.emptyMap());
        assertThat(emptyHumanOne, equalTo(emptyHumanTwo));
        assertThat(emptyHumanOne, not(equalTo(emptyMouseOne)));
    }

    @Test
    public void testToString() {
        System.out.println(new OrganismPhenotypeMatches(Organism.HUMAN, Collections.emptyMap()));
        System.out.println(instance);
    }

    @Test
    public void testGetMatchedHpIds() throws Exception {
        Set expected = Sets.newTreeSet(Arrays.asList("HP:0000001", "HP:0000005"));
        assertThat(instance.getMatchedHpIds(), equalTo(expected));
    }

    @Test
    public void testGetBestForwardAndReciprocalMatches_returnsEmptyListFromEmptyQuery() throws Exception {
        assertThat(instance.getBestForwardAndReciprocalMatches(Collections.emptyList()), equalTo(Collections.emptyList()));
    }

    @Test
    public void testGetBestForwardAndReciprocalMatches() throws Exception {
        List<String> modelPhenotypes = Lists.newArrayList(littleNose.getId(), longToe.getId());
        List<PhenotypeMatch> expected = Lists.newArrayList(noseMatch, bestToeMatch, noseMatch, bestToeMatch);
        expected.forEach(match -> System.out.printf("%s-%s=%f%n", match.getQueryPhenotypeId(), match.getMatchPhenotypeId(), match.getScore()));
        assertThat(instance.getBestForwardAndReciprocalMatches(modelPhenotypes), equalTo(expected));
    }

    @Test
    public void testCanGetTheoreticalBestModel() {
        assertThat(instance.getBestTheoreticalModel(), equalTo(new TheoreticalModel(Organism.HUMAN, instance.getTermPhenotypeMatches())));
    }
}