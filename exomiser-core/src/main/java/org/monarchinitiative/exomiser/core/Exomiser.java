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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core;

import org.monarchinitiative.exomiser.core.analysis.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This is the main entry point for analysing data using the Exomiser. An {@link Analysis}
 * should be build using either a {@link Settings} and the {@link SettingsParser} or with an {@link AnalysisParser}
 * or programmtically using the {@link AnalysisBuilder}
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class Exomiser {

    private static final Logger logger = LoggerFactory.getLogger(Exomiser.class);

    private final AnalysisFactory analysisFactory;

    @Autowired
    public Exomiser(AnalysisFactory analysisFactory) {
        this.analysisFactory = analysisFactory;
    }

    public AnalysisBuilder getAnalysisBuilder() {
        return analysisFactory.getAnalysisBuilder();
    }

    public AnalysisResults run(Analysis analysis) {
        AnalysisMode analysisMode = analysis.getAnalysisMode();
        logger.info("Running analysis with mode: {}", analysisMode);
        AnalysisRunner analysisRunner = analysisFactory.getAnalysisRunnerForMode(analysisMode);
        return analysisRunner.run(analysis);
    }

}
