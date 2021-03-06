/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.filters;

import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FrequencyFilterTest {
    
    private FrequencyFilter instance;

    private VariantEvaluation passesEspAllFrequency;
    private VariantEvaluation passesEspAAFrequency;
    private VariantEvaluation passesEspEAFrequency;
    private VariantEvaluation passesDbsnpFrequency;

    private VariantEvaluation failsFrequency;
    private VariantEvaluation passesNoFrequencyData;

    private static final float FREQ_THRESHOLD = 0.1f;
    private static final float PASS_FREQ = FREQ_THRESHOLD - 0.02f;
    private static final float FAIL_FREQ = FREQ_THRESHOLD + 1.0f;

    private static final Frequency ESP_ALL_PASS = Frequency.valueOf(PASS_FREQ, FrequencySource.ESP_ALL);
    private static final Frequency ESP_ALL_FAIL = Frequency.valueOf(FAIL_FREQ, FrequencySource.ESP_ALL);

    private static final Frequency ESP_AA_PASS = Frequency.valueOf(PASS_FREQ, FrequencySource.ESP_AFRICAN_AMERICAN);

    private static final Frequency ESP_EA_PASS = Frequency.valueOf(PASS_FREQ, FrequencySource.ESP_EUROPEAN_AMERICAN);

    private static final Frequency DBSNP_PASS = Frequency.valueOf(PASS_FREQ, FrequencySource.THOUSAND_GENOMES);

    private static final FrequencyData espAllPassData = FrequencyData.of(RsId.empty(), ESP_ALL_PASS);
    private static final FrequencyData espAllFailData = FrequencyData.of(RsId.empty(), ESP_ALL_FAIL);
    private static final FrequencyData espAaPassData = FrequencyData.of(RsId.empty(), ESP_AA_PASS);
    private static final FrequencyData espEaPassData = FrequencyData.of(RsId.empty(), ESP_EA_PASS);
    private static final FrequencyData dbSnpPassData = FrequencyData.of(RsId.empty(), DBSNP_PASS);
    private static final FrequencyData noFreqData = FrequencyData.empty();

    @Before
    public void setUp() throws Exception {

        instance = new FrequencyFilter(FREQ_THRESHOLD);

        passesEspAllFrequency = makeTestVariantEvaluation();
        passesEspAllFrequency.setFrequencyData(espAllPassData);

        passesEspAAFrequency = makeTestVariantEvaluation();
        passesEspAAFrequency.setFrequencyData(espAaPassData);

        passesEspEAFrequency = makeTestVariantEvaluation();
        passesEspEAFrequency.setFrequencyData(espEaPassData);

        passesDbsnpFrequency = makeTestVariantEvaluation();
        passesDbsnpFrequency.setFrequencyData(dbSnpPassData);

        failsFrequency = makeTestVariantEvaluation();
        failsFrequency.setFrequencyData(espAllFailData);

        passesNoFrequencyData = makeTestVariantEvaluation();
        passesNoFrequencyData.setFrequencyData(noFreqData);

    }
    
    private VariantEvaluation makeTestVariantEvaluation() {
        return VariantEvaluation.builder(1, 1, "A", "T").build();
    }

    @Test
    public void getMaxFrequencyCutoff() {
        assertThat(instance.getMaxFreq(), equalTo(FREQ_THRESHOLD));
    } 
    
    @Test
    public void testGetFilterType() {
        assertThat(instance.getFilterType(), equalTo(FilterType.FREQUENCY_FILTER));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsIllegalArgumentExceptionWhenInstanciatedWithNegativeFrequency() {
        instance = new FrequencyFilter(-1f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsIllegalArgumentExceptionWhenInstanciatedWithFrequencyGreaterThanOneHundredPercent() {
        instance = new FrequencyFilter(101f);
    }

    @Test
    public void testFilterPassesVariantEvaluationWithFrequencyUnderThreshold() {
        instance = new FrequencyFilter(FREQ_THRESHOLD);
        System.out.println(passesEspAllFrequency + " " + passesEspAllFrequency.getFrequencyData());
        FilterResult filterResult = instance.runFilter(passesEspAllFrequency);

        FilterTestHelper.assertPassed(filterResult);
    }

    @Test
    public void testFilterPassesVariantEvaluationWithNoFrequencyData() {
        FilterResult filterResult = instance.runFilter(passesNoFrequencyData);
        FilterTestHelper.assertPassed(filterResult);
    }

    @Test
    public void testFilterFailsVariantEvaluationWithFrequencyDataAboveThreshold() {
        FilterResult filterResult = instance.runFilter(failsFrequency);
        FilterTestHelper.assertFailed(filterResult);
    }

    @Test
    public void testPassesFilterFails() {
        assertThat(instance.passesFilter(espAllFailData), is(false));
    }

    @Test
    public void testEaspAllPassesFilter() {
        assertThat(instance.passesFilter(espAllPassData), is(true));
    }

    @Test
    public void testNoFrequencyDataPassesFilter() {
        assertThat(instance.passesFilter(noFreqData), is(true));
    }

    @Test
    public void testHashCode() {
        FrequencyFilter otherFilter = new FrequencyFilter(FREQ_THRESHOLD);
        assertThat(instance.hashCode(), equalTo(otherFilter.hashCode()));
    }

    @Test
    public void testNotEqualNull() {
        Object obj = null;
        assertThat(instance.equals(obj), is(false));
    }

    @Test
    public void testNotEqualOtherObject() {
        Object obj = "Not equal to this";
        assertThat(instance.equals(obj), is(false));
    }

    @Test
    public void testNotEqualOtherFrequencyFilterWithDifferentThreshold() {
        FrequencyFilter otherFilter = new FrequencyFilter(FAIL_FREQ);
        assertThat(instance.equals(otherFilter), is(false));
    }

    @Test
    public void testEqualsSelf() {
        assertThat(instance.equals(instance), is(true));
    }

    @Test
    public void testToString() {
        System.out.println(instance);
        assertThat(instance.toString().isEmpty(), is(false));
    }

}
